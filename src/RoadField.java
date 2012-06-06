import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.distance.DistanceOp;

import java.util.ArrayList;
import java.util.List;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.ui.geom.Vector2;

public class RoadField extends VectorField{

	 public RoadField(Simulation sim, int frequency) {
		  super(sim, frequency);
	 }

	 public void compute() {

		  // Pre-compute the geometry collection consisting of every
		  // built roads.

		  List<LineString> lines = new ArrayList<LineString>();

		  for(Edge road : this.sim.roads.getEachEdge())
				if(RoadOps.isRoadBuilt(road)) {

					 Coordinate[] coords = new Coordinate[2];

					 double x0 = (Double)road.getNode0().getAttribute("x");
					 double y0 = (Double)road.getNode0().getAttribute("y");
					 coords[0] = new Coordinate(x0, y0);

					 double x1 = (Double)road.getNode1().getAttribute("x");
					 double y1 = (Double)road.getNode1().getAttribute("y");
					 coords[1] = new Coordinate(x1, y1);

					 lines.add(this.sim.geomFact.createLineString(coords));
				}

		  MultiLineString roads = this.sim.geomFact.createMultiLineString(lines.toArray(new LineString[0]));

		  // Each vector points in the direction of the closest built
		  // road.

		  for(int i = 0; i < this.vectors.length; ++i)
				for(int j = 0; j < this.vectors[i].length; ++j) {

					 // The base of the vector.
					 Vector2 p = position(i, j);

					 // The closest point of a road from the base.
					 Vector2 r = closestRoad(p, roads);
					 if(r == null)
						  continue;

					 // Compute the vector going from the base to the
					 // closest point.
					 r.sub(p);

					 // Normalize.
					 r.normalize();

					 // Replace.
					 this.vectors[i][j] = r;
				}
	 }

	 private Vector2 closestRoad(Vector2 base, MultiLineString roads) {

		  // Get the lot containing the vector base.
		  Node lot = LotOps.getLotAt(base.x(), base.y(), this.sim);
		  if(lot == null)
				return null;

		  Polygon cell = (Polygon)lot.getAttribute("polygon");

		  // Instantiate a geometry representing the vector base.
		  Geometry p = this.sim.geomFact.createPoint(new Coordinate(base.x(), base.y()));

		  // Compute the closest point.
		  Coordinate closest = DistanceOp.closestPoints(p, roads)[1];

		  return new Vector2(closest.x, closest.y);
	 }

}
