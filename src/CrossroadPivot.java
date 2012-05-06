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
public class CrossroadPivot {

	public Set<Node> lots;

	 public Node node;

	 public CrossroadPivot() {

		  this.lots = new HashSet<Node>();
	 }

	 public boolean equals(Object o) {

		  if(o instanceof CrossroadPivot) {

				CrossroadPivot c = (CrossroadPivot)o;

				if(!this.node.equals(c.node))
					return false;

				if(this.lots.size() != c.lots.size())
					 return false;

				for(Node lot : this.lots)
					 if(!c.lots.contains(lot))
						  return false;
		  }

		  return true;
	 }

	 public int hashCode() {

		  return this.lots.hashCode();
	 }

	 public String toString() {

		  StringBuilder sb = new StringBuilder("[ (" + this.node + ") ");

		  for(Node lot : this.lots) {
				sb.append(lot.getId());
				sb.append(" ");
		  }

		  sb.append("]");

		  return sb.toString();
	 }

}
