import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.swingViewer.LayerRenderer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

public class BackgroundLayer implements LayerRenderer {

	 private Simulation sim;

	 public BackgroundLayer(Simulation sim) {

		  this.sim = sim;
	 }

	 public void render(Graphics2D g, GraphicGraph graph, double ratio, int w, int h, double minx, double miny, double maxx, double maxy) {

		  // Save the transformation matrix.
		  g = (Graphics2D)g.create();

		  // Set the center of the screen as the origin.
		  g.translate(w/2, h/2);

		  // Scale according to the gu to px ratio and reverse the
		  // y-axis.
		  g.scale(ratio, -ratio);

		  // Draw the Voronoi cells.
		  for(Node lot : this.sim.lots) {

				Polygon poly = (Polygon)lot.getAttribute("polygon");
				if(poly != null) {

					 Coordinate[] vertices = poly.getCoordinates();

					 GeneralPath path = new GeneralPath();
					 path.moveTo(vertices[0].x, vertices[0].y);

					 for(int i = 1, l = vertices.length; i < l; ++i) {
						  Coordinate nextPoint = vertices[i % vertices.length];
						  path.lineTo(nextPoint.x, nextPoint.y);
					 }

					 // Color the cell according to density if the
					 // corresponding attribute is present.
					 Object density = lot.getAttribute("density");

					 if(density != null && density instanceof Double) {

						  double d = ((Double)density).doubleValue();
						  int r = (int)(d * 255);
						  g.setColor(new Color(r, 0, 0));
						  g.fill(path);
					 }
					 else if(density != null && density instanceof Density) {
						  //System.out.println(density);
						  switch((Density)density) {
						  case EMPTY:
								g.setColor(Color.WHITE);
								break;
						  case LOW:
								g.setColor(new Color(255, 0, 0, 100));
								break;
						  case HIGH:
								g.setColor(new Color(255, 0, 0, 200));
								break;
						  }

						  g.fill(path);
					 }

					 g.setColor(Color.GRAY);
					 g.draw(path);
				}
		  }

		  // Draw the road network.

		  g.setColor(Color.BLUE);
		  g.setStroke(new BasicStroke(4));

		  for(Edge road : this.sim.roads.getEachEdge()) {

				Node a = road.getNode0();
				Node b = road.getNode1();

				double aX = (Double)a.getAttribute("x");
				double aY = (Double)a.getAttribute("y");
				double bX = (Double)b.getAttribute("x");
				double bY = (Double)b.getAttribute("y");

				g.drawLine((int)aX, (int)aY, (int)bX, (int)bY);
		  }

		  for(Node crossroad : this.sim.roads) {

				double x = (Double)crossroad.getAttribute("x");
				double y = (Double)crossroad.getAttribute("y");

				if(crossroad.hasAttribute("c")) {
					 g.setColor(Color.YELLOW);
					 g.fillOval((int)x - 5, (int)y - 5, 10, 10);
					 g.setColor(Color.BLUE);
				}
				else
					 g.fillOval((int)x - 5, (int)y - 5, 10, 10);
		  }

		  // Restore the transformation matrix.
		  g.dispose();
	 }
}
