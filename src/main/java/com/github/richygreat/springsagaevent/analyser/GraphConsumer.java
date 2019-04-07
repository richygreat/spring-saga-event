package com.github.richygreat.springsagaevent.analyser;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.github.richygreat.springsagaevent.annotation.SagaBranchStart;
import com.github.richygreat.springsagaevent.annotation.SagaEnd;
import com.github.richygreat.springsagaevent.annotation.SagaSideStep;
import com.github.richygreat.springsagaevent.annotation.SagaStart;
import com.github.richygreat.springsagaevent.annotation.SagaTransition;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.util.mxCellRenderer;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
public class GraphConsumer implements Consumer<List<Annotation>> {
	private final String sagaName;
	private final boolean mainSagaRouteOnly;

	@Override
	public void accept(List<Annotation> annList) {
		DefaultDirectedGraph<String, DefaultEdge> g = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

		List<String> mainRoute = new ArrayList<>();
		List<SagaFunction> lsSagaFunctions = new ArrayList<>();
		annList.forEach(ann -> {
			if (ann.annotationType().equals(SagaStart.class)) {
				SagaStart saga = (SagaStart) ann;
				g.addVertex(saga.triggerPoint());
				lsSagaFunctions.add(new SagaFunction(saga.triggerPoint(), saga.initEvent()));
				mainRoute.add(saga.initEvent());
			} else if (!mainSagaRouteOnly && ann.annotationType().equals(SagaBranchStart.class)) {
				SagaBranchStart saga = (SagaBranchStart) ann;
				g.addVertex(saga.branchoutEvent());
				lsSagaFunctions.add(new SagaFunction(saga.branchoutEvent(), saga.initEvent()));
			} else if (ann.annotationType().equals(SagaTransition.class)) {
				SagaTransition saga = (SagaTransition) ann;
				lsSagaFunctions.add(new SagaFunction(saga.previousEvent(), saga.nextEvent()));
				mainRoute.add(saga.nextEvent());
			} else if (!mainSagaRouteOnly && ann.annotationType().equals(SagaSideStep.class)) {
				SagaSideStep saga = (SagaSideStep) ann;
				lsSagaFunctions.add(new SagaFunction(saga.previousEvent(), saga.finalOutcome()));
			} else if (ann.annotationType().equals(SagaEnd.class)) {
				SagaEnd saga = (SagaEnd) ann;
				lsSagaFunctions.add(new SagaFunction(saga.previousEvent(), saga.finalOutcome()));
				mainRoute.add(saga.finalOutcome());
			}
		});

		lsSagaFunctions.forEach(fun -> {
			g.addVertex(fun.getNext());
		});

		lsSagaFunctions.forEach(fun -> {
			g.addEdge(fun.getPrevious(), fun.getNext());
		});

		JGraphXAdapter<String, DefaultEdge> graphAdapter = new JGraphXAdapter<String, DefaultEdge>(g) {
		};
		mxCompactTreeLayout layout = new mxCompactTreeLayout(graphAdapter, false);
		layout.setNodeDistance(100);
		layout.execute(graphAdapter.getDefaultParent());

		BufferedImage image = mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, true, null);
		File imgFile = new File("src/test/resources/" + sagaName + ".png");
		try {
			ImageIO.write(image, "PNG", imgFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Value
	private static class SagaFunction {
		private String previous;
		private String next;
	}
}
