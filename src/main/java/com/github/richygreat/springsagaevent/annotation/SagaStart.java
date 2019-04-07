package com.github.richygreat.springsagaevent.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * First producer method of a Saga. This annotated method will trigger the first
 * event.
 * 
 * @author Richy
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SagaStart {
	String name();

	String initEvent();

	String triggerPoint();
}
