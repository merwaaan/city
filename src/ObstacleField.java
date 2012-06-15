import java.util.ArrayList;
import java.util.List;

import org.graphstream.ui.geom.Vector2;

public class ObstacleField extends VectorField{

	 public List<Obstacle> obstacles;

	 public ObstacleField(Simulation sim, int frequency) {
		  super(sim, frequency);

		  this.obstacles = this.sim.obstacles = new ArrayList<Obstacle>();

		  obstacles.add(new Obstacle(new Vector2(-1900, -2500), 2000, 3000));
		  obstacles.add(new Obstacle(new Vector2(0, -6000), 4000, 5000));
		  obstacles.add(new Obstacle(new Vector2(-5500, 0), 4000, 5000));

		  //obstacles.add(new Obstacle(new Vector2(700, -700), 500));
	 }

	 public void compute() {

		  // Each vector gets away from the closest obstacle.

		  for(int i = 0; i < this.vectors.length; ++i)
				for(int j = 0; j < this.vectors[i].length; ++j) {

					 // The base of the vector.
					 Vector2 p = position(i, j);

					 List<Obstacle> obstacles = obstaclesAt(p);

					 Vector2 evasion = new Vector2();
					 for(Obstacle o : obstacles) {

						  Vector2 base = new Vector2(o.position);
						  base.sub(p);

						  double d = base.length();

						  // If the vector is inside the obstacle, its
						  // length is 1.
						  if(d < o.radius) {
								base.normalize();
								evasion.add(base);
						  }
						  // If it is inside the falloff radius, its length
						  // is within [0,1].
						  else if(d < o.falloff) {
								double ratio = 1 - (d - o.radius) / (o.falloff - o.radius);
								base.normalize();
								base.scalarMult(ratio);
								evasion.add(base);
						  }
					 }

					 // Scale the vector between 0 and 1.
					 evasion.set(evasion.x() / obstacles.size(), evasion.y() / obstacles.size());

					 // Reverse the direction.
					 evasion.scalarMult(-1);

					 // Replace.
					 this.vectors[i][j] = evasion;
				}
	 }

	 private List<Obstacle> obstaclesAt(Vector2 pos) {

		  List<Obstacle> obstacles = new ArrayList<Obstacle>();

		  for(Obstacle o : this.obstacles) {

				Vector2 base = new Vector2(o.position);
				base.sub(pos);

				if(base.length() < o.radius + o.falloff)
					 obstacles.add(o);
		  }

		  return obstacles;
	 }

}
