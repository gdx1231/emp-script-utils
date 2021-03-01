package com.gdxsoft.easyweb.utils;

/**
 * Symmetric Encrypt/ Decrypt Interface
 *
 */
public interface IUSymmetricEncyrpt {

	/**
	 * 加密
	 * 
	 * @param source      原文
	 * @param charsetName 编码格式，例如 UTF8, GBK ...
	 * @return 密文，用base64 编码
	 * @throws Exception
	 */
	String encrypt(String source, String charsetName) throws Exception;

	/**
	 * 加密
	 * 
	 * @param source UTF8原文
	 * @return 密文，用base64 编码
	 * @throws Exception
	 */
	String encrypt(String source) throws Exception;

	/**
	 * 解密
	 * 
	 * @param base64Encrypt 加密的base64字符串
	 * @param charsetName   转成字符串的 编码
	 * @return 明码
	 * @throws Exception
	 */
	String decrypt(String base64Encrypt, String charsetName) throws Exception;

	/**
	 * 解码，转换为utf8字符串
	 * 
	 * @param base64Encrypt 加密的base64字符串
	 * @return 明码
	 * @throws Exception
	 */
	String decrypt(String base64Encrypt) throws Exception;

	/**
	 * 加密，以byte[]明文输入,byte[]密文输出 encrypt
	 * 
	 * @param sourceBytes 明
	 * @return 密文
	 * @throws Exception
	 */
	byte[] encryptBytes(byte[] sourceBytes) throws Exception;

	/**
	 * 解密，以byte[]密文输入,以byte[]明文输出
	 * 
	 * @param bytesEncryption 密文
	 * @return 明文
	 * @throws Exception
	 */
	public byte[] decryptBytes(byte[] bytesEncryption) throws Exception;
}
