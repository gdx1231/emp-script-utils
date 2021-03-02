package test.java;

import com.gdxsoft.easyweb.utils.UAes;
import com.gdxsoft.easyweb.utils.Utils;

public class TestAes extends TestBase {

	public static void main(String[] args) {
		TestAes t = new TestAes();
		t.testAes();
	}

	public void testAes() {
		this.testAes(UAes.AES_128_CCM);

		this.testAes(UAes.AES_128_GCM);

		this.testAes(UAes.AES_128_CTR);
		this.testAes(UAes.AES_128_ECB);
		this.testAes(UAes.AES_128_CBC);
		this.testAes(UAes.AES_128_CFB);
		this.testAes(UAes.AES_128_OFB);

		this.testAes(UAes.AES_192_GCM);
		this.testAes(UAes.AES_192_CTR);
		this.testAes(UAes.AES_192_ECB);
		this.testAes(UAes.AES_192_CBC);
		this.testAes(UAes.AES_192_CFB);
		this.testAes(UAes.AES_192_OFB);

		this.testAes(UAes.AES_256_GCM);
		this.testAes(UAes.AES_256_CTR);
		this.testAes(UAes.AES_256_ECB);
		this.testAes(UAes.AES_256_CBC);
		this.testAes(UAes.AES_256_CFB);
		this.testAes(UAes.AES_256_OFB);
	}

	public void testAes(String cipherName) {
		super.printCaption(cipherName);
		String key = "0123456789abcdefghijklmn";
		String iv = "0123456789abcdef";
		String aad = "0123456789abcdef";

		String content = "1234567890-史蒂夫-1234567890";

		try {
			this.testAes(cipherName, key, iv, aad, true, content);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		try {
			this.testAes(cipherName, key, iv, aad, false, content);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	public void testAes(String cipherName, String key, String iv, String aad, boolean usingBc, String content)
			throws Exception {
		UAes aes = new UAes(key, iv, cipherName);
		aes.setUsingBc(usingBc);
		System.out.println("BC: " + usingBc);

		if (aes.getBlockCipherMode().equals("GCM")) {
			aes.setAdditionalAuthenticationData("0123456789abcdef");
			aes.setMacSizeBits(128);
		}

		aes.setPaddingMethod(UAes.PKCS7Padding);
		byte[] data = content.getBytes();

		byte[] s1 = aes.encryptBytes(data);
		System.out.println(Utils.bytes2hex(s1));
		byte[] pt1 = aes.decryptBytes(s1);
		System.out.println(new String(pt1));
	}
}
