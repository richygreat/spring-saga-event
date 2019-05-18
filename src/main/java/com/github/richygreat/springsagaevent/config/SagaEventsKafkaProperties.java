package com.github.richygreat.springsagaevent.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

public class SagaEventsKafkaProperties {
	@Value("${spring.application.name}")
	private String springApplicationName;

	@Value("${saga.kafka.resources-prefix:}")
	private String kafkaResourcesPrefix;

	@Value("${spring.kafka.bootstrap-servers:localhost:9092}")
	private List<String> bootstrapServers;

	@Value("${spring.kafka.properties.security.protocol:}")
	private String securityProtocol;

	@Value("${spring.kafka.properties.sasl.mechanism:}")
	private String saslMechanism;

	@Value("${spring.kafka.properties.sasl.jaas.config:}")
	private String jaasConfig;

	@PostConstruct
	public void init() {
		if (StringUtils.hasText(kafkaResourcesPrefix)) {
			kafkaResourcesPrefix = kafkaResourcesPrefix + "-";
		}
	}

	public Map<String, Object> producerProperties() {
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(ProducerConfig.RETRIES_CONFIG, 0);
		props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
		props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
		props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

		populateKafkaProperties(props);
		return props;
	}

	public Map<String, Object> consumerProperties() {
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
		props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 100);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaResourcesPrefix + springApplicationName);

		populateKafkaProperties(props);
		return props;
	}

	public String getSpringApplicationName() {
		return springApplicationName;
	}

	public String getKafkaResourcesPrefix() {
		return kafkaResourcesPrefix;
	}

	private void populateKafkaProperties(Map<String, Object> props) {
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		if (StringUtils.hasText(securityProtocol)) {
			props.put("security.protocol", securityProtocol);
		}
		if (StringUtils.hasText(saslMechanism)) {
			props.put("sasl.mechanism", saslMechanism);
		}
		if (StringUtils.hasText(jaasConfig)) {
			props.put("sasl.jaas.config", jaasConfig);
		}
	}
}
