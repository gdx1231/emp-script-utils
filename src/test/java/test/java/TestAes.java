package test.java;


import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.utils.UAes;
import com.gdxsoft.easyweb.utils.Utils;

public class TestAes extends TestBase {

	public static void main(String[] args) {
		TestAes t = new TestAes();
		t.testAes();
	}

	private String key = "efsd91290123p9023sdkjvjdkl293048192ds9249238490238490234234sdfsdfsdf";
	private String iv = "xsdskdsdflsdl;fl;sd218902sdfjsdxcbu1283`sjl;z";
	private String aad = "s9283ksdvsdklsd2390esfsdfs";
	private String content = "东3圣诞节🐂🐎精神121";

	@Test
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

		try {
			this.testAes(cipherName, true, true);
			this.testAes(cipherName, true, false);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		try {
			this.testAes(cipherName, false, true);
			this.testAes(cipherName, false, false);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	public void testAes(String cipherName, boolean usingBc, boolean autoIv) throws Exception {
		UAes aes = new UAes(key, autoIv ? null : iv, cipherName);
		aes.setUsingBc(usingBc);

		System.out.println("BC: " + usingBc + ", AUTO IV：" + autoIv);

		if (aes.getBlockCipherMode().equals("GCM") || aes.getBlockCipherMode().equals("CCM")) {
			aes.setAdditionalAuthenticationData(aad);
			aes.setMacSizeBits(32);
		}

		aes.setPaddingMethod(UAes.PKCS7Padding);
		byte[] data = content.getBytes();

		byte[] cipherData = aes.encryptBytes(data);
		System.out.println(Utils.bytes2hex(cipherData));

		byte[] plainText = aes.decryptBytes(cipherData);
		System.out.println(new String(plainText));

	}
}
