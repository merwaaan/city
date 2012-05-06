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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

		  lot.setAttribute("pivots", new HashSet<CrossroadPivot>());

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

		Set<CrossroadPivot> pivots = (Set<CrossroadPivot>)lot.getAttribute("pivots");
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
	  * Gives the lot associated with the cell at the given position.
	  *
	  * @param x The x-axis coordinate of the queried position.
	  * @param y The y-axis coordinate of the queried position.
	  * @param g The graph containing the lots.
	  *
	  * @return The Node representing the corresponding lot or null if
	  * it does not exist.
	  */
	 public static Node getLotAt(double x, double y, Graph g) {

		  Coordinate coord = new Coordinate(x, y);
		  Point seed = (new GeometryFactory()).createPoint(coord);

		  for(Node lot : g) {

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

				if(!cell.intersects(neighborCell))
					 sim.lots.removeEdge(link);
		  }
	 }

	public static boolean collectionContainsPolygon(GeometryCollection collection, Polygon polygon) {

		for(int i = 0, l = collection.getNumGeometries(); i < l; ++i)
			if(collection.getGeometryN(i).equals(polygon))
				return true;

		return false;
	}

}
