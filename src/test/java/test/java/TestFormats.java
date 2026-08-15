package test.java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.utils.UFormat;
import com.gdxsoft.easyweb.utils.Utils;

public class TestFormats extends TestBase {

	@Test
	public void testFormatDate() throws Exception {
		printCaption("Date formats");
		Date d = Utils.getDate("2011-12-31 22:59:59", "yyyy-MM-dd HH:mm:ss");

		assertEquals("2011-12-31", UFormat.formatDate("date", d, "zhcn"));
		assertEquals("12/31/2011", UFormat.formatDate("date", d, "enus"));
		assertEquals("2011-12-31 22:59:59", UFormat.formatDate("datetime", d, "zhcn"));
		assertEquals("22:59:59", UFormat.formatDate("time", d, "zhcn"));
		assertEquals("12/31", UFormat.formatDate("shortdate", d, "enus"));
		assertEquals("12-31", UFormat.formatDate("shortdate", d, "zhcn"));
		assertEquals("22:59", UFormat.formatDate("shorttime", d, "zhcn"));
	}

	@Test
	public void testFormats() throws Exception {
		super.printCaption("testFormat");
		testAge();
		testDate();
		testDecimalClearZero();
		testMoney();
		testBytes();
	}

	@Test
	public void testFormatDateString() throws Exception {
		printCaption("Date from string");
		assertEquals("2011-12-31", UFormat.formatDate("date", "2011-12-31 22:59:59", "zhcn"));
		assertEquals("2011-12-31 22:59:59", UFormat.formatDate("datetime", "2011-12-31 22:59:59", "zhcn"));
	}

	@Test
	public void testFormatDateUKUS() throws Exception {
		printCaption("Date UK/US forced");
		Date d = Utils.getDate("2011-12-31 22:59:59", "yyyy-MM-dd HH:mm:ss");
		assertEquals("31/12/2011", UFormat.formatDate("DD_MM_YYYY", d, "zhcn"));
		assertEquals("12/31/2011", UFormat.formatDate("MM_DD_YYYY", d, "zhcn"));
	}

	@Test
	public void testFormatChineseDate() throws Exception {
		printCaption("Chinese date formats");
		Date d = Utils.getDate("2002-08-09 09:07:00", "yyyy-MM-dd HH:mm:ss");

		assertEquals("2002年08月09日", UFormat.formatDate("date_zh", d, "zhcn"));
		assertEquals("2002年8月9日", UFormat.formatDate("date_zh1", d, "zhcn"));
		assertEquals("09点07分", UFormat.formatDate("shorttime_zh", d, "zhcn"));
		assertEquals("2002年08月09日 09点07分", UFormat.formatDate("dateshorttime_zh", d, "zhcn"));
		assertEquals("2002年08月09日 09点07分00秒", UFormat.formatDate("datetime_zh", d, "zhcn"));
	}

	@Test
	public void testFormatChineseDate2() throws Exception {
		printCaption("Chinese date zh2");
		Date d = Utils.getDate("2002-08-09 09:07:00", "yyyy-MM-dd HH:mm:ss");
		String result = UFormat.formatDate("date_zh2", d, "zhcn");
		assertEquals("二零零二年八月九日", result);
	}

	@Test
	public void testFormatWeek() throws Exception {
		printCaption("Week");
		Date d = Utils.getDate("2011-12-31 22:59:59", "yyyy-MM-dd HH:mm:ss"); // Saturday
		assertEquals("六", UFormat.formatWeek(d, "zhcn"));
		assertEquals("Sat", UFormat.formatWeek(d, "enus"));
	}

	@Test
	public void testFormatAge() {
		printCaption("Age");
		String age = UFormat.formatAge("2010-01-01");
		assertNotNull(age);
		int a = Integer.parseInt(age);
		assertTrue(a > 0, "Age should be positive");
	}

	@Test
	public void testFormatAgeDate() {
		printCaption("Age from Date");
		Date birth = Utils.getDate("2000-01-01 00:00:00", "yyyy-MM-dd HH:mm:ss");
		String age = UFormat.formatAge(birth);
		assertNotNull(age);
		assertTrue(Integer.parseInt(age) > 0);
	}

	@Test
	public void testFormatMoney() {
		printCaption("Money format");
		assertEquals("132,312.41", UFormat.formatMoney(132312.4133));
		assertEquals("132,312.42", UFormat.formatMoney(132312.4153));
		assertEquals("132,312.41", UFormat.formatMoney("132,312.4133"));
		assertNull(UFormat.formatMoney(null));
	}

	private void testAge() {
		super.printCaption("Age");

		String age = UFormat.formatAge("2010-01-01");
		System.out.println(age);

		Date dbo = Utils.getDate("2010-01-01");
		System.out.println(Utils.getDateString(dbo));

		String age1 = UFormat.formatAge(dbo);
		System.out.println(age1);

		String format = "yyyy-MM-dd hh:mm:ss";
		String source = "2021-03-21 18:02:01";
		Date date = Utils.getDate(source, format);
		System.out.println(date);

		String f1 = "yyyy-MM-dd HH:mm:ss.SSS";
		String s1 = "2016-08-18T14:19:46";
		Date date1 = Utils.getDate(s1, f1);
		System.out.println(date1);

		String s2 = "2016-08-18T14:19:46.12";
		Date date2 = Utils.getDate(s2, f1);
		System.out.println(date2);
	}

