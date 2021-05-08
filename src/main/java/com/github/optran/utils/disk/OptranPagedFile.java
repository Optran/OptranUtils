package com.github.optran.utils.disk;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.github.optran.utils.exceptions.RuntimeIOException;
import com.github.optran.utils.pagedfile.Page;

class OptranPagedFile {
	private File target;
	private RandomAccessFile raf;
	private int pageSize;
	private byte[] rootPageData;
	private byte[] pageData;

	public OptranPagedFile(File target, int pageSize) {
		this.target = target;
		this.pageSize = pageSize;
		rootPageData = new byte[pageSize - 100];
		pageData = new byte[pageSize];
	}

	private void initRaf() {
		try {
			if (null != raf) {
				return;
			}
			raf = new RandomAccessFile(target, "rw");
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	public byte[] readHeader() {
		byte[] header = new byte[100];
		try {
			initRaf();
			raf.seek(0);
			raf.read(header);
			return header;
		} catch (Exception e) {
			throw new RuntimeIOException(e);
		}
	}

	public void writeHeader(byte[] header) {
		try {
			initRaf();
			raf.seek(0);
			raf.write(header);
		} catch (Exception e) {
			throw new RuntimeIOException(e);
		}
	}

	/**
	 * Reads and returns {@link Page} at the specified index from the disk.
	 * 
	 * @param pageNumber The index of the {@link Page} to be fetched.
	 * @return The {@link Page} at the specified index.
	 * @throws RuntimeIOException If an IO exception is thrown when interacting with
	 *                            the file layer.
	 */
	public byte[] readPage(long pageId) {
		try {
			initRaf();
			byte[] page = null;
			if (0 == pageId) {
				for (int i = 0; i < rootPageData.length; i++) {
					rootPageData[i] = 0;
				}
				raf.seek(100);
				raf.read(rootPageData);
				page = new byte[rootPageData.length];
				for (int i = 0; i < page.length; i++) {
					page[i] = rootPageData[i];
				}
			} else {
				for (int i = 0; i < pageData.length; i++) {
					pageData[i] = 0;
				}
				raf.seek(pageId * pageSize);
				raf.read(pageData);
				page = new byte[pageData.length];
				for (int i = 0; i < page.length; i++) {
					page[i] = pageData[i];
				}
			}
			return page;
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	/**
	 * Writes the {@link Page} that has been provided to the disk.
	 * 
	 * @param page The {@link Page} to be written.
	 * @throws RuntimeIOException If an IO exception is thrown when interacting with
	 *                            the file layer.
	 */
	public void writePage(long pageId, byte[] page) {
		try {
			initRaf();
			if (0 == pageId) {
				raf.seek(100);
			} else {
				raf.seek(pageId * pageSize);
			}
			raf.write(page);
		} catch (Exception e) {
			throw new RuntimeIOException(e);
		}
	}

	public void close() {
		if (null == raf) {
			return;
		}
		try {
			raf.close();
			raf = null;
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	public boolean delete() {
		close();
		return target.delete();
	}
}
