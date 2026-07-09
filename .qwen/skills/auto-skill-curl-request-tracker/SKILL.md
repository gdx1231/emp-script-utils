---
name: curl-request-tracker
description: Pattern for adding request tracking and curl command generation to HTTP client wrapper classes
source: auto-skill
extracted_at: '2026-07-09T04:55:02.535Z'
---

# Adding Request Tracking & Curl Generation to HTTP Client Wrappers

## When to Apply

When an HTTP client wrapper class needs debugging/diagnostic capability to reproduce the last request as a `curl` command.

## Procedure

### 1. Add Tracking Fields

Add private fields to capture the last request's method and body:

```java
private String _LastMethod;
private String _LastBody;
```

The URL is typically already tracked (e.g., `_LastUrl`).

### 2. Add a Recording Helper

```java
private void recordLastRequest(String method, String body) {
    this._LastMethod = method;
    this._LastBody = body;
}
```

### 3. Insert Recording Calls at Every Request Entry Point

Call `recordLastRequest(method, body)` at the **top** of each public HTTP method (doGet, doPost, doPut, doDelete, doPatch, downloadData, doUpload, etc.), **before** any network I/O. This ensures tracking even if the request fails.

- For `String body` overloads: pass the body directly
- For `Map<String, String>` form param overloads: encode to `key=value&...` format first
- For `byte[]` overloads: record as `"<binary N bytes>"`
- For multipart upload overloads: record as `"<multipart upload>"`

### 4. Implement `createLastCurl()`

Build the curl command with `\\\n  ` (backslash + newline + 2 spaces) for line continuation:

```
curl -X METHOD \
  'URL' \
  -H 'User-Agent: ...' \
  -H 'Header: value' \
  -H 'Cookie: ...' \
  --proxy 'scheme://host:port' \
  -d 'body'
```

Key details:
- Wrap URL, header values, cookie, and body in **single quotes**
- Escape single quotes in values: `'` → `'\''`
- Include User-Agent only if set
- Include proxy only if configured
- Include `-d` only if body is non-null and non-empty
- Return `null` if no request has been made yet

### 5. Shell Escape Helper

```java
private static String escapeShell(String value) {
    if (value == null) return "";
    return value.replace("'", "'\\''");
}
```

### 6. Form Data Encoder (if not already present)

```java
private String encodeFormData(Map<String, String> vals) {
    if (vals == null || vals.isEmpty()) return null;
    StringBuilder sb = new StringBuilder();
    String charset = this._Encode == null ? "UTF-8" : this._Encode;
    for (Map.Entry<String, String> entry : vals.entrySet()) {
        if (sb.length() > 0) sb.append("&");
        sb.append(URLEncoder.encode(entry.getKey(), charset))
          .append("=")
          .append(URLEncoder.encode(entry.getValue(), charset));
    }
    return sb.toString();
}
```

## Testing Approach

- Test with a local `HttpServer` (from `com.sun.net.httpserver.HttpServer`) to avoid real network calls
- Verify each HTTP method produces correct `-X METHOD`
- Verify body content appears in `-d` flag
- Verify headers, cookies, proxy appear as `-H` / `--proxy`
- Test shell escaping with values containing single quotes
- Test null/empty cases return null
- Test line break format (`\\\n`)

## Cross-Branch Application

When applying to multiple branches (e.g., main vs jdk17 with different HTTP backends):
- The tracking fields and `createLastCurl()` method are identical across branches
- Only the insertion points differ (different internal HTTP client APIs)
- Use `git stash` workflow: stash main changes → checkout target branch → apply same pattern → stash → checkout main → pop
