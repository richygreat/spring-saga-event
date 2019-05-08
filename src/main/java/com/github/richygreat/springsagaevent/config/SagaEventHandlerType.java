package com.github.richygreat.springsagaevent.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.github.richygreat.springsagaevent.annotation.EventPayload;

public final class SagaEventHandlerType {
	private final String sagaEvent;
	private final Object bean;
	private final Method method;
	private final Annotation annotation;
	private final EventPayload eventPayload;

	public SagaEventHandlerType(String sagaEvent, Object bean, Method method, Annotation annotation,
			EventPayload eventPayload) {
		super();
		this.sagaEvent = sagaEvent;
		this.bean = bean;
		this.method = method;
		this.annotation = annotation;
		this.eventPayload = eventPayload;
	}

	public String getSagaEvent() {
		return sagaEvent;
	}

	public Object getBean() {
		return bean;
	}

	public Method getMethod() {
		return method;
	}

	public Annotation getAnnotation() {
		return annotation;
	}

	public EventPayload getEventPayload() {
		return eventPayload;
	}
}
