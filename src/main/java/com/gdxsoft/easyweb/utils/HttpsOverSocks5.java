package com.gdxsoft.easyweb.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * HTTPS over SOCKS5 proxy implementation.
 * <p>
 * Java's HttpURLConnection does not properly handle HTTPS through SOCKS5 proxy.
 * This class manually implements the SOCKS5 handshake and then layers TLS on top,
 * supporting remote DNS resolution (hostname sent to proxy, not resolved locally).
 * </p>
 * <p>
 * Usage:
 * 
 * <pre>
 * HttpsOverSocks5 socks = new HttpsOverSocks5("127.0.0.1", 1086);
 * HttpsOverSocks5.Response resp = socks.get("https://www.google.com", headers, 15000);
 * </pre>
 */
public class HttpsOverSocks5 {

	private final String proxyHost;
	private final int proxyPort;
	private final String proxyUser;
	private final String proxyPassword;
	private final boolean trustAllCert;

	public HttpsOverSocks5(String proxyHost, int proxyPort) {
		this(proxyHost, proxyPort, null, null, true);
	}

	public HttpsOverSocks5(String proxyHost, int proxyPort, String proxyUser, String proxyPassword) {
		this(proxyHost, proxyPort, proxyUser, proxyPassword, true);
	}

	public HttpsOverSocks5(String proxyHost, int proxyPort, String proxyUser, String proxyPassword,
			boolean trustAllCert) {
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		this.proxyUser = proxyUser;
		this.proxyPassword = proxyPassword;
		this.trustAllCert = trustAllCert;
	}

	/**
	 * SOCKS5 response from proxy
	 */
	public static class Response {
		public int statusCode;
		public Map<String, String> headers = new HashMap<>();
		public byte[] body;

		public String bodyAsString(String charset) {
			try {
				return new String(body, charset);
			} catch (Exception e) {
				return new String(body);
			}
		}
	}

