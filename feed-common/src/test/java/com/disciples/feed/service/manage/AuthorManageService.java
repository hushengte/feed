package com.disciples.feed.service.manage;

import org.springframework.stereotype.Service;

import com.disciples.feed.dao.AuthorDao;
import com.disciples.feed.domain.Author;
import com.disciples.feed.manage.AbstractJpaSimpleCrudService;

@Service
public class AuthorManageService extends AbstractJpaSimpleCrudService<Author, AuthorDao> {

	public AuthorManageService() {
		super(Author.class);
	}

}
