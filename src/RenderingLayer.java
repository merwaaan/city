import java.awt.Color;
import java.awt.Graphics2D;

import org.graphstream.graph.Node;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.swingViewer.LayerRenderer;

public class RenderingLayer implements LayerRenderer {

	 private Simulation sim;

	 public RenderingLayer(Simulation sim) {

		  this.sim = sim;
	 }

	 public void render(Graphics2D g, GraphicGraph graph, double ratio, int w, int h, double minx, double miny, double maxx, double maxy) {

		  g.translate(w/2, h/2);
		  g.scale(1, -1);

		  g.setColor(Color.ORANGE);

		  float[][] edges = this.sim.v.getEdges();
		  for(int i = 0; i < edges.length; ++i) {

				int x0 = (int)edges[i][0];
				int y0 = (int)edges[i][1];
				int x1 = (int)edges[i][2];
				int y1 = (int)edges[i][3];

				g.drawLine(x0, y0, x1, y1);
		  }

		  System.out.println(w + " "  +minx + " " + maxx);
		  System.out.println(""+(200 * ratio));
	 }
}
