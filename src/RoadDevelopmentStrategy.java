import org.graphstream.stream.SinkAdapter;

public class RoadDevelopmentStrategy extends AbstractStrategy {

	 class RoadDevelopmentSink extends SinkAdapter {

		  Simulation sim;
		  RoadDevelopmentStrategy strategy;

		  RoadDevelopmentSink(Simulation sim, RoadDevelopmentStrategy strategy) {

				this.sim = sim;
				this.strategy = strategy;
		  }

		  public void edgeAdded(String graphId, long time, String edgeId) {
		  }
	 }

	 public RoadDevelopmentStrategy(Simulation sim) {
		  super(sim);

		  this.sim.lots.addElementSink(new RoadDevelopmentSink(this.sim, this));
	 }

	 void prepare() {

	 }

	 public void update() {

	 }

}
