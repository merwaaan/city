import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.graphstream.graph.*;

public class DiscreteDensityStrategy extends AbstractStrategy {

	 public static Density[] cachedDensityTypes = {
		  Density.EMPTY,
		  Density.LOW,
		  Density.HIGH
	 };

	 private double[][][] weights = {

		  // from EMPTY
		  {
				{1, -1, -1}, // to EMPTY
				{1, 1, -1}, // to LOW
				{1, 0, 1} // to HIGH
		  },

		  // from LOW
		  {
				{0, -1, 1}, // to EMPTY
				{0, 1, -1}, // to LOW
				{-1, -1, -1} // to HIGH
		  },

		  // from HIGH
		  {
				{1, 1, -1}, // to EMPTY
				{1, 1, -1}, // to LOW
				{0, -1, 1} // to HIGH
		  }
	 };

	 public DiscreteDensityStrategy(Simulation sim) {
		  super(sim);
	 }

	 /**
	  * Gives a random type of density.
	  */
	 private Density randomDensity() {

		  return this.cachedDensityTypes[(int)(Math.random() * this.cachedDensityTypes.length)];
	 }

	 void prepare() {

		  // Give a "density" attribute to each lot.
		  for(Node lot : this.sim.lots)
				lot.setAttribute("density", randomDensity());
	 }

	 public void update() {

		  // Compute next state.
		  for(Node lot : this.sim.lots) {

				Map<Density, Double> potentials = new HashMap<Density, Double>();

				for(Density density : this.cachedDensityTypes)
					 potentials.put(density, potential(lot, density));

				Density next = roulette(lot, potentials);

				lot.setAttribute("nextDensity", next);
		  }

		  // Switch states.
		  for(Node lot : this.sim.lots)
				lot.setAttribute("density", lot.getAttribute("nextDensity"));
	 }

	 private int[] getNeighborDensities(List<Node> neighbors) {

		  // Count the density types of the neighbors.

		  int[] densities = new int[this.cachedDensityTypes.length];

		  for(Node neighbor : neighbors) {

				Density density = (Density)neighbor.getAttribute("density");

				++densities[densityIndex(density)];
		  }

		  return densities;
	 }

	 private int densityIndex(Density density) {

		  switch(density) {
		  case EMPTY:
				return 0;
		  case LOW:
				return 1;
		  case HIGH:
				return 2;
		  }

		  return -1;
	 }

	 private double potential(Node lot, Density targetDensity) {

		  // Get all neighbors.
		  List<Node> neighbors = LotOps.getNeighbors(lot);

		  // Get the density types of all neighbors.
		  int[] densities = getNeighborDensities(neighbors);

		  // Get the density of the current lot.
		  Density lotDensity = (Density)lot.getAttribute("density");

		  double[][] weights = this.weights[densityIndex(lotDensity)];

		  double potential = 0;

		  // Weight.
		  for(int i = 0, l = this.cachedDensityTypes.length; i < l; ++i)
				potential += densities[i] * weights[densityIndex(targetDensity)][i];

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
		  double x = Math.random() * total;

		  // Return the associated density.
		  double acc = 0;
		  for(int i = 0, l = wheel.size(); i < l; ++i) {

				acc += wheel.get(i);

				if(x <= acc)
					 return types.get(i);
		  }

		  return (Density)lot.getAttribute("density");
	 }

}
