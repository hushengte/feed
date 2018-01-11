package com.disciples.feed.rest;

import org.springframework.context.ApplicationListener;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import com.disciples.feed.rest.RepositoryEvent.Type;

/**
 * Abstract class that listens for generic {@link RepositoryEvent}s and dispatches them to a specific method based on the event type.
 */
public abstract class AbstractRepositoryListener<T> implements ApplicationListener<RepositoryEvent> {

	private final Class<?> entityType = GenericTypeResolver.resolveTypeArgument(getClass(), AbstractRepositoryListener.class);

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
	@Override
	@SuppressWarnings({ "unchecked" })
	public final void onApplicationEvent(RepositoryEvent event) {
		Type eventType = event.getEventType();
		Object source =  event.getSource();
		if (Type.READ == eventType) {
			QueryEventSource qes = (QueryEventSource)source;
			Page<?> result = onQuery(qes.getEntityType(), qes.getPageable(), qes.getParameters());
			if (result != null) {
				qes.setResult(result);
			}
		} else {
			Class<?> srcType = source.getClass();
			if (null != entityType && !entityType.isAssignableFrom(srcType)) {
				return;
			}
			if (Type.BEFORE_SAVE == eventType) {
				onBeforeSave((T)source);
			} else if (Type.AFTER_SAVE == eventType) {
				onAfterSave((T)source);
			} else if (Type.BEFORE_DELETE == eventType) {
				onBeforeDelete((T)source);
			} else if (Type.AFTER_DELETE == eventType) {
				onAfterDelete((T)source);
			}
		}
	}

	/**
	 * Override this method if you are interested in {@literal RepositoryEvent.Type.BEFORE_SAVE} events.
	 * @param entity The entity being saved.
	 */
	protected void onBeforeSave(T entity) {}

	/**
	 * Override this method if you are interested in {@literal RepositoryEvent.Type.AFTER_SAVE} events.
	 * @param entity The entity that was just saved.
	 */
	protected void onAfterSave(T entity) {}

	/**
	 * Override this method if you are interested in {@literal RepositoryEvent.Type.BEFORE_DELETE} events.
	 * @param entity The entity that is being deleted.
	 */
	protected void onBeforeDelete(T entity) {}

	/**
	 * Override this method if you are interested in {@literal RepositoryEvent.Type.AFTER_DELETE} events.
	 * @param entity The entity that was just deleted.
	 */
	protected void onAfterDelete(T entity) {}
	
	/**
	 * Override this method if you are interested in {@literal RepositoryEvent.Type.READ} events.
	 * @param entityType Entity class
	 * @param pageable Page request
	 * @param params Query parameters
	 * @return finded page data, can be null
	 */
	protected Page<?> onQuery(Class<?> entityType, Pageable pageable, MultiValueMap<String, Object> params) {
		return null;
	}

}
