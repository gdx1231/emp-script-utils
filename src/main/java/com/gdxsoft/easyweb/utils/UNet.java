package com.gdxsoft.easyweb.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
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
	private final static RequestConfig IGNORE_COOKIES = RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();
	private final static RequestConfig STANDARD = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
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

	private PoolingHttpClientConnectionManager connMgr;
	private RequestConfig requestConfig;
	private String userAgent;
	private int _LastStatusCode;
	private BasicCookieStore _CookieStore;

	private HttpResponse _LastResponse;

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

	public UNet() {
		this._Headers = new HashMap<String, String>();
		_Cookies = new HashMap<String, String>();

		this._CookieStore = new BasicCookieStore();
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
	 * 获取 CookieStore
	 * 
	 * @return BasicCookieStore
	 */
	public BasicCookieStore getCookieStore() {
		return _CookieStore;
	}

	/**
	 * cookie转换为 JSONArray
	 * 
	 * @return cookie转换为 JSONArray
	 */
	public JSONArray listCookieStoreCookes() {
		JSONArray arr = new JSONArray();
		List<Cookie> cks = this.getCookieStore().getCookies();
		for (int i = 0; i < cks.size(); i++) {
			Cookie ck = cks.get(i);
			String s = ck.toString();
			arr.put(s);
		}
		return arr;
	}

	/**
	 * 设置 CookieStore
	 * 
	 * @param cookieStore cookieStore
	 */
	public void setCookieStore(BasicCookieStore cookieStore) {
		this._CookieStore = cookieStore;
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
	 * 生成提交的body
	 * 
	 * @param body 提交的信息
	 * @return StringEntity
	 */
	public StringEntity createStringEntity(String body) {
		String code = this._Encode == null ? "UTF-8" : this._Encode;

		body = body.replace("\\u201c", "“");
		body = body.replace("\\u201d", "”");

		StringEntity postEntity = new StringEntity(body, code);

		return postEntity;
	}

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
		CloseableHttpClient httpclient = this.getHttpClient(u);
		HttpPatch httppost = new HttpPatch(u);
		if (ignoreInvalidCookieWarn) {
			httppost.setConfig(IGNORE_COOKIES);
		}
		for (String key : this._Headers.keySet()) {
			String v = this._Headers.get(key);
			httppost.addHeader(key, v);
		}

		StringEntity postEntity = this.createStringEntity(body);
		httppost.setEntity(postEntity);

		return this.handleResponse(httpclient, httppost);
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

		CloseableHttpClient httpclient = this.getHttpClient(url);

		HttpPatch httPatch = new HttpPatch(url);
		if (ignoreInvalidCookieWarn) {
			httPatch.setConfig(IGNORE_COOKIES);
		}
		this.addRequestHeaders(httPatch);

		try {
			this.addPostData(httPatch, vals);
		} catch (UnsupportedEncodingException e) {
			this._LastErr = e.getLocalizedMessage();
			LOGGER.error(e.getMessage());
			return null;
		}
		this.handleResponse(httpclient, httPatch);
		return this.checkAndHandleRedirectString();
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
		CloseableHttpClient httpclient = this.getHttpClient(u);
		HttpPost httpost = new HttpPost(u);
		if (ignoreInvalidCookieWarn) {
			httpost.setConfig(IGNORE_COOKIES);
		}
		this.addRequestHeaders(httpost);

		StringEntity postEntity = this.createStringEntity(body);
		httpost.setEntity(postEntity);

		this._LastResult = null;
		this._LastBuf = null;
		try {
			CloseableHttpResponse response = httpclient.execute(httpost);
			this._LastBuf = this.readResponseBytes(response);
			return this._LastBuf;
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			this._LastErr = e.getLocalizedMessage();
			return null;
		} finally {
			this.closeHttpClient(httpclient);
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

		byte[] result = null;
		CloseableHttpClient httpclient = this.getHttpClient(url);
		HttpGet request = new HttpGet(url);
		if (ignoreInvalidCookieWarn) {
			request.setConfig(IGNORE_COOKIES);
		}
		
		this.addRequestHeaders(request);

		// 设置请求和传输超时时间
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(20000).setConnectTimeout(50000).build();
		request.setConfig(requestConfig);

		result = this.handleResponseBinary(httpclient, request);

		return result;
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
		String result = null;

		CloseableHttpClient httpclient = this.getHttpClient(url);

		HttpPut httpPut = new HttpPut(url);
		if (ignoreInvalidCookieWarn) {
			httpPut.setConfig(IGNORE_COOKIES);
		}
		this.addRequestHeaders(httpPut);
		StringEntity postEntity = this.createStringEntity(body);
		httpPut.setEntity(postEntity);

		result = this.handleResponse(httpclient, httpPut);

		return result;
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
		CloseableHttpClient httpclient = this.getHttpClient(url);

		HttpPut httpPut = new HttpPut(url);
		if (ignoreInvalidCookieWarn) {
			httpPut.setConfig(IGNORE_COOKIES);
		}
		this.addRequestHeaders(httpPut);

		try {
			this.addPostData(httpPut, vals);
		} catch (UnsupportedEncodingException e) {
			this._LastErr = e.getLocalizedMessage();
			LOGGER.error(e.getMessage());
			return null;
		}
		this.handleResponse(httpclient, httpPut);
		return this.checkAndHandleRedirectString();

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
		String result = null;

		CloseableHttpClient httpclient = this.getHttpClient(url);

		HttpDelete httpDelete = new HttpDelete(url);
		if (ignoreInvalidCookieWarn) {
			httpDelete.setConfig(IGNORE_COOKIES);
		}
		
		this.addRequestHeaders(httpDelete);

		result = this.handleResponse(httpclient, httpDelete);

		return result;
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
		String result = null;

		CloseableHttpClient httpclient = this.getHttpClient(url);

		HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(url);
		
		if (ignoreInvalidCookieWarn) {
			httpDelete.setConfig(IGNORE_COOKIES);
		}
		this.addRequestHeaders(httpDelete);
		StringEntity postEntity = this.createStringEntity(body);
		httpDelete.setEntity(postEntity);

		result = this.handleResponse(httpclient, httpDelete);

		return result;
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

		CloseableHttpClient httpclient = this.getHttpClient(url);

		HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(url);
		if (ignoreInvalidCookieWarn) {
			httpDelete.setConfig(IGNORE_COOKIES);
		}
		
		this.addRequestHeaders(httpDelete);

		try {
			this.addPostData(httpDelete, vals);
		} catch (UnsupportedEncodingException e) {
			this._LastErr = e.getLocalizedMessage();
			LOGGER.error(e.getMessage());
			return null;
		}
		this.handleResponse(httpclient, httpDelete);
		return this.checkAndHandleRedirectString();
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

		CloseableHttpClient httpclient = this.getHttpClient(url);

		HttpGet request = new HttpGet(url);
		if (ignoreInvalidCookieWarn) {
			request.setConfig(IGNORE_COOKIES);
		}
		this.addRequestHeaders(request);

		this.handleResponse(httpclient, request);

		return checkAndHandleRedirectString();
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

		CloseableHttpClient httpclient = this.getHttpClient(url);

		HttpPost httppost = new HttpPost(url);
		if (ignoreInvalidCookieWarn) {
			httppost.setConfig(IGNORE_COOKIES);
		}
		
		this.addRequestHeaders(httppost);

		try {
			this.addPostData(httppost, vals);
		} catch (UnsupportedEncodingException e) {
			this._LastErr = e.getLocalizedMessage();
			LOGGER.error(e.getMessage());
			return null;
		}
		this.handleResponse(httpclient, httppost);
		return this.checkAndHandleRedirectString();
	}

	/**
	 * 提交body 消息
	 * 
	 * @param u    提交地址
	 * @param body 提交内容
	 * @return 执行结果
	 */
	public String doPost(String url, String body) {
		if (this._IsShowLog) {
			LOGGER.info("POST: " + url);
		}
		CloseableHttpClient httpclient = this.getHttpClient(url);

		HttpPost httpost = new HttpPost(url);
		if (ignoreInvalidCookieWarn) {
			httpost.setConfig(IGNORE_COOKIES);
		}
		this.addRequestHeaders(httpost);

		StringEntity postEntity = this.createStringEntity(body);
		httpost.setEntity(postEntity);

		return this.handleResponse(httpclient, httpost);
	}

	/**
	 * POST/PUT/PATCH/DELETE等添加form参数
	 * 
	 * @param http
	 * @param vals
	 * @throws UnsupportedEncodingException
	 */
	private void addPostData(HttpEntityEnclosingRequestBase http, Map<String, String> vals)
			throws UnsupportedEncodingException {
		List<BasicNameValuePair> formparams = new ArrayList<BasicNameValuePair>();
		for (String key : vals.keySet()) {
			String value = vals.get(key);
			formparams.add(new BasicNameValuePair(key, value));
		}
		String code = this._Encode == null ? "UTF-8" : this._Encode;
		UrlEncodedFormEntity uefEntity;

		uefEntity = new UrlEncodedFormEntity(formparams, code);
		http.setEntity(uefEntity);
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

	/**
	 * 检查是否有重定向，有的化执行get（最多7次），没有返回追后执行的内容
	 * 
	 * @return 重定向地址
	 */
	private String checkAndHandleRedirectString() {
		if (this._LastStatusCode == 302 || this._LastStatusCode == 301) {
			this.redirectInc++;
			if (this.redirectInc > 7) {
				LOGGER.error("太多的重定向");
				return null;
			}
			String newUrl = this.get302Or301Location(_LastResponse);
			// 执行重定向
			return this.doGet(newUrl);
		} else {
			return this._LastResult;
		}
	}

	/**
	 * 读取返回的Cookie
	 * 
	 * @param allHeaders
	 */
	public void readResponseCookies(Header[] allHeaders) {
		if (allHeaders == null) {
			return;
		}
		List<String> lst = new ArrayList<String>();
		for (int i = 0; i < allHeaders.length; i++) {
			Header h = allHeaders[i];
			String h_name = h.getName();
			String h_value = h.getValue();

			if (h_name != null && h_name.equalsIgnoreCase("Set-Cookie")) {
				lst.add(h_value);
				if (this.isShowLog()) {
					LOGGER.info(h_name + ": " + h_value);
				}
			}
		}

		this.readCookies(lst);
	}

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
		MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
		String code = this._Encode == null ? "UTF-8" : this._Encode;
		multipartEntityBuilder.setCharset(Charset.forName(code));

		if (fieldName != null && filePath != null) {
			multipartEntityBuilder.addPart(fieldName, new FileBody(new File(filePath)));
		}
		if (vals != null) {
			for (String key : vals.keySet()) {
				StringBody body = new StringBody(vals.get(key), ContentType.MULTIPART_FORM_DATA);
				multipartEntityBuilder.addPart(key, body);
			}
		}
		return this.doUpload(url, multipartEntityBuilder);
	}

	/**
	 * 上传文件和参数
	 * 
	 * @param url        地址
	 * @param fieldName  文件字段名称
	 * @param filePath   文件地址
	 * @param formparams 参数
	 * @return 执行结果
	 */
	public String doUpload(String url, String fieldName, String filePath, List<BasicNameValuePair> formparams) {
		MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
		String code = this._Encode == null ? "UTF-8" : this._Encode;
		multipartEntityBuilder.setCharset(Charset.forName(code));

		if (fieldName != null && filePath != null) {
			multipartEntityBuilder.addPart(fieldName, new FileBody(new File(filePath)));
		}
		if (formparams != null) {
			for (int i = 0; i < formparams.size(); i++) {
				BasicNameValuePair param = formparams.get(i);
				StringBody body = new StringBody(param.getValue(), ContentType.MULTIPART_FORM_DATA);
				multipartEntityBuilder.addPart(param.getName(), body);
			}
		}
		return this.doUpload(url, multipartEntityBuilder);
	}

	/**
	 * 上传文件和参数
	 * 
	 * @param url                    地址
	 * @param multipartEntityBuilder 参数
	 * @return 执行结果
	 */
	public String doUpload(String url, MultipartEntityBuilder multipartEntityBuilder) {
		if (this._IsShowLog) {
			LOGGER.info("U " + url);
		}

		CloseableHttpClient httpclient = this.getHttpClient(url);
		// 创建httpget.
		HttpPost httppost = new HttpPost(url);
		if (ignoreInvalidCookieWarn) {
			httppost.setConfig(IGNORE_COOKIES);
		}
		this.addRequestHeaders(httppost);

		// setConnectTimeout：设置连接超时时间，单位毫秒。setConnectionRequestTimeout：设置从connect
		// Manager获取Connection
		// 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。setSocketTimeout：请求获取数据的超时时间，单位毫秒。
		// 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
		RequestConfig defaultRequestConfig = RequestConfig.custom().setConnectTimeout(5000)
				.setConnectionRequestTimeout(5000).setSocketTimeout(15000).build();
		httppost.setConfig(defaultRequestConfig);
		try {

			HttpEntity reqEntity = multipartEntityBuilder.build();
			httppost.setEntity(reqEntity);

			return this.handleResponse(httpclient, httppost);

		} finally {

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
	public String doUpload(String url, String fieldName, String filePath) {
		HashMap<String, String> vals = null;
		return this.doUpload(url, fieldName, filePath, vals);
	}

	/**
	 * 根据url 获取 httpClient(http/https)
	 * 
	 * @param url
	 * @return CloseableHttpClient
	 */
	private CloseableHttpClient getHttpClient(String url) {
		if (this._IsShowLog) {
			List<Cookie> lst = _CookieStore.getCookies();
			for (int i = 0; i < lst.size(); i++) {
				Cookie item = lst.get(i);
				LOGGER.info(item.toString());
			}

		}

		CloseableHttpClient httpclient;
		if (url.toLowerCase().startsWith("https")) { // ssl
			httpclient = HttpClientBuilder.create().setDefaultRequestConfig(STANDARD)
					.setSSLSocketFactory(createSSLConnSocketFactory()).setConnectionManager(connMgr)
					.setDefaultRequestConfig(requestConfig).setDefaultCookieStore(_CookieStore).build();
		} else {
			// 创建默认的httpClient实例.
			httpclient = HttpClientBuilder.create().setDefaultRequestConfig(STANDARD).setDefaultCookieStore(_CookieStore)
					.build();
		}

		this._LastUrl = url;

		return httpclient;
	}

	/**
	 * 添加请求的头部
	 * 
	 * @param request 请求的头部
	 */
	private void addRequestHeaders(Object request) {
		HttpMessage req = (HttpMessage) request;
		req.addHeader("User-Agent", this.getUserAgent());
		if (this._LastUrl != null) {
			req.addHeader("Referer", this._LastUrl);
			if (this._IsShowLog) {
				LOGGER.info("设置Referer : " + this._LastUrl);
			}
		}

		for (String key : this._Headers.keySet()) {
			String val = this._Headers.get(key);
			req.addHeader(key, val);
			if (this._IsShowLog) {
				LOGGER.info("设置" + key + " : " + val);
			}
		}

		if (this._Cookies != null && !this._Cookies.isEmpty()) {
			String cookies = this.getCookies();
			if (cookies.length() > 0) {
				if (this._IsShowLog) {
					LOGGER.info("设置Cookie : " + cookies);
				}
				req.addHeader("Cookie", cookies);
			}
		}
	}

	/**
	 * 保存最后执行的头
	 * 
	 * @param response
	 */
	private void saveLastHeader(HttpResponse response) {
		Header[] heads = response.getAllHeaders();
		_ResponseHeaders = new HashMap<>();
		for (int i = 0; i < heads.length; i++) {
			Header h = heads[i];
			String name = h.getName();
			String value = h.getValue();
			if (this._IsShowLog) {
				LOGGER.info(name + "=" + value);
			}

			_ResponseHeaders.put(name, value);

			if (name.equalsIgnoreCase("Set-Cookie")) {
				String[] cks = value.split("\\;");
				String[] cks1 = cks[0].split("\\=");
				this.addCookie(cks1[0], cks1.length == 1 ? "" : cks1[1]);
			}
		}
	}

	/**
	 * 处理 301, 302 移动的问题<br>
	 * 301 redirect: 301 代表永久性转移(Permanently Moved)<br>
	 * 302 redirect: 302 代表暂时性转移(Temporarily Moved )<br>
	 * 
	 * @param response 请求返回的对象
	 * @return 返回的 location的 url
	 */
	public String get302Or301Location(HttpResponse response) {

		Header header = response.getFirstHeader("location"); // 跳转的目标地址是在 HTTP-HEAD 中的
		String newuri = header.getValue(); // 这就是跳转后的地址，再向这个地址发出新申请，以便得到跳转后的信息是啥。

		return newuri;
	}

	private byte[] readResponseBytes(HttpResponse response) throws IOException {
		int statusCode = response.getStatusLine().getStatusCode();
		this._LastStatusCode = statusCode;
		if (200 != statusCode) {
			LOGGER.error("response code: " + statusCode);
			return null;
		}

		HttpEntity entity = response.getEntity();
		if (entity != null) {
			byte[] buf = EntityUtils.toByteArray(entity);
			EntityUtils.consume(entity);
			this._LastBuf = buf;
			return buf;
		} else {
			LOGGER.warn("返回没有下载内容");
			return null;
		}
	}

	private byte[] handleResponseBinary(CloseableHttpClient httpclient, HttpRequestBase request) {
		Date t1 = new Date();

		this._LastBuf = null;
		this._LastResult = null;
		HttpResponse response;
		try {
			response = httpclient.execute(request);
			this._LastBuf = this.readResponseBytes(response);
			return this._LastBuf;
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			return null;
		} finally {
			this.closeHttpClient(httpclient);
			if (this._IsShowLog) {
				Date t2 = new Date();
				long tt = com.gdxsoft.easyweb.utils.Utils.timeDiffSeconds(t2, t1);
				LOGGER.info(" R=" + tt + "s, L=" + (this._LastBuf == null ? -1 : this._LastBuf.length));
			}
		}
	}

	/**
	 * 关闭连接
	 * 
	 * @param httpclient
	 */
	private void closeHttpClient(CloseableHttpClient httpclient) {
		// 关闭连接,释放资源
		if (httpclient == null) {
			return;
		}
		try {
			httpclient.close();
		} catch (IOException e) {
			this._LastErr = e.getLocalizedMessage();
			LOGGER.error(e.getMessage());
		}

	}

	/**
	 * 读取返回的body内容
	 * 
	 * @param response 强求的返回
	 * @return 内容
	 * @throws ParseException
	 * @throws IOException
	 */
	private String readResponseString(CloseableHttpResponse response) throws ParseException, IOException {
		String result = null;
		String code = this._Encode == null ? "UTF-8" : this._Encode;
		HttpEntity entity = response.getEntity();

		// 200, 404 ...
		this._LastStatusCode = response.getStatusLine().getStatusCode();

		// 4xx(请求错误) 这些状态代码表示请求可能出错，妨碍了服务器的处理。
		// 400 (错误请求) 服务器不理解请求的语法。
		// 401 (未授权) 请求要求身份验证。 对于需要登录的网页，服务器可能返回此响应。
		// 403 (禁止) 服务器拒绝请求。
		// 404 (未找到) 服务器找不到请求的网页。
		// 405 (方法禁用) 禁用请求中指定的方法。
		// 406 (不接受) 无法使用请求的内容特性响应请求的网页。
		// 407 (需要代理授权) 此状态代码与 401(未授权)类似，但指定请求者应当授权使用代理。
		// 408 (请求超时) 服务器等候请求时发生超时。
		// 409 (冲突) 服务器在完成请求时发生冲突。 服务器必须在响应中包含有关冲突的信息。
		// 410 (已删除) 如果请求的资源已永久删除，服务器就会返回此响应。
		// 411 (需要有效长度) 服务器不接受不含有效内容长度标头字段的请求。
		// 412 (未满足前提条件) 服务器未满足请求者在请求中设置的其中一个前提条件。
		// 413 (请求实体过大) 服务器无法处理请求，因为请求实体过大，超出服务器的处理能力。
		// 414 (请求的 URI 过长) 请求的 URI(通常为网址)过长，服务器无法处理。
		// 415 (不支持的媒体类型) 请求的格式不受请求页面的支持。
		// 416 (请求范围不符合要求) 如果页面无法提供请求的范围，则服务器会返回此状态代码。
		// 417 (未满足期望值) 服务器未满足"期望"请求标头字段的要求。

		// 5xx(服务器错误)这些状态代码表示服务器在尝试处理请求时发生内部错误。 这些错误可能是服务器本身的错误，而不是请求出错。
		// 500 (服务器内部错误) 服务器遇到错误，无法完成请求。
		// 501 (尚未实施) 服务器不具备完成请求的功能。 例如，服务器无法识别请求方法时可能会返回此代码。
		// 502 (错误网关) 服务器作为网关或代理，从上游服务器收到无效响应。
		// 503 (服务不可用) 服务器目前无法使用(由于超载或停机维护)。 通常，这只是暂时状态。
		// 504 (网关超时) 服务器作为网关或代理，但是没有及时从上游服务器收到请求。
		// 505 (HTTP 版本不受支持) 服务器不支持请求中所用的 HTTP 协议版本。

		if (this._LastStatusCode >= 400) {
			LOGGER.error(this._LastStatusCode + " " + this._LastUrl);
		}

		if (entity != null) {
			result = EntityUtils.toString(entity, code);
			EntityUtils.consume(entity);
			this._LastResult = result;
			return result;
		} else {
			LOGGER.warn("返回内容为空" + " " + this._LastUrl);
			return null;
		}
	}

	/**
	 * 处理 Response
	 * 
	 * @param httpclient
	 * @param response
	 * @return
	 */
	private String handleResponse(CloseableHttpClient httpclient, CloseableHttpResponse response) {
		this._LastResponse = response;
		// 保留Cookie
		this.saveLastHeader(response);

		this._LastResult = null;
		this._LastBuf = null;
		try {
			return this.readResponseString(response);
		} catch (ParseException | IOException e) {
			LOGGER.error(e.getMessage());
			this._LastErr = e.getMessage();
			return null;
		} finally {
			closeHttpClient(httpclient);
		}
	}

	/**
	 * 
	 * @param httpclient
	 * @param request
	 * @return
	 */
	private String handleResponse(CloseableHttpClient httpclient, HttpRequestBase request) {
		Date t1 = new Date();

		String result = null;
		try {
			CloseableHttpResponse response = httpclient.execute(request);
			result = this.handleResponse(httpclient, response);

			return result;
		} catch (IOException e) {
			this._LastErr = e.getMessage();
			LOGGER.error(e.getMessage());
			return null;
		} finally {
			if (this._IsShowLog) {
				Date t2 = new Date();
				long tt = com.gdxsoft.easyweb.utils.Utils.timeDiffSeconds(t2, t1);
				LOGGER.info(tt + "s, L=" + (result == null ? -1 : result.length()));
			}
		}

	}

	/**
	 * 提交并处理结果
	 * 
	 * @param httpclient
	 * @param request
	 * @return
	 */
	private String handleResponse(CloseableHttpClient httpclient, HttpEntityEnclosingRequestBase request) {
		Date t1 = new Date();

		String result = null;
		try {
			CloseableHttpResponse response = httpclient.execute(request);
			result = this.handleResponse(httpclient, response);
			return result;
		} catch (IOException e) {
			this._LastErr = e.getMessage();
			LOGGER.error(e.getMessage());
			return null;
		} finally {
			if (this._IsShowLog) {
				Date t2 = new Date();
				long tt = com.gdxsoft.easyweb.utils.Utils.timeDiffSeconds(t2, t1);
				LOGGER.info(tt + "s, L=" + (result == null ? -1 : result.length()));
			}
		}
	}

	/**
	 * 读取返回的Cookie
	 * 
	 * @param lst
	 */
	void readCookies(List<String> lst) {
		for (int ia = 0; ia < lst.size(); ia++) {
			String[] scs = lst.get(ia).split(";");
			// for (int i = 0; i < scs.length; i++) {
			// String[] scc = scs[i].split("=");
			// if (scc.length == 2) {
			// if (scc[0].trim().equalsIgnoreCase("path") ||
			// scc[0].trim().equalsIgnoreCase("domain")
			// || scc[0].trim().equalsIgnoreCase("Expires") ||
			// scc[0].trim().equalsIgnoreCase("Max-Age")) {
			// continue;
			// }
			// // System.out.println(scc[0]+"="+ scc[1]);
			// this.addCookie(scc[0], scc[1]);
			// }
			// }

			// cookie 总是 名称/值在第0位，其它为描述信息
			String[] scc = scs[0].split("=");
			this.addCookie(scc[0], scc[1]);

		}
	}

	@Deprecated
	String readContent(URLConnection u) throws IOException {
		_ReturnUrl = u.getURL();
		// Iterator<String> it = u.getHeaderFields().keySet().iterator();
		// while (it.hasNext()) {
		// String k = it.next();
		// List<String> v = u.getHeaderFields().get(k);
		// System.out.println(k + "=" + v);
		// }
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

	/**
	 * 创建SSL安全连接
	 *
	 * @return SSLConnectionSocketFactory
	 */
	private static SSLConnectionSocketFactory createSSLConnSocketFactory() {
		SSLConnectionSocketFactory sslsf = null;
		try {
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {

				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}
			}).build();
			sslsf = new SSLConnectionSocketFactory(sslContext, new HostnameVerifier() {

				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
		return sslsf;
	}

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
	 * @return the connMgr
	 */
	public PoolingHttpClientConnectionManager getConnMgr() {
		return connMgr;
	}

	/**
	 * @return the requestConfig
	 */
	public RequestConfig getRequestConfig() {
		return requestConfig;
	}

	/**
	 * 最后一次的 Response
	 * 
	 * @return the _LastResponse
	 */
	public HttpResponse getLastResponse() {
		return _LastResponse;
	}

	/**
	 * 最后一次下载后的二进制
	 * 
	 * @return the _LastResult
	 */
	public String getLastResult() {
		return _LastResult;
	}

	/**
	 * 最后一次下载后的二进制
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
		this._LimitRedirectInc = limitRedirectInc;
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
			// Iterator<String> it1 =
			// con.getRequestProperties().keySet().iterator();
			// while (it1.hasNext()) {
			// String k = it1.next();
			// List<String> v = con.getRequestProperties().get(k);
			// System.out.println(k + "=" + v);
			// }
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

	/**
	 * 解决 delete不能提交body的问题
	 */
	class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {

		public final static String METHOD_NAME = "DELETE";

		public HttpDeleteWithBody() {
			super();
		}

		public HttpDeleteWithBody(final URI uri) {
			super();
			setURI(uri);
		}

		/**
		 * @throws IllegalArgumentException if the uri is invalid.
		 */
		public HttpDeleteWithBody(final String uri) {
			super();
			setURI(URI.create(uri));
		}

		@Override
		public String getMethod() {
			return METHOD_NAME;
		}

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
	 * Ignore invalid cookie warning (CookieSpecs.IGNORE_COOKIES)
	 * @param ignoreInvalidCookieWarn
	 */
	public void setIgnoreInvalidCookieWarn(boolean ignoreInvalidCookieWarn) {
		this.ignoreInvalidCookieWarn = ignoreInvalidCookieWarn;
	}
}
