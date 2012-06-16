import java.util.ArrayList;
import java.util.List;

import org.graphstream.ui.geom.Vector2;

public class ObstacleField extends VectorField{

	 public List<Obstacle> obstacles;

	// Flag used to save some computing power by generating this
	// static vector field only once.
	public static boolean alreadyComputed = false;

	 public ObstacleField(Simulation sim, int frequency) {
		  super(sim, frequency);

		  this.obstacles = this.sim.obstacles = new ArrayList<Obstacle>();

		  obstacles.add(new Obstacle(new Vector2(-1900, -2500), 2000, 3500));
		  obstacles.add(new Obstacle(new Vector2(0, -6000), 4000, 5000));
		  obstacles.add(new Obstacle(new Vector2(-5600, 0), 4000, 4500));

		  //obstacles.add(new Obstacle(new Vector2(700, -700), 500));
	 }

	 public void compute() {

		 if(this.alreadyComputed)
			 return;

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

					 // Reverse the direction.
					 evasion.scalarMult(-1);

					 // Replace.
					 this.vectors[i][j] = evasion;
				}

		  this.alreadyComputed = true;
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
