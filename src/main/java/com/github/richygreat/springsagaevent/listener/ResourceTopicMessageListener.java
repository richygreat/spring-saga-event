package com.github.richygreat.springsagaevent.listener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.richygreat.springsagaevent.annotation.SagaCompensationBranchStart;
import com.github.richygreat.springsagaevent.annotation.SagaTransition;
import com.github.richygreat.springsagaevent.config.SagaEventHandlerType;
import com.github.richygreat.springsagaevent.config.SagaEventsAnnotationUtil;
import com.github.richygreat.springsagaevent.support.SagaEventsKafkaConstants;

public class ResourceTopicMessageListener implements MessageListener<String, String> {
	private static final Logger log = LoggerFactory.getLogger(ResourceTopicMessageListener.class);
	private final Map<String, List<SagaEventHandlerType>> sagaEventHandlersMap;
	private final KafkaTemplate<String, String> kafkaTemplate;
	private final String topic;

	public ResourceTopicMessageListener(List<SagaEventHandlerType> sagaEventHandlerTypes,
			KafkaTemplate<String, String> kafkaTemplate, String topic) {
		super();
		this.sagaEventHandlersMap = sagaEventHandlerTypes.stream().collect(Collectors
				.groupingBy(SagaEventHandlerType::getSagaEvent, HashMap::new, Collectors.toCollection(ArrayList::new)));
		this.kafkaTemplate = kafkaTemplate;
		this.topic = topic;
	}

	@Override
	public void onMessage(ConsumerRecord<String, String> data) {
		Headers headers = data.headers();
		Header sagaEventHeader = headers.lastHeader(SagaEventsKafkaConstants.KAFKA_SAGA_EVENT_HEADER);
		if (sagaEventHeader == null || sagaEventHeader.value() == null || sagaEventHeader.value().length == 0) {
			// Valid scenario event is not present
			return;
		}
		String sagaEventHeaderValue = new String(sagaEventHeader.value());
		List<SagaEventHandlerType> sagaEventHandlerTypes = sagaEventHandlersMap.get(sagaEventHeaderValue);
		if (CollectionUtils.isEmpty(sagaEventHandlerTypes)) {
			// Valid scenario we are not interested in this SagaEvent
			return;
		}
		// Find a way to order the events and then execute one by one. If any one fails
		// then whole fails
		for (SagaEventHandlerType sagaEventHandlerType : sagaEventHandlerTypes) {
			Method method = sagaEventHandlerType.getMethod();
			Object obj = sagaEventHandlerType.getBean();
			try {
				Class<?> param0 = method.getParameterTypes()[0];
				Object ret = method.invoke(obj, new ObjectMapper().readValue(data.value(), param0));
				handleNextEvent(sagaEventHandlerType, data.key(), new ObjectMapper().writeValueAsString(ret));
			} catch (Exception e) {
				// If we have failureEvent configured we simply call it
				String failureEvent = SagaEventsAnnotationUtil.getFailureEvent(sagaEventHandlerType.getAnnotation());
				if (!StringUtils.isEmpty(failureEvent)) {
					log.error(String.format("onMessage: Failure with failureEventKey %s", failureEvent), e);
					kafkaTemplate.send(getProducerRecord(topic, data.key(), data.value(), failureEvent));
					return;
				}
			}
		}
	}

	private void handleNextEvent(SagaEventHandlerType sagaEventHandlerType, String key, String value) {
		if (!SagaTransition.class.equals(sagaEventHandlerType.getAnnotation().annotationType())
				&& !SagaCompensationBranchStart.class.equals(sagaEventHandlerType.getAnnotation().annotationType())) {
			return;
		}
		String eventName = null;
		if (SagaTransition.class.equals(sagaEventHandlerType.getAnnotation().annotationType())) {
			SagaTransition sagaTransition = (SagaTransition) sagaEventHandlerType.getAnnotation();
			eventName = sagaTransition.name() + "." + sagaTransition.nextEvent();
		} else {
			SagaCompensationBranchStart sagaCompensationBranchStart = (SagaCompensationBranchStart) sagaEventHandlerType
					.getAnnotation();
			eventName = sagaCompensationBranchStart.name() + "." + sagaCompensationBranchStart.initEvent();
		}
		kafkaTemplate.send(getProducerRecord(topic, key, value, eventName));
	}

	private ProducerRecord<String, String> getProducerRecord(String topic, String key, String value, String event) {
		ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, key, value);
		producerRecord.headers().add(SagaEventsKafkaConstants.KAFKA_SAGA_EVENT_HEADER, event.getBytes());
		return producerRecord;
	}
}
