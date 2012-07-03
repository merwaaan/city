import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.triangulate.VoronoiDiagramBuilder;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

public class LeHavreConfiguration extends Configuration {

    public LeHavreConfiguration(Simulation sim) {

	super(sim);
    }

    void load() {

	// Get the land lots data from a shapefile.
	ShapeFileLoader shpLoader = new ShapeFileLoader(this.sim);
	shpLoader.load("data/le_havre.shp", this.sim);

	// Build a Voronoi diagram for which seeds are the previously
	// computed coordinates.
	Geometry voronoi = LotOps.voronoiDiagram(this.sim.lotCoords);

	// Build the two graphs based on the diagram.
	LotOps.buildLotsGraph(voronoi, this.sim);
	RoadOps.buildRoadsGraph(voronoi, this.sim);

	// Approximate the road network construction from the shape
	// file. Basic intersection tests between Voronoi edges and a
	// subdivision of the true roads are used so the result can be
	// inaccurate.
	for(Edge e : this.sim.roads.getEachEdge()) {

	    Node c0 = e.getNode0();
	    Node c1 = e.getNode1();
	    
	    double c0x = (Double)c0.getAttribute("x");
	    double c0y = (Double)c0.getAttribute("y");
	    double c1x = (Double)c1.getAttribute("x");
	    double c1y = (Double)c1.getAttribute("y");
	    Coordinate[] c0c1  = {new Coordinate(c0x, c0y), new Coordinate(c1x, c1y)};

	    LineString line = this.sim.geomFact.createLineString(c0c1);

	    for(LineString ls : this.sim.trueRoad)
		if(line.crosses(ls)) {
		    RoadOps.buildRoad(e);
		    break;
		}
	}

	// Report the densities from the shape file.
	Coordinate o = new Coordinate(0, 0);
	if(this.sim.shpDensities != null)
	    for(Node lot : this.sim.lots) {

		double x = (Double)lot.getAttribute("x");
		double y = (Double)lot.getAttribute("y");
		Coordinate c = new Coordinate(x, y);

		Density d = this.sim.shpDensities.get(c);
		if(d != null) {

		    // A little bit of cheating here: as we use a
		    // partial view of Le Havre, the lots to the east
		    // have high densities whereas they should be
		    // lower in our virtual city. We cut a radius
		    // around the center of the sub-city and randomly
		    // spawn medium density areas.
		    if(c.distance(o) > 1000)
			if(this.sim.rnd.nextDouble() < 0.03)
			    d = Density.MEDIUM;
			else
			    d = Density.LOW;

		    lot.setAttribute("density", d);
		    LotOps.buildLot(lot);
		}
	    }
    }

    void initStrategies() {

	this.sim.strategies.put("cellular automata", new DensityStrategy(this.sim));

	this.sim.strategies.put("road development", new RoadStrategy(4, this.sim));

	this.sim.strategies.put("lot construction", new LotStrategy(0.5, this.sim));

	this.sim.strategies.put("potential lot construction", new PotentialLotStrategy(0.3, this.sim));
    }

}
