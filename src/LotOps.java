import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import java.util.ArrayList;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

public class LotOps {

	 /**
	  * Populates the "lots" graph. This method should only be called
	  * during the initialization phase of the simulation as new lots
	  * are later inserted dynamically.
	  *
	  * @param coords An array of Coordinate corresponding to the
	  * positions of the seeds of each land lot.
	  * @param voronoi The Voronoi diagram based on the land lots
	  * seeds.
	  *
	  * @param g The graph to be populated.
	  */
	 public static void buildLotsGraph( Coordinate[] coords, Geometry voronoi, Graph g) {

		  for(int i = 0, l = coords.length; i < l; ++i) {

				Node lot = LotOps.placeLot(coords[i].x, coords[i].y, g);

				bindLotToPolygon(lot, voronoi);
		  }

		  //  edges between neighbors.
		  for(Node lot : g)
				LotOps.linkToNeighbors(lot, g);
	 }

	 /**
	  * Adds and positions a new node to the "lots" graph.
	  *
	  * @param x The position of the lot seed on the x-axis.
	  * @param y The position of the lot seed on the y-axis.
	  *
	  * @return The newly added node.
	  */
	 public static Node placeLot(double x, double y, Graph g) {

		  Node lot = g.addNode("lot_" + g.getNodeCount());

		  lot.setAttribute("x", x);
		  lot.setAttribute("y", y);

		  return lot;
	 }

	 /**
	  * Gives the coordinates of a lot.
	  *
	  * @param lot The lot which position is queried.
	  * @param g The graph containing the aforementioned lot.
	  *
	  * @return A Coordinate representing the position of the lot seed.
	  */
	 public static Coordinate getLotCoordinates(Node lot, Graph g) {

		  double x = (Double)lot.getAttribute("x");
		  double y = (Double)lot.getAttribute("y");

		  return new Coordinate(x, y);
	 }

	 /**
	  * Gives the coordinates of every lots.
	  *
	  * @param g The graph containing the lots.
	  *
	  * @return An array of Coordinate containing the position of the
	  * lots seeds.
	  */
	 public static Coordinate[] getLotsCoordinates(Graph g) {

		  Coordinate[] coords = new Coordinate[g.getNodeCount()];

		  int index = 0;
		  for(Node lot : g)
				coords[index++] = LotOps.getLotCoordinates(lot, g);

		  return coords;
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
	  * Clips every lots cells to the general shape of the city. This
	  * method is used to get rid of the large cells occupying the
	  * exterior edges of the Voronoi diagram.
	  *
	  * @param g The graph containing lots to be clipped.
	  */
	 public static void clipLotsToCity(Graph g) {

		  Polygon cityHull = CityOps.getCityHull(g);

		  for(Node lot : g)
				LotOps.clipLotToCity(lot, cityHull, g);
	 }

	 /**
	  * Clips a unique lot cell to the general shape of the city.
	  *
	  * @param lot The lot to be clipped.
	  * @param cityHull A Polygon representing the general shape of the
	  * city. The final cell will be the intersection between th city
	  * hull and the lot cell.
	  * @param g The graph containing every lots.
	  */
	 public static void clipLotToCity(Node lot, Polygon cityHull, Graph g) {

		  Polygon cell = (Polygon)lot.getAttribute("polygon");

		  if(cityHull == null)
				cityHull = CityOps.getCityHull(g);

		  Polygon newCell = (Polygon)cell.intersection(cityHull);

		  lot.setAttribute("polygon", newCell);
	 }

	 /**
	  * Adds edges between a lot and the adjacent ones. Two lots are
	  * considered neighbors if their cells are touching (i.e. if they
	  * share a Voronoi edge).
	  *
	  * @param lot The lot to be linked with its neighbors.
	  * @param g The graph containing the lot and the possible
	  * neighbors.
	  */
	 public static void linkToNeighbors(Node lot, Graph g) {

		  Polygon polygon = (Polygon)lot.getAttribute("polygon");

		  for(Node otherLot : g) {

				if(lot == otherLot || lot.hasEdgeBetween(otherLot))
					 continue;

				Polygon otherPolygon = (Polygon)otherLot.getAttribute("polygon");

				if(polygon.intersects(otherPolygon))
					 g.addEdge("road_" + lot.getId() + "_" + otherLot.getId(), lot, otherLot);
		  }
	 }

	 /**
	  * Removes edges between lots that are no longer adjacent.
	  *
	  * This method is typically called when the topology of the "lots"
	  * graph could have been compromised by an update (e.g. a lot
	  * insertion).
	  *
	  * @param lot The lot to be updated.
	  * @param g The graph containing the lot and its neighbors.
	  */
	 public static void unlinkFromInvalidNeighbors(Node lot, Graph g) {

		  Polygon polygon = (Polygon)lot.getAttribute("polygon");

		  for(Edge e : lot.getEachEdge()) {

				// XXX: check for the edge validity as getEachEdge()
				// sometimes passes a null edge. Weird.
				if(e == null)
					 continue;

				Node neighbor = e.getOpposite(lot);

				Polygon neighborPolygon = (Polygon)neighbor.getAttribute("polygon");

				if(!polygon.intersects(neighborPolygon))
					 g.removeEdge(e);
		  }
	 }

	 public static boolean isLotNeighborWith(Node lot, Node neighbor) {

		  return lot.hasEdgeBetween(neighbor);
	 }

	 public static boolean isLotNeighborWith(Node lot, ArrayList<Node> neighbors) {

		  for(Node neighbor : neighbors)
				if(!lot.hasEdgeBetween(neighbor))
					 return false;

		  return true;
	 }

}
