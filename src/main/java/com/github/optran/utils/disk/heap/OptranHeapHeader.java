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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.github.optran.utils.exceptions.CorruptHeapException;
import com.github.optran.utils.exceptions.RuntimeIOException;

/**
 * This is the header present in all the OptranHeap files. It's presence is
 * mandatory else the file is considered corrupt.
 * 
 * The header always begins with an identifier string OptranHeap terminated by a
 * newline. It then contains 24 freelist pointers 8 bytes each to freelist
 * nodes. The first of the freelists can contains nodes of 32 bytes and each
 * subsequent list can contain nodes double the size of the previous. It should
 * be noted that when a node is allocated, it's number of usable bytes are the
 * whatever the list can contain -4 as a 4 byte header is needed by the system.
 * 
 * @author Ashutosh Wad
 *
 */
public class OptranHeapHeader {
	private static final String FILE_HEADER = "OptranHeap\n";
	private final byte[] header;
	private final long[] freeList;
	private long nextAlloc;
	private static final int[] freeSize = new int[] { 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768,
			65536, 131072, 262144, 524288, 1048576, 2097152, 4194304, 8388608, 16777216, 33554432, 67108864, 134217728,
			268435456 };

	public OptranHeapHeader() {
		header = new byte[100];
		freeList = new long[24];
		saveHeader();
		nextAlloc = 100;
	}

	public OptranHeapHeader(byte[] header, long nextAlloc) throws IOException {
		this.header = header;
		if (100 != header.length) {
			throw new CorruptHeapException("Header too short.");
		}
		byte[] headerString = new byte[FILE_HEADER.length()];
		for (int i = 0; i < headerString.length; i++) {
			headerString[i] = header[i];
		}
		String headerStr = new String(headerString);
		if (!FILE_HEADER.equals(headerStr)) {
			throw new CorruptHeapException("Incorrect header magic: " + headerStr);
		}
		freeList = new long[24];
		loadHeader();
		this.nextAlloc = nextAlloc;
	}

	public byte[] getHeader() {
		saveHeader();
		byte[] retVal = new byte[100];
		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = header[i];
		}
		return retVal;
	}

	/**
	 * This method gets the next reference where memory can be allocated from if the
	 * freelist is empty.
	 * 
	 * @return the nextAlloc
	 */
	public long getNextAlloc() {
		return nextAlloc;
	}

	/**
	 * This method sets the next reference where memory can be allocated from if the
	 * freelist is empty.
	 * 
	 * @param nextAlloc the location where unallocated memory begins.
	 */
	public void setNextAlloc(long nextAlloc) {
		this.nextAlloc = nextAlloc;
	}

	/**
	 * Retrieves the reference to the head of the freelist at index listIndex. The
	 * reference will be 0 if the list is empty.
	 * 
	 * @param listIndex Index of the free list.
	 * @return returns the reference to the head of the freelist at index listIndex.
	 */
	public long getFreeListHeadRef(int listIndex) {
		return freeList[listIndex];
	}

	/**
	 * Sets the reference provided to the free list at index listIndex. The
	 * reference will be 0 if the list is empty.
	 * 
	 * @param listIndex Index of the free list.
	 * @param reference Reference to be set.
	 */
	public void setFreeListHeadRef(int listIndex, long reference) {
		freeList[listIndex] = reference;
	}

	/**
	 * Returns the total space on disk occupied by any block present in the free
	 * list with the index provided.
	 * 
	 * It should be noted that this size is the size on disk, and actual usable
	 * space is the size returned - 4.
	 * 
	 * @param freeListIndex
	 * @return
	 */
	public int calculateFreeListBlockSize(int freeListIndex) {
		return freeSize[freeListIndex];
	}

	/**
	 * This method calculates the index of the freelist that could contain a free
	 * block large enough to accomodate the memory requested.
	 * 
	 * @param size The amount of memory requested.
	 * @return The index of the freelist that could contain a free block large
	 *         enough to accomodate the memory requested.
	 */
	public int calculateFreeListIndex(int size) {
		int retVal = -1;
		if (size > 0 && size <= 28) {
			retVal = 0;
		} else if (size > 28 && size <= 60) {
			retVal = 1;
		} else if (size > 60 && size <= 124) {
			retVal = 2;
		} else if (size > 124 && size <= 252) {
			retVal = 3;
		} else if (size > 252 && size <= 508) {
			retVal = 4;
		} else if (size > 508 && size <= 1020) {
			retVal = 5;
		} else if (size > 1020 && size <= 2044) {
			retVal = 6;
		} else if (size > 2044 && size <= 4092) {
			retVal = 7;
		} else if (size > 4092 && size <= 8188) {
			retVal = 8;
		} else if (size > 8188 && size <= 16380) {
			retVal = 9;
		} else if (size > 16380 && size <= 32764) {
			retVal = 10;
		} else if (size > 32764 && size <= 65532) {
			retVal = 11;
		} else if (size > 65532 && size <= 131068) {
			retVal = 12;
		} else if (size > 131068 && size <= 262140) {
			retVal = 13;
		} else if (size > 262140 && size <= 524284) {
			retVal = 14;
		} else if (size > 524284 && size <= 1048572) {
			retVal = 15;
		} else if (size > 1048572 && size <= 2097148) {
			retVal = 16;
		} else if (size > 2097148 && size <= 4194300) {
			retVal = 17;
		} else if (size > 4194300 && size <= 8388604) {
			retVal = 18;
		} else if (size > 8388604 && size <= 16777212) {
			retVal = 19;
		} else if (size > 16777212 && size <= 33554428) {
			retVal = 20;
		} else if (size > 33554428 && size <= 67108860) {
			retVal = 21;
		} else if (size > 67108860 && size <= 134217724) {
			retVal = 22;
		} else if (size > 134217724 && size <= 268435452) {
			retVal = 23;
		}
		return retVal;
	}

	/**
	 * This helper function loads the internal variables from the header array.
	 */
	private void loadHeader() {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(header);
			ObjectInputStream ois = new ObjectInputStream(bais);
			for (int i = 0; i < freeList.length; i++) {
				freeList[i] = ois.readLong();
			}
			ois.close();
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	/**
	 * This helper function saves the internal variables to the header array.
	 */
	private void saveHeader() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			baos.write(FILE_HEADER.getBytes());
			for (int i = 0; i < freeList.length; i++) {
				oos.writeLong(freeList[i]);
			}
			oos.close();
			byte[] data = baos.toByteArray();
			for (int i = 0; i < data.length; i++) {
				header[i] = data[i];
			}
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}
}
