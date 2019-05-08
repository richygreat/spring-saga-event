package com.github.richygreat.springsagaevent.listener;

import java.util.List;
import java.util.function.BiConsumer;

import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

import com.github.richygreat.springsagaevent.annotation.EventPayload;
import com.github.richygreat.springsagaevent.config.SagaEventHandlerType;
import com.github.richygreat.springsagaevent.config.SagaEventsKafkaProperties;

public class SagaEventKafkaContainerCreator implements BiConsumer<EventPayload, List<SagaEventHandlerType>> {
	private final KafkaTemplate<String, String> kafkaTemplate;
	private final SagaEventsKafkaProperties sagaEventsKafkaProperties;

	public SagaEventKafkaContainerCreator(KafkaTemplate<String, String> kafkaTemplate,
			SagaEventsKafkaProperties sagaEventsKafkaProperties) {
		this.kafkaTemplate = kafkaTemplate;
		this.sagaEventsKafkaProperties = sagaEventsKafkaProperties;
	}

	@Override
	public void accept(EventPayload eventPayload, List<SagaEventHandlerType> sagaEventHandlerTypes) {
		String topic = sagaEventsKafkaProperties.getKafkaResourcesPrefix() + eventPayload.topic();
		ContainerProperties containerProperties = new ContainerProperties(topic);
		containerProperties
				.setMessageListener(new ResourceTopicMessageListener(sagaEventHandlerTypes, kafkaTemplate, topic));

		DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(
				sagaEventsKafkaProperties.consumerProperties());
		ConcurrentMessageListenerContainer<String, String> consumer = new ConcurrentMessageListenerContainer<>(
				consumerFactory, containerProperties);
		consumer.setConcurrency(5);
		consumer.start();
	}
}
