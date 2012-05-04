import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import org.graphstream.algorithm.flow.FlowAlgorithm;
import org.graphstream.algorithm.flow.FordFulkersonAlgorithm;

public class Test {
	 public static void main(String[] args) {

		  new Simulation().run();
		  if(true) return;

		  Graph g = new SingleGraph("test");

		  Node n0 = g.addNode("0");
		  n0.setAttribute("x", -100);
		  n0.setAttribute("y", 0);

		  Node n1 = g.addNode("1");
		  n1.setAttribute("x", 0);
		  n1.setAttribute("y", -100);

		  Node n2 = g.addNode("2");
		  n2.setAttribute("x", 0);
		  n2.setAttribute("y", 0);

		  Node n3 = g.addNode("3");
		  n3.setAttribute("x", 0);
		  n3.setAttribute("y", 100);

		  Node n4 = g.addNode("4");
		  n4.setAttribute("x", 100);
		  n4.setAttribute("y", 0);

		  g.addEdge(n0+""+n1, n0, n1);
		  g.addEdge(n0+""+n2, n0, n2);
		  g.addEdge(n0+""+n3, n0, n3);
		  g.addEdge(n1+""+n4, n1, n4);
		  g.addEdge(n2+""+n4, n2, n4);
		  g.addEdge(n3+""+n4, n3, n4);

		  g.display(false);

		  FlowAlgorithm flowAlgo = new FordFulkersonAlgorithm();

		  flowAlgo.setCapacityAttribute("capacity");

		  flowAlgo.init(g, "0", "4");

		  for(Edge road : g.getEachEdge())
				road.setAttribute("capacity", (int)(Math.random() * 100));

		  System.out.println("CAPACITY ------------------");
		  for(Edge road : g.getEachEdge()) {
				Node nn1 = road.getNode0();
				Node nn2 = road.getNode1();
				System.out.println(road.getAttribute("capacity"));
		  }

		  flowAlgo.compute();

		  System.out.println("FLOW ------------------");
		  for(Edge road : g.getEachEdge()) {
				Node nn1 = road.getNode0();
				Node nn2 = road.getNode1();
				System.out.println(flowAlgo.getFlow(nn1, nn2));
		  }
	 }

}
