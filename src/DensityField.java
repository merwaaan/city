import org.graphstream.ui.geom.Vector2;

public class DensityField extends VectorField{

	 public DensityField(Simulation sim, int frequency) {
		  super(sim, frequency);

		  for(int i = 0; i < this.vectors.length; ++i)
				for(int j = 0; j < this.vectors[i].length; ++j)
					 this.vectors[i][j] = new Vector2(this.sim.rnd.nextInt(5), this.sim.rnd.nextInt(5));
	 }

}
