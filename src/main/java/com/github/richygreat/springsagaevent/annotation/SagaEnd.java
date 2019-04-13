package com.github.richygreat.springsagaevent.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Final Event handler annotation. If this method succeeds then the saga is
 * complete
 * 
 * @author Richy
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SagaEnd {
	String name();

	String previousEvent();

	String finalOutcome();
}
