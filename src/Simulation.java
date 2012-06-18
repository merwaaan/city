import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.triangulate.VoronoiDiagramBuilder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;

import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.geom.Vector2;
import org.graphstream.ui.swingViewer.*;
import org.graphstream.ui.swingViewer.util.Camera;

public class Simulation {

	 /**
	  * The "lots" graph containing every lot seeds as nodes. Its edges
	  * represent the neighborhood relationships between adjacent lots.
	  */
	 public Graph lots;

	 public int width;

	 public List<Coordinate> lotCoords;

	 /**
	  * The "roads" graph containing every constructible routes based
	  * on the edges of the Voronoi diagram. Its nodes are possible
	  * crossroads.
	  */
	 public Graph roads;

	 /**
	  * A pivot is used to associate a lot with the surrounding
	  * crossroads and, conversely, a crossroad with the surrounding
	  * lots.
	  */
	 public Map<Node, CrossroadPivot> pivots;

	 /**
	  * Strategies are processes that are applied to the city at each
	  * iteration of the simulation. They are rules describing its
	  * evolution.
	  */
	 public HashMap<String, Strategy> strategies;

	 /**
	  * Display options.
	  *
	  * `showPotentialLots` enables the drawing of lots that have not
	  * been built yet.
	  *
	  * `showWhichVectorField` is the index of the displayed vector
	  * field. If its value is -1, no fields are displayed. If its
	  * value is -2 the summed up vector field is displayed.
	  *
	  * `drawTrueRoads' enables the drawing of the original road
	  * network if the urban data comes from a shapefile.
	  */
	 public boolean showPotentialLots = true;
	 public boolean showLargeCells = false;
	 public int showWhichVectorField = -1;
	 public boolean drawTrueRoads = false;

	 public List<List<Vector2>> paths;

	 private String lotsStyle = "node {fill-mode: none; size: 5px;} edge {visibility-mode: hidden;}";

	 /**
	  * Current time.
	  */
	 public long now;
	 private long lastStep;

	 // Dirty shortcuts, not enough time to do this cleanly!
	 public PotentialLotStrategy PLS;
	 public List<Obstacle> obstacles;
	 public List<Object[]> mayHaveRoads;
	 public Map<Coordinate, Density> shpDensities;
	 public List<LineString> trueRoad;

	 /**
	  * Minimum delay between each update.
	  */
	 private long stepDuration = 400;
	 private int step = 0;

	 private View view;
	 private Camera camera;

	 public GeometryFactory geomFact;

	 public Random rnd;

	 public Simulation() {

		  // Land lots.

		  this.lots = new SingleGraph("land lots");
		  this.lots.addAttribute("ui.stylesheet", this.lotsStyle);

		  this.lotCoords = null;

		  // Roads.

		  this.roads = new SingleGraph("road network");

		  this.pivots = new HashMap<Node, CrossroadPivot>();

		  // Strategies.

		  this.strategies = new HashMap<String, Strategy>();

		  // Misc.

		  this.paths = new ArrayList<List<Vector2>>();

		  this.rnd = new Random(110186123);

		  this.geomFact = new GeometryFactory();

		  this.initialize();

		  // Set up the view.

		  System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		  this.lots.addAttribute("ui.antialias");

		  this.view = this.lots.display(false).getDefaultView();
		  this.view.resizeFrame(800, 800);
		  this.view.setBackLayerRenderer(new BackgroundLayer(this));
		  this.view.setMouseManager(new MouseManager(this));

		  this.camera = this.view.getCamera();
	 }

