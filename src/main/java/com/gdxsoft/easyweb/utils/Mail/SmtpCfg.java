package com.gdxsoft.easyweb.utils.Mail;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


import jakarta.mail.Session;

public class SmtpCfg {
	private String name;

	private String host;
	private String user;
	private String password;
	private int port = 25;
	private boolean ssl;
	private boolean startTls;
	public boolean isStartTls() {
		return startTls;
	}

	public void setStartTls(boolean startTls) {
		this.startTls = startTls;
	}

	private Map<String, DKIMCfg> domains;

	public SmtpCfg(String name) {
		this.domains = new HashMap<String, DKIMCfg>();
		this.name = name;
	}

	public SmtpCfg(String name, String host, String user, String password, int port) {
		this.name = name;

		this.host = host;
		this.user = user;
		this.password = password;
		this.port = port;
		if (this.port == 465) {
			this.ssl = true;
		}
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isSsl() {
		return ssl;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	public Map<String, DKIMCfg> getDomains() {
		return domains;
	}

	public String getName() {
		return name;
	}

}
