package com.gdxsoft.easyweb.utils;

import org.json.JSONObject;

public class UJSon {
	public final static String RESULT_TAG = "RST";
	public final static String RESULT_ERR = "ERR";
	public final static String RESULT_MSG = "MSG";

	/**
	 * Check the result is successful
	 * 
	 * @param result
	 * @return true=successful
	 */
	public static boolean checkTrue(JSONObject result) {
		return result.optBoolean(RESULT_TAG);
	}

	/**
	 * Check the result is fail
	 * 
	 * @param result
	 * @return true = fail
	 */
	public static boolean checkFalse(JSONObject result) {
		return !checkTrue(result);
	}

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
	 * Create a result JSON with RST=true
	 * 
	 * @return the JSON with RST=true
	 */
	public static JSONObject rstTrue() {
		return rstTrue(null);
	}

	/**
	 * Set result JSON RST=true
	 * 
	 * @param rst the result JSON
	 * @param msg the right message
	 */
	public static void rstSetTrue(JSONObject rst, String msg) {
		rst.put(RESULT_TAG, true);
		if (msg != null) {
			rst.put(RESULT_MSG, msg);
		}
	}

	/**
	 * Set result JSON RST=false
	 * 
	 * @param rst the result JSON
	 * @param err error message
	 */
	public static void rstSetFalse(JSONObject rst, String err) {
		rst.put(RESULT_TAG, false);
		if (err != null) {
			rst.put(RESULT_ERR, err);
		}
	}

}
