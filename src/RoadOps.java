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
				RoadOps.placeCrossroad(c, roads);

		  // Add edges (roads) between nodes (crossroads).
		  for(Node l : lots)
				RoadOps.placeRoadsAroundLot(l, roads);
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
	 public static Node placeCrossroad(Crossroad crossroad, Graph roads) {

		  Node node = roads.addNode("crossroad_" + roads.getNodeCount());

		  // Set the node position.
		  node.setAttribute("x", crossroad.x);
		  node.setAttribute("y", crossroad.y);

		  // Attach the Crossroad object to the node so that it can
		  // serve as a pivot between the node and the associated lots.
		  node.setAttribute("crossroad", crossroad);

		  // Inversely, store a reference to the Crossroad object in
		  // every surrounding lot.
		  for(Node lot : crossroad.getLots()) {

				Set<Crossroad> lotCrossroads = lot.getAttribute("crossroads");
				if(lotCrossroads == null)
					 lotCrossroads = new HashSet<Crossroad>();

				lotCrossroads.add(crossroad);

				lot.setAttribute("crossroads", lotCrossroads);
		  }

		  crossroad.node = node;

		  return node;
	 }

	 /**
	  * Build the roads linking the crossroads surrounding a lot.
	  *
	  * @param lot The lot which surrounding crossroads are to be
	  * linked.
	  * @param roads The road network graph.
	  */
	 public static void placeRoadsAroundLot(Node lot, Graph roads) {

		  Set<Crossroad> lotCrossroads = (Set<Crossroad>)lot.getAttribute("crossroads");

		  for(Edge link : lot.getEachEdge()) {

				Node neighbor = link.getOpposite(lot);

				Set<Crossroad> neighborCrossroads = (Set<Crossroad>)neighbor.getAttribute("crossroads");

				Set<Crossroad> sharedCrossroads = new HashSet<Crossroad>(neighborCrossroads);

				// Only keep the crossroads shared by the two adjacent
				// lots.
				sharedCrossroads.retainAll(lotCrossroads);

				Object[] crossArray = sharedCrossroads.toArray();

				if(crossArray.length == 2) {

					 Node crossA = ((Crossroad)crossArray[0]).node;
					 Node crossB = ((Crossroad)crossArray[1]).node;

					 if(!crossA.hasEdgeBetween(crossB))
						  roads.addEdge(crossA.getId() + "_" + crossB.getId(), crossA, crossB);
				}
		  }
	 }

}
