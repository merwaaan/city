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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

		  // Add the new lot coordinate to the simulation list.
		  sim.lotCoords.add(coord);

		  // XXX: For debugging purposes.
		  //oldLot.setAttribute("ui.style", "fill-color: orange;");
		  //newLot.setAttribute("ui.style", "fill-color: green;");

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

				  changedLots.add(lot);
			  }
		  }

		  // Update the neighborhoods of the updated lots.

		  for(Node lot : changedLots)
				LotOps.unlinkFromInvalidNeighbors(lot, sim);

		  for(Node lot : changedLots)
			  LotOps.linkToNeighbors(lot, sim);

		  // Remove the road network nodes associated with and only
		  // with the updated lots.

		  Set<Node> changedCrossroads = new HashSet<Node>();
		  for(Node lot : changedLots)
			  for(CrossroadPivot pivot : (Set<CrossroadPivot>)lot.getAttribute("pivots")) {

				  Node crossroad = pivot.node;

				  if(RoadOps.crossroadOnlySharedBy(crossroad, changedLots, sim))
					  changedCrossroads.add(crossroad);
			  }

		  for(Node crossroad : new HashSet<Node>(changedCrossroads))
			  RoadOps.removeCrossroad(crossroad, sim);

		  // Recompute the sub-networks of these lots.

		  for(Node lot : changedLots)
			  RoadOps.buildRoadsAroundLot(lot, sim);

		  // Merge with the road network.

		  for(Node lot : changedLots)
			  RoadOps.mergeLotRoadsWithNeighbors(lot, sim);
	 }

}
