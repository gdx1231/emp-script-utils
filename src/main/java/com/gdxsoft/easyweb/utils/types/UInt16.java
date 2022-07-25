package com.gdxsoft.easyweb.utils.types;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public class UInt16 extends Number implements Comparable<UInt16> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int maxValue = 65535; // 2^16 - 1
	/**
	 * The minimum value of a UInt16
	 */
	public final static UInt16 MIN_VALUE = new UInt16();
	/**
	 * The maximum value of a UInt16
	 */
	public final static UInt16 MAX_VALUE = valueOf(maxValue);

	/**
	 * Returns a {@code Long} object holding the value extracted from the specified
	 * {@code String} when parsed with the radix given by the second argument.
	 *
	 * @param s     the string to be parsed
	 * @param radix the radix to be used in interpreting {@code s}
	 * @return a {@code UInt16} object holding the value represented by the string
	 *         argument in the specified radix.
	 * @throws NumberFormatException If the {@code String} does not contain a
	 *                               parsable {@code UInt16}.
	 */
	public static UInt16 valueOf(String s, int radix) throws NumberFormatException {
		return valueOf(Integer.parseInt(s, radix));
	}

	/**
	 * Returns a {@code UInt16} object holding the value of the specified
	 * {@code String}.
	 *
	 * @param s the string to be parsed.
	 * @return a {@code UInt16} object holding the value represented by the string
	 *         argument.
	 * @throws NumberFormatException If the string cannot be parsed as a
	 *                               {@code UInt16}.
	 */
	public static UInt16 valueOf(String s) throws NumberFormatException {
		return valueOf(Integer.parseInt(s, 10));
	}
	
	/**
	 * Return a {@code UInt16} containing the specified value.
	 *
	 * @param value The value to create a {@code short} for.
	 * @return A {@code UInt16} containing the specified value.
	 */
	public static UInt16 valueOf(short value) {
		return valueOf(Short.toUnsignedInt(value));
	}

	/**
	 * Return a {@code UInt16} containing the specified value.
	 *
	 * @param value The value to create a {@code BigInteger} for.
	 * @return A {@code UInt16} containing the specified value.
	 */
	public static UInt16 valueOf(BigInteger value) {
		return valueOf(value.intValue());
	}

	/**
	 * Return a {@code UInt16} containing the specified value.
	 *
	 * @param value The value to create a {@code int} for.
	 * @return A {@code UInt16} containing the specified value.
	 */
	public static UInt16 valueOf(int value) {
		if (value > maxValue) {
			throw new ArithmeticException(
					"the result (" + value + ") greate than the UInt16.MAX_VALUE (" + maxValue + ")");
		} else if (value < 0) {
			throw new ArithmeticException("the result (" + value + ") is negative");
		}
		UInt16 uint = new UInt16();
		uint.innerValue = value;
		return uint;
	}

	/**
	 * Return a {@code UInt16} containing the specified value.
	 *
	 * @param value The value to create a {@code long} for.
	 * @return A {@code UInt16} containing the specified value.
	 */
	public static UInt16 valueOf(long value) {
		return valueOf((int) value);
	}

	/**
	 * Compares two {@code UInt16} values numerically. The value returned is
	 * identical to what would be returned by:
	 *
	 * @param x the first {@code UInt16} to compare
	 * @param y the second {@code UInt16} to compare
	 * @return the value {@code 0} if {@code x == y}; a value less than {@code 0} if
	 *         {@code x < y}; and a value greater than {@code 0} if {@code x > y}
	 */
	public static int compare(UInt16 x, UInt16 y) {
		return Integer.compare(x.innerValue, y.innerValue);
	}

	/**
	 * Returns a string representation of the first argument in the radix specified
	 * by the second argument.
	 *
	 * @param uint  a {@code UInt16} to be converted to a string.
	 * @param radix the radix to use in the string representation.
	 * @return a string representation of the argument in the specified radix.
	 * @see java.lang.Character#MAX_RADIX
	 * @see java.lang.Character#MIN_RADIX
	 */
	public static String toString(UInt16 uint, int radix) {
		return Integer.toString(uint.innerValue, radix);
	}

	/**
	 * Returns a string representation of the {@code UInt16} argument as an unsigned
	 * integer in base&nbsp;16.
	 *
	 * @param uint a {@code UInt16} to be converted to a string.
	 * @return the string representation of the unsigned {@code UInt16} value
	 *         represented by the argument in hexadecimal (base&nbsp;16).
	 */
	public static String toHexString(UInt16 uint) {
		return toString(uint, 4);
	}

	/**
	 * Returns a string representation of the {@code UInt16} argument as an unsigned
	 * integer in base&nbsp;8.
	 *
	 * @param uint a {@code UInt16} to be converted to a string.
	 * @return the string representation of the unsigned {@code UInt16} value
	 *         represented by the argument in octal (base&nbsp;8).
	 */
	public static String toOctalString(UInt16 uint) {
		return toString(uint, 3);
	}

	/**
	 * Returns a string representation of the {@code UInt16} argument as an unsigned
	 * integer in base&nbsp;2.
	 * 
	 * @param uint a {@code UInt16} to be converted to a string.
	 * @return the string representation of the unsigned {@code UInt16} value
	 *         represented by the argument in binary (base&nbsp;2).
	 */
	public static String toBinaryString(UInt16 uint) {
		return toString(uint, 1);
	}

	

	private Integer innerValue;

	public UInt16() {
		innerValue = 0;
	}

	/**
	 * Add this with value
	 * 
	 * @param value
	 * @return this + value
	 */
	public UInt16 add(UInt16 value) {
		if (value == null) {
			return null;
		}
		int v1 = this.innerValue + value.innerValue;
		return UInt16.valueOf(v1);

	}

	public UInt16 add(int value) {
		int v1 = this.innerValue + value;
		return UInt16.valueOf(v1);
	}

	/**
	 * subtract this with value
	 * 
	 * @param value
	 * @return this - value
	 */
	public UInt16 subtract(UInt16 value) {
		if (value == null) {
			return null;
		}
		int v1 = this.innerValue - value.innerValue;
		return UInt16.valueOf(v1);
	}

	public UInt16 subtract(int value) {
		return this.add(value * -1);
	}

	/**
	 * divide this with value
	 * 
	 * @param value
	 * @return this / value
	 */
	public UInt16 divide(UInt16 value) {
		if (value == null) {
			return null;
		}
		int v1 = this.innerValue / value.innerValue;
		return UInt16.valueOf(v1);
	}

	public UInt16 divide(int value) {
		int v1 = this.innerValue / value;
		return UInt16.valueOf(v1);
	}

	/**
	 * multiply this with value
	 * 
	 * @param value
	 * @return this / value
	 */
	public UInt16 multiply(UInt16 value) {
		if (value == null) {
			return null;
		}
		int v = this.innerValue * value.innerValue;
		return UInt16.valueOf(v);
	}

	/**
	 * multiply this with value
	 * 
	 * @param value
	 * @return this / value
	 */
	public UInt16 multiply(int value) {
		int v = this.innerValue * value;
		return UInt16.valueOf(v);
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
		if (obj == null || !(obj instanceof UInt16)) {
			return false;
		}
		return innerValue.intValue() == ((UInt16) obj).innerValue.intValue();
	}

	public BigInteger bigInteger() {
		return BigInteger.valueOf(innerValue);
	}

	@Override
	public short shortValue() {
		return ByteBuffer.wrap(toBytes()).getShort();
	}

	@Override
	public int intValue() {
		return innerValue.intValue();
	}

	@Override
	public long longValue() {
		return innerValue.longValue();
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
		return Long.toString(this.innerValue);
	}

	public byte[] toBytes() {
		ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
		buffer.putInt(this.innerValue);
		byte[] bufs = buffer.array();
		byte[] byte2 = { bufs[2], bufs[3] };

		return byte2;
	}

	/**
	 * Converts this {@code UInt16} to an {@code UInt32}.
	 *
	 * @return {@code UInt16}.
	 */
	public UInt16 toUInt32() {
		return UInt16.valueOf(this.innerValue);

	}

	/**
	 * Converts this {@code UInt16} to an {@code UInt64}.
	 *
	 * @return {@code UInt64}.
	 */
	public UInt64 toUInt64() {
		return UInt64.valueOf(this.innerValue);
	}

	/**
	 * Compares two {@code UInt16} objects numerically.
	 *
	 * @param another the {@code UInt16} to be compared.
	 * @return the value {@code 0} if this {@code Long} is equal to the argument
	 *         {@code UInt16}; <br>
	 *         a value less than {@code 0} if this {@code UInt16} is numerically
	 *         less than the argument {@code Long}; <br>
	 *         and a value greater than {@code 0} if this {@code UInt16} is
	 *         numerically greater than the argument {@code UInt16} (signed
	 *         comparison).
	 */
	@Override
	public int compareTo(UInt16 another) {
		return compare(this, another);
	}

	/**
	 * @return the inner value
	 */
	public Integer getInnerValue() {
		return innerValue;
	}
}
