package com.github.richygreat.springsagaevent.analyser;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import com.github.richygreat.springsagaevent.annotation.SagaBranchStart;
import com.github.richygreat.springsagaevent.annotation.SagaEnd;
import com.github.richygreat.springsagaevent.annotation.SagaEventHandler;
import com.github.richygreat.springsagaevent.annotation.SagaSideStep;
import com.github.richygreat.springsagaevent.annotation.SagaStart;
import com.github.richygreat.springsagaevent.annotation.SagaTransition;

public class AnalyseSagas {
	private Map<String, List<Annotation>> sagaAnnotations = new HashMap<>();

	public List<Class<?>> getSagaEventHandlerClasses(String pathToJar) throws IOException, ClassNotFoundException {
		List<Class<?>> sagaEventHandlers = new ArrayList<>();
		try (JarFile jarFile = new JarFile(pathToJar)) {
			Enumeration<JarEntry> e = jarFile.entries();

			URL[] urls = { new URL("jar:file:" + pathToJar + "!/") };
			URLClassLoader cl = URLClassLoader.newInstance(urls);

			while (e.hasMoreElements()) {
				JarEntry je = e.nextElement();
				if (je.isDirectory() || !je.getName().endsWith(".class")) {
					continue;
				}
				// -6 because of .class
				String className = je.getName().substring(0, je.getName().length() - 6);
				className = className.replace('/', '.');
				Class<?> c = cl.loadClass(className);
				if (c.isAnnotationPresent(SagaEventHandler.class)) {
					sagaEventHandlers.add(c);
				}
			}
		}
		return sagaEventHandlers;
	}

	public void paint() {
		try {
			String pathToJar = "/home/alourdusamy/eclipse-workspaces/study/micro-email/target/micro-email-1.0.0-SNAPSHOT.jar";
			List<Class<?>> sagaEventHandlers = getSagaEventHandlerClasses(pathToJar);
			pathToJar = "/home/alourdusamy/eclipse-workspaces/study/micro-loan-broker/target/micro-loan-broker-1.0.0-SNAPSHOT.jar";
			sagaEventHandlers.addAll(getSagaEventHandlerClasses(pathToJar));
			sagaEventHandlers.forEach(annotationExtractor);
			sagaAnnotations.forEach((saga, lsAnn) -> new GraphConsumer(saga, false).accept(lsAnn));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		AnalyseSagas analyseSagas = new AnalyseSagas();
		analyseSagas.paint();
	}

	Consumer<Class<?>> annotationExtractor = targetClass -> {
		ReflectionUtils.doWithMethods(targetClass, method -> {
			SagaStart sagaStart = AnnotationUtils.findAnnotation(method, SagaStart.class);
			SagaTransition sagaTransition = AnnotationUtils.findAnnotation(method, SagaTransition.class);
			SagaEnd sagaEnd = AnnotationUtils.findAnnotation(method, SagaEnd.class);
			SagaBranchStart sagaBranchStart = AnnotationUtils.findAnnotation(method, SagaBranchStart.class);
			SagaSideStep sagaSideStep = AnnotationUtils.findAnnotation(method, SagaSideStep.class);

			if (sagaStart != null) {
				sagaAnnotations.putIfAbsent(sagaStart.name(), new ArrayList<>());
				sagaAnnotations.get(sagaStart.name()).add(sagaStart);
			} else if (sagaTransition != null) {
				sagaAnnotations.putIfAbsent(sagaTransition.name(), new ArrayList<>());
				sagaAnnotations.get(sagaTransition.name()).add(sagaTransition);
			} else if (sagaEnd != null) {
				sagaAnnotations.putIfAbsent(sagaEnd.name(), new ArrayList<>());
				sagaAnnotations.get(sagaEnd.name()).add(sagaEnd);
			} else if (sagaBranchStart != null) {
				sagaAnnotations.putIfAbsent(sagaBranchStart.name(), new ArrayList<>());
				sagaAnnotations.get(sagaBranchStart.name()).add(sagaBranchStart);
			} else if (sagaSideStep != null) {
				sagaAnnotations.putIfAbsent(sagaSideStep.name(), new ArrayList<>());
				sagaAnnotations.get(sagaSideStep.name()).add(sagaSideStep);
			}
		});
	};
}
