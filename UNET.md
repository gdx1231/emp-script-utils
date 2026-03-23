# UNet 网络请求使用指南

## 概述

`UNet` 是 emp-script-utils 提供的 HTTP/HTTPS 网络请求工具类，位于 `com.gdxsoft.easyweb.utils` 包中。基于 Apache HttpClient 实现，提供简洁的 API 进行网络请求。

**主要功能**:
- GET/POST/PUT/DELETE/PATCH 请求
- 文件上传/下载
- Cookie 管理
- 自定义 Header
- SSL/HTTPS 支持（忽略证书验证）
- 重定向处理
- 超时控制
- 请求日志

---

## 快速开始

### 基本 GET 请求

```java
import com.gdxsoft.easyweb.utils.UNet;

UNet net = new UNet();
String result = net.doGet("https://api.example.com/data");
System.out.println(result);
```

### 基本 POST 请求

```java
import com.gdxsoft.easyweb.utils.UNet;
import java.util.HashMap;

UNet net = new UNet();
HashMap<String, String> params = new HashMap<>();
params.put("username", "admin");
params.put("password", "123456");

String result = net.doPost("https://api.example.com/login", params);
System.out.println(result);
```

### 下载文件

```java
import com.gdxsoft.easyweb.utils.UNet;

UNet net = new UNet();
byte[] fileData = net.downloadData("https://example.com/file.pdf");

// 保存到文件
java.io.File file = new java.io.File("/path/to/file.pdf");
org.apache.commons.io.FileUtils.writeByteArrayToFile(file, fileData);
```

---

## HTTP 方法

### GET 请求

```java
import com.gdxsoft.easyweb.utils.UNet;

UNet net = new UNet();

// 简单 GET
String result = net.doGet("https://api.example.com/users");

// 带参数 GET（手动拼接 URL）
String result2 = net.doGet("https://api.example.com/users?id=123&name=test");

// 获取状态码
int statusCode = net.getLastStatusCode();
System.out.println("状态码：" + statusCode);

// 获取返回内容
String response = net.getLastResult();
System.out.println("返回：" + response);
```

### POST 请求

#### 表单提交

```java
import com.gdxsoft.easyweb.utils.UNet;
import java.util.HashMap;

UNet net = new UNet();
HashMap<String, String> params = new HashMap<>();
params.put("username", "admin");
params.put("password", "secret");
params.put("remember", "true");

String result = net.doPost("https://api.example.com/login", params);
```

#### JSON Body 提交

```java
import com.gdxsoft.easyweb.utils.UNet;
import org.json.JSONObject;

UNet net = new UNet();
JSONObject json = new JSONObject();
json.put("username", "admin");
json.put("password", "secret");

// 添加 JSON Header
net.addHeader("Content-Type", "application/json");

String result = net.doPost("https://api.example.com/login", json.toString());
```

#### 二进制数据提交

```java
import com.gdxsoft.easyweb.utils.UNet;

UNet net = new UNet();
byte[] data = "Binary content".getBytes("UTF-8");

String result = net.doPost("https://api.example.com/upload", data);
```

### PUT 请求

```java
import com.gdxsoft.easyweb.utils.UNet;
import java.util.HashMap;

UNet net = new UNet();

// 字符串 Body
String result = net.doPut("https://api.example.com/user/123", "{\"name\":\"new name\"}");

// 表单参数
HashMap<String, String> params = new HashMap<>();
params.put("name", "new name");
params.put("email", "new@example.com");
String result2 = net.doPut("https://api.example.com/user/123", params);
```

### DELETE 请求

```java
import com.gdxsoft.easyweb.utils.UNet;

UNet net = new UNet();

// 简单 DELETE
String result = net.doDelete("https://api.example.com/user/123");

// 带 Body 的 DELETE
String result2 = net.doDelete("https://api.example.com/user/123", "{\"reason\":\"deleted\"}");

// 带参数的 DELETE
HashMap<String, String> params = new HashMap<>();
params.put("force", "true");
String result3 = net.doDelete("https://api.example.com/user/123", params);
```

### PATCH 请求

