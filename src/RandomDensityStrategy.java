import org.graphstream.graph.*;

public class RandomDensityStrategy extends AbstractStrategy {

	 public RandomDensityStrategy(Simulation sim) {
		  super(sim);
	 }

	 void prepare() {

		  // Give a "density" attribute to each lot.
		  for(Node lot : this.sim.lots)
				lot.setAttribute("density", this.sim.rnd.nextDouble());
	 }

	 public void update() {

		  // Compute next state.
		  for(Node lot : this.sim.lots)
				lot.setAttribute("nextDensity", this.sim.rnd.nextDouble());

		  // Switch states.
		  for(Node lot : this.sim.lots)
				lot.setAttribute("density", lot.getAttribute("nextDensity"));
	 }
}
