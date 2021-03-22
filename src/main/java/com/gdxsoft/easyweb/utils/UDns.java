package com.gdxsoft.easyweb.utils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

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

		if (records == null) {
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

	public static Hashtable<String, String> createDefaultEnv() {
		Hashtable<String, String> env = new Hashtable<String, String>();
		// java.naming.factory.initial
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");

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
		Hashtable<String, String> env = createDefaultEnv();
		// 设置域名服务器
		// java.naming.provider.url
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
		Hashtable<String, String> env = createDefaultEnv();
		return nslookup(env, domain, queryType);
	}

	/**
	 * Query a domain
	 * 
	 * @param env       the java.naming.factory environment
	 * @param domain    the domain name
	 * @param queryType the query type, E.g. txt, a, aaaa, mx, ns ...
	 * @return The all results
	 */
	public static List<String> nslookup(Hashtable<String, String> env, String domain, String queryType) {
		String qt = queryType.toLowerCase().trim();
		List<String> values = new ArrayList<String>();
		try {
			DirContext dnsContext = new InitialDirContext(env);

			Attributes attribs = dnsContext.getAttributes(domain, new String[] { qt });
			Attribute records = attribs.get(qt);

			if (records == null) {
				LOGGER.error("There is no " + queryType + " record available for " + domain);
				return null;
			}
			NamingEnumeration<?> vals = records.getAll();
			while (vals.hasMore()) {
				Object obj = vals.next();
				values.add(obj.toString());
			}

		} catch (NamingException ne) {
			LOGGER.error(ne.getExplanation());
		}

		return values;
	}
}
