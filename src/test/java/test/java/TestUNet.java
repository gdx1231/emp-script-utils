package test.java;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.utils.UNet;

import static org.junit.jupiter.api.Assertions.*;

public class TestUNet extends TestBase {

	private UNet net;

	@BeforeEach
	public void setUp() {
		net = new UNet();
	}

	/**
	 * 通过反射设置 UNet 的私有字段，避免发起真实网络请求
	 */
	private void setField(String name, Object value) throws Exception {
		Field f = UNet.class.getDeclaredField(name);
		f.setAccessible(true);
		f.set(net, value);
	}

	@Test
	public void testCreateLastCurl_NullWhenNoRequest() {
		assertNull(net.createLastCurl());
	}

	@Test
	public void testSslAlpnOnlyAdvertisesHttp1() throws Exception {
		java.lang.reflect.Method createFactory = UNet.class
				.getDeclaredMethod("createSSLConnSocketFactory");
		createFactory.setAccessible(true);
		Object factory = createFactory.invoke(null);

		javax.net.ssl.SSLSocket socket = (javax.net.ssl.SSLSocket) javax.net.ssl.SSLSocketFactory
				.getDefault().createSocket();
		java.lang.reflect.Method prepareSocket = factory.getClass().getSuperclass()
				.getDeclaredMethod("prepareSocket", javax.net.ssl.SSLSocket.class);
		prepareSocket.setAccessible(true);
		prepareSocket.invoke(factory, socket);

		assertArrayEquals(new String[]{"http/1.1"}, socket.getSSLParameters().getApplicationProtocols());
		socket.close();
	}

	@Test
	public void testCreateLastCurl_GetRequest() throws Exception {
		setField("_LastUrl", "https://example.com/api");
		setField("_LastMethod", "GET");
		setField("_LastBody", null);

		String curl = net.createLastCurl();
		assertNotNull(curl);
		assertTrue(curl.startsWith("curl -X GET"));
		assertTrue(curl.contains("'https://example.com/api'"));
		assertFalse(curl.contains("-d "));
		System.out.println(curl);
	}

	@Test
	public void testCreateLastCurl_PostWithStringBody() throws Exception {
		setField("_LastUrl", "https://api.example.com/data");
		setField("_LastMethod", "POST");
		setField("_LastBody", "{\"key\":\"value\"}");

		String curl = net.createLastCurl();
		assertNotNull(curl);
		assertTrue(curl.contains("-X POST"));
		assertTrue(curl.contains("-d '{\"key\":\"value\"}'"));
		System.out.println(curl);
	}

	@Test
	public void testCreateLastCurl_WithHeaders() throws Exception {
		setField("_LastUrl", "https://example.com");
		setField("_LastMethod", "GET");
		setField("_LastBody", null);

		HashMap<String, String> headers = new LinkedHashMap<>();
		headers.put("Content-Type", "application/json");
		headers.put("Authorization", "Bearer token123");
		setField("_Headers", headers);

		String curl = net.createLastCurl();
		assertNotNull(curl);
		assertTrue(curl.contains("-H 'Content-Type: application/json'"));
		assertTrue(curl.contains("-H 'Authorization: Bearer token123'"));
		System.out.println(curl);
	}

	@Test
	public void testCreateLastCurl_WithCookies() throws Exception {
		setField("_LastUrl", "https://example.com");
		setField("_LastMethod", "GET");
		setField("_LastBody", null);

		HashMap<String, String> cookies = new LinkedHashMap<>();
		cookies.put("session", "abc123");
		cookies.put("lang", "zh");
		setField("_Cookies", cookies);

		String curl = net.createLastCurl();
		assertNotNull(curl);
		assertTrue(curl.contains("-H 'Cookie: session=abc123; lang=zh'"));
		System.out.println(curl);
	}

	@Test
	public void testCreateLastCurl_WithProxy() throws Exception {
		setField("_LastUrl", "https://example.com");
		setField("_LastMethod", "GET");
		setField("_LastBody", null);
		setField("_ProxyHost", "127.0.0.1");
		setField("_ProxyPort", 8080);
		setField("_ProxyScheme", "http");

		String curl = net.createLastCurl();
		assertNotNull(curl);
		assertTrue(curl.contains("--proxy 'http://127.0.0.1:8080'"));
		System.out.println(curl);
	}

	@Test
	public void testCreateLastCurl_WithUserAgent() throws Exception {
		setField("_LastUrl", "https://example.com");
		setField("_LastMethod", "GET");
		setField("_LastBody", null);

		net.setUserAgent("MyApp/1.0");

		String curl = net.createLastCurl();
		assertNotNull(curl);
		assertTrue(curl.contains("-H 'User-Agent: MyApp/1.0'"));
		System.out.println(curl);
	}

	@Test
	public void testCreateLastCurl_ShellEscaping() throws Exception {
		setField("_LastUrl", "https://example.com/path?q=it's");
		setField("_LastMethod", "POST");
		setField("_LastBody", "data=it's a \"test\"");

		String curl = net.createLastCurl();
		assertNotNull(curl);
		// 单引号应被转义为 '\''
		assertTrue(curl.contains("it'\\''s"));
		System.out.println(curl);
	}

