import com.vividsolutions.jts.geom.Geometry;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

public class RadialConfiguration extends Configuration {

    public RadialConfiguration(Simulation sim) {

	super(sim);
    }

    void load() {

	// The initial distribution is radial.
	this.sim.randomCoords(2500, 2000);
	
	// Build a Voronoi diagram for which seeds are the previously
	// computed coordinates.
	Geometry voronoi = LotOps.voronoiDiagram(this.sim.lotCoords);
	
	// Build the two graphs based on the diagram.
	LotOps.buildLotsGraph(voronoi, this.sim);
	RoadOps.buildRoadsGraph(voronoi, this.sim);
	
	// Prepare an initial configuration with gradual density from
	// the center.
	for(Node lot : this.sim.lots) {

	    double x = (Double)lot.getAttribute("x");
	    double y = (Double)lot.getAttribute("y");

	    double dist = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));

	    Density d;
	    if(dist < 100)
		d = Density.HIGH;
	    else if(dist < 250)
		d = Density.MEDIUM;
	    else
		d = Density.LOW;

	    lot.setAttribute("density", d);
	    LotOps.buildLot(lot);
	}
    }

    void initStrategies() {

	// First strategy: vertical growth. Each land lots has a
	// density type and they evolve according to their
	// neighborhoods like in any cellular automaton.

	double[][] affinities = {
	    {1, 0.01, 0},         // LOW
	    {0.001, 1.5, 0.0001}, // MEDIUM
	    {0, 0.005, 1.8}       // HIGH
	};

	this.sim.strategies.put("cellular automata", new DensityStrategy(affinities, this.sim));
    }

}
