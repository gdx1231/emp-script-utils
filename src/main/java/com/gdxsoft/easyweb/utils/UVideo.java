package com.gdxsoft.easyweb.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

	/** Default seek position in seconds for the cover frame */
	public static double DEFAULT_SEEK_SECONDS = 1.0;

	/** Default output image format for the cover */
	public static String DEFAULT_COVER_EXT = "webp";

	/** Default JPEG quality (ffmpeg -q:v, 2-31, lower is better quality) */
	public static int DEFAULT_QUALITY = 2;

	/** Default execution timeout in milliseconds (60s) */
	public static long DEFAULT_TIMEOUT = 60000L;

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
}
