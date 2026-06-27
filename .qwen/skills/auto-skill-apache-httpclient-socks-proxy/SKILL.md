---
name: apache-httpclient-socks-proxy
description: Implement SOCKS proxy support for Apache HttpClient 4.x with remote DNS resolution, SSL consistency, and proper resource lifecycle management. (Replaced by JDK 17 native approach — see jdk8-to-jdk17-migration skill for java.net.http.HttpClient SOCKS handling.)
source: auto-skill
extracted_at: '2026-06-27T03:26:23.863Z'
status: legacy
---

> **Note**: This skill describes the Apache HttpClient 4.x approach which has been replaced. The project has migrated from Apache HttpClient to JDK 17's `java.net.http.HttpClient`. For SOCKS proxy with the new HTTP client, see the `jdk8-to-jdk17-migration` skill.

# Apache HttpClient SOCKS Proxy Implementation

## Overview

When adding SOCKS proxy support to Apache HttpClient 4.x, you must implement custom `ConnectionSocketFactory` instances that handle remote DNS resolution, SSL wrapping, port defaults, and connection manager lifecycle. Several subtle gotchas exist that can cause silent failures, resource leaks, or behavioral inconsistency with non-proxy paths.

## Key Implementation Pattern

### 1. Remote DNS Resolution via `InetSocketAddress.createUnresolved`

SOCKS proxies should resolve DNS on the proxy side (remote DNS), not locally. Use `InetSocketAddress.createUnresolved(hostname, port)` instead of `new InetSocketAddress(hostname, port)`:

```java
ConnectionSocketFactory socksSocketFactory = new ConnectionSocketFactory() {
    @Override
    public Socket createSocket(HttpContext context) throws IOException {
        return new Socket(socksProxy);
    }
    @Override
    public Socket connectSocket(int connectTimeout, Socket socket,
            HttpHost host, InetSocketAddress remoteAddress,
            InetSocketAddress localAddress, HttpContext context) throws IOException {
        socket.setSoTimeout(soTimeout);
        // CRITICAL: resolve default port when host.getPort() returns -1
        int port = host.getPort() > 0 ? host.getPort()
                : ("https".equalsIgnoreCase(host.getSchemeName()) ? 443 : 80);
        InetSocketAddress unresolved = InetSocketAddress.createUnresolved(
                host.getHostName(), port);
        socket.connect(unresolved, connectTimeout);
        return socket;
    }
};
```

### 2. Port -1 Gotcha

`HttpHost.getPort()` returns `-1` when the URL doesn't include an explicit port (e.g., `http://example.com`). Passing `-1` to `InetSocketAddress.createUnresolved` throws `IllegalArgumentException`. Always resolve defaults:

```java
int port = host.getPort() > 0 ? host.getPort()
        : ("https".equalsIgnoreCase(host.getSchemeName()) ? 443 : 80);
```

### 3. SSL Consistency with Trust-All Paths

If your non-proxy HTTPS path uses a trust-all `SSLContext` (common in internal tools), the SOCKS SSL path **must** use the same trust-all strategy. Using `SSLSocketFactory.getDefault()` in the SOCKS path will cause `SSLHandshakeException` for self-signed certificates that work fine without a proxy:

```java
// Build trust-all SSLContext consistent with existing createSSLConnSocketFactory()
final javax.net.ssl.SSLSocketFactory trustAllSslFactory;
try {
    SSLContext sslContext = new SSLContextBuilder()
            .loadTrustMaterial(null, (chain, authType) -> true)
            .build();
    trustAllSslFactory = sslContext.getSocketFactory();
} catch (GeneralSecurityException e) {
    throw new RuntimeException("Failed to create trust-all SSL context for SOCKS proxy", e);
}

ConnectionSocketFactory socksSslSocketFactory = new ConnectionSocketFactory() {
    @Override
    public Socket createSocket(HttpContext context) throws IOException {
        return new Socket(socksProxy);
    }
    @Override
    public Socket connectSocket(int connectTimeout, Socket socket,
            HttpHost host, InetSocketAddress remoteAddress,
            InetSocketAddress localAddress, HttpContext context) throws IOException {
        socket.setSoTimeout(soTimeout);
        int port = host.getPort() > 0 ? host.getPort() : 443;
        InetSocketAddress unresolved = InetSocketAddress.createUnresolved(
                host.getHostName(), port);
        socket.connect(unresolved, connectTimeout);
        // Use trust-all factory, NOT SSLSocketFactory.getDefault()
        javax.net.ssl.SSLSocket sslSocket = (javax.net.ssl.SSLSocket) trustAllSslFactory
                .createSocket(socket, host.getHostName(), port, true);
        sslSocket.startHandshake();
        return sslSocket;
    }
};
```

