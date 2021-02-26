package com.gdxsoft.easyweb.utils;

import org.json.JSONObject;

public class UJSon {

	/**
	 * 创建一个 错误返回结果 json
	 * 
	 * @param err 错误
	 * @return 返回结果 json
	 */
	public static JSONObject rstFalse(String err) {
		JSONObject rst = new JSONObject();
		rstSetFalse(rst, err);
		return rst;
	}

	/**
	 * 创建一个 正确返回结果 json
	 * 
	 * @param msg 内容
	 * @return 返回结果 json
	 */
	public static JSONObject rstTrue(String msg) {
		JSONObject rst = new JSONObject();
		rstSetTrue(rst, msg);
		return rst;
	}

	/**
	 * 设置返回结果 true
	 * 
	 * @param rst 结果 json
	 * @param msg 内容
	 */
	public static void rstSetTrue(JSONObject rst, String msg) {
		rst.put("RST", true);
		rst.put("MSG", msg);
	}

	/**
	 * 设置返回结果 false
	 * 
	 * @param rst 结果 json
	 * @param err 错误信息
	 */
	public static void rstSetFalse(JSONObject rst, String err) {
		rst.put("RST", false);
		rst.put("ERR", err);
	}

}