	 private void initialize() {

		  //randomCoords(2500, 2000);

		  //radialCoords(600, 500);

		  ShapeFileLoader shpLoader = new ShapeFileLoader(this);
		  shpLoader.load("data/le_havre.shp", this);

		  // Build a Voronoi diagram for which seeds are the previously
		  // computed coordinates.
		  Geometry voronoi = LotOps.voronoiDiagram(this.lotCoords);

		  // Build the two graphs based on the diagram.
		  LotOps.buildLotsGraph(voronoi, this);
		  RoadOps.buildRoadsGraph(voronoi, this);

		  // Build the road from the shape file.
		  for(Edge e : this.roads.getEachEdge()) {

				Node c0 = e.getNode0();
				Node c1 = e.getNode1();

				double c0x = (Double)c0.getAttribute("x");
				double c0y = (Double)c0.getAttribute("y");
				double c1x = (Double)c1.getAttribute("x");
				double c1y = (Double)c1.getAttribute("y");
				Coordinate[] c0c1  = {new Coordinate(c0x, c0y), new Coordinate(c1x, c1y)};

				LineString line = this.geomFact.createLineString(c0c1);

				for(LineString ls : this.trueRoad)
					 if(line.crosses(ls)) {
						  RoadOps.buildRoad(e);
						  break;
					 }
		  }

		  // Apply the density from the shape file.
		  Coordinate o = new Coordinate(0, 0);
		  if(this.shpDensities != null)
				for(Node lot : this.lots) {

					 double x = (Double)lot.getAttribute("x");
					 double y = (Double)lot.getAttribute("y");
					 Coordinate c = new Coordinate(x, y);

					 Density d = this.shpDensities.get(c);
					 if(d != null) {

						  if(c.distance(o) > 1000)
								if(this.rnd.nextDouble() < 0.03)
									 d = Density.MEDIUM;
								else
									 d = Density.LOW;

						  lot.setAttribute("density", d);
						  LotOps.buildLot(lot);
					 }
				}
	 }

	 public void run() {

		  // Choose appropriate strategies.
		  this.strategies.put("cellular automata", new DensityStrategy(this));
		  this.strategies.put("road development", new RoadStrategy(4, this));
		  this.strategies.put("lot construction", new LotStrategy(0.5, this));
		  this.strategies.put("potential lot construction", new PotentialLotStrategy(0.3, this));
		  screenshot();
		  while(true) {

				this.now = System.currentTimeMillis();

				if(this.now - this.lastStep > this.stepDuration) {

					 for(Strategy strategy : this.strategies.values())
						  strategy.update();

					 this.lastStep = now;

					 ++this.step;

					 System.out.print(this.step+" ");
					 DecimalFormat f = new DecimalFormat("################");
					 //System.out.println(Measure.averageBetweennessCentrality(this));

					 if(this.step >= 500) {

						  /*
						  List<double[]> records = Measure.degreeDistance(this);

						  double s = 150;
						  double max = 3500;

						  for(double d = 0; d < max; d += s) {

								int n = 0;
								double total = 0;

								for(double[] r : records) {
									 int dist = (int)r[0];
									 if(dist < d && dist > d - s) {
										  total += (int)r[1];
										  ++n;
									 }
								}
								System.out.println(d + " " + (n > 0 ? total / n : 0));
						  }
						  */
					 return;
				}

				redraw();
				screenshot();
		  }
	 }
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
		  Thread.sleep(ms);
	 }
	 catch(Exception e) {
		  e.printStackTrace();
	 }
}

/**
 * Saves a screenshot of the simulation under filename
 * `screenshot-X` where X is the step number.
 */
public void screenshot() {

	 this.lots.addAttribute("ui.screenshot", "../screenshot-" + this.step + ".png");
}

/**
 * Generates random geometrical coordinates.
 *
 * @param n The number of coordinates requested.
 * @param width The maximal width of the city.
 */
private void randomCoords(int n, int width) {

	 this.width = width;

	 List<Coordinate> coords = new ArrayList<Coordinate>();

	 int half = this.width / 2;

	 for(int i = 0; i < n; ++i) {

		  // Choose a random position.
		  int x = this.rnd.nextInt(this.width) - half;
		  int y = this.rnd.nextInt(this.width) - half;

		  coords.add(new Coordinate(x, y));
	 }

	 this.lotCoords = coords;
}

/**
 * Generates random geometrical coordinates.
 *
 * @param n The number of coordinates requested.
 * @param radius The maximal width of the city.
 */
private void radialCoords(int n, int radius) {

	 this.width = radius * 2;

	 List<Coordinate> coords = new ArrayList<Coordinate>();

	 int m = 0;

	 while(m < n) {

		  // Choose a random position.
		  int x = this.rnd.nextInt(this.width) - radius;
		  int y = this.rnd.nextInt(this.width) - radius;

		  // Check that it is in the radius.
		  if(Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)) < radius) {
				coords.add(new Coordinate(x, y));
				++m;
		  }
	 }

	 this.lotCoords = coords;
}

}
