import java.util.ArrayList;
import java.util.List;

import org.graphstream.ui.geom.Vector2;

public class ObstacleField extends VectorField{

	 public List<Obstacle> obstacles;

	 public ObstacleField(Simulation sim, int frequency) {
		  super(sim, frequency);

		  this.obstacles = this.sim.obstacles = new ArrayList<Obstacle>();

		  //obstacles.add(new Obstacle(new Vector2(600, -600), 400));
	 }

	 public void compute() {

		  // Each vector gets away from the closest obstacle.

		  for(int i = 0; i < this.vectors.length; ++i)
				for(int j = 0; j < this.vectors[i].length; ++j) {

					 // The base of the vector.
					 Vector2 p = position(i, j);

					 // Find the closest obstacle.
					 Obstacle o = closestObstacle(p);
					 if(o == null)
						 continue;

					 Vector2 sep = new Vector2(o.position);
					 sep.sub(p);

					 // Cut if the vector is not in the radius.
					 if(sep.length() > o.radius)
						  continue;

					 // Reverse the direction and normalize.
					 sep.scalarMult(-1);
					 sep.normalize();

					 // Replace.
					 this.vectors[i][j] = sep;
				}
	 }

	 private Obstacle closestObstacle(Vector2 p) {

		  Obstacle bestObstacle = null;
		  double bestDistance = Double.POSITIVE_INFINITY;

		  for(Obstacle o : this.obstacles) {

				Vector2 sep = new Vector2(o.position);
				sep.sub(p);

				double d = sep.length();

				if(d < bestDistance) {
					 bestObstacle = o;
					 bestDistance = d;
				}
		  }

		  return bestObstacle;
	 }

}
