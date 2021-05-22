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

import java.util.Optional;

import org.apache.log4j.Logger;

import com.github.optran.utils.cache.CacheAction;
import com.github.optran.utils.cache.CacheEvictionListener;
import com.github.optran.utils.cache.CacheLoader;
import com.github.optran.utils.cache.LRUCache;
import com.github.optran.utils.exceptions.RuntimeIOException;

public class CachedPagedFile implements PagedFile {
	private static final Logger logger = Logger.getLogger(CachedPagedFile.class);
	private LRUCache<Long, Page> cache;
	private PagedFile pagedFile;

	public CachedPagedFile(PagedFile pagedFile, int cacheSize) {
		this.pagedFile = pagedFile;
		cache = new LRUCache<Long, Page>(cacheSize);
		cache.addEvictionListener(new CacheEvictionListener<Long, Page>() {
			@Override
			public void evict(Long pageNumber, Page page) {
				writePageToDisk(page);
			}
		});
		cache.setCacheLoader(new CacheLoader<Long, Page>() {
			@Override
			public Optional<Page> get(Long pageNumber) {
				Page page = pagedFile.readPage(pageNumber);
				return Optional.ofNullable(page);
			}
		});
	}

	@Override
	public Page readPage(long pageNumber) {
		return cache.get(pageNumber).get();
	}

	@Override
	public void writePage(Page page) {
		if (null == page) {
			return;
		}
		cache.put(page.getPageId(), page);
	}

	@Override
	public int getPageSize() {
		return pagedFile.getPageSize();
	}

	private void writePageToDisk(Page page) {
		try {
			if (page.isDirty()) {
				pagedFile.writePage(page);
				page.clean();
			}
		} catch (Exception e) {
			throw new RuntimeIOException("Error writing data to the disk.", e);
		}
	}

	@Override
	public void flush() {
		cache.perform(new CacheAction<Long, Page>() {
			@Override
			public void perform(Long pageNumber, Page page) {
				writePageToDisk(page);
			}
		});
		pagedFile.flush();
	}

	@Override
	public long length() {
		return pagedFile.length();
	}

	@Override
	public boolean exists() {
		return pagedFile.exists();
	}

	@Override
	public boolean close() {
		flush();
		return pagedFile.close();
	}
}
