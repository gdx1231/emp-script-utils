package com.gdxsoft.easyweb.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

public class UHtml {

	private static String CACHE_FILE;
	public static Map<Integer, Integer> MAP_COMBINE_FILES;

	static {
		CACHE_FILE = UPath.getCachedPath() + "/uhtml_cached.json";
		MAP_COMBINE_FILES = new ConcurrentHashMap<Integer, Integer>();
		// System.out.println(CACHE_FILE);
		try {
			String cnt = UFile.readFileText(CACHE_FILE);
			JSONArray obj = new JSONArray(cnt);
			for (int i = 0; i < obj.length(); i++) {
				JSONObject o = obj.getJSONObject(i);
				int id = o.optInt("id");
				int code = o.optInt("code");

				MAP_COMBINE_FILES.put(id, code);
			}
		} catch (Exception e) {

		}
	}

	/**
	 * 将单元文件进行合并，例如js文件或css文件等
	 * 
	 * @param path            路径
	 * @param ext             扩展名
	 * @param savePathAndName 保存路径
	 * @return 0表示目录不存在
	 * @throws Exception
	 */
	public static int combineFiles(String path, String ext, String savePathAndName) throws Exception {
		File f1 = new File(path);
		if (!f1.exists()) {
			return 0;
		}

		int file_code = f1.getAbsoluteFile().hashCode();

		File[] fs = f1.listFiles();
		// 将所有子文件的名称和修改时间拼接成字符串，获取hashcode
		// 作为比较的依据
		StringBuilder sbf = new StringBuilder();
		ArrayList<String> fs1 = new ArrayList<String>();
		String ext1 = ext.toUpperCase();
		for (int i = 0; i < fs.length; i++) {
			File f = fs[i];
			if (f.isFile() && f.getName().toUpperCase().endsWith(ext1)) {
				sbf.append("|");
				sbf.append(f.getName());
				sbf.append(f.lastModified());
				fs1.add(f.getAbsolutePath());
			}
		}
		// 没有合适的文件
		if (fs1.size() == 0) {
			return 0;
		}
		int hcode = sbf.toString().hashCode();
		int code_old = -1;
		if (MAP_COMBINE_FILES.containsKey(file_code)) {
			code_old = MAP_COMBINE_FILES.get(file_code);
		}
		if (code_old != hcode) {
			StringBuilder sbc = new StringBuilder();
			for (int i = 0; i < fs1.size(); i++) {
				String f = fs1.get(i);
				String text = UFile.readFileText(f);
				sbc.append("\n/* ");
				sbc.append(f);
				sbc.append(" */\n");
				sbc.append(text);
			}
			UFile.createNewTextFile(savePathAndName, sbc.toString());
			MAP_COMBINE_FILES.put(file_code, hcode);
			saveCahcedFile();
		}
		return hcode;
	}