	private void testDate() throws Exception {
		super.printCaption("Date");

		String[] formats = "date,dateTime,time,dateShortTime,shortTime,shortDate,shortDateTime,week".split(",");

		for (int i = 0; i < formats.length; i++) {
			Date t1 = new Date();
			String toFormat = formats[i].trim();
			String text = UFormat.formatDate(toFormat, t1, "zhcn");
			System.out.println(toFormat + " = " + text);
			String text1 = UFormat.formatDate(toFormat, t1, "enus");
			System.out.println(toFormat + " = " + text1);
		}

		for (int i = 0; i < formats.length; i++) {
			String t1 = "2011-12-31 22:59:59";
			String toFormat = formats[i].trim();
			String text = UFormat.formatDate(toFormat, t1, "zhcn");
			System.out.println(toFormat + " = " + text);
			t1 = "12/31/2011 22:59:59";
			String text1 = UFormat.formatDate(toFormat, t1, "enus");
			System.out.println(toFormat + " = " + text1);
		}
	}

	private void testMoney() throws Exception {
		super.printCaption("Money");

		System.out.println(UFormat.formatMoney(132312.4133));
		// 四舍五入
		System.out.println(UFormat.formatMoney(132312.4153));

		System.out.println(UFormat.formatMoney("132,312.4133"));
		// 四舍五入
		System.out.println(UFormat.formatMoney("132312.4153"));
	}

	private void testBytes() throws Exception {
		super.printCaption("Bytes");

		System.out.println(UFormat.formatBytes(512));
		System.out.println(UFormat.formatBytes(1536));
		System.out.println(UFormat.formatBytes(2411724));
		System.out.println(UFormat.formatBytes(3221225472L));
		System.out.println(UFormat.formatBytes(2199023255552L));
		System.out.println(UFormat.formatBytes(2199023255552000L));
	}

	private void testDecimalClearZero() throws Exception {
		super.printCaption("DecimalClearZero");

		System.out.println(UFormat.formatDecimalClearZero(12.4100));
		System.out.println(UFormat.formatDecimalClearZero("12.5100000"));
		System.out.println(UFormat.formatDecimalClearZero(null));
		System.out.println(UFormat.formatDecimalClearZero("12.5100100"));
	}

	@Test
	public void testFormatInt() {
		printCaption("Int format");
		assertEquals("123", UFormat.formatInt(123.456));
		assertEquals("0", UFormat.formatInt(0.999));
		assertNull(UFormat.formatInt(null));
	}

	@Test
	public void testFormatPercent() throws Exception {
		printCaption("Percent");
		assertEquals("25.00%", UFormat.formatPercent(0.25));
		assertEquals("100.00%", UFormat.formatPercent(1));
		assertNull(UFormat.formatPercent(null));
	}

	@Test
	public void testFormatDecimalClearZero() throws Exception {
		printCaption("Decimal clear zero");
		assertEquals("12.41", UFormat.formatDecimalClearZero(12.4100));
		assertEquals("12.51", UFormat.formatDecimalClearZero("12.5100000"));
		assertEquals("12.51", UFormat.formatDecimalClearZero("12.5100100")); // 4th decimal=0, truncated
		assertNull(UFormat.formatDecimalClearZero(null));
	}

	@Test
	public void testFormatNumberClearZero() throws Exception {
		printCaption("Number clear zero (with commas)");
		String r = UFormat.formatNumberClearZero(1234.50);
		assertNotNull(r);
		assertTrue(r.contains("234"), "Should contain 1234 part");
		assertFalse(r.contains(".50"), "Trailing zero should be stripped");
		assertNull(UFormat.formatNumberClearZero(null));
	}

	@Test
	public void testChineseMoney() {
		printCaption("Chinese money");
		assertEquals("壹佰贰拾叁元肆角伍分", UFormat.formatChineseMoney(123.45));
		assertEquals("零元整", UFormat.formatChineseMoney(0));
		assertEquals("壹元整", UFormat.formatChineseMoney(1));
	}

	@Test
	public void testFormatValueNull() throws Exception {
		printCaption("formatValue with null");
		assertNull(UFormat.formatValue("date", null, "zhcn"));
	}

	@Test
	public void testObjectToString() {
		printCaption("objectToString");
		assertEquals("123", UFormat.objectToString(123));
		assertNull(UFormat.objectToString(null));
	}

	@Test
	public void testObjectToStringArray() {
		printCaption("objectToString — arrays");
		assertEquals("1, 2, 3", UFormat.objectToString(new int[] { 1, 2, 3 }));
		assertEquals("a, b", UFormat.objectToString(new String[] { "a", "b" }));
	}

	@Test
	public void testCalcNumberScale() {
		printCaption("calcNumberScale");
		assertEquals(1.23, UFormat.calcNumberScale(123, new java.math.BigDecimal(100)));
		assertEquals(123, UFormat.calcNumberScale(123, new java.math.BigDecimal(1)));
		assertNull(UFormat.calcNumberScale(null, new java.math.BigDecimal(100)));
	}
}
