package com.github.optran.utils.disk.heap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.github.optran.utils.exceptions.IncorrectInitializationValueException;

@RunWith(JUnit4.class)
public class AllocHeaderTest {
	@Test
	public void testAllocHeader() {
		AllocHeader header = new AllocHeader();
		for (int i = 0; i < 268435456; i++) {
			testHeader(i);
		}
		try {
			testHeader(268435457);
			fail();
		} catch (IncorrectInitializationValueException e) {
			assertEquals(e.getMessage(), "The size in the alloc header cannot be more than 268435456");
		}
		System.out.println("Done");
	}
	private static void testHeader(final int size) {
		try {
			testHeader(size, false, false, false);
			testHeader(size, false, false, true);
			testHeader(size, false, true, false);
			testHeader(size, false, true, true);
			testHeader(size, true, false, false);
			testHeader(size, true, false, true);
			testHeader(size, true, true, false);
			testHeader(size, true, true, true);
		} catch (AssertionError e) {
			System.out.println("Failed for "+size);
			e.printStackTrace();
			System.exit(0);
		}
	}

	private static void testHeader(final int size, final boolean allocated, final boolean prevAllocated,
			final boolean extended) {
		AllocHeader header = null;
		byte[] data = null;
		header = new AllocHeader();
		header.setSize(size);
		header.setAllocated(allocated);
		header.setPrevAllocated(prevAllocated);
		header.setExtended(extended);
		data = header.toBytes();
		header = new AllocHeader();
		header.setBytes(data);
		assertTrue(allocated == header.isAllocated());
		assertTrue(prevAllocated == header.isPrevAllocated());
		assertTrue(extended == header.isExtended());
		assertTrue(size == header.getSize());
	}

	private void printbytes(byte[]data) {
		for (int i = 0; i < data.length; i++) {
			printByte(data[i]);
			System.out.print(" ");
		}
		System.out.println();
	}
	private static void printByte(byte b) {
		String str = Integer.toString(b&0xFF, 2);
		while(str.length()<8) {
			str = "0"+str;
		}
		System.out.print(str);
	}
}
