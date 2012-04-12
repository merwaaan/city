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

		  for(Node lot : this.sim.lots) {

				LookUp data = this.sim.lotsData.get(lot.getId());
				Polygon poly = data.polygon;

				Coordinate[] vertices = poly.getCoordinates();

				GeneralPath path = new GeneralPath();
				path.moveTo(vertices[0].x, vertices[0].y);

				for(int j = 1, l2 = vertices.length; j < l2; ++j) {

					 Coordinate nextPoint = vertices[j % vertices.length];
					 path.lineTo(nextPoint.x, nextPoint.y);
				}

				float density = (Float)data.getAttribute("density");
				int alpha = (int)(density * 255);
				g.setColor(new Color(255, 0, 0, alpha));
				g.fill(path);

				g.setColor(Color.ORANGE);
				g.draw(path);
		  }
	 }
}
