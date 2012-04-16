abstract class AbstractStrategy {

	 protected Simulation sim;

	 public AbstractStrategy(Simulation sim) {

		  this.sim = sim;

		  this.prepare();
	 }

	 abstract void prepare();

	 abstract void update();
}
