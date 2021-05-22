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

import static com.github.optran.utils.PrimitiveSerializationUtils.byteArrToInt;
import static com.github.optran.utils.PrimitiveSerializationUtils.byteArrToLong;
import static com.github.optran.utils.PrimitiveSerializationUtils.longToByteArr;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.github.optran.utils.exceptions.CorruptDataSetException;
import com.github.optran.utils.exceptions.IncorrectInitializationValueException;
import com.github.optran.utils.exceptions.RuntimeIOException;
import com.github.optran.utils.pagedfile.BufferedRandomAccessFile;

/**
 * The DataSet class allows synchronous access to a file to read and write
 * records.
 * 
 * @author Ashutosh Wad
 *
 */
final class DataSet {
	private static final Logger logger = Logger.getLogger(DataSet.class);
	/**
	 * Header Size
	 */
	private static final int HEADER_SIZE = 14;

	/**
	 * Size of total records variable
	 */
	private static final int NUM_REC_SIZE = 8;

	/**
	 * Size of variable containing the location of the last record
	 */
	private static final int LAST_REC_LOC_SIZE = 8;

	private BufferedRandomAccessFile saveFile;
	private long numberOfRecords;
	private long lastRecLoc;
	private long writeHead;
	private byte[] intVar;
	private byte[] longVar;

	protected DataSet(BufferedRandomAccessFile saveFile) throws IOException {
		this.saveFile = saveFile;
		intVar = new byte[4];
		longVar = new byte[8];
		saveFile.setHead(14);/* Set read head just after the header */
		numberOfRecords = readLong();
		lastRecLoc = readLong();
		if (0 == numberOfRecords) {
			writeHead = headerLength();
		} else {
			writeHead = lastRecLoc + read(lastRecLoc).size();
		}
	}

	private int readint() throws IOException {
		int bytesRead = saveFile.read(intVar);
		if (bytesRead < 4) {
			throw new RuntimeIOException("Unable to read int value.");
		}
		return byteArrToInt(intVar);
	}

	private long readLong() throws IOException {
		int bytesRead = saveFile.read(longVar);
		if (bytesRead < 8) {
			throw new RuntimeIOException("Unable to read long value.");
		}
		return byteArrToLong(longVar);
	}

	/**
	 * Returns the length of the header metadata in this file. This is also the
	 * offset of the first record if it exists.
	 * 
	 * @return
	 */
	public int headerLength() {
		return HEADER_SIZE + NUM_REC_SIZE + LAST_REC_LOC_SIZE;
	}

	/**
	 * Returns the location of the last record in the DataSet.
	 * 
	 * @return
	 */
	public long last() {
		return lastRecLoc;
	}

	/**
	 * Returns the current number of elements in the DataSet.
	 * 
	 * @return
	 */
	public long size() {
		return numberOfRecords;
	}

	/**
	 * Reads a record from the location specified.
	 * 
	 * @param location The location to be read from.
	 * @return The record present at the location that has been provided.
	 * @throws IOException If there is any error accessing the file that backs this
	 *                     DataSet.
	 */
	protected synchronized DataSetRecord read(long location) throws IOException {
		if (0 == numberOfRecords) {
			/* This implies nothing was written to the file as yet, so we just return */
			return null;
		}
		if (0 > location) {
			throw new IncorrectInitializationValueException(
					"A file cannot have a negative index. '" + location + "' was provided.");
		}
		logger.trace("Requested Location: " + location);
		logger.trace("WriteHead: " + writeHead);
		if (writeHead > 0 && location >= writeHead) {
			logger.trace("Location greater than writeHead! returning null");
			return null;
		}
		saveFile.setHead(location);
		logger.trace("Set head to: " + location);
		int prevRecord = readint();
		int recLen = readint();
		logger.trace("Previous record offset: " + prevRecord);
		logger.trace("Length of current record: " + recLen);

		if (writeHead > 0 && (8 + recLen + location) > writeHead) {
			throw new CorruptDataSetException(
					"Either the DataSet in question is corrupt, or the location provided for the record is incorrect.");
		}

		byte[] record = null;
		if (recLen == 0) {
			record = new byte[1];
		} else {
			record = new byte[recLen];
		}
		saveFile.read(record);
		if (recLen == 0) {
			if (record[0] == 0) {
				record = null;
			} else {
				record = new byte[0];
			}
		}
		return new DataSetRecord(prevRecord, recLen, record);
	}

	/**
	 * Writes a record to the end of the DataSet.
	 * 
	 * @param record The record to be written.
	 */
	public synchronized void write(byte[] record) {
		// Set the length and record variables.
		int len = -1;
		boolean isNull = false;
		if (null == record) {
			len = 0;
			isNull = true;
		} else {
			len = record.length;
		}
		logger.trace("Write head at: " + writeHead);
		logger.trace("Record length is: " + len);
		if (0 == len) {
			record = new byte[1];
			record[0] = isNull ? (byte) 0 : (byte) 1;
			logger.trace("Null indicator: " + record[0]);
		}
		// Calculate prevRec and write data
		int prevRec = (lastRecLoc > 0) ? (int) (writeHead - lastRecLoc) : 0;
		logger.trace("Previous record offset: " + prevRec);
		byte[] data = new DataSetRecord(prevRec, len, record).serialize();
		saveFile.setHead(writeHead);
		logger.trace("Set head to: " + writeHead);
		saveFile.write(data);
		lastRecLoc = writeHead;
		writeHead = writeHead + data.length;
		numberOfRecords++;
	}

	/**
	 * The commit option when called, persists the saved records across application
	 * restarts. If the commit is not called, records inserted after the last commit
	 * will be lost once the application quits.
	 * 
	 * @return
	 */
	public synchronized void commit() {
		if (0 == numberOfRecords) {
			saveFile.close();
			return;
		}
		saveFile.flush();
		logger.trace("Flushing cached data");
		saveFile.setHead(HEADER_SIZE);
		saveFile.write(longToByteArr(numberOfRecords));
		saveFile.write(longToByteArr(lastRecLoc));
		logger.trace("Commiting records...");
		logger.trace("numberOfRecords: " + numberOfRecords);
		logger.trace("lastRecLoc: " + lastRecLoc);
		saveFile.close();
		logger.trace("File closed successfully");
	}
}
