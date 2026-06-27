---
name: jdk8-to-jdk17-migration
description: Comprehensive JDK 8 to 17 migration with dependency reduction — replacing commons-io, commons-lang3, commons-exec, Apache HttpClient with JDK 17 built-in APIs.
source: auto-skill
extracted_at: '2026-06-27T05:40:00.000Z'
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

**Known limitation (FIXED)**: Java's `HttpURLConnection` SOCKS5 implementation handles HTTP targets correctly but **HTTPS over SOCKS5 times out** ("Read timed out"). For HTTPS over SOCKS5, a custom `HttpsOverSocks5` implementation is needed (see below).

| SOCKS5 (HttpURLConnection fallback) | ✅ | ❌ Read timed out (needs HttpsOverSocks5) |
| SOCKS5 (HttpsOverSocks5) | ✅ | ✅ |

### HTTPS over SOCKS5 — HttpsOverSocks5 implementation

`java.net.http.HttpClient` doesn't support SOCKS proxies at all, and `HttpURLConnection` with SOCKS proxy fails for HTTPS targets. The fix is a custom `HttpsOverSocks5` class that:

1. Opens a raw TCP socket to the SOCKS5 proxy
2. Performs SOCKS5 handshake with hostname-based connection (ATYP=0x03, remote DNS)
3. Wraps the connected socket with `SSLSocket` for TLS
4. Sends the HTTP request manually over the SSL socket
5. Parses the HTTP response (status line, headers, chunked transfer encoding)

Key implementation details:
```java
// SOCKS5 handshake with remote DNS (hostname, not IP)
private void socks5Handshake(Socket socket, String targetHost, int targetPort) {
    // Step 1: Client greeting
    out.write(new byte[] { 0x05, 0x01, 0x00 }); // SOCKS5, 1 method, NO AUTH
    // Step 2: Server selects method
    int ver = in.read(); int method = in.read();
    // Step 3: Connect request with DOMAINNAME (ATYP=0x03)
    ByteArrayOutputStream reqBuf = new ByteArrayOutputStream();
    reqBuf.write(0x05); // VER
    reqBuf.write(0x01); // CMD=CONNECT
    reqBuf.write(0x00); // RSV
    reqBuf.write(0x03); // ATYP=DOMAINNAME
    reqBuf.write(hostBytes.length);
    reqBuf.write(hostBytes);
    reqBuf.write((targetPort >> 8) & 0xFF);
    reqBuf.write(targetPort & 0xFF);
    // Step 4: Read response, skip bound address
}

// SSL over SOCKS tunnel
SSLSocketFactory sslFactory = createTrustAllSSLSocketFactory();
SSLSocket sslSocket = (SSLSocket) sslFactory.createSocket(
    proxySocket, host, port, true);
sslSocket.startHandshake();

// Send HTTP request manually
String req = "GET " + path + " HTTP/1.1\r\n" +
    "Host: " + host + "\r\n" +
    "Connection: close\r\n\r\n";
out.write(req.getBytes(StandardCharsets.UTF_8));

// Parse response (status line, headers, chunked body)
return parseHttpResponse(sslSocket.getInputStream());
```

#### HTTP response parser requirements
When sending raw HTTP over the socket, you need to parse:
- Status line: `HTTP/1.1 200 OK` → extract status code
- Headers: `Name: value\r\n` until blank line
- Body: Content-Length for fixed size, Transfer-Encoding: chunked for chunked, or read-all for connection-close
- Chunked encoding: read hex size line, then chunk data, then CRLF

### Proxy Authentication

#### HTTP Proxy Auth (UNet)
Use `java.net.Authenticator` with `HttpClient.Builder`:
```java
if (this._ProxyUser != null && !this._ProxyUser.isEmpty()) {
    builder.authenticator(new java.net.Authenticator() {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(_ProxyUser,
                    (_ProxyPassword != null ? _ProxyPassword : "").toCharArray());
        }
    });
}
```

