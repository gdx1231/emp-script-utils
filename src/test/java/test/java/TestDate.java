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
		//t.testAes();
		t.testAes();
	}

	 
	@Test
	public void testAes() {
	
		Date date = new Date();
		
		DateTimeFormatter format = DateTimeFormatter.ofPattern("EEE, dd-MMM-yyyy HH:mm:ss", Locale.UK);
		LocalDateTime dt = date.toInstant().atZone(ZoneId.of("GMT")).toLocalDateTime();
		System.out.println( format.format(dt)+" GMT");
		
		
		System.out.println( Utils.getDateGMTString(date));
	}
	
	 
}
