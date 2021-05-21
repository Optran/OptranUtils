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
package com.github.optran.utils.disk.heap;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.github.optran.utils.exceptions.RuntimeIOException;
import com.github.optran.utils.pagedfile.BufferedRandomAccessFile;
import com.github.optran.utils.pagedfile.CachedPagedFile;
import com.github.optran.utils.pagedfile.StandardPagedFile;

public class OptranHeap {
	public static final String OPTRAN_HEAP_PAGE_SIZE = "optran.heap.page.size";
	public static final String OPTRAN_HEAP_CACHE_SIZE = "optran.heap.cache.size";
	private int pageSize;
	private int cacheSize;
	private File targetFile;
	private BufferedRandomAccessFile braf;
	private OptranHeapHeader header;

	private OptranHeap(int pageSize, int cacheSize, File targetFile) {
		this.pageSize = pageSize;
		this.cacheSize = cacheSize;
		this.targetFile = targetFile;
		if(!targetFile.exists()) {
			header = new OptranHeapHeader();
		} else if(0==targetFile.length()) {
			header = new OptranHeapHeader();
		} else {
			initiateHeader();
		}
		try {
			braf = new BufferedRandomAccessFile(new CachedPagedFile(new StandardPagedFile(targetFile, pageSize), cacheSize));
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	private void initiateHeader() {
		try {
			byte[]headerData = null;
			long targetLen = targetFile.length();
			long nextAlloc = targetLen;
			if(targetLen>100) {
				headerData = new byte[100];
			} else {
				headerData = new byte[(int)targetLen];
				nextAlloc = 100;
			}
			InputStream is = new BufferedInputStream(new FileInputStream(targetFile));
			is.read(headerData);
			header = new OptranHeapHeader(headerData, nextAlloc);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	public MemoryReference malloc(int size) {
		long ref = header.getNextAlloc();
		int freeListIndex = header.calculateFreeListIndex(size);
		if(freeListIndex<0) {
			return null;
		}
		int blockSize = header.calculateFreeListBlockSize(freeListIndex);
		header.setNextAlloc(ref + blockSize);
		return null;
	}
	
	public MemoryReference realloc(MemoryReference reference, int size) {
		return null;
	}
	
	public void free(MemoryReference reference) {
	}

	public void save(MemoryReference reference) {
		
	}

	public static OptranHeapBuilder builder() {
		return new OptranHeapBuilder();
	}

	public static class OptranHeapBuilder {
		private int pageSize;
		private int cacheSize;
		private File targetFile;

		public OptranHeapBuilder() {
			pageSize = 512;
			cacheSize = 32;
			targetFile = null;
		}

		public OptranHeapBuilder pageSize(int pageSize) {
			if (pageSize > 512) {
				this.pageSize = pageSize;
			}
			return this;
		}

		public OptranHeapBuilder cacheSize(int cacheSize) {
			if (cacheSize > 0) {
				this.cacheSize = cacheSize;
			}
			return this;
		}

		public OptranHeapBuilder cacheSize(File targetFile) {
			this.targetFile = targetFile;
			return this;
		}

		public OptranHeap build() {
			if(null==targetFile) {
				try {
					targetFile = File.createTempFile("OPTRAN_HEAP_", ".heap");
					targetFile.deleteOnExit();
				} catch (IOException e) {
					throw new RuntimeIOException(e);
				}
			}
			return new OptranHeap(pageSize, cacheSize, targetFile);
		}
	}
}
