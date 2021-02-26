package test.java;

import java.util.Date;

import com.gdxsoft.easyweb.utils.*;
import org.junit.Test;

public class TestFormats extends TestBase{
	public static void main(String[] args) throws Exception {
		TestFormats t = new TestFormats();
		t.testFormats();
	}

	@Test
	public void testFormats() throws Exception {
		super.printCaption("testFormat");
		testAge();
		testDate();
		testDecimalClearZero();
		testMoney();
	}

	private void testAge() {
		super.printCaption("Age");

		String age = UFormat.formatAge("2010-01-01");
		System.out.println(age);

		Date dbo = Utils.getDate("2010-01-01");
		System.out.println(Utils.getDateString(dbo));

		String age1 = UFormat.formatAge(dbo);
		System.out.println(age1);

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

	private void testDecimalClearZero() throws Exception {
		super.printCaption("DecimalClearZero");

		System.out.println(UFormat.formatDecimalClearZero(12.4100));
		System.out.println(UFormat.formatDecimalClearZero("12.5100000"));
		System.out.println(UFormat.formatDecimalClearZero(null));
		System.out.println(UFormat.formatDecimalClearZero("12.5100100"));
	}
}
