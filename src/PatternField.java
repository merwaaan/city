import org.graphstream.ui.geom.Vector2;

public class PatternField extends VectorField{

	 public PatternField(Simulation sim, int frequency) {
		  super(sim, frequency);
	 }

	 public void compute() {

		  int freq = 200;

		  // Each vector points towards regularly fixed points.

		  for(int i = 0; i < this.vectors.length; ++i)
				for(int j = 0; j < this.vectors[i].length; ++j) {

					 // The base of the vector.
					 Vector2 p = position(i, j);
					 double x = p.x();
					 double y = p.y();

					 // Find which fixed point is the closest.

					 double xd = (this.sim.width/2 + x) / freq;
					 double yd = (this.sim.width/2 + y) / freq;

					 int xsnap = (int)(Math.round(xd) * freq) - this.sim.width/2;
					 int ysnap = (int)(Math.round(yd) * freq) - this.sim.width/2;

					 //

					 Vector2 snap = new Vector2(xsnap, ysnap);
					 snap.sub(p);
					 snap.normalize();

					 // Replace.
					 this.vectors[i][j] = snap;
				}
	 }

}
