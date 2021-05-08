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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.github.optran.utils.PrimitiveSerializationUtils;

/**
 * This class allows direct interaction with the Page of data fetched from the
 * file on the disk.
 * 
 * @author Ashutosh Wad
 *
 */
public class OptranFilePage {
	private static final int PAGE_HEADER_LENGTH = 13;
	private long pageId;
	private Map<String, String> metadata;
	private byte pageType;
	private long nextPage;
	private int length;
	private byte[] pageData;

	private int size;
	private int head;
	private boolean dirty;

	public OptranFilePage(long pageId, byte[] pageData) {
		this.pageId = pageId;
		pageType = pageData[0];
		readNextPageValue(pageData);
		readLengthValue(pageData);
		this.size = pageData.length - PAGE_HEADER_LENGTH;
		this.pageData = new byte[size];
		for (int i = 0; i < size; i++) {
			this.pageData[i] = pageData[i + PAGE_HEADER_LENGTH];
		}
		this.head = 0;
		metadata = new HashMap<String, String>();
	}

	private void readNextPageValue(byte[] pageData) {
		byte[] data = new byte[8];
		for (int i = 0; i < data.length; i++) {
			data[i] = pageData[i + 1];
		}
		nextPage = PrimitiveSerializationUtils.byteArrToLong(data);
	}

	private void readLengthValue(byte[] pageData) {
		byte[] data = new byte[4];
		for (int i = 0; i < data.length; i++) {
			data[i] = pageData[i + 9];
		}
		length = PrimitiveSerializationUtils.byteArrToInt(data);
	}

	/**
	 * Returns the index of this page.
	 * 
	 * @return This Page's index.
	 */
	public long getPageId() {
		return pageId;
	}

	/**
	 * This method returns the size of this page on the disk.
	 * 
	 * @return The size of the data held by this page.
	 */
	public int size() {
		return size + PAGE_HEADER_LENGTH;
	}

	/**
	 * This method returns the amount of data this page can contain.
	 * 
	 * @return The amount of data this page can contain.
	 */
	public int capicity() {
		return size;
	}

	/**
	 * Changes the Pages status to clean.
	 */
	public void clean() {
		dirty = false;
	}

	/**
	 * Returns true if the page has been modified, and the changes have not yet been
	 * persisted to the disk.
	 * 
	 * @return True if the page is dirty, false otherwise.
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * @return the pageType
	 */
	public byte getPageType() {
		return pageType;
	}

	/**
	 * @param pageType the pageType to set
	 */
	public void setPageType(byte pageType) {
		this.pageType = pageType;
		dirty = true;
	}
	
	/**
	 * This is a convenience method to allow setting integer values to the page type
	 * field.
	 * 
	 * @param pageType the pageType to set
	 */
	public void setPageType(int pageType) {
		this.pageType = (byte)pageType;
		dirty = true;
	}

	/**
	 * @return the nextPage
	 */
	public long getNextPage() {
		return nextPage;
	}

	/**
	 * @param nextPage the nextPage to set
	 */
	public void setNextPage(long nextPage) {
		this.nextPage = nextPage;
		dirty = true;
	}

	/**
	 * @return the length of data currently present on the page.
	 */
	public int getLength() {
		return length;
	}

	/**
	 * @param length the length of data currently present on the page.
	 */
	public void setLength(int length) {
		this.length = length;
		dirty = true;
	}

	/**
	 * This method gets a copy of the data currently held by this page. The data
	 * returned is a copy, so edits made to the data that is returned will not
	 * impact the state of the page.
	 * 
	 * @return A copy of the page's internal data
	 */
	public byte[] getData() {
		byte[] data = new byte[size + PAGE_HEADER_LENGTH];
		data[0] = pageType;
		byte[] nextPageByteArr = PrimitiveSerializationUtils.longToByteArr(nextPage);
		for (int i = 0; i < nextPageByteArr.length; i++) {
			data[i + 1] = nextPageByteArr[i];
		}
		byte[] lengthByteArr = PrimitiveSerializationUtils.intToByteArr(length);
		for (int i = 0; i < lengthByteArr.length; i++) {
			data[i + 9] = lengthByteArr[i];
		}
		for (int i = 0; i < pageData.length; i++) {
			data[i + PAGE_HEADER_LENGTH] = pageData[i];
		}
		return data;
	}

	/**
	 * Returns the current position of the head in the page.
	 * 
	 * @return The current location of the page head.
	 */
	public int getHead() {
		return head;
	}

	/**
	 * Sets the read head to the specified offset. All subsequent reads will take
	 * place from this offset.
	 * 
	 * @param head The location in the page to which the page head must be set.
	 */
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
	 * Reads a single byte at the current location of the head, and advances the
	 * head. This method will return -1 if there are no more bytes to read.
	 * 
	 * @return The byte at the current head position, or -1 if there are no more
	 *         bytes to read.
	 */
	public int read() {
		if (isHeadValid(head)) {
			return pageData[head++];
		} else {
			return -1;
		}
	}

	/**
	 * Reads as many bytes as it can into the byte array that is provided, and
	 * returns the number of bytes read. The head is also advanced correspondingly.
	 * 
	 * @param value The byte[] to read to.
	 * @return The number of bytes read.
	 */
	public int read(byte[] value) {
		int numBytesRead = 0;
		if (null == value) {
			return numBytesRead;
		}
		int readValue = 0;
		for (int i = 0; i < value.length; i++) {
			readValue = read();
			if (readValue >= 0) {
				value[i] = (byte) readValue;
				numBytesRead++;
			} else {
				break;
			}
		}
		return numBytesRead;
	}

	/**
	 * Writes the byte that is provided to the current location of the head, and
	 * advances the head.
	 * 
	 * @param value The byte to be written.
	 * @return True if the write was successful, false otherwise.
	 */
	public boolean write(int value) {
		if (!isHeadValid(head)) {
			return false;
		}
		pageData[head++] = (byte) value;
		dirty = true;

		if (head > length) {
			length = head;
		}

		return true;
	}

	/**
	 * Writes as many bytes as it can from the value that has been provided to the
	 * current location of the head, and advances the head by the same amount. It
	 * also returns the total number of bytes that have been successfully written.
	 * 
	 * @param value The data to be written.
	 * @return The number of bytes that were written.
	 */
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

	/**
	 * @return the metadataMap
	 */
	public Map<String, String> getMetadata() {
		return Collections.unmodifiableMap(metadata);
	}

	/**
	 * Returns the metadata value associated with the key in question.
	 * 
	 * @param key The key whose value needs to be retrieved.
	 * @return The value associated to the key provided.
	 */
	public String getMetadata(String key) {
		return metadata.get(key);
	}

	/**
	 * @param metadataMap the metadataMap to set
	 */
	public void addMetadata(String key, String value) {
		this.metadata.put(key, value);
	}
}
