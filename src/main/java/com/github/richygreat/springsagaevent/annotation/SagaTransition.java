package com.github.richygreat.springsagaevent.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Transitions are responsible to move the Saga forward
 * <p>
 * Given a previousEvent to which this method listens too and nextEvent which
 * this method publishes, the annotated method is perfect example of a
 * transition function
 * </p>
 * 
 * @author Richy
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SagaTransition {
	String name();

	String previousEvent();

	String nextEvent();

	String failureEvent() default "";
}