	/**
	 * Perform an HTTPS GET through SOCKS5 proxy
	 *
	 * @param url        the target HTTPS URL
	 * @param headers    request headers (nullable)
	 * @param timeoutMs  timeout in milliseconds
	 * @return the response
	 * @throws IOException on connection or protocol error
	 */
	public Response get(String url, Map<String, String> headers, int timeoutMs) throws IOException {
		URI uri = URI.create(url);
		String host = uri.getHost();
		int port = uri.getPort() > 0 ? uri.getPort() : 443;
		String path = uri.getRawPath();
		if (path == null || path.isEmpty()) {
			path = "/";
		}
		if (uri.getRawQuery() != null) {
			path = path + "?" + uri.getRawQuery();
		}

		// Connect to SOCKS5 proxy
		Socket proxySocket = new Socket();
		proxySocket.connect(new InetSocketAddress(proxyHost, proxyPort), timeoutMs);
		proxySocket.setSoTimeout(timeoutMs);

		try {
			// SOCKS5 handshake with remote DNS
			socks5Handshake(proxySocket, host, port);

			// Wrap with SSL
			SSLSocketFactory sslFactory = trustAllCert
					? UNet.createSSLContext(true).getSocketFactory()
					: (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket sslSocket = (SSLSocket) sslFactory.createSocket(proxySocket, host, port, true);
			sslSocket.startHandshake();

			// Send HTTP request
			OutputStream out = sslSocket.getOutputStream();
			StringBuilder req = new StringBuilder();
			req.append("GET ").append(path).append(" HTTP/1.1\r\n");
			req.append("Host: ").append(host);
			if (uri.getPort() > 0 && uri.getPort() != 443) {
				req.append(":").append(uri.getPort());
			}
			req.append("\r\n");

			if (headers != null) {
				for (Map.Entry<String, String> h : headers.entrySet()) {
					req.append(h.getKey()).append(": ").append(h.getValue()).append("\r\n");
				}
			}
			req.append("Connection: close\r\n");
			req.append("\r\n");

			out.write(req.toString().getBytes(StandardCharsets.UTF_8));
			out.flush();

			// Read HTTP response
			return parseHttpResponse(sslSocket.getInputStream());
		} finally {
			proxySocket.close();
		}
	}

	/**
	 * Perform SOCKS5 handshake with hostname-based connection (remote DNS).
	 * Implements RFC 1928 SOCKS Protocol Version 5.
	 */
	private void socks5Handshake(Socket socket, String targetHost, int targetPort) throws IOException {
		InputStream in = socket.getInputStream();
		OutputStream out = socket.getOutputStream();

		// Step 1: Client greeting - advertise NO AUTH and USERNAME/PASSWORD auth
		// VER | NMETHODS | METHODS
		boolean hasAuth = proxyUser != null && !proxyUser.isEmpty();
		if (hasAuth) {
			out.write(new byte[] { 0x05, 0x02, 0x00, 0x02 }); // SOCKS5, 2 methods: NO AUTH, USER/PASS
		} else {
			out.write(new byte[] { 0x05, 0x01, 0x00 }); // SOCKS5, 1 method: NO AUTH
		}
		out.flush();

		// Step 2: Server selects method
		// VER | METHOD
		int ver = in.read();
		int method = in.read();
		if (ver != 0x05) {
			throw new IOException("Not a SOCKS5 proxy (version=" + ver + ")");
		}
		if (method == 0xFF) {
			throw new IOException("SOCKS5 proxy rejected all authentication methods");
		}

		// Step 2b: If server selected USERNAME/PASSWORD auth (0x02), do auth sub-negotiation (RFC 1929)
		if (method == 0x02) {
			if (!hasAuth) {
				throw new IOException("SOCKS5 proxy requires authentication but no credentials provided");
			}
			byte[] userBytes = proxyUser.getBytes(StandardCharsets.UTF_8);
			byte[] passBytes = (proxyPassword != null ? proxyPassword : "").getBytes(StandardCharsets.UTF_8);
			ByteArrayOutputStream authBuf = new ByteArrayOutputStream();
			authBuf.write(0x01); // VER: auth sub-negotiation version
			authBuf.write(userBytes.length);
			authBuf.write(userBytes);
			authBuf.write(passBytes.length);
			authBuf.write(passBytes);
			out.write(authBuf.toByteArray());
			out.flush();

			// Read auth response: VER | STATUS
			int authVer = in.read();
			int authStatus = in.read();
			if (authVer != 0x01 || authStatus != 0x00) {
				throw new IOException("SOCKS5 authentication failed (status=" + authStatus + ")");
			}
		} else if (method != 0x00) {
			throw new IOException("SOCKS5 proxy selected unsupported auth method: " + method);
		}

		// Step 3: Connect request with hostname (ATYP=0x03 for domain name)
		// VER | CMD | RSV | ATYP | DST.ADDR | DST.PORT
		byte[] hostBytes = targetHost.getBytes(StandardCharsets.UTF_8);
		ByteArrayOutputStream reqBuf = new ByteArrayOutputStream();
		reqBuf.write(0x05); // VER: SOCKS5
		reqBuf.write(0x01); // CMD: CONNECT
		reqBuf.write(0x00); // RSV: reserved
		reqBuf.write(0x03); // ATYP: DOMAINNAME
		reqBuf.write(hostBytes.length); // length of hostname
		reqBuf.write(hostBytes); // hostname bytes
		reqBuf.write((targetPort >> 8) & 0xFF); // port high byte
		reqBuf.write(targetPort & 0xFF); // port low byte
		out.write(reqBuf.toByteArray());
		out.flush();

		// Step 4: Read connect response
		// VER | REP | RSV | ATYP | BND.ADDR | BND.PORT
		int respVer = in.read();
		int rep = in.read();
		int rsv = in.read();
		int atyp = in.read();

		if (respVer != 0x05) {
			throw new IOException("Invalid SOCKS5 response version: " + respVer);
		}
		if (rep != 0x00) {
			throw new IOException("SOCKS5 connection failed, reply code: " + rep + " (" + getSocks5ErrorName(rep) + ")");
		}

		// Skip bound address in response
		switch (atyp) {
		case 0x01: // IPv4
			in.skip(4);
			break;
		case 0x03: // Domain name
			int len = in.read();
			in.skip(len);
			break;
		case 0x04: // IPv6
			in.skip(16);
			break;
		default:
			throw new IOException("Unknown SOCKS5 address type: " + atyp);
		}
		// Skip bound port (2 bytes)
		in.skip(2);
	}

	private String getSocks5ErrorName(int rep) {
		switch (rep) {
		case 0x01: return "general failure";
		case 0x02: return "connection not allowed";
		case 0x03: return "network unreachable";
		case 0x04: return "host unreachable";
		case 0x05: return "connection refused";
		case 0x06: return "TTL expired";
		case 0x07: return "command not supported";
		case 0x08: return "address type not supported";
		default: return "unknown(" + rep + ")";
		}
	}

	/**
	 * Parse a raw HTTP/1.x response from the input stream
	 */
	private Response parseHttpResponse(InputStream in) throws IOException {
		Response resp = new Response();

		// Read status line
		String statusLine = readLine(in);
		if (statusLine == null || statusLine.isEmpty()) {
			throw new IOException("Empty HTTP response");
		}
		// HTTP/1.1 200 OK
		String[] statusParts = statusLine.split("\\s+", 3);
		if (statusParts.length >= 2) {
			resp.statusCode = Integer.parseInt(statusParts[1]);
		}

		// Read headers
		int contentLength = -1;
		boolean chunked = false;
		String line;
		while ((line = readLine(in)) != null && !line.isEmpty()) {
			int colon = line.indexOf(':');
			if (colon > 0) {
				String name = line.substring(0, colon).trim().toLowerCase();
				String value = line.substring(colon + 1).trim();
				resp.headers.put(name, value);
				if ("content-length".equals(name)) {
					contentLength = Integer.parseInt(value.trim());
				}
				if ("transfer-encoding".equals(name) && value.toLowerCase().contains("chunked")) {
					chunked = true;
				}
			}
		}

		// Read body
		if (chunked) {
			resp.body = readChunkedBody(in);
		} else if (contentLength >= 0) {
			resp.body = readFixedBody(in, contentLength);
		} else {
			// Read until connection close
			resp.body = in.readAllBytes();
		}

		return resp;
	}

	private byte[] readFixedBody(InputStream in, int length) throws IOException {
		byte[] body = new byte[length];
		int offset = 0;
		while (offset < length) {
			int read = in.read(body, offset, length - offset);
			if (read == -1) break;
			offset += read;
		}
		return body;
	}

	private byte[] readChunkedBody(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		while (true) {
			String chunkSizeLine = readLine(in);
			if (chunkSizeLine == null || chunkSizeLine.isEmpty()) break;
			int chunkSize = Integer.parseInt(chunkSizeLine.trim(), 16);
			if (chunkSize == 0) {
				readLine(in); // trailing CRLF
				break;
			}
			byte[] chunk = readFixedBody(in, chunkSize);
			out.write(chunk);
			readLine(in); // chunk trailing CRLF
		}
		return out.toByteArray();
	}

	private String readLine(InputStream in) throws IOException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		int b;
		while ((b = in.read()) != -1) {
			if (b == '\r') {
				int next = in.read();
				if (next == '\n') break;
				buf.write(b);
				if (next != -1) buf.write(next);
			} else if (b == '\n') {
				break;
			} else {
				buf.write(b);
			}
		}
		if (b == -1 && buf.size() == 0) return null;
		return buf.toString(StandardCharsets.UTF_8);
	}
}
