import org.graphstream.graph.*;

public class AverageDensityStrategy extends AbstractStrategy {

	 public AverageDensityStrategy(Simulation sim) {
		  super(sim);
	 }

	 void prepare() {

		  // Give a "density" attribute to each lot.
		  for(Node lot : this.sim.lots)
				lot.setAttribute("density", new Double(Math.random()));
	 }

	 public void update() {

		  // Compute next state.
		  for(Node lot : this.sim.lots) {

				double totalDensity = 0;

				for(Edge e : lot.getEachEdge()) {

					 Node neighbor = e.getOpposite(lot);
					 double neighborDensity = (Double)neighbor.getAttribute("density");

					 totalDensity += neighborDensity;
				}

				double nextDensity = totalDensity / lot.getDegree();
				lot.setAttribute("nextDensity", nextDensity);
		  }

		  // Switch states.
		  for(Node lot : this.sim.lots)
				lot.setAttribute("density", lot.getAttribute("nextDensity"));
	 }
}
