package com.gdxsoft.easyweb.utils.Mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UMail;
import com.gdxsoft.easyweb.utils.UXml;
import com.gdxsoft.easyweb.utils.Utils;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

public class SmtpCfgs {
	private static Logger LOG = LoggerFactory.getLogger(SmtpCfgs.class);
	/**
	 * The default SMTP server
	 */
	private static SmtpCfg DEF_CFG;
	private static Map<String, SmtpCfg> CFGS;
	private static Map<String, DKIMCfg> DKIMS;

	private static Map<String, List<SmtpCfg>> TO_EMAIL_MAP;
	private static Map<String, List<SmtpCfg>> TO_DOMAIN_MAP;

	private static Map<String, List<SmtpCfg>> FROM_EMAIL_MAP;
	private static Map<String, List<SmtpCfg>> FROM_DOMAIN_MAP;
	static {
		CFGS = new ConcurrentHashMap<>();
		DKIMS = new ConcurrentHashMap<>();

		TO_EMAIL_MAP = new ConcurrentHashMap<>();
		TO_DOMAIN_MAP = new ConcurrentHashMap<>();

		FROM_EMAIL_MAP = new ConcurrentHashMap<>();
		FROM_DOMAIN_MAP = new ConcurrentHashMap<>();
	}

	/**
	 * Initial smtp cfg
	 * 
	 * @param doc
	 */
	public static void initCfgs(Document doc) {
		if (CFGS.size() > 0) {
			CFGS.clear();
			DKIMS.clear();

			TO_EMAIL_MAP.clear();
			TO_DOMAIN_MAP.clear();

			FROM_EMAIL_MAP.clear();
			FROM_DOMAIN_MAP.clear();
		}
		SmtpCfg lastCfg = null;
		NodeList nl = doc.getElementsByTagName("smtp");
		for (int i = 0; i < nl.getLength(); i++) {
			Element eleSmtp = (Element) nl.item(i);
			lastCfg = createSmtpCfg(eleSmtp);
		}

		if (DEF_CFG == null) {
			// the last cfg as default cfg
			DEF_CFG = lastCfg;
		}

		NodeList nlDkim = doc.getElementsByTagName("dkim");
		for (int i = 0; i < nlDkim.getLength(); i++) {
			Element eleDkim = (Element) nlDkim.item(i);
			initDkimCfg(eleDkim);
		}
	}

	/**
	 * Create a SmtpCfg
	 * 
	 * @param eleSmtp the element of smtpCfg
	 * @return SmtpCfg;
	 */
	private static SmtpCfg createSmtpCfg(Element eleSmtp) {
		Map<String, String> smtpParas = UXml.getElementAttributes(eleSmtp, true);

		// SMTP host or ip
		String host = smtpParas.containsKey("host") ? smtpParas.get("host")
				: smtpParas.containsKey("ip") ? smtpParas.get("ip") : null;
		// SMTP user
		String user = smtpParas.containsKey("user") ? smtpParas.get("user") : null;
		// SMTP password
		String password = smtpParas.containsKey("pwd") ? smtpParas.get("pwd") : null;
		// SMTP port
		int port = smtpParas.containsKey("port") ? Integer.parseInt(smtpParas.get("port")) : 25;
		// default server
		boolean isDefault = smtpParas.containsKey("default") ? Utils.cvtBool(smtpParas.get("default")) : false;

		// Use SSL protocol to connect to the server
		boolean isSsl = false;
		String ssl = smtpParas.get("ssl");
		if (ssl == null || ssl.trim().length() == 0) {
			if (port == 465) {
				// the ssl not setting and the port ==  465
				isSsl = true;
			}
		} else {
			isSsl = Utils.cvtBool(ssl);
		}

		// Try to connect to the server using TLS protocol
		boolean isStartTls = smtpParas.containsKey("starttls") ? Utils.cvtBool(smtpParas.get("starttls")) : false;

		// name string
		String smtpname = host + "." + port + "." + user + "." + password + "." + isSsl + "." + isStartTls;

		SmtpCfg cfg = new SmtpCfg(smtpname, host, user, password, port);
		cfg.setSsl(isSsl);
		cfg.setStartTls(isStartTls);

		addFromMap(cfg, eleSmtp);
		addToMap(cfg, eleSmtp);

		if (isDefault) {
			DEF_CFG = cfg;
		}
		addSmtpCfg(cfg);
		return cfg;
	}

