# UNet 网络请求使用指南

## 概述

`UNet` 是 emp-script-utils 提供的 HTTP/HTTPS 网络请求工具类（`com.gdxsoft.easyweb.utils.UNet`）。
基于 JDK 17 `java.net.http.HttpClient` 实现，无外部 HTTP 依赖，支持代理（HTTP/SOCKS5）、
SSE（Server-Sent Events）流式事件和异步调用。

**主要功能**:
- GET/POST/PUT/DELETE/PATCH 请求
- SSE 流式事件（含异步 + 自动重连）
- HTTP / SOCKS5 代理（含用户名密码认证）
- 文件上传（multipart）/ 下载
- Cookie 自动管理
- 请求/响应头自定义
- SSL 证书验证（可关闭）
- 重定向自动跟随
- 超时控制

---

## 快速开始

### GET 请求

```java
UNet net = new UNet();
String result = net.doGet("https://api.example.com/data");
int status = net.getLastStatusCode(); // 200
```

### POST JSON

```java
UNet net = new UNet();
net.addHeader("Content-Type", "application/json");
String result = net.doPost("https://api.example.com/login",
        "{\"username\":\"admin\",\"password\":\"secret\"}");
```

### POST 表单

```java
Map<String, String> params = new HashMap<>();
params.put("username", "admin");
params.put("password", "secret");
String result = net.doPost("https://api.example.com/login", params);
```

### 下载文件

```java
byte[] data = net.downloadData("https://example.com/file.pdf");
Files.write(Path.of("/tmp/file.pdf"), data);
```

---

## HTTP 方法

| 方法 | 说明 |
|------|------|
| `doGet(url)` | GET 请求，返回字符串 |
| `doPost(url, body)` | POST 字符串 body |
| `doPost(url, params)` | POST 表单参数 (Map) |
| `doPost(url, byte[])` | POST 二进制 body |
| `doPut(url, body)` | PUT 字符串 body |
| `doPut(url, params)` | PUT 表单参数 |
| `doDelete(url)` | DELETE 请求 |
| `doDelete(url, body)` | DELETE 带 body |
| `doDelete(url, params)` | DELETE 带表单参数 |
| `doPatch(url, body)` | PATCH 字符串 body |
| `doPatch(url, params)` | PATCH 表单参数 |
| `downloadData(url)` | GET 下载二进制 |
| `postMsgAndDownload(url, body)` | POST 后下载二进制 |
| `doUpload(url, fieldName, filePath)` | 上传单个文件 |
| `doUpload(url, fieldName, filePath, params)` | 上传文件 + 参数 |

### 完整示例

```java
// DELETE 带 body
net.doDelete("https://api.example.com/users/123", "{\"force\":true}");

// PATCH
net.doPatch("https://api.example.com/users/123", "{\"name\":\"new\"}");

// 上传
HashMap<String, String> params = new HashMap<>();
params.put("category", "docs");
net.doUpload("https://api.example.com/upload", "file", "/path/to/report.pdf", params);
```

---

## 代理配置

JDK 17 `HttpClient` 原生支持 HTTP 代理。SOCKS5 代理通过内置回退（HTTP→HttpURLConnection，HTTPS→HttpsOverSocks5）实现。

```java
UNet net = new UNet();

// HTTP 代理
net.setProxy("127.0.0.1", 1087);

// HTTP 代理 + 认证
net.setProxy("127.0.0.1", 1087, "http", "user", "pass");

// SOCKS5 代理
net.setProxy("127.0.0.1", 1086, "socks");

// SOCKS5 + 认证
net.setProxy("127.0.0.1", 1086, "socks", "user", "pass");

// 清除代理
net.clearProxy();
```

**SOCKS5 实现细节**:
- HTTP over SOCKS5 → JDK `HttpURLConnection` + `Proxy.Type.SOCKS`
- HTTPS over SOCKS5 → `HttpsOverSocks5` 类：手动 RFC 1928 握手 + RFC 1929 认证 + TLS 叠加
- 远程 DNS 解析（hostname 发往代理端解析，避免本地 DNS 泄漏）

---

## SSE（Server-Sent Events）流式事件

从服务端接收实时推送的事件流，常用于 AI API 的流式输出（OpenAI/Claude）、消息推送、日志流等场景。

### 核心概念

```
event: update          ← 事件类型（可选）
id: 123                ← 事件 ID（可选）
data: {"msg":"hello"}  ← 事件数据
                       ← 空行分隔事件
retry: 3000            ← 重连间隔 ms（可选）
```

