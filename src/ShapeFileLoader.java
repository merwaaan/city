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

	 public static List<Coordinate> getLandLots(String fileName, int radius) {

		  ArrayList<Point> points = new ArrayList<Point>();

		  try {

				// Load the shapefile.
				File file = new File(fileName);
				if(file == null) {
					 System.err.println("Impossible to load file " + fileName);
					 return null;
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

					 points.add(centroid);
				}

		  }
		  catch(Exception e) {
				e.printStackTrace();
		  }

		  // From ArrayList of Point to array of Coordinate.
		  List<Coordinate> coords = new ArrayList<Coordinate>();
		  for(int i = 0, l = points.size(); i < l; ++i)
			  coords.add(points.get(i).getCoordinate());

		  return coords;
	 }
}
