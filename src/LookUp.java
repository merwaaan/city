import org.graphstream.graph.Node;

import com.vividsolutions.jts.geom.Polygon;

class LookUp {

	 public Node node;
	 public Polygon polygon;
	 //public Edge[] edges;

	 public LookUp() {

	 }

	 public void setAttribute(String attrName, Object obj) {

		  if(this.node == null)
				return;

		  this.node.setAttribute(attrName, obj);
	 }

	 public Object getAttribute(String attrName) {

		  if(this.node == null)
				return null;

		  return this.node.getAttribute(attrName);
	 }
}
