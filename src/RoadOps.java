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
	  * Populates the "roads" graph. This method should only be called
	  * during the initialization phase of the simulation as new roads
	  * are later inserted dynamically.
	  *
	  * @param voronoi The Voronoi Diagram based on the land lots.
	  * @param g The graph representing the complete road network.
	  */
	 public static void buildRoadsGraph(Geometry voronoi, Graph roads, Graph lots) {

		  // Compute the lots surrounding each crossroad.
		  Set<Crossroad> crossroads = RoadOps.computeCrossroads(lots);

		  for(Crossroad c : crossroads) {

				// Determine the crossroad position.
				RoadOps.computeCrossroadPosition(c);

				// Add the crossroad to the "roads" graph.
				RoadOps.placeCrossroad(c.x, c.y, roads);
		  }
	 }

	 /**
	  * Compute the crossroads.
	  *
	  * <p>A crossroad is defined by a list of lots which positions
	  * encircle it. The crossroad position is not computed here, we
	  * only are interested about the surrounding nodes.</p>
	  *
	  * <p>To do so, we compute maximal cliques of lots using the
	  * Bron-Kerbosch algorithm from GraphStream.</p>
	  *
	  * @param lots the "lots" graph
	  */
	 public static Set<Crossroad> computeCrossroads(Graph lots) {

		  List<List<Node>> cliques = new ArrayList<List<Node>>();
		  for (List<Node> clique : Toolkit.getMaximalCliques(lots))
				cliques.add(clique);

		  Set<Crossroad> crossroads = new HashSet<Crossroad>();

		  for(List<Node> clique : cliques) {

				Crossroad crossroad = new Crossroad();

				for(Node lot : clique)
					 crossroad.addLot(lot);

				crossroads.add(crossroad);
		  }

		  return crossroads;
	 }


	 /**

	  //  EXPERIMENTAL: might work if Bron-Kerbosh is too slow.

	 public static Set<Crossroad> computeCrossroads(Node source) {

		  // Build a set containing all of the source neighbors.
		  Set<Node> neighbors = new HashSet<Node>();
		  for(Edge link : source.getEachEdge())
				neighbors.add(link.getOpposite(source));

		  // Build a set containing every lots.
		  Set<Node> all = new HashSet<Node>(neighbors);
		  all.add(source);

		  // Build a reference table associating each lot with its
		  // neighborhood.
		  HashMap<Node, Set<Node>> neighborhoods = new HashMap<Node, Set<Node>>();
		  for(Node lot : all) {

				Set<Node> neighborhood = new HashSet<Node>();

				for(Edge link : lot.getEachEdge())
					 neighborhood.add(link.getOpposite(lot));

				neighborhoods.put(lot, neighborhood);
		  }

		  Set<Node> in = new HashSet<Node>();
		  in.add(source);

		  Set<Node> out = new HashSet<Node>(neighbors);

		  Set<Node> waitings = new HashSet<Node>(neighbors);

		  while(out.size() > 0) {

				Node lot = getRandomFromSet(waitings);

				waitings.remove(lot);

				out.retainAll(neighborhoods.get(lot));

				in.add(lot);
		  }

		  //

		  Crossroad crossroad = new Crossroad();
		  for(Node l : in)
				crossroad.addLot(l);

		  Set<Crossroad> crossroads = new HashSet<Crossroad>();
		  crossroads.add(crossroad);

		  return crossroads;

	 }

	 private static Node getRandomFromSet(Set<Node> lots) {

		  int index = new Random().nextInt(lots.size());

		  int i = 0;
		  for(Node lot : lots) {

				if (i == index)
					 return lot;

				++i;
		  }

		  return null;
	 }
	 */

	 /**
	  * Computes the position of a crossroad based on the lots
	  * surrounding it.
	  *
	  * <p>The result is stored in the Crossroad object.</p>
	  *
	  * @param crossroad the crossroad to place
	  */
	 public static void computeCrossroadPosition(Crossroad crossroad) {

		  // Build for each lot a set containing the positions of its
		  // vertex.

		  ArrayList<Set<Point2D>> pointSets = new ArrayList<Set<Point2D>>();

		  for(Node lot : crossroad.getLots()) {

				Polygon cell = (Polygon)lot.getAttribute("polygon");

				Coordinate[] vertices = cell.getCoordinates();

				Set<Point2D> points = new HashSet<Point2D>();

				for(int i = 0, l = vertices.length; i < l; ++i) {

					 Point2D vertex = new Point2D.Double(vertices[i].x, vertices[i].y);
					 points.add(vertex);
				}

				pointSets.add(points);
		  }

		  // The position of the crossroad is the one shared with every
		  // cell surrounding it.

		  Set<Point2D> intersection = pointSets.get(0);
		  for(Set<Point2D> points : pointSets)
				intersection.retainAll(points);

		  // XXX: Sometimes prints an empty list... Must miss some
		  // crossroads!
		  System.out.println(intersection);

		  // Store the position in the crossroad.
		  if(intersection.size() > 0) {

				Point2D.Double position = (Point2D.Double)intersection.iterator().next();
				crossroad.x = position.getX();
				crossroad.y = position.getY();
		  }
	 }

	 /**
	  * Places a node representing a crossroad in the graph.
	  *
	  * @param x The x-axis position.
	  * @param y The y-axis position.
	  * @param roads The road network graph.
	  */
	 public static Node placeCrossroad(double x, double y, Graph roads) {

		  Node crossroad = roads.addNode("crossroad_" + roads.getNodeCount());

		  crossroad.setAttribute("x", x);
		  crossroad.setAttribute("y", y);

		  return crossroad;
	 }

}
