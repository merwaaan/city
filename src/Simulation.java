import java.awt.Dimension;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.geom.Vector2;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.Viewer;
import org.graphstream.ui.swingViewer.util.Camera;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.triangulate.VoronoiDiagramBuilder;
import com.vividsolutions.jts.operation.buffer.BufferOp;

public class Simulation {

	 public Graph roads;
	 public Graph lots;

	 public Geometry voronoi;

	 private String roadStyle = "node {size: 0px;} edge {fill-color: orange;}";
	 private String lotStyle = "node {size: 5px; fill-color: black;}";

	 public Simulation() {

		  System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

		  this.roads = new SingleGraph("road network");
		  this.roads.addAttribute("ui.stylesheet", roadStyle);

		  this.lots = new SingleGraph("lots");
		  this.lots.addAttribute("ui.stylesheet", lotStyle);

		  this.run();

		  View view = this.lots.display(false).getDefaultView();
		  view.setBackLayerRenderer(new RenderingLayer(this));

		  Camera camera = view.getCamera();
		  camera.setGraphViewport(-1000, 0, 0, 0);
	 }

	 public void run() {

		  Coordinate[] coords = new Coordinate[500];
		  for(int i = 0; i < coords.length; ++i) {

				// Choose a random position.
				float x = (float)(Math.random() * 1000 - 500);
				float y = (float)(Math.random() * 1000 - 500);

				// Add a new coordinate.
				coords[i] = new Coordinate(x, y);

				// Add the corresponding node to the "lots" graph.
				Node lot = this.lots.addNode("lot_" + i);
				lot.setAttribute("x", x);
				lot.setAttribute("y", y);

				LotData data = new LotData();
				data.density = Math.random();

				lot.setAttribute("data", data);
		  }

		  GeometryFactory geomFact = new GeometryFactory();
		  MultiPoint points = geomFact.createMultiPoint(coords);

		  VoronoiDiagramBuilder voronoiBuilder = new VoronoiDiagramBuilder();
		  voronoiBuilder.setSites(points);
		  this.voronoi = voronoiBuilder.getDiagram(geomFact);

		  // The "roads" graph contains the Voronoï edges.
		  for(int i = 0, l = this.voronoi.getNumGeometries(); i < l; ++i) {

				Polygon poly = (Polygon)this.voronoi.getGeometryN(i);
				Coordinate[] vertices = poly.getCoordinates();

				for(Node lot : this.lots) {

					 float x = (Float)lot.getAttribute("x");
					 float y = (Float)lot.getAttribute("y");
					 Point p = geomFact.createPoint(new Coordinate(x, y));

					 if(poly.intersects(p)) {
						  LotData data = lot.getAttribute("data");
						  data.polygon = poly;
						  break;
					 }
				}

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
}