```java
import com.gdxsoft.easyweb.utils.UNet;

UNet net = new UNet();

// 字符串 Body
String result = net.doPatch("https://api.example.com/user/123", "{\"name\":\"updated\"}");

// 表单参数
HashMap<String, String> params = new HashMap<>();
params.put("name", "updated");
String result2 = net.doPatch("https://api.example.com/user/123", params);
```

---

## 文件操作

### 上传文件

```java
import com.gdxsoft.easyweb.utils.UNet;
import java.util.HashMap;

UNet net = new UNet();

// 简单文件上传
String result = net.doUpload(
    "https://api.example.com/upload",  // URL
    "file",                             // 字段名
    "/path/to/file.pdf"                 // 文件路径
);

// 带参数的文件上传
HashMap<String, String> params = new HashMap<>();
params.put("description", "重要文档");
params.put("category", "documents");

String result2 = net.doUpload(
    "https://api.example.com/upload",
    "file",
    "/path/to/file.pdf",
    params
);
```

### 下载文件

```java
import com.gdxsoft.easyweb.utils.UNet;
import org.apache.commons.io.FileUtils;
import java.io.File;

UNet net = new UNet();

// 下载二进制数据
byte[] data = net.downloadData("https://example.com/image.jpg");

// 保存到文件
File file = new File("/path/to/save/image.jpg");
FileUtils.writeByteArrayToFile(file, data);

// 获取下载的文件大小
int size = net.getLastBuf().length;
System.out.println("文件大小：" + size + " 字节");
```

---

## 请求配置

### 自定义 User-Agent

```java
import com.gdxsoft.easyweb.utils.UNet;

UNet net = new UNet();

// 使用预设的 User-Agent
net.setUserAgent(UNet.AGENT);  // Chrome 浏览器
// 或
net.setUserAgent(UNet.AGENT_4);  // IE8

// 自定义 User-Agent
net.setUserAgent("MyApp/1.0 (compatible; CustomBot)");
```

### 自定义 Headers

```java
import com.gdxsoft.easyweb.utils.UNet;
import java.util.HashMap;

UNet net = new UNet();

// 添加单个 Header
net.addHeader("Authorization", "Bearer token123");
net.addHeader("X-Custom-Header", "custom-value");

// 批量添加 Headers
HashMap<String, String> headers = new HashMap<>();
headers.put("Authorization", "Bearer token123");
headers.put("Content-Type", "application/json");
headers.put("Accept", "application/json");
net.addHeaders(headers);

// 清除所有 Headers
net.clearHeaders();
```

### Cookie 管理

```java
import com.gdxsoft.easyweb.utils.UNet;

UNet net = new UNet();

// 从字符串设置 Cookie
net.setCookie("sessionid=abc123; username=admin");

// 获取 Cookie 字符串
String cookies = net.getCookies();
System.out.println("Cookies: " + cookies);

// 获取 CookieStore 对象
org.apache.http.impl.client.BasicCookieStore cookieStore = net.getCookieStore();

// 设置 CookieStore
net.setCookieStore(cookieStore);

// Cookie 列表（JSON 格式）
org.json.JSONArray cookieList = net.listCookieStoreCookes();
System.out.println("Cookie 列表：" + cookieList.toString());
```

### 超时设置

```java
import com.gdxsoft.easyweb.utils.UNet;

UNet net = new UNet();

// 设置超时时间（毫秒）
net.setTimeout(30000);  // 30 秒

// 或使用静态变量（不推荐）
UNet.C_TIME_OUT = 50000;  // 连接超时
UNet.R_TIME_OUT = 50000;  // 读取超时
```

### 字符编码

```java
import com.gdxsoft.easyweb.utils.UNet;

UNet net = new UNet();

// 设置编码
net.setEncode("UTF-8");  // 默认
net.setEncode("GBK");    // 中文编码
net.setEncode("ISO-8859-1");  // 西文编码

// 获取当前编码
String encode = net.getEncode();
```

### 启用日志

```java
import com.gdxsoft.easyweb.utils.UNet;

UNet net = new UNet();

// 启用请求日志
net.setIsShowLog(true);

// 禁用请求日志
net.setIsShowLog(false);
```

---

## 响应处理

### 获取状态码