	/**
	 * Add from addresses to the FROM_DOMAIN_MAP or FROM_EMAIL_MAP
	 * 
	 * @param cfg
	 * @param eleSmtp
	 */
	private static void addFromMap(SmtpCfg cfg, Element eleSmtp) {
		NodeList nlFrom = eleSmtp.getElementsByTagName("from");
		if (cfg.getUser() != null && cfg.getUser().indexOf("@") > 0) {
			// Default SMTP user as FROM
			addFromMap(cfg, cfg.getUser());
		}
		for (int i = 0; i < nlFrom.getLength(); i++) {
			Element item = (Element) nlFrom.item(i);
			Map<String, String> params = UXml.getElementAttributes(item, true);
			if (!params.containsKey("email")) {
				continue;
			}
			String email = params.get("email");
			addFromMap(cfg, email);
		}
	}

	/**
	 * Add the from email to the FROM_DOMAIN_MAP or FROM_EMAIL_MAP
	 * 
	 * @param cfg   the SmtpCfg
	 * @param email the from email
	 */
	private static void addFromMap(SmtpCfg cfg, String email) {
		email = email.trim().toLowerCase();
		if (email.length() == 0) {
			return;
		}

		if (email.startsWith("@")) {
			// domain
			String domain = email.substring(1); // remove @
			if (!FROM_DOMAIN_MAP.containsKey(domain)) {
				FROM_DOMAIN_MAP.put(domain, new ArrayList<>());
			}
			if (!FROM_DOMAIN_MAP.get(domain).contains(cfg)) {
				FROM_DOMAIN_MAP.get(domain).add(cfg);
			}
		} else {
			// email
			if (!FROM_EMAIL_MAP.containsKey(email)) {
				FROM_EMAIL_MAP.put(email, new ArrayList<>());
			}
			if (!FROM_EMAIL_MAP.get(email).contains(cfg)) {
				FROM_EMAIL_MAP.get(email).add(cfg);
			}
		}
		cfg.getFromMap().put(email, email);
	}

	/**
	 * add to addresses to the TO_DOMAIN_MAP or TO_EMAIL_MAP
	 * 
	 * @param cfg
	 * @param eleSmtp
	 */
	private static void addToMap(SmtpCfg cfg, Element eleSmtp) {
		NodeList nlFrom = eleSmtp.getElementsByTagName("to");
		for (int i = 0; i < nlFrom.getLength(); i++) {
			Element item = (Element) nlFrom.item(i);
			Map<String, String> params = UXml.getElementAttributes(item, true);
			if (!params.containsKey("email")) {
				continue;
			}
			String email = params.get("email");
			addToMap(cfg, email);
		}
	}

	/**
	 * Add the email to the TO_DOMAIN_MAP or TO_EMAIL_MAP
	 * 
	 * @param cfg   the SmtpCfg
	 * @param email the email
	 */
	private static void addToMap(SmtpCfg cfg, String email) {
		email = email.trim().toLowerCase();
		if (email.length() == 0) {
			return;
		}
		if (email.startsWith("@")) {
			// domain
			String domain = email.substring(1); // remove @
			if (!TO_DOMAIN_MAP.containsKey(domain)) {
				TO_DOMAIN_MAP.put(domain, new ArrayList<>());
			}
			if (!TO_DOMAIN_MAP.get(domain).contains(cfg)) {
				TO_DOMAIN_MAP.get(domain).add(cfg);
			}
		} else {
			// email
			if (!TO_EMAIL_MAP.containsKey(email)) {
				TO_EMAIL_MAP.put(email, new ArrayList<>());
			}
			if (!TO_EMAIL_MAP.get(email).contains(cfg)) {
				TO_EMAIL_MAP.get(email).add(cfg);
			}
		}
		cfg.getToMap().put(email, email);
	}

	/**
	 * Initial the DKIMCfg
	 * 
	 * @param itemDkim DKIM XML node
	 */
	private static void initDkimCfg(Element itemDkim) {
		DKIMCfg cfg = new DKIMCfg();
		Map<String, String> ps = UXml.getElementAttributes(itemDkim, true);
		String domain = ps.get("domain");
		if (domain == null || domain.trim().length() == 0) {
			String xml = UXml.asXml(itemDkim);
			LOG.warn("no domain -> " + xml);
			return;
		}
		domain = domain.toLowerCase().trim();
		String key = ps.get("key");
		if (key == null || key.trim().length() == 0) {
			String xml = UXml.asXml(itemDkim);
			LOG.warn("no key -> " + xml);
			return;
		}
		String select = ps.get("select");
		if (select == null || select.trim().length() == 0) {
			String xml = UXml.asXml(itemDkim);
			LOG.warn("no select -> " + xml);
			return;
		}

		cfg.setDomain(domain);
		cfg.setPrivateKeyPath(key);
		cfg.setSelect(select);
		cfg.setDkim(true);

		DKIMS.put(domain, cfg);
	}

