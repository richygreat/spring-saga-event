package com.github.richygreat.springsagaevent;

import java.util.UUID;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.richygreat.springsagaevent.annotation.EnableSagaEvents;
import com.github.richygreat.springsagaevent.model.UserDTO;
import com.github.richygreat.springsagaevent.support.SagaEventsKafkaConstants;

@SpringBootTest(classes = TestSaga.TestConfig.class)
@RunWith(SpringRunner.class)
public class TestSaga {
	@Autowired
	private KafkaTemplate<String, String> sagaKafkaTemplate;

	@Test
	public void test() throws JsonProcessingException, InterruptedException {
		UserDTO userDTO = new UserDTO();
		userDTO.setId(UUID.randomUUID().toString());
		userDTO.setName("richygreat");
		ProducerRecord<String, String> producerRecord = new ProducerRecord<>("li5jiphz-user", userDTO.getName(),
				new ObjectMapper().writeValueAsString(userDTO));
		producerRecord.headers().add(SagaEventsKafkaConstants.KAFKA_SAGA_EVENT_HEADER,
				"UserCreated.CreationRequested".getBytes());
		sagaKafkaTemplate.send(producerRecord);

		Thread.sleep(25000);
	}

	@Configuration
	@ComponentScan
	@EnableSagaEvents
	@EnableAutoConfiguration
	static class TestConfig {
	}
}