### 4. ConnectionManager Lifecycle

The SOCKS `PoolingHttpClientConnectionManager` must be assigned to an instance field and properly shut down before creating a new one. Creating it as a local variable leaks threads and sockets on every request:

```java
// WRONG: local variable leaks every time
PoolingHttpClientConnectionManager socksConnMgr = new PoolingHttpClientConnectionManager(registry);

// CORRECT: shutdown old, assign new
if (this.connMgr != null) {
    this.connMgr.shutdown();  // Use shutdown(), not close() — close() throws IOException
}
this.connMgr = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
```

**Note**: Use `shutdown()` instead of `close()` — `close()` declares `throws IOException` which may not be compatible with the enclosing method signature.

### 5. Registry and Client Builder

```java
Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
        .<ConnectionSocketFactory>create()
        .register("http", socksSocketFactory)
        .register("https", socksSslSocketFactory)
        .build();

httpclient = HttpClientBuilder.create()
        .setDefaultRequestConfig(config)
        .setConnectionManager(this.connMgr)
        .setDefaultCookieStore(_CookieStore)
        .build();
```

### 6. HTTP Proxy (non-SOCKS) Path

For HTTP/HTTPS proxies, use Apache HttpClient's built-in `HttpHost` proxy support rather than custom socket factories:

```java
HttpHost proxy = new HttpHost(this._ProxyHost, this._ProxyPort, this._ProxyScheme);
config = RequestConfig.copy(config).setProxy(proxy).build();
```

## Parameter Validation

Always validate proxy parameters at the setter level, not at connection time:

```java
public void setProxy(String host, int port, String scheme) {
    if (host == null || host.trim().isEmpty()) {
        throw new IllegalArgumentException("Proxy host cannot be null or empty");
    }
    if (port < 1 || port > 65535) {
        throw new IllegalArgumentException("Proxy port must be between 1 and 65535");
    }
    if (scheme != null && !"http".equalsIgnoreCase(scheme)
            && !"https".equalsIgnoreCase(scheme)
            && !"socks".equalsIgnoreCase(scheme)) {
        throw new IllegalArgumentException("Unsupported proxy scheme: " + scheme);
    }
    this._ProxyHost = host;
    this._ProxyPort = port;
    this._ProxyScheme = scheme != null ? scheme.toLowerCase() : "http";
}
```

**Two-parameter overload must reset scheme**: If `setProxy(host, port)` is called after `setProxy(host, port, "socks")`, the scheme should reset to `"http"`:

```java
public void setProxy(String host, int port) {
    // ... validation ...
    this._ProxyHost = host;
    this._ProxyPort = port;
    this._ProxyScheme = "http";  // IMPORTANT: reset scheme
}
```

## Common Pitfalls Summary

| Pitfall | Symptom | Fix |
|---------|---------|-----|
| `host.getPort()` returns -1 | `IllegalArgumentException` from `createUnresolved` | Resolve default port (80/443) |
| SOCKS SSL uses `SSLSocketFactory.getDefault()` | `SSLHandshakeException` for self-signed certs | Use same trust-all SSLContext as non-proxy path |
| `PoolingHttpClientConnectionManager` as local variable | Thread/socket leak, eventual OOM | Assign to instance field, `shutdown()` old before new |
| `connMgr.close()` | Compilation error (unchecked IOException) | Use `shutdown()` instead |
| `setProxy(h,p)` doesn't reset scheme | Previous "socks" scheme persists | Reset `_ProxyScheme = "http"` in two-param overload |
| No parameter validation | Cryptic errors at connection time | Validate at setter with clear messages |