```java
import com.gdxsoft.easyweb.utils.UNet;

UNet net = new UNet();
net.doGet("https://api.example.com/data");

int statusCode = net.getLastStatusCode();
System.out.println("HTTP 状态码：" + statusCode);

// 常见状态码判断
if (statusCode == 200) {
    System.out.println("请求成功");
} else if (statusCode == 404) {
    System.out.println("资源未找到");
} else if (statusCode == 500) {
    System.out.println("服务器错误");
}
```

### 获取响应头

```java
import com.gdxsoft.easyweb.utils.UNet;
import java.util.Map;

UNet net = new UNet();
net.doGet("https://api.example.com/data");

// 获取所有响应头
Map<String, String> headers = net.getResponseHeaders();
for (String key : headers.keySet()) {
    System.out.println(key + ": " + headers.get(key));
}

// 获取特定响应头
String contentType = headers.get("Content-Type");
String contentLength = headers.get("Content-Length");
```

### 获取响应内容

```java
import com.gdxsoft.easyweb.utils.UNet;

UNet net = new UNet();

// 文本响应
String textResult = net.doGet("https://api.example.com/data");
String lastResult = net.getLastResult();

// 二进制响应
byte[] binaryResult = net.downloadData("https://example.com/file.pdf");
byte[] lastBuf = net.getLastBuf();
```

### 错误处理

```java
import com.gdxsoft.easyweb.utils.UNet;

UNet net = new UNet();
String result = net.doGet("https://invalid-url.example.com");

if (result == null) {
    String lastErr = net.getLastErr();
    System.out.println("请求失败：" + lastErr);
}

// 检查状态码
int statusCode = net.getLastStatusCode();
if (statusCode >= 400) {
    System.out.println("HTTP 错误：" + statusCode);
}
```

---

## 高级功能

### 重定向处理

```java
import com.gdxsoft.easyweb.utils.UNet;

UNet net = new UNet();

// 自动处理重定向（最多 7 次）
String result = net.doGet("https://short.url/abc123");

// 获取重定向后的 URL
String lastUrl = net.getLastUrl();
System.out.println("最终 URL: " + lastUrl);

// 设置最大重定向次数
net.setLimitRedirectInc(10);
```

### HTTPS/SSL 支持

```java
import com.gdxsoft.easyweb.utils.UNet;

UNet net = new UNet();

// 自动处理 HTTPS（忽略证书验证）
String result = net.doGet("https://self-signed.example.com");

// 注意：UNet 默认信任所有 SSL 证书
// 生产环境建议配置正确的证书验证
```

### 保持会话（Session）

```java
import com.gdxsoft.easyweb.utils.UNet;

UNet net = new UNet();

// 第一次请求（登录）
HashMap<String, String> loginParams = new HashMap<>();
loginParams.put("username", "admin");
loginParams.put("password", "secret");
net.doPost("https://example.com/login", loginParams);

// 后续请求会自动携带 Cookie
String profile = net.doGet("https://example.com/profile");
String settings = net.doGet("https://example.com/settings");
```

### 断点续传（Range 请求）

```java
import com.gdxsoft.easyweb.utils.UNet;

UNet net = new UNet();

// 设置 Range 头
net.addHeader("Range", "bytes=0-1023");  // 下载前 1KB

byte[] data = net.downloadData("https://example.com/largefile.zip");

// 继续下载
net.addHeader("Range", "bytes=1024-2047");  // 下载第二个 1KB
byte[] data2 = net.downloadData("https://example.com/largefile.zip");
```

### 并发请求（多线程）

```java
import com.gdxsoft.easyweb.utils.UNet;
import java.util.concurrent.*;

public class ConcurrentDownloader {
    
    public static void downloadFiles(String[] urls) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(urls.length);
        
        for (String url : urls) {
            executor.submit(() -> {
                UNet net = new UNet();
                byte[] data = net.downloadData(url);
                System.out.println("下载完成：" + url + ", 大小：" + data.length);
                latch.countDown();
            });
        }
        
        latch.await();
        executor.shutdown();
    }
    
    public static void main(String[] args) throws Exception {
        String[] urls = {
            "https://example.com/file1.jpg",
            "https://example.com/file2.jpg",
            "https://example.com/file3.jpg"
        };
        downloadFiles(urls);
    }
}
```

