import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

public class RoadOps {

	 /**
	  * Populates the "roads" graph. This method should only be called
	  * during the initialization phase of the simulation as new lots
	  * are later inserted dynamically.
	  *
	  * @param voronoi The Voronoi Diagram based on the land lots.
	  * @param g The graph representing the complete road network.
	  */
	 public static void buildRoadsGraph(Geometry voronoi, Graph g) {

		  for(int i = 0, l = voronoi.getNumGeometries(); i < l; ++i) {

				Polygon poly = (Polygon)voronoi.getGeometryN(i);
				Coordinate[] vertices = poly.getCoordinates();

				// Populate the "roads" graph with every Voronoi edge as a
				// potential road.
				for(int j = 0, l2 = vertices.length; j < l2; ++j) {

					 Coordinate currentPoint = vertices[j];
					 Coordinate nextPoint = vertices[(j + 1) % vertices.length];

					 int currentNodeId = g.getNodeCount();
					 int nextNodeId = currentNodeId + 1;

					 String edgeId1 = "road_" + currentNodeId + "_" + nextNodeId;
					 String edgeId2 = "road_" + nextNodeId + "_" + currentNodeId;

					 // Check that the edge is not already present.
					 if(g.getEdge(edgeId1) != null || g.getEdge(edgeId2) != null)
						  continue;

					 Node a = g.addNode("" + g.getNodeCount());
					 a.setAttribute("x", currentPoint.x);
					 a.setAttribute("y", currentPoint.y);

					 Node b = g.addNode("" + g.getNodeCount());
					 b.setAttribute("x", nextPoint.x);
					 b.setAttribute("y", nextPoint.y);

					 g.addEdge(edgeId1, a, b);
				}
		  }
	 }

}
