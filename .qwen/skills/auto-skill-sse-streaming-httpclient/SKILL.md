---
name: sse-streaming-httpclient
description: Implement SSE (Server-Sent Events) streaming with java.net.http.HttpClient and streaming response handling in JDK 17+.
source: auto-skill
extracted_at: '2026-06-27T10:17:00.000Z'
---

# SSE Streaming with java.net.http.HttpClient (JDK 17+)

## Overview

JDK 17's `java.net.http.HttpClient` can handle streaming HTTP responses (like SSE Server-Sent Events) using `HttpResponse.BodyHandlers.ofInputStream()`. This skill covers the patterns for: streaming HTTP, SSE protocol parsing, auto-reconnect architecture, and testing with `com.sun.net.httpserver.HttpServer`.

## Streaming with BodyHandlers.ofInputStream

`HttpResponse.BodyHandlers.ofLines()` buffers lines into a `Stream<String>`, which is memory-intensive and not suitable for long-running SSE connections. Use `BodyHandlers.ofInputStream()` instead:

```java
HttpClient client = HttpClient.newBuilder().build();
HttpRequest request = HttpRequest.newBuilder(URI.create(url))
    .header("Accept", "text/event-stream")
    .header("Cache-Control", "no-cache")
    .GET()
    .build();

HttpResponse<InputStream> response = client.send(request,
        HttpResponse.BodyHandlers.ofInputStream());

// response.body() returns an InputStream — must close it in finally
try (InputStream in = response.body()) {
    // Read and parse SSE events
} catch (IOException e) {
    // Handle connection drops
}
```

**Key point**: `response.body().close()` is mandatory — the InputStream returned by `BodyHandlers.ofInputStream()` is connected to the underlying network socket and must be explicitly closed to release resources.

## SSE Protocol Parsing

SSE events are `text/event-stream` lines separated by double newlines (`\n\n`). Each event can contain `event:`, `data:`, `id:`, and `retry:` fields.

### Full parser implementation

```java
private void parseSseEvents(InputStream in, SseListener listener) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
    SseEvent current = new SseEvent();
    StringBuilder dataBuf = new StringBuilder();
    boolean hasData = false;

    String line;
    while ((line = reader.readLine()) != null) {
        if (line.isEmpty()) {
            // Empty line = event boundary → fire event
            if (hasData) {
                current.data = dataBuf.toString();
                listener.onEvent(current);
            }
            current = new SseEvent();
            dataBuf = new StringBuilder();
            hasData = false;
            continue;
        }

        // Lines beginning with colon are comments — skip
        if (line.charAt(0) == ':') {
            continue;
        }

        int colon = line.indexOf(':');
        String field, value;
        if (colon > 0) {
            field = line.substring(0, colon);
            value = line.substring(colon + 1);
            if (value.startsWith(" ")) {
                value = value.substring(1); // trim single leading space per spec
            }
        } else {
            field = line;
            value = "";
        }

        switch (field) {
            case "event": current.event = value; break;
            case "data":
                if (hasData) dataBuf.append("\n"); // multi-line data joined with \n
                dataBuf.append(value);
                hasData = true;
                break;
            case "id": current.id = value; break;
            case "retry":
                try { current.retry = Long.parseLong(value); }
                catch (NumberFormatException ignored) {}
                break;
        }
    }
}
```

### SSE field rules (per spec)

| Field | Behavior |
|-------|----------|
| `data:` | Multiple consecutive `data:` lines joined with `\n` (not `\r\n`) |
| `event:` | Event type name. Absence = unnamed event |
| `id:` | Last-Event-ID for reconnect. Empty string resets ID |
| `retry:` | Milliseconds. Server overrides client default |
| `:` prefix | Comment line, ignored entirely |

## Data Model

```java
public static class SseEvent {
    private String event;  // nullable
    private String data;   // never null if event fired
    private String id;     // nullable
    private Long retry;    // nullable, milliseconds
    // getters only — events are read-only
}

@FunctionalInterface
public interface SseListener {
    void onEvent(SseEvent event);
    default void onError(Exception e) {}
    default void onComplete() {}
}
```

**Design choices**:
- `SseListener` uses `@FunctionalInterface` with default methods for `onError`/`onComplete` — callers only need to implement `onEvent` for simple cases (lambda syntax works: `event -> process(event)`)
- For error handling, create an anonymous class overriding `onError`
- `SseEvent` fields are immutable after construction — set only by the parser

## Request Construction

SSE requests need specific headers (`Accept: text/event-stream`, `Cache-Control: no-cache`). Separate SSE-specific header setup from the general request builder:

