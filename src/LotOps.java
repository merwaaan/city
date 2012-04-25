import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

final class LotOps {

	 /**
	  * Populate the "lots" graph using a Voronoi diagram `voronoi` and
	  * the coordinates `coords` on which it is based.
	  *
	  * Three steps:
	  * 1 - Add a node at each coordinate.
	  * 2 - Bind it to the appropriate Voronoi cell (in polygon form).
	  * 3 - Add an edge between nodes sharing a Voronoi edge.
	  */
	 static void buildLotsGraph( Coordinate[] coords, Geometry voronoi, Graph g) {

		  for(int i = 0, l = coords.length; i < l; ++i) {

				Node lot = LotOps.placeLot(coords[i].x, coords[i].y, g);

				bindLotToPolygon(lot, voronoi);
		  }

		  //  edges between neighbors.
		  for(Node lot : g)
				LotOps.linkToNeighbors(lot, g);
	 }

	 /**
	  * Add a new node to the "lots" graph at position (`x`,`y`).
	  *
	  * Return the new node.
	  */
	 static Node placeLot(double x, double y, Graph g) {

		  Node lot = g.addNode("lot_" + g.getNodeCount());

		  lot.setAttribute("x", x);
		  lot.setAttribute("y", y);

		  return lot;
	 }

	 /**
	  * Return a Coordinate associated with the lot.
	  */
	 static Coordinate getLotCoordinates(Node lot, Graph g) {

		  double x = (Double)lot.getAttribute("x");
		  double y = (Double)lot.getAttribute("y");

		  return new Coordinate(x, y);
	 }

	 /**
	  * Return an array containing a Coordinate associated with each
	  * lot.
	  */
	 static Coordinate[] getLotsCoordinates(Graph g) {

		  Coordinate[] coords = new Coordinate[g.getNodeCount()];

		  int index = 0;
		  for(Node lot : g)
				coords[index++] = LotOps.getLotCoordinates(lot, g);

		  return coords;
	 }

	 /**
	  * Return the node acting as the seed of the polygon containing
	  * the point at (`x`,`y`), null if it doesn't exist.
	  */
	 static Node getLotAt(double x, double y, Graph g) {

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
	  * Compute which polygon of the `voronoi` Voronoi diagram is to be
	  * associated with the `lot` node and store it as a node
	  * attribute.
	  *
	  * Return true if the polygon is found, false otherwise.
	  */
	 static boolean bindLotToPolygon(Node lot, Geometry voronoi) {

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
	  * Return the polygon containing the point at (`x`,`y`), null if
	  * it doesn't exist.
	  */
	 static Polygon getPolygonAt(double x, double y, Geometry voronoi) {

		  Coordinate coord = new Coordinate(x, y);
		  Point seed = (new GeometryFactory()).createPoint(coord);

		  for(int i = 0, l = voronoi.getNumGeometries(); i < l; ++i) {

				Polygon cell = (Polygon)voronoi.getGeometryN(i);

				if(seed.within(cell))
					 return cell;
		  }

		  return null;
	 }

	 static void clipLotsToCity(Graph g) {

		  Polygon cityHull = CityOps.getCityHull(g);

		  for(Node lot : g)
				LotOps.clipLotToCity(lot, cityHull, g);
	 }

	 static void clipLotToCity(Node lot, Polygon cityHull, Graph g) {

		  Polygon cell = (Polygon)lot.getAttribute("polygon");

		  if(cityHull == null)
				cityHull = CityOps.getCityHull(g);

		  Polygon newCell = (Polygon)cell.intersection(cityHull);

		  lot.setAttribute("polygon", newCell);
	 }

	 /**
	  * Add edges representing a neighborhood relationship between lots
	  * which borders share a Voronoi edge.
	  */
	 static void linkToNeighbors(Node lot, Graph g) {

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
	  * Remove edges between lot that are no longer adjacent. This
	  * method is typically called when the topology of the "lots"
	  * graph could have been compromised by an update (e.g. a lot
	  * insertion).
	  */
	 static void unlinkFromInvalidNeighbors(Node lot, Graph g) {

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

}
