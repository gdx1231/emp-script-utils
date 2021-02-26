package com.gdxsoft.easyweb.utils;

import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * 使用DES加密与解密,可对byte[],String类型进行加密与解密 密文可使用String,byte[]存储. 方法: void
 * getKey(String strKey)从strKey的字条生成一个Key String getEncString(String
 * strMing)对strMing进行加密,返回String密文 String getDesString(String
 * strMi)对strMin进行解密,返回String明文 byte[] getEncCode(byte[] byteS)byte[]型的加密 byte[]
 * getDesCode(byte[] byteD)byte[]型的解密
 */
public class UAes {
	public final static String PKCS7Padding = "AES/CBC/PKCS7Padding";
	public final static String PKCS5Padding = "AES/CBC/PKCS5Padding";
	public final static String NoPadding = "AES/CBC/NoPadding";

	private static String AES_KEY_VALUE;
	private static String AES_IV_VALUE;

	/*
	 * AES/CBC/NoPadding 要求 密钥必须是16位的；Initialization vector (IV) 必须是16位
	 * 待加密内容的长度必须是16的倍数，如果不是16的倍数，就会出如下异常： javax.crypto.IllegalBlockSizeException:
	 * Input length not multiple of 16 bytes
	 * 
	 * 由于固定了位数，所以对于被加密数据有中文的, 加、解密不完整
	 * 
	 * 可 以看到，在原始数据长度为16的整数n倍时，假如原始数据长度等于16*n，则使用NoPadding时加密后数据长度等于16*n， 其它情况下加密数据长
	 * 度等于16*(n+1)。在不足16的整数倍的情况下，假如原始数据长度等于16*n+m[其中m小于16]， 除了NoPadding填充之外的任何方
	 * 式，加密数据长度都等于16*(n+1).
	 */
	private static String DEF_METHOD = NoPadding; // "AES/CBC/NoPadding";
	private SecretKeySpec keySpec;
	private IvParameterSpec ivSpec;
	private Cipher encCipher;
	private Cipher deCipher;
	private String method; // aes transformation 加密模式

	static {
		new URsa();
	}

	/**
	 * 初始化默认的 key和iv
	 * 
	 * @param key 默认的密码
	 * @param iv  默认的向量
	 */
	public synchronized static void initDefaultKey(String key, String iv) {
		AES_KEY_VALUE = key;
		AES_IV_VALUE = iv;
	}

	/**
	 * 获取默认密码的 AES (aes128cbc)
	 * 
	 * @return 默认密码的 AES
	 * @throws Exception
	 */
	public synchronized static UAes getInstance() throws Exception {
		if (AES_KEY_VALUE == null || AES_IV_VALUE == null) {
			throw new Exception("请用 UAes.initDefaultKey 初始化");
		}
		UAes aes = new UAes(AES_KEY_VALUE, AES_IV_VALUE);
		return aes;
	}

	/**
	 * 初始化
	 */
	public UAes() {
	}

	/**
	 * 初始化
	 * 
	 * @param key 密码
	 * @param iv  向量
	 */
	public UAes(String key, String iv) {
		byte[] ivBuf = iv.getBytes();
		byte[] keyBuf = key.getBytes();

		this.init(keyBuf, ivBuf);
	}

	/**
	 * 初始化
	 * 
	 * @param keyBuf 密码
	 * @param ivBuf  向量
	 */
	public UAes(byte[] keyBuf, byte[] ivBuf) {
		this.init(keyBuf, ivBuf);
	}

