package com.gdxsoft.easyweb.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cookie Utils
 */
public class UCookies {
	private static Logger LOGGER = LoggerFactory.getLogger(UCookies.class);

	public static String COOKIE_NAME_PREFIX = "__EWA__"; // the encrypted cookie prefix

	private String domain;
	private String path;
	private Integer maxAgeSeconds;
	private boolean httpOnly = true;
	private boolean secret = true;

	private boolean encrypt = false;

	private IUSymmetricEncyrpt symmetricEncrypt;

	/**
	 * Initialize Class
	 */
	public UCookies() {

	}

	/**
	 * Initialize Class
	 * 
	 * @param symmetricEncrypt the Encrypt/Decrypt cookie value provider
	 */
	public UCookies(IUSymmetricEncyrpt symmetricEncrypt) {
		this.symmetricEncrypt = symmetricEncrypt;
	}

	/**
	 * Initialize Class
	 * 
	 * @param path          cookie's path
	 * @param maxAgeSeconds cookie's maxAge
	 */
	public UCookies(String path, Integer maxAgeSeconds) {
		this.path = path;
		this.maxAgeSeconds = maxAgeSeconds;
	}

	/**
	 * Initialize Class
	 * 
	 * @param path          cookie's path
	 * @param maxAgeSeconds cookie's maxAge
	 * @param symmeritic    the encrypt provider
	 */
	public UCookies(String path, Integer maxAgeSeconds, IUSymmetricEncyrpt symmeritic) {
		this.path = path;
		this.maxAgeSeconds = maxAgeSeconds;
		this.setSymmetricEncrypt(symmeritic);
	}

	/**
	 * Clear user's browser all cookies
	 * 
	 * @param request   HttpServletRequest
	 * @param response  HttpServletResponse
	 * @param skipNames skip cookie's names
	 */
	public static void clearCookies(HttpServletRequest request, HttpServletResponse response, List<String> skipNames) {
		if (request == null || request.getCookies() == null) {
			return;
		}
		for (javax.servlet.http.Cookie cookie : request.getCookies()) {
			if (skipNames != null) {
				boolean isSkip = skipNames.stream().anyMatch(item -> cookie.getName().equals(item));
				if (isSkip) {
					continue;
				}
			}
			cookie.setMaxAge(0);
			cookie.setPath("/");
			cookie.setValue(null);
			response.addCookie(cookie);

			cookie.setPath(request.getContextPath());
			response.addCookie(cookie);
		}
	}

