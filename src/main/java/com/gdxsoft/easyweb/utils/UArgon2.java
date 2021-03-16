package com.gdxsoft.easyweb.utils;

import java.io.IOException;
import java.security.MessageDigest;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.crypto.params.Argon2Parameters.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Function Argon2
 * </p>
 * Inputs:<br>
 * <ol>
 * <li><b>password (P):</b> Bytes (0..232-1) Password (or message) to be hashed</li>
 * <li><b>salt (S):</b> Bytes (8..232-1) Salt (16 bytes recommended for password hashing)</li>
 * <li><b>parallelism (p):</b> Number (1..224-1) Degree of parallelism (i.e. number of threads)</li>
 * <li><b>tagLength (T):</b> Number (4..232-1) Desired number of returned bytes</li>
 * <li><b>memorySizeKB (m):</b> Number (8p..232-1) Amount of memory (in kibibytes) to use</li>
 * <li><b>iterations (t):</b> Number (1..232-1) Number of iterations to perform</li>
 * <li><b>version (v):</b> Number (0x13) The current version is 0x13 (19 decimal)</li>
 * <li><b>key (K):</b> Bytes (0..232-1) Optional key (Errata: PDF says 0..32 bytes, RFC says 0..232 bytes)</li>
 * <li><b>associatedData (X):</b> Bytes (0..232-1) Optional arbitrary extra data</li>
 * <li><b>hashType (y):</b> Number (0=Argon2d, 1=Argon2i, 2=Argon2id)</li>
 * </ol>
 * Output:<br>
 * tag: Bytes (tagLength) The resulting generated bytes, tagLength bytes long
 *
 */
public class UArgon2 {
	private static Logger LOGGER = LoggerFactory.getLogger(UArgon2.class);
	private static final int DEFAULT_SALT_LENGTH = 16;
	private static final int DEFAULT_HASH_LENGTH = 32;
	private static final int DEFAULT_PARALLELISM = 1;
	private static final int DEFAULT_MEMORY_KB = 32;
	private static final int DEFAULT_ITERATIONS = 3;
	private static final int DEFAULT_ARGON2_TYPE = Argon2Parameters.ARGON2_id;
	private static final int DEFAULT_VERSION = Argon2Parameters.ARGON2_VERSION_13;

	/**
	 * Hash the password
	 * 
	 * @param password
	 * @return
	 */
	public static String hashPwd(String password) {
		return new UArgon2().hashPassword(password);
	}

	/**
	 * Verify password
	 * 
	 * @param plainPassword  the plain password
	 * @param hashedPassword the hashed password
	 * @return true = successful, false = fail
	 */
	public static boolean verifyPwd(String plainPassword, String hashedPassword) {
		return new UArgon2().verifyPassword(plainPassword, hashedPassword);
	}

	private Builder builder;
	private int argon2Type; // 加密解密类型
	private int version; // 版本
	private int parallelity; // 并行
	private int memory; // 所需内容
	private int iterations; // 迭代
	private int saltLength; // 盐的字节数
	private byte[] salt; // 盐
	private String password; // 原始密码
	private byte[] passwordHash; // hash后的密码，没有版本等其它信息

	public UArgon2() {
		argon2Type = DEFAULT_ARGON2_TYPE;
		version = DEFAULT_VERSION;
		parallelity = DEFAULT_PARALLELISM;
		iterations = DEFAULT_ITERATIONS;
		memory = DEFAULT_MEMORY_KB;
		saltLength = DEFAULT_SALT_LENGTH;
	}

	public String toString() {
		return encode();
	}

	public Builder getBuilder() {
		Builder builder = new Builder(argon2Type).withParallelism(parallelity).withIterations(iterations)
				.withMemoryAsKB(memory).withVersion(version).withSalt(salt);
		return builder;
	}

	/**
	 * Verify password
	 * 
	 * @param plainPassword
	 * @param hashedPassword
	 * @return
	 */
	public boolean verifyPassword(String plainPassword, String hashedPassword) {

		try {
			this.initVerifyParameter(hashedPassword);
		} catch (IllegalArgumentException | IOException e1) {
			LOGGER.error(hashedPassword, e1.getMessage());
			return false;
		}

		builder = this.getBuilder();

		Argon2BytesGenerator dig = new Argon2BytesGenerator();

		dig.init(builder.build());

		byte[] result = new byte[DEFAULT_HASH_LENGTH];
		dig.generateBytes(plainPassword.toCharArray(), result);

		return MessageDigest.isEqual(this.getPasswordHash(), result);
	}

