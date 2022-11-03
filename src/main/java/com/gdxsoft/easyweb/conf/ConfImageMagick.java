package com.gdxsoft.easyweb.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.utils.UPath;

/**
 * ImageMagick bin path configure, ex:<br>
 * &lt;imageMagick path="c:/Program Files/ImageMagick/" /&gt;
 *
 */
public class ConfImageMagick {
	private static Logger LOGGER = LoggerFactory.getLogger(ConfImageMagick.class);
	private static ConfImageMagick INST = null;
	private static long PROP_TIME = 0;

	/**
	 * Return the instance of define
	 * 
	 * @return
	 */
	public static ConfImageMagick getInstance() {
		if (INST != null && UPath.getPropTime() == PROP_TIME) {
			return INST;
		}
		initDefine();
		return INST;
	}

	private synchronized static void initDefine() {
		// <define value="true" />
		if (UPath.getCfgXmlDoc() == null) {
			LOGGER.warn("No ewa_conf found");
			return;
		}

		INST = new ConfImageMagick();

		NodeList nl = UPath.getCfgXmlDoc().getElementsByTagName("imageMagick");
		if (nl.getLength() == 0) { // 老的方法
			/*
			 * <path name="cvt_ImageMagick_Home"
			 * value="E:\Program Files\ImageMagick-7.0.8-Q16\" />
			 */
			INST.path = UPath.getCVT_IMAGEMAGICK_HOME();
		} else {
			Element item = (Element) nl.item(0);
			INST.path = item.getAttribute("path");
		}
		// the last modify time of the ewa_conf.xml
		PROP_TIME = UPath.getPropTime();

		LOGGER.info("ImageMagick path: {}", INST.path);
	}

	// ImageMagick安装目录
	private String path;

	/**
	 * ImageMagick安装目录
	 * 
	 * @return
	 */
	public String getPath() {
		return path;
	}

}
