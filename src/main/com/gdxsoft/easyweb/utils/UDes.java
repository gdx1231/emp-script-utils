package com.gdxsoft.easyweb.utils;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

/**
 * 使用DES加密与解密,可对byte[],String类型进行加密与解密 密文可使用String,byte[]存储. 方法: void
 * getKey(String strKey)从strKey的字条生成一个Key String getEncString(String
 * strMing)对strMing进行加密,返回String密文 String getDesString(String
 * strMi)对strMin进行解密,返回String明文 byte[] getEncCode(byte[] byteS)byte[]型的加密 byte[]
 * getDesCode(byte[] byteD)byte[]型的解密
 */
public class UDes {
	private static String DES_KEY_VALUE;
	private static String DES_IV_VALUE;
	private static final String ALGORITHM_DES = "DES/CBC/PKCS5Padding";

	private Key desKey;
	private IvParameterSpec desIv;

	/**
	 * 初始化默认的 key和iv
	 * 
	 * @param key
	 * @param iv
	 */
	public synchronized static void initDefaultKey(String key, String iv) {
		DES_KEY_VALUE = key;
		DES_IV_VALUE = iv;
	}

	/**
	 * 获取默认实例
	 * 
	 * @return 实例
	 * @throws Exception
	 */
	public synchronized static UDes getInstance() throws Exception {
		UDes des = new UDes();
		return des;
	}

	/**
	 * 默认初始化，key来源 DES_KEY_VALUE
	 * 
	 * @throws Exception
	 */
	public UDes() throws Exception {
		if (DES_KEY_VALUE == null || DES_IV_VALUE == null) {
			throw new Exception("请用 UDes.initDefaultKey 初始化");
		}
		initKeyIv(DES_KEY_VALUE, DES_IV_VALUE);
	}


	public UDes(String key, String iv) throws Exception {
		this.initKeyIv(key, iv);
	}

	/**
	 * 初始化 key, iv
	 * @param key
	 * @param iv
	 * @throws Exception
	 */
	private void initKeyIv(String key, String iv) throws Exception {
		DESKeySpec dks = new DESKeySpec(key.getBytes());

		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		// key的长度不能够小于8位字节
		Key secretKey = keyFactory.generateSecret(dks);
		this.desKey = secretKey;

		this.desIv = createIv(iv);
	}

	
	/**
	 * 创建8位的向量
	 * 
	 * @param iv 字符串
	 * @return Iv
	 */
	public static IvParameterSpec createIv(String iv) {
		byte[] ivBytes = new byte[8];
		byte[] buf = iv.getBytes();
		for (int i = 0; i < ivBytes.length; i++) {
			if (i < buf.length) {
				ivBytes[i] = buf[i];
			} else {
				ivBytes[i] = (byte) 255; // 用 ff填充;
			}
		}

		return new IvParameterSpec(ivBytes);
	}

	/**
	 * 加密
	 * 
	 * @param source      原文
	 * @param charsetName 编码格式，例如 UTF8, GBK ...
	 * @return 密文，用base64 编码
	 * @throws Exception
	 */
	public String encrypt(String source, String charsetName) throws Exception {
		byte[] byteMi = null;
		byte[] byteMing = null;
		String strMi = "";
		try {
			byteMing = source.getBytes(charsetName);
			byteMi = this.encyrptBytes(byteMing);
			strMi = UConvert.ToBase64String(byteMi);
		} catch (Exception e) {
			throw e;
		} finally {
			byteMing = null;
			byteMi = null;
		}
		return strMi;
	}

	/**
	 * 加密
	 * 
	 * @param source UTF8原文
	 * @return 密文，用base64 编码
	 * @throws Exception
	 */
	public String encrypt(String source) throws Exception {
		return this.encrypt(source, "UTF8");
	}

	/**
	 * 同 encrypt 加密
	 * 
	 * @param strMing UTF8原文
	 * @return 密文，用base64 编码
	 * @throws Exception
	 */
	public String getEncString(String strMing) throws Exception {
		return encrypt(strMing);
	}

	/**
	 * 解密
	 * 
	 * @param base64Encrypt 加密的base64字符串
	 * @param charsetName   转成字符串的 编码
	 * @return 明码
	 * @throws Exception
	 */
	public String decrypt(String base64Encrypt, String charsetName) throws Exception {
		byte[] byteMing = null;
		byte[] byteMi = null;
		String strMing = "";
		try {
			byteMi = UConvert.FromBase64String(base64Encrypt);
			byteMing = this.decryptBytes(byteMi);
			strMing = new String(byteMing, charsetName);
		} catch (Exception e) {
			throw e;
		} finally {
			byteMing = null;
			byteMi = null;
		}
		return strMing;
	}

	/**
	 * 解码，转换为utf8字符串
	 * 
	 * @param base64Encrypt 加密的base64字符串
	 * @return 明码
	 * @throws Exception
	 */
	public String decrypt(String base64Encrypt) throws Exception {
		return this.decrypt(base64Encrypt, "UTF8");
	}

	/**
	 * 解码，转换为utf8字符串，同 decrypt
	 * 
	 * @param base64Encrypt 加密的base64字符串
	 * @return 明码
	 * @throws Exception
	 */
	public String getDesString(String base64Encrypt) throws Exception {
		return this.decrypt(base64Encrypt);
	}

	/**
	 * 加密，以byte[]明文输入,byte[]密文输出
	 * 
	 * @param sourceBytes 明
	 * @return 密文
	 * @throws Exception
	 */
	public byte[] encyrptBytes(byte[] sourceBytes) throws Exception {
		byte[] byteFina = null;
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(ALGORITHM_DES);
			cipher.init(Cipher.ENCRYPT_MODE, this.desKey, desIv);

			// cipher = Cipher.getInstance("DES/CBC/PKCS5PADDING");
			// cipher.init(Cipher.ENCRYPT_MODE, DES_KEY);
			byteFina = cipher.doFinal(sourceBytes);
		} catch (Exception e) {
			throw e;
		} finally {
			cipher = null;
		}
		return byteFina;
	}

	/**
	 * 解密，以byte[]密文输入,以byte[]明文输出
	 * 
	 * @param bytesEncryption 密文
	 * @return 明文
	 * @throws Exception
	 */
	public byte[] decryptBytes(byte[] bytesEncryption) throws Exception {
		Cipher cipher;
		byte[] byteFina = null;
		try {
			cipher = Cipher.getInstance(ALGORITHM_DES);
			cipher.init(Cipher.DECRYPT_MODE, this.desKey, desIv);
			byteFina = cipher.doFinal(bytesEncryption);
		} catch (Exception e) {
			throw e;
		} finally {
			cipher = null;
		}
		return byteFina;
	}

	/**
	 * DES 密码
	 * 
	 * @return the desKey
	 */
	public Key getDesKey() {
		return desKey;
	}

	/**
	 * DES 向量
	 * 
	 * @return the desIv
	 */
	public IvParameterSpec getDesIv() {
		return desIv;
	}
}