```java
private HttpRequest buildSseRequest(String url, String body) {
    HttpRequest.Builder builder = newRequestBuilder(url);  // sets User-Agent, Cookie, custom headers
    builder.header("Accept", "text/event-stream");
    builder.header("Cache-Control", "no-cache");

    if (body != null) {
        builder.POST(HttpRequest.BodyPublishers.ofString(body, charset));
    } else {
        builder.GET();
    }
    return builder.build();
}
```

**Why separate**: `newRequestBuilder()` handles cookies/headers/user-agent universally. SSE-specific headers are only added here, keeping the general builder clean for non-SSE requests.

### POST SSE (OpenAI / Anthropic / custom APIs)

Many streaming APIs use POST to initiate the stream with a JSON body:

```java
public void doSse(String url, String body, SseListener listener) {
    HttpClient client = getHttpClient(url);
    HttpRequest request = buildSseRequest(url, body);
    // ... retry loop using executeSse(client, request, listener) ...
}
```

Test pattern for POST SSE:
```java
server.createContext("/sse/post", exchange -> {
    byte[] reqBody = exchange.getRequestBody().readAllBytes();
    exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
    exchange.getResponseHeaders().add("Cache-Control", "no-cache");
    exchange.sendResponseHeaders(200, 0);
    OutputStream os = exchange.getResponseBody();
    String body = new String(reqBody, "UTF-8");
    os.write(("event: echo\ndata: " + body + "\n\n").getBytes());
    os.flush();
    os.close();
});

// Test: verify body was received by server
net.doSseAsync(baseUrl + "/sse/post", "{\"query\":\"test\"}", event -> { ... });
```

## Reconnect Architecture

Split into two layers:

| Layer | Responsibility | Returns |
|-------|---------------|---------|
| `executeSse(client, request, listener)` | Single connection attempt | `true` = recoverable error (IO drop), `false` = don't retry (4xx/5xx, normal EOF) |
| `doSse(url, body, listener)` | Retry loop with backoff | void |

```java
private boolean executeSse(HttpClient client, HttpRequest request, SseListener listener) {
    HttpResponse<InputStream> response = null;
    try {
        response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) {
            // 4xx/5xx — not retryable
            byte[] errBody = response.body().readAllBytes();
            listener.onError(new IOException("SSE HTTP " + response.statusCode()));
            return false;
        }
        parseSseEvents(response.body(), listener);
        return false; // EOF — normal completion
    } catch (IOException e) {
        listener.onError(e);
        return true; // IO error during streaming = potentially recoverable
    } catch (Exception e) {
        listener.onError(e);
        return false;
    } finally {
        if (response != null) {
            try { response.body().close(); } catch (IOException ignored) {}
        }
    }
}

public void doSse(String url, String body, SseListener listener) {
    HttpClient client = getHttpClient(url);
    HttpRequest request = buildSseRequest(url, body);
    int retries = 0;

    while (true) {
        boolean shouldReconnect = executeSse(client, request, listener);
        if (!shouldReconnect) {
            if (reconnectEnabled && retries < maxRetries) {
                Thread.sleep(retryIntervalMs); // or server's retry value
                retries++;
                continue;
            }
            listener.onComplete();
            return;
        }
        // Connection is still streaming — shouldn't happen but handle gracefully
        return;
    }
}
```

**Key design decisions**:
1. `executeSse` returns `boolean` to separate connection-level concerns from retry policy
2. IO exceptions during streaming return `true` (retryable) because they're often transient network issues
3. Non-200 status codes return `false` (not retryable) because re-requesting the same URL would get the same error
4. `response.body()` is always closed in `finally` — even on error paths
5. `HttpRequest` is immutable, so re-sending the same request object is safe

## Async API (CompletableFuture)

```java
public CompletableFuture<Void> doSseAsync(String url, String body, SseListener listener) {
    return CompletableFuture.runAsync(() -> {
        doSse(url, body, listener);
    });
}
```

**Note**: `runAsync()` uses the common ForkJoinPool by default. The caller receives a `CompletableFuture<Void>` that completes when the SSE stream ends, enabling `.thenRun()`, `.join()`, `.get()`, or composition with other futures.

## Testing SSE with com.sun.net.httpserver

Use `HttpServer` with `sendResponseHeaders(200, 0)` (0 = chunked/streaming mode) and `os.flush()` after each event:

```java
server.createContext("/sse/basic", exchange -> {
    exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
    exchange.getResponseHeaders().add("Cache-Control", "no-cache");
    exchange.sendResponseHeaders(200, 0); // 0 = chunked
    OutputStream os = exchange.getResponseBody();

    os.write("id: 1\ndata: {\"msg\":\"hello\"}\n\n".getBytes());
    os.flush(); // CRITICAL: flush each event so client receives it

    os.write("event: update\nid: 2\ndata: {\"msg\":\"world\"}\n\n".getBytes());
    os.flush();

    os.write("id: 3\ndata: {\"msg\":\"done\"}\n\n".getBytes());
    os.flush();
    os.close(); // close triggers EOF on client side
});
```

