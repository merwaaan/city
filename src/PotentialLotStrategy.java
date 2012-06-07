import java.util.ArrayList;
import java.util.List;

import org.graphstream.graph.Node;
import org.graphstream.ui.geom.Vector2;

public class PotentialLotStrategy extends Strategy {

	 public List<VectorField> fields;

	 private double[] weights;

	 public PotentialLotStrategy(Simulation sim) {
		  super(sim);

		  this.fields = new ArrayList<VectorField>();

		  // Choose appropriate vector fields.

		  int frequency = 100;
		  this.fields.add(new DensityField(this.sim, frequency));
		  this.fields.add(new RoadField(this.sim, frequency));
		  //this.fields.add(new ObstacleField(this.sim, frequency));
		  //this.fields.add(new PatternField(this.sim, frequency));

		  this.weights = new double[4];
		  this.weights[0] = 1;
		  this.weights[1] = 1;

		  spawn();
	 }

	 void prepare() {
	 }

	 public void update() {

		  // Recompute the vector fields.
		  for(VectorField field : fields)
				field.compute();

		  // Spawn some seeds.
		  //spawn();
	 }

	 private void spawn() {

		  double x = this.sim.rnd.nextDouble() * this.sim.width - this.sim.width / 2;
		  double y = this.sim.rnd.nextDouble() * this.sim.width - this.sim.width / 2;
		  Vector2 seed = new Vector2(x, y);

		  List<Vector2> path = new ArrayList<Vector2>();

		  for(int i = 0; i < 10; ++i) {

				Vector2 inf = influence(seed.x(), seed.y());

				inf.scalarMult(30);

				seed.add(inf);

				path.add(new Vector2(seed));
		  }

		  this.sim.paths.add(path);
	 }

	 private Vector2 influence(double x, double y) {

		  Vector2 v = new Vector2();

		  for(int i = 0, l = this.fields.size(); i < l; ++i) {

				Vector2 all = this.fields.get(i).influence(x, y);

				all.scalarMult(this.weights[i]);

				v.add(all);
		  }

		  v.normalize();

		  return v;
	 }

}
