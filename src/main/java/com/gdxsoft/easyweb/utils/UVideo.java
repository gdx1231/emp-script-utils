package com.gdxsoft.easyweb.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.conf.ConfFfmpeg;

/**
 * Video utilities using ffmpeg / ffprobe.<br>
 * <br>
 * Requires ffmpeg (and optionally ffprobe) installed on the system.<br>
 * Configure the path in ewa_conf.xml:<br>
 * &lt;ffmpeg path="/opt/homebrew/bin/" /&gt;<br>
 * <br>
 * If not configured, ffmpeg / ffprobe must be available in the system PATH.
 * <p>
 * Usage example:
 *
 * <pre>
 * UVideo v = new UVideo("/path/to/video.mp4")
 *         .setCoverExt("jpg")
 *         .setQuality(2)
 *         .setSize(800, 600);
 * String cover = v.createVideoCover();
 * String keyframe = v.createVideoCoverByKeyFrame();
 * </pre>
 *
 */
public class UVideo {
	private static Logger LOGGER = LoggerFactory.getLogger(UVideo.class);

	/** Cache for resolved executable paths, keyed by executable name */
	private static final Map<String, String> EXECUTABLE_CACHE = new ConcurrentHashMap<>();

	/** Prop time of ewa_conf.xml when the cache was last validated */
	private static long EXECUTABLE_CACHE_PROP_TIME = 0;

	/** Cached selected H.264 encoder name (resolved by {@link #detectH264Encoder()}) */
	private static volatile String CACHED_H264_ENCODER = null;

	/** Default seek position in seconds for the cover frame */
	public static double DEFAULT_SEEK_SECONDS = 1.0;

	/** Default output image format for the cover */
	public static String DEFAULT_COVER_EXT = "webp";

	/** Default JPEG quality (ffmpeg -q:v, 2-31, lower is better quality) */
	public static int DEFAULT_QUALITY = 2;

	/** Default execution timeout in milliseconds (60s) */
	public static long DEFAULT_TIMEOUT = 60000L;

	/**
	 * Default target bitrate for HW-accelerated encoders that take a bitrate
	 * (h264_nvenc, h264_videotoolbox, h264_qsv, h264_amf). libx264 and
	 * h264_vaapi are not affected — they use crf / qp respectively.
	 * <p>
	 * If {@code null} (the default), {@link #pickVideoBitrate(int, int)} is
	 * used to auto-select a bitrate from the resolution ladder. Set to a
	 * literal bitrate (e.g. {@code "4M"}) to force a single value across
	 * all resolutions — useful for output-size budgets. Mutate at runtime
	 * to change globally:
	 * <pre>
	 * UVideo.DEFAULT_VIDEO_BITRATE = "4M";   // force 4 Mbps everywhere
	 * UVideo.DEFAULT_VIDEO_BITRATE = null;   // back to ladder auto-pick
	 * </pre>
	 */
	public static volatile String DEFAULT_VIDEO_BITRATE = null;

	/**
	 * Per-resolution bitrate targets for HW encoders. The map is ordered by
	 * long-edge pixel count so {@link #pickVideoBitrate} picks the smallest
	 * entry whose threshold is &ge; the input's long edge. Tuned for
	 * visually-lossless output at ~libx264 crf 18 for the project's typical
	 * Seedance I2V content (animation, moderate motion). Lower if you need
	 * smaller files; raise for cinematic content with high motion.
	 */
	public static final Map<String, String> VIDEO_BITRATE_LADDER;
	static {
		VIDEO_BITRATE_LADDER = new LinkedHashMap<>();
		VIDEO_BITRATE_LADDER.put("480p_640",  "1M");   // ≤ 640 long-edge (480p, 576p, 864×496)
		VIDEO_BITRATE_LADDER.put("720p_1280", "3M");   // ≤ 1280 long-edge (720p HD)
		VIDEO_BITRATE_LADDER.put("1080p_1920", "6M");  // ≤ 1920 long-edge (1080p FHD)
		VIDEO_BITRATE_LADDER.put("1440p_2560", "12M"); // ≤ 2560 long-edge (QHD/2K)
		VIDEO_BITRATE_LADDER.put("2160p_3840", "35M"); // ≤ 3840 long-edge (4K UHD)
		VIDEO_BITRATE_LADDER.put("4320p_7680", "80M"); // 8K UHD
	}

	// ---- instance fields ----

	/** The video file path */
	private String videoPath;

	/** The output cover image format (jpg, png, webp ...) */
	private String coverExt;

	/** The JPEG quality (ffmpeg -q:v, 2-31, lower is better; 0 to skip) */
	private int quality;

	/** The target width (0 for original) */
	private int width;

	/** The target height (0 for original) */
	private int height;

	/** Execution timeout in milliseconds */
	private long timeout;

	public UVideo() {
		this.coverExt = DEFAULT_COVER_EXT;
		this.quality = DEFAULT_QUALITY;
		this.width = 0;
		this.height = 0;
		this.timeout = DEFAULT_TIMEOUT;
	}
	/**
	 * Create a UVideo instance for the given video file.
	 *
	 * @param videoPath the video file path
	 */
	public UVideo(String videoPath) {
		this.videoPath = videoPath;
		this.coverExt = DEFAULT_COVER_EXT;
		this.quality = DEFAULT_QUALITY;
		this.width = 0;
		this.height = 0;
		this.timeout = DEFAULT_TIMEOUT;
	}

	// ---- getters / setters (fluent) ----

	/**
	 * @return the video file path
	 */
	public String getVideoPath() {
		return videoPath;
	}

	/**
	 * @param videoPath the video file path
	 * @return this instance for chaining
	 */
	public UVideo setVideoPath(String videoPath) {
		this.videoPath = videoPath;
		return this;
	}

	/**
	 * @return the output cover image format (e.g. jpg, png, webp)
	 */
	public String getCoverExt() {
		return coverExt;
	}

	/**
	 * @param coverExt the output cover image format (jpg, png, webp ...)
	 * @return this instance for chaining
	 */
	public UVideo setCoverExt(String coverExt) {
		this.coverExt = coverExt;
		return this;
	}

	/**
	 * @return the JPEG quality (ffmpeg -q:v, 2-31, lower is better)
	 */
	public int getQuality() {
		return quality;
	}

	/**
	 * @param quality the JPEG quality (2-31, lower is better; 0 to skip)
	 * @return this instance for chaining
	 */
	public UVideo setQuality(int quality) {
		this.quality = quality;
		return this;
	}

	/**
	 * @return the target width (0 for original)
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width the target width (0 for original)
	 * @return this instance for chaining
	 */
	public UVideo setWidth(int width) {
		this.width = width;
		return this;
	}

	/**
	 * @return the target height (0 for original)
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height the target height (0 for original)
	 * @return this instance for chaining
	 */
	public UVideo setHeight(int height) {
		this.height = height;
		return this;
	}

	/**
	 * Set both width and height in one call.
	 *
	 * @param width  the target width (0 for original)
	 * @param height the target height (0 for original)
	 * @return this instance for chaining
	 */
	public UVideo setSize(int width, int height) {
		this.width = width;
		this.height = height;
		return this;
	}

	/**
	 * @return the execution timeout in milliseconds
	 */
	public long getTimeout() {
		return timeout;
	}

	/**
	 * @param timeout the execution timeout in milliseconds
	 * @return this instance for chaining
	 */
	public UVideo setTimeout(long timeout) {
		this.timeout = timeout;
		return this;
	}

	// ---- cover creation (instance) ----

	/**
	 * Create a video cover (thumbnail) at the default seek position (1 second).
	 * The cover path is auto-generated next to the video file using
	 * {@link #coverExt}.
	 *
	 * @return the cover file path, or null on failure
	 */
	public String createVideoCover() {
		return createVideoCover(null, DEFAULT_SEEK_SECONDS);
	}

