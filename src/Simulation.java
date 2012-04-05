import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.geom.Vector2;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.util.Camera;

import megamu.mesh.*;

import java.util.Random;

public class Simulation {

	 private Graph g;

	 private float[][] points;

	 public Voronoi v;

	 public Simulation() {

		  this.g = new SingleGraph("roads");

		  View view = this.g.display(false).getDefaultView();
		  view.setBackLayerRenderer(new RenderingLayer(this));

		  Camera camera = view.getCamera();
		  camera.setGraphViewport(-1000, 0, 0, 0);

		  this.run();
	 }

	 public void run() {

		  int n = 1000;
		  this.points = new float[n][2];
		  for(int i = 0; i < n; ++i) {
				this.points[i] = new float[2];
				this.points[i][0] = (float)(Math.random() * 1000 - 500);
				this.points[i][1] = (float)(Math.random() * 1000 - 500);
		  }

		  this.v = new Voronoi(points);

		  for(int i = 0; i < points.length; ++i) {
				Node lot = g.addNode("" + i);
				lot.setAttribute("x", points[i][0]);
				lot.setAttribute("y", points[i][1]);
		  }
	 }

}
