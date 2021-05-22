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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;

import com.github.optran.utils.exceptions.RuntimeIOException;

public class StandardPagedFile implements PagedFile {
	private static final Logger logger = Logger.getLogger(StandardPagedFile.class);
	private File target;
	private RandomAccessFile raf;
	private byte[] pageData;

	public StandardPagedFile(File target, int pageSize) {
		try {
			String errorMessage = validateFile(target);
			if (null != errorMessage) {
				throw new RuntimeIOException(errorMessage);
			}
			if (0 >= pageSize) {
				throw new RuntimeIOException("The page size provided is too small. pageSize = " + pageSize);
			}
			pageData = new byte[pageSize];
			this.target = target;
			raf = new RandomAccessFile(target, "rw");
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
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

	private void initRaf() {
		try {
			if (null != raf) {
				return;
			}
			raf = new RandomAccessFile(target, "rw");
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IOException If an IO exception is thrown when interacting with the
	 *                     file layer.
	 */
	@Override
	public Page readPage(long pageNumber) {
		try {
			initRaf();
			for (int i = 0; i < pageData.length; i++) {
				pageData[i] = 0;
			}
			raf.seek(pageNumber * pageData.length);
			raf.read(pageData);
			Page page = new StandardPage(pageNumber, pageData);
			return page;
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IOException If an IO exception is thrown when interacting with the
	 *                     file layer.
	 */
	@Override
	public void writePage(Page page) {
		try {
			initRaf();
			raf.seek(page.getPageId() * pageData.length);
			raf.write(page.getData());
			page.clean();
			logger.trace("Wrote page (" + page.getPageId() + ") to disk.");
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getPageSize() {
		return pageData.length;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void flush() {
		try {
			raf.getFD().sync();
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean exists() {
		return target.exists();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean close() {
		try {
			raf.close();
			raf = null;
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long length() {
		long length = -1;
		try {
			initRaf();
			length = raf.length();
		} catch (IOException e) {
			logger.error("Unable to retrieve length. ", e);
		}
		return length;
	}
}
