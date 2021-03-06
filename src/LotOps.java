import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.triangulate.VoronoiDiagramBuilder;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

public class LotOps {

	 /**
	  * Builds a Voronoi diagram describing the city structure.
	  *
	  * @param coords An array of Coordinate containing the position of
	  * each land lot.
	  */
	 public static Geometry voronoiDiagram(List<Coordinate> coords) {

		 Coordinate[] coordsArray = new Coordinate[coords.size()];
		 int i = 0;
		 for(Coordinate coord : coords)
			 coordsArray[i++] = coord;

		 MultiPoint points = (new GeometryFactory()).createMultiPoint(coordsArray);

		  VoronoiDiagramBuilder voronoiBuilder = new VoronoiDiagramBuilder();
		  voronoiBuilder.setSites(points);

		  Geometry voronoi = voronoiBuilder.getDiagram(new GeometryFactory());

		  return voronoi;
	 }

	 /**
	  * Populates the "lots" graph. This method should only be called
	  * during the initialization phase of the simulation as new lots
	  * are later inserted dynamically.
	  *
	  * @param coords A list of Coordinate corresponding to the
	  * positions of the seeds of each land lot.
	  * @param voronoi The Voronoi diagram based on the land lots
	  * seeds.
	  * @param sim The simulation.
	  */
	 public static void buildLotsGraph(Geometry voronoi, Simulation sim) {

		 for(Coordinate coord : sim.lotCoords) {

			 // Add a node representing the current lot to the land
			 // lot graph.
			 Node lot = LotOps.addLot(coord.x, coord.y, sim);

			 // Attach the Voronoi cell to the lot as a node
			 // attribute.
			 LotOps.bindLotToPolygon(lot, voronoi);
		  }

		  // Draw edges representing neighborhood relationships
		  // between adjacent lots.
		  for(Node lot : sim.lots)
				LotOps.linkToNeighbors(lot, sim);
	 }

	 /**
	  * Adds a new node to the land lots graph.
	  *
	  * @param x The position of the lot on the x-axis.
	  * @param y The position of the lot on the y-axis.
	  * @param sim The simulation.
	  *
	  * @return The newly added node.
	  */
	 public static Node addLot(double x, double y, Simulation sim) {

		  Node lot = sim.lots.addNode("lot_" + sim.lots.getNodeCount());

		  lot.setAttribute("x", x);
		  lot.setAttribute("y", y);

		  //

		  lot.setAttribute("pivots", new ArrayList<CrossroadPivot>());

		  return lot;
	 }

	 /**
	  * Removes a node from the land lots graph.
	  *
	  * @param lot The node to be deleted.
	  * @param sim The simulation.
	  */
	public static void removeLot(Node lot, Simulation sim) {

		sim.roads.removeNode(lot);

		//

		List<CrossroadPivot> pivots = (List<CrossroadPivot>)lot.getAttribute("pivots");
		for(CrossroadPivot pivot : pivots)
			pivot.lots.remove(pivot);
	}

	 /**
	  * Gives the coordinates of a lot.
	  *
	  * @param lot The lot which position is queried.
	  *
	  * @return A Coordinate representing the position of the lot seed.
	  */
	 public static Coordinate getLotCoordinates(Node lot) {

		  double x = (Double)lot.getAttribute("x");
		  double y = (Double)lot.getAttribute("y");

		  return new Coordinate(x, y);
	 }

	 /**
	  * Gives the lot at the given position.
	  *
	  * @param x The x-axis coordinate of the queried position.
	  * @param y The y-axis coordinate of the queried position.
	  * @param sim The simulation.
	  *
	  * @return The Node representing the corresponding lot or null if
	  * it does not exist.
	  */
	 public static Node getLotAt(double x, double y, Simulation sim) {

		  Coordinate coord = new Coordinate(x, y);
		  Point seed = (new GeometryFactory()).createPoint(coord);

		  for(Node lot : sim.lots) {

				Polygon cell = (Polygon)lot.getAttribute("polygon");

				if(seed.within(cell))
					 return lot;
		  }

		  return null;
	 }

	 public static Polygon getLotCell(Node lot, Geometry voronoi) {

		 double x = (Double)lot.getAttribute("x");
		 double y = (Double)lot.getAttribute("y");

		 Point seed = (new GeometryFactory()).createPoint(new Coordinate(x, y));

		 for(int i = 0, l = voronoi.getNumGeometries(); i < l; ++i) {

			 Polygon cell = (Polygon)voronoi.getGeometryN(i);

			 if(seed.within(cell))
				 return cell;
		 }

		 return null;
	 }


	 /**
	  * Associate a lot with its corresponding Voronoi cell. The cell
	  * is then recorded in the Node as an attribute.
	  *
	  * @param lot The node to be associated with a cell.
	  * @param voronoi The Voronoi diagram containing candidate cells.
	  *
	  * @return true if the polygon is found, false otherwise.
	  */
	 public static boolean bindLotToPolygon(Node lot, Geometry voronoi) {

		  double x = (Double)lot.getAttribute("x");
		  double y = (Double)lot.getAttribute("y");

		  Polygon cell = getPolygonAt(x, y, voronoi);

		  if(cell != null) {
				lot.setAttribute("polygon", cell);
				lot.setAttribute("area", cell.getArea());
				return true;
		  }

		  return false;
	 }

