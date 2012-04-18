import java.awt.Dimension;
import java.awt.Rectangle;
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

	 public Graph roads;
	 public Graph lots;

	 private String roadStyle = "node {size: 0px;} edge {fill-color: orange;}";
	 private String lotStyle = "node {size: 5px; fill-color: black;}";

	 private GeometryFactory geomFact;

	 private View view;

	 public Simulation() {

		  System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

		  /**
			* The "lots" graph contains every lot centroids as nodes. Its
			* edges represent the neighborhood relationships between
			* adjacent lots.
			**/
		  this.lots = new SingleGraph("lots");
		  this.lots.addAttribute("ui.stylesheet", lotStyle);

		  /**
			* The "roads" graph contains every constructible routes from
			* the edges of the Voronoi diagram. Its nodes are possible
			* crossroads.
			**/
		  this.roads = new SingleGraph("road network");
		  this.roads.addAttribute("ui.stylesheet", roadStyle);

		  this.geomFact = new GeometryFactory();

		  this.initialize();

		  // Set up the view.
		  this.view = this.lots.display(false).getDefaultView();
		  this.view.setBackLayerRenderer(new RenderingLayer(this));
		  this.view.getCamera().setGraphViewport(-1000, 0, 0, 0);
	 }

	 private void initialize() {

		  // Compute 100 random coordinates.
		  Coordinate[] coords = this.getRandomCoords(100, 500);

		  // Build a Voronoi diagram for which seeds are the previously
		  // computed coordinates.
		  Geometry voronoi = this.buildVoronoiDiagram(coords);

		  // Build the "lots" and "roads" graphs using the coordinates
		  // and the Voronoi Diagram.
		  this.buildLotsGraph(coords, voronoi);
		  this.buildRoadsGraph(voronoi);
	 }

	 public void run() {

		  // Save a screenshot.
		  this.lots.addAttribute("ui.screenshot", "../screenshot.png");

		  AbstractStrategy strategy = new LotPositioningStrategy(this);

		  for(int i = 0; i < 100; ++i) {

				strategy.update();

				this.redraw();
				this.pause(1000);
		  }
	 }

	 /**
	  * Generate `lotCount` geometrical coordinates with X and Y values
	  * within [-`offset`, +`offset`].
	  **/
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
	  * Build the Voronoi diagram using the list of coordinates
	  * `coords` as seed positions.
	  **/
	 private Geometry buildVoronoiDiagram(Coordinate[] coords) {

		  // Compute the Voronoi diagram.

		  MultiPoint points = this.geomFact.createMultiPoint(coords);

		  VoronoiDiagramBuilder voronoiBuilder = new VoronoiDiagramBuilder();
		  voronoiBuilder.setSites(points);

		  Geometry voronoi = voronoiBuilder.getDiagram(this.geomFact);

		  // Clip the diagram with the buffered convex hull of the
		  // individual centroids to avoid immense lots at the borders
		  // of the environment.
		  //
		  // TODO: a buffered concave hull would be way better.
		  // https://github.com/skipperkongen/jts-algorithm-pack

		  Geometry buffer = points.convexHull().buffer(30);
		  voronoi = voronoi.intersection(buffer);

		  return voronoi;
	 }

	 /**
	  * Populate the "lots" graph using a Voronoi diagram `voronoi` and
	  * the coordinates `coords` on which it is based.
	  *
	  * Three steps:
	  * 1 - Add a node at each coordinate
	  * 2 - Bind it to the appropriate Voronoi cell (in polygon form).
	  * 3 - Add an edge between nodes sharing a Voronoi edge.
	  **/
	 private void buildLotsGraph(Coordinate[] coords, Geometry voronoi) {

		  for(int i = 0, l = coords.length; i < l; ++i) {

				Coordinate coord = coords[i];

				// Add the corresponding node to the "lots" graph.
				Node lot = this.lots.addNode("lot_" + i);
				lot.setAttribute("x", coord.x);
				lot.setAttribute("y", coord.y);

				//
				LotData data = new LotData();
				lot.setAttribute("data", data);

				// Bind the node with the appropriate Voronoi cell.
				for(int j = 0, l2 = voronoi.getNumGeometries(); j < l2; ++j) {

					 Polygon poly = (Polygon)voronoi.getGeometryN(j);
					 Point point = this.geomFact.createPoint(coord);

					 if(poly.contains(point)) {
						  data.polygon = poly;
						  break;
					 }
				}
		  }

		  // Draw edges between neighbors.
		  for(Node lot : this.lots) {

				LotData data = lot.getAttribute("data");
				Polygon poly = data.polygon;

				for(Node otherLot : this.lots) {

					 if(lot == otherLot || lot.hasEdgeBetween(otherLot))
						  continue;

					 LotData otherData = otherLot.getAttribute("data");
					 Polygon otherPoly = otherData.polygon;

					 if(poly.touches(otherPoly))
						  this.lots.addEdge(lot.getId() + "_" + otherLot.getId(), lot, otherLot);
				}
		  }
	 }

	 /**
	  * Populate the "roads" graph using the Voronoi diagram. Each edge
	  * represents a Voronoi edge.
	  **/
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
