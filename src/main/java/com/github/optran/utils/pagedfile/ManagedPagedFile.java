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

public interface ManagedPagedFile extends PagedFile {
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
}
