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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * This class allows the use of the {@link PagedFile} just like the
 * {@link RandomAccessFile} class with better efficiency due to page caching.
 * 
 * @author Ashutosh Wad
 *
 */
public class BufferedRandomAccessFile {
	private long head;
	private int pageSize;
	private PagedFile pagedFile;

	public BufferedRandomAccessFile(PagedFile pagedFile) {
		head = 0;
		this.pagedFile = pagedFile;
		pageSize = pagedFile.getPageSize();
	}

	public BufferedRandomAccessFile(File target, int pageSize) throws IOException {
		head = 0;
		this.pageSize = pageSize;
		pagedFile = new StandardPagedFile(target, pageSize);
	}

	public BufferedRandomAccessFile(File target, int pageSize, int cacheSize) throws IOException {
		head = 0;
		this.pageSize = pageSize;
		pagedFile = new CachedPagedFile(new StandardPagedFile(target, pageSize), cacheSize);
	}

	public long getHead() {
		return head;
	}

	public void setHead(long head) {
		this.head = head;
	}

	public int read() throws IOException {
		Page page = pagedFile.readPage(head / pageSize);
		page.setHead((int) (head % pageSize));
		int retVal = page.read();
		head++;
		return retVal;
	}

	public int read(byte[] data) throws IOException {
		int bytesRead = 0;
		if (null == data) {
			return bytesRead;
		}
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) read();
			bytesRead++;
		}
		return bytesRead;
	}

	public void write(int value) throws IOException {
		Page page = pagedFile.readPage(head / pageSize);
		page.setHead((int) (head % pageSize));
		page.write(value);
		head++;
		pagedFile.writePage(page);
	}

	public int write(byte[] data) throws IOException {
		int bytesWritten = 0;
		if (null == data) {
			return bytesWritten;
		}
		for (int i = 0; i < data.length; i++) {
			write(data[i] & 0xFF);
			bytesWritten++;
		}
		return bytesWritten;
	}

	public boolean exists() {
		return pagedFile.exists();
	}

	public void flush() throws IOException {
		pagedFile.flush();
	}

	public void close() {
		pagedFile.close();
	}
}
