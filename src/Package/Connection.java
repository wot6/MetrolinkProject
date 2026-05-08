package Package;

public class Connection {
	
	// stores station this connection travels to
	String toStation;
	// stores the line colour for this connection
	String lineColour;
	//stores travel time in mins
	double time;
	
	// this is the constructor method used when creating a new connection object in main
	public Connection(String toStation, String lineColour, double time) {
		
		this.toStation = toStation;
		this.lineColour = lineColour;
		this.time = time;
		
	}
	
}
