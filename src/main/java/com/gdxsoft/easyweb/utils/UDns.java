package com.gdxsoft.easyweb.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UDns {
	private static Logger LOGGER = LoggerFactory.getLogger(UDns.class);

	/**
	 * Query the public key from DNS record
	 * 
	 * @param domain   the email sign domain
	 * @param selector the selector
	 * @return public key
	 * @throws Exception
	 */
	public static String queryDkimPublickey(String domain, String selector) {

		String recordname = selector + "._domainkey." + domain;
		String value = null;

		List<String> records = UDns.nslookup(recordname, "txt");

		if (records == null || records.isEmpty()) {
			LOGGER.error("NO " + recordname);
			return null;
		}

		value = records.get(0);

		// v=DKIM1; k=rsa; p=MIGfMA0G...
		String[] tags = value.split(";");
		for (String tag : tags) {
			tag = tag.trim();
			if (tag.startsWith("p=")) {
				String base64Key = tag.substring(2);
				// DER format
				return base64Key;

			}
		}

		return null;
	}

	public static Map<String, String> createDefaultEnv() {
		Map<String, String> env = new HashMap<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
		env.put("com.sun.jndi.dns.timeout.initial", "5000");
		env.put("com.sun.jndi.dns.timeout.retries", "2");

		return env;
	}

	/**
	 * Query a domain
	 * 
	 * @param domain    the domain name
	 * @param queryType the query type, E.g. txt, a, aaaa, mx, ns ...
	 * @param dnsServer the DNS server
	 * @return the multiple results
	 */
	public static List<String> nslookup(String domain, String queryType, String dnsServer) {
		Map<String, String> env = createDefaultEnv();
		env.put(Context.PROVIDER_URL, "dns://" + dnsServer);

		return nslookup(env, domain, queryType);
	}

	/**
	 * Query a domain
	 *
	 * @param domain    the domain name
	 * @param queryType the query type, E.g. txt, a, aaaa, mx, ns ...
	 * @return the multiple results
	 */
	public static List<String> nslookup(String domain, String queryType) {
		Map<String, String> env = createDefaultEnv();
		return nslookup(env, domain, queryType);
	}

	/**
	 * Query a domain
	 *
	 * @param env       the JNDI environment
	 * @param domain    the domain name
	 * @param queryType the query type, E.g. txt, a, aaaa, mx, ns ...
	 * @return The all results
	 */
	public static List<String> nslookup(Map<String, String> env, String domain, String queryType) {
		String qt = queryType.toLowerCase().trim();
		List<String> values = new ArrayList<>();
		try {
			DirContext dnsContext = new InitialDirContext(new Hashtable<>(env));
			try {
				Attributes attribs = dnsContext.getAttributes(domain, new String[] { qt });
				Attribute records = attribs.get(qt);

				if (records == null) {
					LOGGER.error("There is no {} record available for {}", queryType, domain);
					return null;
				}
				NamingEnumeration<?> vals = records.getAll();
				try {
					while (vals.hasMore()) {
						values.add(vals.next().toString());
					}
				} finally {
					vals.close();
				}
			} finally {
				dnsContext.close();
			}
		} catch (NamingException ne) {
			LOGGER.error(ne.getExplanation());
		}

		return values;
	}
}
