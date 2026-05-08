package Package;

// These are packages that I've imported for stuff like file reading, collections and queues that i can use later in my program
import java.util.Scanner;
import java.io.File;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Comparator;

public class Main {
	// This method prints the route using routestep objects, its mainly for the fewest changes route option because routestep method stores the exact line colours used between the start and end stations
	public static void printRouteSteps(ArrayList<RouteStep> route) {
		
		double totalTime = 0;
		int changes = 0;
		String previousLine = "";
		//This loops through each step of the route between the 2 stations
		for(RouteStep step : route) {
			// If the line colour changes this will add a 2 minute penalty and stores the count of the times it changes
			if (!previousLine.equals("") && !previousLine.equals(step.lineColour)) {
				
				System.out.println("\n*Change to " + step.lineColour + " line*");
				totalTime += 2;
				changes++;
				
			}
			// this prints the current journey step
			System.out.println(step.fromStation + " > " + step.toStation + " via " + step.lineColour + " line "+ step.time + " mins \n");
			
			// this adds the connection's travel time to the total
			totalTime += step.time;
			
			// this stores the current line so the next loop can detect for line changes
			previousLine = step.lineColour;
			
		}
		
		System.out.println("Total time (mins): " + totalTime);
		System.out.println("Number of changes: " + changes);
		
	}
	
	// this rebuilds a routestep route after the fewest changes method searching has finished, this works backwards from the final state to the start state and then it reverses the list so it is in the correct travel order
	public static ArrayList<RouteStep> rebuildStepRoute(HashMap<String, String> previousState, HashMap<String, RouteStep> previousStep, String finalKey) {
		
		ArrayList<RouteStep> route = new ArrayList<>();
		
		String currentKey = finalKey;
		
		// this walks backwards through the previous step map
		while(previousStep.containsKey(currentKey)) {
			
			route.add(previousStep.get(currentKey));
			currentKey = previousState.get(currentKey);
			
		}
		
		// this reverses the route because it was built from destination to start
		Collections.reverse(route);
		return route;
		
	}
	
	
	// this creates a unique key using station and lines, this is crucial because it makes sure arriving at the same end station using different lines
	
	public static String makeKey(String station, String line) {
		
		return station + "|" + line;
		
	}
	
	
	
	// This finds the route with the fewest line changes and if 2 routes have the same number of changes, it will select the faster line option
	
	public static ArrayList<RouteStep> findFewestChangesRoute(HashMap<String, ArrayList<Connection>> graph, String start, String end) {
		
		//this sorts first by number of changes then by total time
	    PriorityQueue<State> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(State::getChanges).thenComparingDouble(State::getTotalTime));
	    
	    //this stores the best number of changes found for each station and line state
	    HashMap<String, Integer> bestChanges = new HashMap<>();
	    
	    // this stores the best time found for each station and line state
	    HashMap<String, Double> bestTimes = new HashMap<>();
	    
	    //this stores the previous station and line state so that the route can be rebuilt
	    HashMap<String, String> previousState = new HashMap<>();
	    
	    //this stores the exact connection used to reach each state
	    HashMap<String, RouteStep> previousStep = new HashMap<>();
	    
	    // this starting point has 0 time no current line and 0 changes
	    State startState = new State(start, 0.0, "", 0);
	    priorityQueue.add(startState);

	    bestChanges.put(makeKey(start, ""), 0);
	    bestTimes.put(makeKey(start, ""), 0.0);

	    String finalKey = null;

