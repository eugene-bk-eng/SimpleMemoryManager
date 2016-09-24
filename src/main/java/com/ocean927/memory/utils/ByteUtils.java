/*
 * 
 */
package com.ocean927.memory.utils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.ocean927.memory.impl.MemoryManagerException;

/**
 * The Class ByteUtils.
 */
public class ByteUtils {

	/**
	 * prevent users instantiating this class.
	 */	
	private ByteUtils() {
		
	}
	/**
	 *  WRITE: INT TO NEW BYTE ARRAY.
	 *
	 * @param value the value
	 * @return the byte[]
	 */
	public static final byte[] intToByteArray(int value) {
		return new byte[] { (byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value };
	}

	/**
	 *  WRITE: INT TO PROVIDED BYTE ARRAY.
	 *
	 * @param value the value
	 * @param b the b
	 * @param offset the offset
	 */
	public static final void writeIntToByteArray(int value, byte b[], int offset) {
		b[offset + 0] = (byte) (value >>> 24);
		b[offset + 1] = (byte) (value >>> 16);
		b[offset + 2] = (byte) (value >>> 8);
		b[offset + 3] = (byte) value;
	}

	/**
	 *  method copies from Google com.google.common.primitives.Longs.
	 * WRITE: LONG TO PROVIDED BYTE ARRAY
	 *
	 * @param value the value
	 * @param array the array
	 * @param index the index
	 */
	public static final void writeLongToByteArray(long value, byte array[], int index) {
		for (int i = 7; i >= 0; i--) {
			array[index + i] = (byte) (value & 0xffL);
			value >>= 8;
		}
	}

	/**
	 *  method copies from Google com.google.common.primitives.Longs.
	 * WRITE: LONG TO PROVIDED BYTE ARRAY
	 *
	 * @param value the value
	 * @param array the array
	 * @param index the index
	 */
	public static final void writeLongToByteArray2(long value, byte array[], int index) {
		array[index + 7] = (byte) (value & 0xffL);
		value >>= 8;
		array[index + 6] = (byte) (value & 0xffL);
		value >>= 8;
		array[index + 5] = (byte) (value & 0xffL);
		value >>= 8;
		array[index + 4] = (byte) (value & 0xffL);
		value >>= 8;
		array[index + 3] = (byte) (value & 0xffL);
		value >>= 8;
		array[index + 2] = (byte) (value & 0xffL);
		value >>= 8;
		array[index + 1] = (byte) (value & 0xffL);
		value >>= 8;
		array[index] = (byte) (value & 0xffL);
	}

	/**
	 *  READ: INT FROM BYTE ARRAY.
	 *
	 * @param b the b
	 * @param offset the offset
	 * @return the int
	 */
	public static final int readIntFromByteArray(byte b[], int offset) {
		return b[offset + 0] << 24 | (b[offset + 1] & 0xFF) << 16 | (b[offset + 2] & 0xFF) << 8
				| (b[offset + 3] & 0xFF);
	}

	/**
	 *  READ: LONG FROM BYTE ARRAY.
	 *
	 * @param b the b
	 * @param offset the offset
	 * @return the long
	 */
	public static final long readLongFromByteArray(byte b[], int offset) {

		long result = (b[offset + 0] & 0xFF);
		result <<= 8;
		result |= (b[offset + 1] & 0xFF);
		result <<= 8;
		result |= (b[offset + 2] & 0xFF);
		result <<= 8;
		result |= (b[offset + 3] & 0xFF);
		result <<= 8;
		result |= (b[offset + 4] & 0xFF);
		result <<= 8;
		result |= (b[offset + 5] & 0xFF);
		result <<= 8;
		result |= (b[offset + 6] & 0xFF);
		result <<= 8;
		result |= (b[offset + 7] & 0xFF);

		return result;
	}

	/**
	 *  returns the logarithm of n or k. Think of n as 2^k, k=0...63
	 *
	 * @param n the n
	 * @return the int
	 * @throws Exception the exception
	 */
	public final static int log2_(long n) throws MemoryManagerException {
		if (!((n & (n - 1)) == 0)) {
			throw new MemoryManagerException(n + " not a power of 2");
		}
		
		if (n == 1) {
			return 0;
		} else if (n == 2) {
			return 1;
		} else if (n == 4) {
			return 2;
		} else if (n == 8) {
			return 3;
		} else if (n == 16) {
			return 4;
		} else if (n == 32) {
			return 5;
		} else if (n == 64) {
			return 6;
		} else if (n == 128) {
			return 7;
		} else if (n == 256) {
			return 8;
		} else if (n == 512) {
			return 9;
		} else if (n == 1024) {
			return 10;
		} else if (n == 2048) {
			return 11;
		} else if (n == 4096) {
			return 12;
		} else if (n == 8192) {
			return 13;
		} else if (n == 16384) {
			return 14;
		} else if (n == 32768) {
			return 15;
		} else if (n == 65536) {
			return 16;
		} else if (n == 131072) {
			return 17;
		} else if (n == 262144) {
			return 18;
		} else if (n == 524288) {
			return 19;
		} else if (n == 1048576) {
			return 20;
		} else if (n == 2097152) {
			return 21;
		} else if (n == 4194304) {
			return 22;
		} else if (n == 8388608) {
			return 23;
		} else if (n == 16777216) {
			return 24;
		} else if (n == 33554432) {
			return 25;
		} else if (n == 67108864) {
			return 26;
		} else if (n == 134217728) {
			return 27;
		} else if (n == 268435456) {
			return 28;
		} else if (n == 536870912) {
			return 29;
		} else if (n == 1073741824) {
			return 30;
		} else if (n == 2147483648l) {
			return 31;
		} else if (n > 16777216) {
			return (int) (Math.log(n) / Math.log(2));
		}
		return -1;
	}

	/**
	 * Log 2 v 2.
	 *
	 * @param n the n
	 * @return the int
	 */
	public final static int log2_v2(long n) {
		if (n < 65536) {
			return lookup[(int) n];
		} else if ((n & 16711680) > 0) { // 3rd byte set
			// n [2^16..2^23]
			if (n == 65536) {
				return 16;
			} else if (n == 131072) {
				return 17;
			} else if (n == 262144) {
				return 18;
			} else if (n == 524288) {
				return 19;
			} else if (n == 1048576) {
				return 20;
			} else if (n == 2097152) {
				return 21;
			} else if (n == 4194304) {
				return 22;
			} else if (n == 8388608) {
				return 23;
			}
		} else if ((n & 2130706432) > 0) { // 4th byte set, sign bit not
											// included
			// n [2^24..2^31]
			if (n == 16777216) {
				return 24;
			} else if (n == 33554432) {
				return 25;
			} else if (n == 67108864) {
				return 26;
			} else if (n == 134217728) {
				return 27;
			} else if (n == 268435456) {
				return 28;
			} else if (n == 536870912) {
				return 29;
			} else if (n == 1073741824) {
				return 30;
			} else if (n == 2147483648l) {
				return 31;
			}
		}
		return -1;
	}

	/**
	 * Uses following algorithm https://en.wikipedia.org/wiki/De_Bruijn_sequence
	 *
	 * @param value the value
	 * @return the long
	 * @throws Exception the exception
	 */

	public final static long log2_v3(int value) {
		// 0x077CB531 = 125613361
		long l = (long) (value * 125613361l);
		long shifted = l >> 27;
		// int bit_index = MultiplyDeBruijnBitPosition[ (int)shifted ];
		return shifted;
	}

	/**
	 * Convert char to byte.
	 *
	 * @param c the c
	 * @return the byte buffer
	 */
	public final static ByteBuffer convertCharToByte(char c[]) {
		ByteBuffer bb = Charset.forName("UTF-8").encode(CharBuffer.wrap(c));
		byte[] b = new byte[bb.remaining()];
		bb.get(b);
		return bb;
	}

	/**
	 * Convert byte to string.
	 *
	 * @param b the b
	 * @param offset the offset
	 * @param length the length
	 * @return the string
	 */
	public final static String convertByteToString(final byte b[], int offset, int length) {
		return new String(b, offset, length, StandardCharsets.UTF_8);
	}

	/** The Constant MultiplyDeBruijnBitPosition. */
	@SuppressWarnings(value = { "unused" })
	private final static int MultiplyDeBruijnBitPosition[] = new int[] { 0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25,
			17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9 };

	/** The Constant lookup. */
	protected static final int lookup[] = new int[65536];
	static {
		for (int i = 0; i <= 15; i++) {
			lookup[(int) Math.pow(2, i)] = i;
		}
	}

	/** The Constant bits_in_byte. */
	private static final int bits_in_byte[] = new int[8];
	static {
		for (int i = 0; i < bits_in_byte.length; i++) {
			bits_in_byte[i] = (int) Math.pow(2, i);
		}
	}

	/**
	 * Gets the class name.
	 *
	 * @return the class name
	 */
	/*
	 * stacktrace[1] - is class name here stacktrace[2] - is class name of
	 * caller, one level up.
	 */
	public final static String getClassName() {
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stacktrace[2];
		return e.getClassName();
	}

	/**
	 * Gets the method name.
	 *
	 * @return the method name
	 */
	public final static String getMethodName() {
		StackTraceElement st[] = Thread.currentThread().getStackTrace();
		String methodName = st[2].getMethodName();
		return methodName;
	}

	  
	  /**
  	 * Checks if is powerof 2.
  	 *
  	 * @param n the n
  	 * @return true, if is powerof 2
  	 */
  	public static boolean ispowerof2(int n){
		  return (n & (n - 1))==0 ? true : false;
	  }
	  
	  /**
  	 * Checks if is powerof 2 long.
  	 *
  	 * @param n the n
  	 * @return true, if is powerof 2 long
  	 */
  	public static boolean ispowerof2long(long n){
		  return (n & (n - 1))==0 ? true : false;
	  }
	  	  
}