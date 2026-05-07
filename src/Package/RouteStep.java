package Package;

public class RouteStep {

	String fromStation;
	String toStation;
	String lineColour;
	double time;
	
	public RouteStep(String fromStation, String toStation, String lineColour, double time) {
		
		this.fromStation = fromStation;
		this.toStation = toStation;
		this.lineColour = lineColour;
		this.time = time;
		
	}
}

