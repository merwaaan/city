import org.graphstream.graph.Node;

public class Measure {

	 public double averageCrossroadDegree(Simulation sim) {

		  double total = 0;
		  int n = sim.roads.getNodeCount();

		  // Check if there is at least one crossroad. We would not want
		  // to divide by zero.
		  if(n == 0)
				return 0;

		  for(Node crossroad : sim.roads)
				total += crossroad.getDegree();

		  return total / n;
	 }


}