	/**
	 * Return the DKIMCfg according to parameter emailOrDomain
	 * 
	 * @param emailOrdomain Email or Domain
	 * @return DKIMCfg or null
	 */
	public static DKIMCfg getDkim(String emailOrDomain) {
		String domain1 = emailOrDomain.toLowerCase().trim();
		if (emailOrDomain.indexOf("@") > 0) {
			domain1 = UMail.getEmailDomain(emailOrDomain);
		}
		if (DKIMS.containsKey(domain1)) {
			return DKIMS.get(domain1);
		} else {
			return null;
		}
	}

	/**
	 * Get a SMTP configuration from the SendMail
	 * 
	 * @param sm the SendMail
	 * @return the SMTP configuration
	 */
	public static SmtpCfg getSmtpCfg(SendMail sm) {
		List<Addr> al = new ArrayList<Addr>(); // all recipients

		sm.getTos().forEach((k, v) -> {
			al.add(v);
		});
		sm.getCcs().forEach((k, v) -> {
			al.add(v);
		});
		sm.getBccs().forEach((k, v) -> {
			al.add(v);
		});

		String from = sm.getFrom().getEmail();

		return getSmtpCfg(from, al);
	}

	/**
	 * Get a SMTP configuration from the message
	 * 
	 * @param msg the message
	 * @return the SMTP configuration
	 */
	public static SmtpCfg getSmtpCfg(MimeMessage msg) {
		MailDecode md = new MailDecode(msg, null);
		List<Addr> al = null; // all recipients
		String from = null;
		try {
			al = md.getAllRecipients();
			from = md.getFrom().getEmail();

			return getSmtpCfg(from, al);
		} catch (Exception e) {
			LOG.warn(e.getMessage());
			return getDefaultSmtpCfg();
		}

	}

	/**
	 * Get a SMTP configuration from sender or recipients
	 * 
	 * @param fromEmail  the from address
	 * @param recipients the recipients
	 * @return the SMTP configuration
	 */
	public static SmtpCfg getSmtpCfg(String fromEmail, List<Addr> recipients) {
		fromEmail = fromEmail.trim().toLowerCase();
		// the from email priority is 0
		List<SmtpCfg> smtpFromEmails = FROM_EMAIL_MAP.get(fromEmail);
		if (smtpFromEmails == null) {
			smtpFromEmails = new ArrayList<>();
		} else if (smtpFromEmails.size() == 1) {
			// There is only one from email configuration
			return smtpFromEmails.get(0);
		}

		// the from domain priority is 1
		String fromDomain = UMail.getEmailDomain(fromEmail);
		List<SmtpCfg> smtpFromDomains = FROM_EMAIL_MAP.get(fromDomain);
		if (smtpFromDomains == null) {
			smtpFromDomains = new ArrayList<>();
		} else if (smtpFromEmails.size() == 0 && smtpFromDomains.size() == 1) {
			// There is no from email configuration, and only one from domain configuration
			return smtpFromDomains.get(0);
		}

		// the to email priority is 2
		List<SmtpCfg> lstToEmails = getSmtpCfgByToEmail(recipients);
		// from email match to email
		for (int i = 0; i < smtpFromEmails.size(); i++) {
			SmtpCfg fromCfg = smtpFromEmails.get(i);
			for (int m = 0; m < lstToEmails.size(); m++) {
				SmtpCfg toCfg = lstToEmails.get(m);
				if (fromCfg == toCfg) { // from equals one of recipients
					return fromCfg;
				}
			}
		}
		// from domain match to email
		for (int i = 0; i < smtpFromDomains.size(); i++) {
			SmtpCfg fromCfg = smtpFromDomains.get(i);
			for (int m = 0; m < lstToEmails.size(); m++) {
				SmtpCfg toCfg = lstToEmails.get(m);
				if (fromCfg == toCfg) { // from equals one of recipients
					return fromCfg;
				}
			}
		}

		// the to domain priority is 3
		List<SmtpCfg> lstToDomains = getSmtpCfgByToDomain(recipients);
		// from email match to domain
		for (int i = 0; i < smtpFromEmails.size(); i++) {
			SmtpCfg fromCfg = smtpFromEmails.get(i);
			for (int m = 0; m < lstToDomains.size(); m++) {
				SmtpCfg toCfg = lstToDomains.get(m);
				if (fromCfg == toCfg) { // from equals one of recipients
					return fromCfg;
				}
			}
		}
		// from domain match to domain
		for (int i = 0; i < smtpFromDomains.size(); i++) {
			SmtpCfg fromCfg = smtpFromDomains.get(i);
			for (int m = 0; m < lstToDomains.size(); m++) {
				SmtpCfg toCfg = lstToDomains.get(m);
				if (fromCfg == toCfg) { // from equals one of recipients
					return fromCfg;
				}
			}
		}

		if (smtpFromEmails.size() > 0) {
			return smtpFromEmails.get(0);
		}
		if (smtpFromDomains.size() > 0) {
			return smtpFromDomains.get(0);
		}
		if (lstToEmails.size() > 0) {
			return lstToEmails.get(0);
		}
		if (lstToDomains.size() > 0) {
			return lstToDomains.get(0);
		}

		// return the default configuration
		return getDefaultSmtpCfg();
	}

