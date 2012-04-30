import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.triangulate.VoronoiDiagramBuilder;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.ArrayList;

public class CityOps {

	 /**
	  * Builds a Voronoi diagram describing the city structure.
	  *
	  * @param coords An array of Coordinate containing the position of
	  * each land lot.
	  * @param if true, the resulting diagram will be clipped to the
	  * city shape.
	  */
	 public static Geometry voronoiDiagram(Coordinate[] coords, boolean clip) {

		  MultiPoint points = (new GeometryFactory()).createMultiPoint(coords);

		  VoronoiDiagramBuilder voronoiBuilder = new VoronoiDiagramBuilder();
		  voronoiBuilder.setSites(points);

		  Geometry voronoi = voronoiBuilder.getDiagram(new GeometryFactory());

		  /**
			* Clip the diagram with the buffered convex hull of the
			* individual centroids to avoid immense lots at the borders
			* of the environment.
			*
			* TODO: a buffered concave hull would be way better.
			* https://github.com/skipperkongen/jts-algorithm-pack
			*/

		  if(clip) {
				Geometry buffer = points.convexHull().buffer(30);
				voronoi = voronoi.intersection(buffer);
		  }

		  return voronoi;
	 }

	 /**
	  * Gives the general shape of the city.
	  *
	  * The result is based on the convex envelope of every land lot
	  * seeds which is then buffered a bit so that border cells are not
	  * pressed againt the city border.
	  *
	  * @param lots The graph containing every lots of the city.
	  *
	  * @return A Polygon to the shape of the city.
	  */
	 public static Polygon getCityHull(Graph lots) {

		  Coordinate[] coords = LotOps.getLotsCoordinates(lots);

		  MultiPoint points = (new GeometryFactory()).createMultiPoint(coords);

		  Polygon cityHull = (Polygon)points.convexHull().buffer(30);

		  return cityHull;
	 }

	 /**
	  * Dynamically inserts a new lot to the city.
	  *
	  * This method is typically called when adding a new lot during
	  * the simulation.
	  *
	  * @param x The x-axis position of the new lot.
	  * @param y The y-axis position of the new lot.
	  * @param lots The graph containg every lots of the city.
	  */
	 public static void insertLot(double x, double y, Graph lots) {

		  Coordinate coord = new Coordinate(x, y);
		  Point pos = (new GeometryFactory()).createPoint(coord);

		  /**
			* Find the existing cell within which the new lot will be
			* built. When found, add the new lot to the "lots" graph at
			* the appropriate position.
			*/

		  Node oldLot = LotOps.getLotAt(x, y, lots);

		  if(oldLot == null) {
				System.err.println("Tried to add a new lot in an empty space");
				return;
		  }

		  Node newLot = LotOps.placeLot(coord.x, coord.y, lots);

		  // XXX: For debugging purposes.
		  oldLot.setAttribute("ui.style", "fill-color: orange;");
		  newLot.setAttribute("ui.style", "fill-color: green;");

		  /**
			* Aggregate in a list each lot which polygon and neighborhood
			* could be affected by the construction of the new lot. They
			* are:
			*
		   * - the new lot (obviously)
			* - the old lot
			* - the neighbors of the old lot
			* - the neighbors of neighbors (!)
			*/

		  ArrayList<Node> subLots = new ArrayList<Node>();

		  subLots.add(newLot);

		  subLots.add(oldLot);

		  for(Edge e : oldLot.getEachEdge())
				subLots.add(e.getOpposite(oldLot));

		  for(Edge e1 : oldLot.getEachEdge()) {

				Node n1 = e1.getOpposite(oldLot);

				for(Edge e2 : n1.getEachEdge()) {

					 Node n2 = e2.getOpposite(n1);

					 if(!subLots.contains(n2))
						  subLots.add(n2);
				}
		  }

		  /**
			* Build an array of coordinates corresponding to the
			* centroids of each concerned lot then compute a Voronoi
			* sub-diagram.
			*/

		  Coordinate[] coords = new Coordinate[subLots.size()];

		  for(int i = 0, l = subLots.size(); i < l; ++i)
				coords[i] = new Coordinate((Double)subLots.get(i).getAttribute("x"), (Double)subLots.get(i).getAttribute("y"));

		  Geometry subVoronoi = voronoiDiagram(coords, false);

		  /**
			* Update each node with the polygons from the sub-diagram. To
			* avoid adding the immense cells at the borders of the
			* sub-diagram (the same kind of cells that need to be clipped
			* during the initialization process) to the global diagram,
			* we clip each polygon with its previous version as they can
			* only stay identical or shrink.
			*/
		  for(int i = 0, l = subVoronoi.getNumGeometries(); i < l; ++i) {

				Polygon subCell = (Polygon)subVoronoi.getGeometryN(i);

				for(int j = 0, l2 = coords.length; j < l2; ++j) {

					 Point subCoord = (new GeometryFactory()).createPoint(coords[j]);

					 if(subCoord.within(subCell)) {

						  Node lot = subLots.get(j);

						  Geometry newCell = null;

						  if(lot == newLot)
								newCell = subCell;
						  else if(lot != newLot) {

								Polygon oldCell = (Polygon)subLots.get(j).getAttribute("polygon");
								newCell = subCell.intersection(oldCell);

								/**
								 * In recurrent cases, the intersection
								 * returns a GeometryCollection instead of
								 * a Geometry and, as a consequence, the
								 * polygons are messed up.
								 *
								 * To fix this, we go through the
								 * sub-geometries and only keep the
								 * Polygon and get rid of the LineString.
								 */
								if(newCell instanceof GeometryCollection)
									 for(int k = 0, l3 = newCell.getNumGeometries(); k < l3; ++k)
										  if(newCell.getGeometryN(k) instanceof Polygon)
												newCell = newCell.getGeometryN(k);
						  }

						  lot.setAttribute("polygon", newCell);
					 }
				}
		  }

		  // XXX: Visually adjacent polygon are sometimes not detected
		  // as intersecting. I assume it's due to the JTS clipping
		  // creating a microscopic gap between adjacent cells. To
		  // bypass this undesirable effect we lightly grow each polygon
		  // before the neighborhood update.
		  //
		  // TODO: Fix it! (get rid of clipping and find an alternative)

		  for(Node lot : subLots) {
				Polygon polygon = (Polygon)lot.getAttribute("polygon");
				polygon = (Polygon)polygon.buffer(0.01);
				lot.setAttribute("polygon", polygon);
		  }

		  // Last step: update edges!
		  for(Node lot : subLots)
				LotOps.unlinkFromInvalidNeighbors(lot, lots);
		  for(Node lot : subLots)
				LotOps.linkToNeighbors(lot, lots);

		  LotOps.clipLotsToCity(lots);
	 }

}
