package com.gdxsoft.easyweb.utils;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;

public class UFormat {

	private static String[] WEEEK_NAME_ZHCN = "日,一,二,三,四,五,六".split(",");
	private static String[] WEEEK_NAME_ENUS = "Sun,Mon,Tue,Wed,Thu,Fri,Sat".split(",");

	/**
	 * 格式化
	 * 
	 * @param toFormat 要转换的类型，大小写无关<br>
	 *                 1 date,dateTime,time,dateShortTime,shortTime,shortDate<br>
	 *                 2 age 年龄, int, Money <br>
	 *                 3 fixed2 保留2位小数<br>
	 *                 4 leastMoney 清除小数后的0<br>
	 *                 5 leastDecimal 清除小数后的0,没有逗号<br>
	 *                 6 percent 百分比格式<br>
	 *                 7 week 星期<br>
	 * @param oriValue 原始值
	 * @param lang     语言 enus 或 zhcn
	 * @return 格式化好的字符串
	 * @throws Exception
	 */
	public static String formatValue(String toFormat, Object oriValue, String lang) throws Exception {
		if (oriValue == null)
			return null;
		if (toFormat == null || toFormat.trim().length() == 0)
			return oriValue.toString();

		String f = toFormat.trim().toLowerCase();
		// 日期型
		if (f.indexOf("date") >= 0 || f.indexOf("time") >= 0) {
			return formatDate(toFormat, oriValue, lang);
		} else if (f.equals("age")) { // 年龄 当前年-出生年
			return formatAge(oriValue);
		} else if (f.equals("int")) {
			return formatInt(oriValue);
		} else if (f.equals("money")) {
			return formatMoney(oriValue);
		} else if (f.equals("fixed2")) { // 保留2位小数
			String m = formatMoney(oriValue);
			return m == null ? null : m.replaceAll(",", "");
		} else if (f.equals("leastmoney")) { // 清除小数后的0
			return formatNumberClearZero(oriValue);
		} else if (f.equals("leastdecimal")) {// 清除小数后的0,没有逗号
			return formatDecimalClearZero(oriValue);
		} else if (f.equals("percent")) {// jinzhaopeng 20121114 增加百分比格式
			return formatPercent(oriValue);
		} else if (f.equals("week")) {
			return formatWeek(oriValue, lang);
		}
		return oriValue.toString();
	}

