package com.github.richygreat.springsagaevent.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import lombok.Value;

@Value
public class SagaEventHandlerType {
	private String sagaEvent;
	private Object bean;
	private Method method;
	private Annotation annotation;
}
