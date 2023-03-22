package com.gdxsoft.easyweb.utils;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;

public class UFormat {

	private static final String[] WEEEK_NAME_ZHCN = "日,一,二,三,四,五,六".split(",");
	private static final String[] WEEEK_NAME_ENUS = "Sun,Mon,Tue,Wed,Thu,Fri,Sat".split(",");

	/**
	 * 英式日期表达式
	 */
	public static final String DATE_FROMAT_UK = "dd/MM/yyyy";
	/**
	 * 美式日期表达式
	 */
	public static final String DATE_FROMAT_US = "MM/dd/yyyy";

	public static String DATE_FROMAT_ZHCN = "yyyy-MM-dd";
	public static String DATE_FROMAT_ENUS = DATE_FROMAT_US; // 默认美式表示法

	/**
	 * 数字转汉字大写金额<br>
	 * 参考: https://blog.csdn.net/weixin_42333548/article/details/124662824
	 * 
	 * @param n 数字
	 * @return
	 */
	public static String formatChineseMoney(double n) {
		String fraction[] = { "角", "分" };
		String digit[] = { "零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖" };
		String unit[][] = { { "元", "万", "亿" }, { "", "拾", "佰", "仟" } };

		String head = n < 0 ? "负" : "";
		n = Math.abs(n);

		String s = "";
		for (int i = 0; i < fraction.length; i++) {
			s += (digit[(int) (Math.floor(n * 10 * Math.pow(10, i)) % 10)] + fraction[i]).replaceAll("(零.)+", "");
		}
		if (s.length() < 1) {
			s = "整";
		}
		int integerPart = (int) Math.floor(n);
		for (int i = 0; i < unit[0].length && integerPart > 0; i++) {
			String p = "";
			for (int j = 0; j < unit[1].length && n > 0; j++) {
				p = digit[integerPart % 10] + unit[1][j] + p;
				integerPart = integerPart / 10;
			}
			s = p.replaceAll("(零.)*零$", "").replaceAll("^$", "零") + unit[0][i] + s;
		}
		return head + s.replaceAll("(零.)*零元", "元").replaceFirst("(零.)+", "").replaceAll("(零.)+", "零").replaceAll("^整$",
				"零元整");
	}

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
	 *                 8 ChineseMoney 中文货币
	 * @param oriValue 原始值
	 * @param lang     语言 enus 或 zhcn
	 * @return 格式化好的字符串
	 * @throws Exception
	 */
	public static String formatValue(String toFormat, Object oriValue, String lang) throws Exception {
		if (oriValue == null)
			return null;
		if (toFormat == null || toFormat.trim().length() == 0)
			return objectToString(oriValue);

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
		} else if ("ChineseMoney".equalsIgnoreCase(f)) {
			// 中文货币
			try {
				double v = UConvert.ToDouble(oriValue);
				return formatChineseMoney(v);
			} catch (Exception err) {
				return oriValue.toString();
			}
		}
		return objectToString(oriValue);
	}

	/**
	 * 对象转字符串, Array通过", "拼接，用于处理ClickHouse的数组对象
	 * 
	 * @param oriValue 原始对象
	 * @return Array=a, b, c
	 */
	public static String objectToString(Object oriValue) {
		if (oriValue == null)
			return null;

		if (oriValue.getClass().isArray()) {
			StringBuilder sb = new StringBuilder();
			if (oriValue.getClass().getComponentType().isPrimitive()) {
				String ct = oriValue.getClass().getComponentType().getSimpleName();
				if (ct.equals("int")) {
					int[] arr = (int[]) oriValue;
					for (int i = 0; i < arr.length; i++) {
						int item = arr[i];
						if (i > 0) {
							sb.append(", ");
						}
						sb.append(item);
					}
				} else if (ct.equals("long")) {
					long[] arr = (long[]) oriValue;
					for (int i = 0; i < arr.length; i++) {
						long item = arr[i];
						if (i > 0) {
							sb.append(", ");
						}
						sb.append(item);
					}
				} else if (ct.equals("short")) {
					short[] arr = (short[]) oriValue;
					for (int i = 0; i < arr.length; i++) {
						long item = arr[i];
						if (i > 0) {
							sb.append(", ");
						}
						sb.append(item);
					}
				} else if (ct.equals("boolean")) {
					boolean[] arr = (boolean[]) oriValue;
					for (int i = 0; i < arr.length; i++) {
						boolean item = arr[i];
						if (i > 0) {
							sb.append(", ");
						}
						sb.append(item);
					}
				} else if (ct.equals("double")) {
					double[] arr = (double[]) oriValue;
					for (int i = 0; i < arr.length; i++) {
						double item = arr[i];
						if (i > 0) {
							sb.append(", ");
						}
						sb.append(item);
					}
				} else if (ct.equals("float")) {
					float[] arr = (float[]) oriValue;
					for (int i = 0; i < arr.length; i++) {
						float item = arr[i];
						if (i > 0) {
							sb.append(", ");
						}
						sb.append(item);
					}
				} else if (ct.equals("char")) {
					char[] arr = (char[]) oriValue;
					for (int i = 0; i < arr.length; i++) {
						char item = arr[i];
						if (i > 0) {
							sb.append(", ");
						}
						sb.append(item);
					}
				} else if (ct.equals("byte")) {
					byte[] arr = (byte[]) oriValue;
					return UConvert.ToBase64String(arr);
				}
			} else {
				Object[] arr = (Object[]) oriValue;
				for (int i = 0; i < arr.length; i++) {
					Object item = arr[i];
					if (i > 0) {
						sb.append(", ");
					}
					sb.append(item == null ? "null" : item.toString());
				}
			}
			return sb.toString();
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

		Date t = null;
		// 日期型
		if (cName.indexOf("TIME") < 0 && cName.indexOf("DATE") < 0) {
			String str = oriValue.toString();
			if (str.length() < 10) {
				return oriValue.toString();
			}
			if (str.indexOf("T") > 0) {
				str = str.replace("T", " ");
				if (str.endsWith("Z")) {
					str = str.replace("Z", " ");
				}
			}
			String[] ss = str.split(" ");
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
			String dateFormat = "enus".equals(lang) ? DATE_FROMAT_ENUS
					/* MM/dd/yyyy */ : DATE_FROMAT_ZHCN /* yyyy-MM-dd */;

			t = (Date) oriValue;
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

		// 中文日期格式
		if (f.endsWith("_zh") || f.endsWith("_zh1") && t != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(t);
			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH) + 1;
			int day = cal.get(Calendar.DATE);

			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int minute = cal.get(Calendar.MINUTE);
			int second = cal.get(Calendar.SECOND);

			String zwrq = year + "年" + (month < 10 ? "0" : "") + month + "月" + (day < 10 ? "0" : "") + day + "日";

			// 中文格式日期 2002年08月09日
			if (f.equals("date_zh")) {
				return zwrq;
			}
			String zwrq1 = year + "年" + month + "月" + day + "日";

			// 中文格式日期1 2002年8月9日
			if (f.equals("date_zh1")) {
				return zwrq1;
			}
			// 中文格式日期2 二零零二年八月九日
			if (f.equals("date_zh2")) {
				String[] hz = { "零", "一", "二", "三", "四", "五", "六", "七", "八", "九" };
				for (int i = 0; i < 10; i++) {
					zwrq1 = zwrq1.replace(i + "", hz[i]);
				}
				return zwrq1;
			}
			String zwsj = (hour < 10 ? "0" : "") + hour + "点" + (minute < 10 ? "0" : "") + minute + "分";
			// 中文格式日期时间 2002年08月09日 09点07分
			if (f.equals("dateshorttime_zh")) {
				return zwrq + " " + zwsj;
			}
			// 09点07分
			if (f.equals("shorttime_zh")) {
				return zwsj;
			}

			String zwsj1 = hour + "点" + minute + "分";
			// 中文格式日期时间1 2002年8月9日 9点7分
			if (f.equals("dateshorttime_zh1")) {
				return zwrq1 + " " + zwsj1;
			}
			// 9点7分
			if (f.equals("shorttime_zh1")) {
				return zwsj1;
			}
			zwsj += (second < 10 ? "0" : "") + second + "秒";
			// 中文格式日期时间 2002年08月09日 09点07分00秒
			if (f.equals("datetime_zh")) {
				return zwrq + " " + zwsj;
			}
			// 09点07分00秒
			if (f.equals("time_zh")) {
				return zwsj;
			}

			zwsj1 += second + "秒";
			// 中文格式日期时间 2002年8月9日 9点7分0秒
			if (f.equals("datetime_zh1")) {
				return zwrq1 + " " + zwsj1;
			}
			// 9点7分0秒
			if (f.equals("time_zh1")) {
				return zwsj1;
			}

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
