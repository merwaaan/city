import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

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

		  for(Node lot : this.sim.lots) {

				// Draw the associated Voronoi cell if the polygon exists.
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
					 Double densityD = (Double)lot.getAttribute("density");
					 if(densityD != null) {
						  double density = densityD.doubleValue();
						  int alpha = (int)(density * 255);
						  g.setColor(new Color(255, 0, 0, alpha));
						  g.fill(path);
					 }

					 g.setColor(Color.GRAY);
					 g.draw(path);
				}
		  }

		  // Restore the transformation matrix.
		  g.dispose();
	 }
}
