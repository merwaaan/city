import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import org.graphstream.ui.swingViewer.util.DefaultMouseManager;

public class MouseManager extends DefaultMouseManager {

	 private Simulation sim;

	 public MouseManager(Simulation sim) {

		  this.sim = sim;
	 }

	 public void mouseClicked(MouseEvent event) {

		  int xPx = event.getX();
		  int yPx = event.getY();

		  Rectangle bounds = this.view.getBounds();
		  double xPxCentered = xPx - bounds.getWidth() / 2;
		  double yPxCentered = -(yPx - bounds.getHeight() / 2);

		  double[] coordsGu = this.sim.px2gu(xPxCentered, yPxCentered);
		  System.out.println(xPxCentered+" "+yPxCentered);
		  System.out.println(coordsGu[0]+" "+coordsGu[1]);

		  this.sim.insertLot(coordsGu[0], coordsGu[1]);
	 }
}
