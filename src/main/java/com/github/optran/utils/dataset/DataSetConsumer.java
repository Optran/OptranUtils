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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.github.optran.utils.exceptions.RuntimeIOException;

/**
 * This class is used to read values that have been inserted into the DataSet.
 * 
 * It allows the following:-
 * <ol>
 * <li>Read records in the order they were inserted in.</li>
 * <li>Read records in reverse order.</li>
 * <li>Freedom to change direction of traversal at any point.</li>
 * <li>Directly jump to the first record.</li>
 * <li>Directly jump to the last record.</li>
 * </ol>
 * 
 * @author Ashutosh Wad
 *
 */
public final class DataSetConsumer {
	private DataSet dataSet;
	private long currentRecord;
	private DataSetRecord dataSetRecord;

	protected DataSetConsumer(DataSet dataSet) {
		currentRecord = dataSet.headerLength();
		dataSetRecord = null;
		this.dataSet = dataSet;
	}

	/**
	 * This method when called, sets the current record to the first record in the
	 * DataSet provided that at least one record exists in the DataSet.
	 * 
	 * @return True if the the first record was selected, false otherwise.
	 */
	public boolean first() {
		long n = dataSet.size();
		try {
			if (n > 0) {
				currentRecord = dataSet.headerLength();
				dataSetRecord = dataSet.read(currentRecord);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			throw new RuntimeIOException(e);
		}
	}

	/**
	 * This method when called, sets the current record to the last record in the
	 * DataSet provided that at least one record exists in the DataSet.
	 * 
	 * @return True if the the last record was selected, false otherwise.
	 */
	public boolean last() {
		long n;
		long last;
		synchronized (dataSet) {
			n = dataSet.size();
			last = dataSet.last();
		}
		try {
			if (n > 0) {
				currentRecord = last;
				dataSetRecord = dataSet.read(currentRecord);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			throw new RuntimeIOException(e);
		}
	}

	/**
	 * This method advances the currently selected record to the next record in the
	 * DataSet when at least one entry exists in the DataSet. If this method returns
	 * false, then it does not affect the currently selected record.
	 * 
	 * @return True if the next record was selected, false otherwise.
	 */
	public boolean next() {
		try {
			long currentRecordOld = currentRecord;
			DataSetRecord dataSetRecordOld = dataSetRecord;
			if (dataSetRecord != null) {
				currentRecord += dataSetRecord.size();
			}
			dataSetRecord = dataSet.read(currentRecord);
			if (null == dataSetRecord) {
				currentRecord = currentRecordOld;
				dataSetRecord = dataSetRecordOld;
				return false;
			} else {
				return true;
			}
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	/**
	 * This method sets the currently selected record to the previous record in the
	 * DataSet when at least one entry exists in the DataSet. If this method returns
	 * false, then it does not affect the currently selected record.
	 * 
	 * @return True if the previous record was selected, false otherwise.
	 */
	public boolean previous() {
		long n;
		long last;
		synchronized (dataSet) {
			n = dataSet.size();
			last = dataSet.last();
		}
		if (1 >= n) {
			/*
			 * If there are no records, or if there is only one record, there exist no
			 * previous records
			 */
			return false;
		}
		try {
			if (null == dataSetRecord) {
				return false;
			}
			long prevOffset = dataSetRecord.getPreviousRecord();
			if (0 == prevOffset) {
				return false;
			}
			currentRecord = currentRecord - dataSetRecord.getPreviousRecord();
			dataSetRecord = dataSet.read(currentRecord);
			return (dataSetRecord != null);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	/**
	 * This method retrives the currently selected record.
	 * 
	 * @return The currently selected record.
	 */
	public byte[] read() {
		return parseDataSetRecord(dataSetRecord);
	}

	/**
	 * This is a convinience method that provides a wrapper over the read method to
	 * fetch a String.
	 * 
	 * @return The string representation of the record.
	 */
	public String readString() {
		byte[] data = read();
		if (null == data) {
			return null;
		}
		return new String(data, StandardCharsets.UTF_8);
	}

	private byte[] parseDataSetRecord(DataSetRecord dataSetRecord) {
		if (null == dataSetRecord) {
			return null;
		}
		byte[] data = dataSetRecord.getRecord();
		return data;
	}
}
