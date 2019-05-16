package com.github.richygreat.springsagaevent.config;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import com.github.richygreat.springsagaevent.annotation.EventPayload;
import com.github.richygreat.springsagaevent.annotation.SagaCompensationBranchStart;
import com.github.richygreat.springsagaevent.annotation.SagaCompensationEnd;
import com.github.richygreat.springsagaevent.annotation.SagaEnd;
import com.github.richygreat.springsagaevent.annotation.SagaEventHandler;
import com.github.richygreat.springsagaevent.annotation.SagaSideStep;
import com.github.richygreat.springsagaevent.annotation.SagaTransition;
import com.github.richygreat.springsagaevent.listener.SagaEventKafkaContainerCreator;

public final class SagaEventHandlerBinder {
	private final ApplicationContext applicationContext;
	private final KafkaTemplate<String, String> sagaKafkaTemplate;
	private final SagaEventsKafkaProperties sagaEventsKafkaProperties;

	private static final String INVALID_BINDING_METHOD_ARGS_TYPE = "Only EventPayload type is allowed in Saga Listener methods";
	private static final String INVALID_BINDING_METHOD_ARGS_LENGTH = "Only 1 parameter is allowed in Saga Listener methods";
	private static final String INVALID_BINDING_METHOD_RET_TYPE_VOID = "Return type should not be void for SagaTransition and SagaCompensationBranchStart methods";

	private Map<EventPayload, List<SagaEventHandlerType>> sagaEventHandlerMap = new HashMap<>();

	public SagaEventHandlerBinder(ApplicationContext applicationContext,
			KafkaTemplate<String, String> sagaKafkaTemplate, SagaEventsKafkaProperties sagaEventsKafkaProperties) {
		this.applicationContext = applicationContext;
		this.sagaKafkaTemplate = sagaKafkaTemplate;
		this.sagaEventsKafkaProperties = sagaEventsKafkaProperties;
	}

	public void bind() {
		Map<String, Object> sagaEventHandlerBeanMap = applicationContext.getBeansWithAnnotation(SagaEventHandler.class);
		Collection<Object> sagaEventHandlerBeans = sagaEventHandlerBeanMap.values();
		if (CollectionUtils.isEmpty(sagaEventHandlerBeans)) {
			return;
		}

		bindSagaTransition(sagaEventHandlerBeans);
		bindSagaEnd(sagaEventHandlerBeans);
		bindSagaCompensationBranchStart(sagaEventHandlerBeans);
		bindSagaSideStep(sagaEventHandlerBeans);
		bindSagaCompensationEnd(sagaEventHandlerBeans);

		SagaEventKafkaContainerCreator containerCreator = new SagaEventKafkaContainerCreator(sagaKafkaTemplate,
				sagaEventsKafkaProperties);
		sagaEventHandlerMap.forEach(containerCreator);
	}

	private void bindSagaTransition(Collection<Object> beans) {
		Consumer<Object> kafkaBindingConsumer = new KafkaSagaEventBindingConsumer(sagaEventHandlerMap,
				(bean, method) -> {
					SagaTransition ann = AnnotationUtils.findAnnotation(method, SagaTransition.class);
					if (ann != null) {
						validate(method);
						String sagaEvent = ann.name() + "." + ann.previousEvent();
						return new SagaEventHandlerType(sagaEvent, bean, method, ann,
								method.getParameterTypes()[0].getAnnotation(EventPayload.class));
					}
					return null;
				});
		beans.forEach(kafkaBindingConsumer);
	}

	private void bindSagaEnd(Collection<Object> beans) {
		Consumer<Object> kafkaBindingConsumer = new KafkaSagaEventBindingConsumer(sagaEventHandlerMap,
				(bean, method) -> {
					SagaEnd ann = AnnotationUtils.findAnnotation(method, SagaEnd.class);
					if (ann != null) {
						validate(method);
						String sagaEvent = ann.name() + "." + ann.previousEvent();
						return new SagaEventHandlerType(sagaEvent, bean, method, ann,
								method.getParameterTypes()[0].getAnnotation(EventPayload.class));
					}
					return null;
				});
		beans.forEach(kafkaBindingConsumer);
	}