---

## 完整使用示例

### 示例 1: RESTful API 客户端

```java
import com.gdxsoft.easyweb.utils.UNet;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.HashMap;

public class ApiClient {
    
    private UNet net;
    private String baseUrl;
    
    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.net = new UNet();
        this.net.setEncode("UTF-8");
        this.net.addHeader("Accept", "application/json");
        this.net.addHeader("Content-Type", "application/json");
    }
    
    // 设置认证 Token
    public void setToken(String token) {
        this.net.addHeader("Authorization", "Bearer " + token);
    }
    
    // GET 请求
    public JSONObject get(String endpoint) throws Exception {
        String result = net.doGet(baseUrl + endpoint);
        if (result == null) {
            throw new Exception("请求失败：" + net.getLastErr());
        }
        return new JSONObject(result);
    }
    
    // POST 请求
    public JSONObject post(String endpoint, JSONObject data) throws Exception {
        String result = net.doPost(baseUrl + endpoint, data.toString());
        if (result == null) {
            throw new Exception("请求失败：" + net.getLastErr());
        }
        return new JSONObject(result);
    }
    
    // PUT 请求
    public JSONObject put(String endpoint, JSONObject data) throws Exception {
        String result = net.doPut(baseUrl + endpoint, data.toString());
        if (result == null) {
            throw new Exception("请求失败：" + net.getLastErr());
        }
        return new JSONObject(result);
    }
    
    // DELETE 请求
    public JSONObject delete(String endpoint) throws Exception {
        String result = net.doDelete(baseUrl + endpoint);
        if (result == null) {
            throw new Exception("请求失败：" + net.getLastErr());
        }
        return new JSONObject(result);
    }
    
    public static void main(String[] args) throws Exception {
        ApiClient client = new ApiClient("https://api.example.com");
        
        // 登录获取 Token
        JSONObject loginData = new JSONObject();
        loginData.put("username", "admin");
        loginData.put("password", "secret");
        
        JSONObject loginResult = client.post("/login", loginData);
        String token = loginResult.getString("token");
        client.setToken(token);
        
        // 获取用户列表
        JSONArray users = client.get("/users").getJSONArray("data");
        System.out.println("用户数：" + users.length());
        
        // 创建用户
        JSONObject newUser = new JSONObject();
        newUser.put("name", "张三");
        newUser.put("email", "zhangsan@example.com");
        JSONObject created = client.post("/users", newUser);
        System.out.println("创建成功：" + created.getInt("id"));
        
        // 更新用户
        JSONObject updateData = new JSONObject();
        updateData.put("name", "李四");
        JSONObject updated = client.put("/users/123", updateData);
        
        // 删除用户
        JSONObject deleted = client.delete("/users/123");
    }
}
```

### 示例 2: 文件下载管理器

```java
import com.gdxsoft.easyweb.utils.UNet;
import java.io.*;

public class DownloadManager {
    
    /**
     * 下载文件
     */
    public static boolean download(String url, String savePath) {
        UNet net = new UNet();
        net.setIsShowLog(true);
        
        byte[] data = net.downloadData(url);
        if (data == null) {
            System.err.println("下载失败：" + net.getLastErr());
            return false;
        }
        
        try {
            File file = new File(savePath);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
            System.out.println("下载完成：" + savePath + " (" + data.length + " 字节)");
            return true;
        } catch (IOException e) {
            System.err.println("保存失败：" + e.getMessage());
            return false;
        }
    }
    
    /**
     * 分块下载（支持断点续传）
     */
    public static boolean downloadWithResume(String url, String savePath, int chunkSize) {
        File file = new File(savePath);
        long downloadedSize = file.exists() ? file.length() : 0;
        
        UNet net = new UNet();
        
        try {
            // 获取文件大小
            net.doGet(url);
            Map<String, String> headers = net.getResponseHeaders();
            String contentLengthStr = headers.get("Content-Length");
            if (contentLengthStr == null) {
                System.err.println("无法获取文件大小");
                return false;
            }
            
            long totalSize = Long.parseLong(contentLengthStr);
            System.out.println("文件总大小：" + totalSize + " 字节");
            System.out.println("已下载：" + downloadedSize + " 字节");
            
            if (downloadedSize >= totalSize) {
                System.out.println("文件已下载完成");
                return true;
            }
            
            // 分块下载
            FileOutputStream fos = new FileOutputStream(file, true);
            long position = downloadedSize;
            
            while (position < totalSize) {
                long end = Math.min(position + chunkSize - 1, totalSize - 1);
                String range = "bytes=" + position + "-" + end;
                
                net.addHeader("Range", range);
                byte[] chunk = net.downloadData(url);
                
                if (chunk == null) {
                    System.err.println("下载失败");
                    fos.close();
                    return false;
                }
                
                fos.write(chunk);
                position += chunk.length;
                System.out.println("进度：" + position + "/" + totalSize);
            }
            
            fos.close();
            System.out.println("下载完成");
            return true;
            
        } catch (Exception e) {
            System.err.println("下载异常：" + e.getMessage());
            return false;
        }
    }
    
    public static void main(String[] args) {
        // 简单下载
        download("https://example.com/file.zip", "/tmp/file.zip");
        
        // 分块下载（每块 1MB）
        downloadWithResume("https://example.com/largefile.zip", "/tmp/largefile.zip", 1024 * 1024);
    }
}
```

