import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.geom.Vector2;
import org.graphstream.ui.swingViewer.*;
import org.graphstream.ui.swingViewer.util.Camera;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.triangulate.VoronoiDiagramBuilder;

public class Simulation {

	 /**
	  * The "lots" graph containing every lot seeds as nodes. Its edges
	  * represent the neighborhood relationships between adjacent lots.
	  */
	 public Graph lots;

	 /**
	  * The "roads" graph containing every constructible routes based
	  * on the edges of the Voronoi diagram. Its nodes are possible
	  * crossroads.
	  */
	 public Graph roads;

	 /**
	  * Strategies are processes that are applied to the city at each
	  * iteration of the simulation. They are rules describing its
	  * evolution.
	  */
	 private ArrayList<AbstractStrategy> strategies;

	 private String lotsStyle = "node {size: 5px; fill-color: black;} edge {fill-color: black;}";

	 private View view;
	 private Camera camera;

	 private GeometryFactory geomFact;

	 public Simulation() {

		  this.lots = new SingleGraph("land lots");
		  this.lots.addAttribute("ui.stylesheet", lotsStyle);

		  this.roads = new SingleGraph("road network");

		  this.strategies = new ArrayList<AbstractStrategy>();

		  this.geomFact = new GeometryFactory();

		  this.initialize();

		  // Set up the view.

		  System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		  this.lots.addAttribute("ui.antialias");

		  this.view = this.lots.display(false).getDefaultView();
		  this.view.setBackLayerRenderer(new BackgroundLayer(this));
		  this.view.setMouseManager(new MouseManager(this));

		  this.camera = this.view.getCamera();
		  this.camera.setGraphViewport(-2000, 0, 0, 0);
	 }

	 private void initialize() {

		  // Compute n random coordinates.
		  Coordinate[] coords = getRandomCoords(100, 500);

		  // Build a Voronoi diagram for which seeds are the previously
		  // computed coordinates.
		  Geometry voronoi = CityOps.voronoiDiagram(coords, true);

		  // Build the "lots" and "roads" graphs using the coordinates
		  // and the Voronoi Diagram.
		  LotOps.buildLotsGraph(coords, voronoi, this.lots);
		  RoadOps.buildRoadsGraph(voronoi, this.roads);
	 }

	 int s = 0;
	 public void run() {

		  // Save a screenshot.
		  //this.lots.addAttribute("ui.screenshot", "../screenshot.png");

		  this.strategies.add(new AverageDensityStrategy(this));
		  this.strategies.add(new LotPositioningStrategy(this));

		  for(int i = 0; i < 0; ++i) {

				for(AbstractStrategy strategy : this.strategies)
					 strategy.update();

				redraw();
				pause(1000);
		  }
	 }

	 /**
	  * Generate `lotCount` geometrical coordinates with X and Y values
	  * within [-`offset`, +`offset`].
	  */
	 private Coordinate[] getRandomCoords(int n, int offset) {

		  Coordinate[] coords = new Coordinate[n];

		  int offset2 = offset * 2;

		  for(int i = 0; i < coords.length; ++i) {

				// Choose a random position.
				float x = (float)(Math.random() * offset2 - offset);
				float y = (float)(Math.random() * offset2 - offset);

				coords[i] = new Coordinate(x, y);
		  }

		  return coords;
	 }

	 /*************************
	  *
	  * Some utility functions.
	  *
	  *************************/

	 public double[] px2gu(double xPx, double yPx) {

		  double ratio = this.camera.getMetrics().ratioPx2Gu;

		  double xGu = xPx / ratio;
		  double yGu = yPx / ratio;

		  return new double[]{xGu, yGu};
	 }

	 private void redraw() {

		  this.lots.addAttribute("ui.repaint");
	 }

	 private void pause(int ms) {

		  try {
				Thread.sleep(1000);
		  }
		  catch(Exception e) {
				e.printStackTrace();
		  }
	 }
}
