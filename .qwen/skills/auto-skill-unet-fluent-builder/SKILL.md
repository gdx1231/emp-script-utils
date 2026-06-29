---
name: unet-fluent-builder
description: Fluent builder API for UNet HTTP client — chainable header/agent/timeout/proxy/cookie/trustAllCert setters with short get/post/put/delete/download terminal methods
source: auto-skill
extracted_at: '2026-06-27T06:17:26.483Z'
---

# UNet Fluent Builder API

Fluent method-chaining wrapper around UNet's existing HTTP methods.

## Pattern

```java
String result = UNet.getInstance()
    .header("Authorization", "Bearer token")
    .agent("MyApp/1.0")
    .timeout(5000)
    .get("https://api.example.com/data");
```

## Chainable setters

All return `this`:

| Method | Delegates to |
|--------|-------------|
| `.header(key, value)` | `addHeader(key, value)` |
| `.agent(userAgent)` | `setUserAgent(userAgent)` |
| `.timeout(ms)` | `setTimeout(ms)` |
| `.proxy(host, port)` | `setProxy(host, port)` |
| `.proxy(host, port, scheme)` | `setProxy(host, port, scheme)` |
| `.proxy(host, port, scheme, user, pwd)` | `setProxy(host, port, scheme, user, pwd)` |
| `.encoding(charset)` | `setEncode(charset)` |
| `.cookie(cookieStr)` | `setCookie(cookieStr)` |
| `.trustAllCert(bool)` | `setTrustAllCert(bool)` |
| `.log(bool)` | `setIsShowLog(bool)` |

## Terminal methods

End the chain, execute the request:

| Method | Returns | Delegates to |
|--------|---------|-------------|
| `.get(url)` | String | `doGet(url)` |
| `.post(url, body)` | String | `doPost(url, body)` |
| `.put(url, body)` | String | `doPut(url, body)` |
| `.delete(url)` | String | `doDelete(url)` |
| `.download(url)` | byte[] | `downloadData(url)` |

## Conflict note

`patch(url, body)` already exists as a public method — do NOT add a fluent `patch()`. Use the existing instance method.

## Placement

Add after the SSE section and before the closing `}`. The `getInstance()` static factory and all fluent methods live together in a `// ===================== Fluent builder API =====================` block at the end of `UNet.java`.