### Testing async SSE with CountDownLatch

```java
@Test
public void testSseBasic() throws Exception {
    List<UNet.SseEvent> events = new ArrayList<>();
    CountDownLatch latch = new CountDownLatch(3); // expect 3 events

    net.doSseAsync(url, event -> {
        events.add(event);
        latch.countDown();
    });

    assertTrue(latch.await(5, TimeUnit.SECONDS), "Should receive all 3 events within 5s");
    assertEquals(3, events.size());
    assertEquals("1", events.get(0).getId());
    assertTrue(events.get(0).getData().contains("hello"));
}
```

### Testing sync SSE (blocking)

```java
@Test
public void testSseSyncBlocking() throws Exception {
    List<UNet.SseEvent> events = new ArrayList<>();
    Thread t = new Thread(() -> {
        net.doSse(url, events::add); // blocking call
    });
    t.start();
    t.join(5000);
    assertFalse(t.isAlive(), "SSE blocking call should complete");
    assertEquals(3, events.size());
}
```

### Testing error handling (4xx/5xx)

```java
server.createContext("/sse/error", exchange -> {
    exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
    exchange.sendResponseHeaders(404, 0);
    exchange.close();
});

// Test: non-200 should trigger onError, not onEvent
List<Exception> errors = new ArrayList<>();
CountDownLatch latch = new CountDownLatch(1);

UNet.SseListener listener = new UNet.SseListener() {
    @Override
    public void onEvent(SseEvent e) {}
    @Override
    public void onError(Exception e) {
        errors.add(e);
        latch.countDown();
    }
};

net.doSseAsync(url + "/sse/error", listener);
assertTrue(latch.await(5, TimeUnit.SECONDS));
assertTrue(errors.get(0).getMessage().contains("404"));
```

**Note**: Use anonymous class (not lambda) when overriding `onError` — lambda only implements `onEvent`.

### Testing reconnect

```java
server.createContext("/sse/reconnect", exchange -> {
    exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
    exchange.getResponseHeaders().add("Cache-Control", "no-cache");
    exchange.sendResponseHeaders(200, 0);
    OutputStream os = exchange.getResponseBody();
    os.write("retry: 500\ndata: first\n\n".getBytes());
    os.flush();
    os.close(); // close triggers reconnect
});

// Test: server closes after first event; client reconnects and gets it again
net.setSseReconnect(true, 3, 100);
net.doSseAsync(url, event -> { /* should fire multiple times */ });
```

## Common Pitfalls

| Pitfall | Symptom | Fix |
|---------|---------|-----|
| `BodyHandlers.ofLines()` for SSE | Memory growth, buffering delay | Use `BodyHandlers.ofInputStream()` |
| Not closing `response.body()` InputStream | Socket leak | Always close in `finally` block |
| `sendResponseHeaders(200, length)` vs `(200, 0)` | Connection closes after fixed bytes or hangs waiting | Use `(200, 0)` for chunked streaming |
| Not flushing after each event | Client receives all events at once on close | `os.flush()` after each event |
| Multi-line `data:` fields joined with `\r\n` | Non-spec-compliant behavior | Join with `\n` only |
| Reconnect on 4xx/5xx | Infinite retry loop on bad URL | Only retry on `IOException`, not HTTP errors |
| Forgetting to call `response.body().readAllBytes()` in error path | Stream not consumed, connection not reusable | Always consume error body before returning |

## Reconnect Configuration Pattern

Expose reconnect settings through a single setter with sensible defaults:

```java
private boolean _SseReconnectEnabled;
private int _SseRetryMax = 3;
private long _SseRetryIntervalMs = 3000;

public void setSseReconnect(boolean enabled, int maxRetries, long retryIntervalMs) {
    this._SseReconnectEnabled = enabled;
    this._SseRetryMax = maxRetries;
    this._SseRetryIntervalMs = retryIntervalMs;
}
```

The retry loop in `doSse()` uses these fields, but also honors the server's `retry:` field value if present in `SseEvent.retry`. When server provides a retry value, it overrides the configured `_SseRetryIntervalMs` for that reconnection cycle.

## CompletableFuture Composition

Since `doSseAsync()` returns `CompletableFuture<Void>`, multiple SSE streams can be composed:

```java
CompletableFuture<Void> f1 = net1.doSseAsync(url1, e -> process1(e.getData()));
CompletableFuture<Void> f2 = net2.doSseAsync(url2, e -> process2(e.getData()));

// Wait for both streams to end
CompletableFuture.allOf(f1, f2).join();

// Or chain continuation
f1.thenRun(() -> System.out.println("Stream 1 ended"));
```
