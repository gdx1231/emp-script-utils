package test.java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.conf.ConfArgon2;

public class TestConfArgon2 extends TestBase {

	@Test
	public void testDefaultValues() {
		printCaption("ConfArgon2 default values");
		ConfArgon2 cfg = ConfArgon2.getInstance();
		assertNotNull(cfg);
		assertEquals(1024, cfg.getMemoryKB(), "Default memory should be 1MB");
		assertEquals(3, cfg.getIterations(), "Default iterations should be 3");
	}

	@Test
	public void testSingleton() {
		printCaption("ConfArgon2 singleton");
		ConfArgon2 a = ConfArgon2.getInstance();
		ConfArgon2 b = ConfArgon2.getInstance();
		assertSame(a, b, "Should return same instance");
	}
}
