package com.gdxsoft.easyweb.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UPath;

/**
 * exiftool bin path configure, ex:<br>
 * &lt;exiftool path="/opt/homebrew/bin/" /&gt;<br>
 * The path can be the bin directory containing the exiftool executable, or the
 * full path to the exiftool executable itself. If not configured, exiftool must
 * be available in the system PATH.
 */
public class ConfExiftool {
	private static Logger LOGGER = LoggerFactory.getLogger(ConfExiftool.class);
	private static ConfExiftool INST = null;
	private static long PROP_TIME = 0;

	/**
	 * Return the instance of define
	 * 
	 * @return the instance, or null if ewa_conf not loaded
	 */
	public static ConfExiftool getInstance() {
		if (INST != null && UPath.getPropTime() == PROP_TIME) {
			return INST;
		}
		initDefine();
		return INST;
	}

	private synchronized static void initDefine() {
		if (UPath.getCfgXmlDoc() == null) {
			LOGGER.warn("No ewa_conf found");
			return;
		}

		INST = new ConfExiftool();

		NodeList nl = UPath.getCfgXmlDoc().getElementsByTagName("exiftool");
		if (nl.getLength() == 0) {
			LOGGER.info("exiftool not configured in ewa_conf.xml, will use system PATH");
		} else {
			Element item = (Element) nl.item(0);
			INST.path = item.getAttribute("path");
		}
		// the last modify time of the ewa_conf.xml
		PROP_TIME = UPath.getPropTime();

		if (INST.path != null && INST.path.trim().length() > 0) {
			LOGGER.info("exiftool path: {}", INST.path);
		}
	}

	// exiftool installation directory or executable path
	private String path;

	/**
	 * exiftool installation directory or executable path
	 * 
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

}
