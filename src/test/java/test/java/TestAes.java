package test.java;

import java.io.IOException;

import org.junit.Test;

import com.gdxsoft.easyweb.utils.UAes;
import com.gdxsoft.easyweb.utils.UConvert;
import com.gdxsoft.easyweb.utils.Utils;

public class TestAes extends TestBase {

	public static void main(String[] args) {
		TestAes t = new TestAes();
		t.testAes();
	}

	@Test
	public void testAes() {
		
		String b64="R5npTKH2TdNUdzSFjRwui1mlTQ==";
		try {
			byte[] buf = UConvert.FromBase64String(b64);
			String key = "efsd91290123p9023sdkjvjdkl293048192";
			String iv = "xsdskdsdflsdl;fl;sd";
			String aad = "xxxxxxxxx";
			UAes.initDefaultKey("aes-192-gcm", key, iv, 32, aad);
			
			UAes aes = UAes.getInstance();

			

			//aes.setPaddingMethod(UAes.PKCS7Padding);
			String rst = aes.decrypt(buf);
			System.out.println(rst);
			
			String rst1 = aes.decrypt(buf);
			System.out.println(rst1);
			
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
		
		
		
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
		String key = "efsd91290123p9023sdkjvjdkl293048192";
		String iv = "xsdskdsdflsdl;fl;sd";
		String aad = "xxxxxxxxx";

		String content = "系统管理员";

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

		if (aes.getBlockCipherMode().equals("GCM") || aes.getBlockCipherMode().equals("CCM")) {
			aes.setAdditionalAuthenticationData(aad);
			aes.setMacSizeBits(32);
		}

		aes.setPaddingMethod(UAes.PKCS7Padding);
		byte[] data = content.getBytes();

		byte[] s1 = aes.encryptBytes(data);
		System.out.println(Utils.bytes2hex(s1));
		byte[] pt1 = aes.decryptBytes(s1);
		System.out.println(new String(pt1));
		byte[] pt2 = aes.decryptBytes(s1);
		System.out.println(new String(pt2));
	}
}
