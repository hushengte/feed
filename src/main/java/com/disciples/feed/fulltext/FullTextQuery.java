package com.disciples.feed.fulltext;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.ManyToOne;

import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Full-text query object.
 * @param <T> the type of the class which can be indexed.
 * 
 * @see com.disciples.feed.fulltext.FullTextService
 */
public class FullTextQuery<T> {
	
	private static final int DEFAULT_MAX_RESULTS = 10;
	/** Indexed doc class  */
	private Class<T> docClass;
	/** Search keyword */
	private String keyword;
	/** Search fields */
	private List<String> fields;
	/** Stored fields */
	private Set<String> projections;
	/** Association paths */
	private Set<String> associations;
	/** Page request */
	private Pageable pageable;
	/** Max returned results: when pageable is null, this field is effect */
	private int maxResults = DEFAULT_MAX_RESULTS;
	/** Flag of highlight */
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
			this.projections = new HashSet<String>();
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
		this.associations = new HashSet<String>();
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

	public Set<String> getProjections() {
		return projections;
	}

	public Set<String> getAssociations() {
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
