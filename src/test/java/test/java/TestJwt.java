package test.java;

import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.utils.UJwt;
import com.gdxsoft.easyweb.utils.UJwt.JwtToken;
import com.gdxsoft.easyweb.utils.URsa;

import static org.junit.jupiter.api.Assertions.*;

public class TestJwt extends TestBase {

	// ---- HS256 ----

	@Test
	public void testHs256() throws Exception {
		String secret = "my-secret-key-for-testing-1234567890";

		String token = UJwt.hs256Builder()
				.subject("user123")
				.issuer("test-app")
				.audience("api")
				.claim("role", "admin")
				.expiration(new Date(System.currentTimeMillis() + 3600000))
				.create(secret);

		assertNotNull(token);
		String[] parts = token.split("\\.");
		assertEquals(3, parts.length);

		JwtToken jwt = UJwt.verifyHs256(token, secret);
		assertEquals("user123", jwt.getSubject());
		assertEquals("test-app", jwt.getIssuer());
		assertEquals("api", jwt.getAudience());
		assertEquals("admin", jwt.getStringClaim("role"));
		assertEquals(UJwt.HS256, jwt.getAlgorithm());
		assertFalse(jwt.isExpired());
		assertFalse(jwt.isNotYetValid());

		printCaption("testHs256 passed");
	}

	// ---- HS384 ----

	@Test
	public void testHs384() throws Exception {
		String secret = "my-secret-key-for-testing-1234567890";

		String token = UJwt.hs384Builder()
				.subject("user384")
				.create(secret);

		JwtToken jwt = UJwt.verifyHs384(token, secret);
		assertEquals("user384", jwt.getSubject());
		assertEquals(UJwt.HS384, jwt.getAlgorithm());

		printCaption("testHs384 passed");
	}

	// ---- HS512 ----

	@Test
	public void testHs512() throws Exception {
		String secret = "my-secret-key-for-testing-1234567890";

		String token = UJwt.hs512Builder()
				.subject("user512")
				.create(secret);

		JwtToken jwt = UJwt.verifyHs512(token, secret);
		assertEquals("user512", jwt.getSubject());
		assertEquals(UJwt.HS512, jwt.getAlgorithm());

		printCaption("testHs512 passed");
	}

	// ---- HMAC256 别名 ----

	@Test
	public void testHmac256Alias() throws Exception {
		String secret = "my-secret-key-for-testing-1234567890";

		// 使用 HMAC256 Builder 创建 Token
		String token = UJwt.hmac256Builder()
				.subject("user-hmac256")
				.claim("role", "admin")
				.create(secret);

		assertNotNull(token);

		// 使用 verifyHmac256 验证
		JwtToken jwt = UJwt.verifyHmac256(token, secret);
		assertEquals("user-hmac256", jwt.getSubject());
		assertEquals("admin", jwt.getStringClaim("role"));
		assertEquals(UJwt.HMAC256, jwt.getAlgorithm());

		// 使用通用 verifyHmac 也能验证
		JwtToken jwt2 = UJwt.verifyHmac(token, secret);
		assertEquals("user-hmac256", jwt2.getSubject());

		printCaption("testHmac256Alias passed");
	}

	@Test
	public void testHmac384Alias() throws Exception {
		String secret = "my-secret-key-for-testing-1234567890";

		String token = UJwt.hmac384Builder()
				.subject("user-hmac384")
				.create(secret);

		JwtToken jwt = UJwt.verifyHmac384(token, secret);
		assertEquals("user-hmac384", jwt.getSubject());
		assertEquals(UJwt.HMAC384, jwt.getAlgorithm());

		printCaption("testHmac384Alias passed");
	}

	@Test
	public void testHmac512Alias() throws Exception {
		String secret = "my-secret-key-for-testing-1234567890";

		String token = UJwt.hmac512Builder()
				.subject("user-hmac512")
				.create(secret);

		JwtToken jwt = UJwt.verifyHmac512(token, secret);
		assertEquals("user-hmac512", jwt.getSubject());
		assertEquals(UJwt.HMAC512, jwt.getAlgorithm());

		printCaption("testHmac512Alias passed");
	}

	// ---- HMAC 签名验证失败 ----

