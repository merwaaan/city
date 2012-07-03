abstract class Configuration {

    protected Simulation sim;

    public Configuration(Simulation sim) {

	this.sim = sim;

	/*
	 * First step: load the geographical data that will help
	 * populate the road network graph and the land lot graph.
	 */
	this.load();

	/*
	 * Second step: choose the strategies that will be used to
	 * guide the evolution of the city.
	 */
	this.initStrategies();
    }

    abstract void load();
    abstract void initStrategies();
}
