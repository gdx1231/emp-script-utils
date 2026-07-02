package com.gdxsoft.easyweb.utils;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;

/**
 * JWT (JSON Web Token) 工具类<br>
 * 支持算法: HS256, HS384, HS512, RS256, RS384, RS512<br>
 * <br>
 * HMAC 示例:<br>
 * 
 * <pre>
 * String token = UJwt.hs256Builder()
 *     .subject("user123")
 *     .claim("role", "admin")
 *     .expiration(new Date(System.currentTimeMillis() + 3600000))
 *     .create("my-secret-key");
 *
 * UJwt.JwtToken jwt = UJwt.verifyHs256(token, "my-secret-key");
 * String sub = jwt.getSubject();
 * </pre>
 * 
 * RSA 示例:<br>
 * 
 * <pre>
 * URsa rsa = new URsa();
 * rsa.generateRsaKeys(2048);
 *
 * String token = UJwt.rs256Builder()
 *     .publicKey(rsa.getPublicKey())
 *     .privateKey(rsa.getPrivateKey())
 *     .subject("user123")
 *     .create();
 *
 * UJwt.JwtToken jwt = UJwt.verifyRs256(token, rsa.getPublicKey());
 * </pre>
 */
public class UJwt {

	public static final String HS256 = "HS256";
	public static final String HS384 = "HS384";
	public static final String HS512 = "HS512";
	public static final String RS256 = "RS256";
	public static final String RS384 = "RS384";
	public static final String RS512 = "RS512";

	// HMAC 别名（部分实现使用 HMAC256 而非 HS256）
	public static final String HMAC256 = "HMAC256";
	public static final String HMAC384 = "HMAC384";
	public static final String HMAC512 = "HMAC512";

	private UJwt() {
	}

	/**
	 * 创建 HS256 Builder
	 *
	 * @return Builder
	 */
	public static Builder hs256Builder() {
		return new Builder(HS256);
	}

	/**
	 * 创建 HS384 Builder
	 *
	 * @return Builder
	 */
	public static Builder hs384Builder() {
		return new Builder(HS384);
	}

	/**
	 * 创建 HS512 Builder
	 *
	 * @return Builder
	 */
	public static Builder hs512Builder() {
		return new Builder(HS512);
	}

	/**
	 * 创建 HMAC256 Builder（HS256 别名）
	 *
	 * @return Builder
	 */
	public static Builder hmac256Builder() {
		return new Builder(HMAC256);
	}

	/**
	 * 创建 HMAC384 Builder（HS384 别名）
	 *
	 * @return Builder
	 */
	public static Builder hmac384Builder() {
		return new Builder(HMAC384);
	}

	/**
	 * 创建 HMAC512 Builder（HS512 别名）
	 *
	 * @return Builder
	 */
	public static Builder hmac512Builder() {
		return new Builder(HMAC512);
	}

	/**
	 * 创建 RS256 Builder
	 *
	 * @return Builder
	 */
	public static Builder rs256Builder() {
		return new Builder(RS256);
	}

	/**
	 * 创建 RS384 Builder
	 *
	 * @return Builder
	 */
	public static Builder rs384Builder() {
		return new Builder(RS384);
	}

	/**
	 * 创建 RS512 Builder
	 *
	 * @return Builder
	 */
	public static Builder rs512Builder() {
		return new Builder(RS512);
	}

	/**
	 * 解析 JWT Token（不验证签名，仅解析 Header 和 Payload）
	 *
	 * @param token JWT Token 字符串
	 * @return JwtToken
	 * @throws Exception Token 格式或 JSON 解析错误
	 */
	public static JwtToken parse(String token) throws Exception {
		String[] parts = splitToken(token);
		JSONObject header = parseJson(base64UrlDecode(parts[0]));
		JSONObject payload = parseJson(base64UrlDecode(parts[1]));
		String alg = header.optString("alg", null);
		return new JwtToken(header, payload, alg);
	}

