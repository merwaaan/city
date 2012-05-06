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

		  for(int i = 0, l = crossroads.size(); i < l; ++i) {

				Node a = crossroads.get(i);
				Node b = crossroads.get((i + 1) % l);

				sim.roads.addEdge(a+"_"+b, a, b);
		  }

		  // Update mapping.

		  for(Node crossroad : crossroads)
			  sim.pivots.get(crossroad).lots.add(lot);

		  Set<CrossroadPivot> pivots = new HashSet<CrossroadPivot>();
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
		 Set<CrossroadPivot> lotPivots = (Set<CrossroadPivot>)lot.getAttribute("pivots");

		  // Merge the sub-network of the lot with those of the
		  // neighboring lots.

		  for(Edge link : lot.getEachEdge()) {

				Node neighbor = link.getOpposite(lot);

				// Get the crossroads surrounding the neighbor lot.
				Set<CrossroadPivot> neighborPivots = (Set<CrossroadPivot>)neighbor.getAttribute("pivots");

				for(CrossroadPivot lotPivot : lotPivots) {

					for(CrossroadPivot neighborPivot : new HashSet<CrossroadPivot>(neighborPivots)) {

						Node lotCrossroad = lotPivot.node;
						Node neighborCrossroad = neighborPivot.node;

						if(lotCrossroad.getAttribute("x").equals(neighborCrossroad.getAttribute("x")) &&
						   lotCrossroad.getAttribute("y").equals(neighborCrossroad.getAttribute("y"))) {

							// Merge the two crossroads.
							RoadOps.mergeCrossroads(lotCrossroad, lot, neighborCrossroad, neighbor, sim);

							// Update the mapping.

							//System.out.println(lot+" "+neighbor);
							//System.out.println(lotCrossroad+" "+neighborCrossroad);
							//System.out.println(lotCrossroad.getAttribute("x")+" "+lotCrossroad.getAttribute("y"));

							sim.pivots.remove(neighborCrossroad);

							//System.out.println("1 "+sim.pivots.get(lotCrossroad));
							sim.pivots.get(lotCrossroad).lots.add(neighbor);

							neighborPivots.remove(sim.pivots.get(neighborCrossroad));

							//System.out.println("2 "+sim.pivots.get(lotCrossroad));
							//neighborPivots.add(sim.pivots.get(lotCrossroad));

							//System.out.println();
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

		Set<Node> associatedLots = pivot.lots;

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

		 Node crossroad = sim.roads.addNode(""+Math.random());

		  crossroad.setAttribute("x", x);
		  crossroad.setAttribute("y", y);

		  // Record the node in the pivot.

		  CrossroadPivot pivot = new CrossroadPivot();
		  pivot.node = crossroad;
		  pivot.lots = new HashSet<Node>();

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

		for(Node lot : sim.lots) {

			Set<CrossroadPivot> pivots = (Set<CrossroadPivot>)lot.getAttribute("pivots");

			pivots.remove(pivot);
		}
	}
}
