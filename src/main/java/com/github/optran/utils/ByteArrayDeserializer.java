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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

import com.github.optran.utils.exceptions.RuntimeIOException;

public class ByteArrayDeserializer implements ObjectInput {
	private ObjectInputStream ois;

	public ByteArrayDeserializer(byte[] data) {
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(data));
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public boolean readBoolean() {
		try {
			return ois.readBoolean();
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public byte readByte() {
		try {
			return ois.readByte();
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public char readChar() {
		try {
			return ois.readChar();
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public double readDouble() {
		try {
			return ois.readDouble();
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public float readFloat() {
		try {
			return ois.readFloat();
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public void readFully(byte[]buffer) {
		try {
			ois.readFully(buffer);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public void readFully(byte[] buffer, int off, int len) {
		try {
			ois.readFully(buffer, off, len);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public int readInt() {
		try {
			return ois.readInt();
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public String readLine() {
		try {
			return ois.readLine();
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public long readLong() {
		try {
			return ois.readLong();
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public short readShort() {
		try {
			return ois.readShort();
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public String readUTF() {
		try {
			return ois.readUTF();
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public int readUnsignedByte() {
		return 0;
	}

	@Override
	public int readUnsignedShort() {
		return 0;
	}

	@Override
	public int skipBytes(int len) {
		try {
			return ois.skipBytes(len);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public int available() {
		try {
			return ois.available();
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public void close() {
		try {
			ois.close();
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public int read() {
		try {
			return ois.read();
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public int read(byte[] buffer) {
		try {
			return ois.read(buffer);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public int read(byte[] buffer, int off, int len) {
		try {
			return ois.read(buffer, off, len);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public Object readObject() {
		try {
			return ois.readObject();
		} catch (ClassNotFoundException e) {
			throw new RuntimeIOException(e);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public long skip(long n) {
		try {
			return ois.skip(n);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}
}
