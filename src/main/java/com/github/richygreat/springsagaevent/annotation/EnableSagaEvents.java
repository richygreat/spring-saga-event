package com.github.richygreat.springsagaevent.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.github.richygreat.springsagaevent.config.SagaEventsBootstrapConfiguration;

/**
 * <p>
 * Enable Saga Events listener and producers
 * </p>
 * 
 * @author Richy
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(SagaEventsBootstrapConfiguration.class)
public @interface EnableSagaEvents {
}
