package com.gdxsoft.easyweb.utils;

/**
 * 配置值解析器接口。
 * <p>
 * 用于统一管理密码、路径等敏感配置的解析逻辑。
 * 支持环境变量、系统属性、文件读取等多种方式，也可通过自定义实现对接密钥管理服务。
 * <p>
 * 通过系统属性 {@code ewa.value.resolver} 指定自定义实现类名：
 * <pre>
 * -Dewa.value.resolver=com.example.VaultValueResolver
 * </pre>
 */
public interface ConfValueResolver {

	/**
	 * 解析配置值原始值，返回实际值。
	 * <p>
	 * 返回值说明：
	 * <ul>
	 *   <li>返回非 null — 使用返回值，不再走默认解析</li>
	 *   <li>返回 null — 表示本解析器不处理，继续走默认逻辑</li>
	 * </ul>
	 *
	 * @param rawValue XML 中配置的原始值
	 * @return 解析后的值，或 null 表示不处理
	 */
	String resolve(String rawValue);
}
