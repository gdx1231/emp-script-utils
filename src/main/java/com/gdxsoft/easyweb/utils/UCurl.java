package com.gdxsoft.easyweb.utils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * curl-style command-line HTTP client backed by UNet.
 *
 * Usage:
 * <pre>
 *   java UCurl &lt;url&gt;                    # GET
 *   java UCurl -X POST &lt;url&gt;              # POST (no body)
 *   java UCurl -d "key=val" &lt;url&gt;         # POST with form body
 *   java UCurl --log <url>              # verbose logging
 *   java UCurl -H "Authorization: Bearer t" &lt;url&gt;
 *   java UCurl -x http://127.0.0.1:1087 &lt;url&gt;
 *   java UCurl -o /tmp/out.bin &lt;url&gt;       # download to file
 * </pre>
 */
public class UCurl {

	public static void main(String[] args) {
		if (args.length == 0) {
			printUsage();
			System.exit(1);
		}

		String method = "GET";
		String body = null;
		String outputFile = null;
		boolean verbose = false;
		Map<String, String> headers = new LinkedHashMap<>();
		String proxy = null;

		int i = 0;
		while (i < args.length) {
			switch (args[i]) {
			case "-X":
				method = args[++i].toUpperCase();
				break;
			case "-d":
				body = args[++i];
				if (method.equals("GET")) {
					method = "POST";
				}
				break;
			case "-H":
				String h = args[++i];
				int colon = h.indexOf(':');
				if (colon > 0) {
					headers.put(h.substring(0, colon).trim(), h.substring(colon + 1).trim());
				}
				break;
			case "-x":
				proxy = args[++i];
				break;
			case "-o":
				outputFile = args[++i];
				break;
			case "-v":
			case "--log":
				verbose = true;
				break;
			default:
				if (!args[i].startsWith("-")) {
					String url = ensureScheme(args[i]);
					execute(url, method, body, headers, proxy, outputFile, verbose);
					return;
				}
				System.err.println("Unknown option: " + args[i]);
				System.exit(1);
			}
			i++;
		}

		System.err.println("Error: no URL provided");
		System.exit(1);
	}

	/**
	 * Auto-prepend https:// if the URL lacks a scheme.
	 */
	private static String ensureScheme(String url) {
		if (url.contains("://")) {
			return url;
		}
		return "https://" + url;
	}

	private static void execute(String url, String method, String body, Map<String, String> headers,
			String proxy, String outputFile, boolean verbose) {
		UNet net = new UNet();
		net.setIsShowLog(verbose);

		// Proxy: parse URL-like string (e.g. "http://127.0.0.1:1087" or "socks://127.0.0.1:1086")
		if (proxy != null) {
			applyProxy(net, proxy);
		}

		// Headers
		for (Map.Entry<String, String> e : headers.entrySet()) {
			net.addHeader(e.getKey(), e.getValue());
		}

		// Execute — use binary download path when writing to file
		String result = null;
		byte[] buf = null;

		if (outputFile != null) {
			// Binary path: preserves raw bytes
			switch (method) {
			case "GET":
				buf = net.downloadData(url);
				break;
			case "POST":
				buf = net.postMsgAndDownload(url, body);
				break;
			default:
				// PUT/DELETE use string path then grab buffer
				executeStringMethod(net, method, url, body);
				buf = net.getLastBuf();
				break;
			}
		} else {
			// Text path
			result = executeStringMethod(net, method, url, body);
		}

		int status = net.getLastStatusCode();

		// Output
		if (outputFile != null && buf != null) {
			try {
				java.nio.file.Files.write(java.nio.file.Path.of(outputFile), buf);
				System.err.println("Saved to " + outputFile + " (" + buf.length + " bytes)");
			} catch (Exception ex) {
				System.err.println("Failed to write file: " + ex.getMessage());
				System.exit(1);
			}
		} else if (result != null) {
			System.out.print(result);
			if (!result.endsWith("\n")) {
				System.out.println();
			}
		} else if (buf != null) {
			try {
				System.out.write(buf);
			} catch (IOException e) {
				System.err.println("Write error: " + e.getMessage());
				System.exit(1);
			}
		}

		if (status >= 400) {
			System.exit(1);
		}

	}

	private static String executeStringMethod(UNet net, String method, String url, String body) {
		switch (method) {
		case "GET":
			return net.doGet(url);
		case "POST":
			return net.doPost(url, body);
		case "PUT":
			return net.doPut(url, body);
		case "DELETE":
			return net.doDelete(url, body);
		default:
			System.err.println("Unsupported method: " + method);
			System.exit(1);
			return null;
		}
	}

	/**
	 * Parse proxy URL and apply to UNet. Supports:
	 * <ul>
	 * <li>{@code http://host:port} or just {@code host:port} → HTTP proxy</li>
	 * <li>{@code socks://host:port} or {@code socks5://host:port} → SOCKS5 proxy</li>
	 * </ul>
	 */
	private static void applyProxy(UNet net, String proxy) {
		String scheme = "http";
		String host;
		int port;

		String s = proxy;
		if (s.contains("://")) {
			int idx = s.indexOf("://");
			scheme = s.substring(0, idx);
			s = s.substring(idx + 3);
		}

		int colon = s.lastIndexOf(':');
		if (colon > 0) {
			host = s.substring(0, colon);
			try {
				port = Integer.parseInt(s.substring(colon + 1));
			} catch (NumberFormatException e) {
				port = 1080;
			}
		} else {
			host = s;
			port = "socks".equals(scheme) ? 1080 : 1087;
		}

		// Normalize scheme
		if ("socks5".equals(scheme)) {
			scheme = "socks";
		}

		net.setProxy(host, port, scheme);
	}

	private static void printUsage() {
		System.out.println("UCurl - curl-style HTTP client (backed by UNet)");
		System.out.println();
		System.out.println("Usage:  java UCurl [options] <url>");
		System.out.println();
		System.out.println("Options:");
		System.out.println("  -X METHOD        HTTP method (GET, POST, PUT, DELETE), default GET");
		System.out.println("  -d DATA          Request body (implies POST when method is GET)");
		System.out.println("  -H \"Key: Value\"   Add request header");
		System.out.println("  -x PROXY_URL     Set proxy (http://host:port or socks://host:port)");
		System.out.println("  -o FILE          Write response body to file");
		System.out.println("  -v, --log        Enable verbose request/response logging");
		System.out.println();
		System.out.println("Examples:");
		System.out.println("  java UCurl https://httpbin.org/get");
		System.out.println("  java UCurl -d \"name=foo\" https://httpbin.org/post");
		System.out.println("  java UCurl -H \"Authorization: Bearer xxx\" https://api.example.com/data");
		System.out.println("  java UCurl -x socks://127.0.0.1:1086 https://example.com");
		System.out.println("  java UCurl -o /tmp/page.html https://example.com");
	}
}
