package com.gdxsoft.easyweb.utils;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UFileCheck {
	private static Logger LOGGER = LoggerFactory.getLogger(UFileCheck.class);

	private static ReentrantLock LOCK = new ReentrantLock();
	private static Map<Integer, Integer> FILE_LIST = new ConcurrentHashMap<Integer, Integer>();
	private static Map<Integer, Long> PAST_TIME = new ConcurrentHashMap<Integer, Long>();

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
		if (!f1.exists()) {// 文件不存在
			return true;
		}
		int fileCode = f1.getAbsolutePath().hashCode();
		Integer fileStatusCode = getFileCode(filePath);

		return isChanged(fileCode, fileStatusCode, spanSeconds);

	}

	/**
	 * Get the file status code
	 * 
	 * @param filePath The file path and name
	 * @return the file status code
	 */
	public static int getFileCode(String filePath) {
		File f1 = new File(filePath);
		if (!f1.exists()) {// 文件不存在
			return -1;
		}
		String s1 = f1.getAbsolutePath() + "|" + f1.lastModified() + "|" + f1.length();
		Integer code = Integer.valueOf(s1.hashCode());
		return code;
	}

	/**
	 * Check whether it has changed, the initial setting will return false
	 * 
	 * @param fileCode    The file pull path hash code
	 * @param statusCode  status code
	 * @param spanSeconds The do not check seconds
	 * @return true =changed, false= no change
	 */
	public static boolean isChanged(int fileCode, int statusCode, int spanSeconds) {
		boolean isInitSetting = false; // 是否是初始化设置
		if (isHave(fileCode)) {
			if (!isOverTime(fileCode, spanSeconds)) {
				return false;
			}
		} else {
			isInitSetting = true;
		}

		// 当前时间
		long t1 = System.currentTimeMillis();

		if (FILE_LIST.containsKey(fileCode)) {
			Integer statusCode1 = FILE_LIST.get(fileCode);
			if (statusCode1 != null && statusCode1 == statusCode) {
				// 记录当前时间
				putTime(fileCode, t1);
				return false;
			}
		} else {
			isInitSetting = true;
		}

		putTimeAndFileCode(fileCode, t1, statusCode);

		if (isInitSetting) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Check if the file exists according to the file code
	 * 
	 * @param fileCode The file getAbsolutePath().hashCode()
	 * @return true = yes, false = no
	 */
	public static boolean isHave(int fileCode) {
		return PAST_TIME.containsKey(fileCode);
	}

	/**
	 * Check if the file is out of date according to the file code
	 * 
	 * @param fileCode    The file getAbsolutePath().hashCode()
	 * @param spanSeconds The do not check seconds
	 * @return true = yes, false = no
	 */
	public static boolean isOverTime(int fileCode, int spanSeconds) {
		if (isHave(fileCode)) {
			Long t1 = PAST_TIME.get(fileCode);
			long time = System.currentTimeMillis();
			long diff = time - t1.longValue();
			if (diff < spanSeconds * 1000) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}

	}

	/**
	 * Put the file code and time
	 * 
	 * @param fileCode       The file getAbsolutePath().hashCode()
	 * @param time           The last check time
	 * @param fileStatusCode The file status code
	 */
	public static void putTimeAndFileCode(Integer fileCode, Long time, Integer fileStatusCode) {
		try {
			if (LOCK.tryLock()) {
				PAST_TIME.put(fileCode, time);
				FILE_LIST.put(fileCode, fileStatusCode);
				LOGGER.debug(fileCode + ", TIME=" + time + ", CODE=" + fileStatusCode);
			} else {
				// LOGGER.error("get Lock Failed");
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		} finally {
			// 查询当前线程是否保持此锁。
			if (LOCK.isHeldByCurrentThread()) {
				LOCK.unlock();
			}
		}
	}

	/**
	 * Remove the file
	 * 
	 * @param fileCode The file getAbsolutePath().hashCode()
	 * @return the remove result
	 */
	public static boolean remove(Integer fileCode) {
		if (!isHave(fileCode)) {
			return true;
		}
		try {
			if (LOCK.tryLock()) {
				PAST_TIME.remove(fileCode);
				FILE_LIST.remove(fileCode);

				return true;
			} else {
				// LOGGER.error("get Lock Failed");
				return false;
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return false;
		} finally {
			// 查询当前线程是否保持此锁。
			if (LOCK.isHeldByCurrentThread()) {
				LOCK.unlock();
			}
		}
	}

	/**
	 * Put the last check time
	 * 
	 * @param fileCode f1.getAbsolutePath().hashCode()
	 * @param t1       the last check time
	 */
	public static void putTime(Integer fileCode, Long t1) {
		try {
			if (LOCK.tryLock()) {
				PAST_TIME.put(fileCode, t1);
				LOGGER.debug(fileCode + ", TIME=" + t1);
			} else {
				// LOGGER.error("get Lock Failed");
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		} finally {
			// 查询当前线程是否保持此锁。
			if (LOCK.isHeldByCurrentThread()) {
				LOCK.unlock();
			}
		}

	}
}
