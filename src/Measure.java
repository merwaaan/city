import com.vividsolutions.jts.geom.Polygon;

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
	public static double averageCrossroadDegree(Simulation sim) {

		Graph roads = Measure.builtRoadNetwork(sim);

		double total = 0;
		int n = roads.getNodeCount();

		// Check if there is at least one crossroad. We would not want
		// to divide by zero.
		if(n == 0)
			return 0;

		for(Node crossroad : roads) {

			if(!RoadOps.isCrossroadBuilt(crossroad))
				continue;

			int deg = crossroad.getDegree();

			if(deg != 2)
				total += crossroad.getDegree();
		}

		return total / n;
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

		return Toolkit.diameter(roads, null, false);
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
			if(RoadOps.isCrossroadBuilt(crossroad))
				built.addNode(crossroad.getId());

		for(Edge road : sim.roads.getEachEdge())
			if(RoadOps.isRoadBuilt(road))
				built.addEdge(road.getId(), road.getNode0().getId(), road.getNode1().getId());

		return built;
	}

}
