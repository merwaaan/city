import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.graphstream.graph.Node;
import org.graphstream.stream.SinkAdapter;

public class DensityStrategy extends Strategy {

	 public static Density[] cachedDensityTypes = {
		  Density.LOW,
		  Density.MEDIUM,
		  Density.HIGH
	 };

	 private double[][] affinities = {
		  {1, 0.01, 0},   // LOW
		  {0.001, 1.5, 0.01}, // MEDIUM
		  {0, 0.01, 1.6}    // HIGH
	 };

	 private double[] roadAffinities = {
		  0.1, // LOW
		  0.6, // MEDIUM
		  1    // HIGH
	 };

	 private double[] ratios = {
		  0.1, // LOW
		  1,   // MEDIUM
		  0.2  // HIGH
	 };

	 public DensityStrategy(Simulation sim) {
		  super(sim);

		  this.sim.lots.addSink(new DensityStrategySink(this.sim, this));
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

				if(!ready(lot) || !LotOps.isNextToBuiltRoad(lot))
					 continue;

				Map<Density, Double> potentials = new HashMap<Density, Double>();

				for(Density density : this.cachedDensityTypes)
					 potentials.put(density, potential(lot, density));

				Density next = roulette(lot, potentials);

				lot.setAttribute("nextDensity", next);
		  }

		  // Switch states.
		  for(Node lot : this.sim.lots)
				if(!lot.getAttribute("density").equals(lot.getAttribute("nextDensity"))) {

					 lot.setAttribute("age", 0);
					 lot.setAttribute("density", lot.getAttribute("nextDensity"));
				}
				else
					 lot.setAttribute("age", ((Integer)lot.getAttribute("age")) + 1);
	 }

	 private boolean ready(Node lot) {

		  int age = (Integer)lot.getAttribute("age");

		  double p = sigmoid(age);
		  double r = this.sim.rnd.nextDouble();

		  return r < p;
	 }

	 private double sigmoid(int x) {

		  double weight = 0.02;
		  double offset = 350;

		  return 1 / (1 + Math.exp(-weight * (x - offset)));
	 }

	 private int[] getNeighborDensities(List<Node> neighbors) {

		  // Count the density types of the neighbors.

		  int[] densities = new int[this.cachedDensityTypes.length];

		  for(Node neighbor : neighbors) {

				Density density = (Density)neighbor.getAttribute("density");

				++densities[density.index()]; // += CityOps.getNumBuiltRoadsAround(neighbor);
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

		  // Scale with respect to the road effect.
		  //potential *= roadAffinities[targetDensity.index()];

		  // Scale with respect to the ratios.
		  //potential *= ratios[targetDensity.index()];

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

		 /*
		  Density d = randomDensity();
		  lot.setAttribute("density", d);
		  lot.setAttribute("nextDensity", d);
		 */

		  lot.setAttribute("density", Density.MEDIUM);
		  lot.setAttribute("nextDensity", Density.MEDIUM);

		  lot.setAttribute("age", this.sim.rnd.nextInt(10));
	 }

}
