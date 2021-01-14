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

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.log4j.Logger;

import com.github.optran.utils.exceptions.IncorrectInitializationValueException;

public class LRUCachedPagedFile implements PagedFile {
	private static final Logger logger = Logger.getLogger(Test.class);
	private PagedFile pagedFile;
	private int cacheSize;
	private Map<Long, Page> cacheMap;
	private Queue<Page> lruQueue;

	public LRUCachedPagedFile(PagedFile pagedFile, int cacheSize) {
		if(cacheSize<=0) {
			throw new IncorrectInitializationValueException("Incorrect cache size value. cacheSize = "+cacheSize);
		}
		cacheMap = new HashMap<Long, Page>();
		lruQueue = new LinkedList<Page>();
		this.pagedFile = pagedFile;
		this.cacheSize = cacheSize;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Page readPage(long pageNumber) throws IOException {
		Page page = null;
		if(cacheMap.containsKey(pageNumber)) {
			page = cacheMap.get(pageNumber);
			updateCache(page);
			return page;
		}
		page = pagedFile.readPage(pageNumber);
		page = new RandomAccessPage(page.getPageId(), page.getData(), this);
		updateCache(page);
		return page;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writePage(Page page) throws IOException {
		if(null==page) {
			return;
		}
		updateCache(page);
	}

	private void updateCache(Page page) throws IOException {
		cacheMap.remove(page.getPageId());
		lruQueue.remove(page);
		cacheMap.put(page.getPageId(), page);
		lruQueue.add(page);
		logger.trace("Cache updated for page = "+page.getPageId());
		if(lruQueue.size()>cacheSize) {
			evictPage(lruQueue.poll());
		}
	}

	private void evictPage(Page page) throws IOException {
		if(page.isDirty()) {
			pagedFile.writePage(page);
			page.clean();
			logger.trace("Wrote page ("+page.getPageId()+") to disk.");
		}
		cacheMap.remove(page.getPageId());
		logger.trace("Evicted page = "+page.getPageId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void flush() throws IOException {
		for (Page page : lruQueue) {
			if(page.isDirty()) {
				pagedFile.writePage(page);
				page.clean();
				logger.trace("Wrote page ("+page.getPageId()+") to disk.");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean close() {
		try {
			flush();
			cacheMap.clear();
			lruQueue.clear();
			cacheMap = null;
			lruQueue = null;
			this.pagedFile = null;
			return pagedFile.close();
		} catch (IOException e) {
			return false;
		}
	}
}
