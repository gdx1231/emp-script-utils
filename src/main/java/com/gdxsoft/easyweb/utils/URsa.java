package com.gdxsoft.easyweb.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;

/**
 * RSA工具类
 * 
 * @author admin
 *
 */
public class URsa {
	public static final String KEY_ALGORITHM = "RSA";

	public static final String SIGNATURE_DEFAULT_ALGORITHM = "SHA256withRSA";
	public static final String DIGEST_ALGORITHM = "sha-256";

	public static final String SIGNATURE_SHA256withRSA = "SHA256withRSA";
	public static final String DIGEST_SHA256 = "sha-256";

	public static final String SIGNATURE_SHA1withRSA = "SHA1withRSA";
	public static final String DIGEST_SHA1 = "sha1";

	@Deprecated
	public static final String SIGNATURE_MD5withRSA = "MD5withRSA";
	@Deprecated
	public static final String DIGEST_MD5 = "md5";
	/**
	 * RSA最大加密明文大小
	 */
	private static final int MAX_ENCRYPT_BLOCK = 117;

	/**
	 * RSA最大解密密文大小
	 */
	private static final int MAX_DECRYPT_BLOCK = 128;

	private RSAPrivateKey privateKey;
	private RSAPublicKey publicKey;
	private String signAlgorithm; // 签名算法
	private String digestAlgorithm; // 摘要算法

	static {
		java.security.Security.addProvider(new BouncyCastleProvider());
	}

	public URsa() {
		this.signAlgorithm = SIGNATURE_DEFAULT_ALGORITHM;
		this.digestAlgorithm = DIGEST_ALGORITHM;
	}

	public URsa(String signAlgorithm) {
		this.signAlgorithm = signAlgorithm;
	}

	/**
	 * 创建新的RSA 私匙和公匙
	 * 
	 * @param length 长度
	 * @throws NoSuchAlgorithmException
	 */
	public void generateRsaKeys(int length) throws NoSuchAlgorithmException {
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
		keyPairGen.initialize(length);
		KeyPair keyPair = keyPairGen.generateKeyPair();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

		this.privateKey = privateKey;
		this.publicKey = publicKey;
	}

	/**
	 * 转换PEM文件为key buf
	 * 
	 * @param pemKeyFilePath PEM文件路径
	 * @return
	 * @throws IOException
	 */
	public byte[] readPemKey(String pemKeyFilePath) throws IOException {
		byte[] keyBytes = UFile.readFileBytes(pemKeyFilePath);
		final PemObject pemObject;
		ByteArrayInputStream bis = new ByteArrayInputStream(keyBytes);
		Reader reader = new InputStreamReader(bis);
		PemReader pemReader = new PemReader(reader);
		pemObject = pemReader.readPemObject();

		return pemObject.getContent();
	}

	/**
	 * 通过PEM文件初始化公匙
	 * 
	 * @param pemPublicKeyFilePath 公匙PEM文件
	 * @return 公匙
	 * @throws IOException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	public RSAPublicKey initPublicKey(String pemPublicKeyFilePath)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] keyBytes = this.readPemKey(pemPublicKeyFilePath);
		return this.initPublicKey(keyBytes);
	}

	/**
	 * 初始化公私
	 * 
	 * @param keyBytes 公私二进制
	 * @return 公私
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public RSAPublicKey initPublicKey(byte[] keyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
		this.publicKey = publicKey;
		return publicKey;
	}

	/**
	 * 通过PEM文件初始化私匙
	 * 
	 * @param pemPrivateKeyFilePath 私匙PEM文件
	 * @return 私匙
	 * @throws IOException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	public RSAPrivateKey initPrivateKey(String pemPrivateKeyFilePath)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] keyBytes = this.readPemKey(pemPrivateKeyFilePath);
		return this.initPrivateKey(keyBytes);
	}

	/**
	 * 初始化私匙
	 * 
	 * @param keyBytes 私匙二进制
	 * @return 私匙
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public RSAPrivateKey initPrivateKey(byte[] keyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);

		this.privateKey = privateKey;
		return privateKey;
	}

	/**
	 * 签名，返回base64
	 * 
	 * @param data 需要签名的数据
	 * @return 签名结果 base64
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 */
	public String signBase64(byte[] data) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		byte[] result = this.sign(data);

