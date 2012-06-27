import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

public class RoadOps {

	 /**
	  * Populates the graph representing the road network.
	  *
	  * @param voronoi The voronoi diagram of the city.
	  * @param sim The simulation.
	  */
	 public static void buildRoadsGraph(Geometry voronoi, Simulation sim) {

		  // Build individual sub-network of roads and crossroads for
		  // each lot.
		  for(Node lot : sim.lots)
				RoadOps.buildRoadsAroundLot(lot, sim);

		  // Merge all the sub-networks to form a unique road network.
		  for(Node lot : sim.lots)
				RoadOps.mergeLotRoadsWithNeighbors(lot, sim);
	 }

	 /**
	  * Builds a sub-network of crossroads and roads surrounding a single lot.
	  *
	  * <p>The sub-network is composed of crossroads placed at the
	  * position of the vertices of the Voronoi cell and of roads
	  * linking them.</p>
	  *
	  * @param lot The land lot.
	  * @param sim The simulation.
	  */
	 public static void buildRoadsAroundLot(Node lot, Simulation sim) {

		  Polygon cell = (Polygon)lot.getAttribute("polygon");
		  Coordinate[] vertices = cell.getCoordinates();

		  List<Node> crossroads = new ArrayList<Node>();

		  // Add the nodes at the vertices positions.

		  for(int i = 0, l = vertices.length; i < l; ++i) {

				Node crossroad = RoadOps.addCrossroad(vertices[i].x, vertices[i].y, sim);

				crossroads.add(crossroad);
		  }

		  // Add the edges linking the crossroads.

		  for(int i = 0, l = crossroads.size() - 1; i < l; ++i) {

				Node a = crossroads.get(i);
				Node b = crossroads.get(i + 1);

				sim.roads.addEdge(a+"_"+b, a, b);
		  }

		  // Update mappings.

		  for(Node crossroad : crossroads) {
				List<Node> lotsAroundCross = sim.pivots.get(crossroad).lots;
				if(!lotsAroundCross.contains(lot))
					 lotsAroundCross.add(lot);
		  }

		  List<CrossroadPivot> pivots = new ArrayList<CrossroadPivot>();
		  for(Node crossroad : crossroads)
				pivots.add(sim.pivots.get(crossroad));
		  lot.setAttribute("pivots", pivots);
	 }

	 /**
	  * Merges the sub-network of roads of a lot with those of the
	  * neighboring lots.
	  *
	  * @param lot The current lot.
	  * @param sim The simulation.
	  */
	 public static void mergeLotRoadsWithNeighbors(Node lot, Simulation sim) {

		  // Get the crossroads surrounding the current lot.
		  List<CrossroadPivot> lotPivots = (List<CrossroadPivot>)lot.getAttribute("pivots");

		  // Merge the sub-network of the lot with those of the
		  // neighboring lots.

		  for(Edge link : lot.getEachEdge()) {

				Node neighbor = link.getOpposite(lot);

				// Get the crossroads surrounding the neighbor lot.
				List<CrossroadPivot> neighborPivots = (List<CrossroadPivot>)neighbor.getAttribute("pivots");

				for(CrossroadPivot lotPivot : lotPivots) {

					 for(CrossroadPivot neighborPivot : new ArrayList<CrossroadPivot>(neighborPivots)) {

						  Node lotCrossroad = lotPivot.node;
						  Node neighborCrossroad = neighborPivot.node;

						  if(lotCrossroad.getAttribute("x").equals(neighborCrossroad.getAttribute("x")) &&
							  lotCrossroad.getAttribute("y").equals(neighborCrossroad.getAttribute("y"))) {

								// Merge the two crossroads.
								RoadOps.mergeCrossroads(lotCrossroad, lot, neighborCrossroad, neighbor, sim);

								// Update mappings.

								List<Node> lotsAroundCross = sim.pivots.get(lotCrossroad).lots;
								if(!lotsAroundCross.contains(neighbor))
									 lotsAroundCross.add(neighbor);

								neighborPivots.remove(sim.pivots.get(neighborCrossroad));

								CrossroadPivot pivot = sim.pivots.get(lotCrossroad);
								if(!neighborPivots.contains(pivot))
									 neighborPivots.add(pivot);
						  }
					 }
				}
		  }

		  // Remove the isolated crossroads disconnected by the
		  // merging as they were doublets.
		  for(int i = 0; i < sim.roads.getNodeCount(); ++i)
				if(sim.roads.getNode(i).getDegree() == 0) {
					 RoadOps.removeCrossroad(sim.roads.getNode(i), sim);
					 --i;
				}
	 }

