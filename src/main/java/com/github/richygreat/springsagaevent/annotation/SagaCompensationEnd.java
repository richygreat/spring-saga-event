package com.github.richygreat.springsagaevent.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Final step after failure event
 * 
 * @author Richy
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SagaCompensationEnd {
	String name();

	String previousEvent();

	String finalOutcome();
}
