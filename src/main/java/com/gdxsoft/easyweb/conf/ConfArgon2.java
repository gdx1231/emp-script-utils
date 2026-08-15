package com.gdxsoft.easyweb.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UPath;

/**
 * Argon2 password hashing configuration from ewa_conf.xml.
 * <pre>
 * &lt;argon2 memory="1024" iterations="3" /&gt;
 * </pre>
 */
public class ConfArgon2 {
	private static Logger LOGGER = LoggerFactory.getLogger(ConfArgon2.class);
	private static ConfArgon2 INST = null;
	private static long PROP_TIME = 0;

	/** Default: 1MB (1024 KB), safe for 1GB servers */
	private int memoryKB = 1024;
	/** Default: 3 iterations */
	private int iterations = 3;

	public static ConfArgon2 getInstance() {
		if (INST != null && UPath.getPropTime() == PROP_TIME) {
			return INST;
		}
		initConfig();
		return INST;
	}

	synchronized static void initConfig() {
		INST = new ConfArgon2();

		if (UPath.getCfgXmlDoc() == null) {
			PROP_TIME = UPath.getPropTime();
			LOGGER.debug("No ewa_conf loaded, using Argon2 defaults");
			return;
		}

		NodeList nl = UPath.getCfgXmlDoc().getElementsByTagName("argon2");
		if (nl.getLength() == 0) {
			PROP_TIME = UPath.getPropTime();
			LOGGER.debug("No <argon2> in ewa_conf, using defaults");
			return;
		}
		Element ele = (Element) nl.item(0);

		String memAttr = ele.hasAttribute("memory") ? ele.getAttribute("memory")
				: ele.hasAttribute("Memory") ? ele.getAttribute("Memory") : null;
		String iterAttr = ele.hasAttribute("iterations") ? ele.getAttribute("iterations")
				: ele.hasAttribute("Iterations") ? ele.getAttribute("Iterations") : null;

		if (memAttr != null) {
			try {
				INST.memoryKB = Integer.parseInt(memAttr.trim());
			} catch (NumberFormatException e) {
				LOGGER.warn("Invalid argon2 memory: {}", memAttr);
			}
		}
		if (iterAttr != null) {
			try {
				INST.iterations = Integer.parseInt(iterAttr.trim());
			} catch (NumberFormatException e) {
				LOGGER.warn("Invalid argon2 iterations: {}", iterAttr);
			}
		}

		PROP_TIME = UPath.getPropTime();

		LOGGER.info("Argon2 config: memory={}KB, iterations={}", INST.memoryKB, INST.iterations);
	}

	/** Memory usage in KB (default 1024 = 1MB) */
	public int getMemoryKB() {
		return memoryKB;
	}

	/** Number of iterations (default 3) */
	public int getIterations() {
		return iterations;
	}
}
