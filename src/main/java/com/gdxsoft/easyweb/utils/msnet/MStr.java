package com.gdxsoft.easyweb.utils.msnet;

import java.io.Serializable;

import com.gdxsoft.easyweb.utils.Utils;

/**
 * 兼容的StringBuilder 对象
 * 
 * @author Administrator
 * 
 */
public class MStr implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1877502096998706509L;
	private StringBuilder _Sb;

	private String newLine = "\r\n";

	public MStr() {
		_Sb = new StringBuilder();
	}

	public MStr(Object val) {
		_Sb = new StringBuilder();
		_Sb.append(val);
	}

	public int length() {
		return _Sb.length();
	}

	public void setLength(int length) {
		_Sb.setLength(length);
	}

	public void reset() {
		_Sb.setLength(0);
	}

	/**
	 * 添加内容 = append
	 * 
	 * @param val 内容
	 * @return this
	 */
	public MStr a(Object val) {
		_Sb.append(val);
		return this;
	}

	/**
	 * 添加内容
	 * 
	 * @param val 内容
	 * @return this
	 */
	public MStr append(Object val) {
		_Sb.append(val);
		return this;
	}

	/**
	 * 添加内容，并在结尾添加新行(newLine)，默认\r\n
	 * 
	 * @param val 内容
	 * @return this
	 */
	public MStr appendLine(Object val) {
		_Sb.append(val);
		_Sb.append(this.newLine);
		return this;
	}

	/**
	 * 添加内容，并在结尾添加新行(newLine)，默认\r\n = appendLine
	 * 
	 * @param val 内容
	 * @return this
	 */
	public MStr al(Object val) {
		this.appendLine(val);
		return this;
	}

	/**
	 * 查找字符串位置
	 * 
	 * @param val 字符串
	 * @return
	 */
	public int indexOf(String val) {
		return _Sb.indexOf(val);
	}

	/**
	 * 查找字符串位置
	 * 
	 * @param val   字符串
	 * @param start 开始位置
	 * @return
	 */
	public int indexOf(String val, int start) {
		return _Sb.indexOf(val, start);
	}

	/**
	 * 提取字符串
	 * 
	 * @param startIndex 开始位置
	 * @return 提取的字符串
	 */
	public MStr substring(int startIndex) {
		_Sb.substring(startIndex);
		return this;
	}

	/**
	 * 提取字符串
	 * 
	 * @param startIndex 开始位置
	 * @param endIndex   结束位置
	 * @return 提取的字符串
	 */
	public String substring(int startIndex, int endIndex) {
		return _Sb.substring(startIndex, endIndex);
	}

	/**
	 * 删除字符串
	 * 
	 * @param startIndex 开始位置
	 * @param endIndex   结束位置
	 * @return this
	 */
	public MStr delete(int startIndex, int endIndex) {
		_Sb.delete(startIndex, endIndex);
		return this;
	}

	/**
	 * 插入字符串
	 * 
	 * @param index 位置
	 * @param val   字符串
	 * @return this
	 */
	public MStr insert(int index, Object val) {
		_Sb.insert(index, val);
		return this;
	}

	/**
	 * 只替换一次
	 * 
	 * @param find
	 * @param val
	 * @return this
	 */
	public MStr replace(String find, String val) {
		Utils.replaceStringBuilder(_Sb, find, val);
		return this;
	}

	/**
	 * 返回字符串
	 * 
	 * @return string
	 */
	public String toString() {
		return _Sb.toString();
	}

	/**
	 * 新行字符串，默认是\r\n
	 * 
	 * @return the newLine
	 */
	public String getNewLine() {
		return newLine;
	}

	/**
	 * 设置新行字符串，默认是\r\n
	 * 
	 * @param newLine the newLine to set
	 */
	public void setNewLine(String newLine) {
		this.newLine = newLine;
	}
}
