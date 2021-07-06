package test.java;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.utils.Utils;

public class TestDate extends TestBase {

	public static void main(String[] args) {
		TestDate t = new TestDate();
		// t.testAes();
		t.testAes();
	}

	@Test
	public void testAes() {

		Date date = new Date();

		DateTimeFormatter format = DateTimeFormatter.ofPattern("EEE, dd-MMM-yyyy HH:mm:ss", Locale.UK);
		LocalDateTime dt = date.toInstant().atZone(ZoneId.of("GMT")).toLocalDateTime();
		System.out.println(format.format(dt) + " GMT");

		System.out.println(Utils.getDateGMTString(date));

		String t1 = "2010-12-11 11:20";
		System.out.println(Utils.getDate(t1, "yyyy-MM-dd HH:mm:ss"));
		
		String t1a = "2010-12-11 11:20";
		System.out.println(Utils.getDate(t1a, "yyyy-MM-dd HH:mm"));
		String t7 = "20101211 11:20";
		System.out.println(Utils.getDate(t7, "yyyyMMdd HH:mm:ss.SSS"));
		
		String t2 = "2010-12-11 11:20:18";
		System.out.println(Utils.getDate(t2, "yyyy-MM-dd HH:mm:ss"));

		String t3 = "20101211 11:20:18.999";
		System.out.println(Utils.getDate(t3, "yyyyMMdd HH:mm:ss.SSS"));
		String t4 = "20101211 11:20:18.99";
		System.out.println(Utils.getDate(t4, "yyyyMMdd HH:mm:ss.SSS"));
		String t5 = "20101211 11:20:18.9";
		System.out.println(Utils.getDate(t5, "yyyyMMdd HH:mm:ss.SSS"));
		String t6 = "20101211 11:20:18";
		System.out.println(Utils.getDate(t6, "yyyyMMdd HH:mm:ss.SSS"));
		
	}

}