	public boolean checkPassword() {
		Argon2Parameters parameters = this.builder.build();
		Argon2BytesGenerator dig = new Argon2BytesGenerator();
		dig.init(parameters);
		byte[] result = new byte[DEFAULT_HASH_LENGTH];
		dig.generateBytes(password.toCharArray(), result);
		return MessageDigest.isEqual(this.passwordHash, result);
	}

	public String hashPassword(String password) {
		this.salt = UAes.generateRandomBytes(this.saltLength);
		this.setPassword(password);

		this.builder = getBuilder();
		Argon2Parameters parameters = this.builder.build();
		Argon2BytesGenerator gen = new Argon2BytesGenerator();
		gen.init(parameters);

		byte[] result = new byte[DEFAULT_HASH_LENGTH];

		gen.generateBytes(password.toCharArray(), result, 0, result.length);
		this.setPasswordHash(result);

		String encodedPassword = this.encode();
		return encodedPassword;
	}

	public void initVerifyParameter(String encodedHash) throws IllegalArgumentException, IOException {
		String[] parts = encodedHash.split("\\$");
		if (parts.length < 6) {
			throw new IllegalArgumentException("Invalid encoded Argon2-hash");
		}
		UArgon2 u2 = this;

		// 1 = version
		switch (parts[1]) {
		case "argon2d":
			u2.setArgon2Type(Argon2Parameters.ARGON2_d);
			break;
		case "argon2i":
			u2.setArgon2Type(Argon2Parameters.ARGON2_i);
			break;
		case "argon2id":
			u2.setArgon2Type(Argon2Parameters.ARGON2_id);
			break;
		default:
			throw new IllegalArgumentException("Invalid algorithm type: " + parts[0]);
		}

		// 2 = version
		u2.setVersion(Integer.parseInt(parts[2].substring(2)));

		// 3 = performances
		String[] performanceParams = parts[3].split(",");
		if (performanceParams.length != 3) {
			throw new IllegalArgumentException("Amount of performance parameters invalid");
		}
		if (!performanceParams[0].startsWith("m=")) {
			throw new IllegalArgumentException("Invalid memory parameter");
		}
		u2.setMemory(Integer.parseInt(performanceParams[0].substring(2)));
		if (!performanceParams[1].startsWith("t=")) {
			throw new IllegalArgumentException("Invalid iterations parameter");
		}
		u2.setIterations(Integer.parseInt(performanceParams[1].substring(2)));
		if (!performanceParams[2].startsWith("p=")) {
			throw new IllegalArgumentException("Invalid parallelity parameter");
		}
		u2.setParallelity(Integer.parseInt(performanceParams[2].substring(2)));

		// 4 = salt
		u2.setSalt(UConvert.FromBase64String(parts[4]));

		// 5 = hash password
		u2.setPasswordHash(UConvert.FromBase64String(parts[5]));
	}

	private String encode() {
		StringBuilder stringBuilder = new StringBuilder();
		switch (this.argon2Type) {
		case Argon2Parameters.ARGON2_d:
			stringBuilder.append("$argon2d");
			break;
		case Argon2Parameters.ARGON2_i:
			stringBuilder.append("$argon2i");
			break;
		case Argon2Parameters.ARGON2_id:
			stringBuilder.append("$argon2id");
			break;
		default:
		}
		stringBuilder.append("$v=").append(this.version).append("$m=").append(this.memory).append(",t=")
				.append(this.iterations).append(",p=").append(this.parallelity);

		if (salt != null) {
			// salt
			stringBuilder.append("$").append(UConvert.ToBase64String(this.salt));
		}
		if (this.passwordHash != null) {
			// password
			stringBuilder.append("$").append(UConvert.ToBase64String(this.passwordHash));
		}
		return stringBuilder.toString();
	}

	public int getArgon2Type() {
		return argon2Type;
	}

	public void setArgon2Type(int argon2Type) {
		this.argon2Type = argon2Type;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public int getParallelity() {
		return parallelity;
	}

	public void setParallelity(int parallelity) {
		this.parallelity = parallelity;
	}

	public int getMemory() {
		return memory;
	}

	public void setMemory(int memory) {
		this.memory = memory;
	}

	public int getIterations() {
		return iterations;
	}

	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	public int getSaltLength() {
		return saltLength;
	}

	public void setSaltLength(int saltLength) {
		this.saltLength = saltLength;
	}

	public byte[] getSalt() {
		return salt;
	}

	public void setSalt(byte[] salt) {
		this.salt = salt;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public byte[] getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(byte[] passwordHash) {
		this.passwordHash = passwordHash;
	}
}