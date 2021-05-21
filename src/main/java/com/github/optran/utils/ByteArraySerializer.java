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
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import com.github.optran.utils.exceptions.RuntimeIOException;

public class ByteArraySerializer implements ObjectOutput {
	public final ObjectOutputStream oos;
	private final ByteArrayOutputStream baos;
	private final LimitedByteArrayOutputStream lbaos;

	public ByteArraySerializer() {
		try {
			baos = new ByteArrayOutputStream();
			lbaos = null;
			oos = new ObjectOutputStream(baos);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	public ByteArraySerializer(int limit) {
		try {
			baos = null;
			lbaos = new LimitedByteArrayOutputStream(limit);
			oos = new ObjectOutputStream(lbaos);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	public ByteArraySerializer(byte[]data) {
		try {
			baos = null;
			lbaos = new LimitedByteArrayOutputStream(data);
			oos = new ObjectOutputStream(lbaos);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	public byte[] serialize() {
		if(baos!=null) {
			return baos.toByteArray();
		} else {
			return lbaos.toByteArray();
		}
	}

	@Override
	public void writeBoolean(boolean val) {
		try {
			oos.writeBoolean(val);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public void writeByte(int val) {
		try {
			oos.writeByte(val);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public void writeBytes(String str) {
		try {
			oos.writeBytes(str);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public void writeChar(int val) {
		try {
			oos.writeChar(val);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public void writeChars(String str) {
		try {
			oos.writeChars(str);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public void writeDouble(double val) {
		try {
			oos.writeDouble(val);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public void writeFloat(float val) {
		try {
			oos.writeFloat(val);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public void writeInt(int val) {
		try {
			oos.writeInt(val);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public void writeLong(long val) {
		try {
			oos.writeLong(val);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public void writeShort(int val) {
		try {
			oos.writeShort(val);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public void writeUTF(String str) {
		try {
			oos.writeUTF(str);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public void close() {
		try {
			oos.close();
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public void flush() {
		try {
			oos.flush();
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public void write(int val) {
		try {
			oos.write(val);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public void write(byte[] buf) {
		try {
			oos.write(buf);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public void write(byte[] buf, int off, int len) {
		try {
			oos.write(buf, off, len);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public void writeObject(Object obj) {
		try {
			oos.writeObject(obj);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}
}
