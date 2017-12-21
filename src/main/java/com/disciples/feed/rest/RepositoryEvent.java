package com.disciples.feed.rest;

import org.springframework.context.ApplicationEvent;

/**
 * RepositoryEvent class for events published by the RepositoryService.
 * @see com.disciples.feed.rest.RepositoryService
 */
public class RepositoryEvent extends ApplicationEvent {

	private static final long serialVersionUID = 2907380710910347615L;
	
	public static enum Type {BEFORE_SAVE, AFTER_SAVE, BEFORE_DELETE, AFTER_DELETE, READ};
	
	private Type eventType;

	public RepositoryEvent(QueryEventSource source) {
		super(source);
		this.eventType = Type.READ;
	}

	public RepositoryEvent(Object source, Type eventType) {
		super(source);
		this.eventType = eventType;
	}

	public Type getEventType() {
		return eventType;
	}
	
}
