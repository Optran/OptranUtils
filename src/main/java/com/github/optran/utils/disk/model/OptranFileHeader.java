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
package com.github.optran.utils.disk.model;

import com.github.optran.utils.PrimitiveSerializationUtils;
import com.github.optran.utils.disk.OptranFile;
import com.github.optran.utils.disk.exception.CorruptDiskException;

/**
 * This class is used to parse the header metadata of any {@link OptranFile}
 * file. Its structure is as follows:
 * 
 * <ol>
 * <li>11 bytes -> OptranDisk\n</li>
 * <li>4 bytes -> PageSize (max around 4 gb)</li>
 * <li>8 bytes -> start offset of freelist file. 0 if freelist is empty</li>
 * <li>8 bytes -> last allocated page</li>
 * <li>38 bytes -> reserved initialized to 0</li>
 * </ol>
 * 
 * @author Ashutosh Wad
 *
 */
public class OptranFileHeader {
	private static final String HEADER_DESCRIPTOR = "OptranDisk\n";
	private int pageSize;
	private long freeList;
	private long lastAllocatedPage;
	private boolean dirty;

	public OptranFileHeader() {
		this(512);
	}

	/**
	 * Default constructor which is used when creating an entirely new
	 * {@link OptranFile} file. pageSize is initialized to the pageSize provided,
	 * freeList and lastAllocatedPage are initialized to 0.
	 */
	public OptranFileHeader(int pageSize) {
		this.pageSize = pageSize;
		freeList = 0;
		lastAllocatedPage = 0;
		dirty = true;
	}

	/**
	 * If the OptranDisk file already exists on the disk, its header is loaded using
	 * this constructor.
	 * 
	 * @param header The first 100 bytes of a valid {@link OptranFile} file.
	 */
	public OptranFileHeader(byte[] header) {
		if (null == header) {
			throw new CorruptDiskException("Expected 100 bytes found null");
		}
		if (header.length < 100) {
			throw new CorruptDiskException("Expected 100 bytes found " + header.length);
		}
		loadHeader(header);
		dirty = false;
	}

	/**
	 * This method loads all the header metadata from the byte[] for use in code.
	 */
	private void loadHeader(byte[] header) {
		checkDescriptor(header);
		readPageSize(header);
		readFreeList(header);
		readLastAllocatedPage(header);
	}

	/**
	 * This method checks that the header starts with the correct descriptor.
	 * 
	 * @param header The first 100 bytes of a valid {@link OptranFile} file.
	 */
	private void checkDescriptor(byte[] header) {
		byte[] descriptor = new byte[HEADER_DESCRIPTOR.length()];
		for (int i = 0; i < descriptor.length; i++) {
			descriptor[i] = header[i];
		}
		if (!HEADER_DESCRIPTOR.equals(new String(descriptor))) {
			throw new CorruptDiskException("File header descriptor is corrupt");
		}
	}

	/**
	 * This method loads the pageSize from the header.
	 * 
	 * @param header The first 100 bytes of a valid {@link OptranFile} file.
	 */
	private void readPageSize(byte[] header) {
		byte[] data = new byte[4];
		int offset = HEADER_DESCRIPTOR.length();
		for (int i = 0; i < data.length; i++) {
			data[i] = header[offset + i];
		}
		pageSize = PrimitiveSerializationUtils.byteArrToInt(data);
	}

	/**
	 * This method loads the free list offset from the header.
	 * 
	 * @param header The first 100 bytes of a valid {@link OptranFile} file.
	 */
	private void readFreeList(byte[] header) {
		byte[] data = new byte[8];
		int offset = HEADER_DESCRIPTOR.length() + 4;
		for (int i = 0; i < data.length; i++) {
			data[i] = header[offset + i];
		}
		freeList = PrimitiveSerializationUtils.byteArrToInt(data);
	}

	/**
	 * This method loads the last allocated page from the header.
	 * 
	 * @param header The first 100 bytes of a valid {@link OptranFile} file.
	 */
	private void readLastAllocatedPage(byte[] header) {
		byte[] data = new byte[8];
		int offset = HEADER_DESCRIPTOR.length() + 4 + 8;
		for (int i = 0; i < data.length; i++) {
			data[i] = header[offset + i];
		}
		lastAllocatedPage = PrimitiveSerializationUtils.byteArrToInt(data);
	}

	/**
	 * This method serializes the header metadata to a 100 byte array that can be
	 * written to the head of an {@link OptranFile} file.
	 * 
	 * @return The first 100 bytes of a valid {@link OptranFile} file.
	 */
	public byte[] getHeader() {
		byte[] header = new byte[100];
		byte[] headerDescriptor = HEADER_DESCRIPTOR.getBytes();
		for (int i = 0; i < headerDescriptor.length; i++) {
			header[i] = headerDescriptor[i];
		}

		byte[] data = PrimitiveSerializationUtils.intToByteArr(pageSize);
		int offset = headerDescriptor.length;
		for (int i = 0; i < data.length; i++) {
			header[offset + i] = data[i];
		}

		data = PrimitiveSerializationUtils.longToByteArr(freeList);
		offset = headerDescriptor.length + 4;
		for (int i = 0; i < data.length; i++) {
			header[offset + i] = data[i];
		}

		data = PrimitiveSerializationUtils.longToByteArr(lastAllocatedPage);
		offset = headerDescriptor.length + 4 + 8;
		for (int i = 0; i < data.length; i++) {
			header[offset + i] = data[i];
		}
		return header;
	}

	/**
	 * Returns true if the header has been modified, and the changes have not yet
	 * been persisted to the disk.
	 * 
	 * @return True if the header is dirty, false otherwise.
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Changes the Pages status to clean.
	 */
	public void clean() {
		this.dirty = false;
	}

	/**
	 * @return the pageSize
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * @param pageSize the pageSize to set
	 */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
		dirty = true;
	}

	/**
	 * @return the freeList
	 */
	public long getFreeList() {
		return freeList;
	}

	/**
	 * @param freeList the freeList to set
	 */
	public void setFreeList(long freeList) {
		this.freeList = freeList;
		dirty = true;
	}

	/**
	 * @return the lastAllocatedPage
	 */
	public long getLastAllocatedPage() {
		return lastAllocatedPage;
	}

	/**
	 * @param lastAllocatedPage the lastAllocatedPage to set
	 */
	public void setLastAllocatedPage(long lastAllocatedPage) {
		this.lastAllocatedPage = lastAllocatedPage;
		dirty = true;
	}
}