	    while (!priorityQueue.isEmpty()) {
	    	
	    	// this gets the best available route state from the queue
	        State currentState = priorityQueue.poll();

	        // if the destination is reached it will store the final state and stop
	        if (currentState.station.equals(end)) {
	        	
	            finalKey = makeKey(currentState.station, currentState.currentLine);
	            break;
	            
	        }

	        // this checks for every connection from the current station
	        for (Connection connection : graph.get(currentState.station)) {

	            int newChanges = currentState.changes;
	            double newTime = currentState.totalTime + connection.time;

	            // this adds a change if moving onto a different line
	            if (!currentState.currentLine.equals("") && !currentState.currentLine.equals(connection.lineColour)) {
	            	
	                newChanges++;
	                newTime += 2;
	                
	            }

	            String nextKey = makeKey(connection.toStation, connection.lineColour);
	            String currentKey = makeKey(currentState.station, currentState.currentLine);

	            int oldChanges = bestChanges.getOrDefault(nextKey, Integer.MAX_VALUE);
	            double oldTime = bestTimes.getOrDefault(nextKey, Double.MAX_VALUE);
	            
	            // this updates if the route has fewer changes or same changes but faster timing
	            if (newChanges < oldChanges || (newChanges == oldChanges && newTime < oldTime)) {

	                bestChanges.put(nextKey, newChanges);
	                bestTimes.put(nextKey, newTime);

	                previousState.put(nextKey, currentKey);
	                previousStep.put(nextKey, new RouteStep(currentState.station, connection.toStation, connection.lineColour, connection.time));

	                priorityQueue.add(new State(connection.toStation, newTime, connection.lineColour, newChanges));
	                
	            }
	        }
	    }
	    
	    // if there is no final route, it will return an empty list
	    if (finalKey == null) {
	        return new ArrayList<>();
	    }

