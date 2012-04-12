import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

import org.graphstream.graph.Node;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.swingViewer.LayerRenderer;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.Viewer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

public class RenderingLayer implements LayerRenderer {

	 private Simulation sim;

	 private View view;

	 public RenderingLayer(Simulation sim) {

		  this.sim = sim;
	 }

	 public void render(Graphics2D g, GraphicGraph graph, double ratio, int w, int h, double minx, double miny, double maxx, double maxy) {

		  g.translate(w/2, h/2);
		  g.scale(ratio, -ratio);

		  // Fill cells according to the density.

		  for(int i = 0, l = this.sim.voronoi.getNumGeometries(); i < l; ++i) {

				Polygon poly = (Polygon)this.sim.voronoi.getGeometryN(i);
				Coordinate[] vertices = poly.getCoordinates();

				GeneralPath path = new GeneralPath();
				path.moveTo(vertices[0].x, vertices[0].y);

				for(int j = 1, l2 = vertices.length; j < l2; ++j) {

					 Coordinate nextPoint = vertices[j % vertices.length];
					 path.lineTo(nextPoint.x, nextPoint.y);
				}

				g.setColor(Color.ORANGE);
				g.draw(path);

				g.setColor(new Color(255, 0, 0, 255));
				g.fill(path);
		  }

		  /*		  MPolygon[] regions = this.sim.v.getRegions();
		  for(int i = 0; i < regions.length; ++i) {

				g.setColor(new Color(255, 0, 0, 200));
		  }

		  // Draw edges.

		  g.setColor(Color.BLACK);

		  float[][] edges = this.sim.v.getEdges();
		  for(int i = 0; i < edges.length; ++i) {

				int x0 = (int)edges[i][0];
				int y0 = (int)edges[i][1];
				int x1 = (int)edges[i][2];
				int y1 = (int)edges[i][3];

				g.drawLine(x0, y0, x1, y1);
				}*/
	 }
}
