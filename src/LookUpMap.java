import java.util.HashMap;

import org.graphstream.graph.Node;

import com.vividsolutions.jts.geom.Polygon;

class LookUpMap {

	 private HashMap<String, LookUp> map;

	 public LookUpMap() {

		  this.map = new HashMap<String, LookUp>();
	 }

	 public LookUp get(String id) {

		  if(id == null)
				return null;

		  return this.map.get(id);
	 }

	 public void setNode(String id, Node node) {

		  if(this.map.get(id) == null)
				this.map.put(id, new LookUp());

		  this.map.get(id).node = node;
	 }

	 public void setPolygon(String id, Polygon polygon) {

		  if(this.map.get(id) == null)
				this.map.put(id, new LookUp());

		  this.map.get(id).polygon = polygon;
	 }

	 public void setAttribute(String id, String attrName, Object obj) {

		  if(this.map.get(id) == null)
				this.map.put(id, new LookUp());

		  this.map.get(id).setAttribute(attrName, obj);
	 }

	 public Object getAttribute(String id, String attrName) {

		  if(this.map.get(id) == null)
				return null;

		  return this.map.get(id).getAttribute(attrName);
	 }
}
