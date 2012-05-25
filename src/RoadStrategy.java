import java.util.ArrayList;
import java.util.List;

import org.graphstream.algorithm.Toolkit;
import org.graphstream.algorithm.networksimplex.NetworkSimplex;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.stream.SinkAdapter;

public class RoadStrategy extends Strategy {

	 public RoadStrategy(Simulation sim) {
		  super(sim);

		  this.sim.lots.addElementSink(new RoadStrategySink(this.sim, this));
	 }

	 void prepare() {

		  Edge road = Toolkit.randomEdge(this.sim.roads, this.sim.rnd);
		  RoadOps.buildRoad(road);
	 }

	 public void update() {

		  for(Node crossroad : this.sim.roads)
				crossroad.setAttribute("capacity", 10000);

		  // Crossroads of already built road segments have a positive
		  // supply which is function of the density of the surrounding
		  // lots.

		  int totalSupply = 0;

		  for(Node crossroad : this.sim.roads)
				if(RoadOps.isCrossroadBuilt(crossroad)) {

					 int supply = crossroadSupply(crossroad);

					 totalSupply += supply;

					 crossroad.setAttribute("supply", supply);
				}

		  // Crossroads that are not part of an already built road
		  // segment have a negative supply (a demand) which is function
		  // of the density of the surrounding lots.

		  int totalDemand = 0;

		  for(Node crossroad : this.sim.roads)
				if(!RoadOps.isCrossroadBuilt(crossroad) && RoadOps.isNextToBuiltCrossroad(crossroad)) {

					 int demand = -crossroadSupply(crossroad);

					 totalDemand += demand;

					 crossroad.setAttribute("supply", demand);
				}

		  // Scale values to avoid infeasibility.

		  double ratio = -(double)totalDemand / totalSupply;

		  Node last = null;

		  totalSupply = 0;
		  for(Node crossroad : this.sim.roads)
				if(RoadOps.isCrossroadBuilt(crossroad)) {

					 int supply = (Integer)crossroad.getAttribute("supply");

					 int scaledSupply = (int)(supply * ratio);

					 crossroad.setAttribute("supply", scaledSupply);

					 totalSupply += scaledSupply;

					 last = crossroad;
				}

		  //

		  int supply = (Integer)last.getAttribute("supply");
		  supply += (-totalDemand) - totalSupply;
		  last.setAttribute("supply", supply);

		  // Simplex network algorithm.

		  NetworkSimplex simplex = new NetworkSimplex("supply", "capacity", "cost");

		  simplex.init(this.sim.roads);
		  simplex.compute();
		  //System.out.println(simplex.getSolutionStatus());

		  // Build the a best road.
		  RoadOps.buildRoad(best(simplex));
	 }

	 private int crossroadSupply(Node crossroad) {

		  int supply = 0;

		  List<Node> surroundingLots = ((CrossroadPivot)crossroad.getAttribute("pivot")).lots;

		  for(Node lot : surroundingLots)
				supply += ((Density)lot.getAttribute("density")).index();

		  return supply;
	 }

	 private Edge best(NetworkSimplex simplex) {

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

	 /**
	  *
	  */
	 class RoadStrategySink extends SinkAdapter {

		  Simulation sim;
		  RoadStrategy strategy;

		  RoadStrategySink(Simulation sim, RoadStrategy strategy) {

				this.sim = sim;
				this.strategy = strategy;
		  }

		  public void edgeAdded(String graphId, long time, String edgeId) {


		  }
	 }

}
