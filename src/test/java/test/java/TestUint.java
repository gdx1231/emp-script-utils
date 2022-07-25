package test.java;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.types.UInt32;

public class TestUint extends TestBase {

	public static void main(String[] args) {
		TestUint t = new TestUint();
		t.testUint32();
	}

	@Test
	public void testUint32() {
		UInt32 uint = new UInt32();
		UInt32 uint1 = UInt32.valueOf(32);
		UInt32 uint2 = UInt32.MAX_VALUE;
		UInt32 uint3 = UInt32.valueOf(-10000);

		System.out.println(" new UInt32()=" + uint);
		System.out.println("uint1 = UInt32.valueOf(32)=" + uint1);
		System.out.println("uint2 = UInt32.MAX_VALUE=" + uint2);
		System.out.println("uint3 = UInt32.valueOf(-10000)=" + uint3);
		System.out.println("uint3 = UInt32.valueOf(-10000).toBytes=0x" + Utils.bytes2hex(uint3.toBytes()));
		System.out.println("uint3 = UInt32.valueOf(-10000).toInteger=" + ByteBuffer.wrap(uint3.toBytes()).getInt());

		System.out.println("uint2.equals(uint1) = " + uint2.equals(uint1));

		try {
			System.out.println("uint2.add(uint1) = " +uint2.add(uint1));
		} catch (Exception err) {
			System.out.println(err.getMessage());
		}

		try {
			System.out.println("uint3.intValue() = " +uint3.intValue());
		} catch (Exception err) {
			System.out.println(err.getMessage());
		}
		// System.out.println(uint2+uint1);
		try {
			System.out.println(uint2.divide(uint1));
		} catch (Exception err) {
			System.out.println(err.getMessage());
		}
		try {
			System.out.println(uint1.multiply(uint3));
		} catch (Exception err) {
			System.out.println(err.getMessage());
		}
		try {
			System.out.println(uint1.multiply(10000));
		} catch (Exception err) {
			System.out.println(err.getMessage());
		}
		try {
			System.out.println(uint1.subtract(uint3));
		} catch (Exception err) {
			System.out.println(err.getMessage());
		}

		try {
			System.out.println(uint3.subtract(uint1));
		} catch (Exception err) {
			System.out.println(err.getMessage());
		}
		try {
			System.out.println(uint3.add(uint1));
		} catch (Exception err) {
			System.out.println(err.getMessage());
		}
		try {
			System.out.println(uint3.add(-1000));
		} catch (Exception err) {
			System.out.println(err.getMessage());
		}

		try {
			System.out.println(Integer.toUnsignedString(-11231));
		} catch (Exception err) {
			System.out.println(err.getMessage());
		}
	}
}
