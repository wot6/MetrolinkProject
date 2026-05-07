package Package;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class MetroGUI {

    private JFrame frame;
    private JComboBox<String> startBox;
    private JComboBox<String> endBox;
    private JComboBox<String> routeChoiceBox;
    private JTextArea outputArea;

    private HashMap<String, ArrayList<Connection>> graph;

    public MetroGUI(HashMap<String, ArrayList<Connection>> graph) {
        this.graph = graph;
        createWindow();
    }

    private void createWindow() {
        frame = new JFrame("Manchester Metrolink Journey Planner");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);
        frame.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));

        startBox = new JComboBox<>();
        endBox = new JComboBox<>();

        for (String station : graph.keySet()) {
            startBox.addItem(station);
            endBox.addItem(station);
        }

        routeChoiceBox = new JComboBox<>();
        routeChoiceBox.addItem("Fewest station stops/Shortest Route");
        routeChoiceBox.addItem("Shortest Time/Fastest Route");
        routeChoiceBox.addItem("Fewest changes/Least Line Changes");

        JButton findButton = new JButton("Find Route");

        inputPanel.add(new JLabel("Start station:"));
        inputPanel.add(startBox);

        inputPanel.add(new JLabel("End station:"));
        inputPanel.add(endBox);

        inputPanel.add(new JLabel("Route option:"));
        inputPanel.add(routeChoiceBox);

        inputPanel.add(new JLabel(""));
        inputPanel.add(findButton);

        outputArea = new JTextArea();
        outputArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(outputArea);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        findButton.addActionListener(e -> findRoute());

        frame.setVisible(true);
    }

    
    
    
    private void printStringRouteWithDetails(ArrayList<String> route) {

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
                    outputArea.append("\n** Change to " + connectionUsed.lineColour + " line **\n");
                    totalTime += 2;
                    totalChanges++;
                }

                outputArea.append(currentStation + " > " + nextStation + " via " + connectionUsed.lineColour + " line (" + connectionUsed.time + " mins)\n");

                totalTime += connectionUsed.time;
                previousLine = connectionUsed.lineColour;
            }
        }

        outputArea.append("\nTotal time: " + totalTime + " mins\n");
        outputArea.append("Number of changes: " + totalChanges + "\n");
    }
    
    
    
    
    
    
    private void findRoute() {

        String start = (String) startBox.getSelectedItem();
        String end = (String) endBox.getSelectedItem();
        String choice = (String) routeChoiceBox.getSelectedItem();

        outputArea.setText("");

        if (start == null || end == null) {
            outputArea.setText("Please select valid stations.");
            return;
        }

        outputArea.append("Start: " + start + "\n");
        outputArea.append("End: " + end + "\n");
        outputArea.append("Route option: " + choice + "\n\n");

        if (choice.equals("Fewest station stops/Shortest Route")) {

            outputArea.append("*** Route with Fewest Station Stops ***\n\n");

            ArrayList<String> route = Main.findRoute(graph, start, end);

            printStringRouteWithDetails(route);
            
        } else if (choice.equals("Shortest Time/Fastest Route")) {

            outputArea.append("*** Route with Shortest Time ***\n\n");

            ArrayList<String> route = Main.findShortestTimeRoute(graph, start, end);

            printStringRouteWithDetails(route);
            
        } else if (choice.equals("Fewest changes/Least Line Changes")) {
        	
        	outputArea.append("*** Route with Fewest Changes ***\n\n");
        	
            ArrayList<RouteStep> route = Main.findFewestChangesRoute(graph, start, end);

            double totalTime = 0;
            int changes = 0;
            String previousLine = "";

            for (RouteStep step : route) {

                if (!previousLine.equals("") && !previousLine.equals(step.lineColour)) {
                    outputArea.append("\n** Change to " + step.lineColour + " line **\n");
                    totalTime += 2;
                    changes++;
                }

                outputArea.append(
                    step.fromStation + " -> " +
                    step.toStation + " via " +
                    step.lineColour + " line (" +
                    step.time + " mins)\n"
                );

                totalTime += step.time;
                previousLine = step.lineColour;
            }

            outputArea.append("\nTotal time: " + totalTime + " mins\n");
            outputArea.append("Number of changes: " + changes + "\n");
        }
    }
}