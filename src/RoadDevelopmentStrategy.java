import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.graphstream.algorithm.Toolkit;
import org.graphstream.algorithm.networksimplex.NetworkSimplex;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.stream.SinkAdapter;

public class RoadDevelopmentStrategy extends AbstractStrategy {

	 public RoadDevelopmentStrategy(Simulation sim) {
		  super(sim);

		  this.sim.lots.addElementSink(new RoadDevelopmentSink(this.sim, this));
	 }

	 void prepare() {

		  Edge road = Toolkit.randomEdge(this.sim.roads);
		  RoadOps.buildRoad(road);
	 }

	 public void update() {

		  for(Node crossroad : this.sim.roads) {

				// Crossroads of already built road segments have a positive
				// supply which is function of the density of the surrounding
				// lots.
				if(RoadOps.isCrossroadBuilt(crossroad))
					 crossroad.setAttribute("supply", crossroadSupply(crossroad));

				// Crossroads that are not part of an already built road
				// segment have a negative supply (a demand) which is function
				// of the density of the surrounding lots.
				else
					 crossroad.setAttribute("supply", -crossroadSupply(crossroad));
		  }

		  // TODO scale values to avoid infeasibility

		  /*double part = (double)disconnected.size() / connected.size();

		  Node[] connected_arr = connected.toArray(new Node[0]);

		  connected_arr[0].setAttribute("supply", part == Math.floor(part) ? (int)part : (int)part + 1);
		  for(int i = 1, l = connected_arr.length; i < l; ++i)
				connected_arr[i].setAttribute("supply", (int)part);
		  */

		  // Simplex network algorithm.

		  NetworkSimplex simplex = new NetworkSimplex("supply", "capacity", "cost");

		  simplex.init(this.sim.roads);
		  simplex.compute();
		  System.out.println(simplex.getSolutionStatus());

		  // Build the a best road.
		  RoadOps.buildRoad(roulette(simplex));
	 }

	 private int crossroadSupply(Node crossroad) {

		  int supply = 0;

		  Set<Node> surroundingLots = ((CrossroadPivot)crossroad.getAttribute("pivot")).lots;

		  for(Node lot : surroundingLots)
				supply += ((Density)lot.getAttribute("density")).index() * 1000;

		  return supply;
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
		  double x = Math.random() * total;

		  // Return the associated density.
		  int acc = 0;
		  for(int i = 0, l = wheel.size(); i < l; ++i) {

				acc += wheel.get(i);

				if(x <= acc)
					 return roads.get(i);
		  }

		  return roads.get(roads.size() - 1);
	 }

	 class RoadDevelopmentSink extends SinkAdapter {

		  Simulation sim;
		  RoadDevelopmentStrategy strategy;

		  RoadDevelopmentSink(Simulation sim, RoadDevelopmentStrategy strategy) {

				this.sim = sim;
				this.strategy = strategy;
		  }

		  public void edgeAdded(String graphId, long time, String edgeId) {


		  }
	 }

}
