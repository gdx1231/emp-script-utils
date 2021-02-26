package com.gdxsoft.easyweb.utils.Mail;

public interface IDKIMCanonicalization {
	public String getType();

	public String canonicalizeHeader(String name, String value);

	public String canonicalizeBody(String body);
}
