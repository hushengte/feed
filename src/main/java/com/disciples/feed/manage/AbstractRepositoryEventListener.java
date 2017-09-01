package com.disciples.feed.manage;

import static org.springframework.core.GenericTypeResolver.resolveTypeArgument;

import org.springframework.context.ApplicationListener;
import org.springframework.data.rest.core.event.AfterDeleteEvent;
import org.springframework.data.rest.core.event.AfterSaveEvent;
import org.springframework.data.rest.core.event.BeforeDeleteEvent;
import org.springframework.data.rest.core.event.BeforeSaveEvent;
import org.springframework.data.rest.core.event.RepositoryEvent;

/**
 * Abstract class that listens for generic {@link RepositoryEvent}s and dispatches them to a specific method based on the event type.
 */
public abstract class AbstractRepositoryEventListener<T> implements ApplicationListener<RepositoryEvent> {

	private final Class<?> INTERESTED_TYPE = resolveTypeArgument(getClass(), AbstractRepositoryEventListener.class);

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
	@Override
	@SuppressWarnings({ "unchecked" })
	public final void onApplicationEvent(RepositoryEvent event) {
		Class<?> srcType = event.getSource().getClass();
		if (null != INTERESTED_TYPE && !INTERESTED_TYPE.isAssignableFrom(srcType)) {
			return;
		}
		if (event instanceof BeforeSaveEvent) {
			onBeforeSave((T) event.getSource());
		} else if (event instanceof AfterSaveEvent) {
			onAfterSave((T) event.getSource());
		} else if (event instanceof BeforeDeleteEvent) {
			onBeforeDelete((T) event.getSource());
		} else if (event instanceof AfterDeleteEvent) {
			onAfterDelete((T) event.getSource());
		}
	}

	/**
	 * Override this method if you are interested in {@literal beforeSave} events.
	 * 
	 * @param entity The entity being saved.
	 */
	protected void onBeforeSave(T entity) {}

	/**
	 * Override this method if you are interested in {@literal afterSave} events.
	 * 
	 * @param entity The entity that was just saved.
	 */
	protected void onAfterSave(T entity) {}

	/**
	 * Override this method if you are interested in {@literal beforeDelete} events.
	 * 
	 * @param entity The entity that is being deleted.
	 */
	protected void onBeforeDelete(T entity) {}

	/**
	 * Override this method if you are interested in {@literal afterDelete} events.
	 * 
	 * @param entity The entity that was just deleted.
	 */
	protected void onAfterDelete(T entity) {}

}
