package test.java;


import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.utils.UAes;
import com.gdxsoft.easyweb.utils.Utils;

public class TestAes extends TestBase {

	public static void main(String[] args) {
		TestAes t = new TestAes();
		//t.testAes();
		t.test123();
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
	
	public void test123()  {
		String encryptionBc="A49BC9D5D4B869BC17307829975922FA69E05257E7DEAFA54F1A81754924963135A9374AD628CBD1AD3C46FF0C39AD03396A";
		String encryptionJava="2BB346D5F5587977D07E0B68414761A07CFFB714174A561C97A03D2DED24598349B9D628C1627BADD0CA6D973A28BD6D5ADB";
		
		UAes aes = new UAes(key, null, "aes-256-gcm");
		aes.setUsingBc(false);
		aes.setMacSizeBits(32);
		aes.setAdditionalAuthenticationData(aad);
		
		byte[] cipherDataBc =Utils.hex2bytes(encryptionBc);
		byte[] cipherDataJava =Utils.hex2bytes(encryptionJava);
		
		try {
			System.out.println(aes.decrypt(cipherDataJava));
			System.out.println(aes.decrypt(cipherDataBc));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
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