	/**
	 * 验证 HMAC 签名的 JWT Token (HS256/HS384/HS512)
	 *
	 * @param token  JWT Token
	 * @param secret 密钥
	 * @return JwtToken 验证通过后返回解析的 Token
	 * @throws Exception 签名无效或 Token 格式错误
	 */
	public static JwtToken verifyHmac(String token, String secret) throws Exception {
		String[] parts = splitToken(token);
		JSONObject header = parseJson(base64UrlDecode(parts[0]));
		String alg = header.optString("alg", null);
		if (alg == null || (!alg.startsWith("HS") && !alg.startsWith("HMAC"))) {
			throw new IllegalArgumentException("Not a HMAC JWT, alg=" + alg);
		}

		String unsigned = parts[0] + "." + parts[1];
		byte[] signature = base64UrlDecode(parts[2]);
		byte[] expected = signHmac(unsigned, secret, alg);

		if (!constantTimeEquals(signature, expected)) {
			throw new SecurityException("JWT signature verification failed");
		}

		JSONObject payload = parseJson(base64UrlDecode(parts[1]));
		return new JwtToken(header, payload, alg);
	}

	/**
	 * 验证 HS256 签名的 JWT Token
	 *
	 * @param token  JWT Token
	 * @param secret 密钥
	 * @return JwtToken
	 * @throws Exception 签名无效或 Token 格式错误
	 */
	public static JwtToken verifyHs256(String token, String secret) throws Exception {
		return verifyHmac(token, secret);
	}

	/**
	 * 验证 HS384 签名的 JWT Token
	 *
	 * @param token  JWT Token
	 * @param secret 密钥
	 * @return JwtToken
	 * @throws Exception 签名无效或 Token 格式错误
	 */
	public static JwtToken verifyHs384(String token, String secret) throws Exception {
		return verifyHmac(token, secret);
	}

	/**
	 * 验证 HS512 签名的 JWT Token
	 *
	 * @param token  JWT Token
	 * @param secret 密钥
	 * @return JwtToken
	 * @throws Exception 签名无效或 Token 格式错误
	 */
	public static JwtToken verifyHs512(String token, String secret) throws Exception {
		return verifyHmac(token, secret);
	}

	/**
	 * 验证 HMAC256 签名的 JWT Token（HS256 别名）
	 *
	 * @param token  JWT Token
	 * @param secret 密钥
	 * @return JwtToken
	 * @throws Exception 签名无效或 Token 格式错误
	 */
	public static JwtToken verifyHmac256(String token, String secret) throws Exception {
		return verifyHmac(token, secret);
	}

	/**
	 * 验证 HMAC384 签名的 JWT Token（HS384 别名）
	 *
	 * @param token  JWT Token
	 * @param secret 密钥
	 * @return JwtToken
	 * @throws Exception 签名无效或 Token 格式错误
	 */
	public static JwtToken verifyHmac384(String token, String secret) throws Exception {
		return verifyHmac(token, secret);
	}

	/**
	 * 验证 HMAC512 签名的 JWT Token（HS512 别名）
	 *
	 * @param token  JWT Token
	 * @param secret 密钥
	 * @return JwtToken
	 * @throws Exception 签名无效或 Token 格式错误
	 */
	public static JwtToken verifyHmac512(String token, String secret) throws Exception {
		return verifyHmac(token, secret);
	}

	/**
	 * 验证 RSA 签名的 JWT Token (RS256/RS384/RS512)
	 *
	 * @param token     JWT Token
	 * @param publicKey RSA 公钥
	 * @return JwtToken
	 * @throws Exception 签名无效或 Token 格式错误
	 */
	public static JwtToken verifyRsa(String token, RSAPublicKey publicKey) throws Exception {
		String[] parts = splitToken(token);
		JSONObject header = parseJson(base64UrlDecode(parts[0]));
		String alg = header.optString("alg", null);
		if (alg == null || !alg.startsWith("RS")) {
			throw new IllegalArgumentException("Not a RSA JWT, alg=" + alg);
		}

		String unsigned = parts[0] + "." + parts[1];
		byte[] signature = base64UrlDecode(parts[2]);

		if (!verifyRsaSignature(unsigned, signature, publicKey, alg)) {
			throw new SecurityException("JWT signature verification failed");
		}

		JSONObject payload = parseJson(base64UrlDecode(parts[1]));
		return new JwtToken(header, payload, alg);
	}

	/**
	 * 验证 RS256 签名的 JWT Token
	 *
	 * @param token     JWT Token
	 * @param publicKey RSA 公钥
	 * @return JwtToken
	 * @throws Exception 签名无效或 Token 格式错误
	 */
	public static JwtToken verifyRs256(String token, RSAPublicKey publicKey) throws Exception {
		return verifyRsa(token, publicKey);
	}

	/**
	 * 验证 RS384 签名的 JWT Token
	 *
	 * @param token     JWT Token
	 * @param publicKey RSA 公钥
	 * @return JwtToken
	 * @throws Exception 签名无效或 Token 格式错误
	 */
	public static JwtToken verifyRs384(String token, RSAPublicKey publicKey) throws Exception {
		return verifyRsa(token, publicKey);
	}