#### SOCKS5 Proxy Auth (HttpsOverSocks5)
Implement RFC 1929 username/password authentication in the SOCKS5 handshake:
```java
// Step 1: Advertise both NO AUTH and USER/PASS methods
if (hasAuth) {
    out.write(new byte[] { 0x05, 0x02, 0x00, 0x02 }); // 2 methods
} else {
    out.write(new byte[] { 0x05, 0x01, 0x00 }); // 1 method
}

// Step 2b: If server selected USER/PASS (0x02), do auth sub-negotiation
if (method == 0x02) {
    ByteArrayOutputStream authBuf = new ByteArrayOutputStream();
    authBuf.write(0x01); // VER: sub-negotiation version
    authBuf.write(userBytes.length);
    authBuf.write(userBytes);
    authBuf.write(passBytes.length);
    authBuf.write(passBytes);
    out.write(authBuf.toByteArray());
    out.flush();
    int authVer = in.read(); int authStatus = in.read();
    if (authVer != 0x01 || authStatus != 0x00) {
        throw new IOException("SOCKS5 authentication failed");
    }
}
```

### javax.mail → jakarta.mail Migration

When upgrading from `com.sun.mail/jakarta.mail 1.x` to `org.eclipse.angus/jakarta.mail 2.x`:

#### pom.xml changes
```xml
<!-- Old -->
<dependency>
    <groupId>com.sun.mail</groupId>
    <artifactId>jakarta.mail</artifactId>
    <version>1.6.8</version>
</dependency>
<!-- New -->
<dependency>
    <groupId>org.eclipse.angus</groupId>
    <artifactId>jakarta.mail</artifactId>
    <version>2.0.3</version>
</dependency>
<!-- Required by Jakarta Mail 2.x -->
<dependency>
    <groupId>jakarta.activation</groupId>
    <artifactId>jakarta.activation-api</artifactId>
    <version>2.1.3</version>
</dependency>
```

#### Bulk import replace
Replace `javax.mail.*` → `jakarta.mail.*` and `javax.activation.*` → `jakarta.activation.*` in BOTH main and test sources:
```bash
find src/main/java -name '*.java' -exec sed -i '' \
    's/import javax\.mail\./import jakarta.mail./g; 
     s/import javax\.activation\./import jakarta.activation./g' {} +

# Don't forget test sources!
find src/test/java -name '*.java' -exec sed -i '' \
    's/import javax\.mail\./import jakarta.mail./g;
     s/import javax\.activation\./import jakarta.activation./g' {} +
```

#### com.sun.mail internal classes — no longer exist
| Old (com.sun.mail 1.x) | New (jakarta.mail 2.x) |
|------------------------|------------------------|
| `com.sun.mail.smtp.SMTPMessage` | `jakarta.mail.internet.MimeMessage` (SMTPMessage extends MimeMessage) |
| `com.sun.mail.util.CRLFOutputStream` | Self-implement: `body.replace("\r\n", "\n").replace("\n", "\r\n")` |
| `com.sun.mail.util.QPEncoderStream` | Self-implement QP encoder (`String.format("=%02X", b & 0xFF)`) |
| `com.sun.mail.util.LineOutputStream` | Self-implement: `os.write((line + "\r\n").getBytes(US_ASCII))` |

#### DKIMMessage rewrite
Change `extends SMTPMessage` → `extends MimeMessage`. Constructor signatures are identical (they inherit from MimeMessage). `writeTo(OutputStream, String[])` exists on both. `setAllow8bitMIME()` can be a no-op.

#### DKIMSigner fixes
Replace `CRLFOutputStream` with inline CRLF normalization:
```java
String bodyWithCrlf = body.replace("\r\n", "\n").replace("\n", "\r\n");
baos.write(bodyWithCrlf.getBytes());
```

Replace `QPEncoderStream` with a simple QP encoder:
```java
private static String QuotedPrintable(String s) {
    StringBuilder sb = new StringBuilder();
    for (byte b : s.getBytes()) {
        if ((b >= 33 && b <= 60) || (b >= 62 && b <= 126) || b == 9 || b == 32) {
            sb.append((char) b);
        } else {
            sb.append(String.format("=%02X", b & 0xFF));
        }
    }
    return sb.toString();
}
```

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

### Integer Reference Comparison Bug in Caches
When using `ConcurrentHashMap<String, Integer>` for file state tracking, comparing `Integer` objects with `==` fails for values outside `[-128, 127]`:
```java
// WRONG: == compares references, not values; hashCode() returns full int range
Integer prevCode = map.get(key);
if (prevCode != null && prevCode == statusCode) { ... }

// CORRECT: use intValue() or equals()
if (prevCode != null && prevCode.intValue() == statusCode) { ... }
```
Java caches Integer objects only in `[-128, 127]`. `hashCode()` can return any `int`, so `Integer ==` fails silently for most values.

