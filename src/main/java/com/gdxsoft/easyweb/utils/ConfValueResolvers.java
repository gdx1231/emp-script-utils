package com.gdxsoft.easyweb.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 配置值解析器管理器。
 * <p>
 * 通过系统属性 {@code ewa.value.resolver} 指定自定义实现类名，
 * 或使用 {@link #setResolver(ConfValueResolver)} 编程方式设置。
 */
public class ConfValueResolvers {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfValueResolvers.class);

	/** 系统属性名：指定自定义配置值解析器类名 */
	public static final String PROP_RESOLVER = "ewa.value.resolver";

	private static ConfValueResolver RESOLVER = null;

	/**
	 * 获取配置值解析器实例。优先使用系统属性指定的自定义实现，否则使用默认实现。
	 */
	public static ConfValueResolver getResolver() {
		if (RESOLVER != null) {
			return RESOLVER;
		}
		String className = System.getProperty(PROP_RESOLVER);
		if (StringUtils.isNotBlank(className)) {
			try {
				Class<?> cls = Class.forName(className);
				RESOLVER = (ConfValueResolver) cls.getDeclaredConstructor().newInstance();
				LOGGER.info("Using custom value resolver: {}", className);
				return RESOLVER;
			} catch (Exception e) {
				LOGGER.error("Failed to load custom value resolver: {}, fallback to default", className, e);
			}
		}
		RESOLVER = new DefaultConfValueResolver();
		return RESOLVER;
	}

	/**
	 * 设置自定义配置值解析器（编程方式覆盖系统属性）
	 *
	 * @param resolver 自定义解析器，null 重置为默认
	 */
	public static void setResolver(ConfValueResolver resolver) {
		RESOLVER = resolver;
	}

	/**
	 * 解析配置值的便捷方法
	 *
	 * @param rawValue 原始值
	 * @return 解析后的值，如果解析器返回 null 则返回原值
	 */
	public static String resolve(String rawValue) {
		if (rawValue == null) {
			return null;
		}
		String resolved = getResolver().resolve(rawValue);
		return resolved != null ? resolved : rawValue;
	}
}
