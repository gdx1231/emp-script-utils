package test.java;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.utils.UImages;

public class TestUImages extends TestBase {

	@Test
	public void testParseSizeValid() {
		printCaption("parseSize — valid");
		Dimension d = UImages.parseSize("800x600");
		assertNotNull(d);
		assertEquals(800, d.width);
		assertEquals(600, d.height);
	}

	@Test
	public void testParseSizeNullAndEmpty() {
		printCaption("parseSize — null/empty");
		assertNull(UImages.parseSize(null));
		assertNull(UImages.parseSize(""));
		assertNull(UImages.parseSize("   "));
	}

	@Test
	public void testParseSizeInvalid() {
		printCaption("parseSize — invalid formats");
		assertNull(UImages.parseSize("abc"));
		assertNull(UImages.parseSize("100"));
		assertNull(UImages.parseSize("100x200x300"));
	}

	@Test
	public void testParseSizeTooBig() {
		printCaption("parseSize — exceeds limit");
		assertNull(UImages.parseSize("9000x6000")); // > 8000
	}

	@Test
	public void testParseSizes() {
		printCaption("parseSizes — multi");
		Dimension[] ds = UImages.parseSizes("800x600,400x300,100x100");
		assertEquals(3, ds.length);
		assertEquals(800, ds[0].width);
		assertEquals(600, ds[0].height);
		assertEquals(400, ds[1].width);
		assertEquals(300, ds[1].height);
	}

	@Test
	public void testGetResizedPath() {
		printCaption("getResizedPath");
		File img = new File("/tmp/test.jpg");
		String path = UImages.getResizedPath(img);
		assertTrue(path.endsWith(UImages.RESIZED_TAG));
		assertTrue(path.startsWith("/tmp/test.jpg"));
	}

	@Test
	public void testGetResizedImageName() {
		printCaption("getResizedImageName");
		assertEquals("800x600.jpg", UImages.getResizedImageName(800, 600, "jpg"));
		assertEquals("400x300.png", UImages.getResizedImageName(400, 300, "png"));
		assertEquals("100x100.webp", UImages.getResizedImageName(new Dimension(100, 100), "webp"));
	}

	@Test
	public void testGetNewSize() {
		printCaption("getNewSize — aspect ratio");
		BufferedImage img = new BufferedImage(2000, 1000, BufferedImage.TYPE_INT_RGB);
		int[] size = UImages.getNewSize(img, 400, 400);
		// 2000x1000 → max 400x400: both scale to 400x200 (height constrained)
		assertEquals(400, size[0]);
		assertEquals(200, size[1]);

		// Square image → square output
		BufferedImage square = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
		int[] sq = UImages.getNewSize(square, 200, 200);
		assertEquals(200, sq[0]);
		assertEquals(200, sq[1]);
	}

	@Test
	public void testCheckImageMagick() {
		printCaption("checkImageMagick — returns boolean");
		boolean has = UImages.checkImageMagick();
		assertFalse(has); // no ewa_conf loaded → false
		System.out.println("ImageMagick available: " + has);
	}

	@Test
	public void testParseSizesWithTrailingComma() {
		printCaption("parseSizes — trailing comma");
		Dimension[] ds = UImages.parseSizes("800x600,");
		assertEquals(1, ds.length); // trailing empty ignored
	}

	@Test
	public void testParseSizesWithInvalid() {
		printCaption("parseSizes — mixed valid/invalid");
		Dimension[] ds = UImages.parseSizes("800x600,abc,400x300");
		assertEquals(3, ds.length);
		assertNotNull(ds[0]);
		assertNull(ds[1]); // "abc" = invalid
		assertNotNull(ds[2]);
	}
}