	private void bindSagaCompensationBranchStart(Collection<Object> beans) {
		Consumer<Object> kafkaBindingConsumer = new KafkaSagaEventBindingConsumer(sagaEventHandlerMap,
				(bean, method) -> {
					SagaCompensationBranchStart ann = AnnotationUtils.findAnnotation(method,
							SagaCompensationBranchStart.class);
					if (ann != null) {
						validate(method);
						String sagaEvent = ann.branchoutSagaName() + "." + ann.branchoutEvent();
						return new SagaEventHandlerType(sagaEvent, bean, method, ann,
								method.getParameterTypes()[0].getAnnotation(EventPayload.class));
					}
					return null;
				});
		beans.forEach(kafkaBindingConsumer);
	}

	private void bindSagaSideStep(Collection<Object> beans) {
		Consumer<Object> kafkaBindingConsumer = new KafkaSagaEventBindingConsumer(sagaEventHandlerMap,
				(bean, method) -> {
					SagaSideStep ann = AnnotationUtils.findAnnotation(method, SagaSideStep.class);
					if (ann != null) {
						validate(method);
						String sagaEvent = ann.name() + "." + ann.previousEvent();
						return new SagaEventHandlerType(sagaEvent, bean, method, ann,
								method.getParameterTypes()[0].getAnnotation(EventPayload.class));
					}
					return null;
				});
		beans.forEach(kafkaBindingConsumer);
	}

	private void bindSagaCompensationEnd(Collection<Object> beans) {
		Consumer<Object> kafkaBindingConsumer = new KafkaSagaEventBindingConsumer(sagaEventHandlerMap,
				(bean, method) -> {
					SagaCompensationEnd ann = AnnotationUtils.findAnnotation(method, SagaCompensationEnd.class);
					if (ann != null) {
						validate(method);
						String sagaEvent = ann.name() + "." + ann.previousEvent();
						return new SagaEventHandlerType(sagaEvent, bean, method, ann,
								method.getParameterTypes()[0].getAnnotation(EventPayload.class));
					}
					return null;
				});
		beans.forEach(kafkaBindingConsumer);
	}

	private void validate(Method method) {
		Assert.isTrue(method.getParameterTypes().length == 1, INVALID_BINDING_METHOD_ARGS_LENGTH);
		Assert.isTrue(method.getParameterTypes()[0].isAnnotationPresent(EventPayload.class),
				INVALID_BINDING_METHOD_ARGS_TYPE);
		Assert.isTrue(!method.getReturnType().equals(Void.TYPE), INVALID_BINDING_METHOD_RET_TYPE_VOID);
	}

	public static class KafkaSagaEventBindingConsumer implements Consumer<Object> {
		private final Map<EventPayload, List<SagaEventHandlerType>> sagaEventHandlerMap;
		private final BiFunction<Object, Method, SagaEventHandlerType> sagaEventHandlerConvertor;

		public KafkaSagaEventBindingConsumer(Map<EventPayload, List<SagaEventHandlerType>> sagaEventHandlerMap,
				BiFunction<Object, Method, SagaEventHandlerType> sagaEventHandlerConvertor) {
			this.sagaEventHandlerMap = sagaEventHandlerMap;
			this.sagaEventHandlerConvertor = sagaEventHandlerConvertor;
		}

		@Override
		public void accept(Object bean) {
			Class<?> targetClass = AopUtils.getTargetClass(bean);
			ReflectionUtils.doWithMethods(targetClass, method -> {
				SagaEventHandlerType type = sagaEventHandlerConvertor.apply(bean, method);
				if (type != null) {
					EventPayload ann = method.getParameterTypes()[0].getAnnotation(EventPayload.class);
					sagaEventHandlerMap.putIfAbsent(ann, new ArrayList<>());
					sagaEventHandlerMap.get(ann).add(type);
				}
			});
		}
	}
}
