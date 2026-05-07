package Package;

public class State {

	String station;
	double totalTime;
	String currentLine;
	int changes;

	public State(String station, double totalTime, String currentLine, int changes) {
		
		this.station = station;
		this.totalTime = totalTime;
		this.currentLine = currentLine;
		this.changes = changes;
		
	}

	public double getTotalTime() {
		
		return totalTime;
		
	}
	public int getChanges() {
		
		return changes;
		
	}
}