### 示例 3: 网页爬虫

```java
import com.gdxsoft.easyweb.utils.UNet;
import java.util.*;
import java.util.regex.*;

public class WebCrawler {
    
    private UNet net;
    private Set<String> visitedUrls;
    private Queue<String> urlQueue;
    private int maxDepth;
    
    public WebCrawler() {
        this.net = new UNet();
        this.net.setEncode("UTF-8");
        this.net.setUserAgent(UNet.AGENT);
        this.visitedUrls = new HashSet<>();
        this.urlQueue = new LinkedList<>();
        this.maxDepth = 3;
    }
    
    /**
     * 爬取网页
     */
    public void crawl(String startUrl) {
        urlQueue.offer(startUrl);
        
        while (!urlQueue.isEmpty()) {
            String url = urlQueue.poll();
            
            if (visitedUrls.contains(url)) {
                continue;
            }
            
            visitedUrls.add(url);
            System.out.println("爬取：" + url);
            
            String html = net.doGet(url);
            if (html == null) {
                System.err.println("爬取失败：" + url);
                continue;
            }
            
            // 提取标题
            String title = extractTitle(html);
            System.out.println("标题：" + title);
            
            // 提取链接
            List<String> links = extractLinks(html);
            for (String link : links) {
                String absoluteUrl = toAbsoluteUrl(link, url);
                if (absoluteUrl != null && !visitedUrls.contains(absoluteUrl)) {
                    urlQueue.offer(absoluteUrl);
                }
            }
            
            // 限制爬取深度
            if (visitedUrls.size() >= maxDepth * 10) {
                break;
            }
        }
        
        System.out.println("爬取完成，共访问 " + visitedUrls.size() + " 个页面");
    }
    
    private String extractTitle(String html) {
        Pattern pattern = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "未知标题";
    }
    
    private List<String> extractLinks(String html) {
        List<String> links = new ArrayList<>();
        Pattern pattern = Pattern.compile("href=[\"'](.*?)[\"']", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        
        while (matcher.find()) {
            links.add(matcher.group(1));
        }
        
        return links;
    }
    
    private String toAbsoluteUrl(String link, String baseUrl) {
        if (link.startsWith("http://") || link.startsWith("https://")) {
            return link;
        }
        
        if (link.startsWith("/")) {
            try {
                URL base = new URL(baseUrl);
                return base.getProtocol() + "://" + base.getHost() + link;
            } catch (Exception e) {
                return null;
            }
        }
        
        return null;
    }
    
    public static void main(String[] args) {
        WebCrawler crawler = new WebCrawler();
        crawler.maxDepth = 5;
        crawler.crawl("https://example.com");
    }
}
```

### 示例 4: 微信 API 调用

