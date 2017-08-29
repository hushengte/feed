package com.disciples.feed.fulltext;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.ManyToOne;

import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * 全文检索查询对象
 * @param <T> 文档类
 */
public class FullTextQuery<T> {
	
	private static final int DEFAULT_MAX_RESULTS = 10;
	/** 文档类  */
	private Class<T> docClass;
	/** 关键字 */
	private String keyword;
	/** 查询域 */
	private List<String> fields;
	/** 存储域 */
	private List<String> projections;
	/** 关联字段 */
	private List<String> associations;
	/** 分页请求 */
	private Pageable pageable;
	/** 最大返回结果数：当pageable为null时起作用 */
	private int maxResults = DEFAULT_MAX_RESULTS;
	/** 高亮标记 */
	private boolean highlight;

	private FullTextQuery(Class<T> docClass, String keyword) {
		Assert.notNull(docClass, "docClass must not be null.");
		this.docClass = docClass;
		this.keyword = keyword;
	}
	
	public static <T> FullTextQuery<T> create(Class<T> docClass, String keyword) {
		return new FullTextQuery<T>(docClass, keyword);
	}
	
	public FullTextQuery<T> withFields(String... fields) {
		Assert.notEmpty(fields, "fields must not be empty");
		this.fields = new ArrayList<String>();
		for (String field : fields) {
			if (StringUtils.hasText(field)) {
				this.fields.add(field);
			}
		}
		return this;
	}
	
	public FullTextQuery<T> addProjections(String... projections) {
		if (this.projections == null) {
			this.projections = new ArrayList<String>();
		}
		if (projections != null) {
			for (String projection : projections) {
				if (StringUtils.hasText(projection)) {
					this.projections.add(projection);
				}
			}
		}
		return this;
	}
	
	private boolean validate(String association) {
		PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(docClass, association);
		if (pd != null && (AnnotationUtils.getAnnotation(pd.getReadMethod(), ManyToOne.class) != null || 
				AnnotationUtils.getAnnotation(pd.getWriteMethod(), ManyToOne.class) != null)) {
			return true;
		}
		Field field = ReflectionUtils.findField(docClass, association);
		return field != null && AnnotationUtils.getAnnotation(field, ManyToOne.class) != null;
	}
	
	public FullTextQuery<T> withAssociations(String... associations) {
		List<String> associationList = associations != null ? Arrays.asList(associations) : Collections.<String>emptyList();
		this.associations = new ArrayList<String>();
		for (String association : associationList) {
			if (validate(association)) {
				this.associations.add(association);
			}
		}
		return this;
	}
	
	public FullTextQuery<T> setPageRequest(Pageable pageable) {
		this.pageable = pageable;
		return this;
	}
	
	public FullTextQuery<T> setMaxResults(int maxResults) {
		this.maxResults = maxResults > 0 ? maxResults : DEFAULT_MAX_RESULTS;
		return this;
	}
	
	public FullTextQuery<T> setHighlight(boolean highlight) {
		this.highlight = highlight;
		return this;
	}

	public Class<T> getDocClass() {
		return docClass;
	}

	public String getKeyword() {
		return keyword;
	}

	public List<String> getFields() {
		return fields;
	}

	public List<String> getProjections() {
		return projections;
	}

	public List<String> getAssociations() {
		return associations;
	}

	public Pageable getPageable() {
		return pageable;
	}

	public int getMaxResults() {
		return maxResults;
	}

	public boolean isHighlight() {
		return highlight;
	}
	
}
