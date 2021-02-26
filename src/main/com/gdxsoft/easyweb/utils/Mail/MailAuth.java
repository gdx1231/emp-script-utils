package com.gdxsoft.easyweb.utils.Mail;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;

public class MailAuth extends Authenticator {
	private String strUser;
	private String strPwd;

	public MailAuth(String user, String password) {
		this.strUser = user;
		this.strPwd = password;
	}

	public PasswordAuthentication getPasswordAuthentication() {
		PasswordAuthentication pwd = new PasswordAuthentication(strUser, strPwd);
		return pwd;
	}
}
