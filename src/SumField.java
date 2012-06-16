import java.util.List;

import org.graphstream.ui.geom.Vector2;

public class SumField extends VectorField{

	 private List<VectorField> fields;
	 private double[] weights;

	 public SumField(Simulation sim, int frequency, List<VectorField> fields, double[] weights) {
		  super(sim, frequency);

		  this.fields = fields;
		  this.weights = weights;
	 }

	 public void compute() {

		  for(int i = 0; i < this.vectors.length; ++i)
				for(int j = 0; j < this.vectors[i].length; ++j) {

					 Vector2 all = new Vector2();

					 for(int k = 0; k < this.fields.size(); ++k) {

						  VectorField field = this.fields.get(k);

						  Vector2 v = new Vector2(field.vectors[i][j]);

						  v.scalarMult(this.weights[k]);

						  all.add(v);
					 }

					 all.normalize();

					 this.vectors[i][j] = all;
				}
	 }

}
