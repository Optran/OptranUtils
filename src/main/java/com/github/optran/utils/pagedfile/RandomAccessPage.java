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
package com.github.optran.utils.pagedfile;

import java.io.IOException;

public class RandomAccessPage implements Page {
	private long pageId;
	private int size;
	private byte[] pageData;
	private int head;
	private boolean dirty;
	private PagedFile pagedFile;

	public RandomAccessPage(long pageId, byte[] pageData, PagedFile pagedFile) {
		this.pageId = pageId;
		this.size = pageData.length;
		this.pageData = new byte[pageData.length];
		for (int i = 0; i < pageData.length; i++) {
			this.pageData[i] = pageData[i];
		}
		this.pagedFile = pagedFile;
		this.head = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getPageId() {
		return pageId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		return size;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clean() {
		dirty = false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean flush() {
		if (!dirty) {
			return true;
		}
		try {
			pagedFile.writePage(this);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] getData() {
		byte[] data = new byte[size];
		for (int i = 0; i < data.length; i++) {
			data[i] = pageData[i];
		}
		return data;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getHead() {
		return head;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setHead(int head) {
		if (head < 0) {
			head = 0;
		} else if (head > size) {
			head = size;
		} else {
			this.head = head;
		}
	}

	private boolean isHeadValid(int head) {
		if (head < 0) {
			return false;
		} else if (head >= size) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read() {
		if (isHeadValid(head)) {
			return pageData[head++];
		} else {
			return -1;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(byte[] value) {
		int numBytesRead = 0;
		if (null == value) {
			return numBytesRead;
		}
		int readValue = 0;
		for (int i = 0; i < value.length; i++) {
			readValue = read();
			if(readValue >= 0) {
				value[i] = (byte) readValue;
				numBytesRead++;
			} else {
				break;
			}
		}
		return numBytesRead;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean write(int value) {
		if (!isHeadValid(head)) {
			return false;
		}
		pageData[head++] = (byte) value;
		dirty = true;
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int write(byte[] value) {
		int bytesWritten = 0;
		if (null == value) {
			return bytesWritten;
		}
		boolean writeFlag = false;
		for (int i = 0; i < value.length; i++) {
			writeFlag = write(value[i] & 0xFF);
			if (writeFlag) {
				bytesWritten++;
			} else {
				break;
			}
		}
		return bytesWritten;
	}
}
