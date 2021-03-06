import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CityOps {

	 /**
	  * Dynamically inserts a new lot to the city.
	  *
	  * This method is typically called when adding a new lot during
	  * the simulation.
	  *
	  * @param x The x-axis position of the new lot.
	  * @param y The y-axis position of the new lot.
	  * @param sim The simulation.
	  */
	 public static void insertLot(double x, double y, Simulation sim) {

		  Coordinate coord = new Coordinate(x, y);
		  Point pos = (new GeometryFactory()).createPoint(coord);

		  // Find the existing cell within which the new lot will be
		  // built.

		  Node oldLot = LotOps.getLotAt(x, y, sim);

		  if(oldLot == null) {
				System.err.println("Tried to add a new lot beyond the borders");
				return;
		  }

		  // Add the new lot to the land lots graph at the appropriate
		  // position.
		  Node newLot = LotOps.addLot(coord.x, coord.y, sim);

		  // Add the new lot coordinate to the global list.
		  sim.lotCoords.add(coord);

		  // Compute a new Voronoi diagram.
		  GeometryCollection newVoronoi = (GeometryCollection)LotOps.voronoiDiagram(sim.lotCoords);

		  // Instanciate a list of lots modified by the new lot
		  // insertion. Only these lots will have their neighborhoods
		  // updated.
		  List<Node> changedLots = new ArrayList<Node>();

		  // Bind each changed lot with its new polygon.
		  for(Node lot : sim.lots) {

				Polygon cell = (Polygon)lot.getAttribute("polygon");

				if(cell == null || !LotOps.collectionContainsPolygon(newVoronoi, cell)) {

					 cell = LotOps.getLotCell(lot, newVoronoi);

					 lot.setAttribute("polygon", cell);
					 lot.setAttribute("area", cell.getArea());

					 changedLots.add(lot);
				}
		  }

		  // Save roads separating pairs of changed lots to restore
		  // them later.

		  List<Object[]> roadRecords = new ArrayList<Object[]>();
		  for(Node lot : changedLots)
				for(int i = 0, l = lot.getDegree(); i < l; ++i) {

					 Edge link = lot.getEdge(i);
					 Node neigh = link.getOpposite(lot);
					 Edge road = RoadOps.getRoadBetween(lot, neigh);

					 if(road != null && RoadOps.isRoadBuilt(road)) {
						  Object[] rr = {lot, neigh};
						  roadRecords.add(rr);
					 }
				}

		  // Update the neighborhoods of the updated lots.

		  for(Node lot : changedLots)
				LotOps.unlinkFromInvalidNeighbors(lot, sim);

		  for(Node lot : changedLots)
				LotOps.linkToNeighbors(lot, sim);

		  // Remove the road network nodes associated with and only
		  // with the updated lots.

		  List<Node> changedCrossroads = new ArrayList<Node>();
		  for(Node lot : changedLots)
				for(CrossroadPivot pivot : (List<CrossroadPivot>)lot.getAttribute("pivots")) {

					 Node crossroad = pivot.node;

					 if(RoadOps.crossroadOnlySharedBy(crossroad, changedLots, sim) && !changedCrossroads.contains(crossroad))
						  changedCrossroads.add(crossroad);
				}

		  for(Node crossroad : new ArrayList<Node>(changedCrossroads))
				RoadOps.removeCrossroad(crossroad, sim);

		  // Recompute the sub-networks of these lots.

		  for(Node lot : changedLots)
				RoadOps.buildRoadsAroundLot(lot, sim);

		  // Merge with the road network.

		  for(Node lot : changedLots)
				RoadOps.mergeLotRoadsWithNeighbors(lot, sim);

		  // Restore built roads that were erased by the insertion.
		  for(Object[] rr : roadRecords) {
				Node lot1 = (Node)rr[0];
				Node lot2 = (Node)rr[1];

				// Check if the neighborhood relationship is still valid.
				if(lot1.hasEdgeBetween(lot2)) {
					 Edge r = RoadOps.getRoadBetween(lot1, lot2);
					 if(r != null)
						  RoadOps.buildRoad(r);
				}
		  }
	 }

	 public static int getNumBuiltRoadsAround(Node lot) {

		  int n = 0;

		  List<CrossroadPivot> pivots = (ArrayList<CrossroadPivot>)lot.getAttribute("pivots");

		  CrossroadPivot[] pivots_a = pivots.toArray(new CrossroadPivot[0]);

		  for(int i = 0, l1 = pivots_a.length; i < l1; ++i) {

				Node c0 = pivots_a[i].node;

				for(int j = i + 1; j < l1; ++j) {

					 Node c1 = pivots_a[j].node;

					 if(c0.hasEdgeBetween(c1) && c0.getEdgeBetween(c1).hasAttribute("built"))
						  ++n;
				}
		  }

		  return n;
	 }

}
