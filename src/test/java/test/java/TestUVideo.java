package test.java;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UVideo;

public class TestUVideo extends TestBase {

	/** Temp working directory for generated files */
	private File tempDir;

	@AfterEach
	public void cleanup() {
		if (tempDir != null && tempDir.exists()) {
			File[] files = tempDir.listFiles();
			if (files != null) {
				for (File f : files) {
					f.delete();
				}
			}
			tempDir.delete();
		}
	}

	public static void main(String[] a) {
		TestUVideo t = new TestUVideo();
		try {
			t.testCreateVideoCover();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			t.cleanup();
		}
	}

	@Test
	public void testCreateVideoCover() throws Exception {
		super.printCaption("Test createVideoCover");

		// Copy test video to a temp directory
		String srcVideo = new File("src/test/resources/resources/test_video.mp4").getAbsolutePath();
		tempDir = new File(System.getProperty("java.io.tmpdir"), "uvideo_test_" + System.currentTimeMillis());
		tempDir.mkdirs();
		String videoPath = new File(tempDir, "test_video.mp4").getAbsolutePath();
		UFile.copyFile(srcVideo, videoPath);
		System.out.println("Temp video: " + videoPath);

		// Check ffmpeg availability
		boolean available = UVideo.checkFfmpeg();
		System.out.println("ffmpeg available: " + available);
		if (!available) {
			System.out.println("SKIP: ffmpeg not available");
			return;
		}

		// Test 1: createVideoCover with default settings (auto path)
		UVideo v1 = new UVideo(videoPath);
		String cover1 = v1.createVideoCover();
		System.out.println("Cover (auto path): " + cover1);
		assert cover1 != null && new File(cover1).exists() : "Cover should be created";
		assert new File(cover1).length() > 0 : "Cover file should not be empty";
		assert cover1.equals(videoPath + ".cover." + UVideo.DEFAULT_COVER_EXT) : "Auto path should be video + .cover.ext";

		// Test 2: createVideoCover with explicit path
		UVideo v2 = new UVideo(videoPath);
		String coverPath2 = new File(tempDir, "explicit.jpg").getAbsolutePath();
		String cover2 = v2.createVideoCover(coverPath2);
		System.out.println("Cover (explicit path): " + cover2);
		assert cover2 != null && new File(cover2).exists() : "Cover should be created";

		// Test 3: createVideoCover with seek position
		UVideo v3 = new UVideo(videoPath);
		String coverPath3 = new File(tempDir, "seek2s.jpg").getAbsolutePath();
		String cover3 = v3.createVideoCover(coverPath3, 2.0);
		System.out.println("Cover (seek 2s): " + cover3);
		assert cover3 != null && new File(coverPath3).exists() : "Cover should be created";

		// Test 4: createVideoCover with size and custom format/quality
		UVideo v4 = new UVideo(videoPath);
		v4.setSize(160, 120).setQuality(5).setCoverExt("png");
		String coverPath4 = new File(tempDir, "sized.png").getAbsolutePath();
		String cover4 = v4.createVideoCover(coverPath4, 1.0);
		System.out.println("Cover (160x120 png q5): " + cover4);
		assert cover4 != null && new File(coverPath4).exists() : "Cover should be created";

		// Test 4b: aspect ratio preserved when both width and height specified
		JSONObject info = UVideo.getVideoInfo(videoPath);
		JSONObject vstream = info.getJSONArray("streams").getJSONObject(0);
		int origW = vstream.getInt("width");
		int origH = vstream.getInt("height");
		System.out.println("Original video size: " + origW + "x" + origH);

		UVideo v4b = new UVideo(videoPath).setSize(800, 600);
		String coverPath4b = new File(tempDir, "aspect.jpg").getAbsolutePath();
		v4b.createVideoCover(coverPath4b, 1.0);
		BufferedImage img4b = ImageIO.read(new File(coverPath4b));
		int outW = img4b.getWidth();
		int outH = img4b.getHeight();
		System.out.println("Output size: " + outW + "x" + outH);
		assert outW <= 800 && outH <= 600 : "Output should fit within 800x600";
		double origRatio = (double) origW / origH;
		double outRatio = (double) outW / outH;
		System.out.println("Ratio: orig=" + origRatio + " out=" + outRatio);
		assert Math.abs(origRatio - outRatio) < 0.01 : "Aspect ratio should be preserved";

		// Test 5: getVideoDuration (instance method)
		UVideo v5 = new UVideo(videoPath);
		double duration = v5.getVideoDuration();
		System.out.println("Duration: " + duration + "s");
		assert duration > 0 : "Duration should be positive";

		// Test 6: createVideoCoverByPercent
		UVideo v6 = new UVideo(videoPath);
		String cover6 = v6.createVideoCoverByPercent(0.5);
		System.out.println("Cover (50% auto path): " + cover6);
		assert cover6 != null && new File(cover6).exists() : "Cover should be created";

		// Test 7: createVideoCoverByKeyFrame (explicit path)
		UVideo v7 = new UVideo(videoPath);
		String coverPath7 = new File(tempDir, "keyframe.jpg").getAbsolutePath();
		String cover7 = v7.createVideoCoverByKeyFrame(coverPath7);
		System.out.println("Cover (keyframe): " + cover7);
		assert cover7 != null && new File(coverPath7).exists() : "Keyframe cover should be created";
		assert new File(coverPath7).length() > 0 : "Keyframe cover file should not be empty";

		// Test 8: createVideoCoverByKeyFrame with size (fluent chain)
		UVideo v8 = new UVideo(videoPath).setSize(160, 120).setCoverExt("png");
		String coverPath8 = new File(tempDir, "keyframe_sized.png").getAbsolutePath();
		String cover8 = v8.createVideoCoverByKeyFrame(coverPath8);
		System.out.println("Cover (keyframe 160x120 png): " + cover8);
		assert cover8 != null && new File(coverPath8).exists() : "Keyframe cover should be created";

		// Test 9: createVideoCoverByKeyFrame auto path
		UVideo v9 = new UVideo(videoPath);
		String cover9 = v9.createVideoCoverByKeyFrame();
		System.out.println("Cover (keyframe auto path): " + cover9);
		assert cover9 != null && new File(cover9).exists() : "Keyframe cover should be created";
		assert cover9.equals(videoPath + ".cover." + UVideo.DEFAULT_COVER_EXT) : "Auto path should be video + .cover.ext";

		// Test 10: static getVideoDuration still works
		double durationStatic = UVideo.getVideoDuration(videoPath);
		System.out.println("Duration (static): " + durationStatic + "s");
		assert durationStatic > 0 : "Duration should be positive";

		// Verify temp directory contains video + generated covers
		File[] leftovers = tempDir.listFiles();
		System.out.println("Temp dir files: " + (leftovers != null ? leftovers.length : 0));

		System.out.println("All tests passed!");
	}
}