	@Test
	public void testHmacWrongSecret() throws Exception {
		String token = UJwt.hs256Builder()
				.subject("user123")
				.create("correct-secret");

		assertThrows(Exception.class, () -> {
			UJwt.verifyHs256(token, "wrong-secret");
		});

		printCaption("testHmacWrongSecret passed");
	}

	// ---- RS256 ----

	@Test
	public void testRs256() throws Exception {
		URsa rsa = new URsa();
		rsa.generateRsaKeys(2048);

		String token = UJwt.rs256Builder()
				.subject("user123")
				.issuer("test-app")
				.privateKey(rsa.getPrivateKey())
				.publicKey(rsa.getPublicKey())
				.expiration(new Date(System.currentTimeMillis() + 3600000))
				.create();

		assertNotNull(token);

		JwtToken jwt = UJwt.verifyRs256(token, rsa.getPublicKey());
		assertEquals("user123", jwt.getSubject());
		assertEquals("test-app", jwt.getIssuer());
		assertEquals(UJwt.RS256, jwt.getAlgorithm());
		assertFalse(jwt.isExpired());

		printCaption("testRs256 passed");
	}

	// ---- RS384 ----

	@Test
	public void testRs384() throws Exception {
		URsa rsa = new URsa();
		rsa.generateRsaKeys(2048);

		String token = UJwt.rs384Builder()
				.subject("user384")
				.privateKey(rsa.getPrivateKey())
				.publicKey(rsa.getPublicKey())
				.create();

		JwtToken jwt = UJwt.verifyRs384(token, rsa.getPublicKey());
		assertEquals("user384", jwt.getSubject());
		assertEquals(UJwt.RS384, jwt.getAlgorithm());

		printCaption("testRs384 passed");
	}

	// ---- RS512 ----

	@Test
	public void testRs512() throws Exception {
		URsa rsa = new URsa();
		rsa.generateRsaKeys(2048);

		String token = UJwt.rs512Builder()
				.subject("user512")
				.privateKey(rsa.getPrivateKey())
				.publicKey(rsa.getPublicKey())
				.create();

		JwtToken jwt = UJwt.verifyRs512(token, rsa.getPublicKey());
		assertEquals("user512", jwt.getSubject());
		assertEquals(UJwt.RS512, jwt.getAlgorithm());

		printCaption("testRs512 passed");
	}

	// ---- RSA 签名验证失败（用错误的公钥） ----

	@Test
	public void testRsaWrongKey() throws Exception {
		URsa rsa1 = new URsa();
		rsa1.generateRsaKeys(2048);

		URsa rsa2 = new URsa();
		rsa2.generateRsaKeys(2048);

		String token = UJwt.rs256Builder()
				.subject("user123")
				.privateKey(rsa1.getPrivateKey())
				.publicKey(rsa1.getPublicKey())
				.create();

		assertThrows(Exception.class, () -> {
			UJwt.verifyRs256(token, rsa2.getPublicKey());
		});

		printCaption("testRsaWrongKey passed");
	}

	// ---- RSA 通过 X509 字节验证 ----

	@Test
	public void testRsaVerifyByX509Bytes() throws Exception {
		URsa rsa = new URsa();
		rsa.generateRsaKeys(2048);

		String token = UJwt.rs256Builder()
				.subject("user-x509")
				.privateKey(rsa.getPrivateKey())
				.publicKey(rsa.getPublicKey())
				.create();

		byte[] publicKeyBytes = rsa.getPublicKey().getEncoded();
		JwtToken jwt = UJwt.verifyRsa(token, publicKeyBytes);
		assertEquals("user-x509", jwt.getSubject());

		printCaption("testRsaVerifyByX509Bytes passed");
	}

	// ---- Parse（不验证签名） ----

	@Test
	public void testParse() throws Exception {
		String token = UJwt.hs256Builder()
				.subject("user123")
				.claim("role", "admin")
				.create("some-secret");

		JwtToken jwt = UJwt.parse(token);
		assertEquals("user123", jwt.getSubject());
		assertEquals("admin", jwt.getStringClaim("role"));
		assertEquals(UJwt.HS256, jwt.getAlgorithm());
		assertTrue(jwt.hasClaim("role"));
		assertFalse(jwt.hasClaim("nonexistent"));

		printCaption("testParse passed");
	}

	// ---- 过期 Token ----

