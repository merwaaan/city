import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.ui.geom.Vector2;

public class DensityField extends VectorField{

	 public DensityField(Simulation sim, int frequency) {
		  super(sim, frequency);
	 }

	 public void compute() {

		  // Each vector gets away from high density. The idea is to
		  // populate less dense areas when guiding the potential lot
		  // seeds.

		  for(int i = 0; i < this.vectors.length; ++i)
				for(int j = 0; j < this.vectors[i].length; ++j) {

					 // The base of the vector.
					 Vector2 p = position(i, j);

					 /*
					 // Get away from the center.

					 p.normalize();
					 this.vectors[i][j] = p;
					 */

					 // Get away from the density from the land lot the
					 // vector is positioned on.
					 Node lot = LotOps.getLotAt(p.x(), p.y(), this.sim);
					 if(lot == null || !LotOps.isLotBuilt(lot)) {
						 this.vectors[i][j] = new Vector2();
						 continue;
					 }

					 double x0 = (Double)lot.getAttribute("x");
					 double y0 = (Double)lot.getAttribute("y");

					 // Sum up the distances from the vector base to the
					 // centers of each neighboring lot, weighting with
					 // respect to density.

					 Vector2 v = new Vector2();

					 for(int k = 0, l = lot.getDegree(); k < l; ++k) {

						  Node neighbor = lot.getEdge(k).getOpposite(lot);

						  if(!LotOps.isLotBuilt(neighbor))
								continue;

						  double x1 = (Double)neighbor.getAttribute("x");
						  double y1 = (Double)neighbor.getAttribute("y");

						  // Compute the vector separating the two points.
						  Vector2 d = new Vector2(x1, y1);
						  d.sub(new Vector2(x0, y0));
						  d.normalize();

						  // Weight with respect to density;
						  Density density = (Density)lot.getAttribute("density");
						  d.scalarMult(density.value());

						  v.add(d);
					 }

					 // Reverse the direction so that the vector get away
					 // from high densities instead of moving towards it.
					 v.scalarMult(-1);
					 v.normalize();

					 // Replace.
					 this.vectors[i][j] = v;
				}
	 }

}