	/**
	 * Clear user's browser all cookies(jakarta.servlet.http)
	 * 
	 * @param request   HttpServletRequest
	 * @param response  HttpServletResponse
	 * @param skipNames skip cookie's names
	 */
	public static void clearCookies(jakarta.servlet.http.HttpServletRequest request,
			jakarta.servlet.http.HttpServletResponse response, List<String> skipNames) {
		if (request == null || request.getCookies() == null) {
			return;
		}
		for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
			if (skipNames != null) {
				boolean isSkip = skipNames.stream().anyMatch(item -> cookie.getName().equals(item));
				if (isSkip) {
					continue;
				}
			}
			cookie.setMaxAge(0);
			cookie.setPath("/");
			cookie.setValue(null);
			response.addCookie(cookie);

			cookie.setPath(request.getContextPath());
			response.addCookie(cookie);
		}
	}

	/**
	 * Delete the cookie
	 * 
	 * @param cookieName the cookie name
	 * @param response   HttpServletResponse
	 */
	public void deleteCookie(String cookieName, HttpServletResponse response) {
		Cookie cookie = this.createCookie(cookieName, null);
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}

	/**
	 * Delete the cookie
	 * 
	 * @param cookieName the cookie name
	 * @param response   jakarta.servlet.http.HttpServletResponse
	 */
	public void deleteCookie(String cookieName, jakarta.servlet.http.HttpServletResponse response) {
		jakarta.servlet.http.Cookie cookie = this.createCookieJakarta(cookieName, null);
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}

	/**
	 * Add a cookie
	 * 
	 * @param cookieName  the cookie name
	 * @param cookieValue the cookie value
	 * @param response    HttpServletResponse
	 * @return Cookie the new cookie
	 */
	public Cookie addCookie(String cookieName, String cookieValue, HttpServletResponse response) {
		Cookie cookie = this.createCookie(cookieName, cookieValue);
		response.addCookie(cookie);

		return cookie;
	}

	/**
	 * Add a cookie (jakarta.servlet.http tomcat10)
	 * 
	 * @param cookieName  the cookie name
	 * @param cookieValue the cookie value
	 * @param response    HttpServletResponse
	 * @return Cookie the new cookie
	 */
	public jakarta.servlet.http.Cookie addCookie(String cookieName, String cookieValue,
			jakarta.servlet.http.HttpServletResponse response) {
		jakarta.servlet.http.Cookie cookie = this.createCookieJakarta(cookieName, cookieValue);
		response.addCookie(cookie);

		return cookie;
	}

	/**
	 * UrlEncode a cookie value (ascii)
	 * 
	 * @param cookieValue the cookie plain text
	 * @return UrlEncode.encoded value
	 */
	public static String encodeCookieValue(String cookieValue) {
		if (cookieValue == null) {
			return null;
		}
		String cv;
		try {
			cv = URLEncoder.encode(cookieValue, "ascii");
		} catch (UnsupportedEncodingException e1) {
			LOGGER.warn(e1.getMessage());
			cv = cookieValue;
		}

		return cv;
	}

	/**
	 * URLDecoder a cookie value (ascii)
	 * 
	 * @param encoderCookieValue the UrlEncode.encoded cookie value
	 * @return URLDecoder.decoded value
	 */
	public static String decodeCookieValue(String encoderCookieValue) {
		try {
			return URLDecoder.decode(encoderCookieValue, "ascii");
		} catch (UnsupportedEncodingException e) {
			LOGGER.warn(e.getMessage());
			return encoderCookieValue;
		}
	}

	/**
	 * Create a jakarta.servlet.http.cookie(tomcat10)
	 * 
	 * @param cookieName  the cookie name
	 * @param cookieValue the cookie value
	 * @return new cookie
	 */
	public jakarta.servlet.http.Cookie createCookieJakarta(String cookieName, String cookieValue) {

		jakarta.servlet.http.Cookie cookie = null;

		if (this.encrypt) {
			String ckName = COOKIE_NAME_PREFIX + cookieName;
			if (cookieValue == null) {
				cookie = new jakarta.servlet.http.Cookie(ckName, null);
			} else {
				try {
					String value = this.getSymmetricEncrypt().encrypt(cookieValue);
					cookie = new jakarta.servlet.http.Cookie(ckName, encodeCookieValue(value));
				} catch (Exception e) {
					LOGGER.error(e.getMessage());
					return null;
				}
			}
		} else {
			cookie = new jakarta.servlet.http.Cookie(cookieName, encodeCookieValue(cookieValue));
		}
		cookie.setHttpOnly(httpOnly);
		cookie.setSecure(secret);
		if (domain != null) {
			cookie.setDomain(domain);
		}
		if (path != null) {
			cookie.setPath(path);
		}
		if (this.maxAgeSeconds != null) {
			cookie.setMaxAge(this.maxAgeSeconds);
		}

		return cookie;
	}

	/**
	 * create a new cookie
	 * 
	 * @param cookieName  the cookie name
	 * @param cookieValue the cookie value
	 * @return new cookie
	 */
	public Cookie createCookie(String cookieName, String cookieValue) {

		Cookie cookie = null;

		if (this.encrypt) {
			String ckName = COOKIE_NAME_PREFIX + cookieName;
			if (cookieValue == null) {
				cookie = new Cookie(ckName, null);
			} else {
				try {
					String value = this.getSymmetricEncrypt().encrypt(cookieValue);
					cookie = new Cookie(ckName, encodeCookieValue(value));
				} catch (Exception e) {
					LOGGER.error(e.getMessage());
					return null;
				}
			}
		} else {
			cookie = new Cookie(cookieName, encodeCookieValue(cookieValue));
		}
		cookie.setHttpOnly(httpOnly);
		cookie.setSecure(secret);
		if (domain != null) {
			cookie.setDomain(domain);
		}
		if (path != null) {
			cookie.setPath(path);
		}
		if (this.maxAgeSeconds != null) {
			cookie.setMaxAge(this.maxAgeSeconds);
		}

		return cookie;
	}

	/**
	 * Cookie's domain
	 * 
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * Cookie's Domain
	 * 
	 * @param domain the domain to set
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}

	/**
	 * Cookie's Path
	 * 
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Cookie's Path
	 * 
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Cookie's MaxAge
	 * 
	 * @return the maxAgeSeconds
	 */
	public Integer getMaxAgeSeconds() {
		return maxAgeSeconds;
	}

	/**
	 * Cookie's MaxAge
	 * 
	 * @param maxAgeSeconds the maxAgeSeconds to set
	 */
	public void setMaxAgeSeconds(Integer maxAgeSeconds) {
		this.maxAgeSeconds = maxAgeSeconds;
	}

	/**
	 * Cookie's httpOnly
	 * 
	 * @return the httpOnly
	 */
	public boolean isHttpOnly() {
		return httpOnly;
	}

	/**
	 * Cookie's httpOnly
	 * 
	 * @param httpOnly the httpOnly to set
	 */
	public void setHttpOnly(boolean httpOnly) {
		this.httpOnly = httpOnly;
	}

	/**
	 * Cookie's secure
	 * 
	 * @return the secert
	 */
	public boolean isSecret() {
		return secret;
	}

	/**
	 * Cookie's secure
	 * 
	 * @param secret the secert to set
	 */
	public void setSecret(boolean secret) {
		this.secret = secret;
	}

	/**
	 * Encrypt cookie value, using setSymmetricEncrypt
	 * 
	 * @return the ewaDes
	 */
	public boolean isEncrypt() {
		return encrypt;
	}

	/**
	 * Encrypt/ Decrypt Cookie provider
	 * 
	 * @return
	 */
	public IUSymmetricEncyrpt getSymmetricEncrypt() {
		return symmetricEncrypt;
	}

	/**
	 * Set Encrypt/ Decrypt cookie value provider
	 * 
	 * @param symmetricEncrypt
	 */
	public void setSymmetricEncrypt(IUSymmetricEncyrpt symmetricEncrypt) {
		this.symmetricEncrypt = symmetricEncrypt;
		if (this.symmetricEncrypt != null) {
			this.encrypt = true;
		} else {
			this.encrypt = false;
		}
	}
}
