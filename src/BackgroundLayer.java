import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.stream.SinkAdapter;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.swingViewer.LayerRenderer;

public class BackgroundLayer implements LayerRenderer {

	 private Simulation sim;

	 private int faded = 30;

	 private Map<Node, Transition> transitions;

	 private long transitionDuration = 1000;

	 public BackgroundLayer(Simulation sim) {

		  this.sim = sim;

		  this.transitions = new ConcurrentHashMap<Node, Transition>();

		  this.sim.lots.addSink(new LotWatcher(this.sim, this));
	 }

	 public void render(Graphics2D g, GraphicGraph graph, double ratio, int w, int h, double minx, double miny, double maxx, double maxy) {

		  this.updateTransitions();

		  // Save the transformation matrix.
		  g = (Graphics2D)g.create();

		  // Set the center of the screen as the origin.
		  g.translate(w/2, h/2);

		  // Scale according to the gu to px ratio and reverse the
		  // y-axis.
		  g.scale(ratio, -ratio);

		  // ART!
		  drawLots(g);
		  drawRoads(g);

		  // Restore the transformation matrix.
		  g.dispose();
	 }

	 /**
	  * Draws the lots composing the urban system.
	  *
	  * Only Voronoi cells are drawn as the neighborhood relationships
	  * are handled by the GS viewer.
	  */
	 private void drawLots(Graphics2D g) {

		  for(Node lot : this.sim.lots) {

				// Only draw potential lots if the option is enabled.
				if(!this.sim.showPotentialLots && !LotOps.isLotBuilt(lot))
					 continue;

				Polygon poly = (Polygon)lot.getAttribute("polygon");
				if(poly == null)
					 continue;

				// Build a path going through each vertex.

				Coordinate[] vertices = poly.getCoordinates();
				GeneralPath path = new GeneralPath();

				path.moveTo(vertices[0].x, vertices[0].y);
				for(int i = 1, l = vertices.length; i < l; ++i) {
					 Coordinate nextPoint = vertices[i % vertices.length];
					 path.lineTo(nextPoint.x, nextPoint.y);
				}

				// Fill the cell according to density.
				Density density = (Density)lot.getAttribute("density");

				/*
				if(density != null)
					 g.setColor(density.color(LotOps.isLotBuilt(lot) ? 255 : this.faded));
				else
					 g.setColor(Color.GREEN);
				*/

				if(this.transitions.containsKey(lot))
					 g.setColor(this.transitions.get(lot).current);
				else {
					 Density d = (Density)lot.getAttribute("density");

					 if(d != null)
						  g.setColor(d.color(255));
					 else
						  g.setColor(Color.GREEN);
				}

				g.fill(path);

				// Stroke the cell.

				g.setColor(Color.GRAY);

				if(LotOps.isLotBuilt(lot))
					 g.setStroke(new BasicStroke(2));
				else
					 g.setStroke(new BasicStroke(1,
														  BasicStroke.CAP_BUTT,
														  BasicStroke.JOIN_MITER,
														  10,
														  new float[]{10},
														  0));

				g.draw(path);
		  }
	 }

	 /**
	  * Draws the road network.
	  */
	 private void drawRoads(Graphics2D g) {

		  g.setStroke(new BasicStroke(5));

		  for(Edge road : this.sim.roads.getEachEdge()) {

				// Only shows built roads.
				if(!RoadOps.isRoadBuilt(road))
					 continue;

				Node a = road.getNode0();
				Node b = road.getNode1();

				double aX = (Double)a.getAttribute("x");
				double aY = (Double)a.getAttribute("y");
				double bX = (Double)b.getAttribute("x");
				double bY = (Double)b.getAttribute("y");

				g.setColor(new Color(0, 126, 255));

				g.drawLine((int)aX, (int)aY, (int)bX, (int)bY);
		  }

		  /*
		  for(Node crossroad : this.sim.roads) {

				if(!RoadOps.isCrossroadBuilt(crossroad))
					 continue;

				double x = (Double)crossroad.getAttribute("x");
				double y = (Double)crossroad.getAttribute("y");

				g.setColor(new Color(0, 126, 255));

				g.fillOval((int)x - 5, (int)y - 5, 10, 10);
		  }
		  */
	 }

	 /**
	  * A sink watching for any attribute change in the lots network.
	  *
	  * When a lot change its state to another density type, the color
	  * transition is started.
	  */
	 class LotWatcher extends SinkAdapter {

		  Simulation sim;
		  BackgroundLayer layer;

		  LotWatcher(Simulation sim, BackgroundLayer layer) {

				this.sim = sim;
				this.layer = layer;
		  }

		  public void nodeAttributeChanged(String graphId, long time, String nodeId, String attr, Object oldVal, Object newVal) {

				if(!attr.equals("density"))
					 return;

				Node lot = this.sim.lots.getNode(nodeId);
				boolean built = LotOps.isLotBuilt(lot);

				Color c1 = ((Density)oldVal).color(255);
				long t1 = BackgroundLayer.this.sim.now;;

				Color c2 = ((Density)newVal).color(255);
				long t2 = t1 + BackgroundLayer.this.transitionDuration;

				this.layer.transitions.put(lot, new Transition(c1, t1, c2, t2));
		  }
	 }

	 /**
	  *
	  */
	 class Transition {

		  public Color color1;
		  public long t1;

		  public Color color2;
		  public long t2;

		  public Color current;

		  public Transition(Color color1, long t1, Color color2, long t2) {

				this.color1 = color1;
				this.t1 = t1;

				this.color2 = color2;
				this.t2 = t2;

				this.current = color1;
		  }
	 }

	 private void updateTransitions() {

		  Iterator it = this.transitions.entrySet().iterator();

		  while(it.hasNext()) {

				Map.Entry pair = (Map.Entry)it.next();

				updateTransition((Node)pair.getKey(), (Transition)pair.getValue(), this.sim.now);
		  }
	 }

	 private void updateTransition(Node lot, Transition transition, long now) {

		  float progress = (float)(now - transition.t1) / this.transitionDuration;

		  if(progress > 1)
				this.transitions.remove(lot);

		  int r1 = transition.color1.getRed();
		  int g1 = transition.color1.getGreen();
		  int b1 = transition.color1.getBlue();

		  int r2 = transition.color2.getRed();
		  int g2 = transition.color2.getGreen();
		  int b2 = transition.color2.getBlue();

		  int r = clamp(r1 + progress * (r2 - r1));
		  int g = clamp(g1 + progress * (g2 - g1));
		  int b = clamp(b1 + progress * (b2 - b1));

		  transition.current = new Color(r, g, b);
	 }

	 private int clamp(float x) {

		  x = x > 0 ? x : 0;
		  x = x < 255 ? x : 255;

		  return (int)x;
	 }

}
