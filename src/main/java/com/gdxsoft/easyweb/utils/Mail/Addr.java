package com.gdxsoft.easyweb.utils.Mail;

/**
 * 邮件地址
 * 
 * @author Administrator
 *
 */
public class Addr {
	public Addr() {

	}

	public Addr(String email, String name) {
		this._Email = email;
		this._Name = name;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (this._Name != null) {
			sb.append(this._Name);
			sb.append(" ");
		}
		sb.append("<");
		sb.append(this._Email);
		sb.append(">");
		return sb.toString();
	}

	/**
	 * @return the _Name
	 */
	public String getName() {
		return _Name;
	}

	/**
	 * @param name the _Name to set
	 */
	public void setName(String name) {
		_Name = name;
	}

	/**
	 * @return the _Email
	 */
	public String getEmail() {
		return _Email;
	}

	/**
	 * @param email the _Email to set
	 */
	public void setEmail(String email) {
		_Email = email;
	}

	private String _Name;
	private String _Email;

}
