package com.github.richygreat.springsagaevent.annotation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class SagaEventHandlerBinder {
	private static final String INVALID_BINDING_METHOD_ARGS_LENGTH = "Only 1 parameter is allowed in Saga Listener methods";
	private static final String INVALID_BINDING_METHOD_RET_TYPE_VOID = "Return type should not be void for SagaTransition and SagaCompensationBranchStart methods";

	private final AmqpAdmin amqpSagaAdmin;
	private final TopicExchange sagaEventsExchange;
	private final Queue sagaEventsQueue;
	private final TopicExchange sagaEventsDlxExchange;
	private final Queue sagaEventsDlxQueue;
	private final ApplicationContext applicationContext;

	private Map<String, List<SagaEventHandlerType>> sagaEventHandlerMap = new HashMap<>();

	public Map<String, List<SagaEventHandlerType>> bind() {
		Map<String, Object> sagaEventHandlerBeanMap = applicationContext.getBeansWithAnnotation(SagaEventHandler.class);
		Collection<Object> sagaEventHandlerBeans = sagaEventHandlerBeanMap.values();
		if (CollectionUtils.isEmpty(sagaEventHandlerBeans)) {
			return sagaEventHandlerMap;
		}

		bindSagaTransition(sagaEventHandlerBeans);
		bindSagaEnd(sagaEventHandlerBeans);
		bindSagaCompensationBranchStart(sagaEventHandlerBeans);
		bindSagaSideStep(sagaEventHandlerBeans);
		bindSagaCompensationEnd(sagaEventHandlerBeans);

		return sagaEventHandlerMap;
	}

	private void bindSagaTransition(Collection<Object> beans) {
		Consumer<Object> amqpBindingConsumer = new AmqpSagaEventBindingConsumer(sagaEventHandlerMap, (bean, method) -> {
			SagaTransition sagaTransition = AnnotationUtils.findAnnotation(method, SagaTransition.class);
			if (sagaTransition != null) {
				Assert.isTrue(method.getParameterTypes().length == 1, INVALID_BINDING_METHOD_ARGS_LENGTH);
				Assert.isTrue(!method.getReturnType().equals(Void.TYPE), INVALID_BINDING_METHOD_RET_TYPE_VOID);
				String sagaEvent = sagaTransition.name() + "." + sagaTransition.previousEvent();
				return new SagaEventHandlerType(sagaEvent, bean, method, sagaTransition);
			}
			return null;
		}, amqpSagaAdmin, sagaEventsQueue, sagaEventsExchange, sagaEventsDlxQueue, sagaEventsDlxExchange);
		beans.forEach(amqpBindingConsumer);
	}

	private void bindSagaEnd(Collection<Object> beans) {
		Consumer<Object> amqpBindingConsumer = new AmqpSagaEventBindingConsumer(sagaEventHandlerMap, (bean, method) -> {
			SagaEnd sagaEnd = AnnotationUtils.findAnnotation(method, SagaEnd.class);
			if (sagaEnd != null) {
				Assert.isTrue(method.getParameterTypes().length == 1, INVALID_BINDING_METHOD_ARGS_LENGTH);
				String sagaEvent = sagaEnd.name() + "." + sagaEnd.previousEvent();
				return new SagaEventHandlerType(sagaEvent, bean, method, sagaEnd);
			}
			return null;
		}, amqpSagaAdmin, sagaEventsQueue, sagaEventsExchange, sagaEventsDlxQueue, sagaEventsDlxExchange);
		beans.forEach(amqpBindingConsumer);
	}

	private void bindSagaCompensationBranchStart(Collection<Object> beans) {
		Consumer<Object> amqpBindingConsumer = new AmqpSagaEventBindingConsumer(sagaEventHandlerMap, (bean, method) -> {
			SagaCompensationBranchStart sagaCompensationBranchStart = AnnotationUtils.findAnnotation(method,
					SagaCompensationBranchStart.class);
			if (sagaCompensationBranchStart != null) {
				Assert.isTrue(method.getParameterTypes().length == 1, INVALID_BINDING_METHOD_ARGS_LENGTH);
				Assert.isTrue(!method.getReturnType().equals(Void.TYPE), INVALID_BINDING_METHOD_RET_TYPE_VOID);
				String sagaEvent = sagaCompensationBranchStart.branchoutSagaName() + "."
						+ sagaCompensationBranchStart.branchoutEvent();
				return new SagaEventHandlerType(sagaEvent, bean, method, sagaCompensationBranchStart);
			}
			return null;
		}, amqpSagaAdmin, sagaEventsQueue, sagaEventsExchange, sagaEventsDlxQueue, sagaEventsDlxExchange);
		beans.forEach(amqpBindingConsumer);
	}

	private void bindSagaSideStep(Collection<Object> beans) {
		Consumer<Object> amqpBindingConsumer = new AmqpSagaEventBindingConsumer(sagaEventHandlerMap, (bean, method) -> {
			SagaSideStep sagaSideStep = AnnotationUtils.findAnnotation(method, SagaSideStep.class);
			if (sagaSideStep != null) {
				Assert.isTrue(method.getParameterTypes().length == 1, INVALID_BINDING_METHOD_ARGS_LENGTH);
				String sagaEvent = sagaSideStep.name() + "." + sagaSideStep.previousEvent();
				return new SagaEventHandlerType(sagaEvent, bean, method, sagaSideStep);
			}
			return null;
		}, amqpSagaAdmin, sagaEventsQueue, sagaEventsExchange, sagaEventsDlxQueue, sagaEventsDlxExchange);
		beans.forEach(amqpBindingConsumer);
	}

	private void bindSagaCompensationEnd(Collection<Object> beans) {
		Consumer<Object> amqpBindingConsumer = new AmqpSagaEventBindingConsumer(sagaEventHandlerMap, (bean, method) -> {
			SagaCompensationEnd sagaCompensationEnd = AnnotationUtils.findAnnotation(method, SagaCompensationEnd.class);
			if (sagaCompensationEnd != null) {
				Assert.isTrue(method.getParameterTypes().length == 1, INVALID_BINDING_METHOD_ARGS_LENGTH);
				String sagaEvent = sagaCompensationEnd.name() + "." + sagaCompensationEnd.previousEvent();
				return new SagaEventHandlerType(sagaEvent, bean, method, sagaCompensationEnd);
			}
			return null;
		}, amqpSagaAdmin, sagaEventsQueue, sagaEventsExchange, sagaEventsDlxQueue, sagaEventsDlxExchange);
		beans.forEach(amqpBindingConsumer);
	}

	@RequiredArgsConstructor
	private static class AmqpSagaEventBindingConsumer implements Consumer<Object> {
		private final Map<String, List<SagaEventHandlerType>> sagaEventHandlerMap;
		private final BiFunction<Object, Method, SagaEventHandlerType> sagaEventHandlerConvertor;
		private final AmqpAdmin amqpSagaAdmin;
		private final Queue sagaEventsQueue;
		private final TopicExchange sagaEventsExchange;
		private final Queue sagaEventsDlxQueue;
		private final TopicExchange sagaEventsDlxExchange;

		@Override
		public void accept(Object bean) {
			Class<?> targetClass = AopUtils.getTargetClass(bean);
			ReflectionUtils.doWithMethods(targetClass, method -> {
				SagaEventHandlerType type = sagaEventHandlerConvertor.apply(bean, method);
				if (type != null) {
					sagaEventHandlerMap.putIfAbsent(type.getSagaEvent(), new ArrayList<>());
					sagaEventHandlerMap.get(type.getSagaEvent()).add(type);
					amqpSagaAdmin.declareBinding(
							BindingBuilder.bind(sagaEventsQueue).to(sagaEventsExchange).with(type.getSagaEvent()));
					amqpSagaAdmin.declareBinding(BindingBuilder.bind(sagaEventsDlxQueue).to(sagaEventsDlxExchange)
							.with(type.getSagaEvent()));
				}
			});
		}
	}
}
