/*
 * MIT License
 * 
 * Copyright (c) 2021 Ashutosh Wad
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
*/
package com.github.optran.utils;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public final class PrimitiveSerializationUtils {
	/**
	 * Converts the long value supplied to a byte array.
	 * 
	 * @param val The long value to be encoded.
	 * @return The long value encoded to a byte[]
	 */
	public static final byte[] longToByteArr(long val) {
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.putLong(val);
		return buffer.array();
	}

	/**
	 * Converts the given array of bytes to a long.
	 * <ul>
	 * <li>If a null is provided in the input, this method returns a 0.</li>
	 * <li>If a byte[] with length less than 8 bytes is provided, zero byte values
	 * are left padded until total length is 8</li>
	 * <li>If a byte[] with length greater than 8 bytes is provided, only the last 8
	 * bytes are considered for conversion.</li>
	 * </ul>
	 * 
	 * @param data The byte array to be converted.
	 * @return The long value that best represents the byte[] provided.
	 */
	public static final long byteArrToLong(byte[] data) {
		if (null == data) {
			return 0;
		}
		if (data.length > 8) {
			byte[] temp = new byte[8];
			int tempIndex = 0;
			ByteArrayOutputStream baos = new ByteArrayOutputStream(8);
			for (int i = data.length - 8; i < data.length; i++) {
				temp[tempIndex++] = data[i];
			}
			data = temp;
		}
		if (data.length < 8) {
			byte[] temp = new byte[8];
			int zeroLen = temp.length - data.length;
			for (int i = 0; i < temp.length; i++) {
				if (i < zeroLen) {
					temp[i] = 0;
				} else {
					temp[i] = data[i - zeroLen];
				}
			}
			data = temp;
		}
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.put(data);
		buffer.flip();// need flip
		return buffer.getLong();
	}

	/**
	 * Converts the integer value supplied to a byte array.
	 * 
	 * @param val The integer value to be encoded.
	 * @return The integer value encoded to a byte[]
	 */
	public static final byte[] intToByteArr(int val) {
		ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
		buffer.putInt(val);
		return buffer.array();
	}

	/**
	 * Converts the given array of bytes to a integer.
	 * <ul>
	 * <li>If a null is provided in the input, this method returns a 0.</li>
	 * <li>If a byte[] with length less than 4 bytes is provided, zero byte values
	 * are left padded until total length is 4</li>
	 * <li>If a byte[] with length greater than 4 bytes is provided, only the last 4
	 * bytes are considered for conversion.</li>
	 * </ul>
	 * 
	 * @param data The byte array to be converted.
	 * @return The integer value that best represents the byte[] provided.
	 */
	public static final int byteArrToInt(byte[] data) {
		if (null == data) {
			return 0;
		}
		if (data.length > 4) {
			byte[] temp = new byte[4];
			int tempIndex = 0;
			ByteArrayOutputStream baos = new ByteArrayOutputStream(4);
			for (int i = data.length - 4; i < data.length; i++) {
				temp[tempIndex++] = data[i];
			}
			data = temp;
		}
		if (data.length < 4) {
			byte[] temp = new byte[4];
			int zeroLen = temp.length - data.length;
			for (int i = 0; i < temp.length; i++) {
				if (i < zeroLen) {
					temp[i] = 0;
				} else {
					temp[i] = data[i - zeroLen];
				}
			}
			data = temp;
		}
		ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
		buffer.put(data);
		buffer.flip();// need flip
		return buffer.getInt();
	}

	/**
	 * Returns true if the leftmost bit of the byte that has been provided is set to
	 * 1.
	 * 
	 * @param b The byte value to be checked.
	 * @return True if the leftmost bit of the byte that has been provided is set to
	 *         1 false otherwise.
	 */
	public static final boolean isFirstBitSet(byte b) {
		return 1 == ((b & 0x80) >> 7);
	}
}
