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

	 public Vector2 influence(double x, double y) {

		  // Compute the indexes from the position.
		  int j = (this.sim.width / 2 + (int)x) / this.frequency;
		  int i = (this.sim.width / 2 + (int)y) / this.frequency;

		  // Interpolation.

		  Vector2 pos = position(i, j);
		  double x0 = pos.x();
		  double y0 = pos.y();
		  double x1 = x0 + this.frequency;
		  double y1 = y0 + this.frequency;

		  Vector2 a = new Vector2(x0, y0);
		  Vector2 b = new Vector2(x1, y0);
		  Vector2 c = new Vector2(x1, y1);
		  Vector2 d = new Vector2(x0, y1);

		  //

		  double xratio = (x - x0) / (x1 - x0);

		  a.scalarMult(xratio);
		  b.scalarMult(1 - xratio);
		  a.add(b);
		  a.normalize();

		  d.scalarMult(xratio);
		  c.scalarMult(1 - xratio);
		  d.add(c);
		  d.normalize();

		  //

		  double yratio = (y - y0) / (y1 - y0);

		  a.scalarMult(yratio);
		  d.scalarMult(1- yratio);
		  a.add(d);
		  a.normalize();

		  return a;
	 }

	 public Vector2 position(int i, int j) {

		  int x = -this.sim.width / 2 + j * this.frequency;
		  int y = -this.sim.width / 2 + i * this.frequency;

		  return new Vector2(x, y);
	 }

	 public Vector2 vector(int i, int j) {

		  return this.vectors[i][j];
	 }

}
