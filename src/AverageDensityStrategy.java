import org.graphstream.graph.*;

public class AverageDensityStrategy extends AbstractStrategy {

	 public AverageDensityStrategy(Simulation sim) {
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

				float totalDensity = 0;

				for(Edge e : lot.getEachEdge()) {

					 Node neighbor = e.getOpposite(lot);
					 LotData neighborData = neighbor.getAttribute("data");

					 totalDensity += neighborData.density;
				}

				LotData data = lot.getAttribute("data");
				data.nextDensity = totalDensity / lot.getDegree();
		  }

		  // Switch states.
		  for(Node lot : this.sim.lots) {
				LotData data = lot.getAttribute("data");
				data.density = data.nextDensity;
		  }
	 }
}