	/**
	 * Create a video cover (thumbnail) at the default seek position (1 second).
	 *
	 * @param coverPath the output cover image path, or null to auto-generate
	 * @return the cover file path, or null on failure
	 */
	public String createVideoCover(String coverPath) {
		return createVideoCover(coverPath, DEFAULT_SEEK_SECONDS);
	}

	/**
	 * Create a video cover (thumbnail) at the specified seek position.
	 * <p>
	 * The ffmpeg command used:
	 *
	 * <pre>
	 * ffmpeg -y -ss &lt;seek&gt; -i &lt;input&gt; -vframes 1 [-q:v &lt;quality&gt;] [-vf scale=W:H] &lt;output&gt;
	 * </pre>
	 *
	 * @param coverPath   the output cover image path, or null to auto-generate
	 * @param seekSeconds the seek position in seconds (e.g. 1.0 for the 1-second
	 *                    frame)
	 * @return the cover file path, or null on failure
	 */
	public String createVideoCover(String coverPath, double seekSeconds) {
		return extractFrame(coverPath, new String[] { "-ss", String.valueOf(seekSeconds) },
				"Creating video cover");
	}

	/**
	 * Create a video cover by extracting the first keyframe (I-frame).
	 * The cover path is auto-generated.
	 *
	 * @return the cover file path, or null on failure
	 */
	public String createVideoCoverByKeyFrame() {
		return createVideoCoverByKeyFrame(null);
	}

	/**
	 * Create a video cover by extracting the first keyframe (I-frame) using ffmpeg.
	 * <p>
	 * The ffmpeg command used:
	 *
	 * <pre>
	 * ffmpeg -y -skip_frame nokey -i &lt;input&gt; -vframes 1 [-q:v &lt;quality&gt;] [-vf scale=W:H] &lt;output&gt;
	 * </pre>
	 *
	 * The {@code -skip_frame nokey} option is placed before {@code -i} so the
	 * decoder skips non-keyframes entirely, making extraction very fast.
	 *
	 * @param coverPath the output cover image path, or null to auto-generate
	 * @return the cover file path, or null on failure
	 */
	public String createVideoCoverByKeyFrame(String coverPath) {
		return extractFrame(coverPath, new String[] { "-skip_frame", "nokey" },
				"Extracting first keyframe");
	}

	/**
	 * Create a video cover by extracting the last frame.
	 * <p>
	 * The ffmpeg command used:
	 *
	 * <pre>
	 * ffmpeg -y -sseof -0.5 -i &lt;input&gt; -vframes 1 [-q:v &lt;quality&gt;] [-vf scale=W:H] &lt;output&gt;
	 * </pre>
	 *
	 * Uses {@code -sseof} with a 0.5s offset before the end to avoid
	 * black/empty tail frames common in generated videos. The cover path is
	 * auto-generated next to the video file.
	 *
	 * @return the cover file path, or null on failure
	 */
	public String createVideoCoverByLastFrame() {
		return createVideoCoverByLastFrame(null, 0.5);
	}

	/**
	 * Create a video cover by extracting the last frame.
	 *
	 * @param coverPath the output cover image path, or null to auto-generate
	 * @return the cover file path, or null on failure
	 */
	public String createVideoCoverByLastFrame(String coverPath) {
		return createVideoCoverByLastFrame(coverPath, 0.5);
	}

	/**
	 * Create a video cover by extracting the last frame at the specified seek offset.
	 * <p>
	 * The ffmpeg command used:
	 *
	 * <pre>
	 * ffmpeg -y -sseof -&lt;offset&gt; -i &lt;input&gt; -vframes 1 [-q:v &lt;quality&gt;] [-vf scale=W:H] &lt;output&gt;
	 * </pre>
	 *
	 * @param coverPath         the output cover image path, or null to auto-generate
	 * @param seekOffsetSeconds the seek offset in seconds before the end (e.g. 0.5 for
	 *                          the frame at end-0.5s). Must be &gt; 0.
	 * @return the cover file path, or null on failure
	 */
	public String createVideoCoverByLastFrame(String coverPath, double seekOffsetSeconds) {
		if (seekOffsetSeconds <= 0) {
			seekOffsetSeconds = 0.5;
		}
		return extractFrame(coverPath, new String[] { "-sseof", "-" + String.valueOf(seekOffsetSeconds) },
				"Extracting last frame");
	}

	/**
	 * Create a video cover at a percentage of the video duration.
	 * <p>
	 * For example, {@code percent=0.1} will seek to 10% of the video duration,
	 * which helps avoid black intro screens. Requires ffprobe to get the duration;
	 * if ffprobe is unavailable, falls back to the default seek position.
	 *
	 * @param percent the percentage (0.0 - 1.0)
	 * @return the cover file path, or null on failure
	 */
	public String createVideoCoverByPercent(double percent) {
		return createVideoCoverByPercent(null, percent);
	}

	/**
	 * Create a video cover at a percentage of the video duration.
	 *
	 * @param coverPath the output cover image path, or null to auto-generate
	 * @param percent   the percentage (0.0 - 1.0)
	 * @return the cover file path, or null on failure
	 */
	public String createVideoCoverByPercent(String coverPath, double percent) {
		try {
			double duration = getVideoDuration();
			if (duration <= 0) {
				LOGGER.warn("Cannot get video duration, using default seek: {}s", DEFAULT_SEEK_SECONDS);
				return createVideoCover(coverPath, DEFAULT_SEEK_SECONDS);
			}
			double seekSeconds = duration * percent;
			if (seekSeconds < 0.1) {
				seekSeconds = 0.1;
			}
			LOGGER.info("Seeking to {}s ({}% of {}s duration)", seekSeconds, percent, duration);
			return createVideoCover(coverPath, seekSeconds);
		} catch (Exception e) {
			LOGGER.error("Failed to get video duration, using default seek: {}", e.getMessage());
			return createVideoCover(coverPath, DEFAULT_SEEK_SECONDS);
		}
	}

	/**
	 * Get the duration of the current video in seconds.
	 *
	 * @return the duration in seconds, or -1 on failure
	 * @throws Exception if ffprobe fails or is not available
	 */
	public double getVideoDuration() throws Exception {
		return getVideoDuration(this.videoPath);
	}

	/**
	 * Get video information of the current video using ffprobe.
	 *
	 * @return the JSON object from ffprobe
	 * @throws Exception if ffprobe fails or is not available
	 */
	public JSONObject getVideoInfo() throws Exception {
		return getVideoInfo(this.videoPath);
	}

	// ---- core extraction (instance) ----

