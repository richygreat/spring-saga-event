package com.github.richygreat.springsagaevent.annotation;

import java.lang.annotation.Annotation;

import org.springframework.util.StringUtils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SagaEventsAnnotationUtil {
	static String getFailureEvent(Annotation annotation) {
		if (SagaBranchStart.class.equals(annotation.annotationType())) {
			SagaBranchStart ann = (SagaBranchStart) annotation;
			if (StringUtils.isEmpty(ann.failureEvent())) {
				return null;
			}
			return ann.name() + "." + ann.failureEvent();
		} else if (SagaTransition.class.equals(annotation.annotationType())) {
			SagaTransition ann = (SagaTransition) annotation;
			if (StringUtils.isEmpty(ann.failureEvent())) {
				return null;
			}
			return ann.name() + "." + ann.failureEvent();
		} else if (SagaSideStep.class.equals(annotation.annotationType())) {
			SagaSideStep ann = (SagaSideStep) annotation;
			if (StringUtils.isEmpty(ann.failureEvent())) {
				return null;
			}
			return ann.name() + "." + ann.failureEvent();
		} else if (SagaEnd.class.equals(annotation.annotationType())) {
			SagaEnd ann = (SagaEnd) annotation;
			if (StringUtils.isEmpty(ann.failureEvent())) {
				return null;
			}
			return ann.name() + "." + ann.failureEvent();
		} else {
			return null;
		}
	}
}