	/**
	 * 验证 RS512 签名的 JWT Token
	 *
	 * @param token     JWT Token
	 * @param publicKey RSA 公钥
	 * @return JwtToken
	 * @throws Exception 签名无效或 Token 格式错误
	 */
	public static JwtToken verifyRs512(String token, RSAPublicKey publicKey) throws Exception {
		return verifyRsa(token, publicKey);
	}

	/**
	 * 验证 RSA 签名的 JWT Token（公钥为 X509 编码的字节数组）
	 *
	 * @param token       JWT Token
	 * @param publicKeyX509 公钥的 X509 编码字节
	 * @return JwtToken
	 * @throws Exception 签名无效或 Token 格式错误
	 */
	public static JwtToken verifyRsa(String token, byte[] publicKeyX509) throws Exception {
		KeyFactory kf = KeyFactory.getInstance("RSA");
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyX509);
		RSAPublicKey publicKey = (RSAPublicKey) kf.generatePublic(keySpec);
		return verifyRsa(token, publicKey);
	}

	// ---- HMAC 签名 ----

	private static byte[] signHmac(String data, String secret, String alg) throws Exception {
		String macAlg = toMacAlgorithm(alg);
		Mac mac = Mac.getInstance(macAlg);
		mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), macAlg));
		return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * JWT alg 映射到 Java Mac 算法名称
	 */
	private static String toMacAlgorithm(String jwtAlg) {
		switch (jwtAlg) {
			case HS256:
			case HMAC256:
				return "HmacSHA256";
			case HS384:
			case HMAC384:
				return "HmacSHA384";
			case HS512:
			case HMAC512:
				return "HmacSHA512";
			default:
				throw new IllegalArgumentException("Unsupported HMAC algorithm: " + jwtAlg);
		}
	}

	// ---- RSA 签名 ----

	private static byte[] signRsa(String data, RSAPrivateKey privateKey, String alg)
			throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature sig = Signature.getInstance(toRsaSignatureAlgorithm(alg));
		sig.initSign(privateKey);
		sig.update(data.getBytes(StandardCharsets.UTF_8));
		return sig.sign();
	}

	private static boolean verifyRsaSignature(String data, byte[] signature, RSAPublicKey publicKey, String alg)
			throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature sig = Signature.getInstance(toRsaSignatureAlgorithm(alg));
		sig.initVerify(publicKey);
		sig.update(data.getBytes(StandardCharsets.UTF_8));
		return sig.verify(signature);
	}

	/**
	 * JWT alg 映射到 Java Signature 算法名称
	 */
	private static String toRsaSignatureAlgorithm(String jwtAlg) {
		switch (jwtAlg) {
			case RS256:
				return "SHA256withRSA";
			case RS384:
				return "SHA384withRSA";
			case RS512:
				return "SHA512withRSA";
			default:
				throw new IllegalArgumentException("Unsupported RSA algorithm: " + jwtAlg);
		}
	}

	// ---- Base64URL ----

	private static String base64UrlEncode(byte[] data) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
	}

	private static byte[] base64UrlDecode(String data) {
		return Base64.getUrlDecoder().decode(data);
	}

	// ---- 内部工具 ----

	private static String[] splitToken(String token) {
		if (token == null || token.isEmpty()) {
			throw new IllegalArgumentException("JWT token is empty");
		}
		String[] parts = token.split("\\.");
		if (parts.length != 3) {
			throw new IllegalArgumentException("Invalid JWT token format, expected 3 parts but got " + parts.length);
		}
		return parts;
	}

	private static JSONObject parseJson(byte[] jsonBytes) throws Exception {
		return new JSONObject(new String(jsonBytes, StandardCharsets.UTF_8));
	}

	/**
	 * 常量时间比较，防止计时攻击
	 */
	private static boolean constantTimeEquals(byte[] a, byte[] b) {
		if (a.length != b.length) {
			return false;
		}
		int result = 0;
		for (int i = 0; i < a.length; i++) {
			result |= a[i] ^ b[i];
		}
		return result == 0;
	}

	// ---- Builder ----

	/**
	 * JWT Token 构建器
	 */
	public static class Builder {
		private final JSONObject header;
		private final JSONObject payload;
		private final String algorithm;
		private RSAPrivateKey privateKey;
		private RSAPublicKey publicKey;

		Builder(String algorithm) {
			this.algorithm = algorithm;
			this.header = new JSONObject();
			this.header.put("alg", algorithm);
			this.header.put("typ", "JWT");
			this.payload = new JSONObject();
		}

		/**
		 * 设置主题 (sub)
		 *
		 * @param sub 主题
		 * @return Builder
		 */
		public Builder subject(String sub) {
			payload.put("sub", sub);
			return this;
		}

		/**
		 * 设置签发者 (iss)
		 *
		 * @param iss 签发者
		 * @return Builder
		 */
		public Builder issuer(String iss) {
			payload.put("iss", iss);
			return this;
		}

		/**
		 * 设置受众 (aud)
		 *
		 * @param aud 受众
		 * @return Builder
		 */
		public Builder audience(String aud) {
			payload.put("aud", aud);
			return this;
		}

		/**
		 * 设置 JWT ID (jti)
		 *
		 * @param jti JWT ID
		 * @return Builder
		 */
		public Builder jwtId(String jti) {
			payload.put("jti", jti);
			return this;
		}

		/**
		 * 设置签发时间 (iat)，自动转为 Unix 秒
		 *
		 * @param iat 签发时间
		 * @return Builder
		 */
		public Builder issuedAt(Date iat) {
			payload.put("iat", toSeconds(iat));
			return this;
		}

		/**
		 * 设置过期时间 (exp)，自动转为 Unix 秒
		 *
		 * @param exp 过期时间
		 * @return Builder
		 */
		public Builder expiration(Date exp) {
			payload.put("exp", toSeconds(exp));
			return this;
		}

		/**
		 * 设置生效时间 (nbf)，自动转为 Unix 秒
		 *
		 * @param nbf 生效时间
		 * @return Builder
		 */
		public Builder notBefore(Date nbf) {
			payload.put("nbf", toSeconds(nbf));
			return this;
		}

		/**
		 * 设置自定义声明
		 *
		 * @param key   声明名
		 * @param value 声明值
		 * @return Builder
		 */
		public Builder claim(String key, Object value) {
			payload.put(key, value);
			return this;
		}

		/**
		 * 批量设置声明，将 JSONObject 中的所有键值对合并到 Payload
		 *
		 * @param claims 声明集合
		 * @return Builder
		 */
		public Builder claim(JSONObject claims) {
			if (claims != null) {
				Iterator<String> keys = claims.keys();
				while (keys.hasNext()) {
					String key = keys.next();
					payload.put(key, claims.get(key));
				}
			}
			return this;
		}

		/**
		 * 设置 RSA 私钥（RS256/RS384/RS512 必须）
		 *
		 * @param key RSA 私钥
		 * @return Builder
		 */
		public Builder privateKey(RSAPrivateKey key) {
			this.privateKey = key;
			return this;
		}

		/**
		 * 设置 RSA 公钥（RS256/RS384/RS512 必须）
		 *
		 * @param key RSA 公钥
		 * @return Builder
		 */
		public Builder publicKey(RSAPublicKey key) {
			this.publicKey = key;
			return this;
		}

		/**
		 * 使用 HMAC 密钥签名并生成 JWT Token
		 *
		 * @param secret HMAC 密钥
		 * @return JWT Token 字符串
		 * @throws Exception 签名失败
		 */
		public String create(String secret) throws Exception {
			String unsigned = buildUnsigned();
			byte[] signature = signHmac(unsigned, secret, algorithm);
			return unsigned + "." + base64UrlEncode(signature);
		}

		/**
		 * 使用 RSA 密钥签名并生成 JWT Token<br>
		 * 需先调用 {@link #privateKey(RSAPrivateKey)} 和 {@link #publicKey(RSAPublicKey)} 设置密钥
		 *
		 * @return JWT Token 字符串
		 * @throws Exception 签名失败或未设置密钥
		 */
		public String create() throws Exception {
			if (this.privateKey == null) {
				throw new IllegalStateException("RSA private key is required, call privateKey() first");
			}
			String unsigned = buildUnsigned();
			byte[] signature = signRsa(unsigned, this.privateKey, algorithm);
			return unsigned + "." + base64UrlEncode(signature);
		}

		private String buildUnsigned() {
			String headerEncoded = base64UrlEncode(header.toString().getBytes(StandardCharsets.UTF_8));
			String payloadEncoded = base64UrlEncode(payload.toString().getBytes(StandardCharsets.UTF_8));
			return headerEncoded + "." + payloadEncoded;
		}

		private static long toSeconds(Date date) {
			return date.getTime() / 1000;
		}
	}

	// ---- JwtToken ----

	/**
	 * JWT Token 解析结果
	 */
	public static class JwtToken {
		private final JSONObject header;
		private final JSONObject payload;
		private final String algorithm;

		JwtToken(JSONObject header, JSONObject payload, String algorithm) {
			this.header = header;
			this.payload = payload;
			this.algorithm = algorithm;
		}

		/**
		 * 获取 Header JSON
		 *
		 * @return Header
		 */
		public JSONObject getHeader() {
			return header;
		}

		/**
		 * 获取 Payload JSON
		 *
		 * @return Payload
		 */
		public JSONObject getPayload() {
			return payload;
		}

		/**
		 * 获取签名算法
		 *
		 * @return 算法名称 (HS256, RS256 等)
		 */
		public String getAlgorithm() {
			return algorithm;
		}

		/**
		 * 获取主题 (sub)
		 *
		 * @return 主题，不存在返回 null
		 */
		public String getSubject() {
			return payload.optString("sub", null);
		}

		/**
		 * 获取签发者 (iss)
		 *
		 * @return 签发者，不存在返回 null
		 */
		public String getIssuer() {
			return payload.optString("iss", null);
		}

		/**
		 * 获取受众 (aud)
		 *
		 * @return 受众，不存在返回 null
		 */
		public String getAudience() {
			return payload.optString("aud", null);
		}

		/**
		 * 获取 JWT ID (jti)
		 *
		 * @return JWT ID，不存在返回 null
		 */
		public String getJwtId() {
			return payload.optString("jti", null);
		}

		/**
		 * 获取签发时间 (iat)
		 *
		 * @return 签发时间，不存在返回 null
		 */
		public Date getIssuedAt() {
			return getDateClaim("iat");
		}

		/**
		 * 获取过期时间 (exp)
		 *
		 * @return 过期时间，不存在返回 null
		 */
		public Date getExpiration() {
			return getDateClaim("exp");
		}

		/**
		 * 获取生效时间 (nbf)
		 *
		 * @return 生效时间，不存在返回 null
		 */
		public Date getNotBefore() {
			return getDateClaim("nbf");
		}

		/**
		 * 获取字符串类型的声明值
		 *
		 * @param name 声明名
		 * @return 声明值，不存在返回 null
		 */
		public String getStringClaim(String name) {
			return payload.optString(name, null);
		}

		/**
		 * 获取整数类型的声明值
		 *
		 * @param name 声明名
		 * @return 声明值，不存在返回 0
		 */
		public int getIntClaim(String name) {
			return payload.optInt(name, 0);
		}

		/**
		 * 获取长整数类型的声明值
		 *
		 * @param name 声明名
		 * @return 声明值，不存在返回 0
		 */
		public long getLongClaim(String name) {
			return payload.optLong(name, 0);
		}

		/**
		 * 获取布尔类型的声明值
		 *
		 * @param name 声明名
		 * @return 声明值，不存在返回 false
		 */
		public boolean getBooleanClaim(String name) {
			return payload.optBoolean(name, false);
		}

		/**
		 * 是否存在指定声明
		 *
		 * @param name 声明名
		 * @return 是否存在
		 */
		public boolean hasClaim(String name) {
			return payload.has(name) && !payload.isNull(name);
		}

		/**
		 * Token 是否已过期（检查 exp 声明）
		 *
		 * @return 已过期返回 true，无 exp 声明返回 false
		 */
		public boolean isExpired() {
			if (!payload.has("exp")) {
				return false;
			}
			long exp = payload.getLong("exp");
			return System.currentTimeMillis() > exp * 1000;
		}

		/**
		 * Token 是否尚未生效（检查 nbf 声明）
		 *
		 * @return 尚未生效返回 true，无 nbf 声明返回 false
		 */
		public boolean isNotYetValid() {
			if (!payload.has("nbf")) {
				return false;
			}
			long nbf = payload.getLong("nbf");
			return System.currentTimeMillis() < nbf * 1000;
		}

		private Date getDateClaim(String name) {
			if (!payload.has(name)) {
				return null;
			}
			long seconds = payload.getLong(name);
			return new Date(seconds * 1000);
		}

		@Override
		public String toString() {
			return "JwtToken{alg=" + algorithm + ", payload=" + payload.toString() + "}";
		}
	}
}
