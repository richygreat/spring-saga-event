package com.github.richygreat.springsagaevent.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class level annotation to mark a bean as SagaHandler
 * <p>
 * Without this marker the class won't be wired with Saga events
 * </p>
 * 
 * @author Richy
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SagaEventHandler {
}
