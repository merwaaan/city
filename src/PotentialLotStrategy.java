import com.vividsolutions.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

import org.graphstream.graph.Node;
import org.graphstream.ui.geom.Vector2;

public class PotentialLotStrategy extends Strategy {

	 public List<VectorField> fields;
	 public VectorField sum;

	 private double[] weights;

	 public PotentialLotStrategy(Simulation sim) {
		  super(sim);

		  this.fields = new ArrayList<VectorField>();

		  // Choose appropriate vector fields.
		  int frequency = 73;
		  this.fields.add(new DensityField(this.sim, frequency));
		  this.fields.add(new RoadField(this.sim, frequency));
		  this.fields.add(new ObstacleField(this.sim, frequency));
		  //this.fields.add(new PatternField(this.sim, frequency));

		  // Weight them.
		  this.weights = new double[4];
		  this.weights[0] = 2;
		  this.weights[1] = 1;
		  this.weights[2] = 4;
		  this.weights[3] = 2;

		  // The final vector field which guide the land lot seed.
		  this.sum = new SumField(this.sim, frequency, this.fields, this.weights);

		  // link to this specific strategy in the simulation to have
		  // easier access to vector fields when drawing them.
		  this.sim.PLS = this;
	 }

	 void prepare() {
	 }

	 public void update() {

		  // Recompute the vector fields.
		  for(VectorField field : this.fields)
				field.compute();

		  this.sum.compute();

		  // Spawn a seed near the center.
		  int radius = 400;
		  double x = this.sim.rnd.nextInt(radius * 2) - radius;
		  double y = this.sim.rnd.nextInt(radius * 2) - radius;
		  spawn(x, y);
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
		  double speed = 1;

		  while(!readyToStop(seed) && steps < limit) {

				// Where does the seed should go?
				Vector2 inf = this.sum.influence(seed.x(), seed.y());

				// Make it move.
				inf.scalarMult(speed);
				seed.add(inf);

				path.add(new Vector2(seed));

				// Increase speed.
				speed *= 2;
				speed = Math.min(speed, 100);

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

}