	@Test
	public void testCreateLastCurl_FullExample() throws Exception {
		setField("_LastUrl", "https://api.example.com/v1/users");
		setField("_LastMethod", "POST");
		setField("_LastBody", "{\"name\":\"张三\"}");

		net.setUserAgent("TestAgent/2.0");

		HashMap<String, String> headers = new LinkedHashMap<>();
		headers.put("Content-Type", "application/json");
		setField("_Headers", headers);

		HashMap<String, String> cookies = new LinkedHashMap<>();
		cookies.put("token", "xyz789");
		setField("_Cookies", cookies);

		setField("_ProxyHost", "proxy.local");
		setField("_ProxyPort", 3128);
		setField("_ProxyScheme", "http");

		String curl = net.createLastCurl();
		assertNotNull(curl);
		assertTrue(curl.contains("-X POST"));
		assertTrue(curl.contains("'https://api.example.com/v1/users'"));
		assertTrue(curl.contains("-H 'User-Agent: TestAgent/2.0'"));
		assertTrue(curl.contains("-H 'Content-Type: application/json'"));
		assertTrue(curl.contains("-H 'Cookie: token=xyz789'"));
		assertTrue(curl.contains("--proxy 'http://proxy.local:3128'"));
		assertTrue(curl.contains("-d '{\"name\":\"张三\"}'"));
		System.out.println(curl);
	}

	@Test
	public void testCreateLastCurl_LineBreaks() throws Exception {
		setField("_LastUrl", "https://example.com");
		setField("_LastMethod", "POST");
		setField("_LastBody", "data=test");

		HashMap<String, String> headers = new LinkedHashMap<>();
		headers.put("X-Custom", "value");
		setField("_Headers", headers);

		String curl = net.createLastCurl();
		assertNotNull(curl);
		// 验证使用 \\\n 换行
		assertTrue(curl.contains(" \\\n  "));
		String[] lines = curl.split("\n");
		assertTrue(lines.length > 1, "应该有多行输出");
		System.out.println(curl);
	}

	@Test
	public void testEncodeFormData() throws Exception {
		// 通过 doPost(url, Map) 间接测试 encodeFormData
		// 由于 doPost 会发起网络请求，我们只验证 recordLastRequest 记录的 body
		// 这里用反射直接调用 encodeFormData
		java.lang.reflect.Method m = UNet.class.getDeclaredMethod("encodeFormData", Map.class);
		m.setAccessible(true);

		Map<String, String> vals = new LinkedHashMap<>();
		vals.put("key1", "value1");
		vals.put("key2", "中文");
		String result = (String) m.invoke(net, vals);
		assertNotNull(result);
		assertTrue(result.contains("key1=value1"));
		assertTrue(result.contains("key2="));
		System.out.println("encodeFormData: " + result);
	}

	@Test
	public void testEncodeFormData_Null() throws Exception {
		java.lang.reflect.Method m = UNet.class.getDeclaredMethod("encodeFormData", Map.class);
		m.setAccessible(true);
		assertNull(m.invoke(net, (Map<String, String>) null));
		assertNull(m.invoke(net, new HashMap<>()));
	}

	@Test
	public void testCreateLastCurl_UrlEncoding() throws Exception {
		// 中文字符应被 percent-encode
		setField("_LastUrl", "http://localhost:8080/api?location=北京&date=2026-07-09");
		setField("_LastMethod", "GET");
		setField("_LastBody", null);

		String curl = net.createLastCurl();
		assertNotNull(curl);
		// 北京 → %E5%8C%97%E4%BA%AC
		assertTrue(curl.contains("location=%E5%8C%97%E4%BA%AC"), "中文字符应被 URL 编码");
		assertTrue(curl.contains("date=2026-07-09"), "ASCII 字符不应被编码");
		assertFalse(curl.contains("北京"), "原始中文字符不应出现在 curl 中");
		System.out.println(curl);
	}

	@Test
	public void testCreateLastCurl_UrlPreservesExistingEncoding() throws Exception {
		// 已有 %XX 序列不应被双重编码
		setField("_LastUrl", "http://localhost/api?q=%E4%BD%A0%E5%A5%BD");
		setField("_LastMethod", "GET");
		setField("_LastBody", null);

		String curl = net.createLastCurl();
		assertNotNull(curl);
		assertTrue(curl.contains("%E4%BD%A0%E5%A5%BD"), "已有的 percent-encoding 应保留");
		assertFalse(curl.contains("%25E4"), "不应出现双重编码");
		System.out.println(curl);
	}

	@Test
	public void testCreateLastCurl_UrlMixedEncoding() throws Exception {
		// 混合场景：部分已编码 + 部分中文
		setField("_LastUrl", "http://host/path?name=%E5%BC%A0&city=上海");
		setField("_LastMethod", "GET");
		setField("_LastBody", null);

		String curl = net.createLastCurl();
		assertNotNull(curl);
		assertTrue(curl.contains("name=%E5%BC%A0"), "已编码部分保留");
		assertTrue(curl.contains("city=%E4%B8%8A%E6%B5%B7"), "中文应被编码 (上海→%E4%B8%8A%E6%B5%B7)");
		System.out.println(curl);
	}
}