### Unnecessary ReentrantLock Over ConcurrentHashMap
`ConcurrentHashMap.put()` and `ConcurrentHashMap.remove()` are already atomic since Java 8. Wrapping them with `ReentrantLock` serializes otherwise-concurrent writes:
```java
// BEFORE: unnecessary lock
MAP.put(key, val);  // already atomic

// AFTER: lock-free, better concurrent throughput
MAP.put(key, val);
```
`removeIf()` on `ConcurrentHashMap.entrySet()` is also atomic — no external lock needed.

### Single-Syscall File Attribute Reading
`File.exists()` + `File.lastModified()` + `File.length()` = 3 `stat()` syscalls. Use `Files.readAttributes()` for 1 syscall:
```java
// BEFORE: 3 stat() calls
File f = new File(path);
boolean exists = f.exists();          // stat #1
long modified = f.lastModified();      // stat #2
long size = f.length();                // stat #3

// AFTER: 1 stat() call, resolves symlinks
BasicFileAttributes attr = Files.readAttributes(Path.of(path), BasicFileAttributes.class);
String key = Path.of(path).toRealPath() + "|" + attr.lastModifiedTime().toMillis() + "|" + attr.size();
```
`toRealPath()` also resolves symbolic links, preventing duplicate tracking of the same file.

### hashCode-Based Key Collisions in Static Caches

When using `ConcurrentHashMap<Integer, ...>` as a static cache with `someString.hashCode()` as key, two problems arise:

1. **Hash collisions**: Different strings can produce the same `hashCode()`, causing data loss
2. **Inconsistent keys**: If some code paths use `path.hashCode()` and others use `(path|modified|size).hashCode()`, they never match

**Fix**: Use `String` as the key type directly instead of `Integer`:

```java
// BEFORE: collision-prone
Map<Integer, Long> cache = new ConcurrentHashMap<>();
int key = file.getAbsolutePath().hashCode();
cache.put(key, timestamp);

// AFTER: collision-safe + merge 2 maps into 1
private static class FileState { long time; int code; }
Map<String, FileState> cache = new ConcurrentHashMap<>();
String key = file.getAbsolutePath();
cache.put(key, new FileState(time, code));
```

When making this change, add `@Deprecated` overloads for backward compatibility that wrap old `Integer` parameters to `String.valueOf()`. Also add periodic eviction to prevent unbounded memory growth:

```java
private static void evictStaleIfNeeded() {
    if (cache.size() < 1000) return;
    long cutoff = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1);
    cache.entrySet().removeIf(e -> e.getValue().time < cutoff);
}
```

### General Pitfalls

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
| `setExpandEntityReferences(false)` alone for XXE | Incomplete XXE protection | See XML XXE Protection section below |
| XML `createXmlValue` `&` escaped after `\r`/`\n` | `&#xD;` → `&amp;#xD;` double-escape | `&` must be escaped FIRST, before any other entity references |
| `DocumentBuilderFactory.newInstance()` called per parse | ~1-2ms overhead per call (SPI classpath scan) | Cache in static final field |

## XML XXE (XML External Entity) Protection

### Incomplete approach (vulnerable)

Setting only `setExpandEntityReferences(false)` is insufficient — the parser still resolves external entities (fetches URLs, reads local files), it just doesn't inline the content:

```java
// INSECURE — still resolves external entities!
DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
factory.setExpandEntityReferences(false);
```

### Complete protection

Must add these features to fully disable XXE vectors. **Important tradeoff**: `disallow-doctype-decl` is the strongest XXE prevention but can break legitimate XML parsing in some JDK parsers. Prefer the combination of `external-general-entities` + `external-parameter-entities` + `secure-processing` for broader compatibility:

```java
private static final DocumentBuilderFactory DOC_FACTORY;
static {
    DOC_FACTORY = DocumentBuilderFactory.newInstance();
    DOC_FACTORY.setExpandEntityReferences(false);
    try { DOC_FACTORY.setFeature(
        "http://xml.org/sax/features/external-general-entities", false);
    } catch (ParserConfigurationException ignored) {}
    try { DOC_FACTORY.setFeature(
        "http://xml.org/sax/features/external-parameter-entities", false);
    } catch (ParserConfigurationException ignored) {}
    try { DOC_FACTORY.setFeature(
        "http://javax.xml.XMLConstants/feature/secure-processing", true);
    } catch (ParserConfigurationException ignored) {}
    DOC_FACTORY.setXIncludeAware(false);

    // OPTIONAL: strongest XXE block — but may break valid XML docs
    try { DOC_FACTORY.setFeature(
        "http://apache.org/xml/features/disallow-doctype-decl", true);
    } catch (ParserConfigurationException ignored) {}
}
```

