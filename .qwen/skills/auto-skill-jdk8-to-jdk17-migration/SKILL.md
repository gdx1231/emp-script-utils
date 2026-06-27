---
name: jdk8-to-jdk17-migration
description: Comprehensive JDK 8 to 17 migration with dependency reduction — replacing commons-io, commons-lang3, commons-exec, Apache HttpClient with JDK 17 built-in APIs.
source: auto-skill
extracted_at: '2026-06-27T02:50:09.178Z'
---

# JDK 8 → 17 Migration with Dependency Reduction

## Overview

When migrating a Java library from JDK 8 to JDK 17, many Apache Commons and other third-party dependencies can be replaced with JDK built-in APIs. This skill covers the specific replacements, gotchas, and patterns discovered during a real migration.

## Dependency Replacement Map

### commons-io → JDK 17 NIO/IO

| commons-io | JDK 17 Equivalent |
|-----------|-------------------|
| `IOUtils.toByteArray(url)` | `url.openStream().readAllBytes()` |
| `IOUtils.toByteArray(inputStream)` | `inputStream.readAllBytes()` (JDK 9+) |
| `IOUtils.toString(url, charset)` | `new String(url.openStream().readAllBytes(), charset)` |
| `IOUtils.toString(is, charset)` | `new String(is.readAllBytes(), charset)` |
| `FileUtils.readFileToString(file, charset)` | `Files.readString(file.toPath(), charset)` (JDK 11+) |
| `FileUtils.write(file, content, charset)` | `Files.writeString(file.toPath(), content, charset)` (JDK 11+) |
| `FileUtils.writeByteArrayToFile(file, bytes)` | `Files.write(file.toPath(), bytes)` |
| `FileUtils.copyFile(from, to)` | `Files.copy(Path.of(from), Path.of(to), StandardCopyOption.REPLACE_EXISTING)` |

### commons-lang3 → JDK 11+

| commons-lang3 | JDK Equivalent |
|--------------|----------------|
| `StringUtils.isBlank(s)` | `s == null || s.isBlank()` (JDK 11+) |
| `StringUtils.isEmpty(s)` | `s == null || s.isEmpty()` |
| `BooleanUtils.toBoolean(s)` | `Boolean.parseBoolean(s)` — BUT note: `BooleanUtils` also recognizes "yes", "on", "1", etc. Must add manual checks for these. |

**BooleanUtils replacement pattern:**
```java
// BooleanUtils.toBoolean recognizes "true", "yes", "on", "1", "y", "t" etc.
String v = object.toString().trim().toLowerCase();
if (Boolean.parseBoolean(v) || "true".equals(v)
        || "yes".equals(v) || "on".equals(v) || "y".equals(v) || "t".equals(v)
        || "1".equals(v)) {
    return true;
}
```

### commons-exec → ProcessBuilder

| commons-exec | JDK Equivalent |
|-------------|----------------|
| `CommandLine.parse(line)` | `new ProcessBuilder("sh", "-c", line)` (Unix) or `new ProcessBuilder("cmd", "/c", line)` (Windows) |
| `DefaultExecutor` + `ExecuteWatchdog(60000)` | `process.waitFor(60, TimeUnit.SECONDS)` + `process.destroyForcibly()` |
| `PumpStreamHandler(outputStream)` | `process.getInputStream().readAllBytes()` |

**Full replacement pattern:**
```java
ProcessBuilder pb = new ProcessBuilder("sh", "-c", command);
if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
    pb = new ProcessBuilder("cmd", "/c", command);
}
pb.redirectErrorStream(true);
Process process = pb.start();
byte[] output = process.getInputStream().readAllBytes();
boolean finished = process.waitFor(60, TimeUnit.SECONDS);
if (!finished) {
    process.destroyForcibly();
    // handle timeout
}
int exitCode = process.exitValue();
```

### Apache HttpClient 4.x → java.net.http.HttpClient (JDK 11+)