```java
import com.gdxsoft.easyweb.utils.UNet;
import org.json.JSONObject;

public class WeChatApi {
    
    private String appId;
    private String appSecret;
    private String accessToken;
    private long accessTokenExpiresAt;
    private UNet net;
    
    public WeChatApi(String appId, String appSecret) {
        this.appId = appId;
        this.appSecret = appSecret;
        this.net = new UNet();
        this.net.setEncode("UTF-8");
    }
    
    /**
     * 获取 Access Token
     */
    private String getAccessToken() throws Exception {
        // 检查 Token 是否过期
        if (accessToken != null && System.currentTimeMillis() < accessTokenExpiresAt) {
            return accessToken;
        }
        
        String url = "https://api.weixin.qq.com/cgi-bin/token?" +
                     "grant_type=client_credential&appid=" + appId +
                     "&secret=" + appSecret;
        
        String result = net.doGet(url);
        JSONObject json = new JSONObject(result);
        
        if (json.has("errcode")) {
            throw new Exception("获取 Token 失败：" + json.getString("errmsg"));
        }
        
        accessToken = json.getString("access_token");
        accessTokenExpiresAt = System.currentTimeMillis() + (json.getInt("expires_in") - 300) * 1000;
        
        return accessToken;
    }
    
    /**
     * 创建菜单
     */
    public boolean createMenu(JSONObject menu) throws Exception {
        String token = getAccessToken();
        String url = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=" + token;
        
        net.addHeader("Content-Type", "application/json");
        String result = net.doPost(url, menu.toString());
        
        JSONObject json = new JSONObject(result);
        return json.getString("errmsg").equals("ok");
    }
    
    /**
     * 发送客服消息
     */
    public boolean sendCustomMessage(String openId, String content) throws Exception {
        String token = getAccessToken();
        String url = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + token;
        
        JSONObject msg = new JSONObject();
        msg.put("touser", openId);
        msg.put("msgtype", "text");
        
        JSONObject text = new JSONObject();
        text.put("content", content);
        msg.put("text", text);
        
        net.addHeader("Content-Type", "application/json");
        String result = net.doPost(url, msg.toString());
        
        JSONObject json = new JSONObject(result);
        return json.getString("errmsg").equals("ok");
    }
    
    /**
     * 上传临时素材
     */
    public String uploadMedia(String filePath, String type) throws Exception {
        String token = getAccessToken();
        String url = "https://api.weixin.qq.com/cgi-bin/media/upload?access_token=" + token +
                     "&type=" + type;
        
        String result = net.doUpload(url, "media", filePath);
        JSONObject json = new JSONObject(result);
        
        if (json.has("errcode")) {
            throw new Exception("上传失败：" + json.getString("errmsg"));
        }
        
        return json.getString("media_id");
    }
    
    public static void main(String[] args) throws Exception {
        WeChatApi api = new WeChatApi("your_app_id", "your_app_secret");
        
        // 发送客服消息
        api.sendCustomMessage("user_open_id", "您好，这是测试消息");
        
        // 上传临时素材
        String mediaId = api.uploadMedia("/path/to/image.jpg", "image");
        System.out.println("素材 ID: " + mediaId);
    }
}
```

---

## 注意事项

### 1. 资源释放

- UNet 会自动关闭 HttpClient 连接
- 大量并发请求建议使用连接池

### 2. 内存管理

- 下载大文件时注意内存使用
- 建议分块下载或使用流式处理

### 3. SSL 证书

- UNet 默认信任所有 SSL 证书
- 生产环境建议配置正确的证书验证

### 4. 超时设置

- 默认超时时间较长（500 秒）
- 建议根据实际需求设置合理的超时时间

### 5. 重定向

- 自动处理 301/302 重定向
- 最大重定向次数为 7 次

### 6. 字符编码

- 默认使用 UTF-8 编码
- 处理中文网站时注意编码设置

### 7. Cookie 管理

- 自动保存和发送 Cookie
- 多会话场景使用不同的 UNet 实例

---

## 相关类

| 类名 | 包路径 | 说明 |
|------|--------|------|
| `UNet` | `com.gdxsoft.easyweb.utils` | 网络请求工具 |
| `UUrl` | `com.gdxsoft.easyweb.utils` | URL 处理工具 |
| `Utils` | `com.gdxsoft.easyweb.utils` | 通用工具类 |
