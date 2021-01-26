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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.github.optran.utils.dataset.DataSetConsumer;
import com.github.optran.utils.dataset.DataSetIO;
import com.github.optran.utils.dataset.DataSetIOBuilder;
import com.github.optran.utils.dataset.DataSetProducer;

@RunWith(JUnit4.class)
public class DataSetTest {
	private DataSetIO dataSetIO;

	@Before
	public void before() throws IOException {
		dataSetIO = new DataSetIOBuilder().withCacheSize(100).withPageSize(100).build();
	}


	/**
	 * This test case verifies that opening and closing a file with no records does
	 * not cause errors and allows us to add and remove records as expected post the
	 * first commit.
	 * 
	 * @throws IOException If there is any error when accessing the underlying file.
	 */
	@Test
	public void dataSetTest_TC00001() throws IOException {
		File file = File.createTempFile("OPTRAN_TEST_DATA_SET_",".ds");
		file.delete();
		file.deleteOnExit();
		dataSetIO = new DataSetIOBuilder().withFile(file).withCacheSize(100).withPageSize(100).build();

		DataSetConsumer consumer = dataSetIO.createConsumer();
		DataSetProducer producer = dataSetIO.createProducer();
		// Attempt to consume when no records exist should return null.
		assertNull(consumer.read());
		assertFalse(consumer.next());
		assertFalse(consumer.previous());
		assertNull(consumer.read());
		assertFalse(consumer.first());
		assertFalse(consumer.last());
		assertNull(consumer.read());
		// close producer without writing anything;
		dataSetIO.closeProducer(producer);
		// Open the dataSet anew and verify consumer still works fine
		dataSetIO = new DataSetIOBuilder().withFile(file).withCacheSize(100).withPageSize(100).build();
		assertNull(consumer.read());
		assertFalse(consumer.next());
		assertFalse(consumer.previous());
		assertNull(consumer.read());
		assertFalse(consumer.first());
		assertFalse(consumer.last());
		assertNull(consumer.read());
	}

	/**
	 * This test case validates cases where one record has been inserted into the
	 * file.
	 */
	@Test
	public void dataSetTest_TC00002() {
		DataSetConsumer consumer = dataSetIO.createConsumer();
		DataSetProducer producer = dataSetIO.createProducer();
		producer.writeString("Hello");
		assertNull(consumer.read());
		assertFalse(consumer.previous());
		assertTrue(consumer.next());
		assertEquals("Hello", consumer.readString());
		assertFalse(consumer.next());
		assertFalse(consumer.previous());
		// As read head has not moved we still get the value hello
		assertEquals("Hello", consumer.readString());
		assertTrue(consumer.first());
		assertEquals("Hello", consumer.readString());
		assertTrue(consumer.last());
		assertEquals("Hello", consumer.readString());
	}

	/**
	 * This test case validates cases where two records have been inserted into the
	 * file.
	 */
	@Test
	public void dataSetTest_TC00003() {
		DataSetConsumer consumer = dataSetIO.createConsumer();
		DataSetProducer producer = dataSetIO.createProducer();
		producer.writeString("Hello");
		producer.writeString("World");
		assertNull(consumer.read());
		assertFalse(consumer.previous());
		assertTrue(consumer.next());
		assertEquals("Hello", consumer.readString());
		assertFalse(consumer.previous());
		assertTrue(consumer.next());
		assertEquals("World", consumer.readString());
		assertFalse(consumer.next());
		assertEquals("World", consumer.readString());
		assertTrue(consumer.previous());
		assertEquals("Hello", consumer.readString());
		assertFalse(consumer.previous());
		assertEquals("Hello", consumer.readString());
		assertTrue(consumer.first());
		assertEquals("Hello", consumer.readString());
		assertTrue(consumer.last());
		assertEquals("World", consumer.readString());
		dataSetIO.closeConsumer(consumer);
		dataSetIO.closeProducer(producer);
	}

	/**
	 * This test case validates cases where three records have been inserted into the
	 * file.
	 */
	@Test
	public void dataSetTest_TC00004() {
		DataSetConsumer consumer = dataSetIO.createConsumer();
		DataSetProducer producer = dataSetIO.createProducer();
		producer.writeString("Hello");
		producer.writeString("World");
		producer.writeString("!");
		assertNull(consumer.read());
		assertFalse(consumer.previous());
		assertTrue(consumer.next());
		assertEquals("Hello", consumer.readString());
		assertFalse(consumer.previous());
		assertTrue(consumer.next());
		assertEquals("World", consumer.readString());
		assertTrue(consumer.previous());
		assertEquals("Hello", consumer.readString());
		assertTrue(consumer.next());
		assertEquals("World", consumer.readString());
		assertTrue(consumer.next());
		assertEquals("!", consumer.readString());
		assertFalse(consumer.next());
		assertEquals("!", consumer.readString());
		assertFalse(consumer.next());
		assertTrue(consumer.previous());
		assertEquals("World", consumer.readString());
		assertTrue(consumer.next());
		assertTrue(consumer.previous());
		assertEquals("World", consumer.readString());
		assertTrue(consumer.previous());
		assertEquals("Hello", consumer.readString());
		assertFalse(consumer.previous());
		assertEquals("Hello", consumer.readString());
		assertTrue(consumer.first());
		assertEquals("Hello", consumer.readString());
		assertTrue(consumer.last());
		assertEquals("!", consumer.readString());
		dataSetIO.closeConsumer(consumer);
		dataSetIO.closeProducer(producer);
	}
}
