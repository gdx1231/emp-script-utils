package com.gdxsoft.easyweb.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.utils.msnet.MStr;

/**
 * 访问网络的工具类
 *
 * @author admin
 *
 */
public class UNet {
	private static Logger LOGGER = LoggerFactory.getLogger(UNet.class);
	public static String AGENT_4 = "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0;)";
	public static String AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.54 Safari/537.36";
	public static int C_TIME_OUT = 500000;
	public static int R_TIME_OUT = 500000;

	private String _LastUrl;
	private boolean _IsShowLog = false;
	private HashMap<String, String> _Headers;
	private HashMap<String, String> _Cookies;

	private Map<String, String> _ResponseHeaders;

	private String userAgent;
	private int _LastStatusCode;

	/** 最后一次响应的 headers */
	private Map<String, List<String>> _LastResponse;

	private String _LastErr;
	private String _LastResult;
	private byte[] _LastBuf;

	private int redirectInc;
	// 限制最大Redirect次数
	private int _LimitRedirectInc = 7;

	private String _Encode; // 字符集
	private URL _ReturnUrl;

	// Ignore invalid cookie warning
	private boolean ignoreInvalidCookieWarn;
	private int timeout;

	// Proxy settings
	private String _ProxyHost;
	private int _ProxyPort;
	private String _ProxyScheme = "http";
	private String _ProxyUser;
	private String _ProxyPassword;

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * 设置网络代理（默认 HTTP 协议）
	 *
	 * @param host 代理主机名或 IP
	 * @param port 代理端口（1-65535）
	 */
	public void setProxy(String host, int port) {
		if (host == null || host.trim().isEmpty()) {
			throw new IllegalArgumentException("Proxy host cannot be null or empty");
		}
		if (port < 1 || port > 65535) {
			throw new IllegalArgumentException("Proxy port must be between 1 and 65535");
		}
		this._ProxyHost = host;
		this._ProxyPort = port;
		this._ProxyScheme = "http";
	}

	/**
	 * 设置网络代理
	 *
	 * @param host   代理主机名或 IP
	 * @param port   代理端口（1-65535）
	 * @param scheme 代理协议（http/https/socks）
	 */
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

	/**
	 * 设置网络代理（含用户名密码认证）
	 *
	 * @param host     代理主机名或 IP
	 * @param port     代理端口（1-65535）
	 * @param scheme   代理协议（http/https/socks）
	 * @param user     代理用户名（null 表示无需认证）
	 * @param password 代理密码（null 表示无需认证）
	 */
	public void setProxy(String host, int port, String scheme, String user, String password) {
		setProxy(host, port, scheme);
		this._ProxyUser = user;
		this._ProxyPassword = password;
	}

	/**
	 * 清除代理设置
	 */
	public void clearProxy() {
		this._ProxyHost = null;
		this._ProxyPort = 0;
		this._ProxyScheme = "http";
		this._ProxyUser = null;
		this._ProxyPassword = null;
	}

	public UNet() {
		this._Headers = new HashMap<String, String>();
		_Cookies = new HashMap<String, String>();
	}

	/**
	 * 初始化
	 *
	 * @param cookie      cookie字符串
	 * @param charsetName 字符集
	 */
	public UNet(String cookie, String charsetName) {
		this._Headers = new HashMap<String, String>();
		_Cookies = new HashMap<String, String>();

		this._Cookie = cookie;
		this._Encode = charsetName;
		this.initCookies();
	}

	/**
	 * 获取 CookieStore (返回 cookies 映射)
	 *
	 * @return cookies 映射
	 */
	public Map<String, String> getCookieStore() {
		return _Cookies;
	}

	/**
	 * cookie转换为 JSONArray
	 *
	 * @return cookie转换为 JSONArray
	 */
	public JSONArray listCookieStoreCookes() {
		JSONArray arr = new JSONArray();
		for (Map.Entry<String, String> entry : this._Cookies.entrySet()) {
			arr.put(entry.getKey() + "=" + entry.getValue());
		}
		return arr;
	}

	/**
	 * 设置 CookieStore
	 *
	 * @param cookieStore cookies 映射
	 */
	public void setCookieStore(Map<String, String> cookieStore) {
		this._Cookies = cookieStore != null ? new HashMap<>(cookieStore) : new HashMap<>();
	}

	/**
	 * 最后一次返回状态码
	 *
	 * @return 最后一次返回状态码
	 */
	public int getLastStatusCode() {
		return _LastStatusCode;
	}

	/**
	 * 获取 UserAgent
	 *
	 * @return UserAgent 浏览器代理
	 */
	public String getUserAgent() {
		if (this.userAgent == null) {
			return UNet.AGENT;
		} else {
			return userAgent;
		}
	}

	/**
	 * 设置 UserAgent
	 *
	 * @param userAgent 浏览器代理
	 */
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	/**
	 * 增加自定义请求 header
	 *
	 * @param key header的key
	 * @param v   header的值
	 */
	public void addHeader(String key, String v) {
		if (this._Headers.containsKey(key)) {
			this._Headers.remove(key);
		}
		this._Headers.put(key, v);
	}

	/**
	 * 增加自定义请求 headers，content-length,origin,host,connection等头部会过滤掉
	 *
	 * @param headers
	 */
	public void addHeaders(Map<String, String> headers) {
		if (headers == null) {
			return;
		}
		for (String key : headers.keySet()) {
			if ("content-length".equalsIgnoreCase(key)) {
				continue; // content-length由UNet自动处理
			}
			if ("origin".equalsIgnoreCase(key) || "host".equalsIgnoreCase(key) || "connection".equalsIgnoreCase(key)) {
				continue; // origin, host, connection等头部通常不需要在API请求中设置
			}
			String v = headers.get(key);
			this.addHeader(key, v);
		}
	}

	/**
	 * 清除自定义请求 headers
	 */
	public void clearHeaders() {
		this._Headers.clear();
	}

	/**
	 * 获取cookies字符串
	 *
	 * @return cookies字符串
	 */
	public String getCookies() {
		MStr s = new MStr();
		Iterator<String> it = this._Cookies.keySet().iterator();
		while (it.hasNext()) {
			if (s.length() > 0) {
				s.a("; ");
			}
			String k = it.next();
			String v = _Cookies.get(k);
			s.a(k);
			s.a("=");
			s.a(v);
		}
		return s.toString();
	}

