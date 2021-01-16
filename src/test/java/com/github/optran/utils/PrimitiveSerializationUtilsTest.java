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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PrimitiveSerializationUtilsTest {
	@Test
	public void longConversionTest() {
		long l1 = 65843543l;
		long l2 = PrimitiveSerializationUtils.byteArrToLong(PrimitiveSerializationUtils.longToByteArr(l1));
		assertEquals(l1, l2);
	}

	@Test
	public void longByteArrayTest() {
		byte[] data = new byte[2];
		data[0] = (byte) 0xFF;
		data[1] = (byte) 0x00;
		assertEquals(65280, PrimitiveSerializationUtils.byteArrToLong(data));

		data = new byte[9];
		data[7] = (byte) 0xFF;
		data[8] = (byte) 0x00;
		assertEquals(65280, PrimitiveSerializationUtils.byteArrToLong(data));

		assertEquals(0, PrimitiveSerializationUtils.byteArrToLong(null));
	}

	@Test
	public void intConversionTest() {
		int l1 = 658435;
		int l2 = PrimitiveSerializationUtils.byteArrToInt(PrimitiveSerializationUtils.intToByteArr(l1));
		assertEquals(l1, l2);
	}

	@Test
	public void intByteArrayTest() {
		byte[] data = new byte[2];
		data[0] = (byte) 0xFF;
		data[1] = (byte) 0x01;
		assertEquals(65281, PrimitiveSerializationUtils.byteArrToInt(data));

		data = new byte[5];
		data[3] = (byte) 0xFF;
		data[4] = (byte) 0x01;
		assertEquals(65281, PrimitiveSerializationUtils.byteArrToInt(data));

		assertEquals(0, PrimitiveSerializationUtils.byteArrToInt(null));
	}

	@Test
	public void isFirstBitSetTest() {
		byte b = 1;
		b = (byte)(b<<6);
		assertFalse(PrimitiveSerializationUtils.isFirstBitSet(b));
		b = (byte)(b<<1);
		assertTrue(PrimitiveSerializationUtils.isFirstBitSet(b));
	}
}
