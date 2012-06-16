import com.vividsolutions.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

import org.graphstream.graph.Node;
import org.graphstream.ui.geom.Vector2;

public class PotentialLotStrategy extends Strategy {

	private double growthRate;
	private double acc;

	 public List<VectorField> fields;
	 public VectorField sum;

	 private double[] weights;

	public PotentialLotStrategy(double growthRate, Simulation sim) {
		  super(sim);

		  this.growthRate = growthRate;
		  this.acc = 0;

		  this.fields = new ArrayList<VectorField>();

		  // Choose appropriate vector fields.
		  int frequency = 73;
		  this.fields.add(new DensityField(this.sim, frequency));
		  this.fields.add(new RoadField(this.sim, frequency));
		  this.fields.add(new ObstacleField(this.sim, frequency));
		  //this.fields.add(new PatternField(this.sim, frequency));

		  // Weight them.
		  this.weights = new double[3];
		  this.weights[0] = 2;
		  this.weights[1] = 1;
		  this.weights[2] = 10;
		  //this.weights[3] = 2;

		  // The final vector field which guide the land lot seed.
		  this.sum = new SumField(this.sim, frequency, this.fields, this.weights);

		  // This strategy need a initial set of potential lots to
		  // work. This is mainly useful for delineating the city and
		  // to now when seeds should stop their travel.
		  double radius = 3000;
		  for(double angle = 0, stop = 2 * Math.PI; angle < stop; angle += 0.5) {

			  double x = radius * Math.cos(angle);
			  double y = radius * Math.sin(angle);

			  Node lot = LotOps.getLotAt(x, y, this.sim);
			  if(lot != null)
				  CityOps.insertLot(x, y, this.sim);
		  }

		  // link to this specific strategy in the simulation to have
		  // easier access to vector fields when drawing them.
		  this.sim.PLS = this;
	 }

	 void prepare() {
	 }

	 public void update() {

		 this.acc += this.growthRate;

		 while(this.acc >= 1) {

			 // Recompute the vector fields.
			 for(VectorField field : this.fields)
				 field.compute();

			 this.sum.compute();

			 // Spawn a seed near the center.

			 int radius = 1000;

			 boolean done = false;

			 do {
				 double x = this.sim.rnd.nextInt(radius * 2) - radius;
				 double y = this.sim.rnd.nextInt(radius * 2) - radius;

				 done = spawn(x, y);
			 } while(!done);

			 --this.acc;
		 }
	 }

	 public boolean spawn() {

		  double x = this.sim.rnd.nextDouble() * this.sim.width - this.sim.width / 2;
		  double y = this.sim.rnd.nextDouble() * this.sim.width - this.sim.width / 2;

		  return spawn(x, y);
	 }

	 public boolean spawn(double x, double y) {

		  List<Vector2> path = new ArrayList<Vector2>();

		  Vector2 seed = new Vector2(x, y);

		  int steps = 0;
		  int limit = 200; //

		  double speed = 10;
		  double speedIncrease = 1.1;
		  double speedDecrease = 1.5;

		  while(steps < limit) {

			  // Where does the seed should go?
			  Vector2 inf = this.sum.influence(seed.x(), seed.y());

			  Node lot = LotOps.getLotAt(seed.x(), seed.y(), this.sim);

			  // If we are on a potential lot, slow down.
			  if(!LotOps.isLotBuilt(lot))
				  speed /= speedDecrease;
			  // Otherwise, speed up.
			  else
				  speed = Math.min(speed * speedIncrease, 100);

			  //
			  double area = LotOps.getLotArea(lot);
			  speed *= (1 / (area / 50000));

			  // Make it move.
			  inf.scalarMult(speed);
			  seed.add(inf);

			  // Record the new position in the seed path.
			  path.add(new Vector2(seed));

			  // Should we stop the seed?
			  if(speed < 10)
				  break;

			  ++steps;
		  }

		  if(steps < limit) {
				this.sim.paths.add(path);
				CityOps.insertLot(seed.x(), seed.y(), this.sim);
				return true;
		  }

		  return false;
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
