import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

public class RoadOps {


	 public static void buildRoadsGraph(Geometry voronoi, Graph roads, Graph lots) {

		  Map<Node, List<Node>> lotRoadsMap = new HashMap<Node, List<Node>>();

		  for(Node lot : lots)
				RoadOps.buildRoadsAroundLot(lot, lotRoadsMap, roads);

		  for(Node lot : lots)
				RoadOps.mergeLotRoadsWithNeighbors(lot, lotRoadsMap, roads);
	 }

	 public static void buildRoadsAroundLot(Node lot, Map<Node, List<Node>> lotRoadsMap, Graph roads) {

		  Polygon cell = (Polygon)lot.getAttribute("polygon");
		  Coordinate[] vertices = cell.getCoordinates();

		  List<Node> crossroads = new ArrayList<Node>();

		  for(int i = 0, l = vertices.length; i < l; ++i) {

				Node crossroad = roads.addNode(""+roads.getNodeCount());

				crossroad.setAttribute("x", vertices[i].x);
				crossroad.setAttribute("y", vertices[i].y);

				crossroads.add(crossroad);
		  }

		  for(int i = 0, l = crossroads.size(); i < l; ++i) {

				Node a = crossroads.get(i);
				Node b = crossroads.get((i + 1) % l);

				roads.addEdge(a+"_"+b, a, b);
		  }

		  lotRoadsMap.put(lot, crossroads);
	 }

	 public static void mergeLotRoadsWithNeighbors(Node lot, Map<Node, List<Node>> lotRoadsMap, Graph roads) {

		  List<Node> lotCrossroads = lotRoadsMap.get(lot);

		  for(Edge link : lot.getEachEdge()) {

				Node neighbor = link.getOpposite(lot);

				List<Node> neighborCrossroads = lotRoadsMap.get(neighbor);

				for(Node lotCrossroad : lotCrossroads) {

					 for(int i = 0; i < neighborCrossroads.size(); ++i) {

						  Node neighborCrossroad = neighborCrossroads.get(i);

						  if(lotCrossroad.getAttribute("x").equals(neighborCrossroad.getAttribute("x")) &&
							  lotCrossroad.getAttribute("y").equals(neighborCrossroad.getAttribute("y"))) {

								RoadOps.mergeCrossroads(lotCrossroad, lot, neighborCrossroad, neighbor, lotRoadsMap, roads);
						  }
					 }
				}
		  }

		  for(int i = 0; i < roads.getNodeCount(); ++i)
				if(roads.getNode(i).getDegree() == 0) {
					 roads.removeNode(roads.getNode(i));
					 --i;
				}

	 }

	 private static void mergeCrossroads(Node crossA, Node lotA, Node crossB, Node lotB, Map<Node, List<Node>> lotRoadsMap, Graph g) {

		  List<Node> crossBNeighbors = new ArrayList<Node>();
		  for(Edge e : crossB.getEachEdge())
				crossBNeighbors.add(e.getOpposite(crossB));

		  for(Node crossC : crossBNeighbors) {

				g.removeEdge(crossB, crossC);

				if(!crossA.hasEdgeBetween(crossC))
					 g.addEdge(crossA+"_"+crossC, crossA, crossC);
		  }

		  List<Node> crossLotB = lotRoadsMap.get(lotB);
		  crossLotB.remove(crossB);
		  crossLotB.add(crossA);
	 }

}