	 /**
	  * Merges two nodes belonging to two different sub-networks but
	  * representing the same crossroad.
	  *
	  * <p>The crossroad of the second lot to be merged will be
	  * disconnected from the graph and all of its neighbors will be
	  * linked with the crossroad of the first lot.</p>
	  *
	  * @param crossA The first crossroad.
	  * @param lotA The lot associated with the first crossroad.
	  * @param crossB The second crossroad.
	  * @param lotB The lot associated with the second crossroad.
	  * @param sim The Simulation.
	  */
	 private static void mergeCrossroads(Node crossA, Node lotA, Node crossB, Node lotB, Simulation sim) {

		  // Build a list containing B neighbors.
		  List<Node> crossBNeighbors = new ArrayList<Node>();
		  for(Edge e : crossB.getEachEdge())
				crossBNeighbors.add(e.getOpposite(crossB));

		  // unlink B neighbors from B and link them to A instead.
		  for(Node crossC : crossBNeighbors) {

				sim.roads.removeEdge(crossB, crossC);

				if(!crossA.hasEdgeBetween(crossC))
					 sim.roads.addEdge(crossA+"_"+crossC, crossA, crossC);
		  }
	 }

	 public static boolean crossroadOnlySharedBy(Node crossroad, List<Node> changedLots, Simulation sim) {

		  CrossroadPivot pivot = (CrossroadPivot)crossroad.getAttribute("pivot");

		  List<Node> associatedLots = pivot.lots;

		  for(Node lot : associatedLots)
				if(!changedLots.contains(lot))
					 return false;

		  return true;
	 }

	 /**
	  * Adds a new crossroad to the road graph.
	  *
	  * @param x The position of the lot on the x-axis.
	  * @param y The position of the lot on the y-axis.
	  * @param sim The simulation.
	  *
	  * @return The newly added node.
	  */
	 public static Node addCrossroad(double x, double y, Simulation sim) {

		  // TODO attribute real ID!
		  Node crossroad = sim.roads.addNode(""+sim.rnd.nextLong());

		  crossroad.setAttribute("x", x);
		  crossroad.setAttribute("y", y);

		  // Record the node in the pivot.

		  CrossroadPivot pivot = new CrossroadPivot();
		  pivot.node = crossroad;
		  pivot.lots = new ArrayList<Node>();

		  // Record the pivot in the simulation.

		  sim.pivots.put(crossroad, pivot);

		  // Record the pivot in the node.

		  crossroad.addAttribute("pivot", pivot);

		  return crossroad;
	 }

	 /**
	  * Removes a node from the road graph.
	  *
	  * @param crossroad The crossroad to be deleted.
	  * @param sim The simulation.
	  */
	 public static void removeCrossroad(Node crossroad, Simulation sim) {

		  // Remove the node from the graph.

		  sim.roads.removeNode(crossroad);

		  // Remove the pivot from the simulation.

		  sim.pivots.remove(crossroad);

		  // Remove the pivot from lots who have it referenced.

		  CrossroadPivot pivot = (CrossroadPivot)crossroad.getAttribute("pivot");

		  for(Node lot : sim.lots)
				((List<CrossroadPivot>)lot.getAttribute("pivots")).remove(pivot);
	 }

	 /**
	  * "Builds" a road of the existing road network.
	  *
	  * The edge representing the road is already in the road network
	  * but a boolean attribute "built" is added to differenciate it
	  * from the potential roads.
	  *
	  * @param road The road to build.
	  */
	 public static void buildRoad(Edge road) {

		  // Build the road.
		  road.addAttribute("built");

		  // Build the crossroads at each end of the road.
		  road.getNode0().addAttribute("built");
		  road.getNode1().addAttribute("built");
	 }

