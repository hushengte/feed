package com.disciples.feed.rest;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.ResolvableType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import com.disciples.feed.rest.RepositoryEvent.Type;

/**
 * Abstract class that listens for generic {@link RepositoryEvent}s and dispatches them to a specific method based on the event type.
 */
public abstract class AbstractRepositoryListener<T> implements GenericApplicationListener {
    
    public static final int DEFAULT_ORDER = 0;
    
	private final Class<?> entityType = GenericTypeResolver.resolveTypeArgument(getClass(), AbstractRepositoryListener.class);

	@Override
    public boolean supportsEventType(ResolvableType eventType) {
        return eventType.resolve() == RepositoryEvent.class;
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return true;
    }

    /*
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		Type eventType = ((RepositoryEvent)event).getEventType();
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
			@SuppressWarnings("unchecked")
            T entity = (T)source;
			if (Type.BEFORE_SAVE == eventType) {
				onBeforeSave(entity);
			} else if (Type.AFTER_SAVE == eventType) {
				onAfterSave(entity);
			} else if (Type.BEFORE_DELETE == eventType) {
				onBeforeDelete(entity);
			} else if (Type.AFTER_DELETE == eventType) {
				onAfterDelete(entity);
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