	/**
	 * 初始化 key,iv
	 * 
	 * @param keyBuf 密码
	 * @param ivBuf  向量
	 */
	private void init(byte[] keyBuf, byte[] ivBuf) {
		byte[] ivBytes = new byte[16];// IV length: must be 16 bytes long

		System.arraycopy(ivBuf, 0, ivBytes, 0, ivBuf.length > ivBytes.length ? ivBytes.length : ivBuf.length);
		for (int i = ivBuf.length; i < ivBytes.length; i++) {
			// 不够16位的，补充ff
			ivBytes[i] = (byte) 0xff;
		}

		/**
		 * 设置AES密钥长度 AES要求密钥长度为128位或192位或256位，java默认限制AES密钥长度最多128位
		 * 如需192位或256位，则需要到oracle官网找到对应版本的jdk下载页，在"Additional Resources"中找到 "Java
		 * Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy
		 * Files",点击[DOWNLOAD]下载
		 * 将下载后的local_policy.jar和US_export_policy.jar放到jdk安装目录下的jre/lib/security/目录下，替换该目录下的同名文件
		 */

		byte[] key128Bits = new byte[16]; // 128bit

		System.arraycopy(keyBuf, 0, key128Bits, 0,
				keyBuf.length > key128Bits.length ? key128Bits.length : keyBuf.length);
		for (int i = keyBuf.length; i < key128Bits.length; i++) {
			// 不够16位的，补充ff
			key128Bits[i] = (byte) 0xff;
		}

		SecretKeySpec keyspec = new SecretKeySpec(key128Bits, "AES");
		IvParameterSpec ivspec = new IvParameterSpec(ivBytes);

		this.keySpec = keyspec;
		this.ivSpec = ivspec;
	}

	/**
	 * 加密明文
	 * 
	 * @param source      明文
	 * @param charsetName 字符集名称 utf8/gbk ...
	 * @return base64编码的密文
	 * @throws Exception
	 */
	public String encrypt(String source, String charsetName) throws Exception {
		try {
			byte[] buf = source.getBytes(charsetName);
			byte[] byteMi = this.encryptBytes(buf);
			String strMi = UConvert.ToBase64String(byteMi);
			return strMi;
		} catch (Exception e) {
			throw e;
		} finally {
		}
	}

	/**
	 * 加密明文
	 * 
	 * @param source UTF8明文
	 * @return base64编码的密文
	 * @throws Exception
	 */
	public String encrypt(String source) throws Exception {
		return this.encrypt(source, "UTF8");
	}

	/**
	 * 加密明文 ，同 encrypt
	 * 
	 * @param source 明文
	 * @return base64编码的密文
	 * @throws Exception
	 */
	@Deprecated
	public String encode(String source) throws Exception {
		return this.encrypt(source);
	}

	/**
	 * 加密明文，同 encrypt
	 * 
	 * @param strMing 明文
	 * @return base64编码的密文
	 * @throws Exception
	 */
	@Deprecated
	public String getEncString(String strMing) throws Exception {

		return this.encrypt(strMing);

	}

	/**
	 * 解密 以String密文输入,String明文输出
	 * 
	 * @param base64Encrypt 密文
	 * @return 明文
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
	 * 解密 以String密文输入,String明文输出
	 * 
	 * @param base64Encrypt 密文
	 * @return 明文 UTF8
	 * @throws Exception
	 */
	public String decrypt(String base64Encrypt) throws Exception {
		return this.decrypt(base64Encrypt, "UTF8");
	}

	/**
	 * 解密 以String密文输入
	 * 
	 * @param byteMi 密文
	 * @return 明文 UTF8
	 * @throws Exception
	 */
	public String decrypt(byte[] byteMi) throws Exception {
		byte[] byteMing = this.decryptBytes(byteMi);
		String strMing = new String(byteMing, "UTF8");
		return strMing;
	}

	/**
	 * 解密 以String密文输入,String明文输出
	 * 
	 * @param byteMi 密文
	 * @return 明文 UTF8
	 * @throws Exception
	 */
	@Deprecated
	public String getDesString(byte[] byteMi) throws Exception {
		byte[] byteMing = this.decryptBytes(byteMi);
		String strMing = new String(byteMing, "UTF8");
		return strMing;
	}

	/**
	 * 解密
	 * 
	 * @param base64Mi base64编码的密文
	 * @return 明文UTF8
	 * @throws Exception
	 */
	@Deprecated
	public String decode(String base64Mi) throws Exception {
		return this.decrypt(base64Mi);
	}

	/**
	 * 加密
	 * 
	 * @param strMing 明文
	 * @return base64编码的密文
	 * @throws Exception
	 */
	@Deprecated
	public byte[] getEncBytes(String strMing) throws Exception {
		byte[] byteMi = null;
		byte[] byteMing = null;

		try {
			byteMing = strMing.getBytes("UTF8");
			byteMi = this.encryptBytes(byteMing);

		} catch (Exception e) {
			throw e;
		}
		return byteMi;
	}

	/**
	 * 解密 同 decrypt
	 * 
	 * @param strMi 密文
	 * @return 明文
	 * @throws Exception
	 */
	@Deprecated
	public String getDesString(String strMi) throws Exception {
		return decrypt(strMi);
	}

