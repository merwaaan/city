import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.stream.SinkAdapter;
import org.graphstream.ui.geom.Vector2;
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

		  this.sim.lots.addSink(new TransitionSink(this.sim, this));
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
		  drawVectorField(g);
		  drawPaths(g);
		  drawObstacles(g);

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

				/*
				// Only draw bordering lots if the option is enabled.
				if(!this.sim.showLargeCells && LotOps.isLargeCell(lot))
					 continue;
				*/

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

				if(LotOps.isLotBuilt(lot)) {

					 if(this.transitions.containsKey(lot))
						  g.setColor(this.transitions.get(lot).current);
					 else {
						  Density d = (Density)lot.getAttribute("density");

						  if(d != null)
								g.setColor(d.color(LotOps.isLotBuilt(lot) || true ? 255 : this.faded));
						  else
								g.setColor(Color.WHITE);
					 }

					 g.fill(path);
				}

				g.setColor(Color.GRAY);
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
				g.setStroke(new BasicStroke(3));

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

	 private void drawVectorField(Graphics2D g) {

		  PotentialLotStrategy strategy = (PotentialLotStrategy)this.sim.strategies.get("potential lot construction");
		  if(strategy == null)
				return;

		  if(this.sim.showWhichVectorField < 0)
				return;

		  VectorField field = strategy.fields.get(this.sim.showWhichVectorField);

		  for(int i = 0; i < field.vectors.length; ++i)
				for(int j = 0; j < field.vectors[i].length; ++j) {

					 g.setColor(Color.ORANGE);

					 Vector2 pos = field.position(i, j);
					 int x1 = (int)pos.x();
					 int y1 = (int)pos.y();

					 g.fillOval(x1 - 5, y1 - 5, 10, 10);

					 Vector2 vec = field.vectors[i][j];
					 int x2 = x1 + (int)(vec.x() * 50);
					 int y2 = y1 + (int)(vec.y() * 50);

					 g.drawLine(x1, y1, x2, y2);
				}
	 }

	 private void drawPaths(Graphics2D g) {

		  g.setColor(Color.GREEN);

		  for(int i = 0, l = this.sim.paths.size(); i < l; ++i) {

				List<Vector2> path = this.sim.paths.get(i);

				GeneralPath p = new GeneralPath();

				g.fillOval((int)path.get(0).x()-5, (int)path.get(0).y()-5, 10, 10);

				p.moveTo(path.get(0).x(), path.get(0).y());
				for(int j = 1, l2 = path.size(); j < l2; ++j)
					 p.lineTo(path.get(j).x(), path.get(j).y());

				g.draw(p);
		  }
	 }

	 private void drawObstacles(Graphics2D g) {

		  if(sim.obstacles == null)
				return;

		  g.setColor(Color.BLACK);

		  for(Obstacle o : this.sim.obstacles) {

				int x = (int)o.position.x();
				int y = (int)o.position.y();
				int r = (int)o.radius;

				g.drawOval(x-r, y-r, r*2, r*2);
		  }
	 }

	 /**
	  * A sink watching for any attribute change in the lots network.
	  *
	  * When a lot change its state to another density type, a color
	  * transition is started.
	  */
	 class TransitionSink extends SinkAdapter {

		  Simulation sim;
		  BackgroundLayer layer;

		  TransitionSink(Simulation sim, BackgroundLayer layer) {

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
	  * Holds color transition data.
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
