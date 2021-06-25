package com.gdxsoft.easyweb.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.Mail.SmtpCfgs;
import com.gdxsoft.easyweb.utils.msnet.MTableStr;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UPath {
	private static boolean mainCall = false;
	private static Logger LOG = LoggerFactory.getLogger(UPath.class);
	/**
	 * 默认的ewa_conf的名字
	 */
	public static String CONF_NAME = null;
	/**
	 * ewa_conf.xml的 document对象
	 */
	private static Document CFG_XML_DOC;

	private static HashMap<String, String> RV_GLOBALS;

	/**
	 * 放到 RequestValue 的全局变量
	 * 
	 * @return RequestValue 的全局变量
	 */
	public static HashMap<String, String> getRV_GLOBALS() {
		initPath();
		return RV_GLOBALS;
	}

	private static HashMap<String, String> RV_TYPES;

	/**
	 * 定义RequestValue的初始化类型，例如： USR_ID :int
	 * 
	 * @return 定义RequestValue的初始化类型
	 */
	public static HashMap<String, String> getRvTypes() {
		initPath();
		return RV_TYPES;
	}

	/**
	 * 项目Class所在目录
	 */
	public static String PATH_REAL = "";

	/**
	 * 临时目录根目录
	 */
	public static String PATH_TEMP = "_EWA_TMP_";
	/**
	 * 项目缓存目录
	 */
	public static String PATH_PRJ_CACHE = PATH_TEMP + "/PRJ/";
	/**
	 * 图片的缓存目录
	 */
	public static String PATH_IMG_CACHE = PATH_TEMP + "/IMG/";

	/**
	 * des="图片缩略图保存根路径URL, ！！！需要在Tomcat或Apache或Nginx中配置虚拟路径！！！。"
	 * Name="img_tmp_path_url"，如果ewa_conf中没有配置的话，则取当前contextpath
	 */
	public static String PATH_IMG_CACHE_URL = null;
	/**
	 * 上传文件物理路径，来自ewa_conf中的 img_tmp_path <br>
	 * 如果ewa_conf中没有配置的话，则取当前contextpath
	 */
	private static String PATH_UPLOAD;
	/**
	 * 上传文件物理的URL<br>
	 * 来自ewa_conf中的 img_tmp_path_url <br>
	 * 如果ewa_conf中没有配置的话，则取当前contextpath
	 */
	private static String PATH_UPLOAD_URL;

	private static String PATH_SCRIPT = "";
	private static String PATH_CONFIG = "";
	private static String PATH_MANAGMENT = "";

	private static long PROP_TIME = -1231;

	private static String PATH_GROUP = ""; // 用于组件的生成和导入目录

	private static MTableStr DEBUG_IPS; // 用于页面显示跟踪的IP地址
	private static boolean IS_DEBUG_SQL;

	private static boolean IS_WEB_CALL;

	private static MTableStr VALID_DOMAINS; // 合法的域名，用于合并css，js的合法域名检查，避免跨域攻击

	private static String CVT_OPENOFFICE_HOME;
	private static String CVT_SWFTOOL_HOME;
	private static String CVT_IMAGEMAGICK_HOME;

	private static String WF_XML;
	private static String DATABASE_XML;
	private static MTableStr INIT_PARAS;
	private static long LAST_CHK = 0;
	/**
	 * 用于Cache文件的目录
	 */
	private static String PATH_CACHED = "";
	private static String CFG_CACHE_METHOD;

	private static URL CONF_URL;

	public static long CHK_DURATION = 60 * 1000;
	static {
		// initPath();
	}

	/**
	 * 合法的域名，用于合并css，js的合法域名检查，避免跨域攻击
	 * 
	 * @return 合法的域名
	 */
	public static MTableStr getVALID_DOMAINS() {
		initPath();
		return VALID_DOMAINS;
	}

	/**
	 * 检查是否为合法域名
	 * 
	 * @param domain 域名
	 * @return 是否合法
	 */
	public static boolean checkIsValidDomain(String domain) {
		MTableStr map = getVALID_DOMAINS();
		if (domain == null || domain.trim().length() == 0) {
			return false;
		}
		String s = domain.trim().toLowerCase();
		if (map.containsKey(s)) {
			return true;
		}
		int loc = s.indexOf(".");
		if (loc <= 0) {
			return false;
		}
		if (s.length() == loc + 1) {
			return false;
		}

		String s1 = s.substring(loc + 1);
		// *.gezz.cn
		for (Object key : map.getTable().keySet()) {
			String d = key.toString();
			if (!d.startsWith("*.")) {
				continue;
			}
			d = d.replace("*.", "");
			if (d.equals(s1)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 是否Web加载
	 * 
	 * @return 是否Web加载
	 */
	public static boolean isWebCall() {
		initPath();
		return IS_WEB_CALL;
	}

	/**
	 * 获取OpenOffice目录
	 * 
	 * @return the cVT_OPENOFFICE_HOME
	 */
	public static String getCVT_OPENOFFICE_HOME() {
		return CVT_OPENOFFICE_HOME;
	}

	/**
	 * 获取swftool目录，转换成flash用
	 * 
	 * @return the cVT_SWFTOOL_HOME
	 */
	public static String getCVT_SWFTOOL_HOME() {
		return CVT_SWFTOOL_HOME;
	}

	/**
	 * 获取 ImageMagick的可执行目录(bin)
	 * 
	 * @return ImageMagick的可执行目录(bin)
	 */
	public static String getCVT_IMAGEMAGICK_HOME() {
		initPath();
		return CVT_IMAGEMAGICK_HOME;
	}

	public UPath() {

	}

	/**
	 * 获取EMP SCRIPT的系统配置目录
	 * 
	 * @return 获取EMP SCRIPT的系统配置目录
	 */
	public static String getConfigPath() {
		initPath();
		return PATH_CONFIG;
	}

	/**
	 * 获取EMP SCRIPT的系统描述目录
	 * 
	 * @return 获取EMP SCRIPT的系统描述目录
	 */
	public static String getScriptPath() {
		initPath();
		return PATH_SCRIPT;
	}

	/**
	 * 获取EMP SCRIPT的系统管理目录
	 * 
	 * @return EMP SCRIPT的系统管理目录
	 */
	@Deprecated
	public static String getManagementPath() {
		initPath();
		return PATH_MANAGMENT;
	}

	/**
	 * 获取EMP SCRIPT的系统的BIN目录
	 * 
	 * @return EMP SCRIPT的系统的BIN目录
	 */
	public static String getRealPath() {
		initPath();
		return PATH_REAL;
	}

	/**
	 * 项目路径
	 * 
	 * @return 项目路径
	 */
	public static String getProjectPath() {
		return getConfigPath() + "/projects/";
	}

	/**
	 * 获取组件生产和导入目录
	 * 
	 * @return 获取组件生产和导入目录
	 */
	public static String getGroupPath() {
		initPath();
		return PATH_GROUP;
	}

	/**
	 * 获取用于Cache文件的目录
	 * 
	 * @return 用于Cache文件的目录
	 */
	public static String getCachedPath() {
		initPath();
		return PATH_CACHED;
	}

	/**
	 * 获取ewa_conf.xml的 document对象
	 * 
	 * @return ewa_conf.xml的 document对象
	 */
	public static Document getCfgXmlDoc() {
		initPath();
		return CFG_XML_DOC;
	}

	private synchronized static void initPathReal() {
		Utils ut = new Utils();
		ClassLoader loader = ut.getClass().getClassLoader();

		String path = null;
		URL p0 = loader.getResource("/");
		URL p1 = loader.getResource(".");
		URL p2 = loader.getResource("");

		if (mainCall) {
			System.out.println("getResource(\"/\")" + (p0 == null ? "null" : p0.toString()));
			System.out.println("getResource(\".\")" + (p1 == null ? "null" : p1.toString()));
			System.out.println("getResource(\"\")" + (p2 == null ? "null" : p2.toString()));
		} else {
			LOG.debug("getResource(\"/\")" + (p0 == null ? "null" : p0.toString()));
			LOG.debug("getResource(\".\")" + (p1 == null ? "null" : p1.toString()));
			LOG.debug("getResource(\"\")" + (p2 == null ? "null" : p2.toString()));
		}

		if (p0 != null) { // tomcat call
			path = p0.getPath();
			IS_WEB_CALL = true;
		} else if (p1 != null) {// console call
			path = p1.getPath();
			IS_WEB_CALL = false;
		} else {
			return;
		}
		if (path != null) {
			File f1 = new File(path);
			path = f1.getPath().replaceAll("%20", " ");
			PATH_REAL = path + "/";
			String msg = "PATH_REAL=" + PATH_REAL;
			if (mainCall) {
				System.out.println(msg);
			} else {
				LOG.info(msg);
			}
		}
	}

	private static boolean loadEwaConf() throws Exception {
		File f2 = null;
		if (CONF_NAME != null) { // 用户指定文件名称
			String xmlNameDefined = CONF_NAME;
			f2 = new File(xmlNameDefined);
			if (f2.exists()) {
				URL userDefined = new URL(f2.getAbsolutePath());
				loadConfXml(userDefined);
				return true;
			} else {
				String msg = "Can't found the user's conf file: " + f2.getAbsolutePath();
				if (mainCall) {
					System.out.println(msg);
				} else {
					LOG.error(msg);
				}
			}
		}
		URL ewaConf = UPath.class.getClassLoader().getResource("ewa_conf.xml");
		if (ewaConf != null) {
			loadConfXml(ewaConf);
			return true;
		}
		URL ewaConfConsole = UPath.class.getClassLoader().getResource("ewa_conf_console.xml");
		if (ewaConfConsole != null) {
			loadConfXml(ewaConfConsole);
			return true;
		}
		CFG_XML_DOC = null;
		if (mainCall) {
			System.out.println("Not found ewa_conf.xml or ewa_conf_console.xml");
		} else {
			LOG.error("Not found ewa_conf.xml or ewa_conf_console.xml");
		}
		return false;
	}

	private static void loadConfXml(URL url) throws IOException {
		String xml = IOUtils.toString(url, StandardCharsets.UTF_8);
		CFG_XML_DOC = UXml.asDocument(xml);
		String msg = "Loaded ewa_conf, " + url;

		if (mainCall) {
			System.out.println(msg);
		} else {
			LOG.info(msg);
		}

		CONF_URL = url;

		isConfFileChanged();
	}

	private static boolean isConfFileChanged() {
		if (CONF_URL == null) {
			return true;
		}

		File f = new File(CONF_URL.getPath());
		if (!f.exists()) {
			return true;
		}

		if (f.lastModified() == PROP_TIME) {
			LAST_CHK = System.currentTimeMillis();
			return false;
		} else {
			if (PROP_TIME == -1231) { // Initialized default value
				PROP_TIME = f.lastModified();
			}
			return true;
		}

	}

	/**
	 * 初始化配置路径
	 */
	public static void initPath() {
		long diff = System.currentTimeMillis() - LAST_CHK;
		if (diff < CHK_DURATION) {// 60秒内不重新检查
			return;
		}
		if (isConfFileChanged()) {
			reloadConf();
		} else {
			String msg = "The conf file has not changed";
			if (mainCall) {
				System.out.println(msg);
			} else {
				LOG.debug(msg);
			}
		}
	}

	/**
	 * Reload the configuration
	 */
	public synchronized static void reloadConf() {
		LAST_CHK = System.currentTimeMillis();
		initPathReal();
		try {
			if (!loadEwaConf()) {
				return;
			}
		} catch (Exception e) {
			if (mainCall) {
				System.out.println(e.getMessage());
			} else {
				LOG.error("Load conf fail, " + e.getMessage());
			}
		}
		try {
			initPathXml();
		} catch (Exception e) {
			String msg = "Failed to parse the XML conf, " + e.getMessage();
			if (mainCall) {
				System.out.println(msg);
			} else {
				LOG.error(msg);
			}
		}
	}

	/**
	 * 初始化定义RequestValue的初始化类型，例如： USR_ID->int <br>
	 * &lt;requestValueType Name= "G_SUP_ID, G_ADM_ID, GRP_ID, ENQ_ID, ENQ_JNY_ID" Type="int" /&gt;
	 * 
	 * @param doc
	 */
	private static void initRequestValuesType(Document doc) {
		RV_TYPES = new HashMap<String, String>();
		NodeList nl = doc.getElementsByTagName("requestValueType");
		for (int i = 0; i < nl.getLength(); i++) {
			Element ele = (Element) nl.item(i);
			String names = ele.getAttribute("Name");
			if (names == null || names.trim().length() == 0) {
				continue;
			}
			String paramType = ele.getAttribute("Type");
			if (paramType == null || paramType.trim().length() == 0) {
				paramType = "int";// 默认整数
			} else {
				paramType = paramType.trim().toLowerCase();
			}

			String[] names1 = names.split(",");
			for (int m = 0; m < names1.length; m++) {
				String paramName = names1[m].trim().toUpperCase();
				if (paramName.length() > 0) {
					RV_TYPES.put(paramName, paramType);
				}
			}
		}
	}

	/**
	 * 在ewa_conf.xml配置文件中获取参数
	 * 
	 * @throws Exception
	 */
	private static void initPathXml() {
		Document doc = CFG_XML_DOC;
		NodeList nl = doc.getElementsByTagName("path");
		for (int i = 0; i < nl.getLength(); i++) {
			Element ele = (Element) nl.item(i);
			String name = ele.hasAttribute("name") ? ele.getAttribute("name") : ele.getAttribute("Name");
			String v = ele.hasAttribute("value") ? ele.getAttribute("value") : ele.getAttribute("Value");
			if (!(v.endsWith("/") || v.endsWith("\\"))) {
				v += "/";
			}
			if (name.equals("config_path")) {
				PATH_CONFIG = v;
			} else if (name.equals("script_path")) {
				PATH_SCRIPT = v;
			} else if (name.equals("group_path")) {
				PATH_GROUP = v;
			} else if (name.equals("cached_path")) {
				PATH_CACHED = v;
			} else if (name.equalsIgnoreCase("cvt_office_home")) {
				UPath.CVT_OPENOFFICE_HOME = v;
			} else if (name.equalsIgnoreCase("cvt_swftool_Home")) {
				UPath.CVT_SWFTOOL_HOME = v;
			} else if (name.equalsIgnoreCase("cvt_ImageMagick_Home")) {
				UPath.CVT_IMAGEMAGICK_HOME = v;
				System.out.println(UPath.CVT_IMAGEMAGICK_HOME);
			} else if (name.equals("img_tmp_path")) { // 图片缩略图保存路径
				PATH_IMG_CACHE = v + "/" + PATH_TEMP + "/IMG/";
				if (PATH_IMG_CACHE.startsWith("@")) {
					UFile.buildPaths(PATH_IMG_CACHE.replace("@", ""));
				}
				PATH_IMG_CACHE = PATH_IMG_CACHE.replace("//", "/");
				PATH_UPLOAD = v + (v.endsWith("/") ? "" : "/");
			} else if (name.equals("img_tmp_path_url")) { // 图片缩略图URL
				PATH_IMG_CACHE_URL = v + "/" + PATH_TEMP + "/IMG/";
				// PATH_IMG_CACHE_URL = PATH_IMG_CACHE_URL.replace("//", "/");
				PATH_UPLOAD_URL = v + (v.endsWith("/") ? "" : "/");
			}

		}
		if (PATH_CACHED == null || PATH_CACHED.length() == 0) {
			PATH_CACHED = PATH_REAL + "/ewa_temp/cached/";
		}

		UPath.initSmtpParas(doc);

		DEBUG_IPS = new MTableStr();
		// 记录可以进行DEBUG的ip地址
		UPath.initDebugIps(doc);

		// Workflow
		nl = doc.getElementsByTagName("workflow");
		if (nl.getLength() > 0) {
			Element ele = (Element) nl.item(0);
			WF_XML = UXml.asXml(ele);
		}

		// 数据库连接池
		nl = doc.getElementsByTagName("databases");
		if (nl.getLength() > 0) {
			Element ele = (Element) nl.item(0);
			DATABASE_XML = UXml.asXml(ele);
		}

		// 初始化的参数 用户自定义
		INIT_PARAS = new MTableStr();
		nl = doc.getElementsByTagName("para");
		for (int i = 0; i < nl.getLength(); i++) {
			Element ele = (Element) nl.item(i);
			
			String n = ele.hasAttribute("Name") ? ele.getAttribute("Name") : ele.getAttribute("name");
			n = n.toUpperCase();
			
			String v = ele.hasAttribute("Value") ? ele.getAttribute("Value") : ele.getAttribute("value");

			if (INIT_PARAS.containsKey(n)) {
				INIT_PARAS.removeKey(n);
			} else {
				INIT_PARAS.put(n, v);
			}

		}

		// 加载到 RequestValue的全局变量
		UPath.initRequestValueGlobal(doc);

		UPath.initValidDomains(doc);

		UPath.initCfgCacheMethod(doc);

		// 初始化定义RequestValue的初始化类型，例如： USR_ID->int
		initRequestValuesType(doc);
	}

	/**
	 * 初始化 以进行DEBUG的ip地址
	 * 
	 * @param doc
	 */
	private static void initDebugIps(Document doc) {
		DEBUG_IPS = new MTableStr();
		// 记录可以进行DEBUG的ip地址
		NodeList nl = doc.getElementsByTagName("debug");
		if (nl.getLength() > 0) {
			Element ele = (Element) nl.item(0);
			String ips = ele.getAttribute("ips");
			if (ips.trim().length() > 0) {
				String[] ips1 = ips.split(",");
				for (int i = 0; i < ips1.length; i++) {
					DEBUG_IPS.add(ips1[i], ips1[i]);
				}
			}
			// 是否显示SQL执行情况 debug状态下使用
			String sqlout = ele.getAttribute("sqlout");
			if (sqlout != null && sqlout.equals("true")) {
				IS_DEBUG_SQL = true;
			} else {
				IS_DEBUG_SQL = false;
			}

		}
	}

	/**
	 * 配置文件缓存方式
	 * 
	 * @param doc
	 */
	private static void initCfgCacheMethod(Document doc) {
		// <!-- 配置文件缓存方式 memory / sqlcached -->
		// <!-- memory, 使用 java 内存 (默认)-->
		// <!-- sqlcached,利用 SqlCached 配置 -->
		// <cfgCacheMethod Value="sqlcached" />

		NodeList nl = doc.getElementsByTagName("cfgCacheMethod");
		CFG_CACHE_METHOD = "memory"; // 默认模式
		if (nl.getLength() > 0) {
			Element eleDes = (Element) nl.item(0);
			Map<String, String> vals = UXml.getElementAttributes(eleDes, true);
			String value = vals.get("value");
			if (value != null && value.trim().equalsIgnoreCase("sqlcached")) {
				CFG_CACHE_METHOD = "sqlcached";
			}
		}
	}

	private static void initValidDomains(Document doc) {
		VALID_DOMAINS = new MTableStr();
		NodeList nl = doc.getElementsByTagName("validDomains");
		for (int i = 0; i < nl.getLength(); i++) {
			Element ele = (Element) nl.item(i);
			String v = ele.getAttribute("Value");
			String[] vs = v.split(",");
			for (int m = 0; m < vs.length; m++) {
				String v2 = vs[m].trim().toLowerCase();
				if (v2.length() == 0) {
					continue;
				}
				VALID_DOMAINS.put(v2, v2);
			}
			String host = ele.getAttribute("Host");
			if (host != null && host.trim().length() > 0) {
				VALID_DOMAINS.put("___HOST___", host);
				System.out.println("combin-host:" + VALID_DOMAINS.get("___HOST___"));
			}
		}
	}

	/**
	 * RequestValue的全局变量
	 */
	private static void initRequestValueGlobal(Document doc) {
		// 加载到 RequestValue的全局变量
		RV_GLOBALS = new HashMap<String, String>();
		NodeList nl = doc.getElementsByTagName("rv");
		for (int i = 0; i < nl.getLength(); i++) {
			Element ele = (Element) nl.item(i);
			String n = ele.getAttribute("name");
			if (StringUtils.isBlank(n)) {
				continue;
			}
			n = n.toUpperCase().trim();
			String v = ele.getAttribute("value");
			if (RV_GLOBALS.containsKey(n)) {
				RV_GLOBALS.remove(n);
			} else {
				RV_GLOBALS.put(n, v);
			}

		}
	}

	/**
	 * 初始化SMTP参数
	 * 
	 * @param doc
	 */
	private static void initSmtpParas(Document doc) {
		try {
			SmtpCfgs.initCfgs(doc);
		} catch (Exception err) {
			LOG.error("initSmtpParas", err.getMessage());
		}
	}

	/**
	 * 获取初始化参数
	 * 
	 * @param name 初始化参数名称
	 * @return 初始化参数
	 */
	public static String getInitPara(String name) {
		if (name == null) {
			return null;
		}
		String name1 = name.trim().toUpperCase();
		if (INIT_PARAS.containsKey(name1)) {
			return INIT_PARAS.get(name1);
		} else {
			return null;
		}
	}

	/**
	 * 获取项目WebRoot所在的物理目录
	 * 
	 * @return WebRoot所在的物理目录
	 */
	public static String getRealContextPath() {
		String s1 = getRealPath();
		return s1.split("WEB-INF")[0];
	}

	/**
	 * 流程部门列表
	 * 
	 * @return the wF_DEPT
	 */
	public static String getWFXML() {
		initPath();
		return WF_XML;
	}

	/**
	 * 获取数据库连接池配置
	 * 
	 * @return the dATABASE_XML
	 */
	public static String getDATABASEXML() {
		initPath();
		return DATABASE_XML;
	}

	/**
	 * 获取可用进行DEBUG的IP地址
	 * 
	 * @return 可用进行DEBUG的IP地址
	 */
	public static MTableStr getDebugIps() {
		initPath();
		return DEBUG_IPS;
	}

	/**
	 * IS_DEBUG_SQL
	 * 
	 * @return IS_DEBUG_SQL
	 */
	public static boolean isDebugSql() {
		initPath();
		return IS_DEBUG_SQL;
	}

	/**
	 * 获取上传文件物理路径<br>
	 * Name="img_tmp_path"，如果ewa_conf中没有配置的话，则为当前WEB所住目录
	 * 
	 * @return 上传文件物理路径
	 */
	public static String getPATH_UPLOAD() {
		initPath();
		if (PATH_UPLOAD == null) {
			return null;
		}
		if (PATH_UPLOAD.startsWith("@")) {
			return PATH_UPLOAD.replace("@", "");
		} else {
			return UPath.getRealContextPath() + "/" + PATH_UPLOAD;
		}
	}

	/**
	 * des="图片缩略图保存根路径URL, ！！！需要在Tomcat或Apache或Nginx中配置虚拟路径！！！。" Name="img_tmp_path_url"，如果ewa_conf中没有配置的话，则为null
	 * 
	 * @return 上传文件的Url
	 */
	public static String getPATH_UPLOAD_URL() {
		initPath();
		if (PATH_UPLOAD_URL == null) {
			return null;
		} else {
			return UPath.PATH_UPLOAD_URL;
		}
	}

	/**
	 * 获取图片临时文件路径
	 * 
	 * @return 临时文件路径
	 */
	public static String getPATH_IMG_CACHE() {
		initPath();
		if (PATH_IMG_CACHE.startsWith("@")) {
			return PATH_IMG_CACHE.replace("@", "");
		} else {
			return UPath.getRealContextPath() + UPath.PATH_IMG_CACHE;
		}
	}

	/**
	 * des="图片缩略图保存根路径URL, ！！！需要在Tomcat或Apache或Nginx中配置虚拟路径！！！。"
	 * Name="img_tmp_path_url"，如果ewa_conf中没有配置的话，则取当前contextpath
	 * 
	 * @return 临时文件Url
	 */
	public static String getPATH_IMG_CACHE_URL() {
		initPath();
		if (PATH_IMG_CACHE_URL == null) {
			return PATH_IMG_CACHE;
		}
		return UPath.PATH_IMG_CACHE_URL;
	}

	/**
	 * 获取 配置文件缓存的模式，内存或sqlcached
	 * 
	 * @return 配置文件缓存的模式，内存或sqlcached
	 */
	public static String getCfgCacheMethod() {
		initPath();

		return UPath.CFG_CACHE_METHOD;
	}

	public static long getPropTime() {
		return PROP_TIME;
	}

	public static void main(String[] args) {
		UPath.mainCall = true;
		initPath();
	}

}
