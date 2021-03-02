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

import java.io.IOException;

/**
 * The PagedFile class allows random access to any binary file in a paged
 * fashion.
 * 
 * @author Ashutosh Wad
 *
 */
public interface PagedFile {
	/**
	 * Reads and returns {@link Page} at the specified index.
	 * 
	 * @param pageNumber The index of the {@link Page} to be fetched.
	 * @return The {@link Page} at the specified index.
	 * @throws IOException If an IO exception is thrown when interacting with the
	 *                     file layer.
	 */
	public Page readPage(long pageNumber) throws IOException;

	/**
	 * Writes the {@link Page} that has been provided.
	 * 
	 * @param page The {@link Page} to be written.
	 * @throws IOException If an IO exception is thrown when interacting with the
	 *                     file layer.
	 */
	public void writePage(Page page) throws IOException;

	/**
	 * This function returns the size of any single page that is/can be read from
	 * this file.
	 * 
	 * @return The size of any single page in this file.
	 */
	public int getPageSize();

	/**
	 * This method allocates a page on the disk to be written to later.
	 * 
	 * @return The newly allocated page. Null if no page could be allocated.
	 * @throws IOException If this operation fails.
	 */
	public Page allocPage() throws IOException;

	/**
	 * This method frees the current page. How this freeing is done depends on the
	 * implementation.
	 * 
	 * @return True if the free succeeded, false otherwise.
	 * @throws IOException If this operation fails.
	 */
	public boolean freePage(Page page) throws IOException;

	/**
	 * This method increases the page size of the current page by one default page
	 * size.
	 * 
	 * @return The newly extended page. Null if the page could not be extended.
	 * @throws IOException If this operation fails.
	 */
	public Page extendPage(Page page) throws IOException;

	/**
	 * This method flushes any data that has not yet been written to the disk, to
	 * the disk.
	 */
	public void flush() throws IOException;

	/**
	 * This method checks if the underlying {@link PagedFile} actually exists.
	 * 
	 * @return
	 */
	public boolean exists();

	/**
	 * Closes the file that is being used to persist data. Any calls made to this
	 * instance (other than close itself) post the invocation of close will result
	 * in a {@link NullPointerException}
	 * 
	 * @return True if the close was successful, false otherwise.
	 */
	public boolean close();
}