	/**
	 * Get all SMTP configurations that match the recipient list
	 * 
	 * @param al the recipient list
	 * @return the configurations
	 */
	public static List<SmtpCfg> getSmtpCfgByToEmail(List<Addr> al) {
		List<SmtpCfg> lst = new ArrayList<>();
		if (al == null || al.size() == 0) {
			return lst;
		}
		for (int i = 0; i < al.size(); i++) {
			String email = al.get(i).getEmail();
			List<SmtpCfg> cfgs = getSmtpCfgByToEmail(email);
			if (cfgs != null) {
				lst.addAll(cfgs);
			}
		}
		return lst;
	}

	/**
	 * Get all SMTP configurations that match the recipient domain list
	 * 
	 * @param al the recipient domain list
	 * @return the configurations
	 */
	public static List<SmtpCfg> getSmtpCfgByToDomain(List<Addr> al) {
		List<SmtpCfg> lst = new ArrayList<>();
		if (al == null || al.size() == 0) {
			return lst;
		}
		for (int i = 0; i < al.size(); i++) {
			String email = al.get(i).getEmail();
			String domain = UMail.getEmailDomain(email);
			List<SmtpCfg> cfgs = getSmtpCfgByToDomain(domain);
			if (cfgs.size() > 0) {
				lst.addAll(cfgs);
			}
		}
		return lst;
	}

	public static SmtpCfg getDefaultSmtpCfg() {
		return DEF_CFG;
	}

	/**
	 * Get all configurations that match the to email address
	 * 
	 * @param toEmail the to email address
	 * @return the list
	 */
	public static List<SmtpCfg> getSmtpCfgByToEmail(String toEmail) {
		String em = toEmail.trim().toLowerCase();
		if (TO_EMAIL_MAP.containsKey(em)) {
			return TO_EMAIL_MAP.get(em);
		} else {
			return new ArrayList<SmtpCfg>();
		}
	}

	/**
	 * Get all configurations that match the to domain
	 * 
	 * @param toDomain the to domain name
	 * @return the list
	 */
	public static List<SmtpCfg> getSmtpCfgByToDomain(String toDomain) {
		String em = toDomain.trim().toLowerCase();
		if (TO_DOMAIN_MAP.containsKey(em)) {
			return TO_DOMAIN_MAP.get(em);
		} else {
			return new ArrayList<SmtpCfg>();
		}
	}

	/**
	 * Get all configuration that match the from email address
	 * 
	 * @param fromEmail The from email address
	 * @return
	 */
	public static List<SmtpCfg> getSmtpCfgByFromEmail(String fromEmail) {
		String em = fromEmail.trim().toLowerCase();
		if (FROM_EMAIL_MAP.containsKey(em)) {
			return FROM_EMAIL_MAP.get(em);
		} else {
			return new ArrayList<SmtpCfg>();
		}
	}

	/**
	 * Get all configurations that match the from domain name
	 * 
	 * @param fromDomain the from domain name
	 * @return the result
	 */
	public static List<SmtpCfg> getSmtpCfgByFromDomain(String fromDomain) {
		String em = fromDomain.trim().toLowerCase();
		if (FROM_DOMAIN_MAP.containsKey(em)) {
			return FROM_DOMAIN_MAP.get(em);
		} else {
			return new ArrayList<SmtpCfg>();
		}
	}

	private static void addSmtpCfg(SmtpCfg cfg) {
		String name = cfg.getName();
		if (CFGS.containsKey(name)) {
			CFGS.remove(name);
		}
		CFGS.put(cfg.getName(), cfg);
	}

	/**
	 * Create a mail session
	 * 
	 * @param smtpCfg The SMTP configuration
	 * @return The mail session
	 */
	public static Session createMailSession(SmtpCfg smtpCfg) {
		if (smtpCfg == null) {
			return null;
		}
		return createMailSession(smtpCfg.getHost(), smtpCfg.getUser(), smtpCfg.getPassword(), smtpCfg.getPort(),
				smtpCfg.isSsl(), smtpCfg.isStartTls());
	}

	/**
	 * Create a mail session
	 * 
	 * @param host     The SMTP server host/IP
	 * @param user     The SMTP server user
	 * @param password The SMTP server password
	 * @param port     The SMTP server port
	 * @param ssl      Whether to use SSL protocol to connect the server
	 * @param startTls Whether to use startTls command to send the email
	 * @return The mail session
	 */
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
			props.put("mail.smtp.ssl.enable", true);
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
