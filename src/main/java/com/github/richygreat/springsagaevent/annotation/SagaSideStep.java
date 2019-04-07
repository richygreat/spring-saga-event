package com.github.richygreat.springsagaevent.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Parallel step triggered by an event from Saga. No branching is possible in
 * this.
 * 
 * @author Richy
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SagaSideStep {
	String name();

	String previousEvent();

	String finalOutcome();

	String failureEvent() default "";
}
