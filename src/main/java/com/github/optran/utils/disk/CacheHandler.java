package com.github.optran.utils.disk;

import java.util.Optional;

import com.github.optran.utils.cache.CacheAction;
import com.github.optran.utils.cache.CacheEvictionListener;
import com.github.optran.utils.cache.CacheLoader;
import com.github.optran.utils.disk.model.OptranFilePage;

class CacheHandler implements CacheLoader<Long, OptranFilePage>,
		CacheEvictionListener<Long, OptranFilePage>, CacheAction<Long, OptranFilePage> {
	private OptranPagedFile pagedFile;

	public CacheHandler(OptranPagedFile pagedFile) {
		this.pagedFile = pagedFile;
	}

	@Override
	public void perform(Long pageId, OptranFilePage page) {
		if (page.isDirty()) {
			pagedFile.writePage(pageId, page.getData());
			page.clean();
		}
	}

	@Override
	public void evict(Long pageId, OptranFilePage page) {
		pagedFile.writePage(pageId, page.getData());
	}

	@Override
	public Optional<OptranFilePage> get(Long pageId) {
		return Optional.of(new OptranFilePage(pageId, pagedFile.readPage(pageId)));
	}
}