	    return rebuildStepRoute(previousState, previousStep, finalKey);
	}
	
	
	
	
	
	// this finds the route with the shortest total journey time, this is the dijkstra algorithm because travel times are weighted edges
	public static ArrayList<String> findShortestTimeRoute(HashMap<String, ArrayList<Connection>> graph, String start, String end) {

		// Priority queue processes the route with the lowest time first
		PriorityQueue<State> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(State::getTotalTime));
		
		// stores the shortest time to each station
		HashMap<String, Double> distances = new HashMap<>();
		// stores the previous station used to reach each station
		HashMap<String, String> previous = new HashMap<>();
		
		// at the start every station has infinite distance
		for (String station : graph.keySet()) {
			
			distances.put(station, Double.MAX_VALUE);
			
		}
		
		// start station has 0 distance
		distances.put(start, 0.0);

		priorityQueue.add(new State(start, 0.0, "", 0));

		while (!priorityQueue.isEmpty()) {

			State currentState = priorityQueue.poll();

			String currentStation = currentState.station;

			if (currentStation.equals(end)) {
				
				break;
				
			}
			
			// checks every neighbour stations
			for (Connection connection : graph.get(currentStation)) {

				String neighbour = connection.toStation;

				double newTime = currentState.totalTime + connection.time;
				int newChanges = currentState.changes;
				
				// Adds 2 minutes if changing lines
				if(!currentState.currentLine.equals("") && !currentState.currentLine.equals(connection.lineColour)) {
					
					newTime = newTime + 2;
					newChanges++;
					
				}
				
				// if this route is faster it stores and saves it
				if(newTime < distances.get(neighbour)) {
					
					distances.put(neighbour, newTime);
					previous.put(neighbour, currentStation);
					priorityQueue.add(new State(neighbour, newTime, connection.lineColour, newChanges));
					
				}
			}
		}

		ArrayList<String> route = new ArrayList<>();

		// if the destination is not reached somehow, it will return an empty route
		if (!previous.containsKey(end) && !start.equals(end)) {

			return route;
		}

		// this rebuilds route backwards from the end to the start
		String current = end;

		while (current != null) {

			route.add(current);

			current = previous.get(current);
		}

		Collections.reverse(route);

		return route;
	}
	


	
	// this prints the station name route with line colours travel times total journey time and number of changes
	public static void printRouteWithDetails(HashMap<String, ArrayList<Connection>> graph, ArrayList<String> route) {

		double totalTime = 0;
		int totalChanges = 0;
		String previousLine = "";
		
		// goes through each pair of stations in the route
		for (int i = 0; i < route.size() - 1; i++) {

			String currentStation = route.get(i);
			String nextStation = route.get(i + 1);

			Connection connectionUsed = null;

			// finds the connection object between the 2 stations
			for (Connection connection : graph.get(currentStation)) {
				if (connection.toStation.equals(nextStation)) {
					
					connectionUsed = connection;
					break;
					
				}
			}

			if (connectionUsed != null) {
				
				//if the line has changed then it adds the interchange penalty
				if (!previousLine.equals("") && !previousLine.equals(connectionUsed.lineColour)) {
					
					System.out.println("\n** Change to " + connectionUsed.lineColour + " line **");
					
					totalTime = totalTime + 2;
					totalChanges++;
					
				}

				System.out.println(currentStation + " -> " + nextStation + " via " + connectionUsed.lineColour + " line (" + connectionUsed.time + " mins)");

				totalTime = totalTime + connectionUsed.time;
				previousLine = connectionUsed.lineColour;
			}
		}

		System.out.println("\nTotal Journey Time (mins) = " + totalTime);
		System.out.println("Number of Changes = " + totalChanges);
	}

	
	// this finds a route using breadth first search, it gives the route with the fewest station hops ignoring travel time entirely
	public static ArrayList<String> findRoute(HashMap<String, ArrayList<Connection>> graph, String start, String end) {
		
		Queue<String> queue = new LinkedList<>();
		HashSet<String> alreadyVisited = new HashSet<>();
		HashMap<String, String> previous = new HashMap<>();
		
		queue.add(start);
		alreadyVisited.add(start);
		
		while (!queue.isEmpty()) {

			String current = queue.poll();

			if (current.equals(end)) {
				
				break;
				
			}
			
			// visits every neighbour station
			for (Connection connection : graph.get(current)) {

				String neighbour = connection.toStation;
				
				if (!alreadyVisited.contains(neighbour)) {
					alreadyVisited.add(neighbour);
					previous.put(neighbour, current);
					queue.add(neighbour);
				}
			}
		}
		
		ArrayList<String> route = new ArrayList<>();
		
		// if the end was not reached then it returns an empty route
		if (!alreadyVisited.contains(end)) {
			return route;
		}
		// rebuilds path backwards from end to start
		String current = end;
		
		while (current != null) {
			route.add(current);
			current = previous.get(current);
		}
		
		Collections.reverse(route);

		return route;
	}
	
	public static void main(String args[]) {
		
		// stores every existing station name
		HashSet<String> validStations = new HashSet<>();
		
		// graph structure: station name > list of connections from that station
		HashMap<String, ArrayList<Connection>> graph = new HashMap<>();
		
		/*This references the file I put into the java project directory*/
		File MetroLinkFile = new File("Metrolink_times_linecolour.csv");
		
		String currentLineColour = "";
		
		/*I'm creating a scanner to run through the data on the file and read it, if it doesnt detect data it will catch the error*/
		try (Scanner readMetroFiles = new Scanner(MetroLinkFile)) {
			
			while (readMetroFiles.hasNextLine()) {
				
				String data = readMetroFiles.nextLine().trim();
				if (data.isEmpty()) {
					continue;
				}
				if (data.toLowerCase().startsWith("from")) {
					continue;
				}
				
				String[] dataparts = data.split(",");
				
				if (dataparts.length == 1 || dataparts[1].trim().isEmpty()) {
					
					currentLineColour = dataparts[0].trim().toLowerCase();
					continue;
					
				}
				
				if (dataparts.length >= 3) {
					
					String fromStation = dataparts[0].trim().toLowerCase();
					String toStation = dataparts[1].trim().toLowerCase();
					
					double time = Double.parseDouble(dataparts[2].trim());
					
					validStations.add(fromStation);
					validStations.add(toStation);
					
					if (!graph.containsKey(fromStation)) {
						
						graph.put(fromStation, new ArrayList<Connection>());
						
					}
					
					if (!graph.containsKey(toStation)) {
						
						graph.put(toStation, new ArrayList<Connection>());
						
					}
					
					graph.get(fromStation).add(new Connection(toStation, currentLineColour, time));
					graph.get(toStation).add(new Connection(fromStation, currentLineColour, time));
					
				}
			}
			
		} catch (Exception e) {
			
			System.out.println("Error");
			e.printStackTrace();
			return;
			
		}
		
		System.out.println("Station loaded: " + validStations.size());
		
		// connects the GUI and launches it
		new MetroGUI(graph);
		
	}
}
		