	/**
	 * 根据cookie的字符串，拆分到 _Cookies
	 */
	void initCookies() {
		if (_Cookie == null) {
			return;
		}
		String[] cks = _Cookie.split(";");
		for (int i = 0; i < cks.length; i++) {
			String[] kk = cks[i].split("=");
			if (kk.length == 2) {
				this.addCookie(kk[0], kk[1]);
			}
		}
	}

	/**
	 * 添加 cookie
	 *
	 * @param name 名称
	 * @param val  值
	 */
	void addCookie(String name, String val) {
		name = name.trim();
		if (this._Cookies.containsKey(name)) {
			this._Cookies.remove(name);
		}
		this._Cookies.put(name, val);
	}

	/**
	 * 生成提交的body字符串（处理特殊字符后返回）
	 *
	 * @param body 提交的信息
	 * @return 处理后的 body 字符串
	 */
	public String createStringEntity(String body) {
		body = body.replace("\\u201c", "\u201c");
		body = body.replace("\\u201d", "\u201d");
		return body;
	}

	// ===================== HTTP method implementations =====================

	/**
	 * 发送Patch请求
	 *
	 * @param u    发送地址
	 * @param body 发送正文
	 * @return 执行结果
	 */
	public String doPatch(String u, String body) {
		if (this._IsShowLog) {
			LOGGER.info("PATCH: " + u);
		}
		Date t1 = new Date();
		try {
			HttpClient client = getHttpClient(u);
			String processedBody = this.createStringEntity(body);
			HttpRequest.Builder builder = newRequestBuilder(u);
			builder.method("PATCH", HttpRequest.BodyPublishers.ofString(processedBody, getCharsetObj()));
			HttpRequest request = builder.build();
			String result = executeAndHandleString(client, request);
			logTiming(t1, result);
			return result;
		} catch (Exception e) {
			this._LastErr = e.getMessage();
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	/**
	 * 同 doPatch
	 *
	 * @param u
	 * @param body
	 * @return
	 */
	public String patch(String u, String body) {
		return this.doPatch(u, body);
	}

	/**
	 * 发送 PATCH 请求访问本地应用并根据传递参数不同返回不同结果
	 *
	 * @param url  地址
	 * @param vals 参数
	 * @return 执行结果
	 */
	public String doPatch(String url, Map<String, String> vals) {
		if (this._IsShowLog) {
			LOGGER.info("PATCH " + url);
		}
		Date t1 = new Date();
		try {
			HttpClient client = getHttpClient(url);
			String formData = encodeFormData(vals);
			HttpRequest.Builder builder = newRequestBuilder(url);
			builder.method("PATCH", HttpRequest.BodyPublishers.ofString(formData, getCharsetObj()));
			builder.header("Content-Type", "application/x-www-form-urlencoded; charset=" + getCharset());
			HttpRequest request = builder.build();
			String result = executeAndHandleString(client, request);
			logTiming(t1, result);
			return result;
		} catch (Exception e) {
			this._LastErr = e.getMessage();
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	/**
	 * 提交消息并下载
	 *
	 * @param u    Url地址
	 * @param body 提交的内容
	 * @return 下载的二进制
	 */
	public byte[] postMsgAndDownload(String u, String body) {
		if (this._IsShowLog) {
			LOGGER.info("POST: " + u);
		}
		Date t1 = new Date();
		this._LastResult = null;
		this._LastBuf = null;
		try {
			HttpClient client = getHttpClient(u);
			String processedBody = this.createStringEntity(body);
			HttpRequest.Builder builder = newRequestBuilder(u);
			builder.POST(HttpRequest.BodyPublishers.ofString(processedBody, getCharsetObj()));
			HttpRequest request = builder.build();

			HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
			this._LastStatusCode = response.statusCode();
			saveResponseHeaders(response);

			if (200 != this._LastStatusCode) {
				LOGGER.error("response code: " + this._LastStatusCode);
				return null;
			}

			this._LastBuf = response.body();
			logTimingBytes(t1, this._LastBuf);
			return this._LastBuf;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			this._LastErr = e.getLocalizedMessage();
			return null;
		}
	}

	/**
	 * get 下载二进制
	 *
	 * @param url 地址
	 * @return 文件二进制
	 */
	public byte[] downloadData(String url) {
		if (this._IsShowLog) {
			LOGGER.info("DW " + url);
		}
		// SOCKS 代理回退到 HttpURLConnection
		if (isSocksProxy()) {
			return downloadDataViaSocks(url);
		}
		Date t1 = new Date();
		this._LastBuf = null;
		this._LastResult = null;
		try {
			HttpClient client = getHttpClient(url);
			HttpRequest.Builder builder = newRequestBuilder(url);
			builder.GET();
			HttpRequest request = builder.build();

			HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
			this._LastStatusCode = response.statusCode();
			saveResponseHeaders(response);

			if (200 != this._LastStatusCode) {
				LOGGER.error("response code: " + this._LastStatusCode);
				return null;
			}

			this._LastBuf = response.body();
			logTimingBytes(t1, this._LastBuf);
			return this._LastBuf;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			this._LastErr = e.getLocalizedMessage();
			return null;
		}
	}

	/**
	 * PUT模式
	 *
	 * @param url  地址
	 * @param body 提交的内容
	 * @return 执行结果
	 */
	public String doPut(String url, String body) {
		if (this._IsShowLog) {
			LOGGER.info("PUT " + url);
		}
		Date t1 = new Date();
		try {
			HttpClient client = getHttpClient(url);
			String processedBody = this.createStringEntity(body);
			HttpRequest.Builder builder = newRequestBuilder(url);
			builder.PUT(HttpRequest.BodyPublishers.ofString(processedBody, getCharsetObj()));
			HttpRequest request = builder.build();
			String result = executeAndHandleString(client, request);
			logTiming(t1, result);
			return result;
		} catch (Exception e) {
			this._LastErr = e.getMessage();
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	/**
	 * 发送 PUT 请求访问本地应用并根据传递参数不同返回不同结果
	 *
	 * @param url  地址
	 * @param vals 参数
	 * @return 执行结果
	 */
	public String doPut(String url, Map<String, String> vals) {
		if (this._IsShowLog) {
			LOGGER.info("PUT " + url);
		}
		Date t1 = new Date();
		try {
			HttpClient client = getHttpClient(url);
			String formData = encodeFormData(vals);
			HttpRequest.Builder builder = newRequestBuilder(url);
			builder.PUT(HttpRequest.BodyPublishers.ofString(formData, getCharsetObj()));
			builder.header("Content-Type", "application/x-www-form-urlencoded; charset=" + getCharset());
			HttpRequest request = builder.build();
			String result = executeAndHandleString(client, request);
			logTiming(t1, result);
			return result;
		} catch (Exception e) {
			this._LastErr = e.getMessage();
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	/**
	 * DELETE 模式
	 *
	 * @param url 地址
	 * @return 执行结果
	 */
	public String doDelete(String url) {
		if (this._IsShowLog) {
			LOGGER.info("DELETE " + url);
		}
		Date t1 = new Date();
		try {
			HttpClient client = getHttpClient(url);
			HttpRequest.Builder builder = newRequestBuilder(url);
			builder.DELETE();
			HttpRequest request = builder.build();
			String result = executeAndHandleString(client, request);
			logTiming(t1, result);
			return result;
		} catch (Exception e) {
			this._LastErr = e.getMessage();
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	/**
	 * DELETE模式
	 *
	 * @param url  地址
	 * @param body 提交的内容
	 * @return 执行结果
	 */
	public String doDelete(String url, String body) {
		if (this._IsShowLog) {
			LOGGER.info("DELETE " + url);
		}
		Date t1 = new Date();
		try {
			HttpClient client = getHttpClient(url);
			String processedBody = this.createStringEntity(body);
			HttpRequest.Builder builder = newRequestBuilder(url);
			builder.method("DELETE", HttpRequest.BodyPublishers.ofString(processedBody, getCharsetObj()));
			HttpRequest request = builder.build();
			String result = executeAndHandleString(client, request);
			logTiming(t1, result);
			return result;
		} catch (Exception e) {
			this._LastErr = e.getMessage();
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	/**
	 * 发送 DELETE 请求访问本地应用并根据传递参数不同返回不同结果
	 *
	 * @param url  地址
	 * @param vals 参数
	 * @return 执行结果
	 */
	public String doDelete(String url, Map<String, String> vals) {
		if (this._IsShowLog) {
			LOGGER.info("DELETE " + url);
		}
		Date t1 = new Date();
		try {
			HttpClient client = getHttpClient(url);
			String formData = encodeFormData(vals);
			HttpRequest.Builder builder = newRequestBuilder(url);
			builder.method("DELETE", HttpRequest.BodyPublishers.ofString(formData, getCharsetObj()));
			builder.header("Content-Type", "application/x-www-form-urlencoded; charset=" + getCharset());
			HttpRequest request = builder.build();
			String result = executeAndHandleString(client, request);
			logTiming(t1, result);
			return result;
		} catch (Exception e) {
			this._LastErr = e.getMessage();
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	/**
	 * get 获取网页文本
	 *
	 * @param url 地址
	 * @return 执行结果
	 */
	public String doGet(String url) {
		if (this._IsShowLog) {
			LOGGER.info("GET " + url);
		}
		// SOCKS 代理回退到 HttpURLConnection（java.net.http.HttpClient 不支持 SOCKS）
		if (isSocksProxy()) {
			return doGetViaSocks(url);
		}
		Date t1 = new Date();
		try {
			HttpClient client = getHttpClient(url);
			HttpRequest.Builder builder = newRequestBuilder(url);
			builder.GET();
			HttpRequest request = builder.build();
			String result = executeAndHandleString(client, request);
			logTiming(t1, result);
			return result;
		} catch (Exception e) {
			this._LastErr = e.getMessage();
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	/**
	 * 发送 post请求访问本地应用并根据传递参数不同返回不同结果
	 *
	 * @param url  地址
	 * @param vals 参数
	 * @return 执行结果
	 */
	public String doPost(String url, Map<String, String> vals) {
		if (this._IsShowLog) {
			LOGGER.info("POST " + url);
		}
		Date t1 = new Date();
		try {
			HttpClient client = getHttpClient(url);
			String formData = encodeFormData(vals);
			HttpRequest.Builder builder = newRequestBuilder(url);
			builder.POST(HttpRequest.BodyPublishers.ofString(formData, getCharsetObj()));
			builder.header("Content-Type", "application/x-www-form-urlencoded; charset=" + getCharset());
			HttpRequest request = builder.build();
			String result = executeAndHandleString(client, request);
			logTiming(t1, result);
			return result;
		} catch (Exception e) {
			this._LastErr = e.getMessage();
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	/**
	 * 提交body 消息
	 *
	 * @param url  提交地址
	 * @param body 提交内容
	 * @return 执行结果
	 */
	public String doPost(String url, String body) {
		if (this._IsShowLog) {
			LOGGER.info("POST: " + url);
		}
		Date t1 = new Date();
		try {
			HttpClient client = getHttpClient(url);
			String processedBody = this.createStringEntity(body);
			HttpRequest.Builder builder = newRequestBuilder(url);
			builder.POST(HttpRequest.BodyPublishers.ofString(processedBody, getCharsetObj()));
			HttpRequest request = builder.build();
			String result = executeAndHandleString(client, request);
			logTiming(t1, result);
			return result;
		} catch (Exception e) {
			this._LastErr = e.getMessage();
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	/**
	 * 提交body 消息
	 *
	 * @param url      提交地址
	 * @param bodyBuff 提交二进制内容
	 * @return 执行结果
	 */
	public String doPost(String url, byte[] bodyBuff) {
		if (this._IsShowLog) {
			LOGGER.info("POST: " + url);
		}
		Date t1 = new Date();
		try {
			HttpClient client = getHttpClient(url);
			HttpRequest.Builder builder = newRequestBuilder(url);
			builder.POST(HttpRequest.BodyPublishers.ofByteArray(bodyBuff));
			HttpRequest request = builder.build();
			String result = executeAndHandleString(client, request);
			logTiming(t1, result);
			return result;
		} catch (Exception e) {
			this._LastErr = e.getMessage();
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	/**
	 * 提交body 消息，同 doPost(u, body)
	 *
	 * @param u    提交地址
	 * @param body 提交内容
	 * @return 执行结果
	 */
	public String postMsg(String u, String body) {
		return this.doPost(u, body);
	}

	// ===================== Upload methods =====================

	/**
	 * 上传文件和参数
	 *
	 * @param url       地址
	 * @param fieldName 文件字段名称
	 * @param filePath  文件地址
	 * @param vals      参数
	 * @return 执行结果
	 */
	public String doUpload(String url, String fieldName, String filePath, HashMap<String, String> vals) {
		if (this._IsShowLog) {
			LOGGER.info("U " + url);
		}
		Date t1 = new Date();
		try {
			String boundary = "----UNetBoundary" + UUID.randomUUID().toString().replace("-", "");
			byte[] body = buildMultipartBody(boundary, fieldName, filePath, vals);

			HttpClient client = getHttpClient(url);
			HttpRequest.Builder builder = newRequestBuilder(url);
			builder.POST(HttpRequest.BodyPublishers.ofByteArray(body));
			builder.header("Content-Type", "multipart/form-data; boundary=" + boundary);
			HttpRequest request = builder.build();

			String result = executeAndHandleString(client, request);
			logTiming(t1, result);
			return result;
		} catch (Exception e) {
			this._LastErr = e.getMessage();
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	/**
	 * 上传文件和参数 (使用 Map 形式的参数)
	 *
	 * @param url        地址
	 * @param fieldName  文件字段名称
	 * @param filePath   文件地址
	 * @param formparams 参数
	 * @return 执行结果
	 */
	public String doUpload(String url, String fieldName, String filePath, Map<String, String> formparams) {
		HashMap<String, String> vals = formparams != null ? new HashMap<>(formparams) : null;
		return this.doUpload(url, fieldName, filePath, vals);
	}

	/**
	 * 上传文件
	 *
	 * @param url       地址
	 * @param fieldName 上传域名
	 * @param filePath  文件地址
	 * @return 执行结果
	 */
	public String doUpload(String url, String fieldName, String filePath) {
		HashMap<String, String> vals = null;
		return this.doUpload(url, fieldName, filePath, vals);
	}

	// ===================== Core private methods =====================

	/**
	 * 根据url 获取 HttpClient(http/https)
	 *
	 * @param url 目标 URL
	 * @return HttpClient
	 */
	/**
	 * 是否为 SOCKS 代理（java.net.http.HttpClient 不支持 SOCKS，需回退到 HttpURLConnection）
	 */
	private boolean isSocksProxy() {
		return "socks".equalsIgnoreCase(this._ProxyScheme)
				&& this._ProxyHost != null && !this._ProxyHost.isEmpty();
	}

	/**
	 * 通过 SOCKS 代理执行 GET 请求（使用 HttpURLConnection 回退）
	 */
	private String doGetViaSocks(String url) {
		if (this._IsShowLog) {
			LOGGER.info("SOCKS GET: {}", url);
		}
		// HTTPS over SOCKS5 使用自定义实现（HttpURLConnection 不支持 HTTPS+SOCKS5）
		if (url.toLowerCase().startsWith("https")) {
			return doGetViaSocksHttps(url);
		}
		java.net.Proxy socksProxy = new java.net.Proxy(java.net.Proxy.Type.SOCKS,
				new InetSocketAddress(this._ProxyHost, this._ProxyPort));
		try {
			HttpURLConnection conn = (HttpURLConnection) new URI(url).toURL().openConnection(socksProxy);
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(timeout > 0 ? timeout : C_TIME_OUT);
			conn.setReadTimeout(timeout > 0 ? timeout : R_TIME_OUT);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("User-Agent", getUserAgent());
			String cookies = getCookies();
			if (cookies != null && !cookies.isEmpty()) {
				conn.setRequestProperty("Cookie", cookies);
			}
			for (Map.Entry<String, String> h : _Headers.entrySet()) {
				conn.setRequestProperty(h.getKey(), h.getValue());
			}
			if (_LastUrl != null) {
				conn.setRequestProperty("Referer", _LastUrl);
			}

			this._LastStatusCode = conn.getResponseCode();
			this._LastUrl = url;
			_ResponseHeaders = new HashMap<>();
			conn.getHeaderFields().forEach((k, v) -> {
				if (k != null && v != null && !v.isEmpty()) {
					_ResponseHeaders.put(k.toLowerCase(), v.get(0));
					if ("set-cookie".equalsIgnoreCase(k)) {
						for (String cv : v) {
							readCookies(List.of(cv));
						}
					}
				}
			});

			if (_LastStatusCode >= 400) {
				LOGGER.error("{} {}", _LastStatusCode, url);
			}

			if (_LastStatusCode == 301 || _LastStatusCode == 302) {
				String location = conn.getHeaderField("Location");
				if (location != null) {
					this.redirectInc++;
					if (this.redirectInc > _LimitRedirectInc) {
						LOGGER.error("太多的重定向");
						return null;
					}
					return doGet(location);
				}
			}

			InputStream in = _LastStatusCode >= 400 ? conn.getErrorStream() : conn.getInputStream();
			if (in != null) {
				byte[] body = in.readAllBytes();
				in.close();
				this._LastResult = new String(body, getCharset());
			}
			conn.disconnect();
			return this._LastResult;
		} catch (Exception e) {
			this._LastErr = e.getMessage();
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	/**
	 * HTTPS GET through SOCKS5 proxy using HttpsOverSocks5
	 */
	private String doGetViaSocksHttps(String url) {
		try {
			HttpsOverSocks5 socks = new HttpsOverSocks5(this._ProxyHost, this._ProxyPort,
					this._ProxyUser, this._ProxyPassword);
			Map<String, String> reqHeaders = new HashMap<>();
			reqHeaders.put("User-Agent", getUserAgent());
			String cookies = getCookies();
			if (cookies != null && !cookies.isEmpty()) {
				reqHeaders.put("Cookie", cookies);
			}
			for (Map.Entry<String, String> h : _Headers.entrySet()) {
				reqHeaders.put(h.getKey(), h.getValue());
			}
			if (_LastUrl != null) {
				reqHeaders.put("Referer", _LastUrl);
			}

			int timeoutMs = timeout > 0 ? timeout : C_TIME_OUT;
			HttpsOverSocks5.Response resp = socks.get(url, reqHeaders, timeoutMs);

			this._LastStatusCode = resp.statusCode;
			this._LastUrl = url;
			_ResponseHeaders = resp.headers;

			// Parse Set-Cookie
			String setCk = resp.headers.get("set-cookie");
			if (setCk != null) {
				readCookies(List.of(setCk));
			}

			if (_LastStatusCode >= 400) {
				LOGGER.error("{} {}", _LastStatusCode, url);
			}

			if (_LastStatusCode == 301 || _LastStatusCode == 302) {
				String location = resp.headers.get("location");
				if (location != null) {
					this.redirectInc++;
					if (this.redirectInc > _LimitRedirectInc) {
						LOGGER.error("太多的重定向");
						return null;
					}
					return doGet(location);
				}
			}

			this._LastResult = resp.bodyAsString(getCharset());
			return this._LastResult;
		} catch (Exception e) {
			this._LastErr = e.getMessage();
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	/**
	 * 通过 SOCKS 代理下载二进制（使用 HttpURLConnection 回退）
	 */
	private byte[] downloadDataViaSocks(String url) {
		if (this._IsShowLog) {
			LOGGER.info("SOCKS DW: {}", url);
		}
		// HTTPS over SOCKS5 使用自定义实现
		if (url.toLowerCase().startsWith("https")) {
			return downloadDataViaSocksHttps(url);
		}
		java.net.Proxy socksProxy = new java.net.Proxy(java.net.Proxy.Type.SOCKS,
				new InetSocketAddress(this._ProxyHost, this._ProxyPort));
		try {
			HttpURLConnection conn = (HttpURLConnection) new URI(url).toURL().openConnection(socksProxy);
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(timeout > 0 ? timeout : C_TIME_OUT);
			conn.setReadTimeout(timeout > 0 ? timeout : R_TIME_OUT);
			conn.setRequestProperty("User-Agent", getUserAgent());
			String cookies = getCookies();
			if (cookies != null && !cookies.isEmpty()) {
				conn.setRequestProperty("Cookie", cookies);
			}
			for (Map.Entry<String, String> h : _Headers.entrySet()) {
				conn.setRequestProperty(h.getKey(), h.getValue());
			}

			this._LastStatusCode = conn.getResponseCode();
			this._LastUrl = url;
			_ResponseHeaders = new HashMap<>();
			conn.getHeaderFields().forEach((k, v) -> {
				if (k != null && v != null && !v.isEmpty()) {
					_ResponseHeaders.put(k.toLowerCase(), v.get(0));
				}
			});

			InputStream in = _LastStatusCode >= 400 ? conn.getErrorStream() : conn.getInputStream();
			if (in != null) {
				this._LastBuf = in.readAllBytes();
				in.close();
			}
			conn.disconnect();
			return this._LastBuf;
		} catch (Exception e) {
			this._LastErr = e.getMessage();
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	/**
	 * HTTPS download through SOCKS5 proxy using HttpsOverSocks5
	 */
	private byte[] downloadDataViaSocksHttps(String url) {
		try {
			HttpsOverSocks5 socks = new HttpsOverSocks5(this._ProxyHost, this._ProxyPort,
					this._ProxyUser, this._ProxyPassword);
			Map<String, String> reqHeaders = new HashMap<>();
			reqHeaders.put("User-Agent", getUserAgent());
			String cookies = getCookies();
			if (cookies != null && !cookies.isEmpty()) {
				reqHeaders.put("Cookie", cookies);
			}
			for (Map.Entry<String, String> h : _Headers.entrySet()) {
				reqHeaders.put(h.getKey(), h.getValue());
			}

			int timeoutMs = timeout > 0 ? timeout : C_TIME_OUT;
			HttpsOverSocks5.Response resp = socks.get(url, reqHeaders, timeoutMs);

			this._LastStatusCode = resp.statusCode;
			this._LastUrl = url;
			_ResponseHeaders = resp.headers;
			this._LastBuf = resp.body;
			return this._LastBuf;
		} catch (Exception e) {
			this._LastErr = e.getMessage();
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	private HttpClient getHttpClient(String url) {
		if (this._IsShowLog) {
			for (Map.Entry<String, String> entry : this._Cookies.entrySet()) {
				LOGGER.info(entry.getKey() + "=" + entry.getValue());
			}
		}

		HttpClient.Builder builder = HttpClient.newBuilder()
				.followRedirects(HttpClient.Redirect.NEVER) // 手动处理重定向
				.connectTimeout(Duration.ofMillis(timeout > 0 ? timeout : C_TIME_OUT));

		// Proxy
		boolean hasProxy = this._ProxyHost != null && !this._ProxyHost.isEmpty();
		if (hasProxy) {
			if (this._IsShowLog) {
				LOGGER.info("Using proxy: {}://{}:{}", this._ProxyScheme, this._ProxyHost, this._ProxyPort);
			}
			builder.proxy(ProxySelector.of(new InetSocketAddress(this._ProxyHost, this._ProxyPort)));

			// HTTP 代理认证
			if (this._ProxyUser != null && !this._ProxyUser.isEmpty()) {
				builder.authenticator(new java.net.Authenticator() {
					@Override
					protected java.net.PasswordAuthentication getPasswordAuthentication() {
						return new java.net.PasswordAuthentication(_ProxyUser,
								(_ProxyPassword != null ? _ProxyPassword : "").toCharArray());
					}
				});
			}
		}

		// SSL trust-all for HTTPS
		if (url.toLowerCase().startsWith("https")) {
			builder.sslContext(createTrustAllSSLContext());
		}

		this._LastUrl = url;
		return builder.build();
	}

	/**
	 * 创建信任所有证书的 SSLContext
	 *
	 * @return SSLContext
	 */
	private static SSLContext createTrustAllSSLContext() {
		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(null, new TrustManager[]{
					new X509TrustManager() {
						public void checkClientTrusted(X509Certificate[] chain, String authType) {}
						public void checkServerTrusted(X509Certificate[] chain, String authType) {}
						public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
					}
			}, new SecureRandom());
			return ctx;
		} catch (GeneralSecurityException e) {
			throw new RuntimeException("Failed to create trust-all SSL context", e);
		}
	}

	/**
	 * 构建请求 Builder，添加公共头部
	 *
	 * @param url 请求 URL
	 * @return HttpRequest.Builder
	 */
	private HttpRequest.Builder newRequestBuilder(String url) {
		HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));
		builder.header("User-Agent", getUserAgent());
		if (this._LastUrl != null) {
			builder.header("Referer", this._LastUrl);
			if (this._IsShowLog) {
				LOGGER.info("设置Referer : " + this._LastUrl);
			}
		}

		for (Map.Entry<String, String> h : this._Headers.entrySet()) {
			builder.header(h.getKey(), h.getValue());
			if (this._IsShowLog) {
				LOGGER.info("设置" + h.getKey() + " : " + h.getValue());
			}
		}

		if (this._Cookies != null && !this._Cookies.isEmpty()) {
			String cookies = this.getCookies();
			if (cookies.length() > 0) {
				if (this._IsShowLog) {
					LOGGER.info("设置Cookie : " + cookies);
				}
				builder.header("Cookie", cookies);
			}
		}
		return builder;
	}

	/**
	 * 编码表单数据
	 *
	 * @param vals 参数
	 * @return 编码后的表单数据
	 */
	private String encodeFormData(Map<String, String> vals) {
		String code = getCharset();
		StringBuilder sb = new StringBuilder();
		if (vals == null) {
			return sb.toString();
		}
		for (Map.Entry<String, String> e : vals.entrySet()) {
			if (sb.length() > 0) {
				sb.append("&");
			}
			try {
				sb.append(URLEncoder.encode(e.getKey(), code));
				sb.append("=");
				sb.append(URLEncoder.encode(e.getValue(), code));
			} catch (UnsupportedEncodingException ex) {
				throw new RuntimeException(ex);
			}
		}
		return sb.toString();
	}

	/**
	 * 构建 multipart 请求体
	 *
	 * @param boundary  分隔符
	 * @param fieldName 文件字段名称
	 * @param filePath  文件路径
	 * @param vals      额外参数
	 * @return 字节数组
	 * @throws IOException
	 */
	private byte[] buildMultipartBody(String boundary, String fieldName, String filePath, Map<String, String> vals) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		String code = getCharset();

		// Text fields
		if (vals != null) {
			for (Map.Entry<String, String> e : vals.entrySet()) {
				out.write(("--" + boundary + "\r\n").getBytes(code));
				out.write(("Content-Disposition: form-data; name=\"" + e.getKey() + "\"\r\n\r\n").getBytes(code));
				out.write(e.getValue().getBytes(code));
				out.write("\r\n".getBytes(code));
			}
		}

		// File field
		if (fieldName != null && filePath != null) {
			File f = new File(filePath);
			out.write(("--" + boundary + "\r\n").getBytes(code));
			out.write(("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + f.getName() + "\"\r\n").getBytes(code));
			out.write("Content-Type: application/octet-stream\r\n\r\n".getBytes(code));
			out.write(Files.readAllBytes(f.toPath()));
			out.write("\r\n".getBytes(code));
		}

		out.write(("--" + boundary + "--\r\n").getBytes(code));
		return out.toByteArray();
	}

	/**
	 * 执行请求并处理字符串响应（含重定向）
	 *
	 * @param client  HttpClient
	 * @param request HttpRequest
	 * @return 响应文本
	 */
	private String executeAndHandleString(HttpClient client, HttpRequest request) {
		try {
			HttpResponse<String> response = client.send(request,
					HttpResponse.BodyHandlers.ofString(Charset.forName(getCharset())));

			this._LastStatusCode = response.statusCode();
			saveResponseHeaders(response);

			this._LastResult = response.body();

			if (this._LastStatusCode >= 400) {
				LOGGER.error(this._LastStatusCode + " " + this._LastUrl);
			}

			// 处理重定向
			if (_LastStatusCode == 301 || _LastStatusCode == 302) {
				return checkAndHandleRedirectString();
			}
			return this._LastResult;
		} catch (Exception e) {
			this._LastErr = e.getMessage();
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	/**
	 * 保存响应头部并解析 Set-Cookie
	 *
	 * @param response 响应
	 */
	private void saveResponseHeaders(HttpResponse<?> response) {
		_LastResponse = response.headers().map();
		_ResponseHeaders = new HashMap<>();

		response.headers().map().forEach((name, values) -> {
			if (this._IsShowLog) {
				LOGGER.info(name + "=" + values);
			}
			// 存储第一个值到扁平映射
			if (values != null && !values.isEmpty()) {
				_ResponseHeaders.put(name, values.get(0));
			}

			// 解析 Set-Cookie
			if (name != null && name.equalsIgnoreCase("set-cookie")) {
				for (String cv : values) {
					String[] cks = cv.split(";");
					String[] scc = cks[0].split("=");
					if (scc.length >= 2) {
						this.addCookie(scc[0].trim(), scc[1]);
					} else if (scc.length == 1) {
						this.addCookie(scc[0].trim(), "");
					}
				}
			}
		});
	}

	/**
	 * 检查是否有重定向，有的化执行get（最多7次），没有返回最后执行的内容
	 *
	 * @return 响应文本
	 */
	private String checkAndHandleRedirectString() {
		if (this._LastStatusCode == 302 || this._LastStatusCode == 301) {
			this.redirectInc++;
			if (this.redirectInc > _LimitRedirectInc) {
				LOGGER.error("太多的重定向");
				return null;
			}
			String newUrl = this.get302Or301Location(_LastResponse);
			// 执行重定向
			return this.doGet(newUrl);
		} else {
			this.redirectInc = 0;
			return this._LastResult;
		}
	}

	/**
	 * 读取返回的Cookie
	 *
	 * @param responseHeaders 响应头映射
	 */
	public void readResponseCookies(Map<String, List<String>> responseHeaders) {
		if (responseHeaders == null) {
			return;
		}
		List<String> lst = new ArrayList<String>();
		for (Map.Entry<String, List<String>> entry : responseHeaders.entrySet()) {
			String h_name = entry.getKey();
			if (h_name != null && h_name.equalsIgnoreCase("Set-Cookie")) {
				List<String> values = entry.getValue();
				if (values != null) {
					lst.addAll(values);
					if (this.isShowLog()) {
						for (String v : values) {
							LOGGER.info(h_name + ": " + v);
						}
					}
				}
			}
		}
		this.readCookies(lst);
	}

	/**
	 * 处理 301, 302 移动的问题<br>
	 * 301 redirect: 301 代表永久性转移(Permanently Moved)<br>
	 * 302 redirect: 302 代表暂时性转移(Temporarily Moved )<br>
	 *
	 * @param responseHeaders 响应头映射
	 * @return 返回的 location的 url
	 */
	public String get302Or301Location(Map<String, List<String>> responseHeaders) {
		if (responseHeaders == null) {
			return null;
		}
		for (Map.Entry<String, List<String>> entry : responseHeaders.entrySet()) {
			if (entry.getKey() != null && entry.getKey().equalsIgnoreCase("location")) {
				List<String> values = entry.getValue();
				if (values != null && !values.isEmpty()) {
					return values.get(0);
				}
			}
		}
		return null;
	}

	/**
	 * 读取返回的Cookie
	 *
	 * @param lst
	 */
	void readCookies(List<String> lst) {
		if (lst == null) {
			return;
		}
		for (int ia = 0; ia < lst.size(); ia++) {
			String[] scs = lst.get(ia).split(";");
			// cookie 总是 名称/值在第0位，其它为描述信息
			String[] scc = scs[0].split("=");
			if (scc.length >= 2) {
				this.addCookie(scc[0].trim(), scc[1]);
			} else if (scc.length == 1) {
				this.addCookie(scc[0].trim(), "");
			}
		}
	}

	// ===================== Deprecated methods (HttpURLConnection based) =====================

	@Deprecated
	String readContent(URLConnection u) throws IOException {
		_ReturnUrl = u.getURL();
		for (String key : u.getHeaderFields().keySet()) {
			if (key != null && key.equalsIgnoreCase("Set-Cookie")) {
				List<String> lst = u.getHeaderFields().get(key);
				readCookies(lst);
			}
		}

		BufferedInputStream bis = new BufferedInputStream(u.getInputStream());
		ByteBuffer bb = ByteBuffer.allocate(1024 * 1024 * 10);
		byte[] buf = new byte[4096];
		int m = 0;
		int read;
		while ((read = bis.read(buf)) > 0) {
			m += read;
			bb.put(buf, 0, read);
		}
		String s;
		byte[] buf1 = new byte[m];
		System.arraycopy(bb.array(), 0, buf1, 0, buf1.length);
		bb.clear();
		if (this._Encode != null) {
			s = new String(buf1, _Encode);
		} else {
			s = new String(buf1);
		}
		bb.clear();

		bb = null;
		bis.close();

		return s;
	}

	@Deprecated
	URLConnection createConn(String url) throws IOException {
		URL u = new URL(url);
		URLConnection con = u.openConnection();
		con.addRequestProperty("User-Agent", this.getUserAgent());
		if (this._Cookie != null) {
			con.addRequestProperty("Cookie", this.getCookies());
		}
		if (this._LastUrl != null) {
			con.addRequestProperty("Referer", this._LastUrl);
		}

		Iterator<String> it = this._Headers.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			String v = this._Headers.get(key);

			con.addRequestProperty(key, v);
		}
		return con;
	}

	@Deprecated
	public String doPost_old(String url, java.util.HashMap<String, String> vals) {
		Date t1 = new Date();
		if (this._IsShowLog) {
			System.out.println("P " + url);
		}
		HttpURLConnection url_con = null;
		StringBuilder sb = new StringBuilder();
		Iterator<String> it = vals.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			String val = vals.get(key);
			if (sb.length() > 0) {
				sb.append("&");
			}
			sb.append(key);
			sb.append("=");
			try {
				sb.append(URLEncoder.encode(val, this._Encode == null ? "UTF-8" : _Encode));
			} catch (UnsupportedEncodingException e) {
				this._LastErr = e.getLocalizedMessage();
				return null;
			}
		}

		try {
			url_con = (HttpURLConnection) this.createConn(url);
			url_con.setRequestMethod("POST");
			url_con.setConnectTimeout(C_TIME_OUT);// （单位：毫秒）jdk
			url_con.setReadTimeout(R_TIME_OUT);// （单位：毫秒）jdk 1.5换成这个,读操作超时
			url_con.setDoOutput(true);
			byte[] b = sb.toString().getBytes();
			url_con.getOutputStream().write(b, 0, b.length);
			url_con.getOutputStream().flush();
			url_con.getOutputStream().close();
			String s = this.readContent((URLConnection) url_con);

			if (this._IsShowLog) {
				Date t2 = new Date();
				long tt = com.gdxsoft.easyweb.utils.Utils.timeDiffSeconds(t2, t1);
				System.out.println(" R=" + tt + "s, L=" + s);
			}
			return s;
		} catch (IOException e) {
			this._LastErr = e.getLocalizedMessage();
			System.out.println(e.getMessage());
			return null;
		}
	}

	@Deprecated
	byte[] readData(URLConnection u) throws IOException {
		_ReturnUrl = u.getURL();

		for (String key : u.getHeaderFields().keySet()) {
			if (key != null && key.equalsIgnoreCase("Set-Cookie")) {
				List<String> lst = u.getHeaderFields().get(key);
				readCookies(lst);
			}
		}

		BufferedInputStream bis = new BufferedInputStream(u.getInputStream());
		ByteBuffer bb = ByteBuffer.allocate(12 * 1024 * 1024);
		byte[] buf = new byte[4096];
		int m = 0;
		int read;
		while ((read = bis.read(buf)) > 0) {
			m += read;
			bb.put(buf, 0, read);
		}
		byte[] buf1 = new byte[m];
		System.arraycopy(bb.array(), 0, buf1, 0, buf1.length);
		bb.clear();

		return buf1;
	}

	@Deprecated
	public byte[] downloadData_old(String url) {
		Date t1 = new Date();

		if (this._IsShowLog) {
			System.out.print("G " + url);
		}
		URLConnection con = null;

		try {
			con = this.createConn(url);
			this._LastUrl = url;

			byte[] s1 = this.readData(con);
			if (this._IsShowLog) {
				Date t2 = new Date();
				long tt = com.gdxsoft.easyweb.utils.Utils.timeDiffSeconds(t2, t1);
				System.out.println(" R=" + tt + "s, L=" + s1.length);
			}
			return s1;
		} catch (IOException e) {
			this._LastErr = e.getLocalizedMessage();
			return null;
		} finally {
			if (con != null) {
				con = null;
			}
		}
	}

	@Deprecated
	public String doGet_old(String url) {
		Date t1 = new Date();

		if (this._IsShowLog) {
			System.out.print("G " + url);
		}
		URLConnection con = null;

		try {
			con = this.createConn(url);
			this._LastUrl = url;

			String s1 = this.readContent(con);
			if (this._IsShowLog) {
				Date t2 = new Date();
				long tt = com.gdxsoft.easyweb.utils.Utils.timeDiffSeconds(t2, t1);
				System.out.println(" R=" + tt + "s, L=" + s1.length());
			}
			return s1;
		} catch (IOException e) {
			this._LastErr = e.getLocalizedMessage();
			return null;
		} finally {
			if (con != null) {
				con = null;
			}
		}
	}

	/**
	 * 上传文件
	 *
	 * @param url       地址
	 * @param fieldName 上传域名
	 * @param filePath  文件地址
	 * @return 执行结果
	 */
	@Deprecated
	public String doUpload_old(String url, String fieldName, String filePath) {
		File f = new File(filePath);

		HttpURLConnection url_con = null;
		String BOUNDARY = "---------------------------7d4a6d158c9"; // 分隔符
		StringBuffer sb = new StringBuffer();
		sb.append("--");
		sb.append(BOUNDARY);
		sb.append("\r\n");
		sb.append("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + f.getName() + "\"\r\n");

		// 上传文件类型
		String ct = "application/octet-stream";
		if (f.getName().toUpperCase().endsWith(".GIF")) {
			ct = "image/gif";
		} else if (f.getName().toUpperCase().endsWith(".JPG")) {
			ct = "image/jpeg";
		} else if (f.getName().toUpperCase().endsWith(".PNG")) {
			ct = "image/png";
		} else if (f.getName().toUpperCase().endsWith(".BMP")) {
			ct = "image/bmp";
		}

		sb.append("Content-Type: " + ct + "\r\n\r\n");

		try {
			url_con = (HttpURLConnection) this.createConn(url);
			url_con.setRequestMethod("POST");
			url_con.setConnectTimeout(C_TIME_OUT);// （单位：毫秒）jdk
			url_con.setReadTimeout(R_TIME_OUT);// （单位：毫秒）jdk 1.5换成这个,读操作超时
			url_con.setDoOutput(true);

			byte[] data = sb.toString().getBytes();
			byte[] end_data = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();

			// 上传内容长度
			long contentLength = data.length + f.length() + end_data.length;

			url_con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY); // 设置表单类型和分隔符
			url_con.setRequestProperty("Content-Length", String.valueOf(contentLength)); // 设置内容长度

			OutputStream os = url_con.getOutputStream();
			os.write(data);

			// 要上传的文件
			FileInputStream fis = new FileInputStream(f);
			int rn2;
			byte[] buf2 = new byte[1024];
			while ((rn2 = fis.read(buf2, 0, 1024)) > 0) {
				os.write(buf2, 0, rn2);
			}

			os.write(end_data);
			os.flush();
			os.close();
			fis.close();

			return this.readContent((URLConnection) url_con);
		} catch (IOException e) {
			this._LastErr = e.getLocalizedMessage();
			return null;
		}
	}

	// ===================== Deprecated accessors for removed Apache types =====================

	/**
	 * @return null (连接池已由 java.net.http.HttpClient 内部管理)
	 * @deprecated java.net.http.HttpClient 内部管理连接池
	 */
	@Deprecated
	public Object getConnMgr() {
		return null;
	}

	/**
	 * @return null (请求配置已由 java.net.http.HttpClient.Builder 管理)
	 * @deprecated 超时通过 HttpClient.Builder 管理
	 */
	@Deprecated
	public Object getRequestConfig() {
		return null;
	}

	/**
	 * 最后一次的 Response headers
	 *
	 * @return 响应头映射
	 */
	public Map<String, List<String>> getLastResponse() {
		return _LastResponse;
	}

	// ===================== Helper methods =====================

	/**
	 * 获取字符集名称
	 *
	 * @return 字符集名称
	 */
	private String getCharset() {
		return this._Encode == null ? "UTF-8" : this._Encode;
	}

	/**
	 * 获取字符集对象
	 *
	 * @return Charset
	 */
	private Charset getCharsetObj() {
		return Charset.forName(getCharset());
	}

	/**
	 * 记录字符串结果耗时日志
	 */
	private void logTiming(Date t1, String result) {
		if (this._IsShowLog) {
			Date t2 = new Date();
			long tt = com.gdxsoft.easyweb.utils.Utils.timeDiffSeconds(t2, t1);
			LOGGER.info(tt + "s, L=" + (result == null ? -1 : result.length()));
		}
	}

	/**
	 * 记录二进制结果耗时日志
	 */
	private void logTimingBytes(Date t1, byte[] result) {
		if (this._IsShowLog) {
			Date t2 = new Date();
			long tt = com.gdxsoft.easyweb.utils.Utils.timeDiffSeconds(t2, t1);
			LOGGER.info(" R=" + tt + "s, L=" + (result == null ? -1 : result.length));
		}
	}

	// ===================== Getters and Setters =====================

	/**
	 * 追后一次执行错误
	 *
	 * @return the _LastErr
	 */
	public String getLastErr() {
		return _LastErr;
	}

	/**
	 * 请求和返回编码
	 *
	 * @return the _Encode
	 */
	public String getEncode() {
		return _Encode;
	}

	/**
	 * 请求和返回编码
	 *
	 * @param encode the _Encode to set
	 */
	public void setEncode(String encode) {
		_Encode = encode;
	}

	private String _Cookie;

	/**
	 * @return the _Cookie
	 */
	public String getCookie() {
		return _Cookie;
	}

	/**
	 * @param cookie the _Cookie to set
	 */
	public void setCookie(String cookie) {
		_Cookie = cookie;
		this.initCookies();
	}

	/**
	 * 追后一次执行的 Url
	 *
	 * @return the _LastUrl
	 */
	public String getLastUrl() {
		return _LastUrl;
	}

	/**
	 * 追后一次执行的 Url
	 *
	 * @param lastUrl the 追后一次执行的 Url to set
	 */
	public void setLastUrl(String lastUrl) {
		_LastUrl = lastUrl;
	}

	/**
	 * 获取返回的 Url
	 *
	 * @return 返回的url
	 */
	@Deprecated
	public URL getReturnUrl() {
		return _ReturnUrl;
	}

	/**
	 * 是否输出日志
	 *
	 * @return the _IsShowLog
	 */
	public boolean isShowLog() {
		return _IsShowLog;
	}

	/**
	 * 输出日志
	 *
	 * @param isShowLog the _IsShowLog to set
	 */
	public void setIsShowLog(boolean isShowLog) {
		_IsShowLog = isShowLog;
	}

	/**
	 * 最后一次下载的文本
	 *
	 * @return the _LastResult
	 */
	public String getLastResult() {
		return _LastResult;
	}

	/**
	 * 最后一次下载的二进制
	 *
	 * @return the _LastBuf
	 */
	public byte[] getLastBuf() {
		return _LastBuf;
	}

	/**
	 * 限制最大Redirect次数
	 *
	 * @return 最大Redirect次数
	 */
	public int getLimitRedirectInc() {
		return _LimitRedirectInc;
	}

	/**
	 * 限制最大Redirect次数
	 *
	 * @param limitRedirectInc 最大Redirect次数
	 */
	public void setLimitRedirectInc(int limitRedirectInc) {
		_LimitRedirectInc = limitRedirectInc;
	}

	/**
	 * Get the last response headers
	 *
	 * @return the _ResponseHeaders
	 */
	public Map<String, String> getResponseHeaders() {
		return _ResponseHeaders;
	}

	/**
	 * Ignore invalid cookie warning
	 *
	 * @return
	 */
	public boolean isIgnoreInvalidCookieWarn() {
		return ignoreInvalidCookieWarn;
	}

	/**
	 * Ignore invalid cookie warning
	 *
	 * @param ignoreInvalidCookieWarn
	 */
	public void setIgnoreInvalidCookieWarn(boolean ignoreInvalidCookieWarn) {
		this.ignoreInvalidCookieWarn = ignoreInvalidCookieWarn;
	}
}
