package com.github.richygreat.springsagaevent.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When a new Saga branches out of an existing Saga Event
 * 
 * @author Richy
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SagaCompensationBranchStart {
	String name();

	String branchoutSagaName();

	String branchoutEvent();

	String initEvent();
}
