import java.util.ArrayList;
import java.util.List;

import org.graphstream.graph.Node;

public class PotentialLotStrategy extends Strategy {

	 public List<VectorField> fields;

	 public PotentialLotStrategy(Simulation sim) {
		  super(sim);

		  this.fields = new ArrayList<VectorField>();

		  // Choose appropriate vector fields.

		  int frequency = 100;
		  this.fields.add(new DensityField(this.sim, frequency));
		  //this.fields.add(new RoadField(this.sim, frequency));
		  //this.fields.add(new PatternField(this.sim, frequency));
	 }

	 void prepare() {
	 }

	 public void update() {

	 }

}
