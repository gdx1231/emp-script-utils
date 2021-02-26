package test.java;

import org.junit.Test;

import com.gdxsoft.easyweb.utils.UAes;
import com.gdxsoft.easyweb.utils.UDes;

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

		UDes.initDefaultKey("受到老师发了塑料袋封口受到法律", "823482389429");
		testDes();

		testDes("kslfklssksdfsdflfklsd", "skfsdfksdfksdf");

		UAes.initDefaultKey("受到老师发了塑料袋封口受到法律", "823482389429");
		testAes();
		testAes2();

		testAes("kslfklssksdfsdflfklsd", "skdsdf");
	}

	private void testAes(String key, String iv) throws Exception {
		super.printCaption("AES " + "密码：" + key + ", 向量：" + iv);

		UAes aa = new UAes(key, iv);

		aa.setMethod(UAes.PKCS7Padding);

		System.out.println(text);

		String bb = aa.encrypt(text);
		System.out.println(bb);

		UAes aes1 = new UAes(key, "isdfkjskldfisdlfjsdkfsd");

		String cc = aes1.decrypt(bb).trim();
		System.out.println(cc);

	}

	private void testAes() throws Exception {

		super.printCaption("AES(默认密码，向量)");
		UAes aes = UAes.getInstance();
		aes.setMethod(UAes.PKCS5Padding);
		System.out.println(text);

		String mi = aes.encrypt(text);
		System.out.println(mi);

		String de = aes.decrypt(mi).trim();
		System.out.println(de);
	}

	private void testAes2() throws Exception {

		super.printCaption("AES(默认密码，向量)");
		UAes aes = UAes.getInstance();
		aes.setMethod(UAes.PKCS7Padding);
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
