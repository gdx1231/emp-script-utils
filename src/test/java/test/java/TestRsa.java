package test.java;

import java.io.File;

import com.gdxsoft.easyweb.utils.UConvert;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.URsa;

public class TestRsa extends TestBase {

	private URsa rsa;
	private String privateKeyFilePath;
	private String publicKeyFilePath;

	public static void main(String[] a) throws Throwable {
		TestRsa test = new TestRsa();
		test.testRsa();
	}

	public void testRsa() throws Throwable {
		super.printCaption("测试RSA");

		this.testGenKeys();

		super.printCaption("私匙加密，公匙解密");
		this.testPrivateEncryptPublicDecrypt();

		super.printCaption("公匙加密，私匙解密");
		this.testPublicEncryptPrivateDecrypt();

		super.printCaption("签名验证");
		this.testSign();

	}

	private void testGenKeys() throws Throwable {
		this.rsa = new URsa();
		rsa.generateRsaKeys(1024);

		File privateKeyFile = File.createTempFile("test_private", ".pem");
		String privateKey = rsa.privateKeyToPem();
		System.out.println(privateKey);

		this.privateKeyFilePath = privateKeyFile.getAbsolutePath();
		UFile.createNewTextFile(privateKeyFilePath, privateKey);
		System.out.println(privateKeyFilePath);

		String publicKey = rsa.publicKeyToPem();
		File publicKeyFile = File.createTempFile("test_public", ".pem");
		System.out.println(publicKey);

		this.publicKeyFilePath = publicKeyFile.getAbsolutePath();
		UFile.createNewTextFile(publicKeyFilePath, publicKey);

		System.out.println(publicKeyFilePath);
	}

	/**
	 * 私匙加密，公匙解密
	 * 
	 * @throws Exception
	 */
	private void testPrivateEncryptPublicDecrypt() throws Exception {
		this.rsa = new URsa();
		this.rsa.initPublicKey(this.publicKeyFilePath);
		this.rsa.initPrivateKey(this.privateKeyFilePath);

		String input = "私匙加密，公匙解密";
		System.out.println(input);

		// 加密
		byte[] encryptData = rsa.encryptPrivate(input.getBytes());
		String cipherText = UConvert.ToBase64String(encryptData);
		System.out.println(cipherText);

		// 解密
		byte[] plainData = rsa.decryptPublic(encryptData);
		String plainText = new String(plainData, "utf-8");
		System.out.println(plainText);

	}

	/**
	 * 公匙加密，私匙解密
	 * 
	 * @throws Exception
	 */
	private void testPublicEncryptPrivateDecrypt() throws Exception {
		String input = " 公匙加密，私匙解密";
		System.out.println(input);

		// 加密
		byte[] encryptData = rsa.encryptPublic(input.getBytes());
		String cipherText = UConvert.ToBase64String(encryptData);
		System.out.println(cipherText);

		// 解密
		byte[] plainData = rsa.decryptPrivate(encryptData);
		String plainText = new String(plainData, "utf-8");
		System.out.println(plainText);
	}

	private void testSign() throws Exception {
		StringBuilder sb = new StringBuilder();
		String str = "是的分裂势力的方式打开是独立访客数量大幅";
		for (int i = 0; i < 1100; i++) {
			sb.append(str);
		}
		byte[] data = sb.toString().getBytes();
		String signature = rsa.signBase64(data);
		System.out.println(signature);

		boolean status = rsa.verifyBase64(data, signature);
		System.out.println("验证情况：" + status);
		
		
	}

}
