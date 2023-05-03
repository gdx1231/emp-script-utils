package test.java;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Test;
import com.gdxsoft.easyweb.utils.UAes;
import com.gdxsoft.easyweb.utils.UDes;
import com.gdxsoft.easyweb.utils.UDigest;



public class TestEncyrpt extends TestBase {

	public static void main(String[] a) {
		TestEncyrpt t = new TestEncyrpt();
		try {
			t.testEncyrpt();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String text = "abc收到反馈速度123";

	@Test
	public void testEncyrpt() throws Exception {
		super.printCaption("测试加密解密");

		String ss = UDigest.digestBase64("gdx都是", "HmacSHA256", "ba00fd950a24b1f15606e70f02f8b825");
		System.err.println("gdx都是: "+ ss);
		
		String ss1 = doSignatureBase64("gdx都是",   "ba00fd950a24b1f15606e70f02f8b825");
		System.err.println("gdx都是: "+ ss1);
		
		
		UDes.initDefaultKey("受到老师发了塑料袋封口受到法律", "823482389429");
		testDes();

		testDes("kslfklssksdfsdflfklsd", "skfsdfksdfksdf");

		UAes.initDefaultKey("受到老师发了塑料袋封口受到法律", "823482389429");
		testAes();
		testAes2();

		testAes("kslfklssksdfsdflfklsd", "skdsdf");
	}
	public   String doSignatureBase64(String message, String secret) throws  Exception {
		String algorithm = "HmacSHA256";
		Mac hmacSha256;
		String digestBase64 = null;
			hmacSha256 = Mac.getInstance(algorithm);
			byte[] keyBytes = secret.getBytes("UTF-8");
			byte[] messageBytes = message.getBytes("UTF-8");
			hmacSha256.init(new SecretKeySpec(keyBytes, 0, keyBytes.length, algorithm));
			// 使用HmacSHA256对二进制数据消息Bytes计算摘要
			byte[] digestBytes = hmacSha256.doFinal(messageBytes);
			// 把摘要后的结果digestBytes使用Base64进行编码
			digestBase64 = Base64.encodeBase64String(digestBytes);
		 
		return digestBase64;
	}
	private void testAes(String key, String iv) throws Exception {
		super.printCaption("AES " + "密码：" + key + ", 向量：" + iv);

		UAes aa = new UAes(key, iv, UAes.AES_256_CCM);

		aa.setPaddingMethod(UAes.PKCS7Padding);

		System.out.println(text);

		String bb = aa.encrypt(text);
		System.out.println(bb);

		UAes aes1 = new UAes(key, iv, UAes.AES_256_CCM);
		aes1.setPaddingMethod(UAes.PKCS7Padding);
		String cc = aes1.decrypt(bb).trim();
		System.out.println(cc);

	}

	private void testAes() throws Exception {

		super.printCaption("AES(默认密码，向量)");
		UAes aes = UAes.getInstance();
		aes.setPaddingMethod(UAes.PKCS5Padding);
		System.out.println(text);

		String mi = aes.encrypt(text);
		System.out.println(mi);

		String de = aes.decrypt(mi).trim();
		System.out.println(de);
	}

	private void testAes2() throws Exception {

		super.printCaption("AES(默认密码，向量)");
		UAes aes = UAes.getInstance();
		aes.setPaddingMethod(UAes.PKCS7Padding);
		System.out.println(text);

		String mi = aes.encrypt(text);
		System.out.println(mi);

		String de = aes.decrypt(mi).trim();
		System.out.println(de);
	}

	private void testDes() throws Exception {
		super.printCaption("DES(默认密码，向量)");
		System.out.println(text);

		UDes des = new UDes();
		String mi = des.encrypt(text);
		System.out.println(mi);

		String de = des.decrypt(mi);
		System.out.println(de);

	}

	private void testDes(String key, String iv) throws Exception {
		super.printCaption("DES " + "密码：" + key + ", 向量：" + iv);

		System.out.println(text);
		UDes des = new UDes(key, iv);
		String mi = des.encrypt(text);
		System.out.println(mi);

		String de = des.decrypt(mi);
		System.out.println(de);

	}

}
