package test.java;

import com.gdxsoft.easyweb.utils.UAes;
import com.gdxsoft.easyweb.utils.Utils;

public class TestAes extends TestBase {

	public static void main(String[] args) throws Exception {
		TestAes t = new TestAes();
		t.testAes();
	}

	public void testAes() throws Exception {
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

	public void testAes(String cipherName) throws Exception {
		super.printCaption(cipherName);
		String key = "0123456789abcdefghijklmn";
		String iv = "0123456789abcdef";
		
		UAes aes = new UAes(key, iv, cipherName);

		if(aes.getBlockCipherMode().equals("GCM")) {
			aes.setAdditionalAuthenticationData("0123456789abcdef");
			aes.setMacSizeBits(128);
		}
		
		aes.setPaddingMethod(UAes.PKCS7Padding);
		String content = "1234567890-1234567890";
		byte[] data = content.getBytes();
		if (aes.getKeyLength() <= 16) {
			byte[] s0 = aes.encryptBytes(data);
			System.out.println(Utils.bytes2hex(s0));
			byte[] pt = aes.decryptBytesBc(s0);
			System.out.println(new String(pt));

			byte[] s1 = aes.encryptBytesBc(data);
			System.out.println(Utils.bytes2hex(s1));
			byte[] pt1 = aes.decryptBytes(s1);
			System.out.println(new String(pt1));

		} else {
			byte[] s1 = aes.encryptBytesBc(data);
			System.out.println(Utils.bytes2hex(s1));
			byte[] pt1 = aes.decryptBytesBc(s1);
			System.out.println(new String(pt1));

		}
	
	}
}
