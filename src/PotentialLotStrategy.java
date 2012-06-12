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
		  this.fields.add(new ObstacleField(this.sim, frequency));
		  //this.fields.add(new PatternField(this.sim, frequency));

		  this.weights = new double[4];
		  this.weights[0] = 2;
		  this.weights[1] = 1;
		  this.weights[2] = 4;
		  this.weights[3] = 2;

		  this.sim.PLS = this;
	 }

	 void prepare() {
	 }

	 public void update() {

		  // Recompute the vector fields.
		  for(VectorField field : fields)
				field.compute();

		  // Spawn some seeds.
		  spawn();
	 }

	 public void spawn() {

		  double x = this.sim.rnd.nextDouble() * this.sim.width - this.sim.width / 2;
		  double y = this.sim.rnd.nextDouble() * this.sim.width - this.sim.width / 2;

		  spawn(x, y);
	 }

	 public void spawn(double x, double y) {

		  List<Vector2> path = new ArrayList<Vector2>();

		  Vector2 seed = new Vector2(x, y);

		  for(int i = 0; i < 50; ++i) {

				Vector2 inf = influence(seed.x(), seed.y());

				inf.scalarMult(10);

				seed.add(inf);

				path.add(new Vector2(seed));
		  }

		  this.sim.paths.add(path);

		  CityOps.insertLot(seed.x(), seed.y(), this.sim);
	 }

	 private Vector2 influence(double x, double y) {

		  Vector2 all = new Vector2();

		  for(int i = 0, l = this.fields.size(); i < l; ++i) {

				Vector2 v = this.fields.get(i).influence(x, y);

				v.scalarMult(this.weights[i]);

				all.add(v);
		  }

		  all.normalize();

		  return all;
	 }

}