### 同步阻塞调用

```java
UNet net = new UNet();
List<UNet.SseEvent> events = new ArrayList<>();

net.doSse("https://api.example.com/stream", event -> {
    System.out.println("事件: " + event.getEvent());
    System.out.println("数据: " + event.getData());
    System.out.println("ID:   " + event.getId());
    events.add(event);
});

// doSse 在流结束时返回，之后可以处理所有事件
System.out.println("共收到 " + events.size() + " 个事件");
```

### 异步非阻塞调用

```java
UNet net = new UNet();

CompletableFuture<Void> future = net.doSseAsync("https://api.example.com/stream", event -> {
    System.out.println("实时: " + event.getData());
});

// 主线程不阻塞，可继续做其他事
future.thenRun(() -> System.out.println("SSE 流结束"));
```

### POST 方式发起 SSE（OpenAI / Anthropic 等）

```java
UNet net = new UNet();
net.addHeader("Authorization", "Bearer sk-xxx");
net.addHeader("Content-Type", "application/json");

String body = """
    {
        "model": "gpt-4",
        "messages": [{"role": "user", "content": "你好"}],
        "stream": true
    }
    """;

net.doSse("https://api.openai.com/v1/chat/completions", body, event -> {
    System.out.print(event.getData()); // 流式输出
});

net.doSseAsync("https://api.openai.com/v1/chat/completions", body, event -> {
    // 异步处理每个 token
    appendToUI(event.getData());
});
```

### 自动重连

```java
UNet net = new UNet();

// 启用重连: 最多 5 次, 间隔 2 秒
net.setSseReconnect(true, 5, 2000);

net.doSse("https://api.example.com/stream", event -> {
    processEvent(event);
    // 连接断开时自动重连，onEvent 继续触发
});
```

- 服务端返回的 `retry:` 字段优先于配置的 `retryIntervalMs`
- HTTP 状态码 4xx/5xx 视为不可恢复，不触发重连
- IO 异常（连接断开）触发重连

### SseEvent 字段

| 方法 | 返回 | 说明 |
|------|------|------|
| `getEvent()` | String | 事件类型，可空 |
| `getData()` | String | 事件数据，多行 data 用 `\n` 合并 |
| `getId()` | String | 事件 ID，可空 |
| `getRetry()` | Long | 服务端重连间隔 ms，可空 |

### SSE + CompletableFuture 组合使用

```java
UNet net = new UNet();
List<String> results = Collections.synchronizedList(new ArrayList<>());

CompletableFuture<Void> f = net.doSseAsync(url, event -> {
    results.add(event.getData());
});

// 可组合多个 SSE 流
CompletableFuture<Void> f2 = net2.doSseAsync(url2, event -> { ... });

CompletableFuture.allOf(f, f2).join(); // 等待全部完成
```

---

## 请求配置

### Headers

```java
net.addHeader("Authorization", "Bearer token123");
net.addHeader("X-Api-Key", "abc-def");

// 批量添加（过滤 content-length/origin/host/connection）
Map<String, String> headers = new HashMap<>();
headers.put("Accept", "application/json");
headers.put("X-Trace-Id", "trace-001");
net.addHeaders(headers);

net.clearHeaders();  // 清除所有自定义 header
```

### Cookie

```java
// 字符串初始化
net.setCookie("sessionid=abc; lang=zh");

// Map 操作
Map<String, String> store = new HashMap<>();
store.put("token", "xxx");
net.setCookieStore(store);

String cookies = net.getCookies();  // "sessionid=abc; lang=zh"
Map<String, String> all = net.getCookieStore();

// 多实例保持独立会话
UNet authNet = new UNet();
authNet.doPost("https://api.example.com/login", loginParams);
// authNet 已自动保存登录 Cookie

UNet apiNet = authNet; // 共享同一会话
apiNet.doGet("https://api.example.com/me");
```

### 超时

```java
net.setTimeout(30_000); // 30 秒

// 默认值
UNet.C_TIME_OUT = 500_000; // 连接超时
UNet.R_TIME_OUT = 500_000; // 读取超时（SSE 长连接建议调大）
```

### 编码

```java
net.setEncode("UTF-8"); // 默认
net.setEncode("GBK");
```

### User-Agent

```java
net.setUserAgent(UNet.AGENT);        // Chrome 101
net.setUserAgent("MyBot/1.0");
```

