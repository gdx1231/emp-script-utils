package com.gdxsoft.easyweb.utils;

import org.json.JSONObject;

public class UJSon {

	/**
	 * Create a result JSON with RST=false
	 * 
	 * @param err the error message
	 * @return the JSON with RST=false
	 */
	public static JSONObject rstFalse(String err) {
		JSONObject rst = new JSONObject();
		rstSetFalse(rst, err);
		return rst;
	}

	/**
	 * Create a result JSON with RST=true
	 * 
	 * @param msg the message
	 * @return the JSON with RST=true
	 */
	public static JSONObject rstTrue(String msg) {
		JSONObject rst = new JSONObject();
		rstSetTrue(rst, msg);
		return rst;
	}

	/**
	 * Set result JSON RST=true
	 * 
	 * @param rst the result JSON
	 * @param msg the right message
	 */
	public static void rstSetTrue(JSONObject rst, String msg) {
		rst.put("RST", true);
		rst.put("MSG", msg);
	}

	/**
	 * Set result JSON RST=false
	 * 
	 * @param rst the result JSON
	 * @param err error message
	 */
	public static void rstSetFalse(JSONObject rst, String err) {
		rst.put("RST", false);
		rst.put("ERR", err);
	}

}
