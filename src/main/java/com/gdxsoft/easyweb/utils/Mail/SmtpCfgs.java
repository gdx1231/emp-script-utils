package com.gdxsoft.easyweb.utils.Mail;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UMail;
import com.gdxsoft.easyweb.utils.Utils;

import jakarta.mail.Session;

public class SmtpCfgs {
	private static SmtpCfg DEF_CFG;
	private static Map<String, SmtpCfg> CFGS;
	static {
		CFGS = new ConcurrentHashMap<String, SmtpCfg>();
	}

	/**
	 * Initial smtp cfg
	 * 
	 * @param doc
	 */
	public static void initCfgs(Document doc) {
		if (CFGS.size() > 0) {
			CFGS.clear();
		}
		SmtpCfg lastCfg = null;
		NodeList nl = doc.getElementsByTagName("smtp");
		for (int i = 0; i < nl.getLength(); i++) {
			Element eleSmtp = (Element) nl.item(i);
			lastCfg = initSmtpCfg(eleSmtp);
		}

		if (DEF_CFG == null) {
			// the last cfg as default cfg
			DEF_CFG = lastCfg;
		}
	}

	private static SmtpCfg initSmtpCfg(Element eleSmtp) {
		String smtpname = null;
		String host = null;
		int port = 25;
		String user = null;
		String password = null;
		boolean isDefault = false;
		for (int i = 0; i < eleSmtp.getAttributes().getLength(); i++) {
			Node att = eleSmtp.getAttributes().item(i);
			String name = att.getNodeName().toLowerCase();
			String val = att.getNodeValue();
			if (name.equals("ip")) {
				host = val;
			} else if (name.equals("port")) {
				port = Integer.parseInt(val);
			} else if (name.equals("user")) {
				user = val;
			} else if (name.equals("pwd")) {
				password = val;
			} else if (name.equals("default")) {
				isDefault = Utils.cvtBool(val);
			}
		}
		SmtpCfg cfg = new SmtpCfg(smtpname, host, user, password, port);

		if (isDefault) {
			DEF_CFG = cfg;
		}
		initDomains(cfg, eleSmtp);
		addSmtpCfg(cfg);
		return cfg;
	}

	/**
	 * Initial the domains
	 * 
	 * @param smtpCfg
	 * @param eleSmtp
	 */
	private static void initDomains(SmtpCfg smtpCfg, Element eleSmtp) {
		NodeList nldomains = eleSmtp.getElementsByTagName("domain");
		for (int p = 0; p < nldomains.getLength(); p++) {
			Element itemDkim = (Element) nldomains.item(p);
			DKIMCfg domain = new DKIMCfg();
			for (int i = 0; i < itemDkim.getAttributes().getLength(); i++) {
				Node att = itemDkim.getAttributes().item(i);
				String name = att.getNodeName().toLowerCase();
				String val = att.getNodeValue();
				if (name.equals("dkimDomain")) {
					domain.setDomain(val.toLowerCase().trim());
				} else if (name.equals("dkimKey")) {
					domain.setPrivateKeyPath(val);
				} else if (name.equals("dkimSelect")) {
					domain.setSelect(val);
				}
			}
			if (domain.getDomain() != null) {
				smtpCfg.getDomains().put(domain.getDomain(), domain);
			}
		}
	}

	public static DKIMCfg getDomain(String domain) {
		String domain1 = domain.toLowerCase().trim();
		for (String name : CFGS.keySet()) {
			SmtpCfg cfg = CFGS.get(name);
			if (cfg.getDomains().containsKey(domain1)) {
				return cfg.getDomains().get(domain1);
			}
		}
		return null;
	}

	public static SmtpCfg getDefaultSmtpCfg() {
		return DEF_CFG;
	}

	public static SmtpCfg getSmtpCfgByEmail(String email) {
		String domain = UMail.getEmailDomain(email).toLowerCase();
		if (CFGS.containsKey(domain)) {
			return CFGS.get(domain);
		}
		return getDefaultSmtpCfg();
	}

	public static SmtpCfg getSmtpCfgByDomain(String domain) {
		String domain1 = domain.toLowerCase().trim();
		for (String name : CFGS.keySet()) {
			SmtpCfg cfg = CFGS.get(name);
			if (cfg.getDomains().containsKey(domain1)) {
				return cfg;
			}
		}
		return getDefaultSmtpCfg();
	}

	public static void addSmtpCfg(SmtpCfg cfg) {
		String name = cfg.getName();
		if (CFGS.containsKey(name)) {
			CFGS.remove(name);
		}
		CFGS.put(cfg.getName(), cfg);
	}
	
	
	public static Session createMailSession(SmtpCfg smtpCfg) {
		if (smtpCfg == null) {
			return null;
		}
		return createMailSession(smtpCfg.getHost(), smtpCfg.getUser(), smtpCfg.getPassword(), smtpCfg.getPort(),
				smtpCfg.isSsl(), smtpCfg.isStartTls());
	}

	public static Session createMailSession(String host, String user, String password, int port, boolean ssl,
			boolean startTls) {
		Session mailSession;

		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.host", host);
		props.setProperty("mail.smtp.port", port + ""); // smtps 端口

		if (ssl) {
			// 信任服务器的证书
			props.put("mail.smtp.ssl.trust", host);
			props.put("mail.smtp.ssl.trust", host);
		} else if (startTls) {
			// If true, requires the use of the STARTTLS command. If the server doesn't
			// support the STARTTLS command, or the command fails, the connect method will
			// fail. Defaults to false.
			// props.put("mail.smtp.starttls.required", "true");

			// If true, enables the use of the STARTTLS command (if supported by the server)
			// to switch the connection to a TLS-protected connection before issuing any
			// login commands. Defaults to false.
			props.put("mail.smtp.starttls.enable", "true");
			// 信任服务器的证书
			props.put("mail.smtp.ssl.trust", host);
		}

		if (user != null && user.trim().length() > 0) {
			props.setProperty("mail.smtp.auth", "true");
			MailAuth auth = new MailAuth(user, password);
			mailSession = Session.getInstance(props, auth);
		} else {
			props.setProperty("mail.smtp.auth", "false");
			mailSession = Session.getInstance(props, null);
		}

		return mailSession;
	}
}