### 日志

```java
net.setIsShowLog(true); // 输出请求/响应头、耗时
```

---

## 响应处理

```java
UNet net = new UNet();
net.doGet("https://api.example.com/data");

// 状态码
int code = net.getLastStatusCode();

// 响应头
Map<String, String> headers = net.getResponseHeaders();
String ct = headers.get("content-type");

// Cookie（自动从 Set-Cookie 解析累积）
String cookies = net.getCookies();

// 文本内容
String text = net.getLastResult();

// 二进制内容
byte[] buf = net.downloadData("https://example.com/file.pdf");
byte[] lastBuf = net.getLastBuf();

// 错误信息
if (text == null) {
    System.err.println("请求失败: " + net.getLastErr());
}

// 最终 URL（重定向后）
String finalUrl = net.getLastUrl();
```

---

## 完整示例

### AI API 流式调用（模拟 ChatGPT 流式输出）

```java
UNet net = new UNet();
net.addHeader("Authorization", "Bearer sk-xxxx");
net.addHeader("Content-Type", "application/json");

String body = """
    {"model":"gpt-4","messages":[{"role":"user","content":"写一首诗"}],"stream":true}
    """;

net.doSse("https://api.openai.com/v1/chat/completions", body, event -> {
    String data = event.getData();
    if (!"[DONE]".equals(data)) {
        // 解析 JSON 获取 token 文本并实时打印
        System.out.print(new JSONObject(data)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("delta")
                .optString("content", ""));
    }
});
System.out.println(); // 流结束
```

### 多 SSE 流并发消费

```java
UNet net1 = new UNet();
UNet net2 = new UNet();

CompletableFuture<Void> f1 = net1.doSseAsync("https://stream1.example.com/events",
        e -> processStream1(e.getData()));

CompletableFuture<Void> f2 = net2.doSseAsync("https://stream2.example.com/events",
        e -> processStream2(e.getData()));

CompletableFuture.allOf(f1, f2).join();
```

### 通过代理调用 API

```java
UNet net = new UNet();
net.setProxy("127.0.0.1", 1087, "http");     // Clash HTTP 代理
// net.setProxy("127.0.0.1", 1086, "socks"); // ss-local SOCKS5

net.addHeader("Authorization", "Bearer token");
String result = net.doGet("https://api.openai.com/v1/models");
```

### RESTful API 客户端

```java
public class ApiClient {
    private final UNet net;
    private final String base;

    public ApiClient(String baseUrl) {
        this.base = baseUrl;
        this.net = new UNet();
        net.addHeader("Accept", "application/json");
        net.addHeader("Content-Type", "application/json");
    }

    public JSONObject get(String path) {
        return new JSONObject(net.doGet(base + path));
    }

    public JSONObject post(String path, JSONObject data) {
        return new JSONObject(net.doPost(base + path, data.toString()));
    }

    public JSONObject put(String path, JSONObject data) {
        return new JSONObject(net.doPut(base + path, data.toString()));
    }

    public JSONObject delete(String path) {
        return new JSONObject(net.doDelete(base + path));
    }
}
```

---

## 注意事项

1. **SSE 长连接**: SSE 流可能持续数分钟甚至数小时，超时时间设得太短会导致中途断开。
   建议对 SSE 请求调大 `R_TIME_OUT` 或使用 `setTimeout()`。
2. **SOCKS5 + HTTPS**: `java.net.http.HttpClient` 不支持 SOCKS 代理。本库通过 `HttpsOverSocks5` 手动实现，
   功能完整但性能略低于原生连接（多一层 TLS 握手）。
3. **SSL 证书**: 默认信任所有证书（`trustAll`），生产环境建议自定义 SSLContext。
4. **重定向**: 自动跟随 301/302，默认最多 7 次。
5. **线程安全**: `UNet` 实例非线程安全（内部状态如 Cookie、Headers），多线程场景各线程需独立实例。
6. **Cookie 自动管理**: 响应中的 `Set-Cookie` 自动解析并累积，后续请求自动携带。

---

## 相关类

| 类名 | 说明 |
|------|------|
| `UNet` | 网络请求工具 |
| `UNet.SseEvent` | SSE 事件数据类 |
| `UNet.SseListener` | SSE 回调接口 |
| `HttpsOverSocks5` | HTTPS through SOCKS5 代理实现 |
| `UUrl` | URL 处理工具 |
| `Utils` | 通用工具类 |
