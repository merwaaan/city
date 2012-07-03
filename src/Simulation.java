import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
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

    // used to draw the paths taken when positionning potential land
    // lots.
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

	     //new RadialConfiguration(this);
	     new LeHavreConfiguration(this);
	 }

    public void run() {
	
	while(true) {
	    
	    this.now = System.currentTimeMillis();

	    if(this.now - this.lastStep > this.stepDuration) {

		for(Strategy strategy : this.strategies.values())
		    strategy.update();

		this.lastStep = now;

		++this.step;
		System.out.println(this.step+" ");

		redraw();
		//screenshot();
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

	DecimalFormat f = new DecimalFormat("0000000");

	this.lots.addAttribute("ui.screenshot", "../screenshot-" + f.format(this.step) + ".png");
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