	 /**
	  * Checks if the supplied road is built (in the other case it is
	  * already part of the road network but only as a potential road).
	  *
	  * @param road The road to check.
	  */
	 public static boolean isRoadBuilt(Edge road) {

		  return road.hasAttribute("built");
	 }

	 /**
	  * Checks if the supplied crossroad is one of the extremities of a
	  * built road.
	  *
	  * @param crossroad The crossroad to check.
	  */
	 public static boolean isCrossroadBuilt(Node crossroad) {

		  return crossroad.hasAttribute("built");
	 }

	 /**
	  * Checks if the supplied road is linked to a built road via one
	  * of its crossroads.
	  *
	  * @param road The road to check.
	  */
	 public static boolean isNextToBuiltRoad(Edge road) {

		  return road.getNode0().hasAttribute("built") || road.getNode1().hasAttribute("built");
	 }

	 /**
	  * Checks if the supplied road is linked to a built crossroad.
	  *
	  * @param crossroad The crossroad to check.
	  */
	 public static boolean isNextToBuiltCrossroad(Node crossroad) {

		  for(Edge road : crossroad.getEachEdge())
				if(RoadOps.isCrossroadBuilt(road.getOpposite(crossroad)))
					 return true;

		  return false;
	 }

	 public static Edge getRoadBetween(Node lot1, Node lot2) {

		  List<CrossroadPivot> pivots1 = (List<CrossroadPivot>)lot1.getAttribute("pivots");

		  List<CrossroadPivot> shared = new ArrayList<CrossroadPivot>();
		  for(CrossroadPivot p : pivots1)
				if(p.lots.contains(lot2))
					 shared.add(p);

		  if(shared.size() != 2)
				return null;

		  Node c1 = shared.get(0).node;
		  Node c2 = shared.get(1).node;
		  Edge road = c1.getEdgeBetween(c2);

		  return road;
	 }

	 public static Edge getClosestRoad(int x, int y, Simulation sim) {

		  Edge closest = null;
		  double closestDist = Double.POSITIVE_INFINITY;

		  for(Edge road : sim.roads.getEachEdge()) {

				Node c1 = road.getNode0();
				double c1x = (Double)c1.getAttribute("x");
				double c1y = (Double)c1.getAttribute("y");

				Node c2 = road.getNode1();
				double c2x = (Double)c2.getAttribute("x");
				double c2y = (Double)c2.getAttribute("y");

				double cx = (c1x + c2x) / 2;
				double cy = (c1y + c2y) / 2;

				double dist = Math.sqrt(Math.pow(x - cx, 2) + Math.pow(y - cy, 2));

				if(dist < closestDist) {
					 closest = road;
					 closestDist = dist;
				}
		  }

		  return closest;
	 }

	 public static Node getClosestBuiltCrossroad(int x, int y, Simulation sim) {

		  Node closest = null;
		  double closestDist = Double.POSITIVE_INFINITY;

		  for(Node crossroad : sim.roads) {

				if(!RoadOps.isCrossroadBuilt(crossroad))
					 continue;

				double cx = (Double)crossroad.getAttribute("x");
				double cy = (Double)crossroad.getAttribute("y");

				double dist = Math.sqrt(Math.pow(x - cx, 2) + Math.pow(y - cy, 2));

				if(dist < closestDist) {
					 closest = crossroad;
					 closestDist = dist;
				}
		  }

		  return closest;
	 }

	 public static boolean isRoadDrawable(Edge road) {

		 Node n = road.getNode0();
		 CrossroadPivot pivot = (CrossroadPivot)n.getAttribute("pivot");
		 for(Node lot : pivot.lots)
			 if(LotOps.isLotBuilt(lot) && !LotOps.isLargeCell(lot))
				 return true;

		 n = road.getNode1();
		 pivot = (CrossroadPivot)n.getAttribute("pivot");
		 for(Node lot : pivot.lots)
			 if(LotOps.isLotBuilt(lot) && !LotOps.isLargeCell(lot))
				 return true;

		 return false;
	 }

}
