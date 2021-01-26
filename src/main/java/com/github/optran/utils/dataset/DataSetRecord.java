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

import static com.github.optran.utils.PrimitiveSerializationUtils.intToByteArr;

final class DataSetRecord {
	private final int previousRecord;
	private final int recordSize;
	private final byte[] record;

	public DataSetRecord(int previousRecord, int recordSize, byte[] record) {
		this.previousRecord = previousRecord;
		this.recordSize = recordSize;
		this.record = record;
	}

	public int getPreviousRecord() {
		return previousRecord;
	}

	public int getRecordSize() {
		return recordSize;
	}

	public byte[] getRecord() {
		return record;
	}

	public int size() {
		if (null == record) {
			return 9;
		}
		if (0 == recordSize) {
			return 9;
		}
		return 8 + record.length;
	}

	public byte[] serialize() {
		byte[] serializedData = new byte[8 + record.length];
		int index = 0;
		byte[] curr = intToByteArr(previousRecord);
		for (int i = 0; i < curr.length; i++) {
			serializedData[index++] = curr[i];
		}
		curr = intToByteArr(recordSize);
		for (int i = 0; i < curr.length; i++) {
			serializedData[index++] = curr[i];
		}
		for (int i = 0; i < record.length; i++) {
			serializedData[index++] = record[i];
		}
		return serializedData;
	}
}
