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

public class RandomAccessPagedFile implements PagedFile {
	private RandomAccessFile raf;
	private byte[] pageData;

	public RandomAccessPagedFile(File target, int pageSize) throws IOException {
		String errorMessage = validateFile(target);
		if (null != errorMessage) {
			throw new IOException(errorMessage);
		}
		if (0 >= pageSize) {
			throw new IOException("The page size provided is too small. pageSize = " + pageSize);
		}
		pageData = new byte[pageSize];
		raf = new RandomAccessFile(target, "rws");
	}

	public static String validateFile(File target) {
		if (null == target) {
			return "The file reference that has been provided is null.";
		}
		if (!target.exists()) {
			return null;
		}
		if (target.isDirectory()) {
			return "The file reference that has been provided is a directory.";
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IOException
	 */
	@Override
	public Page readPage(long pageNumber) throws IOException {
		for (int i = 0; i < pageData.length; i++) {
			pageData[i] = 0;
		}
		raf.seek(pageNumber * pageData.length);
		raf.read(pageData);
		Page page = new RandomAccessPage(pageNumber, pageData, this);
		return page;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IOException
	 */
	@Override
	public void writePage(Page page) throws IOException {
		raf.seek(page.getPageId() * pageData.length);
		raf.write(page.getData());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean close() {
		if (null == raf) {
			return true;
		}
		try {
			raf.close();
			raf = null;
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}