| Feature | Purpose | Compatibility |
|---------|---------|---------------|
| `external-general-entities=false` | Prevents resolution of external general entities | ✅ Safe |
| `external-parameter-entities=false` | Prevents resolution of external parameter entities | ✅ Safe |
| `secure-processing=true` | Limits entity expansion (Billion Laughs protection), limits maxOccurs | ✅ Safe |
| `setXIncludeAware(false)` | Disables XInclude (entity injection vector) | ✅ Safe |
| `disallow-doctype-decl=true` | Blocks DOCTYPE entirely — **use with caution** | ⚠️ May break parsers that rely on DOCTYPE for XML declaration, DTDs for file loading. Test thoroughly. |

**Why `try/catch`**: Some XML parsers (Xerces, etc.) may not support all features. The `try/catch` ensures the code works across parser implementations while enabling all available protections. The cumulative effect is defense-in-depth.

**DocumentBuilder is NOT thread-safe** — create a new `DocumentBuilder` per parse from the shared factory:
```java
DocumentBuilder builder = DOC_FACTORY.newDocumentBuilder();
Document doc = builder.parse(inputSource);
```

## XML Character Escaping Order Bug

When building a `createXmlValue()` / XML escaping method, `&` MUST be escaped **first**, before any other characters. Otherwise the `&` in already-escaping sequences (like `&#xD;`) gets double-escaped:

```java
// ⚠️ BUG: \r → &#xD; introduces &, then & → &amp; corrupts it
// Input: "a\r\nb"
s1 = s1.replace("\r", "&#xD;");   // introduces &
s1 = s1.replace("\n", "&#xA;");   // introduces &
s1 = s1.replace("&", "&amp;");    // &#xD; → &amp;#xD;  ← CORRUPTED!

// ✅ CORRECT: & must come first
s1 = s1.replace("&", "&amp;");    // Step 1: escape & first
s1 = s1.replace("<", "&lt;");
s1 = s1.replace(">", "&gt;");
s1 = s1.replace("\"", "&quot;");
s1 = s1.replace("'", "&apos;");   // apostrophe for attribute values
s1 = s1.replace("\r", "&#xD;");   // Step 2: now safe to use &
s1 = s1.replace("\n", "&#xA;");
```

**Also standard-compliant**: XML requires numeric character references to end with `;` — `&#xD;` not `&#xD`.

## SSLContext Factory with Trust-All Toggle

A shared factory method that returns either a trust-all or system-default `SSLContext`:

```java
public static SSLContext createSSLContext(boolean isSelfSign) {
    if (!isSelfSign) {
        // System default — validates against JVM trust store
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, null, null); // null TrustManager = system default
        return ctx;
    }
    // Trust-all — for dev/internal certs
    SSLContext ctx = SSLContext.getInstance("TLS");
    ctx.init(null, new TrustManager[] {
        new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] c, String a) {}
            public void checkServerTrusted(X509Certificate[] c, String a) {}
            public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
        }
    }, new SecureRandom());
    return ctx;
}
```

**Usage**: This single method serves both `java.net.http.HttpClient` (needs `SSLContext`) and `HttpsOverSocks5` (needs `SSLSocketFactory` via `ctx.getSocketFactory()`), eliminating duplicate trust-all SSL implementations.

## Caching XML Factories

`DocumentBuilderFactory.newInstance()` and `TransformerFactory.newInstance()` use SPI (Service Provider Interface) classpath scanning to locate implementations. Creating them every call adds ~1-2ms overhead per XML operation. Cache in static final fields:

```java
private static final DocumentBuilderFactory DOC_FACTORY;
private static final TransformerFactory TRANS_FACTORY;

static {
    DOC_FACTORY = DocumentBuilderFactory.newInstance();
    // ... security configuration ...
    TRANS_FACTORY = TransformerFactory.newInstance();
}

// Usage: factory is cached, builder/transformer created per operation
DocumentBuilder builder = DOC_FACTORY.newDocumentBuilder();  // per parse
Transformer transformer = TRANS_FACTORY.newTransformer();     // per transform
```

**Important**: `DocumentBuilder` and `Transformer` are NOT thread-safe and must be created per operation. The `Factory` objects are thread-safe and can be shared.

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
