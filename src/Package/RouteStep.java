package Package;

// this class stores one step of a completed route, it stores the starting station, destination, line colour used and the travel time
public class RouteStep {

	// stores the station the journey step starts from
	String fromStation;
	
	// stores the station this step travels to
	String toStation;
	
	// stores the line colour used
	String lineColour;
	
	// stores the travel time for this step
	double time;
	
	// the constructor method used when creating a new route step
	public RouteStep(String fromStation, String toStation, String lineColour, double time) {
		
		this.fromStation = fromStation;
		this.toStation = toStation;
		this.lineColour = lineColour;
		this.time = time;
		
	}
}

