import org.graphstream.ui.geom.Vector2;

/**
 * An obstacle can represent any king of unconstructible area : body
 * of water, forests, ...
 *
 * For the sake of simplicity, only circular shapes are considered.
 */
public class Obstacle {

	 /**
	  * Center of the obstacle.
	  */
	 public Vector2 position;

	 /**
	  * Radius of the obstacle.
	  */
	 public double radius;

	 /**
	  * Supplementary radius in which the influence decreases.
	  */
	 public double falloff;

	 public Obstacle(Vector2 position, double radius, double falloff) {

		  this.position = position;
		  this.radius = radius;
		  this.falloff = falloff;
	 }
}
