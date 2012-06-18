import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.stream.SinkAdapter;

public class DensityStrategy extends Strategy {

	 public static Density[] cachedDensityTypes = {
		  Density.LOW,
		  Density.MEDIUM,
		  Density.HIGH
	 };

	 private double[][] affinities = {
		  {0.9, 0.001, 0},         // LOW
		  {0.001, 1, 0.0001}, // MEDIUM
		  {0, 0.001, 1.1}       // HIGH
	 };

	 public DensityStrategy(Simulation sim) {
		  super(sim);

		  this.sim.lots.addSink(new DensityStrategySink(this.sim, this));

		  /*
		  // Prepare an initial configuration with gradual density from
		  // the center.
		  for(Node lot : this.sim.lots) {

				double x = (Double)lot.getAttribute("x");
				double y = (Double)lot.getAttribute("y");

				double dist = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));

				Density d;
				if(dist < 100)
					 d = Density.HIGH;
				else if(dist < 250)
					 d = Density.MEDIUM;
 				else
					 d = Density.LOW;

				lot.setAttribute("density", d);
				LotOps.buildLot(lot);
		  }
		  */
	 }

	 /**
	  * Gives a random type of density.
	  */
	 private Density randomDensity() {

		  return this.cachedDensityTypes[this.sim.rnd.nextInt(this.cachedDensityTypes.length)];
	 }

	 void prepare() {

		  // Give "density" and "age" attributes to each lot.
		  for(Node lot : this.sim.lots)
				prepareLot(lot);
	 }

	 public void update() {

		  // Compute next state.
		  for(Node lot : this.sim.lots) {

				if(!ready(lot))// || !LotOps.isNextToBuiltRoad(lot))
					 continue;

				Map<Density, Double> potentials = new HashMap<Density, Double>();

				for(Density density : this.cachedDensityTypes)
					 potentials.put(density, potential(lot, density));

				Density next = roulette(lot, potentials);

				lot.setAttribute("nextDensity", next);
		  }

		  // Switch states.
		  for(Node lot : this.sim.lots)
				if(lot.getAttribute("nextDensity") != null) {

					 lot.setAttribute("density", lot.getAttribute("nextDensity"));
					 lot.removeAttribute("nextDensity");

					 lot.setAttribute("wait", pickWaitingTime());
				}
				else
					 lot.setAttribute("wait", ((Integer)lot.getAttribute("wait")) - 1);
	 }

	// 0.1 - 50
	 double weight = 0.4;
	 double offset = 15;

	 private int pickWaitingTime() {

		  // Inverse of the sigmoid function.
		  return (int)((Math.log10(1 / this.sim.rnd.nextDouble() - 1) - (weight * offset)) / (-weight));
	 }

	 private boolean ready(Node lot) {

		  int wait = (Integer)lot.getAttribute("wait");

		  return wait <= 0;
	 }

	 private double sigmoid(int x) {

		  return 1 / (1 + Math.exp(-weight * (x - offset)));
	 }

	 private int[] getNeighborDensities(List<Node> neighbors) {

		  // Count the density types of the neighbors.

		  int[] densities = new int[this.cachedDensityTypes.length];

		  for(Node neighbor : neighbors) {

				Density density = (Density)neighbor.getAttribute("density");

				++densities[density.index()];
		  }

		  return densities;
	 }

	 private double potential(Node lot, Density targetDensity) {

		  // Get all neighbors.
		  List<Node> neighbors = LotOps.getNeighbors(lot);

		  // Get the density types of all neighbors.
		  int[] densities = getNeighborDensities(neighbors);

		  // Get the density of the current lot.
		  Density lotDensity = (Density)lot.getAttribute("density");

		  double potential = 0;

		  // Weight.
		  for(int i = 0, l = this.cachedDensityTypes.length; i < l; ++i)
				potential += densities[i] * affinities[targetDensity.index()][i];

		  // Normalize.
		  potential /= neighbors.size();

		  return potential;
	 }

	 private Density roulette(Node lot, Map<Density, Double> potentials) {

		  // Build a roulette wheel.

		  List<Double> wheel = new ArrayList<Double>();
		  List<Density> types = new ArrayList<Density>();

		  for(int i = 0, l = this.cachedDensityTypes.length; i < l; ++i)
				if(potentials.get(this.cachedDensityTypes[i]) > 0) {
					 wheel.add(potentials.get(this.cachedDensityTypes[i]));
					 types.add(this.cachedDensityTypes[i]);
				}

		  // Sum up potentials.
		  double total = 0;
		  for(Double potential : wheel)
				total += potential;

		  // Pick a random value.
		  double x = this.sim.rnd.nextDouble() * total;

		  // Return the associated density.
		  double acc = 0;
		  for(int i = 0, l = wheel.size(); i < l; ++i) {

				acc += wheel.get(i);

				if(x <= acc)
					 return types.get(i);
		  }

		  return (Density)lot.getAttribute("density");
	 }

	 /**
	  * A sink watching for newly added lots so that they can be
	  * prepared and associated with the appropriate attributes.
	  */
	 class DensityStrategySink extends SinkAdapter {

		  Simulation sim;
		  DensityStrategy strategy;

		  DensityStrategySink(Simulation sim, DensityStrategy strategy) {

				this.sim = sim;
				this.strategy = strategy;
		  }

		  public void nodeAdded(String graphId, long time, String nodeId) {

				Node lot = this.sim.lots.getNode(nodeId);

				prepareLot(lot);
		  }
	 }

	 private void prepareLot(Node lot) {

		 if(!lot.hasAttribute("density"))
			 lot.setAttribute("density", Density.LOW);

		 lot.setAttribute("wait", pickWaitingTime());
	 }
}
