import com.vividsolutions.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.algorithm.Toolkit;
import org.graphstream.algorithm.BetweennessCentrality;

public class Measure {

	/**
	 * Gives the average degree of the city crossroads. Nodes with
	 * degree equals to 2 are ignored as they do not really represent
	 * crossroads but rather straight lines.
	 *
	 * @param sim The simulation.
	 * @return The average degree of the road network.
	 */
	public static double averageDegree(Simulation sim) {

		Graph roads = Measure.builtRoadNetwork(sim);

		double total = 0;
		int n = roads.getNodeCount();
		int m = 0;

		// Check if there is at least one crossroad. We would not want
		// to divide by zero.
		if(n == 0)
			return 0;

		for(Node crossroad : roads) {

			int deg = crossroad.getDegree();

			if(deg !=0 && deg != 2) {
				total += crossroad.getDegree();
				++m;
			}
		}

		return total / m;
	}

	public static List<double[]> degreeDistance(Simulation sim) {

		Graph roads = Measure.builtRoadNetwork(sim);

		List<double[]> records = new ArrayList<double[]>();

		for(Node crossroad : roads) {

			int deg = crossroad.getDegree();

			if(deg ==0 || deg == 2)
				continue;

			double[] r = new double[2];

			double x = (Double)crossroad.getAttribute("x");
			double y = (Double)crossroad.getAttribute("y");
			double dist = Math.sqrt(x*x + y*y);

			r[0] = dist;
			r[1] = deg;

			records.add(r);
		}

		return records;
	}

	/**
	 * Computes the betweenness centrality of the nodes of the road
	 * network.
	 *
	 * @param sim The simulation.
	 */
	public static void betweennessCentrality(Simulation sim) {

		Graph roads = Measure.builtRoadNetwork(sim);

		(new BetweennessCentrality("betweenness")).betweennessCentrality(roads);
	}

	/**
	 * Computes the diameter of the road network.
	 *
	 * @param sim The simulation.
	 */
	public static double diameter(Simulation sim) {

		Graph roads = Measure.builtRoadNetwork(sim);

		for(Edge road : roads.getEachEdge()) {

			Node c0 = road.getNode0();
			double c0x = (Double)c0.getAttribute("x");
			double c0y = (Double)c0.getAttribute("y");

			Node c1 = road.getNode1();
			double c1x = (Double)c1.getAttribute("x");
			double c1y = (Double)c1.getAttribute("y");

			double d = Math.sqrt(Math.pow(c0x - c1x, 2) + Math.pow(c0y - c1y, 2));

			road.setAttribute("weight", d);
		}

		return Toolkit.diameter(roads, "weight", false);
	}

	/**
	 * Computes the total area of the city.
	 *
	 * Note that the area represents the total surface of the Voronoi
	 * cells (ignoring the bordering ones and the potential ones) and
	 * that it only serves a an indicator as a cell does not exactly
	 * represent the associated land lot.
	 *
	 * @param sim The simulation.
	 * @return The area.
	 */
	public static double area(Simulation sim) {

		double area = 0;

		for(Node lot : sim.lots)
			if(LotOps.isLotBuilt(lot) && !LotOps.isLargeCell(lot)) {

				Polygon cell = (Polygon)lot.getAttribute("polygon");
				if(cell == null)
					continue;

				area += cell.getArea();
			}

		return area;
	}

	/**
	 * Computes the summed areas of each density types.
	 *
	 * @param sim The simulation.
	 * @return The areas as an array.
	 */
	public static double[] densityAreas(Simulation sim) {

		double[] areas = {0, 0, 0};

		for(Node lot : sim.lots)
			if(LotOps.isLotBuilt(lot) && !LotOps.isLargeCell(lot)) {

				Polygon cell = (Polygon)lot.getAttribute("polygon");
				if(cell == null)
					continue;

				Density d = (Density)lot.getAttribute("density");
				if(d == null)
					continue;

				areas[d.index()] += cell.getArea();
			}

		return areas;
	}

	private static Graph builtRoadNetwork(Simulation sim) {

		Graph built = new SingleGraph("road network");

		for(Node crossroad : sim.roads)
			if(RoadOps.isCrossroadBuilt(crossroad)) {
				Node c = built.addNode(crossroad.getId());
				c.setAttribute("x", crossroad.getAttribute("x"));
				c.setAttribute("y", crossroad.getAttribute("y"));
			}

		for(Edge road : sim.roads.getEachEdge())
			if(RoadOps.isRoadBuilt(road))
				built.addEdge(road.getId(), road.getNode0().getId(), road.getNode1().getId());

		return built;
	}

}
