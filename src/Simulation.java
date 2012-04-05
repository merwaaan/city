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

	 private float[][] points = {
		  {-100, 0},
		  {100, 0},
		  {0, -50},
		  {10, 300},
		  {150, -50},
		  {10, 100},
		  {-300, 300}
	 };

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

		  this.v = new Voronoi(points);

		  for(int i = 0; i < points.length; ++i) {
				Node n = g.addNode("" + i);
				n.setAttribute("x", points[i][0]);
				n.setAttribute("y", points[i][1]);
		  }
	 }

}
