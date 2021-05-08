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
package com.github.optran.utils.disk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.apache.log4j.Logger;

import com.github.optran.utils.cache.LRUCache;
import com.github.optran.utils.disk.model.OptranBlock;
import com.github.optran.utils.disk.model.OptranFileHeader;
import com.github.optran.utils.disk.model.OptranFilePage;
import com.github.optran.utils.exceptions.RuntimeIOException;
import com.github.optran.utils.pagedfile.Page;

public class OptranFile {
	private static final Logger logger = Logger.getLogger(OptranFile.class);
	private static final int ROOT_PAGE_OFFSET = 100;
	private OptranFileHeader header;
	private OptranPagedFile pagedFile;
	private LRUCache<Long, OptranFilePage> cache;
	private CacheHandler cacheHandler;
	private final int rawPageSize;

	public OptranFile(String fileName, int preferredPageSize, int cacheSize) {
		this(new File(fileName), preferredPageSize, cacheSize);
	}

	public OptranFile(File file, int preferredPageSize, int cacheSize) {
		if (null == file) {
			throw new NullPointerException("Null file provided to OptranDisk!");
		}
		if (!file.exists()) {
			header = new OptranFileHeader(preferredPageSize);
		} else if (!file.isFile()) {
			throw new RuntimeIOException("The file provided to OptranDisk is actually directory!");
		} else {
			try {
				byte[] headerArr = new byte[100];
				InputStream is = new FileInputStream(file);
				is.read(headerArr);
				is.close();
				header = new OptranFileHeader(headerArr);
			} catch (IOException e) {
				throw new RuntimeIOException("The file provided to OptranDisk is actually directory!", e);
			}
		}
		rawPageSize = header.getPageSize();
		pagedFile = new OptranPagedFile(file, rawPageSize);
		cacheHandler = new CacheHandler(pagedFile);
		cache = new LRUCache<Long, OptranFilePage>(cacheSize);
		cache.setCacheLoader(cacheHandler);
		cache.addEvictionListener(cacheHandler);
	}

	/**
	 * @return the rawPageSize
	 */
	public int getRawPageSize() {
		return rawPageSize;
	}

	public OptranFilePage malloc() {
		long freeList = header.getFreeList();
		if (freeList != 0) {
			OptranFilePage page = readPage(freeList);
			header.setFreeList(page.getNextPage());
			page.setPageType(1);
			page.setLength(0);
			page.setHead(0);
			return page;
		}

		long nextAddress = header.getLastAllocatedPage() + 1;
		header.setLastAllocatedPage(nextAddress);
		OptranFilePage page = cache.get(nextAddress).get();
		page.setPageType(1);
		page.setLength(0);
		page.setHead(0);
		return page;
	}

	public boolean free(OptranFilePage page) {
		long pageId = page.getPageId();
		page = readPage(pageId);
		if (null == page) {
			return false;
		}
		if (0 == page.getPageId()) {
			return false;
		}

		page.setPageType(0);
		page.setLength(0);
		page.setHead(0);

		long freeList = header.getFreeList();
		header.setFreeList(pageId);
		page.setNextPage(freeList);
		writePage(page);

		return true;
	}

	/**
	 * Reads and returns {@link Page} at the specified index.
	 * 
	 * @param pageNumber The index of the {@link Page} to be fetched.
	 * @return The {@link Page} at the specified index.
	 * @throws IOException If an IO exception is thrown when interacting with the
	 *                     file layer.
	 */
	public OptranFilePage readPage(long pageNumber) {
		long lastAllocatedPage = header.getLastAllocatedPage();
		if (pageNumber == 0) {
			OptranFilePage page = cache.get(pageNumber).get();
			page.setHead(0);
			page.setPageType(1);
			return page;
		}
		if (pageNumber > lastAllocatedPage) {
			return null;
		}
		OptranFilePage page = cache.get(pageNumber).get();
		if (page.getPageType() == 0) {
			page = null;
		}
		page.setHead(0);
		page.setPageType(1);
		return page;
	}

	/**
	 * Writes the {@link Page} that has been provided.
	 * 
	 * @param page The {@link Page} to be written.
	 * @throws IOException If an IO exception is thrown when interacting with the
	 *                     file layer.
	 */
	public void writePage(OptranFilePage page) {
		cache.put(page.getPageId(), page);
	}

	/**
	 * Reads and returns {@link Page} at the specified index.
	 * 
	 * @param pageNumber The index of the {@link Page} to be fetched.
	 * @return The {@link Page} at the specified index.
	 * @throws IOException If an IO exception is thrown when interacting with the
	 *                     file layer.
	 */
	public OptranBlock mallocBlock() {
		return new OptranBlock(this);
	}

	public void freeBlock(OptranBlock block) {
		long blockId = block.getPageId();
		while (0 != blockId) {
			OptranFilePage page = readPage(blockId);
			blockId = page.getNextPage();
			free(page);
		}
	}

	/**
	 * Reads and returns {@link Page} at the specified index.
	 * 
	 * @param pageNumber The index of the {@link Page} to be fetched.
	 * @return The {@link Page} at the specified index.
	 * @throws IOException If an IO exception is thrown when interacting with the
	 *                     file layer.
	 */
	public OptranBlock readBlock(long pageNumber) {
		return new OptranBlock(pageNumber, this);
	}

	/**
	 * Writes the {@link Page} that has been provided.
	 * 
	 * @param page The {@link Page} to be written.
	 * @throws IOException If an IO exception is thrown when interacting with the
	 *                     file layer.
	 */
	public void writeBlock(OptranFilePage page) {
		cache.put(page.getPageId(), page);
	}

	public void flush() {
		if (header.isDirty()) {
			pagedFile.writeHeader(header.getHeader());
			header.clean();
		}
		cache.perform(cacheHandler);
	}

	public void close() {
		flush();
		pagedFile.close();
	}

	public boolean delete() {
		return pagedFile.delete();
	}
}
