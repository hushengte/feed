package com.disciples.feed.manage;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.SpringDataWebConfiguration;

@Configuration
public class ManageWebConfiguration extends SpringDataWebConfiguration {

	@Bean
	public ManageService manageService() {
		return new ManageService();
	}
	
}
