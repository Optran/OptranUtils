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

public interface Heap {
	/**
	 * Allocate memory on disk that can store data of length size.
	 * 
	 * The actual amount of memory that is allocated by malloc depends on the
	 * underlying implementation.
	 * 
	 * @param size length in bytes of the memory to be allocated.
	 * @return Reference to the allocated memory, or 0 if memory could not be
	 *         allocated.
	 */
	public long malloc(int size);

	/**
	 * This method allows one to increase or decrease the size of a pointer as
	 * needed. The system may choose to update the location of the data on the disk
	 * and as such change the reference returned. It should be noted that if the
	 * reference returned by realloc is different from the one in the input, realloc
	 * takes care to free the old pointer and therefore the use of realloc must be
	 * done carefully as any locations still maintaining a reference to the old
	 * pointer will now not work.
	 * 
	 * @param reference Pointer to the old allocated memory
	 * @param size      The new size that is needed.
	 * @return Reference to the allocated memory, or 0 if memory could not be
	 *         allocated.
	 */
	public long realloc(long reference, int size);

	/**
	 * This releases any memory bound to the reference provided and returns it to
	 * the system.
	 * 
	 * @param reference A memory location previously allocated by malloc.
	 */
	public void free(long reference);

	/**
	 * This method returns the amount of memory that has been leased from the system
	 * for the reference in question.
	 * 
	 * @param reference The reference to the memory location previously obtained
	 *                  from malloc.
	 * @return The total usable size that has been bound to the reference.
	 */
	public int sizeOf(long reference);

	/**
	 * Reads the memory that has been bound to the reference provided and returns it
	 * as a byte array.
	 * 
	 * @param reference The reference to the memory location previously obtained
	 *                  from malloc.
	 * @return The memory that has been bound to the reference provided as a byte
	 *         array.
	 */
	public byte[] read(long reference);

	/**
	 * Writes the bytes provided to the memory that has been reserved for the
	 * reference that has been provided.
	 * 
	 * @param reference reference The reference to the memory location previously
	 *                  obtained from malloc.
	 * @param data      The data to be saved.
	 */
	public void write(long reference, byte[] data);
}
