package test.java;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.utils.UJSon;
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

    @Test
    void testMergeVideos_twoClips() throws Exception {
        if (!requireFfmpeg()) return;
        File a = new File(tmpDir, "m_a.mp4");
        File b = new File(tmpDir, "m_b.mp4");
        File out = new File(tmpDir, "merged.mp4");
        UVideo.extractClip(testInput.getAbsolutePath(), a.getAbsolutePath(), 0.0, 2.0, 30000L);
        UVideo.extractClip(testInput.getAbsolutePath(), b.getAbsolutePath(), 2.0, 2.0, 30000L);
        JSONObject result = UVideo.mergeVideos(
                Arrays.asList(a.getAbsolutePath(), b.getAbsolutePath()),
                out.getAbsolutePath(), 60000L);
        assertTrue(UJSon.checkTrue(result), "merged RST=true: " + result);
        assertEquals(out.getAbsolutePath(), result.getString("path"));
        assertTrue(out.exists() && out.length() > 0, "merged non-empty");
        double dur = UVideo.getVideoDuration(out.getAbsolutePath());
        assertEquals(4.0, dur, 0.5, "merged ~4s (got " + dur + "s)");
    }

    @Test
    void testMergeVideos_instance() throws Exception {
        if (!requireFfmpeg()) return;
        File a = new File(tmpDir, "mi_a.mp4");
        File b = new File(tmpDir, "mi_b.mp4");
        File out = new File(tmpDir, "merged_inst.mp4");
        UVideo.extractClip(testInput.getAbsolutePath(), a.getAbsolutePath(), 0.0, 1.5, 30000L);
        UVideo.extractClip(testInput.getAbsolutePath(), b.getAbsolutePath(), 1.5, 1.5, 30000L);
        JSONObject result = new UVideo().setTimeout(60000L).mergeVideos(
                Arrays.asList(a.getAbsolutePath(), b.getAbsolutePath()),
                out.getAbsolutePath());
        assertTrue(UJSon.checkTrue(result), "instance merge RST=true: " + result);
        assertEquals(out.getAbsolutePath(), result.getString("path"));
        double dur = UVideo.getVideoDuration(out.getAbsolutePath());
        assertEquals(3.0, dur, 0.5, "instance merged ~3s (got " + dur + "s)");
    }

    @Test
    void testMergeVideos_tooFewInputs() {
        File out = new File(tmpDir != null ? tmpDir : new File(System.getProperty("java.io.tmpdir")), "m_few.mp4");
        assertTrue(UJSon.checkFalse(UVideo.mergeVideos(null, out.getAbsolutePath(), 30000L)), "null list");
        assertTrue(UJSon.checkFalse(UVideo.mergeVideos(Collections.emptyList(), out.getAbsolutePath(), 30000L)), "empty list");
        assertTrue(UJSon.checkFalse(UVideo.mergeVideos(Collections.singletonList("/x.mp4"), out.getAbsolutePath(), 30000L)), "one input");
    }

    @Test
    void testMergeVideos_missingInput() {
        if (tmpDir == null) return;
        File out = new File(tmpDir, "m_miss.mp4");
        JSONObject result = UVideo.mergeVideos(
                Arrays.asList("/nonexistent/a.mp4", "/nonexistent/b.mp4"),
                out.getAbsolutePath(), 30000L);
        assertTrue(UJSon.checkFalse(result), "missing inputs → RST=false");
        assertTrue(result.optString("ERR", "").contains("not found"), "ERR mentions not found");
    }

    private static void drain(Process p) throws IOException {
        try (java.io.InputStream is = p.getInputStream()) {
            byte[] buf = new byte[4096];
            while (is.read(buf) != -1) { /* drain */ }
        }
    }
}
