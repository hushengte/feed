package com.disciples.feed.service.manage;

import org.springframework.stereotype.Service;

import com.disciples.feed.dao.PublisherDao;
import com.disciples.feed.domain.Publisher;
import com.disciples.feed.manage.AbstractJpaSimpleCrudService;

@Service
public class PublisherManageService extends AbstractJpaSimpleCrudService<Publisher, PublisherDao> {

	public PublisherManageService() {
		super(Publisher.class);
	}

}
