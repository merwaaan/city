import org.graphstream.graph.Node;

public class LotConstructionStrategy extends AbstractStrategy {

	 public LotConstructionStrategy(Simulation sim) {
		  super(sim);
	 }

	 void prepare() {
	 }

	 public void update() {

		  for(Node lot : this.sim.lots)
				if(!LotOps.isLotBuilt(lot) && LotOps.isNextToBuiltRoad(lot))
					 LotOps.buildLot(lot);
	 }

}
