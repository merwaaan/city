import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import org.graphstream.graph.*;
import org.graphstream.ui.swingViewer.util.DefaultMouseManager;

public class MouseManager extends DefaultMouseManager {

	 private Simulation sim;

	 private int screenshotIndex = 1;

	 public MouseManager(Simulation sim) {

		  this.sim = sim;
	 }

	 public void mouseClicked(MouseEvent event) {

		  // Left click: insert a new node

		  if(event.getButton() == MouseEvent.BUTTON1) {

				int xPx = event.getX();
				int yPx = event.getY();

				Rectangle bounds = this.view.getBounds();
				double xPxCentered = xPx - bounds.getWidth() / 2;
				double yPxCentered = -(yPx - bounds.getHeight() / 2);

				double[] coordsGu = this.sim.px2gu(xPxCentered, yPxCentered);

				CityOps.insertLot(coordsGu[0], coordsGu[1], this.sim);

				// Save a screenshot.
				this.sim.lots.addAttribute("ui.screenshot", "../screenshot" + screenshotIndex++ + ".png");
		  }

		  // Right click: display a different vector field.

		  else if(event.getButton() == MouseEvent.BUTTON3) {

				PotentialLotStrategy strategy = (PotentialLotStrategy)this.sim.strategies.get("potential lot construction");
				if(strategy == null)
					 return;

				if(this.sim.showWhichVectorField == strategy.fields.size() - 1)
					 this.sim.showWhichVectorField = -1;
				else
					 ++this.sim.showWhichVectorField;
		  }
	 }
}
