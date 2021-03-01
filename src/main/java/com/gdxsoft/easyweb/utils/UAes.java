package com.gdxsoft.easyweb.utils;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.AEADBlockCipher;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.modes.CCMBlockCipher;
import org.bouncycastle.crypto.modes.CFBBlockCipher;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.modes.OFBBlockCipher;
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 */
public class UAes {
	// GCM (Galois Counter Mode)
	// GCM ciphers are the most widely used block ciphers worldwide. Mandatory as of
	// TLS 1.2 (2008) and used by default by most clients.
	// Message authentication (via GMAC/GHASH) is done on the ciphertext. (This is
	// desirable most of the time.) Note that in most implementations, the auth
	// check and decryption happen in parallel for performance reasons.
	// Performance costs 1 x AES operation and 1 x GHASH per block (GHASH generally
	// faster than AES, so GCM is faster)
	// Encrypt/decrypt of multiple blocks can be parallelized nicely
	// GCM基于并行化设计，可以提供高效的吞吐率和低成本、低延迟。其本质是消息在变型的CTR模式下加密，密文结果与密钥以及消息长度信息在GF(2128)域上相乘。该标准还同时制定了仅支持MAC的工作模式即GMAC。
	// GCM模式使用两个函数：带密钥的Hash函数GHASH，以及计数器每次增1 的CTR模式的GCTR。
	public final static String AES_128_GCM = "aes-128-gcm";
	public final static String AES_192_GCM = "aes-192-gcm";
	public final static String AES_256_GCM = "aes-256-gcm";

	/*
	 * 分组密码链接-消息认证码--CCM Counter with CBC-MAC
	 * 组成CCM的关键算法是AES加密算法、CTR工作模式和CMAC认证算法，在加密和MAC算法中共用一个密钥K。
	 * CCM加密过程的输入由三部分构成：
	 * 1、将要被加密和认证的数据，即明文消息P数据块
	 * 2、将要被认证，但是不需要加密的相关数据A，如协议头等。
	 * 3、临时量N，作为负载和相关数据的补充，对每条消息N取值唯一，以防止重放攻击等。
	 */
	public final static String AES_128_CCM = "aes-128-ccm";
	public final static String AES_192_CCM = "aes-192-ccm";
	public final static String AES_256_CCM = "aes-256-ccm";
	
	// 密码分组链接模式（Cipher Block Chaining (CBC)
	public final static String AES_128_CBC = "aes-128-cbc";
	public final static String AES_192_CBC = "aes-192-cbc";
	public final static String AES_256_CBC = "aes-256-cbc";

	// 密码反馈模式（Cipher FeedBack (CFB)）
	public final static String AES_128_CFB = "aes-128-cfb";
	public final static String AES_192_CFB = "aes-192-cfb";
	public final static String AES_256_CFB = "aes-256-cfb";

	// 计算器模式（Counter (CTR)
	public final static String AES_128_CTR = "aes-128-ctr";
	public final static String AES_192_CTR = "aes-192-ctr";
	public final static String AES_256_CTR = "aes-256-ctr";

	// 输出反馈模式（Output FeedBack (OFB)）
	public final static String AES_128_OFB = "aes-128-ofb";
	public final static String AES_192_OFB = "aes-192-ofb";
	public final static String AES_256_OFB = "aes-256-ofb";

	// 电码本模式（Electronic Codebook Book (ECB)）
	public final static String AES_128_ECB = "aes-128-ecb";
	public final static String AES_192_ECB = "aes-192-ecb";
	public final static String AES_256_ECB = "aes-256-ecb";

	public final static String PKCS7Padding = "PKCS7Padding";
	public final static String PKCS5Padding = "PKCS5Padding";
	public final static String NoPadding = "NoPadding";

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

	private SecretKeySpec keySpec;
	private IvParameterSpec ivSpec;
	private Cipher encCipher;
	private Cipher deCipher;
	private String paddingMethod; // aes transformation 加密模式
	private String cipherName;
	private byte[] iv;
	private byte[] key;

