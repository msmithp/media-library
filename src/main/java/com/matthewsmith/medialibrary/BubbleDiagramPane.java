// **********************************************************************************
// Title: BubbleDiagramPane
// Author: Matthew Smith
// Course Section: CMIS202-ONL1 (Seidel) Spring 2023
// File: BubbleDiagramPane.java
// Description: Displays a bubble diagram of similar media
// **********************************************************************************

package com.matthewsmith.medialibrary;

import javafx.geometry.Point2D;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BubbleDiagramPane extends Pane {
    private final double RADIUS = 150; // radius of middle circle
    private final double CENTER_X = 400; // center X of middle circle
    private final double CENTER_Y = 400; // center Y of middle circle
    private final Library<Media> library;
    private WeightedGraph<Media> graph;
    private Media root;
    private final String type;

    /** Creates a bubble diagram pane from a library and root media */
    public BubbleDiagramPane(Library<Media> library, Media root) {
        this.library = library;
        this.root = root;
        this.type = root.getClass().getSimpleName();
        graph = new WeightedGraph<>();
        createGraph();
        draw();
    }

    /** Sets a new center media and redraws the pane */
    private void setRoot(Media root) {
        this.root = root;
        draw();
    }

    /** Creates a complete graph of all media of a certain type in library */
    private void createGraph() {
        ArrayList<Media> media = library.getMedia();

        // Add a vertex for each media
        for (Media m : media) {
            if (m.getClass().getSimpleName().equals(type)) {
                graph.addVertex(m);
            }
        }

        // Add edges between all media with weight of 1 - similarity score
        for (int i = 0; i < media.size(); i++) {
            Media m1 = media.get(i);
            int index1 = graph.getIndex(m1);

            if (!m1.getClass().getSimpleName().equals(type)) {
                continue;
            }

            for (int j = 0; j < media.size(); j++) {
                Media m2 = media.get(j);
                int index2 = graph.getIndex(m2);

                if (!m2.getClass().getSimpleName().equals(type)) {
                    continue;
                }

                // If m1 and m2 are not the same, add an edge
                if (!m1.equals(m2)) {
                    double weight = 1 - m1.getSimilarity(m2);
                    graph.addEdge(index1, index2, weight);
                    graph.addEdge(index2, index1, weight);
                }
            }
        }
    }

    /** Draws a bubble diagram of media similar to the root media */
    private void draw() {
        this.getChildren().clear();
        drawTitle(root);
        final double width = 800;
        final double height = 800;
        Media[] similar = getSimilarMedia(root);

        // Root circle
        Circle rootCircle = new Circle(width / 2, height / 2, RADIUS, root.getColor());
        Text rootText = new Text(shortenString(root.getName(), 80));
        rootText.setWrappingWidth(RADIUS * 1.45);
        rootText.setTextAlignment(TextAlignment.CENTER);

        // Text style
        rootText.setFont(Font.font("Manrope", FontWeight.BOLD, 20));
        rootText.getStyleClass().add("bubble");
        rootText.setStyle("-fx-font-size: 20;");
        rootText.getStyleClass().add("bubble");
        rootText.setStyle(root.getColorArray()[0] + root.getColorArray()[1] +
                root.getColorArray()[2] < 1.6 ? rootText.getStyle() + " -fx-fill: white;" :
                rootText.getStyle() + " -fx-fill: black;");

        // Show full name on hover
        Tooltip fullName = new Tooltip(root.getName());
        fullName.setFont(Font.font(14));
        fullName.setShowDelay(Duration.millis(300));
        Tooltip.install(rootText, fullName);

        // Put in stack pane to center text on circle
        StackPane circleAndText = new StackPane();
        circleAndText.getChildren().addAll(rootCircle, rootText);
        circleAndText.setLayoutX(width / 2 - RADIUS);
        circleAndText.setLayoutY(height / 2 - RADIUS);
        this.getChildren().add(circleAndText);

        try {
            drawCircles(similar);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Draws the surrounding circles */
    private void drawCircles(Media[] media) throws Exception {
        if (media[0] == null) return;

        final double perimeter = 2 * Math.PI * RADIUS; // Perimeter of center circle
        final double maxRadius = 125; // Radius with a similarity score of 1.0
        final int rootIndex = graph.getIndex(root);

        double totalWeight = 0; // Total weight of results; if too high, fewer results will be displayed
        double percent = 0; // Cumulative percentage of center circle used
        double angle;
        double similarityValue = 1 - graph.getWeight(rootIndex, graph.getIndex(media[0]));
        double newRadius = Math.max(similarityValue, 0.3) * maxRadius;
        double x = 400;
        double y = 250 - newRadius;

        drawACircle(x, y, newRadius, media[0], similarityValue);
        double lastSimilarity = similarityValue;
        totalWeight += similarityValue;

        for (int i = 1; i < media.length; i++) {
            if (media[i] == null || totalWeight > 5.6) return;
            similarityValue = 1 - graph.getWeight(rootIndex, graph.getIndex(media[i]));
            newRadius = Math.max(similarityValue, 0.3) * maxRadius;

            // Convert similarity value of previous circle to a percentage of circle perimeter used
            double percentOfPerimeter = (Math.max(lastSimilarity, 0.3) * maxRadius) / perimeter;
            percent += (i == 1 ? percentOfPerimeter / 2 : percentOfPerimeter) + 0.015;

            // Percent of middle circle where new circle will go
            double nextPercent = percent + ((newRadius / 2) / perimeter);
            if (nextPercent > 1) return;

            /* Angle, in degrees, where the next circle will be placed
            0%: 90 degrees
            25%: 0 degrees
            50%: 270 degrees
            75%: 180 degrees */
            angle = -360 * nextPercent + (nextPercent < 0.25 ? 90 : 450);

            // Point on the perimeter of the circle where the next circle will go
            Point2D perimeterPoint = getPerimeterPoint(CENTER_X, CENTER_Y, RADIUS, angle);
            double perimeterX = perimeterPoint.getX();
            double perimeterY = perimeterPoint.getY();

            // Calculate x and y positions of center of new circle; when similarityValue == 1, the
            // distance from the perimeter will be maxRadius
            x = perimeterX + ((Math.max(similarityValue, 0.30) / (RADIUS / maxRadius)) * (perimeterX - CENTER_X));
            y = perimeterY + ((Math.max(similarityValue, 0.30) / (RADIUS / maxRadius)) * (perimeterY - CENTER_Y));

            // Draw the circle
            drawACircle(x, y, newRadius, media[i], similarityValue);
            lastSimilarity = similarityValue;
            totalWeight += similarityValue;
        }
    }

    /** Takes a center X, center Y, radius, and angle and returns a point on the perimeter of the circle */
    private static Point2D getPerimeterPoint(double centerX, double centerY, double radius, double angle) {
        double perimeterX = centerX + radius * Math.cos(Math.toRadians(angle));
        double perimeterY = centerY - radius * Math.sin(Math.toRadians(angle));
        return new Point2D(perimeterX, perimeterY);
    }

    /** Draws a bubble circle with text */
    private void drawACircle(double x, double y, double radius, Media m, double similarityValue) {
        Circle circle = new Circle(x, y, radius, m.getColor());

        // Shorten name based on size of circle
        String name = shortenString(m.getName(), Math.max((int) (similarityValue * 45), 10));

        Text text = new Text(String.format("%s\nSimilarity: %.2f%%", name, similarityValue * 100));
        text.setWrappingWidth(radius * 1.35);
        text.setTextAlignment(TextAlignment.CENTER);

        // Show full name on hover
        Tooltip fullName = new Tooltip(String.format("%s\nSimilarity: %.2f%%", m.getName(), similarityValue * 100));
        fullName.setFont(Font.font(14));
        fullName.setShowDelay(Duration.millis(300));
        Tooltip.install(text, fullName);

        // Set font size and color
        double fontSize = Math.max(18 * similarityValue, 10);
        text.setFont(Font.font("Manrope", FontWeight.BOLD, fontSize));

        text.setStyle("-fx-font-size: " + fontSize + ";");
        text.getStyleClass().add("bubble");
        text.setStyle(m.getColorArray()[0] + m.getColorArray()[1] +
                m.getColorArray()[2] < 1.6 ? text.getStyle() + " -fx-fill: white;" :
                text.getStyle() + " -fx-fill: black;");

        // Bubble reacts to mouse entering and exiting
        text.setOnMouseEntered(e -> {
            text.setUnderline(true);
            circle.setFill(m.getColor().darker());
        });

        text.setOnMouseExited(e -> {
            text.setUnderline(false);
            circle.setFill(m.getColor());
        });

        circle.setOnMouseEntered(e -> circle.setFill(m.getColor().darker()));
        circle.setOnMouseExited(e -> circle.setFill(m.getColor()));

        text.setOnMouseClicked(e -> setRoot(m));
        circle.setOnMouseClicked(e -> setRoot(m));

        // Place in stack pane to center text on circle
        StackPane circleAndText = new StackPane();
        circleAndText.setLayoutX(x - radius);
        circleAndText.setLayoutY(y - radius);
        circleAndText.getChildren().addAll(circle, text);

        this.getChildren().add(circleAndText);
    }

    private void drawTitle(Media m) {
        Text header = new Text(30, 30, "Similar Media");

        Text titleText = new Text(30, 68, shortenString(m.getName(), 50));
        titleText.getStyleClass().add("title");
        titleText.setStyle(titleText.getStyle() + " -fx-font-size: 2.8em; -fx-line-spacing: -1em;");
        titleText.setWrappingWidth(300);

        this.getChildren().addAll(header, titleText);
    }

    /** Finds the 10 media most similar to a specified media */
    private Media[] getSimilarMedia(Media m) {
        final int numMedia = 10;
        Media[] similarMedia = new Media[numMedia];
        Arrays.fill(similarMedia, null);
        Double[] weights = new Double[numMedia];

        // Initialize array with max distance value 1
        Arrays.fill(weights, 1.0);

        int index1 = graph.getIndex(m);
        List<Integer> neighbors = graph.getNeighbors(index1);

        for (int index2 : neighbors) {
            Media m2 = graph.getVertex(index2);
            try {
                double weight = graph.getWeight(index1, index2);

                // if weight is less than the highest distance in the similarMedia array,
                // add it to the similarMedia array
                if (weight < weights[weights.length - 1]) {
                    // get index
                    int index = 0;
                    for (int i = 0; i < weights.length; i++) {
                        if (weight < weights[i]) {
                            index = i;
                            break;
                        }
                    }
                    insertElement(similarMedia, m2, index); // insert into array
                    insertElement(weights, weight, index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return similarMedia;
    }

    /** Inserts an item into an array at a specified index */
    private static <E> void insertElement(E[] arr, E obj, int index) {
        E previousItem = obj;

        for (int i = index; i < arr.length; i++) {
            E temp = previousItem;
            previousItem = arr[i];
            arr[i] = temp;
        }
    }

    /** Shortens a string to a certain number of characters */
    private static String shortenString(String str, int numChars) {
        return str.length() > numChars ? str.substring(0, numChars) + "..." : str;
    }
}
