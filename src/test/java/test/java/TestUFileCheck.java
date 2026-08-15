package test.java;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;

import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.utils.UFileCheck;

import static org.junit.jupiter.api.Assertions.*;

public class TestUFileCheck extends TestBase {

	@Test
	public void testNoFalseChangeOnInit() throws Exception {
		printCaption("Initial check returns false");
		File tmp = File.createTempFile("ufc-test", ".txt");
		try {
			Files.writeString(tmp.toPath(), "initial");
			// First check: establish baseline
			assertFalse(UFileCheck.fileChanged(tmp.getAbsolutePath()), "Initial check should return false (baseline)");
			// Immediate recheck: should still be false (cooldown)
			assertFalse(UFileCheck.fileChanged(tmp.getAbsolutePath()), "Recheck within cooldown should return false");
		} finally {
			tmp.delete();
		}
	}

	@Test
	public void testDetectChange() throws Exception {
		printCaption("Detect actual file change");
		File tmp = File.createTempFile("ufc-test", ".txt");
		try {
			Files.writeString(tmp.toPath(), "v1");
			String path = tmp.getAbsolutePath();

			// Establish baseline
			assertFalse(UFileCheck.fileChanged(path, 0), "First check baseline");

			// Modify file
			Files.writeString(tmp.toPath(), "v2");
			Files.setLastModifiedTime(tmp.toPath(), FileTime.fromMillis(System.currentTimeMillis() + 1000));

			// Now should detect change
			assertTrue(UFileCheck.fileChanged(path, 0), "Should detect file modification");
		} finally {
			tmp.delete();
		}
	}

	@Test
	public void testCooldown() throws Exception {
		printCaption("Cooldown prevents recheck");
		File tmp = File.createTempFile("ufc-test", ".txt");
		try {
			Files.writeString(tmp.toPath(), "test");
			String path = tmp.getAbsolutePath();

			// Baseline
			UFileCheck.fileChanged(path, 10); // 10 seconds cooldown

			// Modify
			Files.writeString(tmp.toPath(), "changed");
			Files.setLastModifiedTime(tmp.toPath(), FileTime.fromMillis(System.currentTimeMillis() + 1000));

			// Should still return false because within 10 second cooldown
			assertFalse(UFileCheck.fileChanged(path, 10), "Cooldown should prevent recheck");
		} finally {
			tmp.delete();
		}
	}

	@Test
	public void testFileNotExists() {
		printCaption("File not exists returns true");
		assertTrue(UFileCheck.fileChanged("/no/such/file.txt"), "Nonexistent file should return true");
	}

	@Test
	public void testGetFileCode() throws Exception {
		printCaption("getFileCode consistency");
		File tmp = File.createTempFile("ufc-test", ".txt");
		try {
			Files.writeString(tmp.toPath(), "hello world");
			int code1 = UFileCheck.getFileCode(tmp.getAbsolutePath());
			int code2 = UFileCheck.getFileCode(tmp.getAbsolutePath());
			assertEquals(code1, code2, "Same file should produce same code");

			// Modify
			Files.writeString(tmp.toPath(), "modified");
			Files.setLastModifiedTime(tmp.toPath(), FileTime.fromMillis(System.currentTimeMillis() + 1000));
			int code3 = UFileCheck.getFileCode(tmp.getAbsolutePath());
			assertNotEquals(code1, code3, "Modified file should produce different code");
		} finally {
			tmp.delete();
		}
	}

	@Test
	public void testRemove() throws Exception {
		printCaption("Remove tracking");
		File tmp = File.createTempFile("ufc-test", ".txt");
		try {
			Files.writeString(tmp.toPath(), "test");
			String path = tmp.getAbsolutePath();

			assertFalse(UFileCheck.fileChanged(path, 0));
			assertTrue(UFileCheck.isHave(path));

			UFileCheck.remove(path);
			assertFalse(UFileCheck.isHave(path));
		} finally {
			tmp.delete();
		}
	}

	@Test
	public void testNoKeyCollision() throws Exception {
		printCaption("No key collision with path string");
		File tmp1 = File.createTempFile("ufc-a", ".txt");
		File tmp2 = File.createTempFile("ufc-b", ".txt");
		try {
			Files.writeString(tmp1.toPath(), "aaa");
			Files.writeString(tmp2.toPath(), "bbb");

			// Different paths, same hash could collide with Integer keys
			UFileCheck.fileChanged(tmp1.getAbsolutePath(), 0);
			UFileCheck.fileChanged(tmp2.getAbsolutePath(), 0);

			assertTrue(UFileCheck.isHave(tmp1.getAbsolutePath()));
			assertTrue(UFileCheck.isHave(tmp2.getAbsolutePath()));
			// If collisions existed, one would overwrite the other
		} finally {
			tmp1.delete();
			tmp2.delete();
		}
	}
}