	// Continues a multi-part update of the Additional AuthenticationData (AAD).
	// Calls to this method provide AAD to the cipher when operating inmodes such as
	// AEAD (GCM/CCM).

	private String additionalAuthenticationData;

	// if (macSizeBits < 32 || macSizeBits > 128 || macSizeBits % 8 != 0)
	private int macSizeBits = 128;

	private boolean usingBc = true;

	/**
	 * Continues a multi-part update of the Additional AuthenticationData (AAD).
	 * Calls to this method provide AAD to the cipher when operating inmodes such as
	 * AEAD (GCM/CCM).
	 * 
	 * @return the AAD
	 */
	public String getAdditionalAuthenticationData() {
		return additionalAuthenticationData;
	}

	/**
	 * Continues a multi-part update of the Additional AuthenticationData (AAD).
	 * Calls to this method provide AAD to the cipher when operating inmodes such as
	 * AEAD (GCM/CCM).
	 * 
	 * @param additionalAuthenticationData AAD
	 */
	public void setAdditionalAuthenticationData(String additionalAuthenticationData) {
		this.additionalAuthenticationData = additionalAuthenticationData;
	}

	static {
		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
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
		this.cipherName = AES_128_CBC;
	}

	/**
	 * 初始化
	 * 
	 * @param key 密码
	 * @param iv  向量
	 */
	public UAes(String key, String iv) {
		this.cipherName = AES_128_CBC;

		byte[] ivBuf = iv.getBytes(StandardCharsets.UTF_8);
		byte[] keyBuf = key.getBytes(StandardCharsets.UTF_8);

		this.init(keyBuf, ivBuf);
	}

