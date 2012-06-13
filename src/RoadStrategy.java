import java.util.ArrayList;
import java.util.List;

import org.graphstream.algorithm.Toolkit;
import org.graphstream.algorithm.networksimplex.NetworkSimplex;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

public class RoadStrategy extends Strategy {

	 public RoadStrategy(Simulation sim) {
		  super(sim);
	 }

	 void prepare() {

		  RoadOps.buildRoad(RoadOps.getClosestRoad(0, 0, this.sim));
	 }

	 public void update() {

		  addRoads(n);
	 }

	 private void addRoads(int n) {

		  for(Node crossroad : this.sim.roads)
				crossroad.setAttribute("capacity", 10000);

		  // The central built Crossroad acts as the supplier of
		  // flow.

		  Node centralCrossroad = RoadOps.getClosestBuiltCrossroad(0, 0, sim);

		  // Crossroads that are already built or connected to a built
		  // one have a negative supply (a demand) which is function of
		  // the density of the surrounding lots.

		  int totalDemand = 0;

		  for(Node crossroad : this.sim.roads)
				if(crossroad != centralCrossroad &&
					(RoadOps.isCrossroadBuilt(crossroad) ||
					 RoadOps.isNextToBuiltCrossroad(crossroad))) {

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
		  = bests(simplex, n);

		  RoadOps.buildRoad(best(simplex));
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

	 private Edge best(NetworkSimplex simplex, int n) {

		  Edge bestRoad = null;
		  int maxFlow = 0;

		  for(Edge road : this.sim.roads.getEachEdge())
				if(simplex.getFlow(road) >= maxFlow && !RoadOps.isRoadBuilt(road) && RoadOps.isNextToBuiltRoad(road)) {
					 bestRoad = road;
					 maxFlow = simplex.getFlow(road);
				}

		  return bestRoad;
	 }

	 private Edge roulette(NetworkSimplex simplex) {

		  // Fill a roulette wheel.
		  List<Integer> wheel = new ArrayList<Integer>();
		  List<Edge> roads = new ArrayList<Edge>();
		  for(Edge road : this.sim.roads.getEachEdge())
				if(!RoadOps.isRoadBuilt(road) && simplex.getFlow(road) > 0) {
					 wheel.add(simplex.getFlow(road));
					 roads.add(road);
				}

		  // Sum up flows.
		  int total = 0;
		  for(Integer flow : wheel)
				total += flow;

		  // Pick a random value.
		  int x = this.sim.rnd.nextInt(total);

		  // Return the associated density.
		  int acc = 0;
		  for(int i = 0, l = wheel.size(); i < l; ++i) {

				acc += wheel.get(i);

				if(x <= acc)
					 return roads.get(i);
		  }

		  return roads.get(roads.size() - 1);
	 }

}
