import org.graphstream.ui.geom.Vector2;

abstract class VectorField {

	 public Vector2[][] vectors;

	 private int frequency;

	 public int n;
	 public int nn;

	 public Simulation sim;

	 public VectorField(Simulation sim, int frequency) {

		  this.sim = sim;
		  this.frequency = frequency;

		  this.n = this.sim.width / frequency;
		  this.nn = n * n;

		  this.vectors = new Vector2[n][n];

		  for(int i = 0; i < this.vectors.length; ++i)
				for(int j = 0; j < this.vectors[i].length; ++j)
					 this.vectors[i][j] = new Vector2();
	 }

	 abstract void compute();

	 public Vector2 influence(int x, int y, int radius) {

		  return new Vector2();
	 }

	 public Vector2 position(int i, int j) {

		  int x = -this.sim.width / 2 + j * this.frequency;
		  int y = -this.sim.width / 2 + i * this.frequency;

		  //System.out.println(i+" "+j+" "+x+" "+y);
		  return new Vector2(x, y);
	 }

	 public Vector2 vector(int i, int j) {

		  return this.vectors[i][j];
	 }

}
