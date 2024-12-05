package com.gdxsoft.easyweb.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UPinYin {
	//拼音库
	private final static String PINYIN = "/pinyin/pinyin.txt";
	//多音字库
	private final static String M_PINYIN = "/pinyin/multi.txt";
	//繁体中文-简体中文对照表
	private final static String TRADITIONAL = "/pinyin/traditional.txt";
	/**
	 * 拼音库
	 */
	private static Map<String, String> MAP_PY = new ConcurrentHashMap<>();
	/**
	 * 多音字库
	 */
	private static Map<String, String> MAP_MPY = new ConcurrentHashMap<>();
	
	/**
	 * 繁体中文-简体中文对照表
	 */
	private static Map<String, String> MAP_TRADITIONAL = new ConcurrentHashMap<>();
	

	private static Map<String, String> MAP_MARKED_VOWEL = new ConcurrentHashMap<>();

	private static final String PINYIN_SEPARATOR = ","; // 拼音分隔符
	private static final String ALL_UNMARKED_VOWEL = "aeiouv";
	private static final String ALL_MARKED_VOWEL = "āáǎàēéěèīíǐìōóǒòūúǔùǖǘǚǜ"; // 所有带声调的拼音字母
	private static final String CHINESE_REGEX = "[\\u4e00-\\u9fa5]";

	static {
		for (int i = ALL_MARKED_VOWEL.length() - 1; i >= 0; i--) {
			char originalChar = ALL_MARKED_VOWEL.charAt(i);
			char replaceChar = ALL_UNMARKED_VOWEL.charAt((i - i % 4) / 4);
			MAP_MARKED_VOWEL.put(String.valueOf(originalChar), String.valueOf(replaceChar));
			MAP_MARKED_VOWEL.put("ü", "v");
		}
		loadTable(PINYIN, MAP_PY);
		loadTable(M_PINYIN, MAP_MPY);
		loadTable(TRADITIONAL, MAP_TRADITIONAL);
		
	}

	/**
	 * 将字符串转换成相应格式的拼音
	 * 
	 * @param str          需要转换的字符串
	 * @param separator    拼音分隔符
	 * @param pinyinFormat 拼音格式：WITH_TONE_NUMBER--数字代表声调，WITHOUT_TONE--不带声调，WITH_TONE_MARK--带声调
	 * @return 字符串的拼音
	 * @throws PinyinException
	 */
	public static String convertToPinyinString(String str) throws Exception {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		int strLen = str.length();
		while (i < strLen) {
			char c = str.charAt(i);
			// 判断是否为汉字或者〇
			if (isChinese(c)) {
				// 获取汉字拼音
				String pinyin = MAP_PY.get(String.valueOf(c));
				if (pinyin == null) {
					pinyin = "";
				}
				sb.append(pinyin);
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static String[] convertToMPinyinArr(String str) {
		if (MAP_MPY.containsKey(str)) {
			return MAP_MPY.get(str).split(PINYIN_SEPARATOR);
		} else {
			return null;
		}
	}
	
	private static char convertToSimpleChinese(char c) {
		String key = String.valueOf(c);
		if (MAP_TRADITIONAL.containsKey(key)) {
			return MAP_TRADITIONAL.get(key).charAt(0);
		}
		return c;
	}

	public static List<String> convertToPinyinArr(String str) throws Exception {
		List<String> pinyins = new ArrayList<String>();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (!isChinese(c)) {
				pinyins.add(String.valueOf(c));
				continue;
			}
			c = convertToSimpleChinese(c);
			
			StringBuilder msb = new StringBuilder();
			msb.append(c);
			boolean isMpy = false;
			for (int m = i + 1; m < str.length(); m++) {
				char c2 = str.charAt(m);
				if (!isChinese(c2)) {
					break;
				}
				msb.append(c2);
				String[] mpys = convertToMPinyinArr(msb.toString());
				if (mpys != null) {
					for (String mp : mpys) {
						pinyins.add(mp);
					}
					i = m;
					isMpy = true;
					break;
				}
			}
			if (!isMpy) {
				// 获取汉字拼音
				String pinyin = convertToPinyin(c);
				pinyins.add(pinyin);
			}
		}
		return pinyins;
	}

	public static String convertToPinyin(char c) throws Exception {
		// 判断是否为汉字
		if (isChinese(c)) {
			// 获取汉字拼音
			String pinyin = MAP_PY.get(String.valueOf(c));
			if (pinyin == null) {
				pinyin = "";
			}
			return pinyin.split(PINYIN_SEPARATOR)[0];
		} else {
			return String.valueOf(c);
		}
	}

	/**
	 * 将带声调格式的拼音转换为不带声调格式的拼音
	 * 
	 * @param str 字符串
	 * @return 不带声调的拼音
	 * @throws Exception
	 */
	public static List<String> convertWithoutTone(String str) throws Exception {
		List<String> pinyins = convertToPinyinArr(str);
		List<String> result = new ArrayList<String>();

		for (int i = 0; i < pinyins.size(); i++) {
			String pinyin = pinyins.get(i);
			for (String key : MAP_MARKED_VOWEL.keySet()) {
				pinyin = pinyin.replace(key, MAP_MARKED_VOWEL.get(key));
			}
			result.add(pinyin);
		}

		return result;
	}
	/**
	 * 将带声调格式的拼音转换为不带声调格式的拼音字符串，用,分隔
	 * @param str 字符串
	 * @return 不带声调格式的拼音字符串，用,分隔
	 * @throws Exception
	 */
	public static String convertWithoutToneString(String str) throws Exception {
		List<String> pinyins = convertToPinyinArr(str);
		StringBuilder result = new StringBuilder();

		for (int i = 0; i < pinyins.size(); i++) {
			String pinyin = pinyins.get(i);
			for (String key : MAP_MARKED_VOWEL.keySet()) {
				pinyin = pinyin.replace(key, MAP_MARKED_VOWEL.get(key));
			}
			if(i>0) {
				result.append(PINYIN_SEPARATOR);
			}
			result.append(pinyin);
		}

		return result.toString();
	}

	private static void loadTable(String classpath, Map<String, String> map) {
		InputStream is = UPinYin.class.getResourceAsStream(classpath);
		try {

			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] arr = line.split("=");
				if (arr.length != 2) {
					continue;
				}
				map.put(arr[0], arr[1]);
			}
			br.close();
		} catch (IOException e) {

		}
	}

	/**
	 * 判断某个字符是否为汉字
	 * 
	 * @param c 需要判断的字符
	 * @return 是汉字返回true，否则返回false
	 */
	public static boolean isChinese(char c) {
		return '〇' == c || String.valueOf(c).matches(CHINESE_REGEX);
	}

}
