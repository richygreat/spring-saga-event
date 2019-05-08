package com.github.richygreat.springsagaevent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class SagaEventsBootstrapConfiguration {
	@Bean
	public SagaEventsKafkaProperties sagaEventsKafkaProperties() {
		return new SagaEventsKafkaProperties();
	}

	@Bean
	public KafkaTemplate<String, String> sagaKafkaTemplate(SagaEventsKafkaProperties sagaEventsKafkaProperties) {
		ProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<String, String>(
				sagaEventsKafkaProperties.producerProperties());
		KafkaTemplate<String, String> template = new KafkaTemplate<String, String>(producerFactory);
		return template;
	}

	@Bean
	public SagaKafkaConsumerRegistryBean sagaKafkaConsumerRegistryBean(
			SagaEventsKafkaProperties sagaEventsKafkaProperties, KafkaTemplate<String, String> sagaKafkaTemplate) {
		return new SagaKafkaConsumerRegistryBean(sagaEventsKafkaProperties, sagaKafkaTemplate);
	}
}
