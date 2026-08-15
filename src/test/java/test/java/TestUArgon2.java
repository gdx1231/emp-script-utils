package test.java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.utils.UArgon2;

public class TestUArgon2 extends TestBase {

	@Test
	public void testHashAndVerify() {
		printCaption("Argon2 hash and verify");
		String password = "MySecureP@ss123!";

		String hashed = UArgon2.hashPwd(password);
		assertNotNull(hashed);
		assertTrue(hashed.startsWith("$argon2id$v=19$m=1024,t=3,p=1$"),
				"Hash should start with argon2id prefix, got: " + hashed);

		// Correct password verifies
		assertTrue(UArgon2.verifyPwd(password, hashed),
				"Correct password should verify");

		// Wrong password fails
		assertFalse(UArgon2.verifyPwd("WrongPassword", hashed),
				"Wrong password should not verify");
	}

	@Test
	public void testMultipleHashesDifferent() {
		printCaption("Each hash has unique salt");
		String password = "same-password";

		String h1 = UArgon2.hashPwd(password);
		String h2 = UArgon2.hashPwd(password);

		// Same password should produce different hashes (different salts)
		assertNotEquals(h1, h2, "Same password should produce different hashes");

		// Both should verify
		assertTrue(UArgon2.verifyPwd(password, h1));
		assertTrue(UArgon2.verifyPwd(password, h2));
	}

	@Test
	public void testNullAndEmptyPassword() {
		printCaption("Null/empty password handling");
		assertFalse(UArgon2.verifyPwd(null, "$argon2id$v=19$..."));
		assertFalse(UArgon2.verifyPwd("", "$argon2id$v=19$..."));
		assertFalse(UArgon2.verifyPwd("pwd", null));
		assertFalse(UArgon2.verifyPwd("pwd", ""));
	}

	@Test
	public void testDefaultMemory1024() {
		printCaption("Default memory is 1024KB (1MB) via hash parsing");
		// Verify default is 1MB by checking the hash output format
		String hashed = UArgon2.hashPwd("test");
		// Format: $argon2id$v=19$m=1024,t=3,p=1$...
		assertTrue(hashed.contains("m=1024,") || hashed.contains("m=1024$"),
				"Hash should contain m=1024 (1MB), got: " + hashed);
		assertTrue(hashed.contains("t=3,") || hashed.contains("t=3$"),
				"Hash should contain t=3 iterations, got: " + hashed);
	}

	@Test
	public void testArgon2TypeEncoding() {
		printCaption("Argon2 type encoding in hash string");
		String hashed = UArgon2.hashPwd("test");
		assertNotNull(hashed);

		// Format: $argon2id$v=19$m=...,t=...,p=...$salt$hash
		String[] parts = hashed.split("\\$");
		assertTrue(parts.length >= 6, "Hash should have at least 6 segments");

		// parts[0] is empty (before first $), parts[1]="argon2id"
		assertEquals("argon2id", parts[1]);
		assertTrue(parts[2].startsWith("v="));
		assertTrue(parts[3].contains("m="));
		assertTrue(parts[3].contains("t="));
		assertTrue(parts[3].contains("p="));
	}
}
