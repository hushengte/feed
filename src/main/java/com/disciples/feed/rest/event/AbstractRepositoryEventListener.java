package com.disciples.feed.rest.event;

import org.springframework.context.ApplicationListener;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import com.disciples.feed.rest.event.RepositoryEvent.Type;

/**
 * Abstract class that listens for generic {@link RepositoryEvent}s and dispatches them to a specific method based on the event type.
 */
public abstract class AbstractRepositoryEventListener<T> implements ApplicationListener<RepositoryEvent> {

	private final Class<?> entityType = GenericTypeResolver.resolveTypeArgument(getClass(), AbstractRepositoryEventListener.class);

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
	@Override
	@SuppressWarnings({ "unchecked" })
	public final void onApplicationEvent(RepositoryEvent event) {
		Class<?> srcType = event.getSource().getClass();
		if (null != entityType && !entityType.isAssignableFrom(srcType)) {
			return;
		}
		Type eventType = event.getEventType();
		Object source =  event.getSource();
		if (Type.BEFORE_SAVE == eventType) {
			onBeforeSave((T)source);
		} else if (Type.AFTER_SAVE == eventType) {
			onAfterSave((T)source);
		} else if (Type.BEFORE_DELETE == eventType) {
			onBeforeDelete((T)source);
		} else if (Type.AFTER_DELETE == eventType) {
			onAfterDelete((T)source);
		} else {
			QueryEventSource qes = (QueryEventSource)source;
			Page<T> result = onQuery(qes.getEntityType(), qes.getPageable(), qes.getParameters());
			qes.setResult(result);
		}
	}

	/**
	 * Override this method if you are interested in {@literal beforeSave} events.
	 * @param entity The entity being saved.
	 */
	protected void onBeforeSave(T entity) {}

	/**
	 * Override this method if you are interested in {@literal afterSave} events.
	 * @param entity The entity that was just saved.
	 */
	protected void onAfterSave(T entity) {}

	/**
	 * Override this method if you are interested in {@literal beforeDelete} events.
	 * @param entity The entity that is being deleted.
	 */
	protected void onBeforeDelete(T entity) {}

	/**
	 * Override this method if you are interested in {@literal afterDelete} events.
	 * @param entity The entity that was just deleted.
	 */
	protected void onAfterDelete(T entity) {}
	
	protected Page<T> onQuery(Class<?> entityType, Pageable pageable, MultiValueMap<String, Object> params) {
		return null;
	}

}
