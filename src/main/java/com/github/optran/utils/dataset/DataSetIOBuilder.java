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

import static com.github.optran.utils.PrimitiveSerializationUtils.byteArrToLong;
import static com.github.optran.utils.PrimitiveSerializationUtils.longToByteArr;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.github.optran.utils.exceptions.CorruptDataSetException;
import com.github.optran.utils.pagedfile.BufferedRandomAccessFile;

public final class DataSetIOBuilder {
	private static final String TEMP_FILE_PREFIX = "OPTRAN_DATA_SET_";
	private static final String FILE_HEADER = "OptranDataSet\n";
	private static final String ERROR_MSG = "DataSet is either corrupt or this file is not a DataSet file.";

	private File file;
	private int pageSize;
	private int cacheSize;

	public DataSetIOBuilder() {
		file = null;
		pageSize = 65536;
		cacheSize = 100;
	}

	public DataSetIOBuilder withFile(File file) {
		this.file = file;
		return this;
	}

	public DataSetIOBuilder withPageSize(int pageSize) {
		this.pageSize = pageSize;
		return this;
	}

	public DataSetIOBuilder withCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
		return this;
	}

	public DataSetIO build() throws IOException {
		if (file == null) {
			file = File.createTempFile(TEMP_FILE_PREFIX,".ds");
			file.deleteOnExit();
			createDataSet();
		}
		if (!file.exists()) {
			createDataSet();
		} else {
			validateDataSet();
		}
		if(pageSize<=10) {
			pageSize = 65536;
		}
		BufferedRandomAccessFile praf = null;
		if(cacheSize>0) {
			praf = new BufferedRandomAccessFile(file, pageSize, cacheSize);
		} else {
			praf = new BufferedRandomAccessFile(file, pageSize);
		}
		return new DataSetIO(new DataSet(praf));
	}

	private void createDataSet() throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
		bos.write(FILE_HEADER.getBytes(StandardCharsets.UTF_8));
		bos.write(longToByteArr(0));
		bos.write(longToByteArr(0));
		bos.close();
	}

	private void validateDataSet() throws IOException {
		long fileLength = file.length();
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

		/* Start header validation */
		byte[]actualHeader = FILE_HEADER.getBytes(StandardCharsets.UTF_8);
		byte[]header = new byte[actualHeader.length];

		int readLen = bis.read(header);
		if(readLen!=actualHeader.length) {
			throw new CorruptDataSetException(ERROR_MSG);
		}
		for (int i = 0; i < actualHeader.length; i++) {
			if(actualHeader[i]!=header[i]) {
				throw new CorruptDataSetException(ERROR_MSG);
			}
		}

		/* Validate that the total number of records are present */
		byte[]numRecordsBytes = new byte[8];
		readLen = bis.read(numRecordsBytes);
		if(readLen<8) {
			throw new CorruptDataSetException(ERROR_MSG);
		}
		long numRec = byteArrToLong(numRecordsBytes);

		/* Validate that the last record offset is present */
		byte[]lastRecordBytes = new byte[8];
		readLen = bis.read(lastRecordBytes);
		if(readLen<8) {
			throw new CorruptDataSetException(ERROR_MSG);
		}
		long lastRecord = byteArrToLong(lastRecordBytes);

		if(numRec>0) {
			if(lastRecord<(FILE_HEADER.getBytes(StandardCharsets.UTF_8).length + 8 + 8)) {
				throw new CorruptDataSetException(ERROR_MSG);
			}
		}
		if(lastRecord>=fileLength) {
			throw new CorruptDataSetException(ERROR_MSG);
		}
		bis.close();
	}
}