	/**
	 * Core method to extract a single frame from the video using ffmpeg. Uses the
	 * instance fields {@link #videoPath}, {@link #coverExt}, {@link #quality},
	 * {@link #width}, {@link #height} and {@link #timeout}.
	 *
	 * @param coverPath   the output cover image path, or null to auto-generate
	 * @param inputArgs   extra arguments inserted before {@code -i}
	 * @param description short description used in log messages
	 * @return the cover file path, or null on failure
	 */
	private String extractFrame(String coverPath, String[] inputArgs, String description) {
		File videoFile = new File(this.videoPath);
		if (!videoFile.exists() || !videoFile.isFile()) {
			LOGGER.error("Video file not found: {}", this.videoPath);
			return null;
		}

		// Auto-generate cover path if not provided: videoFullpath + ".cover.ext"
		if (StringUtils.isBlank(coverPath)) {
			coverPath = this.videoPath + ".cover." + this.coverExt;
		}

		// Ensure parent directory exists
		File coverFile = new File(coverPath);
		File parentDir = coverFile.getParentFile();
		if (parentDir != null && !parentDir.exists()) {
			parentDir.mkdirs();
		}

		// Get ffmpeg executable
		String ffmpeg;
		try {
			ffmpeg = getFfmpeg();
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}

		// Build the command:
		// ffmpeg -y [inputArgs...] -i <input> -vframes 1 [-q:v <quality>] [-vf
		// scale=W:H] <output>
		CommandLine cmd = new CommandLine(ffmpeg);
		cmd.addArgument("-y", false);
		if (inputArgs != null) {
			for (String arg : inputArgs) {
				cmd.addArgument(arg, false);
			}
		}
		cmd.addArgument("-i", false);
		cmd.addArgument(videoFile.getAbsolutePath(), false);
		cmd.addArgument("-vframes", false);
		cmd.addArgument("1", false);
		if (this.quality > 0) {
			cmd.addArgument("-q:v", false);
			cmd.addArgument(String.valueOf(this.quality), false);
		}
		if (this.width > 0 && this.height > 0) {
			// Both dimensions specified: scale to fit within W x H keeping aspect ratio
			cmd.addArgument("-vf", false);
			cmd.addArgument("scale=" + this.width + ":" + this.height
					+ ":force_original_aspect_ratio=decrease", false);
		} else if (this.width > 0 || this.height > 0) {
			// Only one dimension specified: -1 auto-calculates the other
			int w = this.width > 0 ? this.width : -1;
			int h = this.height > 0 ? this.height : -1;
			cmd.addArgument("-vf", false);
			cmd.addArgument("scale=" + w + ":" + h, false);
		}
		cmd.addArgument(coverFile.getAbsolutePath(), false);

		LOGGER.info("{}: {}", description, cmd.toString());

		ExecuteResult result = execute(cmd, this.timeout);
		if (result.isSuccess()) {
			if (coverFile.exists() && coverFile.length() > 0) {
				LOGGER.info("{}: {}", description, coverFile.getAbsolutePath());
				return coverFile.getAbsolutePath();
			} else {
				LOGGER.error("{} not created (file not found or empty): {}", description,
						coverFile.getAbsolutePath());
				return null;
			}
		} else {
			LOGGER.error("ffmpeg failed (exit {}): {}", result.exitCode, result.output);
			return null;
		}
	}

	// ---- static utility methods ----

	/**
	 * Get the video duration in seconds using ffprobe.
	 *
	 * @param videoPath the video file path
	 * @return the duration in seconds, or -1 on failure
	 * @throws Exception if ffprobe fails or is not available
	 */
	public static double getVideoDuration(String videoPath) throws Exception {
		JSONObject info = getVideoInfo(videoPath);
		JSONObject format = info.optJSONObject("format");
		if (format == null) {
			return -1;
		}
		String duration = format.optString("duration", null);
		if (StringUtils.isBlank(duration)) {
			return -1;
		}
		try {
			return Double.parseDouble(duration);
		} catch (NumberFormatException e) {
			LOGGER.warn("Cannot parse duration: {}", duration);
			return -1;
		}
	}

	/**
	 * Get video information (format and streams) using ffprobe.
	 * <p>
	 * The JSON returned contains:
	 *
	 * <pre>
	 * {
	 *   "streams": [{ "width": 1920, "height": 1080, "codec_name": "h264", ... }],
	 *   "format": { "duration": "120.5", "size": "12345678", ... }
	 * }
	 * </pre>
	 *
	 * @param videoPath the video file path
	 * @return the JSON object from ffprobe
	 * @throws Exception if ffprobe fails or is not available
	 */
	public static JSONObject getVideoInfo(String videoPath) throws Exception {
		File videoFile = new File(videoPath);
		if (!videoFile.exists() || !videoFile.isFile()) {
			throw new Exception("Video file not found: " + videoPath);
		}

		String ffprobe = getFfprobe();

		CommandLine cmd = new CommandLine(ffprobe);
		cmd.addArgument("-v", false);
		cmd.addArgument("error", false);
		cmd.addArgument("-print_format", false);
		cmd.addArgument("json", false);
		cmd.addArgument("-show_format", false);
		cmd.addArgument("-show_streams", false);
		cmd.addArgument(videoFile.getAbsolutePath(), false);

		ExecuteResult result = execute(cmd, DEFAULT_TIMEOUT);
		if (!result.isSuccess()) {
			throw new Exception("ffprobe failed (exit " + result.exitCode + "): " + result.output);
		}

		return new JSONObject(result.output);
	}

	/**
	 * Get the ffmpeg executable path.
	 * <p>
	 * Resolution order:
	 * <ol>
	 * <li>Configured path in ewa_conf.xml ({@code <ffmpeg path="..." />})</li>
	 * <li>{@code which} / {@code where} command lookup</li>
	 * <li>Auto-detect in common installation paths for the current OS</li>
	 * <li>System PATH (returns "ffmpeg")</li>
	 * </ol>
	 *
	 * @return the ffmpeg executable path
	 * @throws Exception if ffmpeg is configured but the executable is not found
	 */
	public static String getFfmpeg() throws Exception {
		return getExecutable("ffmpeg");
	}

	/**
	 * Get the ffprobe executable path.
	 *
	 * @return the ffprobe executable path
	 * @throws Exception if ffprobe is configured but the executable is not found
	 */
	public static String getFfprobe() throws Exception {
		return getExecutable("ffprobe");
	}

	/**
	 * Check whether ffmpeg is available (configured or in system PATH).
	 *
	 * @return true if ffmpeg is available
	 */
	public static boolean checkFfmpeg() {
		try {
			String ffmpeg = getFfmpeg();
			File f = new File(ffmpeg);
			if (f.exists()) {
				return true;
			}
			// ffmpeg might be in PATH (just "ffmpeg"), try running it
			CommandLine cmd = new CommandLine(ffmpeg);
			cmd.addArgument("-version", false);
			ExecuteResult result = execute(cmd, 10000L);
			return result.isSuccess();
		} catch (Exception e) {
			LOGGER.warn("ffmpeg not available: {}", e.getMessage());
			return false;
		}
	}

	// ---- static helpers (executable resolution, cached) ----

	/**
	 * Resolve the executable path for ffmpeg or ffprobe, with caching.
	 * <p>
	 * The resolved path is cached per executable name and reused on subsequent
	 * calls. The cache is automatically invalidated when the ewa_conf.xml
	 * configuration file changes (detected via {@link UPath#getPropTime()}).
	 *
	 * @param name the executable name (ffmpeg or ffprobe)
	 * @return the full executable path
	 * @throws Exception if ffmpeg is configured but the executable is not found
	 */
	private static String getExecutable(String name) throws Exception {
		long currentPropTime = UPath.getPropTime();
		if (currentPropTime != EXECUTABLE_CACHE_PROP_TIME) {
			EXECUTABLE_CACHE.clear();
			EXECUTABLE_CACHE_PROP_TIME = currentPropTime;
		}

		String cached = EXECUTABLE_CACHE.get(name);
		if (cached != null) {
			return cached;
		}

		String result = resolveExecutable(name);

		// Re-read prop time: resolveExecutable may trigger UPath.initPath()
		// which changes the prop time, so sync the stamp to avoid clearing the
		// cache on the very next call.
		EXECUTABLE_CACHE_PROP_TIME = UPath.getPropTime();
		EXECUTABLE_CACHE.put(name, result);
		return result;
	}

