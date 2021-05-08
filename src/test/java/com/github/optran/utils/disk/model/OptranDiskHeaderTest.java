package com.github.optran.utils.disk.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.github.optran.utils.PrimitiveSerializationUtils;
import com.github.optran.utils.disk.exception.CorruptDiskException;

@RunWith(JUnit4.class)
public class OptranDiskHeaderTest {
	private OptranFileHeader header;
	private int pageSize;
	private long freeList;
	private long lastAllocatedPage;

	@Before
	public void before() {
		byte[] data = new byte[4];
		for (int i = 0; i < data.length; i++) {
			data[i] = 1;
		}
		pageSize = PrimitiveSerializationUtils.byteArrToInt(data);

		data = new byte[8];
		for (int i = 0; i < data.length; i++) {
			data[i] = 2;
		}
		freeList = PrimitiveSerializationUtils.byteArrToInt(data);

		data = new byte[8];
		for (int i = 0; i < data.length; i++) {
			data[i] = 3;
		}
		lastAllocatedPage = PrimitiveSerializationUtils.byteArrToInt(data);
	}

	@Test
	public void testHeaderInitialization() {
		OptranFileHeader header = new OptranFileHeader();
		assertEquals(512, header.getPageSize());
		assertEquals(0, header.getFreeList());
		assertEquals(0, header.getLastAllocatedPage());

		header = new OptranFileHeader(pageSize);
		assertEquals(pageSize, header.getPageSize());
		assertEquals(0, header.getFreeList());
		assertEquals(0, header.getLastAllocatedPage());
	}

	@Test
	public void testHeaderSerialization() {
		OptranFileHeader header = new OptranFileHeader();
		header.setPageSize(pageSize);
		header.setFreeList(freeList);
		header.setLastAllocatedPage(lastAllocatedPage);

		byte[] headerArr = header.getHeader();
		header = new OptranFileHeader(headerArr);

		assertEquals(pageSize, header.getPageSize());
		assertEquals(freeList, header.getFreeList());
		assertEquals(lastAllocatedPage, header.getLastAllocatedPage());
	}

	@Test
	public void testIncorrectHeaderSize() {
		try {
			OptranFileHeader header = new OptranFileHeader(null);
			fail();
		} catch (CorruptDiskException e) {
			assertEquals("Expected 100 bytes found null", e.getMessage());
		}
		try {
			OptranFileHeader header = new OptranFileHeader(new byte[99]);
			fail();
		} catch (CorruptDiskException e) {
			assertEquals("Expected 100 bytes found 99", e.getMessage());
		}
	}

	@Test
	public void testIncorrectHeaderDescription() {
		try {
			OptranFileHeader header = new OptranFileHeader(new byte[100]);
			fail();
		} catch (CorruptDiskException e) {
			assertEquals("File header descriptor is corrupt", e.getMessage());
		}
	}

	@Test
	public void testHeaderDirtyStatus() {
		OptranFileHeader header = null;

		header = new OptranFileHeader(512);
		assertTrue(header.isDirty());
		header.clean();
		assertFalse(header.isDirty());
		header.setPageSize(256);
		assertTrue(header.isDirty());
		header.clean();
		assertFalse(header.isDirty());
		
		header = new OptranFileHeader(512);
		assertTrue(header.isDirty());
		header.clean();
		assertFalse(header.isDirty());
		header.setFreeList(10);
		assertTrue(header.isDirty());
		header.clean();
		assertFalse(header.isDirty());
		
		header = new OptranFileHeader(512);
		assertTrue(header.isDirty());
		header.clean();
		assertFalse(header.isDirty());
		header.setLastAllocatedPage(10);
		assertTrue(header.isDirty());
		header.clean();
		assertFalse(header.isDirty());
	}
}
