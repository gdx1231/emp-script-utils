package test.java;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.utils.UQRCode;

public class TestUQRCode extends TestBase {

	@Test
	public void testCreateQRCodeBufImg() {
		printCaption("createQRCodeBufImg — basic QR generation");
		BufferedImage img = UQRCode.createQRCodeBufImg("https://www.gdxsoft.com", 200);
		assertNotNull(img, "QR image should not be null");
		assertEquals(200, img.getWidth(), img.getHeight(),
				"QR should be square, width=height");
	}

	@Test
	public void testCreateQRCodeBasic() {
		printCaption("createQRCode(msg) — default 300px");
		byte[] data = UQRCode.createQRCode("hello world");
		assertNotNull(data);
		assertTrue(data.length > 100, "QR JPEG should be >100 bytes");
	}

	@Test
	public void testCreateQRCodeCustomSize() {
		printCaption("createQRCode(msg, width) — custom size");
		byte[] data = UQRCode.createQRCode("test", 150);
		assertNotNull(data);
		assertTrue(data.length > 50);
	}

	@Test
	public void testCreateQRCodeBufImgMaxWidth() {
		printCaption("createQRCodeBufImg — capped at max width");
		BufferedImage img = UQRCode.createQRCodeBufImg("test", 9999);
		assertNotNull(img);
		assertTrue(img.getWidth() <= 3000, "Should cap at 3000 max width");
	}

	@Test
	public void testGetQRCodeSavedPath() {
		printCaption("getQRCodeSavedPath");
		String[] paths = UQRCode.getQRCodeSavedPath("abc123def", "jpg");
		assertNotNull(paths);
		assertEquals(2, paths.length);
		assertTrue(paths[0].endsWith("abc123def.jpg"), "Physical path should end with filename");
		assertTrue(paths[1].endsWith("abc123def.jpg"), "URL path should end with filename");
	}
}
