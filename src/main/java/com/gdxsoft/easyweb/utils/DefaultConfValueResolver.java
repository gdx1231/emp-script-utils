package com.gdxsoft.easyweb.utils;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认配置值解析器，支持：
 * <ul>
 *   <li>{@code ${env.VAR}} — 环境变量</li>
 *   <li>{@code ${user.home}} 等 — 系统属性，fallback 环境变量</li>
 *   <li>{@code file://path} — 从文件读取（路径同样支持变量）</li>
 *   <li>其他 — 原样返回</li>
 * </ul>
 */
public class DefaultConfValueResolver implements ConfValueResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConfValueResolver.class);
	private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

	@Override
	public String resolve(String rawValue) {
		if (rawValue == null) {
			return null;
		}

		String result = rawValue;

		// 1. 解析 ${...} 变量
		if (result.contains("${")) {
			result = resolveVariables(result);
		}

		// 2. file:// 从文件读取
		if (result.startsWith("file://")) {
			String filePath = resolveFilePath(result.substring(7));
			try {
				String content = UFile.readFileText(filePath).trim();
				LOGGER.debug("Read value from file: {}", filePath);
				return content;
			} catch (IOException e) {
				LOGGER.error("Failed to read value from file: {} {}", filePath, e.getMessage());
				return null;
			}
		}

		return result;
	}

	private static String resolveVariables(String value) {
		Matcher m = VAR_PATTERN.matcher(value);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String key = m.group(1);
			String resolved = resolveVariable(key);
			if (resolved != null) {
				m.appendReplacement(sb, Matcher.quoteReplacement(resolved));
			}
		}
		m.appendTail(sb);
		return sb.toString();
	}

	private static String resolveVariable(String key) {
		if (key.startsWith("env.")) {
			return System.getenv(key.substring(4));
		}
		String value = System.getProperty(key);
		if (value == null) {
			value = System.getenv(key);
		}
		return value;
	}

	private static String resolveFilePath(String path) {
		if (path == null) {
			return null;
		}
		String result = path;
		if (result.startsWith("~")) {
			result = System.getProperty("user.home") + result.substring(1);
		}
		return resolveVariables(result);
	}
}