	/**
	 * 初始化
	 * 
	 * @param key 密码
	 * @param iv  向量
	 */
	public UAes(String key, String iv, String cipherName) {
		this.cipherName = cipherName;
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
	 * Encryption data
	 * 
	 * @param plainData data
	 * @return encryption data
	 * @throws Exception
	 */
	public byte[] encryptBytes(byte[] plainData) throws Exception {
		if (this.usingBc) {
			return this.encryptBytesBc(plainData);
		} else {
			return this.encryptBytesJava(plainData);
		}
	}

	/**
	 * 加密以byte[]明文输入,byte[]密文输出
	 * 
	 * @param source 明文
	 * @return 密文
	 * @throws Exception
	 */
	public byte[] encryptBytesJava(byte[] plainData) throws Exception {
		if (this.encCipher == null) {
			Cipher cipher = this.getCipherJava(true);
			this.encCipher = cipher;
		}
		byte[] byteFina = null;

		if (this.getPaddingMethod().equals(NoPadding)) {
			// 填充Padding
			int blockSize = encCipher.getBlockSize();
			int plaintextLength = plainData.length;
			if (plaintextLength % blockSize != 0) {
				plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
			}
			byte[] plaintext = new byte[plaintextLength];
			System.arraycopy(plainData, 0, plaintext, 0, plainData.length);

			byteFina = encCipher.doFinal(plaintext);
		} else {
			byteFina = encCipher.doFinal(plainData);
		}
		return byteFina;
	}

	/**
	 * Decrypt data
	 * 
	 * @param encryptedData the encrypted data
	 * @return plain data
	 * @throws Exception
	 */
	public byte[] decryptBytes(byte[] encryptedData) throws Exception {
		if (this.usingBc) {
			return this.decryptBytesBc(encryptedData);
		} else {
			return this.decryptBytesJava(encryptedData);
		}
	}

	/**
	 * 解密以byte[]密文输入,以byte[]明文输出
	 * 
	 * @param bytesEncrypt 密文
	 * @return 明文
	 * @throws Exception
	 */
	public byte[] decryptBytesJava(byte[] encryptedData) throws Exception {
		byte[] byteFina = null;
		if (deCipher == null) {
			Cipher cipher = this.getCipherJava(false);
			this.deCipher = cipher;
		}

		byteFina = this.deCipher.doFinal(encryptedData);
		return byteFina;
	}

	/**
	 * Block cipher mode of operation
	 * 
	 * @return CBC/CFB/ECB ...
	 */
	public String getBlockCipherMode() {
		String method;
		if (this.cipherName.indexOf("cfb") > 0) {
			method = "CFB";
		} else if (this.cipherName.indexOf("ofb") > 0) {
			method = "OFB";
		} else if (this.cipherName.indexOf("ecb") > 0) {
			method = "ECB";
		} else if (this.cipherName.indexOf("ctr") > 0) {
			method = "CTR";
		} else if (this.cipherName.indexOf("gcm") > 0) {
			method = "GCM";
		} else {
			method = "CBC";
		}

		return method;
	}

	/**
	 * GCM using
	 * 
	 * @return
	 */
	private byte[] createAAD() {
		byte[] aad = (this.additionalAuthenticationData == null || this.additionalAuthenticationData.length() == 0)
				? ivSpec.getIV() // using iv data
				: this.additionalAuthenticationData.getBytes(StandardCharsets.UTF_8);
		return aad;
	}

	/**
	 * Create an AES cipher according to the parameter blockMode
	 * 
	 * @param isEncrypt true=encrypt/ false=decrypt
	 * @return AES cipher
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws NoSuchProviderException
	 */
	private Cipher getCipherJava(boolean isEncrypt) throws InvalidKeyException, InvalidAlgorithmParameterException,
			NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException {

		Cipher cipher;
		String blockMode = this.getBlockCipherMode();
		// AES/CBC/PKCS5Padding AES/CBC/NoPadding
		String transformation = "AES/" + blockMode + "/" + this.getPaddingMethod();
		if (blockMode.equals("GCM")) {
			transformation = "AES/" + blockMode + "/" + NoPadding;
		}
		cipher = Cipher.getInstance(transformation);

		if (blockMode.equals("ECB")) {
			// ECB mode does not use an IV
			cipher.init(isEncrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, keySpec);
		} else if (blockMode.equals("GCM")) {
			// Create GCMParameterSpec
			// if (macSizeBits < 32 || macSizeBits > 128 || macSizeBits % 8 != 0)
			int macSizeBits = this.getMacSizeBits();
			GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(macSizeBits, this.iv);
			cipher.init(isEncrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);

			// Additional AuthenticationData (AAD).
			byte[] aad = this.createAAD();
			cipher.updateAAD(aad);
		} else if (blockMode.equals("CCM")) {
			// Create GCMParameterSpec
			// if (macSizeBits < 32 || macSizeBits > 128 || macSizeBits % 8 != 0)
			int macSizeBits = this.getMacSizeBits();
			GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(macSizeBits, this.iv);
			cipher.init(isEncrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);

			// Additional AuthenticationData (AAD).
			byte[] aad = this.createAAD();
			cipher.updateAAD(aad);
		} else {
			cipher.init(isEncrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, keySpec, ivSpec);
		}

		return cipher;
	}

	/**
	 * Encrypt data (Using bouncy castle)
	 * 
	 * @param plainData source
	 * @return encrypted data
	 * @throws Exception
	 */
	public byte[] encryptBytesBc(byte[] plainData) throws Exception {
		BCCipher cipher = this.getCipherBc(true);
		byte[] result = cipher.processBytes(plainData);
		return result;
	}

	/**
	 * Decrypt data (Using bouncy castle)
	 * 
	 * @param encryptedData the encrypted data
	 * @return plain data
	 * @throws Exception
	 */
	public byte[] decryptBytesBc(byte[] encryptedData) throws Exception {
		BCCipher cipher = this.getCipherBc(false);

		byte[] result = cipher.processBytes(encryptedData);

		return result;

	}

	/**
	 * 初始化 key,iv
	 * 
	 * @param keyBuf 密码
	 * @param ivBuf  向量
	 */
	private void init(byte[] keyBuf, byte[] ivBuf) {

		int keyBitLength = this.getKeyLength();

		byte[] ivBytes = new byte[16];// IV length: must be 16 bytes long
		Arrays.fill(ivBytes, (byte) 0);
		System.arraycopy(ivBuf, 0, ivBytes, 0, ivBuf.length > ivBytes.length ? ivBytes.length : ivBuf.length);
		this.iv = ivBytes;

		/**
		 * 设置AES密钥长度 AES要求密钥长度为128位或192位或256位，java默认限制AES密钥长度最多128位
		 * 如需192位或256位，则需要到oracle官网找到对应版本的jdk下载页，在"Additional Resources"中找到 "Java
		 * Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy
		 * Files",点击[DOWNLOAD]下载
		 * 将下载后的local_policy.jar和US_export_policy.jar放到jdk安装目录下的jre/lib/security/目录下，替换该目录下的同名文件
		 */

		System.out.println(keyBitLength);

		byte[] key = new byte[keyBitLength];
		Arrays.fill(key, (byte) 0);
		System.arraycopy(keyBuf, 0, key, 0, keyBuf.length > key.length ? key.length : keyBuf.length);

		this.key = key;

		SecretKeySpec keyspec = new SecretKeySpec(key, "AES");
		IvParameterSpec ivspec = new IvParameterSpec(ivBytes);

		this.keySpec = keyspec;
		this.ivSpec = ivspec;
	}

	private BlockCipherPadding getPadding() {
		if (this.getPaddingMethod().equals(PKCS7Padding)) {
			BlockCipherPadding pdding = new org.bouncycastle.crypto.paddings.PKCS7Padding();
			return pdding;
		} else if (this.getPaddingMethod().equals(PKCS5Padding)) {
			BlockCipherPadding pdding = new org.bouncycastle.crypto.paddings.PKCS7Padding();
			return pdding;
		} else {
			return null;
		}
	}

	/**
	 * Create an AES cipher according to the parameter blockMode
	 * 
	 * @param isEncyrpt true=encrypt/ false=decrypt
	 * @return BCCipher
	 * @throws InvalidAlgorithmParameterException
	 */
	private BCCipher getCipherBc(boolean isEncyrpt) throws InvalidAlgorithmParameterException {
		AESEngine engine = new AESEngine();

		BCCipher cipher = new BCCipher();
		int bitBlockSize = 16 * 8;
		BlockCipherPadding pdding = getPadding();

		String blockMode = this.getBlockCipherMode();

		boolean usingIV = true;
		if (blockMode.equals("CFB")) {
			if (pdding != null) {
				cipher.cipherBufferBlock = new PaddedBufferedBlockCipher(new CFBBlockCipher(engine, bitBlockSize),
						pdding);
			} else {
				cipher.cipherBufferBlock = new PaddedBufferedBlockCipher(new CFBBlockCipher(engine, bitBlockSize));
			}

		} else if (blockMode.equals("OFB")) {
			if (pdding != null) {
				cipher.cipherBufferBlock = new PaddedBufferedBlockCipher(new OFBBlockCipher(engine, bitBlockSize),
						pdding);
			} else {
				cipher.cipherBufferBlock = new PaddedBufferedBlockCipher(new OFBBlockCipher(engine, bitBlockSize));
			}
		} else if (blockMode.equals("ECB")) {
			usingIV = false;
			if (pdding != null) {
				cipher.cipherBufferBlock = new PaddedBufferedBlockCipher(engine, pdding);
			} else {
				cipher.cipherBufferBlock = new PaddedBufferedBlockCipher(engine);
			}
		} else if (blockMode.equals("CTR")) {
			// Implements the Segmented Integer Counter (SIC) mode on top of a simple block
			// cipher. This mode is also known as CTR mode.
			if (pdding != null) {
				cipher.cipherBufferBlock = new PaddedBufferedBlockCipher(new SICBlockCipher(engine), pdding);
			} else {
				cipher.cipherBufferBlock = new PaddedBufferedBlockCipher(new SICBlockCipher(engine));
			}
		} else if (blockMode.equals("GCM")) {
			cipher.aeadBlockCipher = new GCMBlockCipher(engine);
		}  else if (blockMode.equals("CCM")) {
			cipher.aeadBlockCipher = new CCMBlockCipher(engine);
		} else {
			// default cbc
			if (pdding != null) {
				cipher.cipherBufferBlock = new PaddedBufferedBlockCipher(new CBCBlockCipher(engine), pdding);
			} else {
				cipher.cipherBufferBlock = new PaddedBufferedBlockCipher(new CBCBlockCipher(engine));
			}
		}
		KeyParameter keyP = new KeyParameter(key);
		if (cipher.cipherBufferBlock != null) {
			if (usingIV) {
				CipherParameters cipherParameters = new ParametersWithIV(keyP, iv);
				cipher.cipherBufferBlock.init(isEncyrpt, cipherParameters);
			} else {
				cipher.cipherBufferBlock.init(isEncyrpt, keyP);
			}
		}
		if (cipher.aeadBlockCipher != null) {
			// if (macSizeBits < 32 || macSizeBits > 128 || macSizeBits % 8 != 0)
			int macSizeBits = this.getMacSizeBits();
			AEADParameters parameters = new AEADParameters(keyP, macSizeBits, iv);
			cipher.aeadBlockCipher.init(isEncyrpt, parameters);

			byte[] aad = this.createAAD();
			cipher.aeadBlockCipher.processAADBytes(aad, 0, aad.length);
		}

		return cipher;
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
	 * AES key bytes length
	 * 
	 * @return length
	 */
	public int getKeyLength() {
		if (cipherName.indexOf("192") > 0) {
			return 24;
		} else if (cipherName.indexOf("256") > 0) {
			return 32;
		}
		return 16;
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
	public String getPaddingMethod() {
		return paddingMethod;
	}

	/**
	 * PADDING，例如 AES/CBC/PKCS7Padding
	 * 
	 * @param paddingMethod 模式
	 */
	public void setPaddingMethod(String paddingMethod) {
		this.paddingMethod = paddingMethod;
	}

	public String getCipherName() {
		return cipherName;
	}

	public void setCipherName(String cipherName) {
		this.cipherName = cipherName;
	}

	public int getMacSizeBits() {
		return macSizeBits;
	}

	public void setMacSizeBits(int macSizeBits) {
		this.macSizeBits = macSizeBits;
	}

	public boolean isUsingBc() {
		return usingBc;
	}

	public void setUsingBc(boolean usingBc) {
		this.usingBc = usingBc;
	}
}

class BCCipher {
	BufferedBlockCipher cipherBufferBlock;
	AEADBlockCipher aeadBlockCipher;

	public byte[] processBytes(byte[] source)
			throws DataLengthException, IllegalStateException, InvalidCipherTextException {
		if (this.cipherBufferBlock != null) {
			byte[] buffer = new byte[cipherBufferBlock.getOutputSize(source.length)];
			int pos = cipherBufferBlock.processBytes(source, 0, source.length, buffer, 0);
			pos += cipherBufferBlock.doFinal(buffer, pos);

			return Arrays.copyOf(buffer, pos);
		} else if (this.aeadBlockCipher != null) {
			byte[] buffer = new byte[aeadBlockCipher.getOutputSize(source.length)];
			int pos = aeadBlockCipher.processBytes(source, 0, source.length, buffer, 0);
			pos += aeadBlockCipher.doFinal(buffer, pos);

			return Arrays.copyOf(buffer, pos);
		} else {
			return null;
		}
	}

}
