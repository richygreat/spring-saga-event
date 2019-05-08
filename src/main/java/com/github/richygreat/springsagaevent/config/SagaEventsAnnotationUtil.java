package com.github.richygreat.springsagaevent.config;

import java.lang.annotation.Annotation;

import org.springframework.util.StringUtils;

import com.github.richygreat.springsagaevent.annotation.SagaTransition;

public final class SagaEventsAnnotationUtil {
	public static String getFailureEvent(Annotation annotation) {
		if (SagaTransition.class.equals(annotation.annotationType())) {
			SagaTransition ann = (SagaTransition) annotation;
			if (StringUtils.isEmpty(ann.failureEvent())) {
				return null;
			}
			return ann.name() + "." + ann.failureEvent();
		} else {
			return null;
		}
	}
}
