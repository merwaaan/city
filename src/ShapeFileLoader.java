import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;

import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.Geometry;

final class ShapeFileLoader {

	 public static void load(String fileName, Simulation sim) {

		  ArrayList<Point> points = new ArrayList<Point>();

		  // Extreme coordinates must be recorded.
		  double left = Double.POSITIVE_INFINITY;
		  double right = Double.NEGATIVE_INFINITY;
		  double bottom = Double.POSITIVE_INFINITY;
		  double top = Double.NEGATIVE_INFINITY;

		  try {

				// Load the shapefile.
				File file = new File(fileName);
				if(file == null) {
					 System.err.println("Impossible to load file " + fileName);
					 return;
				}

				Map<String, URL> connect = new HashMap<String, URL>();
				connect.put("url", file.toURL());

				DataStore store = DataStoreFinder.getDataStore(connect);
				String[] typeNames = store.getTypeNames();
				String typeName = typeNames[0];

				FeatureCollection features = store.getFeatureSource(typeName).getFeatures();

				// Go through each and every polygon.
				FeatureIterator iterator = features.features();
				while(iterator.hasNext()) {

					 SimpleFeature f = (SimpleFeature)iterator.next();

					 Point centroid = ((MultiPolygon)f.getDefaultGeometry()).getCentroid();

					 if(Math.random() < 0.4) {
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
		  for(int i = 0, l = points.size(); i < l; ++i)
			  coords.add(points.get(i).getCoordinate());

		  // Center the points at (0,0) and scale.
		  double xoffset = Math.abs(left - right) / 2;
		  double yoffset = Math.abs(bottom - top) / 2;

		  for(Coordinate c : coords) {
			  c.x = (c.x - left - xoffset);
			  c.y = (c.y - bottom - yoffset);
		  }

		  sim.lotCoords = coords;
		  sim.width = (int)(right - left);
	 }
}
