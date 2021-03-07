/**
 * 
 */
package com.gdxsoft.easyweb.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Url工具类，添加删除参数等
 * 
 * @author guolei
 *
 */
public class UUrl {
	private static Logger LOGGER = LoggerFactory.getLogger(UUrl.class);
	private HttpServletRequest request_;
	private String root_; // 域名和协议，结尾以 '/'结束
	private String root0_; // 域名和协议，结尾不包含 '/'
	private String path_;
	private String name_;
	private Map<String, String> params_;
	private Map<String, String> names_;

	public UUrl() {
		params_ = new HashMap<String, String>();
		names_ = new HashMap<String, String>();
	}

	public UUrl(String url) {
		params_ = new HashMap<String, String>();
		names_ = new HashMap<String, String>();

		this.initWithUrl(url);
	}

	private void initWithUrl(String urlString) {
		try {
			URL url = new URL(urlString);

			String getServerName = url.getHost();
			int getServerPort = url.getPort();
			String getScheme = url.getProtocol();
			String port = (getServerPort == 80 || getServerPort == 443) ? "" : ":" + getServerPort + "";
			if (getServerPort == 443) {
				getScheme = "https";
			}
			this.root0_ = getScheme + "://" + getServerName + port;
			this.root_ = this.root0_ + "/";

			this.path_ = url.getPath();
			this.name_ = "";

			this.initParameters(url.getQuery());

		} catch (MalformedURLException e) {
			LOGGER.error("", e);
		}
	}

	/**
	 * 初始化
	 * 
	 * @param request HttpServletRequest
	 */
	public UUrl(HttpServletRequest request) {
		request_ = request;
		params_ = new HashMap<String, String>();
		names_ = new HashMap<String, String>();
		this.init();
	}

	/**
	 * 获取参数表达式
	 * 
	 * @return 参数表达式
	 */
	public String getParameters() {
		StringBuilder sb = new StringBuilder();
		for (String name : this.params_.keySet()) {
			String value = this.params_.get(name);

			String name1 = Utils.textToUrl(name);
			String value1 = Utils.textToUrl(value);

			if (sb.length() > 0) {
				sb.append("&");
			}
			sb.append(name1);
			sb.append("=");
			sb.append(value1);
		}

		return sb.toString();
	}

	/**
	 * 获取url，不包含域名
	 * 
	 * @return 获取url，不包含域名
	 */
	public String getUrl() {
		return this.getUrl(true);
	}

	/**
	 * 获取url，不包含域名
	 * 
	 * @param includeQuery 是否包含QueryString
	 * @return 获取url，不包含域名
	 */
	public String getUrl(boolean includeQuery) {
		StringBuilder sb = new StringBuilder();
		sb.append(this.path_);
		// sb.append("/");
		sb.append(this.name_);

		if (includeQuery) {
			String query = this.getParameters();
			if (query.length() > 0) {
				sb.append("?");
				sb.append(query);
			}
		}
		return sb.toString();
	}

	/**
	 * 获取完整的url，包含域名与协议和QueryString
	 * 
	 * @return 获取完整的url
	 */
	public String getUrlWithDomain() {
		return this.getUrlWithDomain(true);
	}

	/**
	 * 获取完整的url，包含域名与协议
	 * 
	 * @param includeQuery 是否包含QueryString
	 * @return 获取完整的url
	 */
	public String getUrlWithDomain(boolean includeQuery) {
		StringBuilder sb = new StringBuilder();
		sb.append(this.root0_);
		sb.append(this.getUrl(includeQuery));

		return sb.toString();
	}

	/**
	 * 添加参数
	 * 
	 * @param name  名称
	 * @param value 值
	 */
	public void add(String name, Object value) {
		if (value == null) {
			return;
		}
		this.remove(name);

		String name1 = name.toUpperCase().trim();
		this.names_.put(name1, name);
		this.params_.put(name, value.toString());
	}

	/**
	 * 删除参数
	 * 
	 * @param name 名称
	 * @return 是否删除
	 */
	public boolean remove(String name) {
		String name1 = name.toUpperCase().trim();
		if (names_.containsKey(name1)) {
			String paraName = names_.remove(name1);
			params_.remove(paraName);

			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns the parameter
	 * 
	 * @param name the parameter name
	 * @return the parameter
	 */
	public String getParamter(String name) {
		String name1 = name.toUpperCase().trim();
		return this.params_.get(name1);
	}

	/**
	 * 初始化
	 */
	private void init() {
		if (request_ == null) {
			return;
		}
		String getServerName = request_.getServerName();
		int getServerPort = request_.getServerPort();
		String getScheme = request_.getScheme();
		String port = (getServerPort == 80 || getServerPort == 443) ? "" : ":" + getServerPort + "";
		if (getServerPort == 443) {
			getScheme = "https";
		}
		this.root0_ = getScheme + "://" + getServerName + port;
		this.root_ = this.root0_ + "/";
		this.initParameters(request_.getQueryString());

		this.path_ = request_.getContextPath();
		this.name_ = request_.getServletPath();
	}

	private void initParameters(String query) {

		if (query == null || query.trim().length() == 0) {
			return;
		}

		String[] qs = query.split("\\&");
		for (int i = 0; i < qs.length; i++) {
			String[] param = qs[i].split("\\=");
			if (param.length == 0) {
				continue; // index.jsp?=&ewa_lang=zhcn&login_type=APP_LOGIN_TEACHER
			}
			String paraName = param[0];
			String paraValue;
			if (param.length > 1) {
				paraValue = param[1];
				if (paraValue.indexOf("%") >= 0) {
					try {
						paraValue = java.net.URLDecoder.decode(paraValue, "utf-8");
					} catch (Exception e) {
					}
				}
			} else {
				paraValue = "";
			}

			this.add(paraName, paraValue);
		}
	}

	/**
	 * HttpServletRequest
	 * 
	 * @return HttpServletRequest
	 */
	public HttpServletRequest getRequest_() {
		return request_;
	}

	/**
	 * 域名和协议，结尾以 '/'结束
	 * 
	 * @return 域名和协议，结尾以 '/'结束
	 */
	public String getRoot() {
		return root_;
	}

	/**
	 * 域名和协议，结尾不包含 '/'
	 * 
	 * @return 域名和协议，结尾不包含 '/'
	 */
	public String getRoot0() {
		return root0_;
	}

	/**
	 * 域名和协议
	 * 
	 * @param root
	 */
	public void setRoot(String root) {
		int inc = 0;
		while (root.endsWith("/")) {
			root = root.substring(0, root.length() - 1);
			if (inc > 10000) {
				break;
			}
		}
		this.root0_ = root;
		this.root_ = this.root0_ + "/";
	}

	/**
	 * 路径
	 * 
	 * @return 路径
	 */
	public String getPath() {
		return path_;
	}

	/**
	 * 路径
	 * 
	 * @param path 路径
	 */
	public void setPath(String path) {
		this.path_ = path;
	}

	/**
	 * 文件名称
	 * 
	 * @return 文件名称
	 */
	public String getName() {
		return name_;
	}

	/**
	 * 文件名称
	 * 
	 * @param name 文件名称
	 */
	public void setName(String name) {
		this.name_ = name;
	}

	/**
	 * 参数表
	 * 
	 * @return 参数表
	 */
	public Map<String, String> getParams() {
		return params_;
	}

	/**
	 * 参数名称，大写
	 * 
	 * @return 参数名称，大写
	 */
	public Map<String, String> getNames() {
		return names_;
	}
}
