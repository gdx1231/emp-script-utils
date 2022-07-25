package com.gdxsoft.easyweb.utils.types;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public class UInt64 extends Number implements Comparable<UInt64> {
	private static final long serialVersionUID = -3630290028733469911L;

	/**
	 * 2^64 - 1
	 */
	private static final BigInteger maxValue = BigInteger.valueOf(2).pow(64).subtract(BigInteger.valueOf(1));
	/**
	 * The minimum value of a UInt64
	 */
	public final static UInt64 MIN_VALUE = new UInt64();
	/**
	 * The maximum value of a UInt64
	 */
	public final static UInt64 MAX_VALUE = valueOf(maxValue);

	/**
	 * Return a {@code UInt64} containing the specified value.
	 *
	 * @param value The value to create a {@code BigInteger} for.
	 * @return A {@code UInt64} containing the specified value.
	 */
	public static UInt64 valueOf(BigInteger value) {
		if (value.compareTo(maxValue) > 0) {
			throw new ArithmeticException(
					"the result (" + value + ") greate than the UInt64.MAX_VALUE (" + maxValue + ")");
		} else if (value.doubleValue() < 0) {
			throw new ArithmeticException("the result (" + value + ") is negative");
		}
		UInt64 uint64 = new UInt64();
		uint64.innerValue = value;

		return uint64;
	}

	/**
	 * Returns a {@code Long} object holding the value extracted from the specified
	 * {@code String} when parsed with the radix given by the second argument.
	 *
	 * @param s     the string to be parsed
	 * @param radix the radix to be used in interpreting {@code s}
	 * @return a {@code UInt64} object holding the value represented by the string
	 *         argument in the specified radix.
	 * @throws NumberFormatException If the {@code String} does not contain a
	 *                               parsable {@code UInt64}.
	 */
	public static UInt64 valueOf(String s, int radix) throws NumberFormatException {
		BigInteger v = new BigInteger(s, radix);
		return valueOf(v);
	}

	/**
	 * Returns a {@code UInt64} object holding the value of the specified
	 * {@code String}.
	 *
	 * @param s the string to be parsed.
	 * @return a {@code UInt64} object holding the value represented by the string
	 *         argument.
	 * @throws NumberFormatException If the string cannot be parsed as a
	 *                               {@code UInt64}.
	 */
	public static UInt64 valueOf(String s) throws NumberFormatException {
		BigInteger v = new BigInteger(s);
		return valueOf(v);
	}
	
	/**
	 * Return a {@code UInt64} containing the specified value.
	 *
	 * @param value The value to create a {@code long} for.
	 * @return A {@code UInt64} containing the specified value.
	 */
	public static UInt64 valueOf(long value) {
		BigInteger v1 = new BigInteger(Long.toUnsignedString(value));
		return valueOf(v1);
	}

	/**
	 * Return a {@code UInt64} containing the specified value.
	 *
	 * @param value The value to create a {@code int} for.
	 * @return A {@code UInt64} containing the specified value.
	 */
	public static UInt64 valueOf(int value) {
		BigInteger v1 = BigInteger.valueOf(Integer.toUnsignedLong(value));
		return valueOf(v1);
	}

	/**
	 * Return a {@code UInt64} containing the specified value.
	 *
	 * @param value The value to create a {@code short} for.
	 * @return A {@code UInt64} containing the specified value.
	 */
	public static UInt64 valueOf(short value) {
		BigInteger v1 = BigInteger.valueOf(Short.toUnsignedLong(value));
		return valueOf(v1);
	}

	/**
	 * Compares two {@code UInt64} values numerically. The value returned is
	 * identical to what would be returned by:
	 *
	 * @param x the first {@code UInt64} to compare
	 * @param y the second {@code UInt64} to compare
	 * @return the value {@code 0} if {@code x == y}; a value less than {@code 0} if
	 *         {@code x < y}; and a value greater than {@code 0} if {@code x > y}
	 */
	public static int compare(UInt64 x, UInt64 y) {
		return x.compareTo(y);
	}

	/**
	 * Returns a string representation of the first argument in the radix specified
	 * by the second argument.
	 *
	 * @param uint64 a {@code UInt64} to be converted to a string.
	 * @param radix  the radix to use in the string representation.
	 * @return a string representation of the argument in the specified radix.
	 * @see java.lang.Character#MAX_RADIX
	 * @see java.lang.Character#MIN_RADIX
	 */
	public static String toString(UInt64 uint64, int radix) {
		return uint64.innerValue.toString(radix);
	}

	/**
	 * Returns a string representation of the {@code UInt64} argument as an unsigned
	 * integer in base&nbsp;16.
	 *
	 * @param uint64 a {@code UInt64} to be converted to a string.
	 * @return the string representation of the unsigned {@code UInt64} value
	 *         represented by the argument in hexadecimal (base&nbsp;16).
	 */
	public static String toHexString(UInt64 uint64) {
		return uint64.innerValue.toString(4);
	}

	/**
	 * Returns a string representation of the {@code UInt64} argument as an unsigned
	 * integer in base&nbsp;8.
	 *
	 * @param uint64 a {@code UInt64} to be converted to a string.
	 * @return the string representation of the unsigned {@code UInt64} value
	 *         represented by the argument in octal (base&nbsp;8).
	 */
	public static String toOctalString(UInt64 uint64) {
		return uint64.innerValue.toString(3);
	}

	/**
	 * Returns a string representation of the {@code UInt64} argument as an unsigned
	 * integer in base&nbsp;2.
	 * 
	 * @param uint64 a {@code UInt64} to be converted to a string.
	 * @return the string representation of the unsigned {@code UInt64} value
	 *         represented by the argument in binary (base&nbsp;2).
	 */
	public static String toBinaryString(UInt64 uint64) {
		return uint64.innerValue.toString(1);
	}

	

	private BigInteger innerValue;

	public UInt64() {
		innerValue = BigInteger.valueOf(0);
	}

	/**
	 * Add this with value
	 * 
	 * @param value
	 * @return this + value
	 */
	public UInt64 add(UInt64 value) {
		if (value == null) {
			return null;
		}
		BigInteger v1 = this.innerValue.add(value.innerValue);
		return UInt64.valueOf(v1);

	}

	/**
	 * Add this with value
	 * 
	 * @param value
	 * @return this + value
	 */
	public UInt64 add(UInt32 value) {
		if (value == null) {
			return null;
		}

		return this.add(value.toUInt64());
	}

	/**
	 * Add this with value
	 * 
	 * @param value
	 * @return this + value
	 */
	public UInt64 add(UInt16 value) {
		if (value == null) {
			return null;
		}
		return this.add(value.toUInt64());
	}

	/**
	 * Add this with value
	 * 
	 * @param value
	 * @return this + value
	 */
	public UInt64 add(long value) {
		BigInteger v1 = this.innerValue.add(BigInteger.valueOf(value));
		return UInt64.valueOf(v1);
	}

	/**
	 * Add this with value
	 * 
	 * @param value
	 * @return this + value
	 */
	public UInt64 add(int value) {
		BigInteger v1 = this.innerValue.add(new BigInteger(Integer.toString(value)));
		return UInt64.valueOf(v1);
	}

	/**
	 * subtract this with value
	 * 
	 * @param value
	 * @return this - value
	 */
	public UInt64 subtract(UInt64 value) {
		if (value == null) {
			return null;
		}
		BigInteger v1 = this.innerValue.subtract(value.innerValue);
		return UInt64.valueOf(v1);
	}

	/**
	 * subtract this with value
	 * 
	 * @param value
	 * @return this - value
	 */
	public UInt64 subtract(UInt32 value) {
		return this.add(value.getInnerValue() * -1);
	}

	/**
	 * subtract this with value
	 * 
	 * @param value
	 * @return this - value
	 */
	public UInt64 subtract(UInt16 value) {
		return this.add(value.getInnerValue() * -1);
	}

	/**
	 * subtract this with value
	 * 
	 * @param value
	 * @return this - value
	 */
	public UInt64 subtract(int value) {
		return this.add(value * -1);
	}

	/**
	 * subtract this with value
	 * 
	 * @param value
	 * @return this - value
	 */
	public UInt64 subtract(long value) {
		return this.add(value * -1);
	}

	/**
	 * divide this with value
	 * 
	 * @param value
	 * @return this / value
	 */
	public UInt64 divide(UInt64 value) {
		if (value == null) {
			return null;
		}
		BigInteger v1 = this.innerValue.divide(value.innerValue);
		return UInt64.valueOf(v1);
	}

	/**
	 * divide this with value
	 * 
	 * @param value
	 * @return this / value
	 */
	public UInt64 divide(UInt32 value) {
		if (value == null) {
			return null;
		}
		return divide(value.toUInt64());
	}

	/**
	 * divide this with value
	 * 
	 * @param value
	 * @return this / value
	 */
	public UInt64 divide(UInt16 value) {
		if (value == null) {
			return null;
		}
		return divide(value.toUInt64());
	}

	/**
	 * divide this with value
	 * 
	 * @param value
	 * @return this / value
	 */
	public UInt64 divide(int value) {
		BigInteger v1 = this.innerValue.divide(new BigInteger(Integer.toString(value)));
		return UInt64.valueOf(v1);
	}

	/**
	 * divide this with value
	 * 
	 * @param value
	 * @return this / value
	 */
	public UInt64 divide(long value) {
		BigInteger v1 = this.innerValue.divide(new BigInteger(Long.toString(value)));
		return UInt64.valueOf(v1);
	}

	/**
	 * multiply this with value
	 * 
	 * @param value
	 * @return this * value
	 */
	public UInt64 multiply(UInt64 value) {
		if (value == null) {
			return null;
		}
		BigInteger v = this.innerValue.multiply(value.innerValue);
		return UInt64.valueOf(v);
	}

	/**
	 * multiply this with value
	 * 
	 * @param value
	 * @return this * value
	 */
	public UInt64 multiply(UInt32 value) {
		if (value == null) {
			return null;
		}

		return this.multiply(value.toUInt64());
	}

	/**
	 * multiply this with value
	 * 
	 * @param value
	 * @return this * value
	 */
	public UInt64 multiply(UInt16 value) {
		if (value == null) {
			return null;
		}
		return this.multiply(value.toUInt64());
	}

	/**
	 * multiply this with value
	 * 
	 * @param value
	 * @return this * value
	 */
	public UInt64 multiply(int value) {
		BigInteger v = this.innerValue.multiply(new BigInteger(Integer.toString(value)));
		return UInt64.valueOf(v);
	}

	/**
	 * multiply this with value
	 * 
	 * @param value
	 * @return this * value
	 */
	public UInt64 multiply(long value) {
		BigInteger v = this.innerValue.multiply(new BigInteger(Long.toString(value)));
		return UInt64.valueOf(v);
	}

	/**
	 * Compares this object to the specified object. The result is {@code true} if
	 * and only if the argument is not {@code null} and is a {@code Long} object
	 * that contains the same {@code long} value as this object.
	 *
	 * @param obj the object to compare with.
	 * @return {@code true} if the objects are the same; {@code false} otherwise.
	 */
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof UInt64)) {
			return false;
		}
		return innerValue.longValue() == ((UInt64) obj).innerValue.longValue();
	}

	public BigInteger bigInteger() {
		return innerValue;
	}

	@Override
	public int intValue() {
		return toUInt32().intValue();
	}

	@Override
	public long longValue() {
		return ByteBuffer.wrap(toBytes()).getLong();
	}

	@Override
	public float floatValue() {
		return innerValue.floatValue();
	}

	@Override
	public double doubleValue() {
		return innerValue.doubleValue();
	}

	@Override
	public String toString() {
		return this.innerValue.toString();
	}

	public byte[] toBytes() {
		return this.innerValue.toByteArray();
	}

	/**
	 * Converts this {@code UInt64} to an {@code UInt16}.
	 *
	 * @return {@code UInt16}.
	 */
	public UInt16 toUInt16() {
		return UInt16.valueOf(this.innerValue.longValue());

	}

	/**
	 * Converts this {@code UInt64} to an {@code UInt32}.
	 *
	 * @return {@code UInt32}.
	 */
	public UInt32 toUInt32() {
		return UInt32.valueOf(this.innerValue);
	}

	/**
	 * Compares two {@code UInt64} objects numerically.
	 *
	 * @param another the {@code UInt64} to be compared.
	 * @return the value {@code 0} if this {@code Long} is equal to the argument
	 *         {@code UInt64}; <br>
	 *         a value less than {@code 0} if this {@code UInt64} is numerically
	 *         less than the argument {@code Long}; <br>
	 *         and a value greater than {@code 0} if this {@code UInt64} is
	 *         numerically greater than the argument {@code UInt64} (signed
	 *         comparison).
	 */
	@Override
	public int compareTo(UInt64 another) {
		return compare(this, another);
	}

	/**
	 * @return the inner value
	 */
	public BigInteger getInnerValue() {
		return innerValue;
	}
}
