package com.gdxsoft.easyweb.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.msnet.MStr;

public class USign {

	/**
	 * 按照Map字段（Key）顺序拼接字符串 a=1&amp;b=31&amp;c=0
	 * 
	 * @param map
	 * @param skipBlankValue 是否忽略空值null or blank
	 * @return
	 */
	public static String concatSortedStr(Map<String, ?> map, boolean skipBlankValue) {
		ArrayList<String> names = new ArrayList<String>();
		map.forEach((key, value) -> {
			names.add(key);
		});
		String[] names1 = new String[names.size()];
		names.toArray(names1);
		Arrays.sort(names1);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < names1.length; i++) {
			Object val = map.get(names1[i]);
			if (val == null) {
				if (skipBlankValue) {
					continue;
				}
				val = "";
			}
			String v1 = val.toString().trim();
			if (v1.length() == 0) {
				if (skipBlankValue) {
					continue;
				}
			}
			if (sb.length() > 0) {
				sb.append("&");
			}
			sb.append(names1[i]);
			sb.append("=");
			sb.append(v1);
		}
		return sb.toString();
	}

	/**
	 * 按照JSONObject 字段（Key）顺序拼接字符串 a=1&amp;b=31&amp;c=0
	 * 
	 * @param json
	 * @param skipBlankValue 是否忽略空值null or blank
	 * @return
	 */
	public static String concatSortedStr(JSONObject json, boolean skipBlankValue) {
		Iterator<?> keys = json.keys();
		ArrayList<String> names = new ArrayList<String>();
		while (keys.hasNext()) {
			String key = keys.next().toString();
			names.add(key);
		}

		String[] names1 = new String[names.size()];
		names.toArray(names1);
		Arrays.sort(names1);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < names1.length; i++) {
			Object val = json.get(names1[i]);
			if (val == null) {
				if (skipBlankValue) {
					continue;
				}
				val = "";
			}
			String v1 = val.toString().trim();
			if (v1.length() == 0) {
				if (skipBlankValue) {
					continue;
				}
			}
			sb.append(names1[i]);
			sb.append("=");
			sb.append(v1);
		}
		return sb.toString();
	}

	/**
	 * 按照顺序拼接 Document的字节的的tag和innerText a=1&amp;b=31&amp;c=0
	 * 
	 * @param doc
	 * @param skipBlankValue 是否忽略空值null or blank
	 * @return
	 */
	public static String concatSortedStr(Document doc, boolean skipBlankValue) {
		NodeList nl = doc.getFirstChild().getChildNodes();
		ArrayList<String> names = new ArrayList<String>();

		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeType() == Element.ELEMENT_NODE && n.getNodeName() != null) {
				names.add(n.getNodeName());
			}
		}
		String[] list = new String[names.size()];
		names.toArray(list);

		Arrays.sort(list);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list.length; i++) {
			String name = list[i];
			if (name.equals("sign")) { // sign不作为签名字符串
				continue;
			}
			String val = doc.getElementsByTagName(name).item(0).getTextContent();
			if (val == null) {
				if (skipBlankValue) {
					continue;
				}
				val = "";
			}
			String v1 = val.toString().trim();
			if (v1.length() == 0) {
				if (skipBlankValue) {
					continue;
				}
			}

			if (sb.length() > 0) {
				sb.append("&");
			}
			sb.append(name);
			sb.append("=");
			sb.append(val);
		}
		return sb.toString();
	}

	/**
	 * 按照顺序JSON对象的字符串，签名的Md5
	 * 
	 * @param json    需要签名的JSON对象
	 * @param keyName 最后拼接的签名名称
	 * @param keyVal  最后拼接的签名值
	 * @return Md5
	 */
	public static String signMd5(JSONObject json, String keyName, String keyVal) {
		StringBuilder sb = new StringBuilder(concatSortedStr(json, true));

		sb.append("&");
		sb.append(keyName);
		sb.append("=");
		sb.append(keyVal);

		return Utils.md5(sb.toString());
	}

	/**
	 * 按照顺序JSON对象的字符串，签名的 sha1
	 * 
	 * @param json    需要签名的JSON对象
	 * @param keyName 最后拼接的签名名称
	 * @param keyVal  最后拼接的签名值
	 * @return sha1
	 */
	public static String signSha1(JSONObject json, String keyName, String keyVal) {
		StringBuilder sb = new StringBuilder(concatSortedStr(json, true));

		sb.append("&");
		sb.append(keyName);
		sb.append("=");
		sb.append(keyVal);

		return Utils.sha1(sb.toString());
	}

	/**
	 * 按照顺序Map对象的字符串，签名的Md5
	 * 
	 * @param parameters 需要签名的Map对象
	 * @param keyName    最后拼接的签名名称
	 * @param keyVal     最后拼接的签名值
	 * @return md5
	 */
	public static String signMd5(Map<String, ?> parameters, String keyName, String keyVal,
			boolean skipBlankValue) {
		StringBuilder sb = new StringBuilder(concatSortedStr(parameters, skipBlankValue));

		sb.append("&");
		sb.append(keyName);
		sb.append("=");
		sb.append(keyVal);

		return Utils.md5(sb.toString());
	}

	/**
	 * 按照顺序Map对象的字符串，签名的sha1
	 * 
	 * @param parameters 需要签名的Map对象
	 * @param keyName    最后拼接的签名名称
	 * @param keyVal     最后拼接的签名值
	 * @return sha1
	 */
	public static String signSha1(Map<String, ?> parameters, String keyName, String keyVal,
			boolean skipBlankValue) {
		StringBuilder sb = new StringBuilder(concatSortedStr(parameters, skipBlankValue));

		sb.append("&");
		sb.append(keyName);
		sb.append("=");
		sb.append(keyVal);

		return Utils.sha1(sb.toString());
	}

	/**
	 * 按照顺序XML Document对象的字符串，签名的Md5
	 * 
	 * @param doc     需要签名的Document对象
	 * @param keyName 最后拼接的签名名称
	 * @param keyVal  最后拼接的签名值
	 * @return Md5
	 */
	public static String signMd5(Document doc, String keyName, String keyVal, boolean skipBlankValue) {

		StringBuilder sb = new StringBuilder(USign.concatSortedStr(doc, skipBlankValue));

		sb.append("&");
		sb.append(keyName);
		sb.append("=");
		sb.append(keyVal);

		return Utils.md5(sb.toString());
	}

	/**
	 * 按照顺序XML Document对象的字符串，签名的sha1
	 * 
	 * @param doc     需要签名的Document对象
	 * @param keyName 最后拼接的签名名称
	 * @param keyVal  最后拼接的签 名值
	 * @return sha1
	 */
	public static String signSha1(Document doc, String keyName, String keyVal, boolean skipBlankValue) {

		StringBuilder sb = new StringBuilder(USign.concatSortedStr(doc, skipBlankValue));

		sb.append("&");
		sb.append(keyName);
		sb.append("=");
		sb.append(keyVal);

		return Utils.sha1(sb.toString());
	}

	/**
	 * 在Document下添加 节点
	 * 
	 * @param doc       Document
	 * @param nodeName  创建的Node节点的tagName
	 * @param innerText node内文字
	 */
	public static void addXmlNode(Document doc, String tagName, String innerText) {
		Element ele = doc.createElement(tagName);
		// CDATASection eleCdata = doc.createCDATASection(text);
		// ele.appendChild(eleCdata);
		if (innerText != null) {
			ele.setTextContent(innerText);
			doc.getFirstChild().appendChild(ele);
		}
	}

	/**
	 * 将数字前导为0，例如 123生成00000123
	 * 
	 * @param number 数字
	 * @param maxLen 合成的字符长度
	 * @return
	 */
	public static String fixNumberWithZero(int number, int maxLen) {
		MStr sb = new MStr("0000000000000000000000000000000000000000");
		while (maxLen > sb.length()) {
			sb.append(sb.toString());
		}
		sb.append(number);
		int len = sb.length();
		return sb.toString().substring(len - maxLen, len);
	}
}
