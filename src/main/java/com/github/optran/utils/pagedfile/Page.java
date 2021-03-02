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

/**
 * This class allows direct interaction with the Page of data fetched from the
 * file on the disk.
 * 
 * @author Ashutosh Wad
 *
 */
public interface Page {
	/**
	 * Returns the index of this page.
	 * 
	 * @return This Page's index.
	 */
	public long getPageId();

	/**
	 * This method returns the size of the data held by this page.
	 * 
	 * @return The size of the data held by this page.
	 */
	public int size();

	/**
	 * Changes the Pages status to clean.
	 */
	public void clean();

	/**
	 * Returns true if the page has been modified, and the changes have not yet been
	 * persisted to the disk.
	 * 
	 * @return True if the page is dirty, false otherwise.
	 */
	public boolean isDirty();

	/**
	 * This method gets a copy of the data currently held by this page. The data
	 * returned is a copy, so edits made to the data that is returned will not
	 * impact the state of the page.
	 * 
	 * @return A copy of the page's internal data
	 */
	public byte[] getData();

	/**
	 * Returns the current position of the head in the page.
	 * 
	 * @return The current location of the page head.
	 */
	public int getHead();

	/**
	 * Sets the read head to the specified offset. All subsequent reads will take
	 * place from this offset.
	 * 
	 * @param head The location in the page to which the page head must be set.
	 */
	public void setHead(int head);

	/**
	 * Reads a single byte at the current location of the head, and advances the
	 * head. This method will return -1 if there are no more bytes to read.
	 * 
	 * @return The byte at the current head position, or -1 if there are no more
	 *         bytes to read.
	 */
	public int read();

	/**
	 * Reads as many bytes as it can into the byte array that is provided, and
	 * returns the number of bytes read. The head is also advanced correspondingly.
	 * 
	 * @param value The byte[] to read to.
	 * @return The number of bytes read.
	 */
	public int read(byte[] value);

	/**
	 * Writes the byte that is provided to the current location of the head, and
	 * advances the head.
	 * 
	 * @param value The byte to be written.
	 * @return True if the write was successful, false otherwise.
	 */
	public boolean write(int value);

	/**
	 * Writes as many bytes as it can from the value that has been provided to the
	 * current location of the head, and advances the head by the same amount. It
	 * also returns the total number of bytes that have been successfully written.
	 * 
	 * @param value The data to be written.
	 * @return The number of bytes that were written.
	 */
	public int write(byte[] value);
}
