package com.disciples.feed.annotation;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Abstract base {@code @Configuration} class providing common structure
 * for enabling Feed's annotation-driven full text service capability.
 *
 * @author Ted Smith
 * @see EnableFullText
 */
@Configuration
public class AbstractFullTextConfiguration implements ImportAware {
	
	protected AnnotationAttributes enableFullText;

	@Override
	public void setImportMetadata(AnnotationMetadata importMetadata) {
		this.enableFullText = AnnotationAttributes.fromMap(importMetadata.getAnnotationAttributes(EnableFullText.class.getName(), false));
	}

}
