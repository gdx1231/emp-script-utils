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