	 /**
	  * Gives the Voronoi cell at a given position.
	  *
	  * @param x The x-axis position.
	  * @param y The y-axis position.
	  * @param voronoi The Voronoi diagram containing candidate cells.
	  *
	  * @return The cell as a Polygon.
	  */
	 public static Polygon getPolygonAt(double x, double y, Geometry voronoi) {

		  Coordinate coord = new Coordinate(x, y);

		  Point seed = (new GeometryFactory()).createPoint(coord);

		  for(int i = 0, l = voronoi.getNumGeometries(); i < l; ++i) {

				Polygon cell = (Polygon)voronoi.getGeometryN(i);

				if(seed.within(cell))
					 return cell;
		  }

		  return null;
	 }

	 /**
	  * Adds edges between a lot and the adjacent ones. Two lots are
	  * considered neighbors if their cells are touching (i.e. if they
	  * share a Voronoi edge).
	  *
	  * @param lot The lot to be linked with its neighbors.
	  * @param sim The simulation.
	  */
	 public static void linkToNeighbors(Node lot, Simulation sim) {

		  Polygon polygon = (Polygon)lot.getAttribute("polygon");

		  for(Node otherLot : sim.lots) {

				if(lot == otherLot || lot.hasEdgeBetween(otherLot))
					 continue;

				Polygon otherPolygon = (Polygon)otherLot.getAttribute("polygon");

				if(polygon.intersects(otherPolygon))
					 sim.lots.addEdge("road_" + lot.getId() + "_" + otherLot.getId(), lot, otherLot);
		  }
	 }

	 /**
	  * Removes edges between lots that are no longer adjacent.
	  *
	  * This method is typically called when the topology of the land
	  * lots graph could have been compromised by an update (e.g. a
	  * lot insertion).
	  *
	  * @param lot The lot to be updated.
	  * @param sim The simulation.
	  */
	 public static void unlinkFromInvalidNeighbors(Node lot, Simulation sim) {

		  Polygon cell = (Polygon)lot.getAttribute("polygon");

		  for(Edge link : lot.getEachEdge()) {

				// XXX: check for the edge validity as getEachEdge()
				// sometimes passes a null edge. Weird.
				if(link == null)
					 continue;

				Node neighbor = link.getOpposite(lot);

				Polygon neighborCell = (Polygon)neighbor.getAttribute("polygon");

				if(!cell.overlaps(neighborCell))
					 sim.lots.removeEdge(link);
		  }
	 }

	 public static boolean collectionContainsPolygon(GeometryCollection collection, Polygon polygon) {

		  for(int i = 0, l = collection.getNumGeometries(); i < l; ++i)
				if(collection.getGeometryN(i).equals(polygon))
					 return true;

		  return false;
	 }

	 public static List<Node> getNeighbors(Node lot) {

		  List<Node> neighbors = new ArrayList<Node>();

		  for(Edge link : lot.getEachEdge())
				neighbors.add(link.getOpposite(lot));

		  return neighbors;
	 }

	 /**
	  * "Builds" a lot of the existing urban network.
	  *
	  * The node representing the lot is already in the urban network
	  * but a boolean attribute "built" is added to differenciate it
	  * from the potential lots.
	  *
	  * @param lot The lot to build.
	  */
	 public static void buildLot(Node lot) {

		  // Build the road.
		  lot.addAttribute("built");
	 }

	 /**
	  * Checks if the supplied lot is built (in the other case it is
	  * already part of the urban network but only as a potential lot).
	  *
	  * @param lot The lot to check.
	  */
	 public static boolean isLotBuilt(Node lot) {

		  return lot.hasAttribute("built");
	 }

	 /**
	  * Checks if the supplied lot is next to a built road (if it have
	  * at least one built crossroad).
	  *
	  * @param lot The lot to check.
	  */
	 public static boolean isNextToBuiltRoad(Node lot) {

		  List<CrossroadPivot> pivots = (List<CrossroadPivot>)lot.getAttribute("pivots");

		  for(CrossroadPivot pivot : pivots)
				if(RoadOps.isCrossroadBuilt(pivot.node))
					 return true;

		  return false;
	 }

	 /**
	  * Check if the supplied cell is considered as a large one.
	  * This method is primarily used identify bordering celle.
	  *
	  * @param lot The lot to check.
	  * @return true if the cell is large, false otherwise.
	  */
	 public static boolean isLargeCell(Node lot) {

		  return lot.hasAttribute("area") && ((Double)lot.getAttribute("area")) > 75000;
	 }

	public static double getLotArea(Node lot) {

		return (Double)lot.getAttribute("area");
	}

	 /**
	  * Chekcs if the two supplied land lots are neighbors.
	  *
	  * @param lot1 The first land lot.
	  * @param lot2 The other land lot.
	  * @return A boolean indicating neighborhood.
	  */
	 public static boolean areNeighbors(Node lot1, Node lot2) {

		  return lot1.hasEdgeBetween(lot2);
	 }

}
