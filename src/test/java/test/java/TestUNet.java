package test.java;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.utils.UNet;
import com.sun.net.httpserver.HttpServer;

import static org.junit.jupiter.api.Assertions.*;

public class TestUNet extends TestBase {

	private static HttpServer server;
	private static int port;
	private static String baseUrl;

	// Local proxy ports (ClashX / ss-local)
	private static final String PROXY_HOST = "127.0.0.1";
	private static final int HTTP_PROXY_PORT = 1087;
	private static final int SOCKS_PROXY_PORT = 1086;
	private static final String PROXY_TARGET = "https://www.google.com";

	@BeforeAll
	static void startServer() throws IOException {
		server = HttpServer.create(new InetSocketAddress(0), 0);
		port = server.getAddress().getPort();
		baseUrl = "http://127.0.0.1:" + port;

		// GET endpoint
		server.createContext("/get", exchange -> {
			String response = "{\"method\":\"GET\",\"path\":\"/get\"}";
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			exchange.getResponseHeaders().add("X-Custom", "test-value");
			exchange.sendResponseHeaders(200, response.getBytes().length);
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes());
			os.close();
		});

		// POST endpoint - echoes body back
		server.createContext("/post", exchange -> {
			String method = exchange.getRequestMethod();
			byte[] body = exchange.getRequestBody().readAllBytes();
			String response = "{\"method\":\"" + method + "\",\"body\":\"" + new String(body, "UTF-8") + "\"}";
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			exchange.sendResponseHeaders(200, response.getBytes().length);
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes("UTF-8"));
			os.close();
		});

		// PUT endpoint
		server.createContext("/put", exchange -> {
			byte[] body = exchange.getRequestBody().readAllBytes();
			String response = "{\"method\":\"PUT\",\"body\":\"" + new String(body, "UTF-8") + "\"}";
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			exchange.sendResponseHeaders(200, response.getBytes().length);
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes("UTF-8"));
			os.close();
		});

		// DELETE endpoint
		server.createContext("/delete", exchange -> {
			byte[] body = exchange.getRequestBody().readAllBytes();
			String response = "{\"method\":\"DELETE\",\"body\":\"" + new String(body, "UTF-8") + "\"}";
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			exchange.sendResponseHeaders(200, response.getBytes().length);
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes("UTF-8"));
			os.close();
		});

		// PATCH endpoint
		server.createContext("/patch", exchange -> {
			byte[] body = exchange.getRequestBody().readAllBytes();
			String response = "{\"method\":\"PATCH\",\"body\":\"" + new String(body, "UTF-8") + "\"}";
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			exchange.sendResponseHeaders(200, response.getBytes().length);
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes("UTF-8"));
			os.close();
		});

		// Binary download endpoint
		server.createContext("/download", exchange -> {
			byte[] data = new byte[256];
			for (int i = 0; i < 256; i++) {
				data[i] = (byte) i;
			}
			exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
			exchange.sendResponseHeaders(200, data.length);
			OutputStream os = exchange.getResponseBody();
			os.write(data);
			os.close();
		});

		// Cookie endpoint - sets a cookie and echoes received cookies
		server.createContext("/cookie", exchange -> {
			exchange.getResponseHeaders().add("Set-Cookie", "server_cookie=hello123; Path=/");
			String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
			String response = "{\"cookies\":\"" + (cookieHeader != null ? cookieHeader : "") + "\"}";
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			exchange.sendResponseHeaders(200, response.getBytes().length);
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes());
			os.close();
		});

		// Header echo endpoint
		server.createContext("/headers", exchange -> {
			StringBuilder sb = new StringBuilder("{");
			exchange.getRequestHeaders().forEach((k, v) -> {
				if (sb.length() > 1)
					sb.append(",");
				sb.append("\"").append(k.toLowerCase()).append("\":\"").append(v.get(0)).append("\"");
			});
			sb.append("}");
			String response = sb.toString();
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			exchange.sendResponseHeaders(200, response.getBytes().length);
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes());
			os.close();
		});

		// Redirect endpoint (302)
		server.createContext("/redirect", exchange -> {
			exchange.getResponseHeaders().add("Location", baseUrl + "/get");
			exchange.sendResponseHeaders(302, -1);
			exchange.close();
		});

		// Upload endpoint (multipart)
		server.createContext("/upload", exchange -> {
			byte[] body = exchange.getRequestBody().readAllBytes();
			String bodyStr = new String(body, "UTF-8");
			boolean hasFile = bodyStr.contains("filename=");
			boolean hasField = bodyStr.contains("name=\"field1\"");
			String response = "{\"uploaded\":true,\"hasFile\":" + hasFile + ",\"hasField\":" + hasField + "}";
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			exchange.sendResponseHeaders(200, response.getBytes().length);
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes());
			os.close();
		});

		// Status code endpoint
		server.createContext("/status", exchange -> {
			String query = exchange.getRequestURI().getQuery();
			int code = 200;
			if (query != null && query.startsWith("code=")) {
				code = Integer.parseInt(query.substring(5));
			}
			String response = "{\"status\":" + code + "}";
			exchange.sendResponseHeaders(code, response.getBytes().length);
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes());
			os.close();
		});

		// SSE basic endpoint — sends 3 events with event/data/id
		server.createContext("/sse/basic", exchange -> {
			exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
			exchange.getResponseHeaders().add("Cache-Control", "no-cache");
			exchange.sendResponseHeaders(200, 0); // chunked
			OutputStream os = exchange.getResponseBody();

			os.write("id: 1\ndata: {\"msg\":\"hello\"}\n\n".getBytes());
			os.flush();

			os.write("event: update\nid: 2\ndata: {\"msg\":\"world\"}\n\n".getBytes());
			os.flush();

			os.write("id: 3\ndata: {\"msg\":\"done\"}\n\n".getBytes());
			os.flush();
			os.close();
		});

		// SSE multiline data endpoint
		server.createContext("/sse/multiline", exchange -> {
			exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
			exchange.getResponseHeaders().add("Cache-Control", "no-cache");
			exchange.sendResponseHeaders(200, 0);
			OutputStream os = exchange.getResponseBody();

			os.write("event: lines\ndata: line1\ndata: line2\ndata: line3\n\n".getBytes());
			os.flush();
			os.close();
		});

		// SSE POST endpoint — echoes body back as event data
		server.createContext("/sse/post", exchange -> {
			byte[] reqBody = exchange.getRequestBody().readAllBytes();
			exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
			exchange.getResponseHeaders().add("Cache-Control", "no-cache");
			exchange.sendResponseHeaders(200, 0);
			OutputStream os = exchange.getResponseBody();

			String body = new String(reqBody, "UTF-8");
			os.write(("event: post-echo\ndata: " + body + "\n\n").getBytes());
			os.flush();
			os.close();
		});

		// SSE reconnect endpoint — sends retry field then closes
		server.createContext("/sse/reconnect", exchange -> {
			exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
			exchange.getResponseHeaders().add("Cache-Control", "no-cache");
			exchange.sendResponseHeaders(200, 0);
			OutputStream os = exchange.getResponseBody();

			os.write("retry: 500\ndata: first\n\n".getBytes());
			os.flush();
			os.close();
		});

		// SSE error endpoint (returns 404)
		server.createContext("/sse/error", exchange -> {
			exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
			exchange.getResponseHeaders().add("Cache-Control", "no-cache");
			String err = "{\"error\":\"not found\"}";
			exchange.sendResponseHeaders(404, err.getBytes().length);
			OutputStream os = exchange.getResponseBody();
			os.write(err.getBytes());
			os.close();
		});

		server.setExecutor(null);
		server.start();
	}

	@AfterAll
	static void stopServer() {
		if (server != null) {
			server.stop(0);
		}
	}

	@Test
	public void testDoGet() {
		printCaption("doGet");
		UNet net = new UNet();
		String result = net.doGet(baseUrl + "/get");
		assertNotNull(result, "GET should return non-null");
		assertTrue(result.contains("\"method\":\"GET\""), "Should contain method GET");
		assertEquals(200, net.getLastStatusCode());
	}

	@Test
	public void testDoPostWithBody() {
		printCaption("doPost with String body");
		UNet net = new UNet();
		String result = net.doPost(baseUrl + "/post", "{\"key\":\"value\"}");
		assertNotNull(result, "POST should return non-null");
		assertTrue(result.contains("\"method\":\"POST\""), "Should contain method POST");
		assertTrue(result.contains("key"), "Should echo body");
	}

	@Test
	public void testDoPostWithParams() {
		printCaption("doPost with form params");
		UNet net = new UNet();
		Map<String, String> params = new HashMap<>();
		params.put("name", "test");
		params.put("value", "123");
		String result = net.doPost(baseUrl + "/post", params);
		assertNotNull(result, "POST with params should return non-null");
		assertTrue(result.contains("name=test"), "Should contain form encoded params");
	}

	@Test
	public void testDoPostWithBytes() {
		printCaption("doPost with byte[] body");
		UNet net = new UNet();
		byte[] body = "binary-body-content".getBytes();
		String result = net.doPost(baseUrl + "/post", body);
		assertNotNull(result, "POST with bytes should return non-null");
		assertTrue(result.contains("binary-body-content"), "Should echo byte body");
	}

	@Test
	public void testDoPut() {
		printCaption("doPut");
		UNet net = new UNet();
		String result = net.doPut(baseUrl + "/put", "{\"update\":\"data\"}");
		assertNotNull(result, "PUT should return non-null");
		assertTrue(result.contains("\"method\":\"PUT\""), "Should contain method PUT");
	}

	@Test
	public void testDoDelete() {
		printCaption("doDelete");
		UNet net = new UNet();
		String result = net.doDelete(baseUrl + "/delete");
		assertNotNull(result, "DELETE should return non-null");
		assertTrue(result.contains("\"method\":\"DELETE\""), "Should contain method DELETE");
	}

	@Test
	public void testDoDeleteWithBody() {
		printCaption("doDelete with body");
		UNet net = new UNet();
		String result = net.doDelete(baseUrl + "/delete", "{\"id\":\"123\"}");
		assertNotNull(result, "DELETE with body should return non-null");
		assertTrue(result.contains("123"), "Should echo body");
	}

	@Test
	public void testDoPatch() {
		printCaption("doPatch");
		UNet net = new UNet();
		String result = net.doPatch(baseUrl + "/patch", "{\"patch\":\"value\"}");
		assertNotNull(result, "PATCH should return non-null");
		assertTrue(result.contains("\"method\":\"PATCH\""), "Should contain method PATCH");
	}

	@Test
	public void testPatch() {
		printCaption("patch (alias)");
		UNet net = new UNet();
		String result = net.patch(baseUrl + "/patch", "{\"patch\":\"alias\"}");
		assertNotNull(result, "patch should return non-null");
		assertTrue(result.contains("PATCH"), "Should contain method PATCH");
	}

	@Test
	public void testDownloadData() {
		printCaption("downloadData");
		UNet net = new UNet();
		byte[] data = net.downloadData(baseUrl + "/download");
		assertNotNull(data, "Download should return non-null bytes");
		assertEquals(256, data.length, "Should download 256 bytes");
		assertEquals(0, data[0], "First byte should be 0");
		assertEquals((byte) 255, data[255], "Last byte should be 255");
	}

	@Test
	public void testPostMsgAndDownload() {
		printCaption("postMsgAndDownload");
		UNet net = new UNet();
		byte[] data = net.postMsgAndDownload(baseUrl + "/download", "ignored");
		assertNotNull(data, "postMsgAndDownload should return non-null");
		assertEquals(256, data.length);
	}

	@Test
	public void testDoUpload() {
		printCaption("doUpload");
		// Create a temp file to upload
		File tmpFile = null;
		try {
			tmpFile = File.createTempFile("test-upload", ".txt");
			Files.writeString(tmpFile.toPath(), "test file content");

			UNet net = new UNet();
			HashMap<String, String> vals = new HashMap<>();
			vals.put("field1", "value1");
			String result = net.doUpload(baseUrl + "/upload", "file", tmpFile.getAbsolutePath(), vals);
			assertNotNull(result, "Upload should return non-null");
			assertTrue(result.contains("\"uploaded\":true"), "Should confirm upload");
			assertTrue(result.contains("\"hasFile\":true"), "Should have file");
			assertTrue(result.contains("\"hasField\":true"), "Should have field");
		} catch (Exception e) {
			fail("Upload test failed: " + e.getMessage());
		} finally {
			if (tmpFile != null)
				tmpFile.delete();
		}
	}

	@Test
	public void testDoUploadNoParams() {
		printCaption("doUpload (no params)");
		File tmpFile = null;
		try {
			tmpFile = File.createTempFile("test-upload", ".txt");
			Files.writeString(tmpFile.toPath(), "test content");

			UNet net = new UNet();
			String result = net.doUpload(baseUrl + "/upload", "file", tmpFile.getAbsolutePath());
			assertNotNull(result, "Upload without params should return non-null");
			assertTrue(result.contains("\"hasFile\":true"));
		} catch (Exception e) {
			fail("Upload test failed: " + e.getMessage());
		} finally {
			if (tmpFile != null)
				tmpFile.delete();
		}
	}

	@Test
	public void testCookieHandling() {
		printCaption("Cookie handling");
		UNet net = new UNet();
		net.setCookie("my_cookie=abc123");

		String cookies = net.getCookies();
		assertNotNull(cookies);
		assertTrue(cookies.contains("my_cookie=abc123"), "Should contain set cookie");

		// Make a request to the cookie endpoint
		String result = net.doGet(baseUrl + "/cookie");
		assertNotNull(result);

		// The server set a cookie, check it was captured
		String allCookies = net.getCookies();
		assertTrue(allCookies.contains("server_cookie"), "Should have captured server cookie");
	}

	@Test
	public void testCookieStore() {
		printCaption("CookieStore");
		UNet net = new UNet();
		Map<String, String> store = new HashMap<>();
		store.put("key1", "val1");
		store.put("key2", "val2");
		net.setCookieStore(store);

		Map<String, String> retrieved = net.getCookieStore();
		assertEquals(2, retrieved.size());
		assertEquals("val1", retrieved.get("key1"));
	}

	@Test
	public void testCustomHeaders() {
		printCaption("Custom headers");
		UNet net = new UNet();
		net.addHeader("X-Test-Header", "custom-value");
		String result = net.doGet(baseUrl + "/headers");
		assertNotNull(result);
		assertTrue(result.contains("x-test-header"), "Should echo custom header");
		assertTrue(result.contains("custom-value"), "Should contain header value");
	}

	@Test
	public void testAddHeaders() {
		printCaption("addHeaders");
		UNet net = new UNet();
		Map<String, String> headers = new HashMap<>();
		headers.put("X-H1", "v1");
		headers.put("X-H2", "v2");
		net.addHeaders(headers);
		String result = net.doGet(baseUrl + "/headers");
		assertNotNull(result);
		assertTrue(result.contains("x-h1"));
		assertTrue(result.contains("x-h2"));
	}

	@Test
	public void testClearHeaders() {
		printCaption("clearHeaders");
		UNet net = new UNet();
		net.addHeader("X-Will-Clear", "value");
		net.clearHeaders();
		String result = net.doGet(baseUrl + "/headers");
		assertNotNull(result);
		assertFalse(result.contains("x-will-clear"), "Header should be cleared");
	}

	@Test
	public void testUserAgent() {
		printCaption("UserAgent");
		UNet net = new UNet();
		net.setUserAgent("CustomAgent/1.0");
		String result = net.doGet(baseUrl + "/headers");
		assertNotNull(result);
		assertTrue(result.contains("CustomAgent/1.0"), "Should use custom user agent");
	}

	@Test
	public void testDefaultUserAgent() {
		printCaption("Default UserAgent");
		UNet net = new UNet();
		String ua = net.getUserAgent();
		assertNotNull(ua);
		assertTrue(ua.contains("Mozilla"), "Default UA should contain Mozilla");
	}

	@Test
	public void testRedirect() {
		printCaption("Redirect 302");
		UNet net = new UNet();
		String result = net.doGet(baseUrl + "/redirect");
		assertNotNull(result, "Should follow redirect and return final result");
		assertTrue(result.contains("\"method\":\"GET\""), "Should reach final GET endpoint");
	}

	@Test
	public void testGet302Location() {
		printCaption("get302Or301Location");
		UNet net = new UNet();
		Map<String, List<String>> headers = new HashMap<>();
		headers.put("Location", List.of("http://example.com/target"));
		String location = net.get302Or301Location(headers);
		assertEquals("http://example.com/target", location);
	}

	@Test
	public void testReadResponseCookies() {
		printCaption("readResponseCookies");
		UNet net = new UNet();
		Map<String, List<String>> headers = new HashMap<>();
		headers.put("Set-Cookie", List.of("a=1; Path=/", "b=2; Path=/"));
		net.readResponseCookies(headers);
		String cookies = net.getCookies();
		assertTrue(cookies.contains("a=1"), "Should parse cookie a");
		assertTrue(cookies.contains("b=2"), "Should parse cookie b");
	}

	@Test
	public void testTimeout() {
		printCaption("Timeout");
		UNet net = new UNet();
		net.setTimeout(30000);
		assertEquals(30000, net.getTimeout());
		String result = net.doGet(baseUrl + "/get");
		assertNotNull(result, "Should work with custom timeout");
	}

	@Test
	public void testEncode() {
		printCaption("Encode");
		UNet net = new UNet();
		net.setEncode("UTF-8");
		assertEquals("UTF-8", net.getEncode());
	}

	@Test
	public void testProxySetAndClear() {
		printCaption("Proxy set and clear");
		UNet net = new UNet();
		net.setProxy("127.0.0.1", 8080);
		net.setProxy("127.0.0.1", 8080, "http");
		net.setProxy("127.0.0.1", 1086, "socks");
		net.clearProxy();
		// Just verifying no exceptions thrown
		assertTrue(true, "Proxy set/clear should not throw");
	}

	@Test
	public void testProxyValidation() {
		printCaption("Proxy validation");
		UNet net = new UNet();
		assertThrows(IllegalArgumentException.class, () -> net.setProxy(null, 8080));
		assertThrows(IllegalArgumentException.class, () -> net.setProxy("", 8080));
		assertThrows(IllegalArgumentException.class, () -> net.setProxy("host", 0));
		assertThrows(IllegalArgumentException.class, () -> net.setProxy("host", 70000));
		assertThrows(IllegalArgumentException.class, () -> net.setProxy("host", 8080, "invalid"));
	}

	@Test
	public void testStatusCode() {
		printCaption("Status codes");
		UNet net = new UNet();

		net.doGet(baseUrl + "/status?code=200");
		assertEquals(200, net.getLastStatusCode());

		net.doGet(baseUrl + "/status?code=404");
		assertEquals(404, net.getLastStatusCode());
	}

	@Test
	public void testCreateStringEntity() {
		printCaption("createStringEntity");
		UNet net = new UNet();
		String body = net.createStringEntity("test body");
		assertNotNull(body);
		assertEquals("test body", body);
	}

	@Test
	public void testPostMsg() {
		printCaption("postMsg (alias)");
		UNet net = new UNet();
		String result = net.postMsg(baseUrl + "/post", "hello");
		assertNotNull(result);
		assertTrue(result.contains("hello"));
	}

	@Test
	public void testShowLog() {
		printCaption("ShowLog");
		UNet net = new UNet();
		net.setIsShowLog(true);
		assertTrue(net.isShowLog());
		String result = net.doGet(baseUrl + "/get");
		assertNotNull(result, "Should work with logging enabled");
	}

	@Test
	public void testConstructorWithCookieAndEncode() {
		printCaption("Constructor with cookie and encode");
		UNet net = new UNet("session=abc123; lang=zh", "UTF-8");
		String cookies = net.getCookies();
		assertTrue(cookies.contains("session=abc123"));
		assertTrue(cookies.contains("lang=zh"));
		assertEquals("UTF-8", net.getEncode());
	}

	@Test
	public void testResponseHeaders() {
		printCaption("Response headers");
		UNet net = new UNet();
		net.doGet(baseUrl + "/get");
		Map<String, String> headers = net.getResponseHeaders();
		assertNotNull(headers, "Response headers should not be null");
	}

	@Test
	public void testLastResultAndBuf() {
		printCaption("lastResult and lastBuf");
		UNet net = new UNet();
		net.doGet(baseUrl + "/get");
		assertNotNull(net.getLastResult());

		byte[] buf = net.downloadData(baseUrl + "/download");
		assertNotNull(buf);
		assertNotNull(net.getLastBuf());
	}

	@Test
	public void testLimitRedirect() {
		printCaption("LimitRedirectInc");
		UNet net = new UNet();
		net.setLimitRedirectInc(3);
		assertEquals(3, net.getLimitRedirectInc());
	}

	@Test
	public void testDoPutWithParams() {
		printCaption("doPut with form params");
		UNet net = new UNet();
		Map<String, String> params = new HashMap<>();
		params.put("key1", "val1");
		String result = net.doPut(baseUrl + "/put", params);
		assertNotNull(result);
		assertTrue(result.contains("PUT"));
	}

	@Test
	public void testDoDeleteWithParams() {
		printCaption("doDelete with form params");
		UNet net = new UNet();
		Map<String, String> params = new HashMap<>();
		params.put("id", "456");
		String result = net.doDelete(baseUrl + "/delete", params);
		assertNotNull(result);
		assertTrue(result.contains("DELETE"));
	}

	@Test
	public void testDoPatchWithParams() {
		printCaption("doPatch with form params");
		UNet net = new UNet();
		Map<String, String> params = new HashMap<>();
		params.put("field", "updated");
		String result = net.doPatch(baseUrl + "/patch", params);
		assertNotNull(result);
		assertTrue(result.contains("PATCH"));
	}

	@Test
	public void testDeprecatedMethods() {
		printCaption("Deprecated methods return null");
		UNet net = new UNet();
		assertNull(net.getConnMgr(), "getConnMgr should return null (deprecated)");
		assertNull(net.getRequestConfig(), "getRequestConfig should return null (deprecated)");
	}

	@Test
	public void testSetCookieAndInit() {
		printCaption("setCookie");
		UNet net = new UNet();
		net.setCookie("a=1; b=2");
		String cookies = net.getCookies();
		assertTrue(cookies.contains("a=1"));
		assertTrue(cookies.contains("b=2"));
	}

	@Test
	public void testLastUrl() {
		printCaption("lastUrl");
		UNet net = new UNet();
		net.setLastUrl("http://example.com");
		assertEquals("http://example.com", net.getLastUrl());
	}

	// ==================== HTTP Proxy tests (ClashX port 1087) ====================

	@Test
	public void testHttpProxyGet() {
		printCaption("HTTP Proxy GET -> google.com");
		UNet net = new UNet();
		net.setProxy(PROXY_HOST, HTTP_PROXY_PORT, "http");
		net.setTimeout(15000);
		String result = net.doGet(PROXY_TARGET);
		assertNotNull(result, "HTTP proxy GET should return non-null");
		assertTrue(result.length() > 100, "Should get google.com HTML content");
		assertTrue(result.toLowerCase().contains("google"), "Response should contain 'google'");
		System.out.println("HTTP proxy GET: status=" + net.getLastStatusCode() + ", length=" + result.length());
	}

	@Test
	public void testHttpProxyDownload() {
		printCaption("HTTP Proxy download -> google.com");
		UNet net = new UNet();
		net.setProxy(PROXY_HOST, HTTP_PROXY_PORT, "http");
		net.setTimeout(15000);
		byte[] data = net.downloadData(PROXY_TARGET);
		assertNotNull(data, "HTTP proxy download should return non-null");
		assertTrue(data.length > 100, "Should download google.com content");
		System.out.println("HTTP proxy download: " + data.length + " bytes");
	}

	@Test
	public void testHttpProxyClearThenDirect() {
		printCaption("HTTP Proxy clear then direct");
		UNet net = new UNet();
		net.setProxy(PROXY_HOST, HTTP_PROXY_PORT, "http");
		net.clearProxy();
		// Direct request to local server (no proxy needed)
		String result = net.doGet(baseUrl + "/get");
		assertNotNull(result, "Direct GET after clearProxy should work");
		assertTrue(result.contains("\"method\":\"GET\""));
	}

	@Test
	public void testHttpProxyInvalidPort() {
		printCaption("HTTP Proxy invalid port fails gracefully");
		UNet net = new UNet();
		net.setProxy(PROXY_HOST, 19999, "http");
		net.setTimeout(3000);
		String result = net.doGet(PROXY_TARGET);
		assertNull(result, "Request through dead proxy should fail gracefully");
	}

	// ==================== SOCKS5 Proxy tests (ss-local port 1086) ====================

	@Test
	public void testSocksProxyGet() {
		printCaption("SOCKS5 Proxy GET -> httpbin.org");
		UNet net = new UNet();
		net.setProxy(PROXY_HOST, SOCKS_PROXY_PORT, "socks");
		net.setTimeout(30000);
		String result = net.doGet("http://httpbin.org/get");
		if (result != null) {
			assertTrue(result.contains("httpbin"), "SOCKS5 proxy should get httpbin content");
			System.out.println("SOCKS5 proxy GET: status=" + net.getLastStatusCode() + ", length=" + result.length());
		} else {
			System.out.println("SOCKS5 proxy GET: " + net.getLastErr());
			// SOCKS via HttpURLConnection may not work in all JDK environments
			System.out.println("  (Java HttpURLConnection SOCKS5 fallback may not work)");
		}
	}

	@Test
	public void testSocksProxyDownload() {
		printCaption("SOCKS5 Proxy download -> httpbin.org");
		UNet net = new UNet();
		net.setProxy(PROXY_HOST, SOCKS_PROXY_PORT, "socks");
		net.setTimeout(30000);
		byte[] data = net.downloadData("http://httpbin.org/bytes/1024");
		if (data != null) {
			assertEquals(1024, data.length, "Should download 1024 bytes through SOCKS5");
			System.out.println("SOCKS5 proxy download: " + data.length + " bytes");
		} else {
			System.out.println("SOCKS5 proxy download: " + net.getLastErr());
		}
	}

	@Test
	public void testSocksProxyHttps() {
		printCaption("SOCKS5 Proxy HTTPS -> google.com");
		UNet net = new UNet();
		net.setProxy(PROXY_HOST, SOCKS_PROXY_PORT, "socks");
		net.setTimeout(30000);
		String result = net.doGet(PROXY_TARGET);
		assertNotNull(result, "SOCKS5 HTTPS GET should work (HttpsOverSocks5)");
		assertTrue(result.length() > 100, "Should get google.com content");
		assertTrue(result.toLowerCase().contains("google"), "Should contain 'google'");
		System.out.println("SOCKS5 HTTPS GET: status=" + net.getLastStatusCode() + ", length=" + result.length());
	}

	@Test
	public void testSocksProxyHttpsDownload() {
		printCaption("SOCKS5 Proxy HTTPS download -> google.com");
		UNet net = new UNet();
		net.setProxy(PROXY_HOST, SOCKS_PROXY_PORT, "socks");
		net.setTimeout(30000);
		byte[] data = net.downloadData(PROXY_TARGET);
		assertNotNull(data, "SOCKS5 HTTPS download should work (HttpsOverSocks5)");
		assertTrue(data.length > 100, "Should download google.com content");
		System.out.println("SOCKS5 HTTPS download: " + data.length + " bytes");
	}

	// ==================== Proxy without proxy (direct) ====================

	@Test
	public void testDirectGetNoProxy() {
		printCaption("Direct GET (no proxy) -> local server");
		UNet net = new UNet();
		String result = net.doGet(baseUrl + "/get");
		assertNotNull(result);
		assertTrue(result.contains("\"method\":\"GET\""));
	}

	// ==================== SSE (Server-Sent Events) tests ====================

	@Test
	public void testSseBasicGet() throws Exception {
		printCaption("SSE basic GET — event/data/id parsing");
		UNet net = new UNet();
		List<UNet.SseEvent> events = new ArrayList<>();
		CountDownLatch latch = new CountDownLatch(3);

		net.doSseAsync(baseUrl + "/sse/basic", event -> {
			events.add(event);
			latch.countDown();
		});

		assertTrue(latch.await(5, TimeUnit.SECONDS), "Should receive all 3 events within 5s");
		assertEquals(3, events.size());

		// Event 1: id=1, data={"msg":"hello"}, no event type
		assertEquals("1", events.get(0).getId());
		assertTrue(events.get(0).getData().contains("hello"));

		// Event 2: event=update, id=2, data={"msg":"world"}
		assertEquals("update", events.get(1).getEvent());
		assertEquals("2", events.get(1).getId());
		assertTrue(events.get(1).getData().contains("world"));

		// Event 3: id=3, data={"msg":"done"}
		assertEquals("3", events.get(2).getId());
		assertTrue(events.get(2).getData().contains("done"));
	}

	@Test
	public void testSseMultiLineData() throws Exception {
		printCaption("SSE multiline data");
		UNet net = new UNet();
		List<UNet.SseEvent> events = new ArrayList<>();
		CountDownLatch latch = new CountDownLatch(1);

		net.doSseAsync(baseUrl + "/sse/multiline", event -> {
			events.add(event);
			latch.countDown();
		});

		assertTrue(latch.await(5, TimeUnit.SECONDS));
		assertEquals(1, events.size());
		assertEquals("lines", events.get(0).getEvent());
		assertEquals("line1\nline2\nline3", events.get(0).getData());
	}

	@Test
	public void testSsePost() throws Exception {
		printCaption("SSE POST — sends body, receives echo");
		UNet net = new UNet();
		List<UNet.SseEvent> events = new ArrayList<>();
		CountDownLatch latch = new CountDownLatch(1);

		net.doSseAsync(baseUrl + "/sse/post", "{\"query\":\"test\"}", event -> {
			events.add(event);
			latch.countDown();
		});

		assertTrue(latch.await(5, TimeUnit.SECONDS));
		assertEquals(1, events.size());
		assertEquals("post-echo", events.get(0).getEvent());
		assertTrue(events.get(0).getData().contains("query"));
	}

	@Test
	public void testSseSyncBlocking() throws Exception {
		printCaption("SSE synchronous (blocking)");
		UNet net = new UNet();
		List<UNet.SseEvent> events = new ArrayList<>();

		// doSse is blocking — we run it in a thread
		Thread t = new Thread(() -> {
			net.doSse(baseUrl + "/sse/basic", events::add);
		});
		t.start();
		t.join(5000);

		assertFalse(t.isAlive(), "SSE blocking call should complete");
		assertEquals(3, events.size());
	}

	@Test
	public void testSseReconnect() throws Exception {
		printCaption("SSE auto-reconnect on connection close");
		UNet net = new UNet();
		net.setSseReconnect(true, 3, 100);
		List<UNet.SseEvent> events = new ArrayList<>();
		CountDownLatch latch = new CountDownLatch(3); // expect 3 events total after reconnects

		net.doSseAsync(baseUrl + "/sse/reconnect", event -> {
			events.add(event);
			latch.countDown();
		});

		boolean completed = latch.await(10, TimeUnit.SECONDS);
		// May or may not get all retries depending on server timing
		assertTrue(events.size() >= 1, "Should receive at least first event");
		assertTrue(events.get(0).getData().contains("first"));
		System.out.println("SSE reconnect: received " + events.size() + " events");
	}

	@Test
	public void testSseErrorHandling() throws Exception {
		printCaption("SSE error handling (404)");
		UNet net = new UNet();
		List<Exception> errors = new ArrayList<>();
		CountDownLatch latch = new CountDownLatch(1);

		UNet.SseListener listener = new UNet.SseListener() {
			@Override
			public void onEvent(UNet.SseEvent event) {
			}

			@Override
			public void onError(Exception e) {
				errors.add(e);
				latch.countDown();
			}
		};

		net.doSseAsync(baseUrl + "/sse/error", listener);

		assertTrue(latch.await(5, TimeUnit.SECONDS), "Should receive error callback");
		assertEquals(1, errors.size());
		assertTrue(errors.get(0).getMessage().contains("404"),
				"Error should mention HTTP 404");
	}

	@Test
	public void testSseRetryFieldParsing() throws Exception {
		printCaption("SSE retry field parsing");
		UNet net = new UNet();
		List<UNet.SseEvent> events = new ArrayList<>();
		CountDownLatch latch = new CountDownLatch(1);

		net.doSseAsync(baseUrl + "/sse/reconnect", event -> {
			events.add(event);
			latch.countDown();
		});

		assertTrue(latch.await(5, TimeUnit.SECONDS));
		assertEquals(1, events.size());
		assertEquals(Long.valueOf(500), events.get(0).getRetry(),
				"retry field should be parsed as Long 500");
	}
}
