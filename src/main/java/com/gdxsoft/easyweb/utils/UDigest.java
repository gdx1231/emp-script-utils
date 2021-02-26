package com.gdxsoft.easyweb.utils;

import java.io.UnsupportedEncodingException;

import org.bouncycastle.crypto.ExtendedDigest;
import org.bouncycastle.crypto.digests.*;

/**
 * 摘要算法的通用类 摘要算法<br>
 * md5, sha1, sha224, sha256, sha384, sha512, sha3, sm3<br>
 * RIPEMD320, RIPEMD256, RIPEMD160, RIPEMD128<br>
 * GOST3411, GOST3411_2012_256, GOST3411_2012_512, Tiger
 * 
 * @author admin
 *
 */
public class UDigest {

	/**
	 * 摘要算法
	 * 
	 * @param utf8String 字符串，获取utf8 bytes
	 * @param algorithm  算法 md5, sha1, sha224, sha256, sha384, sha512, sha3, sm3<br>
	 *                   RIPEMD320, RIPEMD256, RIPEMD160, RIPEMD128<br>
	 *                   GOST3411, GOST3411_2012_256, GOST3411_2012_512, Tiger
	 * @return 摘要 Hex
	 */
	public static String digestHex(String utf8String, String algorithm) {
		byte[] result = digest(utf8String, algorithm);
		return Utils.bytes2hex(result);
	}

	/**
	 * 摘要算法
	 * 
	 * @param data      二进制数据
	 * @param algorithm 算法 md5, sha1, sha224, sha256, sha384, sha512, sha3, sm3<br>
	 *                  RIPEMD320, RIPEMD256, RIPEMD160, RIPEMD128<br>
	 *                  GOST3411, GOST3411_2012_256, GOST3411_2012_512, Tiger
	 * @return 摘要 Hex
	 */
	public static String digestHex(byte[] data, String algorithm) {
		byte[] result = digest(data, algorithm);
		return Utils.bytes2hex(result);
	}

	/**
	 * 摘要算法
	 * 
	 * @param utf8String 字符串，获取utf8 bytes
	 * @param algorithm  算法 md5, sha1, sha224, sha256, sha384, sha512, sha3, sm3<br>
	 *                   RIPEMD320, RIPEMD256, RIPEMD160, RIPEMD128<br>
	 *                   GOST3411, GOST3411_2012_256, GOST3411_2012_512, Tiger
	 * @return 摘要 Base64
	 */
	public static String digestBase64(String utf8String, String algorithm) {
		byte[] result = digest(utf8String, algorithm);
		return UConvert.ToBase64String(result);
	}

	/**
	 * 摘要算法
	 * 
	 * @param data      二进制数据
	 * @param algorithm 算法 md5, sha1, sha224, sha256, sha384, sha512, sha3, sm3<br>
	 *                  RIPEMD320, RIPEMD256, RIPEMD160, RIPEMD128<br>
	 *                  GOST3411, GOST3411_2012_256, GOST3411_2012_512, Tiger
	 * @return 摘要 Base64
	 */
	public static String digestBase64(byte[] data, String algorithm) {
		byte[] result = digest(data, algorithm);
		return UConvert.ToBase64String(result);
	}

	/**
	 * 摘要算法
	 * 
	 * @param utf8String 字符串，获取utf8 bytes
	 * @param algorithm  算法 md5, sha1, sha224, sha256, sha384, sha512, sha3, sm3<br>
	 *                   RIPEMD320, RIPEMD256, RIPEMD160, RIPEMD128<br>
	 *                   GOST3411, GOST3411_2012_256, GOST3411_2012_512, Tiger
	 * @return 摘要二进制
	 */
	public static byte[] digest(String utf8String, String algorithm) {
		byte[] buf;
		try {
			buf = utf8String.getBytes("utf-8");
			return digest(buf, algorithm);
		} catch (UnsupportedEncodingException e) {
			return null;
		}

	}

