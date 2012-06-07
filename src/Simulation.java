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

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.triangulate.VoronoiDiagramBuilder;

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
	  * field. If its value is -1, no fields are displayed.
	  */
	 public boolean showPotentialLots = true;
	 public int showWhichVectorField = 0;

	 public List<List<Vector2>> paths;
	 public PotentialLotStrategy PLS;

	 private String lotsStyle = "node {fill-mode: none; size: 5px;} edge {visibility-mode: hidden;}";

	 /**
	  * Current time.
	  */
	 public long now;
	 private long lastStep;

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

		  this.width = 2000;

		  // Compute n random coordinates.
		  this.lotCoords = getRandomCoords(500);
		  //this.lotCoords = getFixedCoords();
		  //this.lotCoords = ShapeFileLoader.getLandLots("data/world_borders/world_borders.shp");
		  //this.lotCoords = ShapeFileLoader.getLandLots("data/IGN/PARCELLE.SHP");

		  // Build a Voronoi diagram for which seeds are the previously
		  // computed coordinates.
		  Geometry voronoi = LotOps.voronoiDiagram(this.lotCoords);

		  LotOps.buildLotsGraph(voronoi, this);

		  RoadOps.buildRoadsGraph(voronoi, this);
	 }

	 public void run() {

		  // Choose appropriate strategies.
		  this.strategies.put("cellular automata", new DensityStrategy(this));
		  this.strategies.put("road development", new RoadStrategy(this));
		  this.strategies.put("lot construction", new LotStrategy(0.6, this));
		  this.strategies.put("potential lot construction", new PotentialLotStrategy(this));

		  while(true) {

				this.now = System.currentTimeMillis();

				if(this.now - this.lastStep > this.stepDuration) {

					 for(Strategy strategy : this.strategies.values())
						  strategy.update();

					 this.lastStep = now;
					 ++this.step;
				}

				redraw();
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
	  * @return A list of coordinates.
	  */
	 private List<Coordinate> getRandomCoords(int n) {

		  List<Coordinate> coords = new ArrayList<Coordinate>();

		  int half = this.width / 2;

		  for(int i = 0; i < n; ++i) {

				// Choose a random position.
				int x = this.rnd.nextInt(this.width) - half;
				int y = this.rnd.nextInt(this.width) - half;

				coords.add(new Coordinate(x, y));
		  }

		  return coords;
	 }

	 /**
	  * Generates a fixed configuration to base report figures upon.
	  */
	 	 private List<Coordinate> getFixedCoords() {

		  List<Coordinate> coords = new ArrayList<Coordinate>();

		  coords.add(new Coordinate(-300, 500));
		  coords.add(new Coordinate(-500, -500));
		  coords.add(new Coordinate(200, -300));
		  coords.add(new Coordinate(500, 0));

		  return coords;
	 }

}