This is the largest migration. Key mappings:

| Apache HttpClient | java.net.http Equivalent |
|-------------------|--------------------------|
| `CloseableHttpClient` | `java.net.http.HttpClient` (built-in connection pooling) |
| `HttpGet/Post/Put/Delete` | `HttpRequest.newBuilder().GET()/POST()/PUT()/DELETE()` |
| `HttpPatch` | `HttpRequest.newBuilder().method("PATCH", bodyPublisher)` |
| `CloseableHttpResponse` | `HttpResponse<String>` or `HttpResponse<byte[]>` |
| `StringEntity(body)` | `HttpRequest.BodyPublishers.ofString(body)` |
| `ByteArrayEntity(bytes)` | `HttpRequest.BodyPublishers.ofByteArray(bytes)` |
| `UrlEncodedFormEntity` | Manual URL encoding with `URLEncoder` |
| `MultipartEntityBuilder` | Manual multipart body with `ByteArrayOutputStream` |
| `BasicCookieStore` | `Map<String, String>` cookie storage |
| `RequestConfig.setConnectTimeout()` | `HttpClient.Builder.connectTimeout(Duration)` |
| `SSLConnectionSocketFactory` + `TrustStrategy` | `SSLContext` + `TrustManager` |
| `HttpHost` proxy | `ProxySelector.of(InetSocketAddress)` |
| `EntityUtils.toString(entity)` | `response.body()` |
| `EntityUtils.toByteArray(entity)` | `HttpResponse.BodyHandlers.ofByteArray()` |

#### PATCH method workaround
`java.net.http.HttpClient` doesn't have a native PATCH method. Use:
```java
builder.method("PATCH", HttpRequest.BodyPublishers.ofString(body));
```

#### DELETE with body
`java.net.http.HttpClient` supports DELETE with body natively:
```java
builder.method("DELETE", HttpRequest.BodyPublishers.ofString(body));
```

#### SSL Trust-All
```java
private static SSLContext createTrustAllSSLContext() {
    SSLContext ctx = SSLContext.getInstance("TLS");
    ctx.init(null, new TrustManager[]{
        new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) {}
            public void checkServerTrusted(X509Certificate[] chain, String authType) {}
            public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
        }
    }, new SecureRandom());
    return ctx;
}
```

#### Multipart upload
```java
String boundary = UUID.randomUUID().toString();
ByteArrayOutputStream out = new ByteArrayOutputStream();
// Text fields
for (Map.Entry<String, String> e : vals.entrySet()) {
    out.writeBytes("--" + boundary + "\r\n");
    out.writeBytes("Content-Disposition: form-data; name=\"" + e.getKey() + "\"\r\n\r\n");
    out.write(e.getValue().getBytes(charset));
    out.writeBytes("\r\n");
}
// File field
out.writeBytes("--" + boundary + "\r\n");
out.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + file.getName() + "\"\r\n");
out.writeBytes("Content-Type: application/octet-stream\r\n\r\n");
out.write(Files.readAllBytes(file.toPath()));
out.writeBytes("\r\n");
out.writeBytes("--" + boundary + "--\r\n");

// Set content type header
.header("Content-Type", "multipart/form-data; boundary=" + boundary)
```

#### Redirect handling
`java.net.http.HttpClient.Redirect.NEVER` disables automatic redirects. Handle manually by checking response status 301/302 and reading the "location" header.

#### SOCKS proxy — HttpClient doesn't support it, use HttpURLConnection fallback

`java.net.http.HttpClient`'s `ProxySelector` only supports HTTP proxies — SOCKS requests silently fail with "HTTP/1.1 header parser received no bytes". The fix is to detect SOCKS and fall back to `HttpURLConnection` with `java.net.Proxy(Proxy.Type.SOCKS, ...)`:

```java
private boolean isSocksProxy() {
    return "socks".equalsIgnoreCase(this._ProxyScheme)
            && this._ProxyHost != null && !this._ProxyHost.isEmpty();
}

// In doGet() / downloadData(), check SOCKS before using HttpClient:
if (isSocksProxy()) {
    return doGetViaSocks(url);  // HttpURLConnection fallback
}

private String doGetViaSocks(String url) {
    java.net.Proxy socksProxy = new java.net.Proxy(
        java.net.Proxy.Type.SOCKS,
        new InetSocketAddress(this._ProxyHost, this._ProxyPort));
    HttpURLConnection conn = (HttpURLConnection) new URI(url).toURL().openConnection(socksProxy);
    conn.setRequestMethod("GET");
    conn.setConnectTimeout(timeout > 0 ? timeout : 500000);
    conn.setReadTimeout(timeout > 0 ? timeout : 500000);
    conn.setInstanceFollowRedirects(false);
    conn.setRequestProperty("User-Agent", getUserAgent());
    // ... set headers, cookies, etc. ...
    int statusCode = conn.getResponseCode();
    InputStream in = statusCode >= 400 ? conn.getErrorStream() : conn.getInputStream();
    byte[] body = in != null ? in.readAllBytes() : new byte[0];
    in.close();
    conn.disconnect();
    return new String(body, charset);
}
```

**Known limitation**: Java's `HttpURLConnection` SOCKS5 implementation handles HTTP targets correctly but **HTTPS over SOCKS5 times out** ("Read timed out"). The SOCKS tunnel is established, but TLS handshake through the tunnel fails in JDK 17. For HTTPS targets through SOCKS, users should use an HTTP proxy instead. Verified test results:

| Proxy Type | HTTP Target | HTTPS Target |
|-----------|-------------|-------------|
| HTTP Proxy | ✅ | ✅ |
| SOCKS5 (HttpURLConnection fallback) | ✅ | ❌ Read timed out |

### javax.servlet → jakarta.servlet

Simple package rename: `javax.servlet.http.*` → `jakarta.servlet.http.*`. Update Maven dependency:
```xml
<!-- Old -->
<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <version>4.0.1</version>
</dependency>
<!-- New -->
<dependency>
    <groupId>jakarta.servlet</groupId>
    <artifactId>jakarta.servlet-api</artifactId>
    <version>6.1.0</version>
</dependency>
```

## BouncyCastle Gotchas

### Lazy Provider Registration
Instead of registering `BouncyCastleProvider` in a `static {}` block (which forces BC loading at class init), use lazy registration:

```java
private static void ensureBcProvider() {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
        Security.addProvider(new BouncyCastleProvider());
    }
}
```

**Critical: call `ensureBcProvider()` only inside BC-specific methods, NOT in constructors.** Calling it in constructors forces BC registration even when the user only uses JDK paths. Example:

```java
// WRONG: forces BC registration even for JDK-only users
public URsa() {
    ensureBcProvider();  // don't do this
}

// CORRECT: BC registered only when BC methods are actually called
public byte[] signBc(byte[] data) {
    ensureBcProvider();  // lazy, only when needed
    // ... BC code ...
}
```

### Deprecating BC-Specific Public Methods
When defaulting to JDK, mark all BC-specific public methods as `@Deprecated` with a pointer to the JDK equivalent:

```java
/**
 * @deprecated Use {@link #signJava(byte[])} instead
 */
@Deprecated
public byte[] signBc(byte[] data) { ... }
```

Methods to deprecate: `signBc`, `verifyBc`, `encryptBc`, `decryptBc`, `digestMessageBc`, `readPemKey` (uses BC PemReader), and any method returning BC types (e.g., `UDigest.getDigest()` returns `org.bouncycastle.crypto.Digest`).

Keep `setUsingBc()`/`isUsingBc()` **without** deprecation — they're still needed for CCM mode and other BC-only features.

### PKCS7Padding → PKCS5Padding
JDK's `Cipher` doesn't recognize `"PKCS7Padding"` — it uses `"PKCS5Padding"`. For AES (128-bit blocks), they are functionally identical. Auto-map in the JDK cipher path:

```java
String padding = this.getPaddingMethod();
if ("PKCS7Padding".equalsIgnoreCase(padding)) {
    padding = "PKCS5Padding";
}
String transformation = "AES/" + blockMode + "/" + padding;
```

### Hex encoding
Replace `org.bouncycastle.util.encoders.Hex` with JDK 17's `java.util.HexFormat`:
- `Hex.toHexString(b)` → `HexFormat.of().formatHex(b)`
- `Hex.toHexString(b).toUpperCase()` → `HexFormat.of().withUpperCase().formatHex(b)`
- `Hex.decode(hexs)` → `HexFormat.of().parseHex(hexs)`

## Deprecating Insecure Algorithms/Modes

When migrating to JDK 17, mark insecure or non-standard cipher modes as `@Deprecated` with `@Deprecated` annotation on the constant fields:

```java
// DES — 56-bit key, broken
@Deprecated
public class UDes implements IUSymmetricEncyrpt { ... }

// AES/ECB — no IV, deterministic encryption
@Deprecated
public final static String AES_128_ECB = "aes-128-ecb";

// AES/CCM — JDK doesn't support, only BC
@Deprecated
public final static String AES_128_CCM = "aes-128-ccm";

// RSA with MD5
@Deprecated
public static final String SIGNATURE_MD5withRSA = "MD5withRSA";
```

### What NOT to deprecate

- **MD5 / SHA-1**: While cryptographically weakened, they remain widely used for non-security purposes (checksums, file hashes, identifiers). Do NOT mark `md5()` / `sha1()` utility methods or `DIGEST_MD5` / `DIGEST_SHA1` constants as deprecated unless the user explicitly requests it.
- **SHA-1 RSA signatures**: Still used in legacy systems; deprecate only if the user asks.

## Common Pitfalls

| Pitfall | Symptom | Fix |
|---------|---------|-----|
| `AES/CBC/PKCS7Padding` with JDK Cipher | `NoSuchAlgorithmException` | Map PKCS7Padding → PKCS5Padding |
| `BooleanUtils.toBoolean` vs `Boolean.parseBoolean` | "yes", "on" return false | Add manual checks for "yes", "on", "1", "y", "t" |
| Test files still import removed deps | Test compilation failure | Grep test sources for removed package imports |
| `java.net.http` doesn't support SOCKS proxy natively | Proxy connections fail | Fall back to HTTP proxy or system properties |
| CCM mode requires BC provider | `NoSuchAlgorithmException` in JDK path | Route CCM through BC path explicitly |
| hsqldb `jdk8` classifier on JDK 17 | Wrong artifact resolved | Remove `<classifier>jdk8</classifier>` from pom.xml |
| Maven plugin hardcodes `target/` path | `copy-rename-maven-plugin` fails when `<directory>` is changed | Replace `target/` with `${project.build.directory}/` in all plugin configs |
| BC provider registered in constructor | BC loaded even for JDK-only users | Call `ensureBcProvider()` only in BC-specific methods, not constructors |

## Migration Checklist

1. Update `pom.xml`: Java version, artifactId, build directory
2. Fix Maven plugin paths: replace hardcoded `target/` with `${project.build.directory}/`
3. Remove dependencies one by one, replacing with JDK equivalents
4. Fix `javax.servlet` → `jakarta.servlet` (simple find-replace)
5. Update BouncyCastle: lazy registration (in BC methods only, not constructors), PKCS7→PKCS5 mapping
6. Deprecate BC-specific public methods with `@Deprecated` + `@link` to JDK equivalent
7. Replace Hex with JDK 17 HexFormat
8. **Grep test sources** for removed package imports — tests often use removed deps too
9. Run `mvn clean compile` to verify all source compiles
10. Run `mvn test` to verify all tests pass
11. Fix any test failures (often test files import removed deps)
