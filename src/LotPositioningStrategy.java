import org.graphstream.graph.*;

public class LotPositioningStrategy extends AbstractStrategy {

	 public LotPositioningStrategy(Simulation sim) {
		  super(sim);
	 }

	 void prepare() {

		  for(Node lot : this.sim.lots) {

				// Give a "size" attribute to each lot.
				double size = this.sim.rnd.nextDouble();
				lot.setAttribute("size", size);

				// Change the node size accordingly.
				int min = 3, max = 20;
				int nodeSize = (int)(min + size * (max - min));
				//lot.setAttribute("ui.style", "size: " + nodeSize + "px;");
		  }
	 }

	 public void update() {

	 }
}
