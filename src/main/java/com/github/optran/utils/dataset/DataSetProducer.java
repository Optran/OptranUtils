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
package com.github.optran.utils.dataset;

import java.nio.charset.StandardCharsets;

/**
 * This class is used to write values to the DataSet.
 * 
 * @author Ashutosh Wad
 *
 */
public final class DataSetProducer {
	private DataSet dataSet;

	protected DataSetProducer(DataSet dataSet) {
		this.dataSet = dataSet;
	}

	/**
	 * 
	 * @param str
	 * @return
	 */
	public boolean writeString(String str) {
		if (null == str) {
			return write((byte[]) null);
		}
		return write(str.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * The write method writes the record provided to the DataSet.
	 * 
	 * @param record The record to be written.
	 * @return True if the write was a success, false otherwise.
	 */
	public boolean write(byte[] record) {
		try {
			dataSet.write(record);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * The commit option when called, persists the saved records across application
	 * restarts. If the commit is not called, records inserted after the last commit
	 * will be lost once the application quits.
	 * 
	 * @return
	 */
	public boolean commit() {
		try {
			dataSet.commit();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
