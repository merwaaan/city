import java.util.ArrayList;
import java.util.List;

import org.graphstream.ui.geom.Vector2;

public class ObstacleField extends VectorField{

	 public List<Obstacle> obstacles;

	 public ObstacleField(Simulation sim, int frequency) {
		  super(sim, frequency);

		  this.obstacles = this.sim.obstacles = new ArrayList<Obstacle>();

		  obstacles.add(new Obstacle(new Vector2(-1900, -2500), 2000));
		  obstacles.add(new Obstacle(new Vector2(0, -6000), 4000));
		  obstacles.add(new Obstacle(new Vector2(-5500, 0), 4000));

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

						  evasion.add(base);
					 }
					 evasion.normalize();

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

				if(base.length() < o.radius)
					 obstacles.add(o);
		  }

		  return obstacles;
	 }

}
