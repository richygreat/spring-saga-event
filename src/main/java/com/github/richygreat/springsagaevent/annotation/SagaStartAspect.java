package com.github.richygreat.springsagaevent.annotation;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.amqp.core.AmqpTemplate;

import lombok.RequiredArgsConstructor;

@Aspect
@RequiredArgsConstructor
public class SagaStartAspect {
	private final AmqpTemplate amqpSagaTemplate;

	@Around("@annotation(SagaStart)")
	public Object handleSagaStartEvent(ProceedingJoinPoint joinPoint) throws Throwable {
		Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
		SagaStart sagaStart = method.getAnnotation(SagaStart.class);
		Object ret = joinPoint.proceed();
		String eventName = sagaStart.name() + "." + sagaStart.initEvent();
		amqpSagaTemplate.convertAndSend("SagaEvents", eventName, ret);
		return ret;
	}
}