	@Test
	public void testExpiredToken() throws Exception {
		String secret = "test-secret";

		String token = UJwt.hs256Builder()
				.subject("user123")
				.expiration(new Date(System.currentTimeMillis() - 10000))
				.create(secret);

		JwtToken jwt = UJwt.verifyHs256(token, secret);
		assertTrue(jwt.isExpired());

		printCaption("testExpiredToken passed");
	}

	// ---- nbf (not before) ----

	@Test
	public void testNotBefore() throws Exception {
		String secret = "test-secret";

		// nbf 在未来
		String token = UJwt.hs256Builder()
				.subject("user123")
				.notBefore(new Date(System.currentTimeMillis() + 60000))
				.create(secret);

		JwtToken jwt = UJwt.verifyHs256(token, secret);
		assertTrue(jwt.isNotYetValid());

		printCaption("testNotBefore passed");
	}

	// ---- 所有标准声明 ----

	@Test
	public void testAllStandardClaims() throws Exception {
		String secret = "test-secret";
		Date now = new Date();

		String token = UJwt.hs256Builder()
				.subject("sub-val")
				.issuer("iss-val")
				.audience("aud-val")
				.jwtId("jti-val")
				.issuedAt(now)
				.expiration(new Date(now.getTime() + 3600000))
				.notBefore(now)
				.claim("custom_int", 42)
				.claim("custom_bool", true)
				.create(secret);

		JwtToken jwt = UJwt.verifyHs256(token, secret);
		assertEquals("sub-val", jwt.getSubject());
		assertEquals("iss-val", jwt.getIssuer());
		assertEquals("aud-val", jwt.getAudience());
		assertEquals("jti-val", jwt.getJwtId());
		assertNotNull(jwt.getIssuedAt());
		assertNotNull(jwt.getExpiration());
		assertNotNull(jwt.getNotBefore());
		assertEquals(42, jwt.getIntClaim("custom_int"));
		assertTrue(jwt.getBooleanClaim("custom_bool"));

		printCaption("testAllStandardClaims passed");
	}

	// ---- claim(JSONObject) 批量设置 ----

	@Test
	public void testClaimJsonObject() throws Exception {
		String secret = "test-secret";

		org.json.JSONObject claims = new org.json.JSONObject();
		claims.put("role", "admin");
		claims.put("level", 5);
		claims.put("active", true);

		String token = UJwt.hs256Builder()
				.subject("user123")
				.claim(claims)
				.create(secret);

		JwtToken jwt = UJwt.verifyHs256(token, secret);
		assertEquals("user123", jwt.getSubject());
		assertEquals("admin", jwt.getStringClaim("role"));
		assertEquals(5, jwt.getIntClaim("level"));
		assertTrue(jwt.getBooleanClaim("active"));

		printCaption("testClaimJsonObject passed");
	}

	// ---- 无效 Token 格式 ----

	@Test
	public void testInvalidTokenFormat() {
		assertThrows(IllegalArgumentException.class, () -> {
			UJwt.parse("invalid-token");
		});

		assertThrows(IllegalArgumentException.class, () -> {
			UJwt.parse("");
		});

		assertThrows(IllegalArgumentException.class, () -> {
			UJwt.parse(null);
		});

		printCaption("testInvalidTokenFormat passed");
	}

	// ---- RSA Builder 缺少密钥 ----

	@Test
	public void testRsaBuilderMissingKey() {
		assertThrows(IllegalStateException.class, () -> {
			UJwt.rs256Builder().subject("test").create();
		});

		printCaption("testRsaBuilderMissingKey passed");
	}

	// ---- Token 篡改检测 ----

	@Test
	public void testTokenTamperDetection() throws Exception {
		String secret = "test-secret";

		String token = UJwt.hs256Builder()
				.subject("user123")
				.claim("role", "user")
				.create(secret);

		// 篡改 payload 中的 role
		String[] parts = token.split("\\.");
		String tamperedPayload = java.util.Base64.getUrlEncoder().withoutPadding()
				.encodeToString("{\"sub\":\"user123\",\"role\":\"admin\"}".getBytes());
		String tamperedToken = parts[0] + "." + tamperedPayload + "." + parts[2];

		assertThrows(Exception.class, () -> {
			UJwt.verifyHs256(tamperedToken, secret);
		});

		printCaption("testTokenTamperDetection passed");
	}
}
