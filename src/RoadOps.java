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

		 // Keep a mapping associating each lot (nodes of the land lot
		 // graph) with the surrounding crossroads (nodes of the road
		 // network graph).
		 Map<Node, List<Node>> lotRoadsMap = new HashMap<Node, List<Node>>();

		 // Build individual sub-network of roads and crossroads for
		 // each lot.
		  for(Node lot : sim.lots)
				RoadOps.buildRoadsAroundLot(lot, lotRoadsMap, sim);

		  // Merge all the sub-networks to form a unique road network.
		  for(Node lot : sim.lots)
				RoadOps.mergeLotRoadsWithNeighbors(lot, lotRoadsMap, sim);
	 }

	/**
	 * Builds a sub-network of crossroads and roads surrounding a single lot.
	 *
	 * <p>The sub-network is composed of crossroads placed at the
	 * position of the vertices of the Voronoi cell and of roads
	 * linking them.</p>
	 *
	 * @param lot The land lot.
	 * @param lotRoadsMap The lot to crossroads mapping.
	 * @param sim The simulation.
	 */
	 public static void buildRoadsAroundLot(Node lot, Map<Node, List<Node>> lotRoadsMap, Simulation sim) {

		  Polygon cell = (Polygon)lot.getAttribute("polygon");
		  Coordinate[] vertices = cell.getCoordinates();

		  List<Node> crossroads = new ArrayList<Node>();

		  // Add the nodes at the vertices positions.
		  for(int i = 0, l = vertices.length; i < l; ++i) {

				Node crossroad = sim.roads.addNode(""+sim.roads.getNodeCount());

				crossroad.setAttribute("x", vertices[i].x);
				crossroad.setAttribute("y", vertices[i].y);

				crossroads.add(crossroad);
		  }

		  // Add the edges linking the crossroads.
		  for(int i = 0, l = crossroads.size(); i < l; ++i) {

				Node a = crossroads.get(i);
				Node b = crossroads.get((i + 1) % l);

				sim.roads.addEdge(a+"_"+b, a, b);
		  }

		  // Record the crossroads in the lot to crossroads mapping.
		  lotRoadsMap.put(lot, crossroads);
	 }

	/**
	 * Merges the sub-network of roads of a lot with those of the
	 * neighboring lots.
	 *
	 * @param lot The current lot.
	 * @param lotRoadsMap The lot to roads mapping.
	 * @param sim The simulation.
	 */
	 public static void mergeLotRoadsWithNeighbors(Node lot, Map<Node, List<Node>> lotRoadsMap, Simulation sim) {

		 // Get the crossroads surrounding the current lot.
		  List<Node> lotCrossroads = lotRoadsMap.get(lot);

		  // Merge the sub-network of the lot with those of the
		  // neighboring lots.

		  for(Edge link : lot.getEachEdge()) {

				Node neighbor = link.getOpposite(lot);

				List<Node> neighborCrossroads = lotRoadsMap.get(neighbor);

				for(Node lotCrossroad : lotCrossroads) {

					 for(int i = 0; i < neighborCrossroads.size(); ++i) {

						  Node neighborCrossroad = neighborCrossroads.get(i);

						  if(lotCrossroad.getAttribute("x").equals(neighborCrossroad.getAttribute("x")) &&
							  lotCrossroad.getAttribute("y").equals(neighborCrossroad.getAttribute("y"))) {

								RoadOps.mergeCrossroads(lotCrossroad, lot, neighborCrossroad, neighbor, lotRoadsMap, sim);
						  }
					 }
				}
		  }

		  // Remove the isolated crossroads disconnected by the
		  // merging as they were doublets.

		  for(int i = 0; i < sim.roads.getNodeCount(); ++i)
				if(sim.roads.getNode(i).getDegree() == 0) {
					 sim.roads.removeNode(sim.roads.getNode(i));
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
	 * @param lotRoadsmap The lot to roads mapping.
	 * @param sim The Simulation.
	 */
	 private static void mergeCrossroads(Node crossA, Node lotA, Node crossB, Node lotB, Map<Node, List<Node>> lotRoadsMap, Simulation sim) {

		  List<Node> crossBNeighbors = new ArrayList<Node>();
		  for(Edge e : crossB.getEachEdge())
				crossBNeighbors.add(e.getOpposite(crossB));

		  for(Node crossC : crossBNeighbors) {

				sim.roads.removeEdge(crossB, crossC);

				if(!crossA.hasEdgeBetween(crossC))
					 sim.roads.addEdge(crossA+"_"+crossC, crossA, crossC);
		  }

		  List<Node> crossLotB = lotRoadsMap.get(lotB);
		  crossLotB.remove(crossB);
		  crossLotB.add(crossA);
	 }

}