	/**
	 * 返回年龄 当前年-出生年
	 * 
	 * @param dbo 出生日期
	 * @return 年龄
	 */
	public static String formatAge(Object dbo) {
		if (dbo == null)
			return null;
		Date t;
		String cName = dbo.getClass().getName().toUpperCase();
		// 日期型
		if (cName.indexOf("TIME") < 0 && cName.indexOf("DATE") < 0) {
			if (dbo.toString().length() < 10) {
				return dbo.toString();
			}
			String[] ss = dbo.toString().split(" ");
			t = Utils.getDate(ss[0]);
		} else {
			t = (Date) dbo;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(t);
		Calendar calToday = Calendar.getInstance();
		cal.setTime(t);

		int age = calToday.get(Calendar.YEAR) - cal.get(Calendar.YEAR);

		return age + "";
	}

	/**
	 * 格式化为星期
	 * 
	 * @param oriValue 字符串或日期
	 * @param lang     enus 或 zhcn
	 * @return 星期几
	 * @throws Exception
	 */
	public static String formatWeek(Object oriValue, String lang) throws Exception {
		if (oriValue == null)
			return null;

		String cName = oriValue.getClass().getName().toUpperCase();
		Date t;

		// 日期型
		if (cName.indexOf("TIME") < 0 && cName.indexOf("DATE") < 0) {
			t = Utils.getDate(oriValue.toString());
		} else {
			t = (Date) oriValue;
		}

		java.util.Calendar cal = java.util.Calendar.getInstance();
		cal.setTime(t);

		int wk = cal.get(java.util.Calendar.DAY_OF_WEEK) - 1;
		String[] wks = "enus".equals(lang) ? WEEEK_NAME_ENUS : WEEEK_NAME_ZHCN;

		return wks[wk];
	}

	/**
	 * 格式化日期
	 * 
	 * @param toFormat 格式 date,dateTime,time,dateShortTime,shortTime,shortDate，week
	 *                 大小写无关
	 * @param oriValue 来源数据，日期或字符型
	 * @param lang     语言 enus 或 zhcn
	 * @return 格式化后的字符
	 * @throws Exception
	 */
	public static String formatDate(String toFormat, Object oriValue, String lang) throws Exception {
		if (oriValue == null)
			return null;
		if (toFormat == null || toFormat.trim().length() == 0)
			return oriValue.toString();

		String f = toFormat.trim().toLowerCase();

		if ("week".equals(f)) {
			return formatWeek(oriValue, lang);
		}

		String cName = oriValue.getClass().getName().toUpperCase();

		String sDate = null;
		String sTime = null;
		String sDt = null;

		// 日期型
		if (cName.indexOf("TIME") < 0 && cName.indexOf("DATE") < 0) {
			if (oriValue.toString().length() < 10) {
				return oriValue.toString();
			}
			String[] ss = oriValue.toString().split(" ");
			sDate = ss[0];
			sTime = "";
			if (ss.length > 1) {
				sTime = ss[1];
			} else if (ss.length == 0 && ss[0].indexOf(":") > 0) {// 时间
				sDate = "";
				sTime = ss[0];
			}
			sDt = sDate + " " + sTime;
		} else {
			// 美国或中国
			String dateFormat = "enus".equals(lang) ? "MM/dd/yyyy" : "yyyy-MM-dd";

			Date t = (Date) oriValue;
			sDate = Utils.getDateString(t, dateFormat);
			sTime = Utils.getTimeString(t);
			sDt = sDate + " " + sTime;
		}
		// 日期格式
		if (f.equals("date")) {
			return sDate;
		}
		// 日期和时间格式
		if (f.equals("datetime")) {
			return sDt;
		}
		if (f.equals("time")) {
			return sTime;
		}

		String sTimeShort = sTime.lastIndexOf(":") > 0 ? sTime.substring(0, sTime.lastIndexOf(":")) : sTime;
		String sDateShort = sDate.substring(5);
		if (lang != null && lang.trim().equalsIgnoreCase("enus")) {
			sDateShort = sDate.substring(0, 5);
		}

		// 日期和短时间格式
		if (f.equals("dateshorttime")) {
			return sDate + " " + sTimeShort;
		}
		if (f.equals("shortdatetime")) {
			return sDateShort + " " + sTimeShort;
		}
		if (f.equals("shorttime")) {
			return sTimeShort;
		}
		if (f.equals("shortdate")) {
			return sDateShort;
		}
		return oriValue.toString();
	}

	/**
	 * 格式化为整型
	 * 
	 * @param oriValue 原始数据
	 * @return 整型字符串
	 */
	public static String formatInt(Object oriValue) {
		if (oriValue == null)
			return null;
		String v1 = oriValue.toString();
		String[] v2 = v1.split("\\.");
		return v2[0];
	}

	/**
	 * 格式化为货币型
	 * 
	 * @param oriValue 原始数据
	 * @return 小数点2位货币表达式
	 */
	public static String formatMoney(Object oriValue) {
		if (oriValue == null)
			return null;
		String sv = oriValue.toString();
		try {
			double number = Double.parseDouble(sv.replace(",", ""));

			NumberFormat numberFormat = NumberFormat.getNumberInstance();
			numberFormat.setMaximumFractionDigits(2);
			numberFormat.setMinimumFractionDigits(2);
			return numberFormat.format(number);
		} catch (Exception err) {
			return oriValue.toString();
		}
		/*
		 * String v1 = oriValue.toString(); String[] v2 = v1.split("\\."); //java.text.
		 * if (v2.length == 1) { return v2[0] + ".00"; } else { v1 =
		 * v2[0].trim().length() == 0 ? "0" : v2[0] + "."; String v3 = v2[1] + "0000";
		 * return v1 + v3.substring(0, 2); }
		 */
	}

	/**
	 * 格式为百分数
	 * 
	 * @param oriValue 原始数据
	 * @return 百分比
	 * @throws Exception
	 */
	public static String formatPercent(Object oriValue) throws Exception {
		if (oriValue == null)
			return null;
		double d1 = UConvert.ToDouble(oriValue.toString()) * 100;
		java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
		String v1 = df.format(d1) + "%";
		return v1;
	}

	/**
	 * 格式化为有逗号分隔的数字，并清除小数末尾的0，最多保留4位小数
	 * 
	 * @param oriValue 数据
	 * @return 格式化为有逗号分隔的数字，并清除小数末尾的0，最多保留4位小数
	 * @throws Exception
	 */
	public static String formatNumberClearZero(Object oriValue) throws Exception {
		if (oriValue == null) {
			return null;
		}
		try {
			double d1 = Double.parseDouble(oriValue.toString());
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMinimumFractionDigits(0);
			nf.setMaximumFractionDigits(4);
			return nf.format(d1);
		} catch (Exception err) {
			return oriValue.toString();
		}
	}

	/**
	 * 清除小数末尾的0，最多保留4位小数
	 * 
	 * @param oriValue 数据
	 * @return 清除小数末尾的0，最多保留4位小数
	 * @throws Exception
	 */
	public static String formatDecimalClearZero(Object oriValue) throws Exception {
		if (oriValue == null) {
			return null;
		}
		try {
			double d1 = Double.parseDouble(oriValue.toString());
			java.text.DecimalFormat df = new java.text.DecimalFormat("#.####");
			return df.format(d1);
		} catch (Exception err) {
			return oriValue.toString();
		}
	}
}
