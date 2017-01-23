package com.disciples.feed.service.manage;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.disciples.feed.dao.BookDao;
import com.disciples.feed.domain.Book;
import com.disciples.feed.manage.AbstractJpaSimpleCrudService;
import com.disciples.feed.utils.MapUtils;

@Service
public class BookManageService extends AbstractJpaSimpleCrudService<Book, BookDao> {

	public BookManageService() {
		super(Book.class);
	}

	@Override
	protected Page<Book> getPage(Pageable pageable, Map<String, Object> params) {
		Integer publisherId = MapUtils.getInt(params, "option");
		if (publisherId != null) {
			return getDao().findByPublisherId(publisherId, pageable);
		}
		return super.getPage(pageable, params);
	}

}