	/**
	 * 摘要算法
	 * 
	 * @param data      二进制数据
	 * @param algorithm 算法 md5, sha1, sha224, sha256, sha384, sha512, sha3, sm3<br>
	 *                  RIPEMD320, RIPEMD256, RIPEMD160, RIPEMD128<br>
	 *                  GOST3411, GOST3411_2012_256, GOST3411_2012_512, Tiger
	 * @return 摘要二进制
	 */
	public static byte[] digest(byte[] data, String algorithm) {
		if ("md5".equalsIgnoreCase(algorithm) || "md-5".equalsIgnoreCase(algorithm)) {
			MD5Digest md5 = new MD5Digest();
			return digestGeneralDigest(data, md5);
		}
		if ("sha1".equalsIgnoreCase(algorithm) || "sha-1".equalsIgnoreCase(algorithm)) {
			SHA1Digest digest = new SHA1Digest();
			return digestGeneralDigest(data, digest);
		}
		/*
		 * SHA-3第三代安全散列算法(Secure Hash Algorithm
		 * 3)，之前名为Keccak（念作/ˈkɛtʃæk/或/kɛtʃɑːk/)）算法，设计者宣称在 Intel Core 2
		 * 的CPU上面，此算法的性能是12.5cpb（每字节周期数，cycles per byte）。不过，在硬件实做上面，这个算法比起其他算法明显的快上很多。
		 */
		if ("sha3".equalsIgnoreCase(algorithm) || "sha-3".equalsIgnoreCase(algorithm)) {
			SHA3Digest digest = new SHA3Digest();
			return digestKeccakDigest(data, digest);
		}

		if ("sha224".equalsIgnoreCase(algorithm) || "sha-224".equalsIgnoreCase(algorithm)) {
			SHA224Digest digest = new SHA224Digest();
			return digestGeneralDigest(data, digest);
		}
		if ("sha256".equalsIgnoreCase(algorithm) || "sha-256".equalsIgnoreCase(algorithm)) {
			SHA256Digest digest = new SHA256Digest();
			return digestGeneralDigest(data, digest);
		}

		if ("sha384".equalsIgnoreCase(algorithm) || "sha-384".equalsIgnoreCase(algorithm)) {
			SHA384Digest digest = new SHA384Digest();
			return digestLongDigest(data, digest);
		}
		if ("sha512".equalsIgnoreCase(algorithm) || "sha-512".equalsIgnoreCase(algorithm)) {
			SHA512Digest digest = new SHA512Digest();
			return digestLongDigest(data, digest);
		}
		// 国密
		if ("SM3".equalsIgnoreCase(algorithm) || "SM-3".equalsIgnoreCase(algorithm)) {
			SM3Digest digest = new SM3Digest();
			return digestGeneralDigest(data, digest);
		}

		/*
		 * RIPEMD（RACE Integrity Primitives Evaluation Message
		 * Digest，RACE原始完整性校验消息摘要，是Hans
		 * Dobbertin等3人在md4,md5的基础上，于1996年提出来的。算法共有4个标准128、160、256和320，其对应输出长度分别为16字节、
		 * 20字节、32字节和40字节。不过，让人难以致信的是RIPEMD的设计者们根本就没有真正设计256和320位这2种标准，
		 * 他们只是在128位和160位的基础上，修改了初始参数和s-box来达到输出为256和320位的目的。所以，256位的强度和128相当，
		 * 而320位的强度和160位相当。RIPEMD建立在md的基础之上，所以，其添加数据的方式和md5完全一样。
		 */
		if ("RIPEMD320".equalsIgnoreCase(algorithm)) {
			RIPEMD320Digest digest = new RIPEMD320Digest();
			return digestGeneralDigest(data, digest);
		}
		if ("RIPEMD256".equalsIgnoreCase(algorithm)) {
			RIPEMD256Digest digest = new RIPEMD256Digest();
			return digestGeneralDigest(data, digest);
		}

		if ("RIPEMD160".equalsIgnoreCase(algorithm)) {
			RIPEMD160Digest digest = new RIPEMD160Digest();
			return digestGeneralDigest(data, digest);
		}
		if ("RIPEMD128".equalsIgnoreCase(algorithm)) {
			RIPEMD128Digest digest = new RIPEMD128Digest();
			return digestGeneralDigest(data, digest);
		}

		/*
		 * 一.Gost算法 Gost(Gosudarstvennyi Standard)
		 * 算法是一种由前苏联设计的类似DES算法的分组密码算法.她是一个64位分组及256位密钥的采用32轮简单迭代型加密算法.
		 * DES算法中采用的是56位长密钥,在密码科学中,一个对称密码系统安全性是由算法的强度和密钥长度决定的,在确保算法足够强(
		 * 攻击密码系统的唯一方法就是采用穷举法试探所有可能的密钥)的前提下,密钥的长度直接决定着穷举攻击的复杂度:
		 */
		if ("GOST3411".equalsIgnoreCase(algorithm)) {
			GOST3411Digest digest = new GOST3411Digest();
			return digestExtendedDigest(data, digest);
		}
		if ("GOST3411_2012_256".equalsIgnoreCase(algorithm)) {
			GOST3411_2012_256Digest digest = new GOST3411_2012_256Digest();
			return digestExtendedDigest(data, digest);
		}
		if ("GOST3411_2012_512".equalsIgnoreCase(algorithm)) {
			GOST3411_2012_512Digest digest = new GOST3411_2012_512Digest();
			return digestExtendedDigest(data, digest);
		}

		// Tiger是一种散列算法，用于生成数据的密钥。
		// Tiger算法最早在1995年提出，运行在64位平台的192位版本，另外还有截短的128位和160位版本，它们与192位版本的初始化值没有区别，只是作了截短处理，就像是192位版本散列值的前缀。
		if ("Tiger".equalsIgnoreCase(algorithm)) {
			TigerDigest digest = new TigerDigest();
			return digestExtendedDigest(data, digest);
		}
		// BLAKE2 系列比常见的 MD5，SHA-1，SHA-2，SHA-3 更快，同时提供不低于 SHA-3 的安全性。
		// BLAKE2 系列从著名的 ChaCha 算法衍生而来，有两个主要版本 BLAKE2b（BLAKE2）和 BLAKE2s。
		// BLAKE2b 为 64 位 CPU（包括 ARM Neon）优化，可以生成最长64字节的摘要；BLAKE2s 为 8-32 位 CPU
		// 设计，可以生成最长 32 字节的摘要。
		if ("Blake2b".equalsIgnoreCase(algorithm)) {
			Blake2bDigest digest = new Blake2bDigest();
			return digestExtendedDigest(data, digest);
		}
		if ("Blake2s".equalsIgnoreCase(algorithm)) {
			Blake2sDigest digest = new Blake2sDigest();
			return digestExtendedDigest(data, digest);
		}
		if ("md2".equalsIgnoreCase(algorithm)) {
			MD2Digest md2 = new MD2Digest();
			return digestExtendedDigest(data, md2);
		}
		if ("md4".equalsIgnoreCase(algorithm)) {
			MD4Digest md4 = new MD4Digest();
			return digestGeneralDigest(data, md4);
		}
		return null;

	}

	private static byte[] digestKeccakDigest(byte[] data, KeccakDigest keccakDigest) {
		keccakDigest.update(data, 0, data.length);
		byte[] encrypt = new byte[keccakDigest.getDigestSize()];
		keccakDigest.doFinal(encrypt, 0);
		return encrypt;

	}

	private static byte[] digestExtendedDigest(byte[] data, ExtendedDigest extendedDigest) {
		extendedDigest.update(data, 0, data.length);
		byte[] encrypt = new byte[extendedDigest.getDigestSize()];
		extendedDigest.doFinal(encrypt, 0);
		return encrypt;

	}

	private static byte[] digestLongDigest(byte[] data, LongDigest longDigest) {
		longDigest.update(data, 0, data.length);
		byte[] encrypt = new byte[longDigest.getDigestSize()];
		longDigest.doFinal(encrypt, 0);
		return encrypt;
	}

	private static byte[] digestGeneralDigest(byte[] data, GeneralDigest generalDigest) {
		generalDigest.update(data, 0, data.length);
		byte[] encrypt = new byte[generalDigest.getDigestSize()];
		generalDigest.doFinal(encrypt, 0);
		return encrypt;
	}
}
