import org.graphstream.graph.*;

public class LotPositioningStrategy extends AbstractStrategy {

	 public LotPositioningStrategy(Simulation sim) {
		  super(sim);
	 }

	 void prepare() {

		  for(Node lot : this.sim.lots) {

				// Give a "size" attribute to each lot.
				LotData data = lot.getAttribute("data");
				data.size = Math.random();

				// Change the node size accordingly.
				int min = 3, max = 20;
				int nodeSize = (int)(min + data.size * (max - min));
				lot.setAttribute("ui.style", "size: " + nodeSize + "px;");
		  }
	 }

	 public void update() {

	 }
}
