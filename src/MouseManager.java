import com.vividsolutions.jts.geom.Polygon;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import org.graphstream.graph.*;
import org.graphstream.ui.swingViewer.util.DefaultMouseManager;

public class MouseManager extends DefaultMouseManager {


	 private Simulation sim;

	 public MouseManager(Simulation sim) {

		  this.sim = sim;
	 }

	 public void mouseClicked(MouseEvent event) {

		  // Left click:

		  if(event.getButton() == MouseEvent.BUTTON1) {

				// Display the area of the clicked cell.
				/*
				int xPx = event.getX();
				int yPx = event.getY();

				Rectangle bounds = this.view.getBounds();
				double xPxCentered = xPx - bounds.getWidth() / 2;
				double yPxCentered = -(yPx - bounds.getHeight() / 2);

				double[] coordsGu = this.sim.px2gu(xPxCentered, yPxCentered);

				Node lot = LotOps.getLotAt(coordsGu[0], coordsGu[1], this.sim);
				Polygon cell = (Polygon)lot.getAttribute("polygon");
				System.out.println(cell.getArea());
				*/

				int xPx = event.getX();
				int yPx = event.getY();

				Rectangle bounds = this.view.getBounds();
				double xPxCentered = xPx - bounds.getWidth() / 2;
				double yPxCentered = -(yPx - bounds.getHeight() / 2);

				double[] coordsGu = this.sim.px2gu(xPxCentered, yPxCentered);

				this.sim.PLS.spawn(coordsGu[0], coordsGu[1]);

				// Interactive node insertion code.
				/*
				  int xPx = event.getX();
				  int yPx = event.getY();

				  Rectangle bounds = this.view.getBounds();
				  double xPxCentered = xPx - bounds.getWidth() / 2;
				  double yPxCentered = -(yPx - bounds.getHeight() / 2);

				  double[] coordsGu = this.sim.px2gu(xPxCentered, yPxCentered);

				  CityOps.insertLot(coordsGu[0], coordsGu[1], this.sim);
				*/
		  }

		  // Middle click: save screen shot.

		  else if(event.getButton() == MouseEvent.BUTTON2)
				this.sim.screenshot();

		  // Right click: display a different vector field.

		  else if(event.getButton() == MouseEvent.BUTTON3) {

				PotentialLotStrategy strategy = (PotentialLotStrategy)this.sim.strategies.get("potential lot construction");
				if(strategy == null)
					 return;

				if(this.sim.showWhichVectorField == strategy.fields.size() - 1)
					 this.sim.showWhichVectorField = -2;
				else
					 ++this.sim.showWhichVectorField;
		  }
	 }

}