	/**
	 * Resolve the executable path for ffmpeg or ffprobe (no caching).
	 *
	 * @param name the executable name (ffmpeg or ffprobe)
	 * @return the full executable path
	 * @throws Exception if ffmpeg is configured but the executable is not found
	 */
	private static String resolveExecutable(String name) throws Exception {
		String os = System.getProperty("os.name").toLowerCase();
		boolean isWindows = os.startsWith("windows");
		String exeName = isWindows ? name + ".exe" : name;

		// 1. Check ConfFfmpeg configuration
		ConfFfmpeg conf = ConfFfmpeg.getInstance();
		if (conf != null && StringUtils.isNotBlank(conf.getPath())) {
			String confPath = conf.getPath();
			File pathFile = new File(confPath);

			if (pathFile.isDirectory()) {
				File exe = new File(pathFile, exeName);
				if (!exe.exists()) {
					String err = name + " not found at [" + exe.getAbsolutePath() + "]";
					LOGGER.error(err);
					throw new Exception(err);
				}
				return exe.getAbsolutePath();
			} else if (pathFile.isFile()) {
				String fileName = pathFile.getName().toLowerCase();
				if (!fileName.equals(exeName.toLowerCase())) {
					if (fileName.contains("ffmpeg") && name.equals("ffprobe")) {
						String probePath = pathFile.getAbsolutePath().replace("ffmpeg", "ffprobe");
						File probe = new File(probePath);
						if (probe.exists()) {
							return probe.getAbsolutePath();
						}
					} else if (fileName.contains("ffprobe") && name.equals("ffmpeg")) {
						String mpegPath = pathFile.getAbsolutePath().replace("ffprobe", "ffmpeg");
						File mpeg = new File(mpegPath);
						if (mpeg.exists()) {
							return mpeg.getAbsolutePath();
						}
					}
				}
				return pathFile.getAbsolutePath();
			} else {
				String err = name + " path not found: [" + confPath + "]";
				LOGGER.error(err);
				throw new Exception(err);
			}
		}

		// 2. Lookup via which (Unix) / where (Windows)
		String found = findByWhich(exeName, isWindows);
		if (found != null) {
			LOGGER.info("{} found via which/where at: {}", name, found);
			return found;
		}

		// 3. Auto-detect in common installation paths
		found = findInCommonPaths(name, exeName, os);
		if (found != null) {
			LOGGER.info("{} auto-detected at: {}", name, found);
			return found;
		}

		// 4. Fall back to system PATH
		LOGGER.info("{} not found, relying on system PATH", name);
		return name;
	}

	/**
	 * Use the system {@code which} (Unix) or {@code where} (Windows) command to
	 * locate the executable in the system PATH.
	 *
	 * @param exeName   the platform-specific executable name
	 * @param isWindows whether the current OS is Windows
	 * @return the full path if found, or null
	 */
	private static String findByWhich(String exeName, boolean isWindows) {
		String finder = isWindows ? "where" : "which";
		Process process = null;
		try {
			ProcessBuilder pb = new ProcessBuilder(finder, exeName);
			pb.redirectErrorStream(true);
			process = pb.start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String firstLine = reader.readLine();
			reader.close();

			int exitCode = process.waitFor();
			if (exitCode != 0 || firstLine == null || firstLine.trim().isEmpty()) {
				return null;
			}
			firstLine = firstLine.trim();
			if (firstLine.startsWith("\"") && firstLine.endsWith("\"") && firstLine.length() > 1) {
				firstLine = firstLine.substring(1, firstLine.length() - 1);
			}
			File f = new File(firstLine);
			if (f.exists() && f.canExecute()) {
				return f.getAbsolutePath();
			}
		} catch (Exception e) {
			LOGGER.debug("{} {} failed: {}", finder, exeName, e.getMessage());
		} finally {
			if (process != null) {
				process.destroy();
			}
		}
		return null;
	}

	/**
	 * Search for the executable in common installation paths based on the OS.
	 *
	 * @param name    the executable base name (ffmpeg or ffprobe)
	 * @param exeName the platform-specific executable name
	 * @param os      the lower-cased OS name
	 * @return the full path if found, or null
	 */
	private static String findInCommonPaths(String name, String exeName, String os) {
		List<String> dirs = new ArrayList<>();

		if (os.startsWith("windows")) {
			dirs.add("C:\\ffmpeg\\bin");
			dirs.add("C:\\Program Files\\ffmpeg\\bin");
			dirs.add("C:\\Program Files (x86)\\ffmpeg\\bin");
			dirs.add("C:\\tools\\ffmpeg\\bin");
			dirs.add("C:\\ProgramData\\chocolatey\\bin");
			String userProfile = System.getenv("USERPROFILE");
			if (userProfile != null && userProfile.trim().length() > 0) {
				dirs.add(userProfile + "\\AppData\\Local\\Programs\\ffmpeg\\bin");
				dirs.add(userProfile + "\\scoop\\apps\\ffmpeg\\current\\bin");
			}
		} else if (os.contains("mac")) {
			dirs.add("/opt/homebrew/bin");
			dirs.add("/usr/local/bin");
			dirs.add("/opt/local/bin");
			dirs.add("/usr/bin");
		} else {
			dirs.add("/usr/bin");
			dirs.add("/usr/local/bin");
			dirs.add("/snap/bin");
			dirs.add("/opt/ffmpeg/bin");
			dirs.add("/flatpak/exports/bin");
		}

		for (String dir : dirs) {
			File exe = new File(dir, exeName);
			if (exe.exists() && exe.canExecute()) {
				return exe.getAbsolutePath();
			}
		}
		return null;
	}

	/**
	 * Execute a command and capture the combined stdout/stderr output.
	 *
	 * @param cmd     the command line to execute
	 * @param timeout the timeout in milliseconds
	 * @return the execution result
	 */
	private static ExecuteResult execute(CommandLine cmd, long timeout) {
		DefaultExecutor executor = new DefaultExecutor();
		executor.setExitValues(new int[] { 0 });

		ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout);
		executor.setWatchdog(watchdog);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
		executor.setStreamHandler(streamHandler);

