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

		  boolean availablePotentialLots = true;

		  while(this.acc >= 1 && availablePotentialLots)
				for(int i = 0, l = this.sim.lots.getNodeCount(); i < l; ++i) {

					 Node lot = this.sim.lots.getNode(i);

					 if(!LotOps.isLotBuilt(lot) && !LotOps.isLargeCell(lot) && LotOps.isNextToBuiltRoad(lot)) {
						  LotOps.buildLot(lot);
						  --this.acc;
						  break;
					 }

					 if(i == l - 1)
						  availablePotentialLots = false;
				}
	 }

}