	private static void saveCahcedFile() {
		JSONArray obj = new JSONArray();
		for (Integer id : MAP_COMBINE_FILES.keySet()) {
			int code = MAP_COMBINE_FILES.get(id);
			JSONObject o = new JSONObject();
			o.put("id", id);
			o.put("code", code);
			obj.put(o);
		}
		try {
			UFile.createNewTextFile(CACHE_FILE, obj.toString());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Get http body content
	 * 
	 * @param request HttpServletRequest (javax)
	 * @return the http body string(UTF8)
	 * @throws IOException
	 */
	public static String getHttpBody(javax.servlet.http.HttpServletRequest request) throws IOException {
		InputStream is = request.getInputStream();
		String str = getHttpBody(is);
		return str;
	}

	/**
	 * Get http body content (jakarta, TOMCAT10)
	 * 
	 * @param request HttpServletRequest (jakarta)
	 * @return the http body string(UTF8)
	 * @throws IOException
	 */
	/*
	 * public static String getHttpBody(jakarta.servlet.http.HttpServletRequest request) throws IOException {
	 * InputStream is = request.getInputStream(); String str = getHttpBody(is); return str; }
	 */
	private static String getHttpBody(InputStream is) throws IOException {
		byte[] bytes = new byte[1024 * 1024];
		int nRead = 1;
		int nTotalRead = 0;
		while (nRead > 0) {
			nRead = is.read(bytes, nTotalRead, bytes.length - nTotalRead);
			if (nRead > 0)
				nTotalRead = nTotalRead + nRead;
		}
		String str = new String(bytes, 0, nTotalRead, "utf-8");
		// System.out.println("Str:" + str);
		return str;
	}

	/**
	 * Get the base url from the HttpServletRequest
	 * 
	 * @param request the HttpServletRequest
	 * @return the base url
	 */
	public static String getHttpBase(javax.servlet.http.HttpServletRequest request) {
		String port = ":" + request.getServerPort();
		String scheme = request.getHeader("x-forwarded-protocol");
		if (scheme == null) {
			scheme = request.getScheme();
		}
		if (request.getServerPort() == 80 || request.getServerPort() == 443) {
			port = "";
		}

		String ctx = request.getContextPath();
		int inc = 0;
		// 避免 http://gezz.cn/////////ex/grd.jsp?js_debug=1 情况出现
		while (ctx.startsWith("//")) {
			ctx = ctx.replace("//", "/");
			inc++;
			if (inc > 500) {
				break;
			}
		}
		String __base = "//" + request.getServerName() + port + "" + ctx;
		return __base;
	}

	/**
	 * Get the base url from the HttpServletRequest
	 * 
	 * @param request the HttpServletRequest
	 * @param baseAdd the path of attachment
	 * @return the base url
	 */
	public static String getHttpBase(javax.servlet.http.HttpServletRequest request, String baseAdd) {

		String __base = getHttpBase(request);
		if (baseAdd != null) {
			__base = __base + "/" + baseAdd;
		}
		return __base;
	}

	/* 
	 * Get request base url from HttpServletRequest (Jakarta TOMCAT10)
	 * 
	 * @param request the HttpServletRequest
	 * @return the base url
	 */
	/*
	 * public static String getHttpBase(jakarta.servlet.http.HttpServletRequest request) { String port = ":" +
	 * request.getServerPort(); String scheme = request.getHeader("x-forwarded-protocol"); if (scheme == null) { scheme
	 * = request.getScheme(); } if (request.getServerPort() == 80 || request.getServerPort() == 443) { port = ""; }
	 * 
	 * String ctx = request.getContextPath(); int inc = 0; while (ctx.startsWith("//")) { ctx = ctx.replace("//", "/");
	 * inc++; if (inc > 500) { break; } } String __base = "//" + request.getServerName() + port + "" + ctx; return
	 * __base; }
	 */

	/* 
	 * Get request base (Jakarta TOMCAT10)
	 * 
	 * @param request the HttpServletRequest
	 * @param baseAdd the path of attachment
	 * @return the base url
	 */
	/*
	 * public static String getHttpBase(jakarta.servlet.http.HttpServletRequest request, String baseAdd) { String __base
	 * = getHttpBase(request); if (baseAdd != null) { __base = __base + "/" + baseAdd; } return __base; }
	 */

	public static String htmlETag() {
		return "";
	}

	/**
	 * 去除html的 on事件 例如 onclick,onmousedown ..., 用于Html编辑器粘贴后去除
	 * 
	 * @param html
	 * @return 去除html的 on事件 例如 onclick,onmousedown ..., 用于Html编辑器粘贴后去除
	 */
	public static String removeHtmlEvents(String html) {
		if (html == null || html.length() == 0) {
			return html;
		}
		String regex = "<\\w+.*(on\\w+)";
		Pattern patternForTag = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher mat = patternForTag.matcher(html);
		try {
			while (mat.find()) {
				MatchResult mr = mat.toMatchResult();
				html = html.replace(mr.group(1), "_gdx_");
			}
		} catch (Exception err) {
			System.out.println(err.getMessage());
		}

		// String regex1 = "<a\\b.*\\b(href)";
		// Pattern patternForTag1 = Pattern.compile(regex1,
		// Pattern.CASE_INSENSITIVE);
		// Matcher mat1 = patternForTag1.matcher(html);
		// try {
		// while (mat1.find()) {
		// MatchResult mr = mat1.toMatchResult();
		// html = html.replace(mr.group(1), "_gdx_");
		// }
		// } catch (Exception err) {
		// System.out.println(err.getMessage());
		// }

		return html;
	}

	/**
	 * 去除html标签属性(保留图片的src路径)
	 * 
	 * @param html
	 * @return 去除html标签属性(保留图片的src路径)
	 */
	public static String removeHtmlAttributes(String html) {
		if (html == null || html.length() == 0) {
			return html;
		}

		String regex4 = "<\\w+\\b([^>]*)>";
		Pattern patternForTag4 = Pattern.compile(regex4, Pattern.CASE_INSENSITIVE);
		Matcher mat4 = patternForTag4.matcher(html);
		try {
			while (mat4.find()) {
				MatchResult mr = mat4.toMatchResult();
				if (mr.group().toUpperCase().indexOf("<IMG") >= 0) {
					continue;
				}
				if (mr.group(1).length() > 0)
					html = html.replace(mr.group(1), "");
			}
		} catch (Exception err) {
			System.out.println(err.getMessage());
		}

		return html;
	}

	/**
	 * Remote the html tag
	 * 
	 * @param html    the html string
	 * @param tagName the tag to delete (iframe, script ...)
	 * @return the result
	 */
	public static String removeHtmlTag(String html, String tagName) {
		if (html == null || html.length() == 0) {
			return html;
		}
		String regex2 = "<" + tagName + "[^>]*>[^<]*</" + tagName + "[^>]*>";
		Pattern patternForTag2 = Pattern.compile(regex2, Pattern.CASE_INSENSITIVE);
		Matcher mat2 = patternForTag2.matcher(html);
		try {
			while (mat2.find()) {
				MatchResult mr = mat2.toMatchResult();
				html = html.replace(mr.group(), "");
			}
		} catch (Exception err) {
			System.out.println(err.getMessage());
		}

		String regex3 = "<" + tagName + ".*/>";
		Pattern patternForTag3 = Pattern.compile(regex3, Pattern.CASE_INSENSITIVE);
		Matcher mat3 = patternForTag3.matcher(html);
		try {
			while (mat3.find()) {
				MatchResult mr = mat3.toMatchResult();
				html = html.replace(mr.group(), "");
			}
		} catch (Exception err) {
			System.out.println(err.getMessage());
		}
		return html;
	}

	/**
	 * Create a pagination div
	 * 
	 * @param iCurPage      the current page
	 * @param iPageSize     the page size
	 * @param iTotalRecords the total records
	 * @param pageUrlRoot   the page url (../bbs/xxx/yy/{EXP}.html) the {EXP} is the
	 *                      page number replacement
	 * @return the pagination html
	 */
	public static String createListSplit(int iCurPage, int iPageSize, int iTotalRecords, String pageUrlRoot) {
		if (iTotalRecords <= iPageSize) {
			return "";
		}
		int ipages = iTotalRecords / iPageSize;
		int left = iTotalRecords % iPageSize;
		if (left > 0) {
			ipages++;
		}
		int start = iCurPage - 5;
		if (start <= 0) {
			start = 1;
		}
		int end = start + 10;
		if (end >= ipages) {
			end = ipages;
		}
		if (10 - (end - start) > 0) {
			start = start - (10 - (end - start));
		}
		pageUrlRoot = Utils.textToHtml(pageUrlRoot);

		StringBuilder sbPages = new StringBuilder();
		sbPages.append("<div class=\"ewa-pages\"><ul >");
		if (iCurPage != 1) {
			sbPages.append("<li><a href=\"" + pageUrlRoot.replace("{EXP}", (iCurPage - 1) + "") + "\"> &lt; </a></li>");
		}
		if (start > 1) {
			sbPages.append("<li><a href=\"" + pageUrlRoot.replace("{EXP}", 1 + "") + "\">" + (1) + "...</a></li>");
		}
		for (int i = start; i < end; i++) {
			if (i < 1) {
				continue;
			}
			if (i == iCurPage) {
				sbPages.append("<li><span>" + (iCurPage) + "</span></li>");
			} else {
				sbPages.append("<li><a href=\"" + pageUrlRoot.replace("{EXP}", i + "") + "\">" + (i) + "</a></li>");
			}
		}

		if (iCurPage <= ipages) {
			sbPages.append(
					"<li><a href=\"" + pageUrlRoot.replace("{EXP}", ipages + "") + "\">..." + (ipages) + "</a></li>");
		}
		if (iCurPage != ipages) {
			sbPages.append("<li><a href=\"" + pageUrlRoot.replace("{EXP}", (iCurPage + 1) + "") + "\"> &gt; </a></li>");
		}
		if (iCurPage == ipages) {
			sbPages.append("<li><span>" + (ipages) + "</span></li>");
		}
		sbPages.append("<div class='last'></div></ul></div>");

		return sbPages.toString();
	}

	/**
	 * Create a pagination table
	 * 
	 * @param iCurPage      the current page
	 * @param iPageSize     the page size
	 * @param iTotalRecords the total records
	 * @param pageUrlRoot   the page url (../bbs/xxx/yy/{EXP}.html) the {EXP} is the
	 *                      page number replacement
	 * @return the pagination html(table)
	 */
	public static String createListSplitTable(int iCurPage, int iPageSize, int iTotalRecords, String pageUrlRoot) {
		if (iTotalRecords <= iPageSize) {
			return "";
		}
		int ipages = iTotalRecords / iPageSize;
		int left = iTotalRecords % iPageSize;
		if (left > 0) {
			ipages++;
		}
		int start = iCurPage - 5;
		if (start <= 0) {
			start = 1;
		}
		int end = start + 10;
		if (end >= ipages) {
			end = ipages;
		}
		if (10 - (end - start) > 0) {
			start = start - (10 - (end - start));
		}
		pageUrlRoot = Utils.textToHtml(pageUrlRoot);

		StringBuilder sbPages = new StringBuilder();
		sbPages.append("<table cellpadding=4 cellspacing=4 class=\"ewa-pages\"><tr>");
		if (iCurPage != 1) {
			sbPages.append("<td><a href=\"" + pageUrlRoot.replace("{EXP}", (iCurPage - 1) + "") + "\"> &lt; </a></td>");
		}
		if (start > 1) {
			sbPages.append("<td><a href=\"" + pageUrlRoot.replace("{EXP}", 1 + "") + "\">" + (1) + "...</a></td>");
		}
		for (int i = start; i < end; i++) {
			if (i < 1) {
				continue;
			}
			if (i == iCurPage) {
				sbPages.append("<td><span>" + (iCurPage) + "</span></td>");
			} else {
				sbPages.append("<td><a href=\"" + pageUrlRoot.replace("{EXP}", i + "") + "\">" + (i) + "</a></td>");
			}
		}

		if (iCurPage < ipages) {
			sbPages.append(
					"<td><a href=\"" + pageUrlRoot.replace("{EXP}", ipages + "") + "\">..." + (ipages) + "</a></td>");
		}
		if (iCurPage == ipages) {
			sbPages.append("<td><span>" + (ipages) + "</span></td>");
		}
		if (iCurPage != ipages) {
			sbPages.append("<td><a href=\"" + pageUrlRoot.replace("{EXP}", (iCurPage + 1) + "") + "\"> &gt; </a></td>");
		}
		sbPages.append("</tr></table>");

		return sbPages.toString();
	}
}
