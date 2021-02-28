package com.gdxsoft.easyweb.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
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

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.signers.RSADigestSigner;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.crypto.util.PublicKeyFactory;
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

	private RSAPrivateKey privateKey;
	private RSAPublicKey publicKey;
	private String signAlgorithm; // 签名算法
	private String digestAlgorithm; // 摘要算法
	private String cliperAlgorithm = "RSA/ECB/PKCS1Padding";

	private boolean usingBc = true;

	static {
		java.security.Security.addProvider(new BouncyCastleProvider());
	}

	/**
	 * sign: SHA256withRSA digest: sha-256
	 */
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
	 * 更加扩展名，初始化公匙
	 * 
	 * @param publicKeyFilePath DER OR PEM 私匙文件
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws IOException
	 */
	public RSAPublicKey initPublicKey(String publicKeyFilePath)
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		String ext = UFile.getFileExt(publicKeyFilePath);

		if (ext.equalsIgnoreCase("der")) {
			return this.initPublicDerKey(publicKeyFilePath);
		} else if (ext.equalsIgnoreCase("pem")) {
			return this.initPublicPemKey(publicKeyFilePath);
		} else {
			return null;
		}

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
	public RSAPublicKey initPublicPemKey(String pemPublicKeyFilePath)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] keyBytes = this.readPemKey(pemPublicKeyFilePath);
		return this.initPublicDerKey(keyBytes);
	}

	/**
	 * 初始化DER 公匙
	 * 
	 * @param derPublicKeyFilePath 公匙DER文件
	 * @return 公匙
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public RSAPublicKey initPublicDerKey(String derPublicKeyFilePath)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] keyBytes = UFile.readFileBytes(derPublicKeyFilePath);
		return this.initPublicDerKey(keyBytes);
	}

	/**
	 * 初始化公私
	 * 
	 * @param keyBytes 公私二进制
	 * @return 公私
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public RSAPublicKey initPublicDerKey(byte[] keyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
		this.publicKey = publicKey;
		return publicKey;
	}

	/**
	 * 根据文件扩展名(xxx.der / xxx.pem)初始化私匙
	 * 
	 * @param privateKeyFilePath PEM/ DER 私匙文件
	 * @return 私匙
	 * @throws IOException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	public RSAPrivateKey initPrivateKey(String privateKeyFilePath)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		String ext = UFile.getFileExt(privateKeyFilePath);

		if (ext.equalsIgnoreCase("der")) {
			return this.initPrivateDerKey(privateKeyFilePath);
		} else if (ext.equalsIgnoreCase("pem")) {
			return this.initPrivatePemKey(privateKeyFilePath);
		} else {
			return null;
		}
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
	public RSAPrivateKey initPrivatePemKey(String pemPrivateKeyFilePath)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] keyBytes = this.readPemKey(pemPrivateKeyFilePath);
		return this.initPrivateKey(keyBytes);
	}

	/**
	 * 通过PEM文件初始化私匙
	 * 
	 * @param derPrivateKeyFilePath 私匙DER文件
	 * @return 私匙
	 * @throws IOException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	public RSAPrivateKey initPrivateDerKey(String derPrivateKeyFilePath)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] keyBytes = UFile.readFileBytes(derPrivateKeyFilePath);
		return this.initPrivateKey(keyBytes);
	}

	/**
	 * 初始化私匙 DER
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
	public String signBase64(byte[] data) throws Exception {
		byte[] result = this.sign(data);

		return UConvert.ToBase64String(result);
	}

	/**
	 * RSA signing (Using java.security or BC According to usingBc parameter)
	 * 
	 * @param data the source data
	 * @return signature
	 * 
	 * @throws Exception
	 */
	public byte[] sign(byte[] data) throws Exception {
		if (this.usingBc) {
			return this.signBc(data);
		} else {
			return this.signJava(data);
		}
	}

	/**
	 * RSA signing (Using java.secruity)
	 * 
	 * @param data the source data
	 * @return signature
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 * @throws Exception
	 */
	public byte[] signJava(byte[] data) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		// 获取信息的摘要
		// byte[] digest = this.digestMessage(data);

		Signature sig = Signature.getInstance(signAlgorithm);
		sig.initSign(this.privateKey);
		sig.update(data);
		byte[] signData = sig.sign();

		return signData;
	}

	/**
	 * RSA signing(Using BC)
	 * 
	 * @param data the source data
	 * @return signature
	 * 
	 * @throws Exception
	 */
	public byte[] signBc(byte[] data) throws IOException, DataLengthException, CryptoException {
		AsymmetricKeyParameter keyParameter = PrivateKeyFactory.createKey(this.privateKey.getEncoded());
		Digest digest = UDigest.getDigest(this.digestAlgorithm);
		RSADigestSigner signer = new RSADigestSigner(digest);
		signer.init(true, keyParameter);
		signer.update(data, 0, data.length);
		byte[] signature = signer.generateSignature();
		return signature;
	}

	/**
	 * Verification the RSA digital signature (Using java.security or BC According
	 * to usingBc parameter)
	 * 
	 * @param data       the source data
	 * @param base64Sign sign the signature of the source data(base64)
	 * @return verify result
	 * 
	 * @throws Exception
	 */
	public boolean verifyBase64(byte[] data, String base64Sign) throws Exception {
		byte[] sign = UConvert.FromBase64String(base64Sign);
		return this.verify(data, sign);
	}

	/**
	 * Verification the RSA digital signature (Using java.security or BC According
	 * to usingBc parameter)
	 * 
	 * @param data the source data
	 * @param sign the signature of the source data
	 * @return verify result
	 * 
	 * @throws Exception
	 */
	public boolean verify(byte[] data, byte[] sign) throws Exception {
		if (this.usingBc) {
			return this.verifyBc(data, sign);
		} else {
			return this.verifyJava(data, sign);
		}
	}

	/**
	 * Verification the RSA digital signature (Using java.security)
	 * 
	 * @param data the source data
	 * @param sign the signature of the source data
	 * @return verify result
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 * @throws Exception
	 */
	public boolean verifyJava(byte[] data, byte[] sign)
			throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature sig = Signature.getInstance(signAlgorithm);
		sig.initVerify(this.publicKey);
		sig.update(data);

		return sig.verify(sign);
	}

	/**
	 * Verification the RSA digital signature(USING BC)
	 * 
	 * @param data the source data
	 * @param sign the signature of the source data
	 * @return verify result
	 * 
	 * @throws IOException
	 */
	public boolean verifyBc(byte[] data, byte[] sign) throws IOException {
		AsymmetricKeyParameter publKey = PublicKeyFactory.createKey(this.publicKey.getEncoded());
		Digest digest = UDigest.getDigest(this.digestAlgorithm);
		RSADigestSigner signer = new RSADigestSigner(digest);
		signer.init(false, publKey);
		signer.update(data, 0, data.length);

		return signer.verifySignature(sign);
	}

	/**
	 * Create data digest(Using java.security or BC According to usingBc parameter)
	 * 
	 * @param data the source data
	 * @return the digest result
	 * 
	 * @throws NoSuchAlgorithmException
	 */
	public byte[] digestMessage(byte[] data) throws NoSuchAlgorithmException {
		if (this.usingBc) {
			return this.digestMessage(data);
		} else {
			return this.digestMessageJava(data);
		}
	}

	/**
	 * Create data digest(Using java.security)
	 * 
	 * @param data the source data
	 * @return the digest result
	 * 
	 * @throws NoSuchAlgorithmException
	 */
	public byte[] digestMessageJava(byte[] data) throws NoSuchAlgorithmException {

		MessageDigest messageDigest = MessageDigest.getInstance(digestAlgorithm);
		messageDigest.update(data);
		byte[] digest = messageDigest.digest();

		return digest;

	}

	/**
	 * Create data digest(Using BC)
	 * 
	 * @param data the source data
	 * @return the digest result
	 */
	public byte[] digestMessageBc(byte[] data) {
		return UDigest.digest(data, digestAlgorithm);
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
	 * Encryption (Using java.security or BC According to usingBc parameter)
	 * 
	 * @param data the plain data
	 * @param key  publicKey / privateKey
	 * @return the encryption data
	 * 
	 * @throws Exception
	 */
	public byte[] encrypt(byte[] data, Key key) throws Exception {
		if (this.usingBc) {
			return this.encryptBc(data, key);
		} else {
			return this.encryptJava(data, key);
		}
	}

	/**
	 * Encryption (Using java.security)
	 * 
	 * @param data the plain data
	 * @param key  publicKey/ privateKey
	 * @return the encryption data
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public byte[] encryptJava(byte[] data, Key key) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

		Cipher cipher = Cipher.getInstance(cliperAlgorithm);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] encryptData = cipher.doFinal(data);
		return encryptData;

	}

	/**
	 * Encryption (Using BC)
	 * 
	 * @param data the plain data
	 * @param key  publicKey/ privateKey
	 * @return the encryption data
	 * 
	 * @throws IOException
	 * @throws InvalidCipherTextException
	 */
	public byte[] encryptBc(byte[] data, Key key) throws IOException, InvalidCipherTextException {
		AsymmetricBlockCipher e = this.createAsymmetricBlockCipher(key, true);
		byte[] encryptData = e.processBlock(data, 0, data.length);
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
	public byte[] decryptPublic(String base64EncryptData) throws Exception {
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
	public byte[] decryptPublic(byte[] encryptData) throws Exception {
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
	public byte[] decryptPrivate(String base64EncryptData) throws Exception {
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
	public byte[] decryptPrivate(byte[] encryptData) throws Exception {
		return this.decrypt(encryptData, privateKey);
	}

	/**
	 * Decryption
	 * 
	 * @param encryptData Encrypted data
	 * @param key         publicKey / privateKey
	 * @return the plain data
	 * @throws Exception
	 */
	public byte[] decrypt(byte[] encryptData, Key key) throws Exception {
		if (this.usingBc) {
			return this.decryptBc(encryptData, key);
		} else {
			return this.decryptJava(encryptData, key);
		}
	}

	/**
	 * Decryption (Using java)
	 * 
	 * @param encryptData Encrypted data
	 * @param key         publicKey / privateKey
	 * @return the plain data
	 * 
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws Exception
	 */
	public byte[] decryptJava(byte[] encryptData, Key key) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

		Cipher cipher = Cipher.getInstance(cliperAlgorithm);
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] plainData = cipher.doFinal(encryptData);
		return plainData;
	}

	/**
	 * Decryption (Using BC)
	 * 
	 * @param encryptData Encrypted data
	 * @param key         PublicKey/ PrivateKey
	 * @return the plain data
	 * 
	 * @throws IOException
	 * @throws InvalidCipherTextException
	 */
	public byte[] decryptBc(byte[] encryptData, Key key) throws IOException, InvalidCipherTextException {

		AsymmetricBlockCipher e = this.createAsymmetricBlockCipher(key, false);
		byte[] result = e.processBlock(encryptData, 0, encryptData.length);

		return result;
	}

	/**
	 * Create an BC cipher
	 * 
	 * @param key       privateKey/ publicKey
	 * @param isEncrypt true=Encryption, false=Decryption
	 * @return the Cipher
	 * @throws IOException
	 */
	private AsymmetricBlockCipher createAsymmetricBlockCipher(Key key, boolean isEncrypt) throws IOException {
		boolean isPublicKey = true;
		String keyName = key.getClass().getName().toLowerCase();
		if (keyName.indexOf("private") >= 0) {
			isPublicKey = false;
		}
		AsymmetricKeyParameter keyParameter = isPublicKey ? PublicKeyFactory.createKey(key.getEncoded())
				: PrivateKeyFactory.createKey(key.getEncoded());
		AsymmetricBlockCipher e = new RSAEngine();
		e = new org.bouncycastle.crypto.encodings.PKCS1Encoding(e);

		e.init(isEncrypt, keyParameter);
		return e;
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

	/**
	 * Using java.security or BC for encryption/decryption according to parameter
	 * usingBc
	 * 
	 * @return true= BC, false= java.security
	 */
	public boolean isUsingBc() {
		return usingBc;
	}

	/**
	 * Using java.security or BC for encryption/decryption according to parameter
	 * usingBc
	 * 
	 * @param usingBc true= BC, false= java.security
	 */
	public void setUsingBc(boolean usingBc) {
		this.usingBc = usingBc;
	}
}