package Package;

// Swing is used for creating GUIs (grapical user interface)
import javax.swing.*;

// AWT is used for layouts and window location
import java.awt.*;

// Collections can be used to store graph data and routes
import java.util.ArrayList;
import java.util.HashMap;

public class MetroGUI {

	// this is the main JFrame window
	private JFrame frame;
	
	// these are dropdown boxes of options for start station end station and route type
	private JComboBox<String> startBox;
	private JComboBox<String> endBox;
	private JComboBox<String> routeChoiceBox;
	
	// this is the parts of the GUI that shows the route result printed on the textarea element
	private JTextArea outputArea;

	// this is the graph structure passed from Main.java
	private HashMap<String, ArrayList<Connection>> graph;

	// this is the constructor method
	public MetroGUI(HashMap<String, ArrayList<Connection>> graph) {
		
		this.graph = graph;
		
		// creates a window
		createWindow();
		
	}

	public void createWindow() {

		// creates the main metro window
		frame = new JFrame("Manchester Metrolink Journey Planner");
		
		// this line closes the GUI completely when the user decides to click on the exit button
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//sets the size of the window in pixels
		frame.setSize(700, 500);
		
		// border layout for positioning components
		frame.setLayout(new BorderLayout());

		// panel used for inputs at the top of the window
		JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));

		// dropdown boxes for selecting stations
		startBox = new JComboBox<>();
		endBox = new JComboBox<>();

		/* This adds all stations from the graph into both dropdown boxes */
		for (String station : graph.keySet()) {
			startBox.addItem(station);
			endBox.addItem(station);
		}
		
		// dropdown box for selecting route algorithm
		routeChoiceBox = new JComboBox<>();
		
		routeChoiceBox.addItem("Fewest station stops/Shortest Route");
		routeChoiceBox.addItem("Shortest Time/Fastest Route");
		routeChoiceBox.addItem("Fewest changes/Least Line Changes");

		// button that starts the route search
		JButton findButton = new JButton("Find Route");

		// adds labels and dropdown boxes into the panel
		inputPanel.add(new JLabel("Start station:"));
		inputPanel.add(startBox);

		inputPanel.add(new JLabel("End station:"));
		inputPanel.add(endBox);

		inputPanel.add(new JLabel("Route option:"));
		inputPanel.add(routeChoiceBox);

		inputPanel.add(new JLabel(""));
		inputPanel.add(findButton);

		// text area where the route result is shown
		outputArea = new JTextArea();
		
		// this prevents the user typing in the output area
		outputArea.setEditable(false);

		// makes sure we have a scrollframe so we can scroll with mouses on the output area
		JScrollPane scrollPane = new JScrollPane(outputArea);

		// adds the panels onto the window
		frame.add(inputPanel, BorderLayout.NORTH);
		frame.add(scrollPane, BorderLayout.CENTER);

		/* When the button is clicked, the route calculation method runs */
		findButton.addActionListener(e -> findRoute());

		// displays the window true
		frame.setVisible(true);
	}

	// this prints the route details for routes stored as station names
	public void printStringRouteWithDetails(ArrayList<String> route) {

		double totalTime = 0;
		int totalChanges = 0;
		String previousLine = "";

		// loops through every station pair in the route
		for (int i = 0; i < route.size() - 1; i++) {

			String currentStation = route.get(i);
			String nextStation = route.get(i + 1);

			Connection connectionUsed = null;

			/* This finds the connection between the current station and next station */
			for (Connection connection : graph.get(currentStation)) {

				if (connection.toStation.equals(nextStation)) {
					connectionUsed = connection;
					break;
				}
			}

			if (connectionUsed != null) {
				// adds a change penalty if switching lines
				if (!previousLine.equals("") && !previousLine.equals(connectionUsed.lineColour)) {

					outputArea.append("\n** Change to " + connectionUsed.lineColour + " line **\n");

					totalTime = totalTime + 2;
					totalChanges++;
				}
				
				// prints the current journey step
				outputArea.append(
					currentStation + " > " +
					nextStation + " via " +
					connectionUsed.lineColour + " line (" +
					connectionUsed.time + " mins)\n"
				);
				
				// updates totals
				totalTime = totalTime + connectionUsed.time;
				previousLine = connectionUsed.lineColour;
			}
		}
		
		// prints final totals
		outputArea.append("\nTotal time: " + totalTime + " mins\n");
		outputArea.append("Number of changes: " + totalChanges + "\n");
	}
	// this method runs when the user presses the find route buton
	public void findRoute() {

		// gets selected values from dropdown boxes
		String start = (String) startBox.getSelectedItem();
		String end = (String) endBox.getSelectedItem();
		String choice = (String) routeChoiceBox.getSelectedItem();

		// clears previous output
		outputArea.setText("");


		// checks if stations exist
		if (start == null || end == null) {
			outputArea.setText("Please select valid stations.");
			return;
		}

		// prints selected options
		outputArea.append("Start: " + start + "\n");
		outputArea.append("End: " + end + "\n");
		outputArea.append("Route option: " + choice + "\n\n");

		// fewest station stops route option
		if (choice.equals("Fewest station stops/Shortest Route")) {

			outputArea.append("*** Route with Fewest Station Stops ***\n\n");

			// calls BFS route method from Main.java
			ArrayList<String> route = Main.findRoute(graph, start, end);

			printStringRouteWithDetails(route);

			// shortest total journey time route option
		} else if (choice.equals("Shortest Time/Fastest Route")) {

			outputArea.append("*** Route with Shortest Time ***\n\n");

			// calls shortest time algorithm from Main.java
			ArrayList<String> route = Main.findShortestTimeRoute(graph, start, end);

			printStringRouteWithDetails(route);

			// fewest line changes route option
		} else if (choice.equals("Fewest changes/Least Line Changes")) {

			outputArea.append("*** Route with Fewest Changes ***\n\n");

			// calls fewest changes algorithm from Main.java
			ArrayList<RouteStep> route = Main.findFewestChangesRoute(graph, start, end);

			double totalTime = 0;
			int changes = 0;
			String previousLine = "";

			// loops through each route step
			for (RouteStep step : route) {
				// checks if the line changed
				if (!previousLine.equals("") && !previousLine.equals(step.lineColour)) {

					outputArea.append("\n** Change to " + step.lineColour + " line **\n");

					totalTime = totalTime + 2;
					changes++;
				}

				// prints journey step
				outputArea.append(
					step.fromStation + " > " +
					step.toStation + " via " +
					step.lineColour + " line (" +
					step.time + " mins)\n"
				);

				// updates totals
				totalTime = totalTime + step.time;
				previousLine = step.lineColour;
			}

			// prints final totals
			outputArea.append("\nTotal time: " + totalTime + " mins\n");
			outputArea.append("Number of changes: " + changes + "\n");
		}
	}
}