package com.github.richygreat.springsagaevent.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.richygreat.springsagaevent.config.SagaEventsKafkaProperties;
import com.github.richygreat.springsagaevent.support.SagaEventsKafkaConstants;

@Aspect
public class SagaStartAspect {
	private final KafkaTemplate<String, String> kafkaTemplate;
	private final SagaEventsKafkaProperties sagaEventsKafkaProperties;

	public SagaStartAspect(KafkaTemplate<String, String> kafkaTemplate,
			SagaEventsKafkaProperties sagaEventsKafkaProperties) {
		super();
		this.kafkaTemplate = kafkaTemplate;
		this.sagaEventsKafkaProperties = sagaEventsKafkaProperties;
	}

	@Around("@annotation(SagaStart)")
	public Object handleSagaStartEvent(ProceedingJoinPoint joinPoint) throws Throwable {
		Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
		SagaStart sagaStart = method.getAnnotation(SagaStart.class);
		Object ret = joinPoint.proceed();
		EventPayload eventPayload = ret.getClass().getAnnotation(EventPayload.class);
		String topic = sagaEventsKafkaProperties.getKafkaResourcesPrefix() + eventPayload.topic();

		String event = sagaStart.name() + "." + sagaStart.initEvent();
		String key = null;
		String value = new ObjectMapper().writeValueAsString(ret);
		if (StringUtils.hasText(eventPayload.keyField())) {
			Field field = ret.getClass().getDeclaredField(eventPayload.keyField());
			field.setAccessible(true);
			key = String.valueOf(field.get(ret));
		}
		ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, key, value);
		producerRecord.headers().add(SagaEventsKafkaConstants.KAFKA_SAGA_EVENT_HEADER, event.getBytes());
		kafkaTemplate.send(producerRecord);
		return ret;
	}
}
