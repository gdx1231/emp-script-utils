package com.gdxsoft.easyweb.utils.Mail;


public class DKIMCanonicalizationSimpleImpl implements IDKIMCanonicalization {

	@Override
	public String getType() {
		return "simple";
	}

	@Override
	public String canonicalizeHeader(String name, String value) {

		return name + ":" + value;
	}

	@Override
	public String canonicalizeBody(String body) {

		if (body == null || "".equals(body)) {
			return "\r\n";
		}

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
