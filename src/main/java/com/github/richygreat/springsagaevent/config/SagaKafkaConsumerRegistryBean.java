package com.github.richygreat.springsagaevent.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.kafka.core.KafkaTemplate;

public class SagaKafkaConsumerRegistryBean implements ApplicationContextAware, SmartInitializingSingleton {
	private final KafkaTemplate<String, String> sagaKafkaTemplate;
	private final SagaEventsKafkaProperties sagaEventsKafkaProperties;
	private ApplicationContext applicationContext;

	public SagaKafkaConsumerRegistryBean(SagaEventsKafkaProperties sagaEventsKafkaProperties,
			KafkaTemplate<String, String> sagaKafkaTemplate) {
		this.sagaEventsKafkaProperties = sagaEventsKafkaProperties;
		this.sagaKafkaTemplate = sagaKafkaTemplate;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterSingletonsInstantiated() {
		SagaEventHandlerBinder binder = new SagaEventHandlerBinder(applicationContext, sagaKafkaTemplate,
				sagaEventsKafkaProperties);
		binder.bind();
	}
}
