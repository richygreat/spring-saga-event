package com.github.richygreat.springsagaevent.annotation;

import java.lang.annotation.Annotation;

import org.springframework.util.StringUtils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SagaEventsAnnotationUtil {
	static String getFailureEvent(Annotation annotation) {
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
