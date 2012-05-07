import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.graphstream.graph.*;

public class DiscreteDensityStrategy extends AbstractStrategy {

	 private List<Density> cachedDensityTypes;

	 private double[][] weights = {
		  {0.99, 0.1, 0.05, 0.01},
		  {0.01, 0.99, 0.01, 0.01},
		  {0.01, 0.05, 0.99, 0.01},
		  {0.01, 0, 0, 0.99}
	 };

	 public DiscreteDensityStrategy(Simulation sim) {
		  super(sim);
	 }

	 /**
	  * Caches the different types of density for faster access.
	  */
	 private void cacheDensityTypes() {

		  this.cachedDensityTypes = new ArrayList<Density>();

		  for(Density d : Density.values())
				this.cachedDensityTypes.add(d);
	 }

	 /**
	  * Gives a random type of density.
	  */
	 private Density randomDensity() {

		  return this.cachedDensityTypes.get((int)(Math.random() * this.cachedDensityTypes.size()));
	 }

	 void prepare() {

		  if(this.cachedDensityTypes == null)
				cacheDensityTypes();

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

				Density next = roulette(potentials);

				lot.setAttribute("nextDensity", next);
		  }

		  // Switch states.
		  for(Node lot : this.sim.lots)
				lot.setAttribute("density", lot.getAttribute("nextDensity"));


	 }

	 private int[] getNeighborDensities(List<Node> neighbors) {

		  // Count the density types of the neighbors.

		  int[] densities = new int[4];

		  for(Node neighbor : neighbors) {

				Density density = (Density)neighbor.getAttribute("density");

				++densities[this.cachedDensityTypes.indexOf(density)];
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
		  for(int i = 0, l = this.cachedDensityTypes.size(); i < l; ++i)
				potential += densities[i] * this.weights[this.cachedDensityTypes.indexOf(lotDensity)][i];

		  // Normalize.
		  potential /= neighbors.size();

		  return potential;
	 }

	 private Density roulette(Map<Density, Double> potentials) {

		  // Build a roulette wheel.
		  double[] wheel = {
				potentials.get(Density.EMPTY),
				potentials.get(Density.LOW),
				potentials.get(Density.MEDIUM),
				potentials.get(Density.HIGH),
		  };

		  // Sum up potentials.
		  double total = 0;
		  for(int i = 0, l = wheel.length; i < l; ++i)
				total += wheel[i];

		  // Pick a random value.
		  double x = Math.random() * total;

		  // Return the associated density.
		  double acc = 0;
		  for(int i = 0, l = wheel.length; i < l; ++i) {

				acc += wheel[i];

				if(x <= acc)
					 return this.cachedDensityTypes.get(i);
		  }

		  return Density.HIGH;
	 }

}
