import org.graphstream.graph.*;

public class RandomDensityStrategy extends AbstractStrategy {

	 public RandomDensityStrategy(Simulation sim) {
		  super(sim);
	 }

	 void prepare() {

		  // Give a density attribute to each lot.
		  for(Node lot : this.sim.lots) {
				LotData data = lot.getAttribute("data");
				data.density = Math.random();
		  }
	 }

	 public void update() {

		  // Compute next state.
		  for(Node lot : this.sim.lots) {
				LotData data = lot.getAttribute("data");
				data.nextDensity = Math.random();
		  }

		  // Switch states.
		  for(Node lot : this.sim.lots) {
				LotData data = lot.getAttribute("data");
				data.density = data.nextDensity;
		  }
	 }
}
