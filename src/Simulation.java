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
	  * The "lots" graph contains every lot centroids as nodes. Its
	  * edges represent the neighborhood relationships between
	  * adjacent lots.
	  */
	 public Graph lots;

	 /**
	  * The "roads" graph contains every constructible routes from
	  * the edges of the Voronoi diagram. Its nodes are possible
	  * crossroads.
	  */
	 public Graph roads;

	 /**
	  *
	  */
	 private ArrayList<AbstractStrategy> strategies;

	 private String lotsStyle = "node {size: 5px; fill-color: gray;} edge {fill-color: gray;}";

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
		  Geometry voronoi = getVoronoiDiagram(coords, true);

		  // Build the "lots" and "roads" graphs using the coordinates
		  // and the Voronoi Diagram.
		  buildLotsGraph(coords, voronoi);
		  buildRoadsGraph(voronoi);
	 }

	 public void run() {

		  // Save a screenshot.
		  //this.lots.addAttribute("ui.screenshot", "../screenshot.png");

		  this.strategies.add(new AverageDensityStrategy(this));
		  this.strategies.add(new LotPositioningStrategy(this));

		  redraw();
		  //this.lots.setAttribute("ui.screenshot", "0.png");

		  /*
		  for(int i = 0; i < 50; ++i) {
				pause(1000);
				insertLot(Math.random() * 500, Math.random() * 500);
				redraw();
				this.lots.setAttribute("ui.screenshot", (i+1)+".png");
		  }
		  */

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
	 private Coordinate[] getRandomCoords(int lotCount, int offset) {

		  Coordinate[] coords = new Coordinate[lotCount];

		  int offset2 = offset * 2;

		  for(int i = 0; i < coords.length; ++i) {

				// Choose a random position.
				float x = (float)(Math.random() * offset2 - offset);
				float y = (float)(Math.random() * offset2 - offset);

				coords[i] = new Coordinate(x, y);
		  }

		  return coords;
	 }

	 /**
	  * Build and return the Voronoi diagram using the list of
	  * coordinates `coords` as seed positions.
	  **/
	 private Geometry getVoronoiDiagram(Coordinate[] coords, boolean clip) {

		  // Compute the Voronoi diagram.

		  MultiPoint points = this.geomFact.createMultiPoint(coords);

		  VoronoiDiagramBuilder voronoiBuilder = new VoronoiDiagramBuilder();
		  voronoiBuilder.setSites(points);

		  Geometry voronoi = voronoiBuilder.getDiagram(this.geomFact);

		  /**
			* Clip the diagram with the buffered convex hull of the
			* individual centroids to avoid immense lots at the borders
			* of the environment.
			*
			* TODO: a buffered concave hull would be way better.
			* https://github.com/skipperkongen/jts-algorithm-pack
			*/

		  if(clip) {
				Geometry buffer = points.convexHull().buffer(30);
				voronoi = voronoi.intersection(buffer);
		  }

		  return voronoi;
	 }

	 /**
	  * Add a new node to the "lots" graph at position (`x`,`y`).
	  *
	  * Return the new node.
	  */
	 private Node placeLot(double x, double y) {

		  Node lot = this.lots.addNode("lot_" + this.lots.getNodeCount());

		  lot.setAttribute("x", x);
		  lot.setAttribute("y", y);

		  return lot;
	 }

	 /**
	  * Return the node acting as the seed of the polygon containing
	  * the point at (`x`,`y`), null if it doesn't exist.
	  */
	 private Node getLotAt(double x, double y) {

		  Point seed = this.geomFact.createPoint(new Coordinate(x, y));

		  for(Node lot : this.lots) {

				Polygon cell = (Polygon)lot.getAttribute("polygon");
				//System.out.println(lot.getId() + "/" + this.lots.getNodeCount());
				if(seed.within(cell))
					 return lot;
		  }

		  return null;
	 }

	 /**
	  * Return the polygon containing the point at (`x`,`y`), null if
	  * it doesn't exist.
	  */
	 private Polygon getPolygonAt(double x, double y, Geometry voronoi) {

		  Point seed = this.geomFact.createPoint(new Coordinate(x, y));

		  for(int i = 0, l = voronoi.getNumGeometries(); i < l; ++i) {

				Polygon cell = (Polygon)voronoi.getGeometryN(i);

				if(cell.contains(seed))
					 return cell;
		  }

		  return null;
	 }

	 /**
	  * Compute which polygon of the `voronoi` Voronoi diagram is to be
	  * associated with the `lot` node and store it as a node
	  * attribute.
	  *
	  * Return true if the polygon is found, false otherwise.
	  */
	 private boolean bindLotToPolygon(Node lot, Geometry voronoi) {

		  double x = (Double)lot.getAttribute("x");
		  double y = (Double)lot.getAttribute("y");

		  Polygon cell = getPolygonAt(x, y, voronoi);
		  if(cell != null) {
				lot.setAttribute("polygon", cell);
				return true;
		  }

		  return false;
	 }

	 /**
	  * Add edges representing a neighborhood relationship between lots
	  * which borders share a Voronoi edge.
	  */
	 private void linkLotToNeighbors(Node lot) {

		  Polygon polygon = (Polygon)lot.getAttribute("polygon");

		  for(Node otherLot : this.lots) {

				if(lot == otherLot || lot.hasEdgeBetween(otherLot))
					 continue;

				Polygon otherPolygon = (Polygon)otherLot.getAttribute("polygon");

				if(polygon.touches(otherPolygon))
					 this.lots.addEdge("road_" + lot.getId() + "_" + otherLot.getId(), lot, otherLot);
		  }
	 }

	 /**
	  * Populate the "lots" graph using a Voronoi diagram `voronoi` and
	  * the coordinates `coords` on which it is based.
	  *
	  * Three steps:
	  * 1 - Add a node at each coordinate.
	  * 2 - Bind it to the appropriate Voronoi cell (in polygon form).
	  * 3 - Add an edge between nodes sharing a Voronoi edge.
	  */
	 private void buildLotsGraph(Coordinate[] coords, Geometry voronoi) {

		  for(int i = 0, l = coords.length; i < l; ++i) {

				Node lot = placeLot(coords[i].x, coords[i].y);

				bindLotToPolygon(lot, voronoi);
		  }

		  //  edges between neighbors.
		  for(Node lot : this.lots)
				linkLotToNeighbors(lot);
	 }

	 /**
	  * Populate the "roads" graph using the Voronoi diagram. Each edge
	  * represents a Voronoi edge.
	  */
	 private void buildRoadsGraph(Geometry voronoi) {

		  for(int i = 0, l = voronoi.getNumGeometries(); i < l; ++i) {

				Polygon poly = (Polygon)voronoi.getGeometryN(i);
				Coordinate[] vertices = poly.getCoordinates();

				// Populate the "roads" graph with every Voronoi edge as a
				// potential road.
				for(int j = 0, l2 = vertices.length; j < l2; ++j) {

					 Coordinate currentPoint = vertices[j];
					 Coordinate nextPoint = vertices[(j + 1) % vertices.length];

					 Node a = this.roads.addNode("" + this.roads.getNodeCount());
					 a.setAttribute("x", currentPoint.x);
					 a.setAttribute("y", currentPoint.y);

					 Node b = this.roads.addNode("" + this.roads.getNodeCount());
					 b.setAttribute("x", nextPoint.x);
					 b.setAttribute("y", nextPoint.y);

					 this.roads.addEdge(a.getId()+" "+b.getId(), a, b);
				}
		  }
	 }

	 public void insertLot(double x, double y) {

		  Coordinate coord = new Coordinate(x, y);
		  Point pos = this.geomFact.createPoint(coord);

		  /**
			* Find the existing cell within which the new lot will be
			* built. When found, add the new lot to the "lots" graph at
			* the appropriate position.
			*/

		  Node oldLot = getLotAt(x, y);

		  Node newLot = placeLot(coord.x, coord.y);

		  // XXX: For debugging purposes.
		  oldLot.setAttribute("ui.style", "fill-color: orange;");
		  newLot.setAttribute("ui.style", "fill-color: green;");

		  /**
			* Aggregate in a list each lot which polygon and neighborhood
			* could be affected by the construction of the new lot. They
			* are:
			*
		   * - the new lot (obviously)
			* - the old lot
			* - the neighbors of the old lot
			* - the neighbors of neighbors (!)
			*/

		  ArrayList<Node> subLots = new ArrayList<Node>();

		  subLots.add(newLot);

		  subLots.add(oldLot);

		  for(Edge e : oldLot.getEachEdge())
				subLots.add(e.getOpposite(oldLot));

		  for(Edge e1 : oldLot.getEachEdge()) {

				Node n1 = e1.getOpposite(oldLot);

				for(Edge e2 : n1.getEachEdge()) {

					 Node n2 = e2.getOpposite(n1);

					 if(!subLots.contains(n2))
						  subLots.add(n2);
				}
		  }

		  /**
			* Build an array of coordinates corresponding to the
			* centroids of each concerned lot then compute a Voronoi
			* sub-diagram.
			*/

		  Coordinate[] coords = new Coordinate[subLots.size()];

		  for(int i = 0, l = subLots.size(); i < l; ++i)
				coords[i] = new Coordinate((Double)subLots.get(i).getAttribute("x"), (Double)subLots.get(i).getAttribute("y"));

		  Geometry subVoronoi = getVoronoiDiagram(coords, false);

		  /**
			* Update each node with the polygons from the sub-diagram. To
			* avoid adding the immense cells at the borders of the
			* sub-diagram (the same kind of cells that need to be clipped
			* during the initialization process) to the global diagram,
			* we clip each polygon with its previous version as they can
			* only stay identical or shrink.
			*/
		  for(int i = 0, l = subVoronoi.getNumGeometries(); i < l; ++i) {

				Polygon subCell = (Polygon)subVoronoi.getGeometryN(i);

				for(int j = 0, l2 = coords.length; j < l2; ++j) {

					 Point subCoord = this.geomFact.createPoint(coords[j]);

					 if(subCoord.within(subCell)) {

						  Node lot = subLots.get(j);

						  Geometry newCell;

						  if(lot == newLot)
								lot.setAttribute("polygon", subCell);
						  else if(lot != newLot) {

								Polygon oldCell = (Polygon)subLots.get(j).getAttribute("polygon");

								newCell = subCell.intersection(oldCell);

								/**
								 * In recurrent cases, the intersection
								 * returns a GeometryCollection instead of
								 * a Geometry and, as a consequence, the
								 * polygons are messed up.
								 *
								 * To fix this, we go through the
								 * sub-geometries and only keep the
								 * Polygon and get rid of the LineString.
								 */
								if(newCell instanceof GeometryCollection)
									 for(int k = 0, l3 = newCell.getNumGeometries(); k < l3; ++k)
										  if(newCell.getGeometryN(k) instanceof Polygon)
												newCell = newCell.getGeometryN(k);

								lot.setAttribute("polygon", newCell);

								break;
						  }
					 }
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
				Thread.sleep(1000);
		  }
		  catch(Exception e) {
				e.printStackTrace();
		  }
	 }
}
