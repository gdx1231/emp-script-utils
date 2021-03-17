package test.java;

import com.gdxsoft.easyweb.utils.UDns;

import java.util.List;

import org.junit.jupiter.api.Test;
public class TestDns extends TestBase{

	public static void main(String[] args) {
		TestDns t = new TestDns();
		t.testDns();
	}

	@Test
	public void testDns() {
		super.printCaption("Test DNS default server");
		testDns("www.gdxsoft.com", "a");
		testDns("www.gdxsoft.com", "aaaa");
		testDns("gdxsoft.com", "txt");
		testDns("gdxsoft.com", "ns");
		testDns("gdxsoft.com", "mx");
		
		super.printCaption("Test DNS 8.8.8.8 ");
		testDns("www.gdxsoft.com", "a", "8.8.8.8");
		testDns("www.gdxsoft.com", "aaaa", "8.8.8.8");
		testDns("gdxsoft.com", "txt", "8.8.8.8");
		testDns("gdxsoft.com", "ns", "8.8.8.8");
		testDns("gdxsoft.com", "mx", "8.8.8.8");
	}

	private void testDns(String domain, String type) {
		List<String> result = UDns.nslookup(domain, type);
		System.out.println(domain + ", " + type + ", " + result);
	}
	private void testDns(String domain, String type, String dnsServer) {
		List<String> result = UDns.nslookup(domain, type, dnsServer);
		System.out.println(domain + ", " + type + ", " + result);
	}
}
