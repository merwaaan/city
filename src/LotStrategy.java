import org.graphstream.graph.Node;

public class LotStrategy extends Strategy {

	 private double growthRate;
	 private double acc;

	 public LotStrategy(double growthRate, Simulation sim) {
		  super(sim);

		  this.growthRate = growthRate;
		  this.acc = 0;
	 }

	 void prepare() {
	 }

	 public void update() {

		  this.acc += this.growthRate;

		  while(this.acc >= 1)
				for(Node lot : this.sim.lots)
					 if(!LotOps.isLotBuilt(lot) && LotOps.isNextToBuiltRoad(lot)) {
						  LotOps.buildLot(lot);
						  --this.acc;
						  break;
					 }
	 }

}
