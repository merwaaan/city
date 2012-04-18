import org.graphstream.graph.*;

public class RandomDensityStrategy extends AbstractStrategy {

	 public RandomDensityStrategy(Simulation sim) {
		  super(sim);
	 }

	 void prepare() {

		  // Give a "density" attribute to each lot.
		  for(Node lot : this.sim.lots)
				lot.setAttribute("density", new Double(Math.random()));
	 }

	 public void update() {

		  // Compute next state.
		  for(Node lot : this.sim.lots)
				lot.setAttribute("nextDensity", new Double(Math.random()));

		  // Switch states.
		  for(Node lot : this.sim.lots)
				lot.setAttribute("density", lot.getAttribute("nextDensity"));
	 }
}
