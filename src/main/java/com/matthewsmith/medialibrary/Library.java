// **********************************************************************************
// Title: Library
// Author: Matthew Smith
// Course Section: CMIS202-ONL1 (Seidel) Spring 2023
// File: Library.java
// Description: Handles logic and graphical output for the library in MediaLibrary
// **********************************************************************************

package com.matthewsmith.medialibrary;

import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;

public class Library extends Pane {
    private ArrayList<Media> media;
    private File file;
    private int size;

    /** Empty constructor */
    public Library() {
        this(new ArrayList<>());
    }

    /** Constructor with ArrayList<Media> */
    public Library(ArrayList<Media> media) {
        this.media = media;
        file = new File("library.txt");

        try {
            file.createNewFile();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        this.size = media.size();
        draw();
    }

    /** Returns the size of the library */
    public int getSize() {
        return size;
    }

    /** Adds an element to the library */
    public void add(Media m) {
        this.media.add(m);
        this.size++;
        write();
        draw();
    }

    /** Removes an element from the library */
    public void remove(Media m) {
        this.media.remove(m);
        this.size--;
        write();
        draw();
    }

    /** Recursive quick sort - O(n log n) time complexity */
    public void sort(Comparator<Media> c) {
        sort(c, 0, size - 1);
        draw();
    }

    /** Quick sort helper method */
    private void sort(Comparator<Media> c, int first, int last) {
        if (last > first) {
            int pivot = partition(c, first, last);
            sort(c, first, pivot - 1); // Recursive call, first part of list
            sort(c, pivot + 1, last); // Recursive call, second part of list
        }
    }

    /** Partitions the media ArrayList for use in the sort() method */
    private int partition(Comparator<Media> c, int first, int last) {
        Media pivot = media.get(first);
        int low = first + 1;
        int high = last;

        while (high > low) {
            // Search forwards through the list from the left until an element
            // greater than the pivot is found
            while (low <= high && c.compare(media.get(low), pivot) <= 0) {
                low++;
            }

            // Search backwards through the list from the right until an element
            // less than or equal to the pivot is found
            while (low <= high && c.compare(media.get(high), pivot) > 0) {
                high--;
            }

            if (high > low) {
                // Swap items at high and low
                Media temp = media.get(high);
                media.set(high, media.get(low));
                media.set(low, temp);
            }
        }

        while (high > first && c.compare(media.get(high), pivot) >= 0) {
            high--;
        }

        if (c.compare(pivot, media.get(high)) > 0) {
            media.set(first, media.get(high)); // Element at first index becomes element at high
            media.set(high, pivot); // Element at index of high becomes pivot
            return high;
        } else {
            return first; // Return first index
        }
    }

    /** Reads data from the library.txt file into the media ArrayList */
    public void read() {
        if (file.length() > 0) {
            try {
                FileInputStream in = new FileInputStream("library.txt");
                ObjectInputStream objectIn = new ObjectInputStream(in);
                media = (ArrayList<Media>) objectIn.readObject();
                this.size = media.size();
                draw();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /** Writes the contents of the media ArrayList to the library.txt file */
    private void write() {
        try {
            FileOutputStream out = new FileOutputStream("library.txt");
            ObjectOutputStream objectOut = new ObjectOutputStream(out);
            objectOut.writeObject(media);
            objectOut.close();
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Draws the library */
    private void draw() {
        this.getChildren().clear();

        double x = 60; // Starting x-value
        for (Media m : media) {
            Rectangle entry = new Rectangle(x, 70, 65, 327);
            entry.setFill(Color.color(m.getColor()[0], m.getColor()[1], m.getColor()[2], m.getColor()[3]));

            Text nameText = new Text(0, 233.5,m.getName());
            nameText.setFont(Font.font("Arial", FontWeight.BOLD, 15));
            nameText.setText(shortenName(nameText)); // Shorten name

            // Text is centered on rectangle before being rotated
            double leftBound = (x + 32.5) - (calculateTextLength(nameText) / 2);
            nameText.setX(leftBound);
            nameText.setTextAlignment(TextAlignment.LEFT);
            nameText.setRotate(270); // Text is rotated

            // Name color is set to black or white depending on the lightness of the rectangle color
            nameText.setFill(m.getColor()[0] + m.getColor()[1] + m.getColor()[2] < 1.5 ? Color.WHITE : Color.BLACK);

            entry.setOnMouseClicked(e -> {
                // Show view/edit screen
            });

            this.getChildren().addAll(entry, nameText);
            x += 80;
        }

        this.getChildren().add(new Rectangle(0, 397, Math.max(798, 120 + (size * 80)), 15));
    }

    /** Returns a shortened String of a Text object */
    private static String shortenName(Text t) {
        String name = t.getText();
        double length = calculateTextLength(t);

        if (length > 350) {
            // Calculate the percentage (between 0 and 1) of the name that can fit, multiply by
            // the length of the Text object to find the maximum number of characters that fit
            double limit = (270 / length) * name.length();
            String shortName = "";

            for (int i = 0; i < limit && i < name.length(); i++) {
                shortName += String.valueOf(name.charAt(i));
            }

            shortName += "..."; // Add ellipses

            return shortName;
        } else {
            return name;
        }
    }

    /** Calculates length of a text object */
    private static double calculateTextLength(Text t) {
        Point2D leftBound = new Point2D(t.getLayoutBounds().getMinX(), t.getLayoutBounds().getMinY());
        Point2D rightBound = new Point2D(t.getLayoutBounds().getMaxX(), t.getLayoutBounds().getMaxY());
        return rightBound.getX() - leftBound.getX();
    }
}
