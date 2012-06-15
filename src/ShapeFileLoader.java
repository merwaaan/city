import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.distance.DistanceOp;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;

import org.graphstream.ui.geom.Vector2;

import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
//import org.opengis.geometry.Geometry;

class ShapeFileLoader {

	 private Simulation sim;

	 public ShapeFileLoader(Simulation sim) {
		  this.sim = sim;
	 }

	 public void load(String fileName, Simulation sim) {

		  List<Point> points = new ArrayList<Point>();

		  // Extreme coordinates must be recorded.
		  double left = Double.POSITIVE_INFINITY;
		  double right = Double.NEGATIVE_INFINITY;
		  double bottom = Double.POSITIVE_INFINITY;
		  double top = Double.NEGATIVE_INFINITY;

		  SimpleFeature roadFeature = null;
		  Map<Point, Density> shpDensities_ = new HashMap<Point, Density>();

		  try {

				URL url = this.getClass().getResource("./data/le_havre.shp");
				ShapefileDataStore store = new ShapefileDataStore(url);
				String name = store.getTypeNames()[0];
				FeatureSource source = store.getFeatureSource(name);
				FeatureCollection features = source.getFeatures();

				// Add every lot to the coordinates list.

				FeatureIterator iterator = features.features();
				while(iterator.hasNext()) {

					 SimpleFeature f = (SimpleFeature)iterator.next();

					 String type = (String)f.getAttribute("CODE");

					 if(type.equals("12220")) // ROAD
						  roadFeature = f;
					 else {

						  Point centroid = ((MultiPolygon)f.getDefaultGeometry()).getCentroid();

						  // Translate the file attributes to one of the
						  // three density types.
						  if(type.equals("11100"))
								shpDensities_.put(centroid, Density.HIGH);
						  else if(type.equals("11220") || type.equals("11210"))
								shpDensities_.put(centroid, Density.MEDIUM);
						  else
								shpDensities_.put(centroid, Density.LOW);

						  points.add(centroid);

						  if(centroid.getX() < left)
								left = centroid.getX();
						  if(centroid.getX() > right)
								right = centroid.getX();
						  if(centroid.getY() < bottom)
								bottom = centroid.getY();
						  if(centroid.getY() > top)
								top = centroid.getY();
					 }
				}

		  }
		  catch(Exception e) {
				e.printStackTrace();
		  }

		  // From ArrayList of Point to array of Coordinate.
		  List<Coordinate> coords = new ArrayList<Coordinate>();
		  Map<Coordinate, Density> shpDensities_2 = new HashMap<Coordinate, Density>();
		  for(Point p : points) {
			  Coordinate c = p.getCoordinate();
			  coords.add(c);
			  shpDensities_2.put(c, shpDensities_.get(p));
		  }

		  // Center the points at (0,0) and scale.
		  double xoffset = Math.abs(left - right) / 2;
		  double yoffset = Math.abs(bottom - top) / 2;

		  Map<Coordinate, Density> shpDensities_3 = new HashMap<Coordinate, Density>();
		  for(Coordinate c : coords) {

			  Density d = shpDensities_2.get(c);

			  c.x = (c.x - left - xoffset);
			  c.y = (c.y - bottom - yoffset);

			  shpDensities_3.put(c, d);
		  }

		  this.sim.shpDensities = shpDensities_3;

		  class Centerer implements CoordinateFilter {
				double l, ox, b, oy;
				public Centerer(double l, double ox, double b, double oy) {
					 this.l = l;
					 this.ox = ox;
					 this.b = b;
					 this.oy = oy;
				}
				public void filter(Coordinate c) {
					 c.x = (c.x - l - ox);
					 c.y = (c.y - b - oy);
				}
		  }

		  // Rebuild the road multi-polygon as a list of small sub-segments.

		  MultiPolygon roadGeometry = (MultiPolygon)roadFeature.getDefaultGeometry();
		  roadGeometry.apply(new Centerer(left, xoffset, bottom, yoffset));

		  List<LineString> roadLines = new ArrayList<LineString>();
		  int coordsPerLineString = 10; // length of the segments.

		  for(int i = 0; i < roadGeometry.getNumGeometries(); ++i) {

			  Polygon poly = (Polygon)roadGeometry.getGeometryN(i);

			  for(int j = 0; j < poly.getNumInteriorRing(); ++j) {

				  Coordinate[] polyCoords = poly.getInteriorRingN(j).getCoordinates();

				  int sections = (int)Math.ceil((double)polyCoords.length / coordsPerLineString);

				  for(int k = 0; k < sections; ++k) {

					  int index = k * coordsPerLineString;

					  Coordinate[] subCoords = Arrays.copyOfRange(polyCoords, index, index + Math.min(coordsPerLineString, polyCoords.length - index));

					  if(subCoords.length > 1)
						  roadLines.add(this.sim.geomFact.createLineString(subCoords));
				  }
			  }
		  }

		  //
		  sim.lotCoords = coords;
		  sim.width = (int)(right - left);
		  this.sim.trueRoad = roadLines;
	 }
}
