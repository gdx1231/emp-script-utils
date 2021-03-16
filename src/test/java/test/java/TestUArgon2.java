package test.java;


import org.junit.Test;

import com.gdxsoft.easyweb.utils.UArgon2;

public class TestUArgon2 extends TestBase {

	public static void main(String[] args) {
		TestUArgon2 t = new TestUArgon2();
		t.test();
	}

	@Test
	public void test() {
		super.printCaption("Test UArgon2");
		String password = "sdi912390190";
		 
		this.hash(password);
		this.hash(password);
	}

	private void hash(String password) {
		System.out.println(password);
		String s = UArgon2.hashPwd(password );
		System.out.println(s);
		boolean isok = UArgon2.verifyPwd(password, s);
		System.out.println(isok);
	}
}
