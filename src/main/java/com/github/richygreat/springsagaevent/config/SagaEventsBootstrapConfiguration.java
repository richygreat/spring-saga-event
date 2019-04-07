package com.github.richygreat.springsagaevent.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.richygreat.springsagaevent.annotation.SagaEventsListenerService;
import com.github.richygreat.springsagaevent.annotation.SagaStartAspect;

@Configuration
public class SagaEventsBootstrapConfiguration {
	@Value("${spring.application.name}")
	private String springApplicationName;

	@Value("${saga.events.concurrency:1}")
	private int maxConcurrency;

	@Value("${saga.events.max-retry:0}")
	private int maxRetryAttemptsCount;

	@Bean
	public AmqpAdmin amqpSagaAdmin(ConnectionFactory connectionFactory) {
		return new RabbitAdmin(connectionFactory);
	}

	@Bean
	public AmqpTemplate amqpSagaTemplate(ConnectionFactory connectionFactory) {
		return new RabbitTemplate(connectionFactory);
	}

	@Bean
	public TopicExchange sagaEventsExchange() {
		return new TopicExchange("SagaEvents");
	}

	@Bean
	public TopicExchange sagaEventsDlxExchange() {
		return new TopicExchange("SagaEventsDlx");
	}

	@Bean
	public Queue sagaEventsQueue() {
		Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put("x-dead-letter-exchange", "SagaEventsDlx");
		return new Queue(springApplicationName, true, false, false, arguments);
	}

	@Bean
	public Queue sagaEventsDlxQueue() {
		return new Queue(springApplicationName + "-dlq");
	}

	@Bean
	public SagaStartAspect sagaStartAspect(AmqpTemplate amqpSagaTemplate) {
		return new SagaStartAspect(amqpSagaTemplate);
	}

	@Bean
	public SagaEventsListenerService sagaEventsListenerService(AmqpTemplate amqpSagaTemplate, AmqpAdmin amqpSagaAdmin,
			TopicExchange sagaEventsExchange, Queue sagaEventsQueue, TopicExchange sagaEventsDlxExchange,
			Queue sagaEventsDlxQueue) {
		return new SagaEventsListenerService(amqpSagaTemplate, amqpSagaAdmin, sagaEventsExchange, sagaEventsQueue,
				sagaEventsDlxExchange, sagaEventsDlxQueue, maxRetryAttemptsCount);
	}

	@Bean
	public SimpleMessageListenerContainer sageEventsSimpleMessageListenerContainer(ConnectionFactory connectionFactory,
			Queue sagaEventsQueue, SagaEventsListenerService sagaEventsListenerService) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueues(sagaEventsQueue);
		container.setMessageListener(sagaEventsListenerService);
		container.setConcurrency(String.valueOf(maxConcurrency));
		return container;
	}
}
