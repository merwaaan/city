import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.geom.Vector2;

import java.util.Random;

public class Simulation {

	 private Graph g;

	 public Simulation() {

		  this.g = new SingleGraph("roads");

		  Node n1 = this.g.addNode("1");
		  n1.setAttribute("x", 0);
		  n1.setAttribute("y", 0);

		  Node n2 = this.g.addNode("2");
		  n2.setAttribute("x", 10);
		  n2.setAttribute("y", 0);

		  this.g.addEdge("1-2", "1", "2");

		  this.g.display(false);
	 }

	 public void run() {

		  for(int i = 0; i < 30; ++i)
				this.newSegment();
	 }

	 private void newSegment() {

		  // Select a random seed node.
		  Node seed = null;
		  do {
				seed = Toolkit.randomNode(this.g, new Random());
		  } while(seed.getDegree() > 3);

		  // Expand the seed.

		  int seedX = (Integer)seed.getAttribute("x");
		  int seedY = (Integer)seed.getAttribute("y");

		  double angle = 0;
		  for(int i = 0, l = seed.getDegree(); i < l; ++i) {

				Edge e = seed.getEdge(i);
				Node n = e.getOpposite(seed);
				int nX = (Integer)n.getAttribute("x");
				int nY = (Integer)n.getAttribute("y");

				double a = Math.atan2(nY - seedY, nX - seedX);
				angle += a;
				System.out.println("> " + (a * 180 / Math.PI));
				System.out.println(">> " + a);
		  }
		  angle /= seed.getDegree();

		  System.out.println(angle * 180 / Math.PI);

		  //if(angle > 0.1 && angle < 0.1)
		  //		angle = Math.PI / 2;

		  Vector2 dir = new Vector2();
		  dir.set(0, Math.cos(angle));
		  dir.set(1, Math.sin(angle));
		  dir.normalize();
		  dir.scalarMult(-1);
		  dir.scalarMult(10);

		  int newX = (int)(seedX + dir.x());
		  int newY = (int)(seedY + dir.y());

		  Node newSegment = this.g.addNode("" + this.g.getNodeCount() + 1);
		  newSegment.setAttribute("x", newX);
		  newSegment.setAttribute("y", newY);
		  this.g.addEdge(seed.getId() + "-" + newSegment.getId(), seed.getId(), newSegment.getId());
	 }
}
