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

import static com.github.optran.utils.disk.heap.DiskHeapHeader.HEADER_LENGTH;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.github.optran.utils.exceptions.CorruptDataSetException;
import com.github.optran.utils.exceptions.NotEnoughMemoryException;
import com.github.optran.utils.exceptions.RuntimeIOException;
import com.github.optran.utils.pagedfile.BufferedRandomAccessFile;
import com.github.optran.utils.pagedfile.CachedPagedFile;
import com.github.optran.utils.pagedfile.StandardPagedFile;

public class DiskHeap implements Heap {
	public static final long UNALLOCATED = 0;
	private int pageSize;
	private int cacheSize;
	private File targetFile;
	private BufferedRandomAccessFile braf;
	private DiskHeapHeader header;

	private DiskHeap(int pageSize, int cacheSize, File targetFile) {
		this.pageSize = pageSize;
		this.cacheSize = cacheSize;
		this.targetFile = targetFile;
		if (!targetFile.exists()) {
			header = new DiskHeapHeader();
		} else if (0 == targetFile.length()) {
			header = new DiskHeapHeader();
		} else {
			initiateHeader();
		}
		braf = new BufferedRandomAccessFile(
				new CachedPagedFile(new StandardPagedFile(targetFile, pageSize), cacheSize));
	}

	private void initiateHeader() {
		try {
			byte[] headerData = null;
			long targetLen = targetFile.length();
			long nextAlloc = 0;
			if (targetLen > HEADER_LENGTH) {
				headerData = new byte[HEADER_LENGTH];
				nextAlloc = targetLen;
			} else {
				headerData = new byte[(int) targetLen];
				nextAlloc = HEADER_LENGTH;
			}
			InputStream is = new BufferedInputStream(new FileInputStream(targetFile));
			is.read(headerData);
			is.close();
			header = new DiskHeapHeader(headerData, nextAlloc);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	private AllocHeader createAllocHeader(int size) {
		AllocHeader allocHeader = new AllocHeader();
		allocHeader.setAllocated(true);
		allocHeader.setSize(size);
		return allocHeader;
	}

	private AllocHeader readHeader(long location) {
		AllocHeader allocHeader = new AllocHeader();
		byte[] allocHeaderArr = new byte[4];
		braf.setHead(location);
		braf.read(allocHeaderArr);
		allocHeader.setBytes(allocHeaderArr);
		return allocHeader;
	}

	private void writeHeader(long location, AllocHeader allocHeader) {
		braf.setHead(location);
		braf.write(allocHeader.toBytes());
	}

	@Override
	public long malloc(int size) {
		long reference = UNALLOCATED;
		int freeListIndex = header.calculateFreeListIndex(size);
		if (freeListIndex < 0) {
			return reference;
		}
		int blockSize = header.calculateFreeListBlockSize(freeListIndex);
		reference = allocFromFreeList(size);
		long referenceHeader = 0;

		if (UNALLOCATED == reference) {
			referenceHeader = header.getNextAlloc();
			header.setNextAlloc(referenceHeader + blockSize);
			reference = referenceHeader + 4;
		} else {
			referenceHeader = reference - 4;
		}

		//Allocate the new block
		AllocHeader allocHeader = createAllocHeader(size);
		writeHeader(referenceHeader, allocHeader);
		writeHeader(referenceHeader+blockSize-4, allocHeader);

		return reference;
	}

	private long allocFromFreeList(int size) {
		int freeListIndex = header.calculateFreeListIndex(size);
		int blockSize = header.calculateFreeListBlockSize(freeListIndex);

		long freeListRef = header.getFreeListHeadRef(freeListIndex);
		if(UNALLOCATED==freeListRef) {
			return UNALLOCATED;
		}

		final long reference = freeListRef;
		byte[]data = new byte[blockSize];
		braf.setHead(freeListRef-4);
		braf.read(data);
		FreeListBlock freeListHead = new FreeListBlock(data);
		
		freeListRef = freeListHead.getNextNode();
		if(UNALLOCATED==freeListRef) {
			header.setFreeListHeadRef(freeListIndex, UNALLOCATED);
		} else {
			braf.setHead(freeListRef-4);
			braf.read(data);
			FreeListBlock newFreeListHead = new FreeListBlock(data);
			newFreeListHead.setPreviousNode(0);
			braf.setHead(freeListRef-4);
			braf.write(newFreeListHead.toBytes());
			header.setFreeListHeadRef(freeListIndex, freeListRef);
		}

		return reference;
	}

	@Override
	public long realloc(long reference, int size) {
		return 0;
	}

	@Override
	public void free(long reference) {
		AllocHeader allocHeader = readHeader(reference-4);
		int size = allocHeader.getSize();

		int freeListIndex = header.calculateFreeListIndex(size);
		int blockSize = header.calculateFreeListBlockSize(freeListIndex);
		
		FreeListBlock freedBlock = new FreeListBlock(blockSize);
		
		long freeListRef = header.getFreeListHeadRef(freeListIndex);
		if(0!=freeListRef) {
			byte[]data = new byte[blockSize];
			braf.setHead(freeListRef-4);
			braf.read(data);

			FreeListBlock freeListHead = new FreeListBlock(data);
			freeListHead.setPreviousNode(reference);
			freedBlock.setNextNode(freeListRef);
			braf.setHead(freeListRef-4);
			braf.write(freeListHead.toBytes());
		}
		freeListRef = reference;
		header.setFreeListHeadRef(freeListIndex, freeListRef);
		braf.setHead(reference-4);
		braf.write(freedBlock.toBytes());
	}

	@Override
	public int sizeOf(long reference) {
		int size = readHeader(reference-4).getSize();
		if(0>header.calculateFreeListIndex(size)) {
			throw new CorruptDataSetException("Unable to determine the size of the reference.");
		}
		return size;
	}

	@Override
	public byte[] read(long reference) {
		byte[]data = new byte[sizeOf(reference)];
		braf.setHead(reference);
		braf.read(data);
		return data;
	}

	@Override
	public void save(long reference, byte[] data) {
		int size = sizeOf(reference);
		if(data.length>size) {
			throw new NotEnoughMemoryException("Provided data is larger than the space allocated for this pointer.");
		}
		braf.setHead(reference);
		braf.write(data);
	}

	public void flush() {
		braf.setHead(0);
		braf.write(header.getHeader());
		braf.flush();
	}

	public void close() {
		flush();
		braf.close();
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

		public OptranHeapBuilder targetFile(File targetFile) {
			this.targetFile = targetFile;
			return this;
		}

		public DiskHeap build() {
			if (null == targetFile) {
				try {
					targetFile = File.createTempFile("OPTRAN_HEAP_", ".heap");
					targetFile.deleteOnExit();
				} catch (IOException e) {
					throw new RuntimeIOException(e);
				}
			}
			return new DiskHeap(pageSize, cacheSize, targetFile);
		}
	}
}
