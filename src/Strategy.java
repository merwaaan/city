abstract class Strategy {

	 protected Simulation sim;

	 public Strategy(Simulation sim) {

		  this.sim = sim;

		  this.prepare();
	 }

	 abstract void prepare();

	 abstract void update();
}
