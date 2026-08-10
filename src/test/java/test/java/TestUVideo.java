package test.java;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.utils.UVideo;

public class TestUVideo {
    private static File testInput;
    private static File tmpDir;
    private static boolean ffmpegAvailable;

    @BeforeAll
    static void setUp() throws Exception {
        ffmpegAvailable = UVideo.checkFfmpeg();
        if (!ffmpegAvailable) {
            System.out.println("[SKIP] ffmpeg not available");
            return;
        }
        tmpDir = Files.createTempDirectory("uvideo_test_").toFile();
        testInput = new File(tmpDir, "test_input.mp4");

        // 5-second test pattern video at 30fps
        ProcessBuilder pb = new ProcessBuilder(
                UVideo.getFfmpeg(), "-y",
                "-f", "lavfi", "-i", "testsrc=duration=5:size=320x240:rate=30",
                "-t", "5",
                testInput.getAbsolutePath());
        pb.redirectErrorStream(true);
        Process p = pb.start();
        drain(p);
        int exit = p.waitFor();
        if (exit != 0 || !testInput.exists() || testInput.length() == 0) {
            ffmpegAvailable = false;
            System.out.println("[SKIP] Failed to generate test video (exit=" + exit + ")");
        }
    }

    @AfterAll
    static void tearDown() {
        if (tmpDir != null && tmpDir.exists()) {
            for (File f : tmpDir.listFiles()) { f.delete(); }
            tmpDir.delete();
        }
    }

    private static boolean requireFfmpeg() {
        if (!ffmpegAvailable) System.out.println("[SKIP] ffmpeg not available");
        return ffmpegAvailable;
    }

    @Test
    void testExtractTailClip_duration() throws Exception {
        if (!requireFfmpeg()) return;
        File out = new File(tmpDir, "tail.mp4");
        String result = UVideo.extractClip(
                testInput.getAbsolutePath(), out.getAbsolutePath(), 3.0, 2.0, 30000L);
        assertNotNull(result, "tail clip created");
        assertTrue(out.exists() && out.length() > 0, "tail clip non-empty");
        double dur = UVideo.getVideoDuration(out.getAbsolutePath());
        assertEquals(2.0, dur, 0.2, "tail clip ~2s (got " + dur + "s)");
    }

    @Test
    void testExtractTailClip_shorterThanInput() throws Exception {
        if (!requireFfmpeg()) return;
        File out = new File(tmpDir, "tail2.mp4");
        UVideo.extractClip(testInput.getAbsolutePath(), out.getAbsolutePath(), 3.0, 2.0, 30000L);
        double inDur = UVideo.getVideoDuration(testInput.getAbsolutePath());
        double outDur = UVideo.getVideoDuration(out.getAbsolutePath());
        assertTrue(outDur < inDur - 1.0,
                "tail (" + outDur + "s) shorter than input (" + inDur + "s)");
    }

    @Test
    void testExtractHeadClip_duration() throws Exception {
        if (!requireFfmpeg()) return;
        File out = new File(tmpDir, "head.mp4");
        String result = UVideo.extractClip(
                testInput.getAbsolutePath(), out.getAbsolutePath(), 0.0, 2.0, 30000L);
        assertNotNull(result, "head clip created");
        double dur = UVideo.getVideoDuration(out.getAbsolutePath());
        assertEquals(2.0, dur, 0.2, "head clip ~2s (got " + dur + "s)");
    }

    @Test
    void testExtractClip_nonexistentInput() {
        File out = new File(tmpDir, "nope.mp4");
        String result = UVideo.extractClip(
                "/nonexistent/video.mp4", out.getAbsolutePath(), 0, 2.0, 30000L);
        assertNull(result, "nonexistent input → null");
    }

    @Test
    void testTailPlusHead_roundTrip() throws Exception {
        if (!requireFfmpeg()) return;
        double inputDur = UVideo.getVideoDuration(testInput.getAbsolutePath());
        File tail = new File(tmpDir, "rt_tail.mp4");
        File head = new File(tmpDir, "rt_head.mp4");
        double tailStart = Math.max(0, inputDur - 2.0);
        UVideo.extractClip(testInput.getAbsolutePath(), tail.getAbsolutePath(), tailStart, 2.0, 30000L);
        UVideo.extractClip(testInput.getAbsolutePath(), head.getAbsolutePath(), 0, 2.0, 30000L);
        double tailDur = UVideo.getVideoDuration(tail.getAbsolutePath());
        double headDur = UVideo.getVideoDuration(head.getAbsolutePath());
        assertEquals(2.0, tailDur, 0.2, "round-trip tail");
        assertEquals(2.0, headDur, 0.2, "round-trip head");
        assertTrue(tailDur + headDur <= inputDur + 0.5,
                "tail+head (" + (tailDur + headDur) + "s) <= input (" + inputDur + "s)");
    }

    private static void drain(Process p) throws IOException {
        try (java.io.InputStream is = p.getInputStream()) {
            byte[] buf = new byte[4096];
            while (is.read(buf) != -1) { /* drain */ }
        }
    }
}
