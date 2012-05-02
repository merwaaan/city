import java.util.HashSet;
import java.util.Set;

import org.graphstream.graph.Node;

/**
 * A object holding data representing a crossroad.
 *
 * <p>It is typically instantiated during the construction of the road
 * network to store data about crossroads which nodes have not been
 * created yet.</p>
 *
 * <p>It is later kept to serve as a pivot between the node of the
 * road network representing a crossroad and the nodes of the land
 * lots graph surrounding it.</p>
 */
public class Crossroad {

	 private Set<Node> lots;

	 public double x;
	 public double y;

	 public Node node;

	 public Crossroad() {

		  this.lots = new HashSet<Node>();
	 }

	 public void addLot(Node lot) {

		  this.lots.add(lot);
	 }

	 public Set<Node> getLots() {

		  return this.lots;
	 }

	 public boolean containsLot(Node lot) {

		  return this.lots.contains(lot);
	 }

	 public int size() {

		  return this.lots.size();
	 }

	 public boolean equals(Object o) {

		  if(o instanceof Crossroad) {

				Crossroad c = (Crossroad)o;

				if(this.size() != c.size())
					 return false;

				for(Node lot : this.lots)
					 if(!c.containsLot(lot))
						  return false;
		  }

		  return true;
	 }

	 public int hashCode() {

		  return this.lots.hashCode();
	 }

	 public String toString() {

		  StringBuilder sb = new StringBuilder("[ ");

		  for(Node lot : this.lots) {
				sb.append(lot.getId());
				sb.append(" ");
		  }

		  sb.append("]");

		  return sb.toString();
	 }

}
