package Package;

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
	
	public static void printRouteSteps(ArrayList<RouteStep> route) {
		
		double totalTime = 0;
		int changes = 0;
		String previousLine = "";
		
		for(RouteStep step : route) {
			if (!previousLine.equals("") && !previousLine.equals(step.lineColour)) {
				
				System.out.println("\n*Change to " + step.lineColour + " line*");
				totalTime += 2;
				changes++;
				
			}
			
			System.out.println(step.fromStation + " > " + step.toStation + " via " + step.lineColour + " line "+ step.time + " mins \n");
			
			totalTime += step.time;
			previousLine = step.lineColour;
			
		}
		
		System.out.println("Total time (mins): " + totalTime);
		System.out.println("Number of changes: " + changes);
		
	}
	public static ArrayList<RouteStep> rebuildStepRoute(HashMap<String, String> previousState, HashMap<String, RouteStep> previousStep, String finalKey) {
		
		ArrayList<RouteStep> route = new ArrayList<>();
		
		String currentKey = finalKey;
		
		while(previousStep.containsKey(currentKey)) {
			
			route.add(previousStep.get(currentKey));
			currentKey = previousState.get(currentKey);
			
		}
		
		Collections.reverse(route);
		return route;
		
	}
	
	
	
	
	public static String makeKey(String station, String line) {
		
		return station + "|" + line;
		
	}
	
	
	
	
	
	public static ArrayList<RouteStep> findFewestChangesRoute(HashMap<String, ArrayList<Connection>> graph, String start, String end) {

	    PriorityQueue<State> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(State::getChanges).thenComparingDouble(State::getTotalTime));

	    HashMap<String, Integer> bestChanges = new HashMap<>();
	    HashMap<String, Double> bestTimes = new HashMap<>();
	    HashMap<String, String> previousState = new HashMap<>();
	    HashMap<String, RouteStep> previousStep = new HashMap<>();

	    State startState = new State(start, 0.0, "", 0);
	    priorityQueue.add(startState);

	    bestChanges.put(makeKey(start, ""), 0);
	    bestTimes.put(makeKey(start, ""), 0.0);

	    String finalKey = null;

	    while (!priorityQueue.isEmpty()) {

	        State currentState = priorityQueue.poll();

	        if (currentState.station.equals(end)) {
	        	
	            finalKey = makeKey(currentState.station, currentState.currentLine);
	            break;
	            
	        }

	        for (Connection connection : graph.get(currentState.station)) {

	            int newChanges = currentState.changes;
	            double newTime = currentState.totalTime + connection.time;

	            if (!currentState.currentLine.equals("") && !currentState.currentLine.equals(connection.lineColour)) {
	            	
	                newChanges++;
	                newTime += 2;
	                
	            }

	            String nextKey = makeKey(connection.toStation, connection.lineColour);
	            String currentKey = makeKey(currentState.station, currentState.currentLine);

	            int oldChanges = bestChanges.getOrDefault(nextKey, Integer.MAX_VALUE);
	            double oldTime = bestTimes.getOrDefault(nextKey, Double.MAX_VALUE);

	            if (newChanges < oldChanges || (newChanges == oldChanges && newTime < oldTime)) {

	                bestChanges.put(nextKey, newChanges);
	                bestTimes.put(nextKey, newTime);

	                previousState.put(nextKey, currentKey);
	                previousStep.put(nextKey, new RouteStep(currentState.station, connection.toStation, connection.lineColour, connection.time));

	                priorityQueue.add(new State(connection.toStation, newTime, connection.lineColour, newChanges));
	                
	            }
	        }
	    }

	    if (finalKey == null) {
	        return new ArrayList<>();
	    }

	    return rebuildStepRoute(previousState, previousStep, finalKey);
	}
	
	
	
	
	
	
	public static ArrayList<String> findShortestTimeRoute(HashMap<String, ArrayList<Connection>> graph, String start, String end) {

		PriorityQueue<State> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(State::getTotalTime));

		HashMap<String, Double> distances = new HashMap<>();
		HashMap<String, String> previous = new HashMap<>();

		for (String station : graph.keySet()) {
			
			distances.put(station, Double.MAX_VALUE);
			
		}
		
		distances.put(start, 0.0);

		priorityQueue.add(new State(start, 0.0, "", 0));

		while (!priorityQueue.isEmpty()) {

			State currentState = priorityQueue.poll();

			String currentStation = currentState.station;

			if (currentStation.equals(end)) {
				
				break;
				
			}

			for (Connection connection : graph.get(currentStation)) {

				String neighbour = connection.toStation;

				double newTime = currentState.totalTime + connection.time;
				int newChanges = currentState.changes;
				
				if(!currentState.currentLine.equals("") && !currentState.currentLine.equals(connection.lineColour)) {
					
					newTime = newTime + 2;
					newChanges++;
					
				}
				if(newTime < distances.get(neighbour)) {
					
					distances.put(neighbour, newTime);
					previous.put(neighbour, currentStation);
					priorityQueue.add(new State(neighbour, newTime, connection.lineColour, newChanges));
					
				}
			}
		}

		ArrayList<String> route = new ArrayList<>();

		if (!previous.containsKey(end) && !start.equals(end)) {

			return route;
		}

		String current = end;

		while (current != null) {

			route.add(current);

			current = previous.get(current);
		}

		Collections.reverse(route);

		return route;
	}
	


	
	
	public static void printRouteWithDetails(HashMap<String, ArrayList<Connection>> graph, ArrayList<String> route) {

		double totalTime = 0;
		int totalChanges = 0;
		String previousLine = "";

		for (int i = 0; i < route.size() - 1; i++) {

			String currentStation = route.get(i);
			String nextStation = route.get(i + 1);

			Connection connectionUsed = null;

			for (Connection connection : graph.get(currentStation)) {
				if (connection.toStation.equals(nextStation)) {
					
					connectionUsed = connection;
					break;
					
				}
			}

			if (connectionUsed != null) {

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
		if (!alreadyVisited.contains(end)) {
			return route;
		}
		String current = end;
		while (current != null) {
			route.add(current);
			current = previous.get(current);
		}
		Collections.reverse(route);

		return route;
	}
	
	public static void main(String args[]) {
		
		HashSet<String> validStations = new HashSet<>();
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
//					System.out.println("Current line colour: " + currentLineColour);
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
//					System.out.println("Adding: " + fromStation + " -> " + toStation + " on " + currentLineColour);
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
		
		new MetroGUI(graph);
		
	}
}
		