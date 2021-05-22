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

import java.io.IOException;
import java.io.OutputStream;

public class LimitedByteArrayOutputStream extends OutputStream {
	private final byte[] data;
	private int index;

	public LimitedByteArrayOutputStream(int limit) {
		data = new byte[limit];
		index = 0;
	}

	public LimitedByteArrayOutputStream(byte[]data) {
		this.data = data;
		index = 0;
	}

	public void skip(int value) {
		index = index + value;
	}

	@Override
	public void write(int b) throws IOException {
		if (index >= data.length) {
			throw new IOException("Memory usage exeeded. Max limit was " + data.length);
		}
		data[index++] = (byte)(b&0xFF);
	}

	public byte[] toByteArray() {
		return data;
	}
}
