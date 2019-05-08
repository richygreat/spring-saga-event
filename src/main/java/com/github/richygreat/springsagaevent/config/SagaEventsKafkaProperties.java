package com.github.richygreat.springsagaevent.config;

import java.util.HashMap;
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
		String servers = toConnectionString(new String[] { "velomobile-01.srvs.cloudkafka.com:9094",
				"velomobile-02.srvs.cloudkafka.com:9094", "velomobile-03.srvs.cloudkafka.com:9094" }, "9094");
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
		props.put("security.protocol", "SASL_SSL");
		props.put("sasl.mechanism", "SCRAM-SHA-256");
		props.put("sasl.jaas.config",
				"org.apache.kafka.common.security.scram.ScramLoginModule required username=\"li5jiphz\" password=\"nqa9_PsDVN5Szgm6cT7rZwNnTyOfZei6\";");
	}

	private String toConnectionString(String[] hosts, String defaultPort) {
		String[] fullyFormattedHosts = new String[hosts.length];
		for (int i = 0; i < hosts.length; i++) {
			if (hosts[i].contains(":") || StringUtils.isEmpty(defaultPort)) {
				fullyFormattedHosts[i] = hosts[i];
			} else {
				fullyFormattedHosts[i] = hosts[i] + ":" + defaultPort;
			}
		}
		return StringUtils.arrayToCommaDelimitedString(fullyFormattedHosts);
	}
}
