package com.gdxsoft.easyweb.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File change detection with cooldown (don't check too frequently).
 * Uses absolute path as key to avoid hash collisions.
 * Automatically evicts entries not accessed for over 1 hour to prevent memory leaks.
 */
public class UFileCheck {
	private static Logger LOGGER = LoggerFactory.getLogger(UFileCheck.class);

	/** Holds the last known state of a tracked file */
	private static class FileState {
		final long lastCheckTime;
		final int statusCode;

		FileState(long lastCheckTime, int statusCode) {
			this.lastCheckTime = lastCheckTime;
			this.statusCode = statusCode;
		}
	}

	private static Map<String, FileState> FILE_STATES = new ConcurrentHashMap<>();

	private static final long EVICTION_AGE_MS = TimeUnit.HOURS.toMillis(1);

	/**
	 * Check whether the file is changed, do NOT check within 5 seconds
	 *
	 * @param filePath The file path and name
	 * @return true =changed, false= no change
	 */
	public static boolean fileChanged(String filePath) {
		return fileChanged(filePath, 5);
	}

	/**
	 * Check whether the file is changed, do NOT check within spanSeconds seconds
	 *
	 * @param filePath    The file path and name
	 * @param spanSeconds The do not check seconds
	 * @return true =changed, false= no change
	 */
	public static boolean fileChanged(String filePath, int spanSeconds) {
		File f1 = new File(filePath);
		if (!f1.exists()) {
			return true;
		}
		String key = f1.getAbsolutePath();
		int statusCode = getFileCode(filePath);

		return isChanged(key, statusCode, spanSeconds);
	}

	/**
	 * Get the file status code (composite hash of path | lastModified | length).
	 * Uses Files.readAttributes for single syscall (vs separate exists/lastModified/length).
	 *
	 * @param filePath The file path and name
	 * @return the file status code, -1 if file doesn't exist
	 */
	public static int getFileCode(String filePath) {
		Path p = Path.of(filePath);
		try {
			BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
			String s1 = p.toRealPath() + "|" + attr.lastModifiedTime().toMillis() + "|" + attr.size();
			return s1.hashCode();
		} catch (IOException e) {
			return -1;
		}
	}

	/**
	 * Check whether it has changed. Initial setting returns false (baseline established).
	 *
	 * @param key         File absolute path as key
	 * @param statusCode  Current file status code
	 * @param spanSeconds The do not check seconds
	 * @return true = changed, false = no change
	 */
	public static boolean isChanged(String key, int statusCode, int spanSeconds) {
		evictStaleIfNeeded();

		FileState state = FILE_STATES.get(key);

		if (state != null) {
			// Within cooldown: return cached result
			if (!isOverTime(state, spanSeconds)) {
				return false;
			}
			// Cooldown expired, check if status changed
			if (state.statusCode == statusCode) {
				updateTime(key, System.currentTimeMillis(), statusCode);
				return false;
			}
			// Status changed → update and report change
			updateState(key, System.currentTimeMillis(), statusCode);
			return true;
		}

		// First time tracking this file: establish baseline, don't report change
		updateState(key, System.currentTimeMillis(), statusCode);
		return false;
	}

	/**
	 * Check if the file exists in tracking map
	 */
	public static boolean isHave(String key) {
		return FILE_STATES.containsKey(key);
	}

	/**
	 * Check if the cooldown period has elapsed
	 */
	public static boolean isOverTime(String key, int spanSeconds) {
		FileState state = FILE_STATES.get(key);
		return state == null || isOverTime(state, spanSeconds);
	}

	private static boolean isOverTime(FileState state, int spanSeconds) {
		return System.currentTimeMillis() - state.lastCheckTime >= spanSeconds * 1000L;
	}

	private static void updateState(String key, long time, int statusCode) {
		FILE_STATES.put(key, new FileState(time, statusCode));
	}

	private static void updateTime(String key, long time, int statusCode) {
		FILE_STATES.put(key, new FileState(time, statusCode));
	}

	/**
	 * Remove a file from tracking
	 *
	 * @param key File absolute path
	 * @return true if removed or not tracked
	 */
	public static boolean remove(String key) {
		FILE_STATES.remove(key);
		return true;
	}

	private static void evictStaleIfNeeded() {
		if (FILE_STATES.size() < 1000) {
			return;
		}
		// ConcurrentHashMap.removeIf is atomic — no external lock needed
		long cutoff = System.currentTimeMillis() - EVICTION_AGE_MS;
		FILE_STATES.entrySet().removeIf(e -> e.getValue().lastCheckTime < cutoff);
	}

	/**
	 * @deprecated Use {@link #isChanged(String, int, int)} with file path string
	 */
	@Deprecated
	public static boolean isChanged(int fileCode, int statusCode, int spanSeconds) {
		return isChanged(String.valueOf(fileCode), statusCode, spanSeconds);
	}

	/**
	 * @deprecated Use {@link #isHave(String)} with file path string
	 */
	@Deprecated
	public static boolean isHave(int fileCode) {
		return isHave(String.valueOf(fileCode));
	}

	/**
	 * @deprecated Use {@link #isOverTime(String, int)} with file path string
	 */
	@Deprecated
	public static boolean isOverTime(int fileCode, int spanSeconds) {
		return isOverTime(String.valueOf(fileCode), spanSeconds);
	}

	/**
	 * @deprecated Use {@link #putTimeAndStatus(String, Long, Integer)} with file path string
	 */
	@Deprecated
	public static void putTimeAndFileCode(Integer fileCode, Long time, Integer fileStatusCode) {
		updateState(String.valueOf(fileCode), time, fileStatusCode);
	}

	/**
	 * @deprecated Use {@link #putTime(String, Long)} with file path string
	 */
	@Deprecated
	public static void putTime(Integer fileCode, Long t1) {
		updateTime(String.valueOf(fileCode), t1, 0);
	}

	/**
	 * @deprecated Use {@link #remove(String)} with file path string
	 */
	@Deprecated
	public static boolean remove(Integer fileCode) {
		return remove(String.valueOf(fileCode));
	}
}