		return UConvert.ToBase64String(result);
	}

	/**
	 * 签名
	 * 
	 * @param data 需要签名的数据
	 * @return 签名结果
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 * @throws Exception
	 */
	public byte[] sign(byte[] data) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		// 获取信息的摘要
		byte[] digest = this.digestMessage(data);

		Signature sig = Signature.getInstance(signAlgorithm);
		sig.initSign(this.privateKey);
		sig.update(digest);
		byte[] signData = sig.sign();

		return signData;
	}

	/**
	 * 验证签名，签名是base64字符串
	 * 
	 * @param data       数据
	 * @param base64Sign base64签名结果
	 * @return 是否成功
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 * @throws IOException
	 */
	public boolean verifyBase64(byte[] data, String base64Sign)
			throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, IOException {
		byte[] sign = UConvert.FromBase64String(base64Sign);
		return this.verify(data, sign);
	}

	/**
	 * 验证签名
	 * 
	 * @param data 需要验证的数据
	 * @param sign 签名结果
	 * @return 是否成功
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 * @throws Exception
	 */
	public boolean verify(byte[] data, byte[] sign)
			throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

		byte[] digest = this.digestMessage(data);

		Signature sig = Signature.getInstance(signAlgorithm);
		sig.initVerify(this.publicKey);
		sig.update(digest);

		return sig.verify(sign);
	}

	/**
	 * 获取信息的摘要
	 * 
	 * @param data
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public byte[] digestMessage(byte[] data) throws NoSuchAlgorithmException {
		return UDigest.digest(data, digestAlgorithm);
		/*
		 * MessageDigest messageDigest = MessageDigest.getInstance(digestAlgorithm);
		 * messageDigest.update(data); byte[] digest = messageDigest.digest();
		 * 
		 * return digest;
		 */
	}

	/**
	 * 公匙加密
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public byte[] encryptPublic(byte[] data) throws Exception {
		byte[] encryptData = this.encrypt(data, publicKey);
		return encryptData;
	}

	/**
	 * 私匙加密
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public byte[] encryptPrivate(byte[] data) throws Exception {
		byte[] encryptData = this.encrypt(data, privateKey);
		return encryptData;
	}

	/**
	 * 加密
	 * 
	 * @param data 数据
	 * @param key  公匙或私匙
	 * @return 加密内容
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public byte[] encrypt(byte[] data, Key key) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		int inputLen = data.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		int i = 0;
		byte[] cache;
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
				cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(data, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_ENCRYPT_BLOCK;
		}
		byte[] encryptData = out.toByteArray();
		try {
			out.close();
		} catch (IOException e) {
		}
		return encryptData;

	}

	/**
	 * 公匙解密
	 * 
	 * @param base64EncryptData base64加密数据
	 * @return 解密数据
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public byte[] decryptPublic(String base64EncryptData) throws IOException, InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		byte[] encryptData = UConvert.FromBase64String(base64EncryptData);
		return this.decryptPublic(encryptData);
	}

	/**
	 * 公匙解密
	 * 
	 * @param encryptData 加密数据
	 * @return 解密数据
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public byte[] decryptPublic(byte[] encryptData) throws InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		return this.decrypt(encryptData, this.publicKey);
	}

	/**
	 * 私匙解密
	 * 
	 * @param base64EncryptData base64加密数据
	 * @return 解密数据
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public byte[] decryptPrivate(String base64EncryptData) throws IOException, InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		byte[] encryptData = UConvert.FromBase64String(base64EncryptData);
		return this.decryptPrivate(encryptData);
	}

	/**
	 * 私匙解密
	 * 
	 * @param encryptData 加密数据
	 * @return 解密数据
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public byte[] decryptPrivate(byte[] encryptData) throws InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		return this.decrypt(encryptData, privateKey);
	}

	/**
	 * 私匙解密
	 * 
	 * @param encryptData 加密数据
	 * @return 解密数据
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws Exception
	 */
	public byte[] decrypt(byte[] encryptData, Key key) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, key);
		int inputLen = encryptData.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段解密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
				cache = cipher.doFinal(encryptData, offSet, MAX_DECRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(encryptData, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_DECRYPT_BLOCK;
		}
		byte[] plainData = out.toByteArray();
		try {
			out.close();
		} catch (IOException e) {
		}
		return plainData;
	}

	/**
	 * 输出pem格式私匙
	 * 
	 * @return pem格式私匙
	 * @throws IOException
	 */
	public String privateKeyToPem() throws IOException {
		return this.toPem(this.privateKey, "RSA PRIVATE KEY");
	}

	/**
	 * 输出 pem格式公匙
	 * 
	 * @return pem格式公匙
	 * @throws IOException
	 */
	public String publicKeyToPem() throws IOException {
		return this.toPem(this.publicKey, "PUBLIC KEY");
	}

	/**
	 * 输出 pem 格式
	 * 
	 * @param key
	 * @param type
	 * @return
	 * @throws IOException
	 */
	private String toPem(Key key, String type) throws IOException {
		PemObject pemObject = new PemObject(type, key.getEncoded());
		StringWriter sw = new StringWriter();
		try (PemWriter pw = new PemWriter(sw)) {
			pw.writeObject(pemObject);
		}
		return sw.toString();
	}

	/**
	 * 获取私匙
	 * 
	 * @return 私匙
	 */
	public RSAPrivateKey getPrivateKey() {
		return privateKey;
	}

	/**
	 * 获取公匙
	 * 
	 * @return 公匙
	 */
	public RSAPublicKey getPublicKey() {
		return publicKey;
	}

	/**
	 * 算法
	 * 
	 * @return 算法
	 */
	public String getSignAlgorithm() {
		return signAlgorithm;
	}

	/**
	 * 算法
	 * 
	 * @param signAlgorithm 算法
	 */
	public void setSignAlgorithm(String signAlgorithm) {
		this.signAlgorithm = signAlgorithm;
	}

	/**
	 * 摘要算法
	 * 
	 * @return 摘要算法
	 */
	public String getDigestAlgorithm() {
		return digestAlgorithm;
	}

	/**
	 * 摘要算法
	 * 
	 * @param digestAlgorithm 摘要算法
	 */
	public void setDigestAlgorithm(String digestAlgorithm) {
		this.digestAlgorithm = digestAlgorithm;
	}

	/**
	 * 设置私匙
	 * 
	 * @param privateKey
	 */
	public void setPrivateKey(RSAPrivateKey privateKey) {
		this.privateKey = privateKey;
	}

	/**
	 * 设置公匙
	 * 
	 * @param publicKey
	 */
	public void setPublicKey(RSAPublicKey publicKey) {
		this.publicKey = publicKey;
	}

}