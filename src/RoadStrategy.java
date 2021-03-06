import java.util.ArrayList;
import java.util.List;

import org.graphstream.algorithm.Toolkit;
import org.graphstream.algorithm.networksimplex.NetworkSimplex;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

public class RoadStrategy extends Strategy {

	private int growth;

	public RoadStrategy(int growth, Simulation sim) {
		  super(sim);

		  this.growth = growth;
	 }

	 void prepare() {

		  // Only build the central road.
		  RoadOps.buildRoad(RoadOps.getClosestRoad(0, 0, this.sim));

		  /*
			 Build roads o the central horizontal line.
		  for(int i = -600; i < 600; i += 10)
				RoadOps.buildRoad(RoadOps.getClosestRoad(i, 0, this.sim));
		  */

		  /*
		  // Build every road
		  for(Edge road : this.sim.roads.getEachEdge())
				RoadOps.buildRoad(road);
		  */
	 }

	 public void update() {

		  addRoads(this.growth);
	 }

	 private void addRoads(int n) {

		  for(Node crossroad : this.sim.roads)
				crossroad.setAttribute("capacity", 10000);

		  // The central built crossroad acts as the supplier of
		  // flow.

		  Node centralCrossroad = RoadOps.getClosestBuiltCrossroad(0, 0, sim);

		  // Crossroads that are already built or connected to a built
		  // one have a negative supply (a demand) which is function of
		  // the density of the surrounding lots.

		  int totalDemand = 0;

		  for(Node crossroad : this.sim.roads)
				if(crossroad != centralCrossroad)
					 /*&&
					(RoadOps.isCrossroadBuilt(crossroad) ||
					RoadOps.isNextToBuiltCrossroad(crossroad)))*/ {

					 int demand = -crossroadSupply(crossroad);

					 totalDemand += demand;

					 crossroad.setAttribute("supply", demand);
				}

		  // Finally, let the central crossroad emit enough flow.

		  centralCrossroad.setAttribute("supply", -totalDemand);

		  // Simplex network algorithm.

		  NetworkSimplex simplex = new NetworkSimplex("supply", "capacity", "cost");

		  simplex.init(this.sim.roads);
		  simplex.compute();
		  //System.out.println(simplex.getSolutionStatus());

		  // Build the a best road.
		  List<Edge> bests = bests(simplex, n);

		  for(Edge road : bests)
			  RoadOps.buildRoad(road);
	 }

	 private int crossroadSupply(Node crossroad) {

		  int supply = 0;

		  List<Node> surroundingLots = ((CrossroadPivot)crossroad.getAttribute("pivot")).lots;

		  for(Node lot : surroundingLots) {
				supply += ((Density)lot.getAttribute("density")).index() * 1000;
				for(int i = 0, l = lot.getDegree(); i < l; ++i) {
					 Node lot2 = lot.getEdge(i).getOpposite(lot);
					 supply += ((Density)lot2.getAttribute("density")).index() * 1000;
				}
		  }

		  return supply;
	 }

	private List<Edge> bests(NetworkSimplex simplex, int n) {

		List<Object[]> records = new ArrayList<Object[]>();

		for(Edge road : this.sim.roads.getEachEdge())
			if(!RoadOps.isRoadBuilt(road) && RoadOps.isNextToBuiltRoad(road)) {
				Object[] r = new Object[2];
				r[0] = road;
				r[1] = simplex.getFlow(road);
				records.add(r);
			}

		List<Edge> results = new ArrayList<Edge>();

		for(int i = 0; i < n; ++i) {
			Edge b = maxFlowRoad(records);
			if(b != null)
				results.add(b);
		}

		return results;
	}

	private Edge maxFlowRoad(List<Object[]> records) {

		// Find the edge of maximum flow.

		int index = -1;
		int maxFlow = -9999999;

		for(int i = 0, l = records.size(); i < l; ++i) {

			Object[] r = (Object[])records.get(i);
			int flow = (Integer)r[1];

			if(flow > maxFlow) {
				index = i;
				maxFlow = flow;
			}
		}

		Edge edge = null;

		// Remove it from the list.
		if(index > -1) {
			Object[] r = records.remove(index);
			edge = (Edge)r[0];
		}

		return edge;
	}

}
