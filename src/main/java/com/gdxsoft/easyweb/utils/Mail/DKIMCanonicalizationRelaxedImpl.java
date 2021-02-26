package com.gdxsoft.easyweb.utils.Mail;


/*
 * 消息规范化算法
 * Provides Simple and Relaxed Canonicalization according to DKIM RFC 4871.
 * 
 */
public class DKIMCanonicalizationRelaxedImpl implements IDKIMCanonicalization {

	/**
	 * 体规范化算法 relaxed
	 */
	public String getType() {

		return "relaxed";
	}

	public String canonicalizeHeader(String name, String value) {
		name = name.trim().toLowerCase();
		value = value.replaceAll("\\s+", " ").trim();
		return name + ":" + value;
	}

	public String canonicalizeBody(String body) {
		if (body == null || "".equals(body)) {
			return "\r\n";
		}

		body = body.replaceAll("[ \\t\\x0B\\f]+", " ");
		body = body.replaceAll(" \r\n", "\r\n");

		// The body must end with \r\n
		if (!"\r\n".equals(body.substring(body.length() - 2, body.length()))) {
			return body + "\r\n";
		}

		// Remove trailing empty lines ...
		while ("\r\n\r\n".equals(body.substring(body.length() - 4, body.length()))) {
			body = body.substring(0, body.length() - 2);
		}

		return body;
	}

}