	/**
	 * 解密 ，同 decryptBytes
	 * 
	 * @param bytesEncrypt 密文
	 * @return 明文
	 * @throws Exception
	 */
	@Deprecated
	public byte[] getDesBytes(byte[] bytesEncrypt) throws Exception {
		return this.decryptBytes(bytesEncrypt);
	}

	/**
	 * 填充模式 paadding
	 * 
	 * @return 填充模式
	 */
	public String getAesMethod() {
		if (this.method == null) {
			return DEF_METHOD;
		} else {
			return this.method;
		}
	}

	/**
	 * 加密以byte[]明文输入,byte[]密文输出
	 * 
	 * @param source 明文
	 * @return 密文
	 * @throws Exception
	 */
	public byte[] encryptBytes(byte[] source) throws Exception {
		// AES/CBC/PKCS5Padding
		// AES/CBC/NoPadding
		if (this.encCipher == null) {
			Cipher cipher = Cipher.getInstance(getAesMethod());
			cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

			this.encCipher = cipher;
		}
		byte[] byteFina = null;

		// 填充Padding
		int blockSize = encCipher.getBlockSize();
		int plaintextLength = source.length;
		if (plaintextLength % blockSize != 0) {
			plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
		}
		byte[] plaintext = new byte[plaintextLength];
		System.arraycopy(source, 0, plaintext, 0, source.length);

		byteFina = encCipher.doFinal(plaintext);

		return byteFina;
	}

	/**
	 * 解密以byte[]密文输入,以byte[]明文输出
	 * 
	 * @param bytesEncrypt 密文
	 * @return 明文
	 * @throws Exception
	 */
	public byte[] decryptBytes(byte[] bytesEncrypt) throws Exception {
		byte[] byteFina = null;
		if (deCipher == null) {
			Cipher cipher = Cipher.getInstance(getAesMethod());
			cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
			this.deCipher = cipher;
		}

		byteFina = this.deCipher.doFinal(bytesEncrypt);
		return byteFina;
	}

	/**
	 * 初始化key和 iv，iv =keyBytes倒序
	 * 
	 * @param keyBytes 密码
	 * @throws Exception
	 */
	public void createKey(byte[] keyBytes) throws Exception {
		if (keyBytes.length < 16) {
			throw new Exception("key长度>=16bytes");
		}
		byte[] ivBytes = new byte[16];// IV length: must be 16 bytes long
		for (int i = 0; i < ivBytes.length; i++) {
			ivBytes[i] = keyBytes[keyBytes.length - 1 - i];
		}

		this.init(keyBytes, ivBytes);
	}

	/**
	 * 获取密码
	 * 
	 * @return 密码
	 */
	public SecretKeySpec getKeySpec() {
		return keySpec;
	}

	/**
	 * 设置 密码
	 * 
	 * @param keySpec 密码
	 */
	public void setKeySpec(SecretKeySpec keySpec) {
		this.keySpec = keySpec;
	}

	/**
	 * 获取向量 iv
	 * 
	 * @return 向量 iv
	 */
	public IvParameterSpec getIvSpec() {
		return ivSpec;
	}

	/**
	 * 设置向量 iv
	 * 
	 * @param ivSpec iv
	 */
	public void setIvSpec(IvParameterSpec ivSpec) {
		this.ivSpec = ivSpec;
	}

	/**
	 * 编码对象
	 * 
	 * @return 编码对象
	 */
	public Cipher getEncCipher() {
		return encCipher;
	}

	/**
	 * 编码对象
	 * 
	 * @param encCipher 编码对象
	 */
	public void setEncCipher(Cipher encCipher) {
		this.encCipher = encCipher;
	}

	/**
	 * 获取解码对象
	 * 
	 * @return 解码对象
	 */
	public Cipher getDeCipher() {
		return deCipher;
	}

	/**
	 * 设置解码对象
	 * 
	 * @param deCipher 解码对象
	 */
	public void setDeCipher(Cipher deCipher) {
		this.deCipher = deCipher;
	}

	/**
	 * PADDING 模式，例如 AES/CBC/PKCS7Padding
	 * 
	 * @return 模式
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * PADDING，例如 AES/CBC/PKCS7Padding
	 * 
	 * @param method 模式
	 */
	public void setMethod(String method) {
		this.method = method;
	}

}
