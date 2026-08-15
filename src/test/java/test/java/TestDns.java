package test.java;

import com.gdxsoft.easyweb.utils.UDns;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestDns extends TestBase {

	// TEST-NET-1: non-routable IP, should trigger timeout
	private static final String UNREACHABLE_DNS = "192.0.2.1";

	@Test
	public void testNslookupA() {
		printCaption("A record lookup");
		List<String> records = UDns.nslookup("www.gdxsoft.com", "a");
		assertNotNull(records, "A records should not be null");
		assertFalse(records.isEmpty(), "Should have at least one A record");
		System.out.println("A: " + records);
	}

	@Test
	public void testNslookupNs() {
		printCaption("NS record lookup");
		List<String> records = UDns.nslookup("gdxsoft.com", "ns");
		assertNotNull(records, "NS records should not be null");
		assertFalse(records.isEmpty(), "Should have at least one NS record");
		System.out.println("NS: " + records);
	}

	@Test
	public void testNslookupMx() {
		printCaption("MX record lookup");
		List<String> records = UDns.nslookup("gdxsoft.com", "mx");
		assertNotNull(records, "MX records should not be null");
		System.out.println("MX: " + records);
	}

	@Test
	public void testNslookupTxt() {
		printCaption("TXT record lookup");
		List<String> records = UDns.nslookup("gdxsoft.com", "txt");
		assertNotNull(records, "TXT records should not be null");
		System.out.println("TXT: " + records);
	}

	@Test
	public void testNslookupWithSpecificServer() {
		printCaption("NS lookup via 8.8.8.8");
		List<String> records = UDns.nslookup("www.gdxsoft.com", "a", "8.8.8.8");
		assertNotNull(records, "A records via 8.8.8.8 should not be null");
		assertFalse(records.isEmpty(), "Should have at least one A record");
		System.out.println("A via 8.8.8.8: " + records);
	}

	@Test
	public void testNslookupNonexistentType() {
		printCaption("Query non-existent record type");
		// SRV records rarely exist on root domains
		List<String> records = UDns.nslookup("gdxsoft.com", "srv");
		// May be null (no records) or empty — both valid
		System.out.println("SRV: " + records);
	}

	@Test
	public void testTimeoutWithUnreachableDns() {
		printCaption("Timeout with unreachable DNS (TEST-NET-1)");
		long t0 = System.currentTimeMillis();
		List<String> records = UDns.nslookup("www.gdxsoft.com", "a", UNREACHABLE_DNS);
		long elapsed = System.currentTimeMillis() - t0;
		// DNS timeout is OS-dependent; just verify no exception was thrown
		System.out.println("Unreachable DNS timeout: " + elapsed + "ms, result=" + records);
	}

	@Test
	public void testDkimPublicKeyQuery() {
		printCaption("DKIM public key query");
		// Google's DKIM key (well-known public record)
		String key = UDns.queryDkimPublickey("gmail.com", "20210112");
		// DKIM records may or may not exist depending on the selector
		// Just verify no crash and the method completes
		System.out.println("DKIM key (gmail/20210112): "
				+ (key != null ? key.substring(0, Math.min(60, key.length())) + "..." : "null"));
	}

	@Test
	public void testCreateDefaultEnv() {
		printCaption("createDefaultEnv settings");
		Map<String, String> env = UDns.createDefaultEnv();
		assertNotNull(env);
		assertEquals("com.sun.jndi.dns.DnsContextFactory",
				env.get("java.naming.factory.initial"));
		assertEquals("5000", env.get("com.sun.jndi.dns.timeout.initial"));
		assertEquals("2", env.get("com.sun.jndi.dns.timeout.retries"));
	}
}
