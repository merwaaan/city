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

	 private int faded = 30;

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

				if(!this.sim.showPotentialLots && !LotOps.isLotBuilt(lot))
					 continue;

				Polygon poly = (Polygon)lot.getAttribute("polygon");
				if(poly != null) {

					 Coordinate[] vertices = poly.getCoordinates();

					 GeneralPath path = new GeneralPath();
					 path.moveTo(vertices[0].x, vertices[0].y);

					 for(int i = 1, l = vertices.length; i < l; ++i) {
						  Coordinate nextPoint = vertices[i % vertices.length];
						  path.lineTo(nextPoint.x, nextPoint.y);
					 }

					 // Color the cell according to density.
					 Density density = (Density)lot.getAttribute("density");

					 int alpha = LotOps.isLotBuilt(lot) ? 255 : this.faded;

					 if(density != null) {
						  switch(density) {
						  case EMPTY:
								g.setColor(new Color(255, 255, 255, alpha));
								break;
						  case LOW:
								g.setColor(new Color(255, 145, 145, alpha));
								break;
						  case HIGH:
								g.setColor(new Color(255, 48, 48, alpha));
								break;
						  }
					 }
					 else
						  g.setColor(Color.GREEN);

					 g.fill(path);

					 //

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

		  // Draw the road network.

		  g.setStroke(new BasicStroke(5));

		  for(Edge road : this.sim.roads.getEachEdge()) {

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

		  for(Node crossroad : this.sim.roads) {

				if(!RoadOps.isCrossroadBuilt(crossroad))
					 continue;

				double x = (Double)crossroad.getAttribute("x");
				double y = (Double)crossroad.getAttribute("y");

				g.setColor(new Color(0, 126, 255));

				g.fillOval((int)x - 5, (int)y - 5, 10, 10);
		  }

		  // Restore the transformation matrix.
		  g.dispose();
	 }
}
