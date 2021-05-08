package com.github.optran.utils.disk.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.github.optran.utils.disk.OptranFile;

public class OptranBlock {
	private long pageId;
	private Map<String, String> metadata;
	private OptranFile disk;
	private byte[] data;

	public OptranBlock(OptranFile disk) {
		this(disk.malloc().getPageId(), disk);
	}

	public OptranBlock(Long pageId, OptranFile disk) {
		this.pageId = pageId;
		this.disk = disk;
		read();
	}

	private void read() {
		int length = 0;
		List<byte[]> readList = new LinkedList<>();
		long currentPageId = pageId;
		boolean hasNext = true;
		while (hasNext) {
			OptranFilePage page = disk.readPage(currentPageId);
			currentPageId = page.getNextPage();
			byte[] pageData = page.getData();
			length = length + pageData.length;
			readList.add(pageData);
		}
		data = new byte[length];
		int index = 0;
		for (byte[] pageData : readList) {
			for (int i = 0; i < pageData.length; i++) {
				data[index++] = pageData[i];
			}
		}
	}

	private void save() {
		int remaining = data.length;
		int index = 0;
		long currentPageId = pageId;

		OptranFilePage page = disk.readPage(currentPageId);
		while (remaining > 0) {
			page.setHead(0);
			int bytesToWrite = (page.capicity() > remaining) ? page.capicity() : remaining;
			for (int i = 0; i < bytesToWrite; i++) {
				page.write(data[index++]);
				remaining--;
			}
			for (int i = bytesToWrite; i < page.capicity(); i++) {
				page.write(0);
			}
			if (remaining > 0) {
				currentPageId = page.getNextPage();
				if (0 == currentPageId) {
					OptranFilePage nextPage = disk.malloc();
					currentPageId = nextPage.getPageId();
					page.setNextPage(currentPageId);
					disk.writePage(page);
					page = nextPage;
				} else {
					disk.writePage(page);
					page = disk.readPage(currentPageId);
				}
			}
			if (0 == remaining) {
				// Need to free up any excess pages
				currentPageId = page.getNextPage();
				if (0 != currentPageId) {
					page.setNextPage(0);
					disk.writePage(page);
					while (0 != currentPageId) {
						page = disk.readPage(currentPageId);
						currentPageId = page.getNextPage();
						disk.free(page);
					}
				}
			}
		}
	}

	/**
	 * @return the pageId
	 */
	public long getPageId() {
		return pageId;
	}

	/**
	 * @param pageId the pageId to set
	 */
	public void setPageId(long pageId) {
		this.pageId = pageId;
	}

	/**
	 * @return the data
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(byte[] data) {
		this.data = data;
		save();
	}
}
