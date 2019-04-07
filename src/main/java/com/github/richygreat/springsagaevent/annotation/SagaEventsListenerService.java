package com.github.richygreat.springsagaevent.annotation;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public class SagaEventsListenerService implements MessageListener, InitializingBean, ApplicationContextAware, Ordered {
	private final AmqpTemplate amqpSagaTemplate;
	private final AmqpAdmin amqpSagaAdmin;
	private final TopicExchange sagaEventsExchange;
	private final Queue sagaEventsQueue;
	private final TopicExchange sagaEventsDlxExchange;
	private final Queue sagaEventsDlxQueue;
	private final int maxRetryAttemptsCount;
	private ApplicationContext applicationContext;

	private Map<String, List<SagaEventHandlerType>> sagaEventHandlerMap;

	@Override
	public void onMessage(Message message) {
		List<String> routingKeys = getRoutingKeys(message);
		Optional<String> optionalSagaEvent = sagaEventHandlerMap.keySet().stream().filter(routingKeys::contains)
				.findFirst();
		if (!optionalSagaEvent.isPresent()) {
			return;
		}
		String sagaEvent = optionalSagaEvent.get();
		List<SagaEventHandlerType> sagaEventHandlerTypes = sagaEventHandlerMap.get(sagaEvent);
		if (sagaEventHandlerTypes != null) {
			for (SagaEventHandlerType sagaEventHandlerType : sagaEventHandlerTypes) {
				Method method = sagaEventHandlerType.getMethod();
				Object obj = sagaEventHandlerType.getBean();
				try {
					Object ret = method.invoke(obj, new String(message.getBody()));
					handleNextEvent(sagaEventHandlerType, ret);
				} catch (Exception e) {
					// If we have failureEvent configured we simply call it
					String failureEventKey = SagaEventsAnnotationUtil
							.getFailureEvent(sagaEventHandlerType.getAnnotation());
					if (!StringUtils.isEmpty(failureEventKey)) {
						log.error(String.format("onMessage: Failure with failureEventKey %s", failureEventKey), e);
						amqpSagaTemplate.send("SagaEvents", failureEventKey, message);
						return;
					}

					// If we don't have a failureEvent we retry the request until maxAttemptCount
					/**
					 * If the message fails and the attempt is under maxRetryAttemptCount then
					 * retry. It is called a retry because we push to the original queue. If the
					 * message fails and the attempt is over maxRetryAttemptCount then push to dlq.
					 */
					int attemptCount = getAttemptCount(message);
					if (maxRetryAttemptsCount <= attemptCount) {
						throw new AmqpRejectAndDontRequeueException(
								String.format("onMessage: Reenqueue stopped. currentAttempt: %d", attemptCount), e);
					}
					message.getMessageProperties().getHeaders().put("retry-count", ++attemptCount);
					log.error(String.format("onMessage: Reenqueue message. currentAttempt: %d", attemptCount), e);
					amqpSagaTemplate.send("SagaEvents", sagaEvent, message);
				}
			}
		}
	}

	private int getAttemptCount(Message message) {
		MessageProperties messageProperties = message.getMessageProperties();
		if (CollectionUtils.isEmpty(messageProperties.getHeaders())) {
			return 0;
		}
		Map<String, Object> headers = message.getMessageProperties().getHeaders();
		if (!headers.containsKey("retry-count")) {
			return 0;
		}
		return (Integer) headers.get("retry-count");
	}

	private List<String> getRoutingKeys(Message message) {
		MessageProperties messageProperties = message.getMessageProperties();
		if (CollectionUtils.isEmpty(messageProperties.getXDeathHeader())) {
			return Collections.singletonList(messageProperties.getReceivedRoutingKey());
		}
		List<Map<String, ?>> xDeath = message.getMessageProperties().getXDeathHeader();
		Optional<Map<String, ?>> optionalRoutingKeys = xDeath.stream().filter(map -> map.containsKey("routing-keys"))
				.findFirst();
		if (!optionalRoutingKeys.isPresent()) {
			return Collections.emptyList();
		}
		@SuppressWarnings("unchecked")
		List<String> routingKeys = (List<String>) optionalRoutingKeys.get().get("routing-keys");
		return routingKeys;
	}

	private void handleNextEvent(SagaEventHandlerType sagaEventHandlerType, Object ret) {
		if (SagaBranchStart.class.equals(sagaEventHandlerType.getAnnotation().annotationType())) {
			SagaBranchStart sagaBranchStart = (SagaBranchStart) sagaEventHandlerType.getAnnotation();
			String eventName = sagaBranchStart.name() + "." + sagaBranchStart.initEvent();
			amqpSagaTemplate.convertAndSend("SagaEvents", eventName, ret);
		} else if (SagaTransition.class.equals(sagaEventHandlerType.getAnnotation().annotationType())) {
			SagaTransition sagaTransition = (SagaTransition) sagaEventHandlerType.getAnnotation();
			String eventName = sagaTransition.name() + "." + sagaTransition.nextEvent();
			amqpSagaTemplate.convertAndSend("SagaEvents", eventName, ret);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		final SagaEventHandlerBinder sagaEventHandlerBinder = new SagaEventHandlerBinder(amqpSagaAdmin,
				sagaEventsExchange, sagaEventsQueue, sagaEventsDlxExchange, sagaEventsDlxQueue, applicationContext);
		sagaEventHandlerMap = sagaEventHandlerBinder.bind();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}
}
