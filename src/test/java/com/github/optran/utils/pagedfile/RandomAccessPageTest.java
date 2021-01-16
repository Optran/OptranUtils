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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class RandomAccessPageTest {
	private RandomAccessPage page = null;
	private PagedFileTest pagedFileTest;

	@Before
	public void before() {
		pagedFileTest = new PagedFileTest();
		page = new RandomAccessPage(1, new byte[10], pagedFileTest);
	}

	@Test
	public void initTest() {
		byte[]data = page.getData();
		assertEquals(10, data.length);
		for (int i = 0; i < data.length; i++) {
			assertEquals(0, data[i]);
		}
		assertEquals(10, page.size());
		assertEquals(1, page.getPageId());
	}

	@Test
	public void readPastEndOfPage() {
		page.setHead(10);
		assertEquals(-1, page.read());
	}

	@Test
	public void readTest() {
		page.write("ashutoshwad".getBytes(StandardCharsets.UTF_8));
		page.setHead(0);
		byte[]data = new byte[8];
		page.read(data);
		assertEquals("ashutosh", new String(data, StandardCharsets.UTF_8));

		page.setHead(0);
		data = new byte[12];
		data[10]=' ';
		data[11]=' ';
		page.read(data);
		assertEquals("ashutoshwa  ", new String(data, StandardCharsets.UTF_8));
	}

	@Test
	public void writeTest() {
		page.write("ashutoshwad".getBytes(StandardCharsets.UTF_8));
		assertEquals("ashutoshwa", new String(page.getData(), StandardCharsets.UTF_8));		
	}

	@Test
	public void writeTestHead() {
		page.setHead(1);
		page.write("ashutoshwad".getBytes(StandardCharsets.UTF_8));
		byte[]data = new byte[9];
		page.setHead(1);
		assertEquals(9, page.read(data));;
		String result = new String(data, StandardCharsets.UTF_8);
		assertEquals("ashutoshw", result);

		data = new byte[10];
		page.setHead(0);
		page.write("optran".getBytes(StandardCharsets.UTF_8));
		page.setHead(0);
		assertEquals(10, page.read(data));;
		result = new String(data, StandardCharsets.UTF_8);
		assertEquals("optranoshw", result);
		assertTrue(page.isDirty());
		page.clean();
		assertFalse(page.isDirty());
	}

	@Test
	public void readWriteNullTest() {
		assertEquals(0, page.read(null));
		assertEquals(0, page.write(null));
		assertEquals(0, page.getHead());
		page.setHead(1);
		assertEquals(0, page.read(null));
		assertEquals(0, page.write(null));
		assertEquals(1, page.getHead());
	}

	@Test
	public void setHeadTest() {
		page.setHead(-1);
		assertEquals(0, page.getHead());
		page.setHead(0);
		assertEquals(0, page.getHead());
		page.setHead(3);
		assertEquals(3, page.getHead());
		page.setHead(10);
		assertEquals(10, page.getHead());
		page.setHead(11);
		assertEquals(10, page.getHead());
	}

	@Test
	public void testValidityForHeadLessThan0() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method isHeadValid = page.getClass().getDeclaredMethod("isHeadValid", int.class);
		isHeadValid.setAccessible(true);
		assertFalse((boolean)isHeadValid.invoke(page, -1));
	}

	@Test
	public void testFlush() {
		page.flush();
		assertFalse(pagedFileTest.isInvokeWritePage());
		page.write("ashutosh".getBytes());
		page.flush();
		assertTrue(pagedFileTest.isInvokeWritePage());
		page = new RandomAccessPage(2, new byte[10], new PagedFile() {
			
			@Override
			public void writePage(Page page) throws IOException {
				throw new IOException();
			}
			
			@Override
			public Page readPage(long pageNumber) throws IOException {
				return null;
			}
			
			@Override
			public void flush() throws IOException {
			}
			
			@Override
			public boolean close() {
				return false;
			}
		});
		page.write("ashutosh".getBytes());
		assertFalse(page.flush());
	}

	private class PagedFileTest implements PagedFile {
		boolean invokeReadPage;
		boolean invokeWritePage;
		boolean invokeFlush;
		boolean invokeClose;

		public PagedFileTest() {
			init();
		}
	
		public void init() {
			this.invokeReadPage = false;
			this.invokeWritePage = false;
			this.invokeFlush = false;
			this.invokeClose = false;
		}

		public boolean isInvokeReadPage() {
			return invokeReadPage;
		}

		public void setInvokeReadPage(boolean invokeReadPage) {
			this.invokeReadPage = invokeReadPage;
		}

		public boolean isInvokeWritePage() {
			return invokeWritePage;
		}

		public void setInvokeWritePage(boolean invokeWritePage) {
			this.invokeWritePage = invokeWritePage;
		}

		public boolean isInvokeFlush() {
			return invokeFlush;
		}

		public void setInvokeFlush(boolean invokeFlush) {
			this.invokeFlush = invokeFlush;
		}

		public boolean isInvokeClose() {
			return invokeClose;
		}

		public void setInvokeClose(boolean invokeClose) {
			this.invokeClose = invokeClose;
		}

		@Override
		public Page readPage(long pageNumber) throws IOException {
			invokeReadPage = true;
			return null;
		}

		@Override
		public void writePage(Page page) throws IOException {
			invokeWritePage = true;
		}

		@Override
		public void flush() throws IOException {
			invokeWritePage = true;
		}

		@Override
		public boolean close() {
			invokeWritePage = true;
			return invokeWritePage;
		}
		
	} 
}
