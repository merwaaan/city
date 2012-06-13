import com.vividsolutions.jts.geom.Polygon;

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

		  // Spawn a seed near the center.
		  int radius = 50;
		  double x = this.sim.rnd.nextInt(radius * 2) - radius;
		  double y = this.sim.rnd.nextInt(radius * 2) - radius;
		  //spawn(x, y);
	 }

	 public void spawn() {

		  double x = this.sim.rnd.nextDouble() * this.sim.width - this.sim.width / 2;
		  double y = this.sim.rnd.nextDouble() * this.sim.width - this.sim.width / 2;

		  spawn(x, y);
	 }

	 public void spawn(double x, double y) {

		  List<Vector2> path = new ArrayList<Vector2>();

		  Vector2 seed = new Vector2(x, y);

		  int steps = 0;
		  int limit = 100;

		  while(!readyToStop(seed) && steps < limit) {

				Vector2 inf = influence(seed.x(), seed.y());

				inf.scalarMult(50);

				seed.add(inf);

				path.add(new Vector2(seed));

				++steps;
		  }

		  if(steps < limit)
				this.sim.paths.add(path);

		  CityOps.insertLot(seed.x(), seed.y(), this.sim);
	 }

	 private boolean readyToStop(Vector2 seed) {

		  Node lot = LotOps.getLotAt(seed.x(), seed.y(), this.sim);
		  if(lot == null)
				return false;

		  Polygon cell = (Polygon)lot.getAttribute("polygon");
		  if(cell == null)
				return false;

		  double area = cell.getArea();

		  return area > 50000;
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