		try {
			executor.execute(cmd);
			String output = outputStream.toString();
			return new ExecuteResult(0, output, cmd.toString());
		} catch (ExecuteException e) {
			String output = outputStream.toString();
			LOGGER.error("Command failed (exit {}): {}", e.getExitValue(), cmd.toString());
			return new ExecuteResult(e.getExitValue(), output, cmd.toString());
		} catch (IOException e) {
			LOGGER.error("Command failed: {}", e.getMessage());
			return new ExecuteResult(-1, e.getMessage(), cmd.toString());
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
			}
		}
	}

	/**
	 * Extract a video clip from {@link #videoPath}.
	 * No stream copy — re-encodes for frame-accurate cutting.
	 *
	 * @param outPath  output clip file path
	 * @param startSec start position in seconds
	 * @param duration clip duration in seconds
	 * @return the output file path, or null on failure
	 */
	public String extractClip(String outPath, double startSec, double duration) {
		return extractClip(this.videoPath, outPath, startSec, duration, this.timeout);
	}

	/**
	 * Extract a video clip from a given file.
	 * Re-encodes (no stream copy) for frame-accurate cutting — essential for short
	 * clips where keyframe alignment would otherwise produce the wrong segment.
	 * <p>
	 * ffmpeg command:
	 * <pre>
	 * ffmpeg -y -ss &lt;start&gt; -i &lt;input&gt; -t &lt;duration&gt; &lt;output&gt;
	 * </pre>
	 *
	 * @param inputPath  source video file path
	 * @param outPath    output clip file path
	 * @param startSec   start position in seconds
	 * @param duration   clip duration in seconds
	 * @param timeoutMs  execution timeout in milliseconds
	 * @return the output file path, or null on failure
	 */
	public static String extractClip(String inputPath, String outPath,
			double startSec, double duration, long timeoutMs) {
		File inFile = new File(inputPath);
		if (!inFile.exists() || !inFile.isFile()) {
			LOGGER.error("Input video not found: {}", inputPath);
			return null;
		}

		File outFile = new File(outPath);
		File parentDir = outFile.getParentFile();
		if (parentDir != null && !parentDir.exists()) {
			parentDir.mkdirs();
		}

		String ffmpeg;
		try {
			ffmpeg = getFfmpeg();
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}

		// -ss before -i (input seeking) for speed; no -c copy so frame-accurate
		CommandLine cmd = new CommandLine(ffmpeg);
		cmd.addArgument("-y", false);
		cmd.addArgument("-ss", false);
		cmd.addArgument(String.format("%.2f", startSec), false);
		cmd.addArgument("-i", false);
		cmd.addArgument(inFile.getAbsolutePath(), false);
		cmd.addArgument("-t", false);
		cmd.addArgument(String.format("%.2f", duration), false);
		cmd.addArgument(outFile.getAbsolutePath(), false);

		LOGGER.info("Extracting clip: {}", cmd.toString());

		ExecuteResult result = execute(cmd, timeoutMs > 0 ? timeoutMs : DEFAULT_TIMEOUT);
		if (result.isSuccess()) {
			if (outFile.exists() && outFile.length() > 0) {
				LOGGER.info("Clip extracted: {}", outFile.getAbsolutePath());
				return outFile.getAbsolutePath();
			}
			LOGGER.error("Clip file not created or empty: {}", outFile.getAbsolutePath());
			return null;
		}
		LOGGER.error("ffmpeg clip extraction failed (exit {}): {}", result.exitCode, result.output);
		return null;
	}

	/**
	 * Merge multiple video files into one, using instance timeout.
	 * Uses ffmpeg concat demuxer with stream copy when possible.
	 *
	 * @param inputPaths ordered list of source video paths
	 * @param outPath    output merged video path
	 * @return JSON result ({@code RST=true} with {@code path}, or {@code RST=false} with {@code ERR})
	 */
	public JSONObject mergeVideos(List<String> inputPaths, String outPath) {
		return mergeVideos(inputPaths, outPath, this.timeout);
	}

	/**
	 * Merge with per-input trim (head/tail seconds to drop) for transition
	 * stitching. See {@link #mergeVideos(List, String, long, TrimSpec[])}.
	 */
	public JSONObject mergeVideos(List<String> inputPaths, String outPath, TrimSpec[] trimSpecs) {
		return mergeVideos(inputPaths, outPath, this.timeout, trimSpecs);
	}

	/**
	 * Merge multiple video files into one using ffmpeg filter_complex concat.
	 * <p>
	 * Builds and runs:
	 *
	 * <pre>
	 * ffmpeg -y -i in0 -i in1 ... -filter_complex
	 *   "[0:v][0:a][1:v][1:a]...concat=n=N:v=1:a=1[v][a]"
	 *   -map [v] -map [a] -c:v libx264 -crf 18 -preset fast
	 *   -c:a aac -ar 48000 -ac 2 -vsync cfr &lt;output&gt;
	 * </pre>
	 *
	 * Forces re-encode with unified codec/parameters (libx264 video, AAC 48kHz
	 * stereo) so heterogeneous inputs (different sample rates, codecs,
	 * time bases) merge without the audio drift that ffmpeg's concat demuxer
	 * produces when it silently resamples mid-stream. All inputs must have the
	 * same resolution / pixel format / frame rate (filter_complex concat
	 * requires this). All inputs must have an audio stream.
	 *
	 * @param inputPaths ordered list of source video paths (at least 2)
	 * @param inputPaths ordered list of source video paths (at least 2)
	 * @param outPath    output merged video path
	 * @param timeoutMs  execution timeout in milliseconds
	 * @return JSON result ({@code RST=true} with {@code path}, or {@code RST=false} with {@code ERR})
	 */
	public static JSONObject mergeVideos(List<String> inputPaths, String outPath, long timeoutMs) {
		return mergeVideos(inputPaths, outPath, timeoutMs, null);
	}

	/**
	 * Merge with per-input trim. Each {@link TrimSpec} (parallel to
	 * {@code inputPaths}, may be {@code null} for an entry to skip trim) drops
	 * the specified seconds from the head and/or tail of the matching input
	 * before feeding the concat filter. Used by transition stitching so the
	 * 0.5s head/tail baked into the transition video doesn't double-play at
	 * the join.
	 *
	 * @param inputPaths ordered list of source video paths (at least 2)
	 * @param outPath    output merged video path
	 * @param timeoutMs  execution timeout in milliseconds
	 * @param trimSpecs  optional parallel array; {@code null} entries (or a
	 *                   {@code null} array) leave the matching input untrimmed
	 * @return JSON result ({@code RST=true} with {@code path}, or {@code RST=false} with {@code ERR})
	 */
	public static JSONObject mergeVideos(List<String> inputPaths, String outPath, long timeoutMs,
			TrimSpec[] trimSpecs) {
		if (inputPaths == null || inputPaths.size() < 2) {
			String err = "mergeVideos requires at least 2 input paths";
			LOGGER.error(err);
			return UJSon.rstFalse(err);
		}
		if (trimSpecs != null && trimSpecs.length != inputPaths.size()) {
			String err = "trimSpecs length must match inputPaths size";
			LOGGER.error(err);
			return UJSon.rstFalse(err);
		}
		if (StringUtils.isBlank(outPath)) {
			String err = "mergeVideos output path is blank";
			LOGGER.error(err);
			return UJSon.rstFalse(err);
		}

		String ffprobePath;
		try {
			ffprobePath = getFfprobe();
		} catch (Exception e) {
			String err = "ffprobe required to probe audio streams: " + e.getMessage();
			LOGGER.error(err);
			return UJSon.rstFalse(err);
		}

		List<File> inputs = new ArrayList<>();
		List<File> audioTempFiles = new ArrayList<>();
		for (String p : inputPaths) {
			if (StringUtils.isBlank(p)) {
				String err = "mergeVideos input path is blank";
				LOGGER.error(err);
				cleanupTempFiles(audioTempFiles);
				return UJSon.rstFalse(err);
			}
			File f = new File(p);
			if (!f.exists() || !f.isFile()) {
				String err = "Input video not found: " + p;
				LOGGER.error(err);
				cleanupTempFiles(audioTempFiles);
				return UJSon.rstFalse(err);
			}
			if (!probeHasAudio(ffprobePath, f)) {
				LOGGER.warn("Input has no audio stream, generating silent-audio temp: {}", f.getName());
				File silent = createSilentAudioCopy(f);
				if (silent == null) {
					String err = "Failed to generate silent-audio copy for: " + p;
					LOGGER.error(err);
					cleanupTempFiles(audioTempFiles);
					return UJSon.rstFalse(err);
				}
				audioTempFiles.add(silent);
				inputs.add(silent);
			} else {
				inputs.add(f);
			}
		}

		File outFile = new File(outPath);
		File parentDir = outFile.getParentFile();
		if (parentDir != null && !parentDir.exists()) {
			parentDir.mkdirs();
		}

		String ffmpeg;
		try {
			ffmpeg = getFfmpeg();
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			cleanupTempFiles(audioTempFiles);
			return UJSon.rstFalse(e.getMessage());
		}

		long timeout = timeoutMs > 0 ? timeoutMs : DEFAULT_TIMEOUT;
		try {
			// Pick bitrate: explicit override wins, otherwise ladder-pick by
			// the first input's resolution. Dimensions probe is best-effort
			// — failure falls back to "2M" via pickVideoBitrate(0, 0).
			String bitrate = DEFAULT_VIDEO_BITRATE;
			if (bitrate == null) {
				int[] wh = probeVideoDimensions(ffprobePath, inputs.get(0));
				bitrate = pickVideoBitrate(wh[0], wh[1]);
				LOGGER.info("mergeVideos: auto-picked bitrate {} for {}x{} (input {})",
						bitrate, wh[0], wh[1], inputs.get(0).getName());
			} else {
				LOGGER.info("mergeVideos: using forced bitrate {} (input {})",
						bitrate, inputs.get(0).getName());
			}
			CommandLine cmd = buildConcatFilterCommand(ffmpeg, ffprobePath, inputs, outFile, bitrate, trimSpecs);
			LOGGER.info("Merging videos (filter_complex): {}", cmd.toString());
			ExecuteResult result = execute(cmd, timeout);
			if (result.isSuccess() && outFile.exists() && outFile.length() > 0) {
				LOGGER.info("Videos merged: {}", outFile.getAbsolutePath());
				JSONObject rst = UJSon.rstTrue();
				rst.put("path", outFile.getAbsolutePath());
				rst.put("mode", "filter");
				return rst;
			}
			String err = "ffmpeg merge failed (exit " + result.exitCode + "): " + result.output;
			LOGGER.error(err);
			return UJSon.rstFalse(err);
		} catch (Exception e) {
			LOGGER.error("mergeVideos failed: {}", e.getMessage());
			return UJSon.rstFalse("mergeVideos failed: " + e.getMessage());
		} finally {
			cleanupTempFiles(audioTempFiles);
		}
	}

	/**
	 * Delete the given temp files (best-effort). Used to remove the
	 * silent-audio copies created for audio-less inputs.
	 */
	private static void cleanupTempFiles(List<File> files) {
		for (File f : files) {
			if (f != null && f.exists()) {
				if (!f.delete()) {
					LOGGER.warn("Failed to delete temp file: {}", f.getAbsolutePath());
				}
			}
		}
	}

	/**
	 * Create a copy of the given video file with a synthesized silent stereo
	 * 48 kHz AAC audio track added, so the file looks like a normal
	 * video+audio file to subsequent filter_complex operations. Uses
	 * {@code -map 0:v -map 1:a -shortest} to attach an anullsrc side-stream
	 * without re-encoding the original video (fast). The temp file is
	 * normally deleted by the caller via {@link #cleanupTempFiles}.
	 *
	 * @return the temp file, or null on failure
	 */
	private static File createSilentAudioCopy(File source) {
		String ffmpeg;
		try {
			ffmpeg = getFfmpeg();
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}
		try {
			File tmp = File.createTempFile("uvideo_silent_", ".mp4");
			CommandLine cmd = new CommandLine(ffmpeg);
			cmd.addArgument("-y", false);
			cmd.addArgument("-i", false);
			cmd.addArgument(source.getAbsolutePath(), false);
			cmd.addArgument("-f", false);
			cmd.addArgument("lavfi", false);
			cmd.addArgument("-i", false);
			cmd.addArgument("anullsrc=channel_layout=stereo:sample_rate=48000", false);
			cmd.addArgument("-map", false);
			cmd.addArgument("0:v", false);
			cmd.addArgument("-map", false);
			cmd.addArgument("1:a", false);
			cmd.addArgument("-c:v", false);
			cmd.addArgument("copy", false);
			cmd.addArgument("-c:a", false);
			cmd.addArgument("aac", false);
			cmd.addArgument("-ar", false);
			cmd.addArgument("48000", false);
			cmd.addArgument("-ac", false);
			cmd.addArgument("2", false);
			cmd.addArgument("-shortest", false);
			cmd.addArgument("-movflags", false);
			cmd.addArgument("+faststart", false);
			cmd.addArgument(tmp.getAbsolutePath(), false);
			ExecuteResult result = execute(cmd, 60000L);
			if (result.isSuccess() && tmp.exists() && tmp.length() > 0) {
				return tmp;
			}
			LOGGER.error("silent-audio copy failed (exit {}): {}", result.exitCode, result.output);
			if (tmp.exists()) {
				tmp.delete();
			}
			return null;
		} catch (IOException e) {
			LOGGER.error("createSilentAudioCopy failed: {}", e.getMessage());
			return null;
		}
	}

	/**
	 * Build ffmpeg filter_complex concat command. The filter graph forces
	 * unified audio sample rate and channel count via the concat filter's
	 * implicit normalization, eliminating the audio drift that the concat
	 * demuxer produces when inputs have different sample rates. The video
	 * encoder is selected by {@link #detectH264Encoder()} so the same code
	 * runs on macOS (VideoToolbox), Linux (NVENC/VAAPI/QSV), and Windows
	 * (NVENC/QSV/AMF) with HW acceleration when available, falling back to
	 * libx264. For VAAPI the {@code -vaapi_device} argument is injected
	 * before the inputs and each video stream is uploaded to a VAAPI
	 * surface before the concat filter. Callers must ensure every input
	 * has an audio stream (audio-less inputs are pre-processed by
	 * {@link #mergeVideos} into silent-audio temp files).
	 */
	private static CommandLine buildConcatFilterCommand(String ffmpeg, String ffprobe, List<File> inputs,
			File outFile, String bitrate, TrimSpec[] trimSpecs) {
		String encoder = detectH264Encoder();
		boolean isVaapi = "h264_vaapi".equals(encoder);

		CommandLine cmd = new CommandLine(ffmpeg);
		cmd.addArgument("-y", false);
		if (isVaapi) {
			String device = resolveVaapiDevice();
			cmd.addArgument("-vaapi_device", false);
			cmd.addArgument(device, false);
		}
		// Per-input trim via ffmpeg input options. -ss before -i is fast
		// (input seek, no decode). -t limits total duration; trims the tail.
		for (int i = 0; i < inputs.size(); i++) {
			TrimSpec t = trimSpecs == null ? null : trimSpecs[i];
			if (t != null && t.startSec > 0) {
				cmd.addArgument("-ss", false);
				cmd.addArgument(String.format("%.3f", t.startSec), false);
			}
			if (t != null && t.endSec > 0) {
				cmd.addArgument("-t", false);
				// Probe this input's duration so end-trim math is exact.
				double dur = probeVideoDurationForTrim(ffprobe, inputs.get(i));
				double endLimit = Math.max(0.0, dur - t.endSec - t.startSec);
				cmd.addArgument(String.format("%.3f", endLimit), false);
			}
			cmd.addArgument("-i", false);
			cmd.addArgument(inputs.get(i).getAbsolutePath(), false);
		}

		StringBuilder graph = new StringBuilder();
		// 用第一个 input 的分辨率作为目标（避免不同 provider 输出分辨率不一致导致 concat 失败）
		int[] targetWh = probeVideoDimensions(ffprobe, inputs.get(0));
		int targetW = targetWh[0] > 0 ? targetWh[0] : 1280;
		int targetH = targetWh[1] > 0 ? targetWh[1] : 720;
		for (int i = 0; i < inputs.size(); i++) {
			if (isVaapi) {
				// VAAPI: scale + format + hwupload
				graph.append("[").append(i).append(":v]scale=").append(targetW).append(":").append(targetH)
						.append(",format=nv12|vaapi,hwupload[v").append(i).append("];");
				graph.append("[").append(i).append(":a]aresample=48000,aformat=sample_fmts=fltp:channel_layouts=stereo[a")
						.append(i).append("];");
			} else {
				// 普通：scale 到目标分辨率
				graph.append("[").append(i).append(":v:0]scale=").append(targetW).append(":").append(targetH)
						.append("[v").append(i).append("];");
			}
		}
		// concat: [v0][0:a:0][v1][1:a:0]...
		for (int i = 0; i < inputs.size(); i++) {
			if (isVaapi) {
				graph.append("[v").append(i).append("][a").append(i).append("]");
			} else {
				graph.append("[v").append(i).append("][").append(i).append(":a:0]");
			}
		}
		graph.append("concat=n=").append(inputs.size()).append(":v=1:a=1[v][a]");

		cmd.addArgument("-filter_complex", false);
		cmd.addArgument(graph.toString(), false);
		cmd.addArgument("-map", false);
		cmd.addArgument("[v]", false);
		cmd.addArgument("-map", false);
		cmd.addArgument("[a]", false);

		// Video encoder: hardware-accelerated when available, libx264 fallback.
		addVideoEncoderArgs(cmd, encoder, bitrate);
		cmd.addArgument("-pix_fmt", false);
		cmd.addArgument("yuv420p", false);
		if (isVaapi) {
			// VAAPI filter graph already produced yuv420p-compat surfaces; skip -vsync.
		} else if ("h264_videotoolbox".equals(encoder)) {
			// VideoToolbox ignores -vsync; skip to avoid "encoder setup failed" warnings.
		} else {
			cmd.addArgument("-vsync", false);
			cmd.addArgument("cfr", false);
		}

		// Audio: unified across platforms (aac 48kHz stereo 192k).
		cmd.addArgument("-c:a", false);
		cmd.addArgument("aac", false);
		cmd.addArgument("-ar", false);
		cmd.addArgument("48000", false);
		cmd.addArgument("-ac", false);
		cmd.addArgument("2", false);
		cmd.addArgument("-b:a", false);
		cmd.addArgument("192k", false);
		cmd.addArgument("-movflags", false);
		cmd.addArgument("+faststart", false);
		cmd.addArgument(outFile.getAbsolutePath(), false);
		return cmd;
	}

	/**
	 * Probe the video duration in seconds for end-trim math. Uses ffprobe
	 * with -show_entries format=duration. Returns -1 on failure (caller
	 * should fall back to no end trim).
	 */
	private static double probeVideoDurationForTrim(String ffprobe, File file) {
		CommandLine cmd = new CommandLine(ffprobe);
		cmd.addArgument("-v", false);
		cmd.addArgument("error", false);
		cmd.addArgument("-show_entries", false);
		cmd.addArgument("format=duration", false);
		cmd.addArgument("-of", false);
		cmd.addArgument("csv=p=0", false);
		cmd.addArgument(file.getAbsolutePath(), false);
		try {
			ExecuteResult result = execute(cmd, 10000L);
			if (!result.isSuccess() || result.output == null) {
				return -1;
			}
			String s = result.output.trim();
			if (s.isEmpty() || "N/A".equals(s)) {
				return -1;
			}
			return Double.parseDouble(s);
		} catch (Exception e) {
			LOGGER.warn("ffprobe duration probe failed for {}: {}", file.getName(), e.getMessage());
			return -1;
		}
	}

	/**
	 * Probe whether the given file has at least one audio stream. Uses
	 * {@code ffprobe -select_streams a} and treats non-empty output as
	 * "has audio". Returns false on probe failure (safer to inject
	 * anullsrc than to error out on a transient probe issue).
	 */
	private static boolean probeHasAudio(String ffprobe, File file) {
		CommandLine cmd = new CommandLine(ffprobe);
		cmd.addArgument("-v", false);
		cmd.addArgument("error", false);
		cmd.addArgument("-select_streams", false);
		cmd.addArgument("a", false);
		cmd.addArgument("-show_entries", false);
		cmd.addArgument("stream=codec_type", false);
		cmd.addArgument("-of", false);
		cmd.addArgument("csv=p=0", false);
		cmd.addArgument(file.getAbsolutePath(), false);
		try {
			ExecuteResult result = execute(cmd, 10000L);
			if (!result.isSuccess()) {
				return false;
			}
			return result.output != null && !result.output.trim().isEmpty();
		} catch (Exception e) {
			LOGGER.warn("ffprobe audio probe failed for {}: {}", file.getName(), e.getMessage());
			return false;
		}
	}

	/**
	 * Detect the best available H.264 encoder for the current host. Priority
	 * order: NVIDIA NVENC → Apple VideoToolbox → Intel QSV → AMD AMF → Linux
	 * VAAPI → libx264 (software fallback). VAAPI is only considered when a
	 * render node exists on the host (checked via
	 * {@link #resolveVaapiDevice()}). Result is cached after the first call.
	 * Returns the encoder name (e.g. {@code h264_videotoolbox},
	 * {@code libx264}); pass to {@link #addVideoEncoderArgs} for ffmpeg args.
	 *
	 * @return the selected encoder name, never null
	 */
	public static synchronized String detectH264Encoder() {
		if (CACHED_H264_ENCODER != null) {
			return CACHED_H264_ENCODER;
		}
		String[] preferred = {
				"h264_nvenc",
				"h264_videotoolbox",
				"h264_qsv",
				"h264_amf",
				"h264_vaapi",
				"libx264"
		};
		String available = listAvailableEncoders();
		for (String enc : preferred) {
			if (available == null || !containsEncoder(available, enc)) {
				continue;
			}
			if ("h264_vaapi".equals(enc) && resolveVaapiDevice() == null) {
				// VAAPI encoder is built-in but no render node exists; skip.
				continue;
			}
			CACHED_H264_ENCODER = enc;
			LOGGER.info("Selected H.264 encoder: {}", enc);
			return enc;
		}
		// Last-resort fallback even if encoder probe failed.
		CACHED_H264_ENCODER = "libx264";
		LOGGER.warn("No H.264 encoder detected, falling back to libx264");
		return CACHED_H264_ENCODER;
	}

	/**
	 * Resolve the VAAPI render node device path. Order: 1. environment
	 * variable {@code FFMPEG_VAAPI_DEVICE} (operator override), 2. standard
	 * render nodes {@code /dev/dri/renderD128}, {@code /dev/dri/renderD129}.
	 * Returns the first existing path, or {@code null} if none exists (which
	 * tells {@link #detectH264Encoder()} to skip VAAPI).
	 */
	static String resolveVaapiDevice() {
		String env = System.getenv("FFMPEG_VAAPI_DEVICE");
		if (env != null && !env.isBlank() && new File(env).exists()) {
			return env;
		}
		String[] candidates = { "/dev/dri/renderD128", "/dev/dri/renderD129" };
		for (String c : candidates) {
			if (new File(c).exists()) {
				return c;
			}
		}
		return null;
	}

	/**
	 * Run {@code ffmpeg -encoders} and return the combined stdout/stderr as a
	 * single string. Used by {@link #detectH264Encoder()} to probe available
	 * encoders.
	 */
	private static String listAvailableEncoders() {
		String ffmpeg;
		try {
			ffmpeg = getFfmpeg();
		} catch (Exception e) {
			LOGGER.warn("Cannot resolve ffmpeg for encoder probe: {}", e.getMessage());
			return null;
		}
		CommandLine cmd = new CommandLine(ffmpeg);
		cmd.addArgument("-hide_banner", false);
		cmd.addArgument("-encoders", false);
		try {
			ExecuteResult result = execute(cmd, 10000L);
			if (result.isSuccess()) {
				return result.output;
			}
			LOGGER.warn("ffmpeg -encoders exited with {}", result.exitCode);
			return null;
		} catch (Exception e) {
			LOGGER.warn("ffmpeg -encoders probe failed: {}", e.getMessage());
			return null;
		}
	}

	/**
	 * Check whether the given encoder name appears in the output of
	 * {@code ffmpeg -encoders}. The output lists one encoder per line as
	 * {@code " V....D h264_videotoolbox    VideoToolbox H.264 Encoder (codec h264)"}.
	 */
	private static boolean containsEncoder(String encodersOutput, String encoderName) {
		if (encodersOutput == null) {
			return false;
		}
		// Match by line start with whitespace, then encoder name with word boundary.
		String needle = " " + encoderName + " ";
		for (String line : encodersOutput.split("\\r?\\n")) {
			if (line.contains(needle)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Pick a target H.264 bitrate for the given video dimensions. Uses
	 * {@link #VIDEO_BITRATE_LADDER} keyed by the longer edge of the input.
	 * Returns the bitrate of the smallest ladder entry whose long-edge
	 * threshold is &ge; the input; falls back to the largest ladder entry
	 * for inputs above 8K, and to {@code "2M"} if the ladder is empty.
	 *
	 * @param width  video width in pixels
	 * @param height video height in pixels
	 * @return bitrate string suitable for ffmpeg {@code -b:v} (e.g. {@code "3M"})
	 */
	public static String pickVideoBitrate(int width, int height) {
		if (width <= 0 || height <= 0) {
			return "2M";
		}
		long longEdge = Math.max(width, height);
		String picked = null;
		for (Map.Entry<String, String> e : VIDEO_BITRATE_LADDER.entrySet()) {
			String key = e.getKey();
			int underscore = key.lastIndexOf('_');
			if (underscore < 0) {
				continue;
			}
			try {
				long threshold = Long.parseLong(key.substring(underscore + 1));
				if (longEdge <= threshold) {
					picked = e.getValue();
					break;
				}
			} catch (NumberFormatException ignored) {
				// Skip malformed ladder entries.
			}
		}
		if (picked != null) {
			return picked;
		}
		// Above the largest ladder entry (8K+): use the last (largest) entry.
		String last = null;
		for (String v : VIDEO_BITRATE_LADDER.values()) {
			last = v;
		}
		return last != null ? last : "2M";
	}

	/**
	 * Probe the width and height of the first video stream in {@code file}
	 * using ffprobe. Returns {@code {width, height}} or {@code {0, 0}} on
	 * failure. Used by {@link #mergeVideos} to pick a resolution-appropriate
	 * bitrate from the ladder when {@link #DEFAULT_VIDEO_BITRATE} is null.
	 */
	static int[] probeVideoDimensions(String ffprobe, File file) {
		CommandLine cmd = new CommandLine(ffprobe);
		cmd.addArgument("-v", false);
		cmd.addArgument("error", false);
		cmd.addArgument("-select_streams", false);
		cmd.addArgument("v:0", false);
		cmd.addArgument("-show_entries", false);
		cmd.addArgument("stream=width,height", false);
		cmd.addArgument("-of", false);
		cmd.addArgument("csv=p=0", false);
		cmd.addArgument(file.getAbsolutePath(), false);
		try {
			ExecuteResult result = execute(cmd, 10000L);
			if (!result.isSuccess() || result.output == null) {
				return new int[] { 0, 0 };
			}
			String[] parts = result.output.trim().split(",");
			if (parts.length < 2) {
				return new int[] { 0, 0 };
			}
			int w = Integer.parseInt(parts[0].trim());
			int h = Integer.parseInt(parts[1].trim());
			return new int[] { w, h };
		} catch (Exception e) {
			LOGGER.warn("ffprobe dimension probe failed for {}: {}", file.getName(), e.getMessage());
			return new int[] { 0, 0 };
		}
	}

	/**
	 * Append platform-specific encoding arguments to the given command for
	 * the selected H.264 encoder. Targets roughly libx264 crf 18 visual
	 * quality. Bitrate-mode encoders (NVENC, VideoToolbox, QSV, AMF) use
	 * the given {@code bitrate} (which the caller resolved via
	 * {@link #pickVideoBitrate} when {@link #DEFAULT_VIDEO_BITRATE} is null).
	 */
	private static void addVideoEncoderArgs(CommandLine cmd, String encoder, String bitrate) {
		cmd.addArgument("-c:v", false);
		cmd.addArgument(encoder, false);
		// maxrate = 1.25 * bitrate, bufsize = 2 * bitrate — standard VBR cap.
		String maxrate = scaleBitrate(bitrate, 1.25);
		String bufsize = scaleBitrate(bitrate, 2.0);
		switch (encoder) {
		case "h264_nvenc":
			cmd.addArgument("-preset", false);
			cmd.addArgument("p4", false); // p1-p7, p4 ≈ medium
			cmd.addArgument("-rc", false);
			cmd.addArgument("vbr", false);
			cmd.addArgument("-b:v", false);
			cmd.addArgument(bitrate, false);
			cmd.addArgument("-maxrate", false);
			cmd.addArgument(maxrate, false);
			cmd.addArgument("-bufsize", false);
			cmd.addArgument(bufsize, false);
			break;
		case "h264_videotoolbox":
			cmd.addArgument("-b:v", false);
			cmd.addArgument(bitrate, false);
			cmd.addArgument("-realtime", false);
			cmd.addArgument("true", false);
			cmd.addArgument("-allow_sw", false);
			cmd.addArgument("0", false);
			break;
		case "h264_qsv":
			cmd.addArgument("-preset", false);
			cmd.addArgument("veryfast", false);
			cmd.addArgument("-b:v", false);
			cmd.addArgument(bitrate, false);
			cmd.addArgument("-maxrate", false);
			cmd.addArgument(maxrate, false);
			cmd.addArgument("-bufsize", false);
			cmd.addArgument(bufsize, false);
			break;
		case "h264_amf":
			cmd.addArgument("-quality", false);
			cmd.addArgument("balanced", false);
			cmd.addArgument("-rc", false);
			cmd.addArgument("vbr_peak", false);
			cmd.addArgument("-b:v", false);
			cmd.addArgument(bitrate, false);
			cmd.addArgument("-maxrate", false);
			cmd.addArgument(maxrate, false);
			cmd.addArgument("-bufsize", false);
			cmd.addArgument(bufsize, false);
			break;
		case "h264_vaapi":
			// VAAPI uses constant-qp mode (no VBR cap needed).
			cmd.addArgument("-qp", false);
			cmd.addArgument("18", false);
			break;
		case "libx264":
		default:
			cmd.addArgument("-preset", false);
			cmd.addArgument("fast", false);
			cmd.addArgument("-crf", false);
			cmd.addArgument("18", false);
			break;
		}
	}

	/**
	 * Scale a bitrate string (e.g. {@code "8M"}, {@code "500k"}) by the given
	 * factor. Preserves the suffix. Used to compute maxrate/bufsize from the
	 * target bitrate.
	 */
	static String scaleBitrate(String bitrate, double factor) {
		if (bitrate == null || bitrate.isEmpty()) {
			return "10M";
		}
		char suffix = bitrate.charAt(bitrate.length() - 1);
		boolean hasSuffix = (suffix == 'k' || suffix == 'K' || suffix == 'm' || suffix == 'M');
		String numPart = hasSuffix ? bitrate.substring(0, bitrate.length() - 1) : bitrate;
		try {
			double v = Double.parseDouble(numPart) * factor;
			long rounded = Math.round(v);
			return hasSuffix ? (rounded + String.valueOf(suffix)) : String.valueOf(rounded);
		} catch (NumberFormatException e) {
			return "10M";
		}
	}

	/**
	 * Result of a command execution.
	 */
	private static class ExecuteResult {
		final int exitCode;
		final String output;
		final String command;

		ExecuteResult(int exitCode, String output, String command) {
			this.exitCode = exitCode;
			this.output = output;
			this.command = command;
		}

		boolean isSuccess() {
			return exitCode == 0;
		}
	}

	/**
	 * Per-input trim specification for {@link #mergeVideos(List, String, long, TrimSpec[])}.
	 * Tells the filter graph to drop {@code startSec} from the head and
	 * {@code endSec} from the tail of the corresponding input before
	 * feeding it to the concat filter. Used to absorb the 0.5s overlap
	 * baked into transition videos (Seedance I2V picks the from-last frame
	 * at sseof -0.5 and the to-first frame at 0.5s, so the preceding shot
	 * and following shot must each be trimmed by the same amount to avoid
	 * a visible jump at the join).
	 */
	public static class TrimSpec {
		/** Seconds to drop from the head of the input. 0 = no head trim. */
		public final double startSec;
		/** Seconds to drop from the tail of the input. 0 = no tail trim. */
		public final double endSec;

		public TrimSpec(double startSec, double endSec) {
			this.startSec = startSec;
			this.endSec = endSec;
		}

		/** No trim. */
		public static TrimSpec none() {
			return new TrimSpec(0, 0);
		}
	}
}
