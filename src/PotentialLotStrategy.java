import java.util.ArrayList;
import java.util.List;

import org.graphstream.graph.Node;

public class PotentialLotStrategy extends Strategy {

	 public List<VectorField> fields;

	 public PotentialLotStrategy(Simulation sim) {
		  super(sim);

		  this.fields = new ArrayList<VectorField>();

		  // Choose appropriate vector fields.

		  this.fields.add(new DensityField(this.sim, 30));
	 }

	 void prepare() {
	 }

	 public void update() {

	 }

}
