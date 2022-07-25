package com.gdxsoft.easyweb.utils.types;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public class UInt32 extends Number implements Comparable<UInt32> {
	private static final long serialVersionUID = 2201173633528553172L;
	private static final long maxValue = 4294967295l; // 2^32 - 1
	/**
	 * The minimum value of a UInt32
	 */
	public final static UInt32 MIN_VALUE = new UInt32();
	/**
	 * The maximum value of a UInt32
	 */
	public final static UInt32 MAX_VALUE = valueOf(maxValue);

	/**
	 * Returns a {@code Long} object holding the value extracted from the specified
	 * {@code String} when parsed with the radix given by the second argument.
	 *
	 * @param s     the string to be parsed
	 * @param radix the radix to be used in interpreting {@code s}
	 * @return a {@code UInt32} object holding the value represented by the string
	 *         argument in the specified radix.
	 * @throws NumberFormatException If the {@code String} does not contain a
	 *                               parsable {@code UInt32}.
	 */
	public static UInt32 valueOf(String s, int radix) throws NumberFormatException {
		return valueOf(Long.parseLong(s, radix));
	}

	/**
	 * Returns a {@code UInt32} object holding the value of the specified
	 * {@code String}.
	 *
	 * @param s the string to be parsed.
	 * @return a {@code UInt32} object holding the value represented by the string
	 *         argument.
	 * @throws NumberFormatException If the string cannot be parsed as a
	 *                               {@code UInt32}.
	 */
	public static UInt32 valueOf(String s) throws NumberFormatException {
		return valueOf(Long.parseLong(s, 10));
	}
	
	/**
	 * Return a {@code UInt32} containing the specified value.
	 *
	 * @param value The value to create a {@code BigInteger} for.
	 * @return A {@code UInt32} containing the specified value.
	 */
	public static UInt32 valueOf(BigInteger value) {
		return valueOf(value.longValue());
	}

	/**
	 * Return a {@code UInt32} containing the specified value.
	 *
	 * @param value The value to create a {@code long} for.
	 * @return A {@code UInt32} containing the specified value.
	 */
	public static UInt32 valueOf(long value) {
		if (value > maxValue) {
			throw new ArithmeticException(
					"the result (" + value + ") greate than the UInt32.MAX_VALUE (" + maxValue + ")");
		} else if (value < 0) {
			throw new ArithmeticException("the result (" + value + ") is negative");
		}
		UInt32 uint = new UInt32();
		uint.innerValue = value;

		return uint;
	}

	/**
	 * Return a {@code UInt32} containing the specified value.
	 *
	 * @param value The value to create a {@code int} for.
	 * @return A {@code UInt32} containing the specified value.
	 */
	public static UInt32 valueOf(int value) {
		return valueOf(Integer.toUnsignedLong(value));
	}

	/**
	 * Return a {@code UInt32} containing the specified value.
	 *
	 * @param value The value to create a {@code short} for.
	 * @return A {@code UInt32} containing the specified value.
	 */
	public static UInt32 valueOf(short value) {
		return valueOf(Short.toUnsignedLong(value));
	}
	
	/**
	 * Compares two {@code UInt32} values numerically. The value returned is
	 * identical to what would be returned by:
	 *
	 * @param x the first {@code UInt32} to compare
	 * @param y the second {@code UInt32} to compare
	 * @return the value {@code 0} if {@code x == y}; a value less than {@code 0} if
	 *         {@code x < y}; and a value greater than {@code 0} if {@code x > y}
	 */
	public static int compare(UInt32 x, UInt32 y) {
		return Long.compare(x.longValue(), y.longValue());
	}

	/**
	 * Returns a string representation of the first argument in the radix specified
	 * by the second argument.
	 *
	 * @param uint  a {@code UInt32} to be converted to a string.
	 * @param radix the radix to use in the string representation.
	 * @return a string representation of the argument in the specified radix.
	 * @see java.lang.Character#MAX_RADIX
	 * @see java.lang.Character#MIN_RADIX
	 */
	public static String toString(UInt32 uint, int radix) {
		return Long.toString(uint.longValue(), radix);
	}

	/**
	 * Returns a string representation of the {@code UInt32} argument as an unsigned
	 * integer in base&nbsp;16.
	 *
	 * @param uint a {@code UInt32} to be converted to a string.
	 * @return the string representation of the unsigned {@code UInt32} value
	 *         represented by the argument in hexadecimal (base&nbsp;16).
	 */
	public static String toHexString(UInt32 uint) {
		return toString(uint, 4);
	}

	/**
	 * Returns a string representation of the {@code UInt32} argument as an unsigned
	 * integer in base&nbsp;8.
	 *
	 * @param uint a {@code UInt32} to be converted to a string.
	 * @return the string representation of the unsigned {@code UInt32} value
	 *         represented by the argument in octal (base&nbsp;8).
	 */
	public static String toOctalString(UInt32 uint) {
		return toString(uint, 3);
	}

	/**
	 * Returns a string representation of the {@code UInt32} argument as an unsigned
	 * integer in base&nbsp;2.
	 * 
	 * @param uint a {@code UInt32} to be converted to a string.
	 * @return the string representation of the unsigned {@code UInt32} value
	 *         represented by the argument in binary (base&nbsp;2).
	 */
	public static String toBinaryString(UInt32 uint) {
		return toString(uint, 1);
	}

	

	private Long innerValue;

	public UInt32() {
		innerValue = 0l;
	}

	/**
	 * Add this with value
	 * 
	 * @param value
	 * @return this + value
	 */
	public UInt32 add(UInt32 value) {
		if (value == null) {
			return null;
		}
		long v1 = this.innerValue + value.innerValue;
		return UInt32.valueOf(v1);

	}

	/**
	 * Add this with value
	 * 
	 * @param value
	 * @return this + value
	 */
	public UInt32 add(UInt16 value) {
		if (value == null) {
			return null;
		}
		return this.add(value.toUInt32());

	}

	/**
	 * Add this with value
	 * 
	 * @param value
	 * @return this + value
	 */
	public UInt32 add(UInt64 value) {
		if (value == null) {
			return null;
		}
		return this.add(value.toUInt32());

	}

	/**
	 * Add this with value
	 * 
	 * @param value
	 * @return this + value
	 */
	public UInt32 add(int value) {
		long v1 = this.innerValue + value;
		return UInt32.valueOf(v1);
	}

	/**
	 * subtract this with value
	 * 
	 * @param value
	 * @return this - value
	 */
	public UInt32 subtract(UInt32 value) {
		if (value == null) {
			return null;
		}
		long v1 = this.innerValue.longValue() - value.innerValue.longValue();
		return UInt32.valueOf(v1);
	}

	/**
	 * subtract this with value
	 * 
	 * @param value
	 * @return this - value
	 */
	public UInt32 subtract(UInt16 value) {
		if (value == null) {
			return null;
		}
		return this.subtract(value.toUInt32());
	}

	/**
	 * subtract this with value
	 * 
	 * @param value
	 * @return this - value
	 */
	public UInt32 subtract(UInt64 value) {
		if (value == null) {
			return null;
		}
		return this.subtract(value.toUInt32());
	}

	/**
	 * subtract this with value
	 * 
	 * @param value
	 * @return this - value
	 */
	public UInt32 subtract(int value) {
		return this.add(value * -1);
	}

	/**
	 * divide this with value
	 * 
	 * @param value
	 * @return this / value
	 */
	public UInt32 divide(UInt32 value) {
		if (value == null) {
			return null;
		}
		long v1 = this.innerValue.longValue() / value.innerValue.longValue();
		return UInt32.valueOf(v1);
	}

	/**
	 * subtract this with value
	 * 
	 * @param value
	 * @return this - value
	 */
	public UInt32 divide(UInt16 value) {
		if (value == null) {
			return null;
		}
		return this.divide(value.toUInt32());
	}

	/**
	 * subtract this with value
	 * 
	 * @param value
	 * @return this - value
	 */
	public UInt32 divide(UInt64 value) {
		if (value == null) {
			return null;
		}
		return this.divide(value.toUInt32());
	}

	/**
	 * divide this with value
	 * 
	 * @param value
	 * @return this / value
	 */
	public UInt32 divide(int value) {
		long v1 = this.innerValue.longValue() / value;
		return UInt32.valueOf(v1);
	}

	/**
	 * multiply this with value
	 * 
	 * @param value
	 * @return this / value
	 */
	public UInt32 multiply(UInt32 value) {
		if (value == null) {
			return null;
		}
		long v = this.innerValue.longValue() * value.innerValue.longValue();
		return UInt32.valueOf(v);
	}

	/**
	 * subtract this with value
	 * 
	 * @param value
	 * @return this - value
	 */
	public UInt32 multiply(UInt16 value) {
		if (value == null) {
			return null;
		}
		return this.multiply(value.toUInt32());
	}

	/**
	 * subtract this with value
	 * 
	 * @param value
	 * @return this - value
	 */
	public UInt32 multiply(UInt64 value) {
		if (value == null) {
			return null;
		}
		return this.multiply(value.toUInt32());
	}

	/**
	 * multiply this with value
	 * 
	 * @param value
	 * @return this / value
	 */
	public UInt32 multiply(int value) {
		long v = this.innerValue.longValue() * value;
		return UInt32.valueOf(v);
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
		if (obj == null || !(obj instanceof UInt32)) {
			return false;
		}
		return innerValue.longValue() == ((UInt32) obj).innerValue.longValue();
	}

	public BigInteger bigInteger() {
		return BigInteger.valueOf(innerValue);
	}

	@Override
	public int intValue() {
		return ByteBuffer.wrap(toBytes()).getInt();
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
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.putLong(this.innerValue);
		byte[] bufs = buffer.array();
		byte[] byte4 = new byte[4];
		for (int i = 0; i < 4; i++) {
			byte4[i] = bufs[i + 4];
		}

		return byte4;
	}

	/**
	 * Converts this {@code UInt32} to an {@code UInt16}.
	 *
	 * @return {@code UInt16}.
	 */
	public UInt16 toUInt16() {
		return UInt16.valueOf(this.innerValue);

	}

	/**
	 * Converts this {@code UInt32} to an {@code UInt64}.
	 *
	 * @return {@code UInt64}.
	 */
	public UInt64 toUInt64() {
		return UInt64.valueOf(this.innerValue);
	}

	/**
	 * Compares two {@code UInt32} objects numerically.
	 *
	 * @param another the {@code UInt32} to be compared.
	 * @return the value {@code 0} if this {@code Long} is equal to the argument
	 *         {@code UInt32}; <br>
	 *         a value less than {@code 0} if this {@code UInt32} is numerically
	 *         less than the argument {@code Long}; <br>
	 *         and a value greater than {@code 0} if this {@code UInt32} is
	 *         numerically greater than the argument {@code UInt32} (signed
	 *         comparison).
	 */
	@Override
	public int compareTo(UInt32 another) {
		return compare(this, another);
	}

	/**
	 * @return the inner value
	 */
	public Long getInnerValue() {
		return innerValue;
	}
}
