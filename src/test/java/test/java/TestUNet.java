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
}
