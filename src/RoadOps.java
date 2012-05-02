import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

public class RoadOps {

	 /**
	  * Populates the road network graph. This method should only be
	  * called during the initialization phase of the simulation as new
	  * roads are later inserted dynamically.
	  *
	  * @param voronoi The Voronoi Diagram based on the land lots.
	  * @param g The graph representing the complete road network.
	  */
	 public static void buildRoadsGraph(Geometry voronoi, Graph roads, Graph lots) {

		  // Compute the lots surrounding each crossroad.
		  Set<Crossroad> crossroads = RoadOps.computeCrossroads(lots);

		  // Add a node representing the crossroad to the road network.
		  for(Crossroad c : crossroads)
				RoadOps.placeCrossroad(c, c.x, c.y, roads);

		  //for(Crossroad c : crossroads)
				//RoadOps.linkToNeighbors(c, roads);
	 }

	 /**
	  * Computes the crossroads based on the land lots graph.
	  *
	  * @param lots The land lots graph.
	  *
	  * @return A set of Crossroad.
	  */
	 public static Set<Crossroad> computeCrossroads(Graph lots) {

		  Set<Crossroad> crossroads = new HashSet<Crossroad>();

		  for(Node lot : lots)
				crossroads.addAll(RoadOps.computeCrossroadsFromLot(lot));

		  return crossroads;
	 }

	 /**
	  *
	  */
	 private static Set<Crossroad> computeCrossroadsFromLot(Node lot) {

		  Polygon cell = (Polygon)lot.getAttribute("polygon");
		  Coordinate[] coords = cell.getCoordinates();

		  Set<Crossroad> crossroads = new HashSet<Crossroad>();

		  for(int i = 0, l = coords.length; i < l; ++i)
				crossroads.add(RoadOps.cycleAroundVertex(coords[i], lot, null));

		  return crossroads;
	 }

	 /**
	  *
	  */
	 private static Crossroad cycleAroundVertex(Coordinate vertex, Node currentLot, Crossroad crossroad) {

		  // Instantiate the Crossroad object if it is null; typically
		  // at the first call of this method.

		  if(crossroad == null) {

				crossroad = new Crossroad();

				crossroad.x = vertex.x;
				crossroad.y = vertex.y;
		  }

		  crossroad.addLot(currentLot);

		  // Continue the cycle with a neighboring lot sharing the
		  // vertex AND absent from the crossroad being built.

		  for(Edge link : currentLot.getEachEdge()) {

				Node nextLot = link.getOpposite(currentLot);

				if(!crossroad.containsLot(nextLot) && LotOps.hasVertex(nextLot, vertex))
					 return RoadOps.cycleAroundVertex(vertex, nextLot, crossroad);
		  }

		  return crossroad;
	 }

	 /**
	  * Places a node representing a crossroad in the graph.
	  *
	  * <p>The Crossroad object used during the construction phase is
	  * stored as a node attribute.</p>
	  *
	  * <p>The Crossroad object is also added to the crossroads
	  * attribute of each of its surrounding lots.</p>
	  *
	  * @param crossroad The Crossroad object used during the
	  * construction phase.
	  * @param x The x-axis position.
	  * @param y The y-axis position.
	  * @param roads The road network graph.
	  */
	 public static Node placeCrossroad(Crossroad crossroad, double x, double y, Graph roads) {

		  Node node = roads.addNode("crossroad_" + roads.getNodeCount());

		  // Attribute the position.
		  node.setAttribute("x", x);
		  node.setAttribute("y", y);

		  // Attribute the Crossroad object to the node so that it can
		  // serve as a pivot between the crossroad node and the
		  // associated lot nodes.
		  node.setAttribute("crossroad", crossroad);

		  // Inversely, attribute a reference to the Crossroad object to
		  // every surrounding lot.
		  for(Node lot : crossroad.getLots()) {

				Set<Crossroad> lotCrossroads = lot.getAttribute("crossroads");
				if(lotCrossroads == null)
					 lotCrossroads = new HashSet<Crossroad>();

				lotCrossroads.add(crossroad);

				lot.setAttribute("crossroads", lotCrossroads);
		  }

		  // Also,
		  crossroad.node = node;

		  return node;
	 }

	 /**
	  * Adds an edge between a crossroad and its neighbors.
	  *
	  * @param crossroad The crossroad which node is associated with
	  * the node to be linked.
	  */
	 public static void linkToNeighbors(Crossroad crossroad, Graph roads) {

		  Set<Node> lots = crossroad.getLots();

		  Object[] lotsArray = lots.toArray();

		  // For each pair of lots...
		  for(int i = 1, l = lotsArray.length; i < l; ++i) {

				Node l1 = (Node)lotsArray[i];
				Set<Crossroad> c1 = l1.getAttribute("crossroads");

				for(int j = 0; j < i; ++j) {

					 Node l2 = (Node)lotsArray[j];
					 Set<Crossroad> c2 = new HashSet<Crossroad>((Set<Crossroad>)l2.getAttribute("crossroads"));

					 // Compute the two shared crossroads.
					 c2.retainAll(c1);

					 //
					 if(c2.contains(crossroad)) {

						  // Remove the current crossroad from the set so
						  // that only the neighbor remains.
						  c2.remove(crossroad);

						  Object[] neighborOnly = c2.toArray();

						  if(neighborOnly.length == 1) {

								Crossroad neighbor = (Crossroad)neighborOnly[0];

								if(!crossroad.node.hasEdgeBetween(neighbor.node))
									roads.addEdge(crossroad.node.getId() + "_" + neighbor.node.getId(), crossroad.node, neighbor.node);
						  }
					 }
				}
		  }
	 }

	 public static Coordinate getCrossroadCoordinates(Crossroad crossroad) {

		  return new Coordinate(crossroad.x, crossroad.y);
	 }

	 public static Point2D getCrossroadPoint2D(Crossroad crossroad) {

		  Coordinate coord = RoadOps.getCrossroadCoordinates(crossroad);

		  return new Point2D.Double(coord.x, coord.y);
	 }

}
