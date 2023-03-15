// **********************************************************************************
// Title: Library
// Author: Matthew Smith
// Course Section: CMIS202-ONL1 (Seidel) Spring 2023
// File: Library.java
// Description: Handles logic and graphical output for the library in MediaLibrary
// **********************************************************************************

package com.matthewsmith.medialibrary;

import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.*;
import java.time.Year;
import java.util.*;

public class Library extends Pane {
    private ArrayList<Media> media;
    private transient Stack<Library> history; // History of user actions, for undo function
    private transient Stack<Library> undoHistory; // History of undone actions, for redo function
    private HashMap<Media, String> groups;
    private File file;
    private int size;

    /** Constructors */
    public Library() {
        this(new ArrayList<>());
    }

    public Library(ArrayList<Media> media) {
        this.media = media;
        file = new File("library.txt");

        try {
            file.createNewFile();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        this.history = new Stack<>();
        this.undoHistory = new Stack<>();
        groups = new HashMap<>();
        this.size = this.media.size();
        draw();
    }

    public Library(ArrayList<Media> media, HashMap<Media, String> groups, File file, int size) {
        this.media = media;
        this.groups = groups;
        this.file = file;
        this.size = size;
    }

    /** Returns the list of media */
    private ArrayList<Media> getMedia() {
        return media;
    }

    /** Returns the list of groups */
    private HashMap<Media, String> getGroups() {
        return groups;
    }

    /** Returns the size of the library */
    public int getSize() {
        return size;
    }

    /** Adds an element to the library */
    public void add(Media m) {
        saveState();
        this.media.add(m);
        this.size++;
        MediaLibrary.setSize(this.size);
        write();
        draw();
    }

    /** Removes an element from the library */
    public void remove(Media m) {
        saveState();
        this.media.remove(m);
        this.size--;
        MediaLibrary.setSize(this.size);
        write();
        draw();
    }

    /** Undoes previous action */
    public void undo() {
        if (!history.isEmpty()) {
            saveUndoneState();
            Library newLib = history.pop();
            this.media = newLib.getMedia();
            this.groups = newLib.getGroups();
            this.size = newLib.getSize();
            MediaLibrary.setSize(this.size);
            write();
            draw();
        }
    }

    /** Redoes previously undone action */
    public void redo() {
        if (!undoHistory.isEmpty()) {
            history.push(new Library(copy(this.media), (HashMap<Media, String>) this.groups.clone(), this.file, this.size));

            Library newLib = undoHistory.pop();
            this.media = newLib.getMedia();
            this.groups = newLib.getGroups();
            this.size = newLib.getSize();
            MediaLibrary.setSize(this.size);
            write();
            draw();
        }
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
                ObjectInputStream objectIn = new ObjectInputStream(new BufferedInputStream(in));
                media = (ArrayList<Media>) objectIn.readObject();
                groups = (HashMap<Media, String>) objectIn.readObject();
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
            ObjectOutputStream objectOut = new ObjectOutputStream(new BufferedOutputStream(out));
            objectOut.writeObject(media);
            objectOut.writeObject(groups);
            objectOut.close();
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Draws the library */
    public void draw() {
        this.getChildren().clear();

        double x = 60; // Starting x-value
        for (Media m : media) {
            drawEntry(m, x);
            x += 80;
        }

        MediaLibrary.setTitle("My Media Library");
        this.getChildren().add(new Rectangle(0, 397, Math.max(798, 120 + (size * 80)), 15));
    }

    public void drawGroup(String name) {
        this.getChildren().clear();

        if (!groups.containsValue(name)) {
            return;
        }

        double x = 60;
        for (Media m : groups.keySet()) {
            if (groups.get(m).equals(name)) {
                drawEntry(m, x);
                x += 80;
            }
        }

        this.getChildren().add(new Rectangle(0, 397, Math.max(798, 60 + x), 15));
    }

    /** Draws one media entry (rectangle and text) */
    private void drawEntry(Media m, double x) {
        Rectangle entry = new Rectangle(x, 70, 65, 327);
        entry.setFill(Color.color(m.getColorArray()[0], m.getColorArray()[1], m.getColorArray()[2], m.getColorArray()[3]));

        Text nameText = new Text(0, 233.5,m.getName());
        nameText.setFont(Font.font("Arial", FontWeight.BOLD, 15));

        if (calculateTextLength(nameText) > 290) {
            nameText.setText(shortenName(nameText, 290)); // Shorten name
        }

        // Text is centered on rectangle before being rotated
        double leftBound = (x + 32.5) - (calculateTextLength(nameText) / 2);
        nameText.setX(leftBound);
        nameText.setTextAlignment(TextAlignment.LEFT);
        nameText.setRotate(270); // Text is rotated

        // Name color is set to black or white depending on the lightness of the rectangle color
        nameText.setFill(m.getColorArray()[0] + m.getColorArray()[1] + m.getColorArray()[2] < 1.6 ? Color.WHITE : Color.BLACK);

        // Entry reacts to mouse entering and exiting
        nameText.setOnMouseEntered(e -> {
            nameText.setUnderline(true);
            entry.setFill(m.getColor().darker());
        });

        nameText.setOnMouseExited(e -> {
            nameText.setUnderline(false);
            entry.setFill(m.getColor());
        });

        entry.setOnMouseEntered(e -> entry.setFill(m.getColor().darker()));
        entry.setOnMouseExited(e -> entry.setFill(m.getColor()));

        // Create "view" screen when an entry is clicked
        entry.setOnMouseClicked(e -> showViewScreen(m));
        nameText.setOnMouseClicked(e -> showViewScreen(m));

        this.getChildren().addAll(entry, nameText);
    }

    /** Saves current state of library to history stack */
    private void saveState() {
        history.push(new Library(copy(this.media), (HashMap<Media, String>) this.groups.clone(), this.file, this.size));
        undoHistory.clear();
    }

    /** Saves current state of library to undo history stack */
    private void saveUndoneState() {
        undoHistory.push(new Library(copy(this.media), (HashMap<Media, String>) this.groups.clone(), this.file, this.size));
    }

    /** Displays a screen to view information about a piece of media */
    private void showViewScreen(Media m) {
        this.getChildren().clear(); // Clear screen

        double currentY = 55; // Starting y-value

        Text nameText = new Text(200, currentY, m.getName());
        nameText.setFont(Font.font("Arial", FontWeight.BOLD, 35));
        nameText.setWrappingWidth(500);
        currentY += (calculateTextHeight(nameText));

        Text descriptionText = new Text(200, currentY, m.getDescription());
        descriptionText.setWrappingWidth(500);
        currentY += (calculateTextHeight(descriptionText) + 25);

        Text genreText = new Text(200, currentY, "Genre: " + m.getGenre());
        genreText.setWrappingWidth(225);
        Text formatText = new Text(500, currentY, "Format: " + m.getFormat());
        formatText.setWrappingWidth(225);
        currentY += (Math.max(calculateTextHeight(genreText), calculateTextHeight(formatText)) + 25);

        Text yearText = new Text(200, currentY, "Year released: " + m.getYear());
        yearText.setWrappingWidth(225);
        Text yearConsumedText = new Text(500, currentY, "Year consumed: " + m.getYearConsumed());
        yearConsumedText.setWrappingWidth(225);
        currentY += (Math.max(calculateTextHeight(yearText), calculateTextHeight(yearConsumedText)) + 25);

        Text ratingText = new Text(200, currentY, "Rating: " + m.getRating() + "/10");
        yearConsumedText.setWrappingWidth(500);
        currentY += (Math.max(calculateTextHeight(yearText), calculateTextHeight(yearConsumedText)) + 75);

        Rectangle line = new Rectangle(200, currentY - 50, 500, 3);

        this.getChildren().addAll(nameText, descriptionText, genreText, formatText,
                yearText, yearConsumedText, ratingText, line);

        if (m instanceof Movie) {
            Text directorText = new Text(200, currentY, "Director: " + ((Movie) m).getDirector());
            directorText.setWrappingWidth(225);

            Text durationText = new Text(500, currentY, "Duration: " + ((Movie) m).getDuration() + " minutes");
            durationText.setWrappingWidth(225);

            currentY += (Math.max(calculateTextHeight(directorText), calculateTextHeight(durationText)) + 25);
            this.getChildren().addAll(directorText, durationText);
        } else if (m instanceof Show) {
            Text creatorText = new Text(200, currentY, "Creator: " + ((Show) m).getCreator());
            creatorText.setWrappingWidth(225);

            Text numSeasonsText = new Text(500, currentY, "Number of seasons: " + ((Show) m).getNumSeasons());
            numSeasonsText.setWrappingWidth(225);

            currentY += (Math.max(calculateTextHeight(creatorText), calculateTextHeight(numSeasonsText)) + 25);

            Text numEpisodesText = new Text(200, currentY, "Number of episodes: " + ((Show) m).getNumEpisodes());
            numEpisodesText.setWrappingWidth(225);

            currentY += (calculateTextHeight(numEpisodesText) + 25);
            this.getChildren().addAll(creatorText, numSeasonsText, numEpisodesText);
        } else if (m instanceof Game) {
            Text developerText = new Text(200, currentY, "Developer: " + ((Game) m).getDeveloper());
            developerText.setWrappingWidth(225);

            Text consoleText = new Text(500, currentY, "Console: " + ((Game) m).getConsole());
            consoleText.setWrappingWidth(225);

            currentY += (Math.max(calculateTextHeight(developerText), calculateTextHeight(consoleText)) + 25);

            Text numPlayersText = new Text(200, currentY, "Number of players: " + ((Game) m).getNumPlayers());
            numPlayersText.setWrappingWidth(225);

            currentY += (calculateTextHeight(numPlayersText) + 25);
            this.getChildren().addAll(developerText, consoleText, numPlayersText);
        } else if (m instanceof Music) {
            Text artistText = new Text(200, currentY, "Artist: ");
            artistText.setWrappingWidth(225);

            currentY += (calculateTextHeight(artistText) + 25);
            this.getChildren().add(artistText);
        } else if (m instanceof Book) {
            Text authorText = new Text(200, currentY, "Author: ");
            authorText.setWrappingWidth(225);

            currentY += (calculateTextHeight(authorText) + 25);
            this.getChildren().add(authorText);
        }

        Button btOK = new Button("OK");
        btOK.setLayoutX(330);
        btOK.setLayoutY(currentY);
        btOK.setPrefWidth(60);

        Button btEdit = new Button ("Edit");
        btEdit.setLayoutX(400);
        btEdit.setLayoutY(currentY);
        btEdit.setPrefWidth(60);

        Button btDelete = new Button("Delete");
        btDelete.setLayoutX(470);
        btDelete.setLayoutY(currentY);
        btDelete.setPrefWidth(60);

        Rectangle rect = new Rectangle(50, 0, 125, Math.max(currentY + 40, 467));
        rect.setFill(m.getColor());
        this.getChildren().addAll(btOK, btEdit, btDelete, rect);

        btOK.setOnAction(e -> draw());
        btEdit.setOnAction(e -> showEditScreen(m));
        btDelete.setOnAction(e -> remove(m));

        MediaLibrary.setScroll(0, 0);
    }

    /** Displays a screen to edit information about a piece of media */
    private void showEditScreen(Media m) {
        this.getChildren().clear(); // Clear screen
        Text errorText = new Text(200, 25, ""); // Error message
        errorText.setFill(Color.RED);

        Text name = new Text(200, 55, "Name: ");
        TextField nameTF = new TextField();
        nameTF.setText(m.getName());
        nameTF.setLayoutX(350);
        nameTF.setLayoutY(40);

        Text genre = new Text(200, 95, "Genre: ");
        TextField genreTF = new TextField();
        genreTF.setText(m.getGenre());
        genreTF.setLayoutX(350);
        genreTF.setLayoutY(80);

        Text description = new Text(200, 135, "Description: ");
        TextArea descriptionTA = new TextArea();
        descriptionTA.setText(m.getDescription());
        descriptionTA.setPrefRowCount(3);
        descriptionTA.setPrefColumnCount(20);
        descriptionTA.setWrapText(true);
        descriptionTA.setLayoutX(350);
        descriptionTA.setLayoutY(120);

        Text format = new Text(200, 220, "Format: ");
        TextField formatTF = new TextField();
        formatTF.setText(m.getFormat());
        formatTF.setLayoutX(350);
        formatTF.setLayoutY(205);

        Text year = new Text(200, 260, "Year released: ");
        TextField yearTF = new TextField();
        yearTF.setText(m.getYear() + "");
        yearTF.setLayoutX(350);
        yearTF.setLayoutY(245);

        Text yearConsumed = new Text(200, 300, "Year consumed: ");
        TextField yearConsumedTF = new TextField();
        yearConsumedTF.setText(m.getYearConsumed() + "");
        yearConsumedTF.setLayoutX(350);
        yearConsumedTF.setLayoutY(285);

        Text rating = new Text(200, 340, "Your rating out of 10: ");
        TextField ratingTF = new TextField();
        ratingTF.setText(m.getRating() + "");
        ratingTF.setLayoutX(350);
        ratingTF.setLayoutY(325);

        Text color = new Text(200, 385, "Color: ");
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setValue(m.getColor());
        colorPicker.setPrefHeight(30);
        colorPicker.setLayoutX(350);
        colorPicker.setLayoutY(365);

        Text groupText = new Text(200, 0, "Set group: ");
        TextField groupTF = new TextField();
        groupTF.setText(groups.get(m) == null ? "" : groups.get(m));
        groupTF.setLayoutX(350);

        Rectangle line = new Rectangle(200, 420, 500, 3);
        Button btSave = new Button("Save");
        btSave.setPrefWidth(60);
        btSave.setLayoutX(360);
        Button btCancel = new Button("Cancel");
        btCancel.setPrefWidth(60);
        btCancel.setLayoutX(440);
        double buttonY = 0;

        this.getChildren().addAll(errorText, name, nameTF, genre, genreTF, description, descriptionTA, format, formatTF, year,
                yearTF, yearConsumed, yearConsumedTF, rating, ratingTF, color, colorPicker, line, btSave, btCancel, groupText, groupTF);

        if (m instanceof Movie) {
            Text directorText = new Text(200, 475, "Director: ");
            TextField directorTF = new TextField();
            directorTF.setText(((Movie) m).getDirector());
            directorTF.setLayoutX(350);
            directorTF.setLayoutY(460);

            Text durationText = new Text(200, 515, "Duration: ");
            TextField durationTF = new TextField();
            durationTF.setText(((Movie) m).getDuration() + "");
            durationTF.setLayoutX(350);
            durationTF.setLayoutY(500);

            groupText.setY(555);
            groupTF.setLayoutY(540);

            buttonY = 595;
            btSave.setLayoutY(buttonY);
            btCancel.setLayoutY(buttonY);

            this.getChildren().addAll(directorText, directorTF, durationText, durationTF);

            btSave.setOnAction(e -> {
                try {
                    if (nameTF.getText().isBlank()) {
                        errorText.setText("You must enter a movie name");
                        MediaLibrary.setScroll(0, 0);
                    } else if (!ratingTF.getText().isBlank() && Media.validateRating(Double.parseDouble(ratingTF.getText()))) {
                        errorText.setText("Rating must be between 0 and 10");
                        MediaLibrary.setScroll(0, 0);
                    } else if (!durationTF.getText().isBlank() && Integer.parseInt(durationTF.getText()) < 0) {
                        errorText.setText("Duration cannot be less than 0");
                        MediaLibrary.setScroll(0, 0);
                    } else { // Successful case
                        saveState();
                        m.setName(nameTF.getText());
                        m.setDescription(descriptionTA.getText());
                        m.setGenre(genreTF.getText());
                        m.setFormat(formatTF.getText());
                        m.setYear(yearTF.getText().isBlank() ? Year.now().getValue() : Integer.parseInt(yearTF.getText()));
                        m.setYearConsumed(yearConsumedTF.getText().isBlank() ? Year.now().getValue() : Integer.parseInt(yearConsumedTF.getText()));
                        m.setRating(ratingTF.getText().isBlank() ? 0 : Double.parseDouble(ratingTF.getText()));
                        m.setColor(colorPicker.getValue());
                        if (!groupTF.getText().isBlank()) {
                            groups.put(m, groupTF.getText());
                        } else {
                            groups.remove(m);
                        }
                        ((Movie) m).setDirector(directorTF.getText());
                        ((Movie) m).setDuration(durationTF.getText().isBlank() ? 0 : Integer.parseInt(durationTF.getText()));
                        showViewScreen(m);
                        write();
                    }
                } catch (Exception ex) {
                    errorText.setText("Invalid input");
                    MediaLibrary.setScroll(0, 0);
                }
            });
        } else if (m instanceof Show) {
            Text creatorText = new Text(200, 475, "Creator: ");
            TextField creatorTF = new TextField();
            creatorTF.setText(((Show) m).getCreator());
            creatorTF.setLayoutX(350);
            creatorTF.setLayoutY(460);

            Text numSeasonsText = new Text(200, 515, "Number of seasons: ");
            TextField numSeasonsTF = new TextField();
            numSeasonsTF.setText(((Show) m).getNumSeasons() + "");
            numSeasonsTF.setLayoutX(350);
            numSeasonsTF.setLayoutY(500);

            Text numEpisodesText = new Text(200, 555, "Number of episodes: ");
            TextField numEpisodesTF = new TextField();
            numEpisodesTF.setText(((Show) m).getNumEpisodes() + "");
            numEpisodesTF.setLayoutX(350);
            numEpisodesTF.setLayoutY(540);

            groupText.setY(595);
            groupTF.setLayoutY(580);

            buttonY = 635;
            btSave.setLayoutY(buttonY);
            btCancel.setLayoutY(buttonY);

            this.getChildren().addAll(creatorText, creatorTF, numSeasonsText, numSeasonsTF, numEpisodesText, numEpisodesTF);

            btSave.setOnAction(e -> {
                try {
                    if (nameTF.getText().isBlank()) {
                        errorText.setText("You must enter a show name");
                        MediaLibrary.setScroll(0, 0);
                    } else if (!ratingTF.getText().isBlank() && Media.validateRating(Double.parseDouble(ratingTF.getText()))) {
                        errorText.setText("Rating must be between 0 and 10");
                        MediaLibrary.setScroll(0, 0);
                    } else if (!numEpisodesTF.getText().isBlank() && Integer.parseInt(numEpisodesTF.getText()) < 0) {
                        errorText.setText("Number of episodes cannot be less than 0");
                        MediaLibrary.setScroll(0, 0);
                    } else if(!numSeasonsTF.getText().isBlank() && Integer.parseInt(numSeasonsTF.getText()) < 0) {
                        errorText.setText("Number of seasons cannot be less than 0");
                        MediaLibrary.setScroll(0, 0);
                    } else { // Successful case
                        saveState();
                        m.setName(nameTF.getText());
                        m.setDescription(descriptionTA.getText());
                        m.setGenre(genreTF.getText());
                        m.setFormat(formatTF.getText());
                        m.setYear(yearTF.getText().isBlank() ? Year.now().getValue() : Integer.parseInt(yearTF.getText()));
                        m.setYearConsumed(yearConsumedTF.getText().isBlank() ? Year.now().getValue() : Integer.parseInt(yearConsumedTF.getText()));
                        m.setRating(ratingTF.getText().isBlank() ? 0 : Double.parseDouble(ratingTF.getText()));
                        m.setColor(colorPicker.getValue());
                        if (!groupTF.getText().isBlank()) {
                            groups.put(m, groupTF.getText());
                        } else {
                            groups.remove(m);
                        }
                        ((Show) m).setCreator(creatorTF.getText());
                        ((Show) m).setNumEpisodes(numEpisodesTF.getText().isBlank() ? 0 : Integer.parseInt(numEpisodesTF.getText()));
                        ((Show) m).setNumSeasons(numSeasonsTF.getText().isBlank() ? 0 : Integer.parseInt(numSeasonsTF.getText()));
                        showViewScreen(m);
                        write();
                    }
                } catch (Exception ex) {
                    errorText.setText("Invalid input");
                    MediaLibrary.setScroll(0, 0);
                }
            });
        } else if (m instanceof Game) {
            Text developerText = new Text(200, 475, "Developer: ");
            TextField developerTF = new TextField();
            developerTF.setText(((Game) m).getDeveloper());
            developerTF.setLayoutX(350);
            developerTF.setLayoutY(460);

            Text consoleText = new Text(200, 515, "Console: ");
            TextField consoleTF = new TextField();
            consoleTF.setText(((Game) m).getConsole() + "");
            consoleTF.setLayoutX(350);
            consoleTF.setLayoutY(500);

            Text numPlayersText = new Text(200, 555, "Number of players: ");
            TextField numPlayersTF = new TextField();
            numPlayersTF.setText(((Game) m).getNumPlayers() + "");
            numPlayersTF.setLayoutX(350);
            numPlayersTF.setLayoutY(540);

            groupText.setY(595);
            groupTF.setLayoutY(580);

            buttonY = 635;
            btSave.setLayoutY(buttonY);
            btCancel.setLayoutY(buttonY);

            this.getChildren().addAll(developerText, developerTF, consoleText, consoleTF, numPlayersText, numPlayersTF);

            btSave.setOnAction(e -> {
                try {
                    if (nameTF.getText().isBlank()) {
                        errorText.setText("You must enter a game name");
                        MediaLibrary.setScroll(0, 0);
                    } else if (!ratingTF.getText().isBlank() && Media.validateRating(Double.parseDouble(ratingTF.getText()))) {
                        errorText.setText("Rating must be between 0 and 10");
                        MediaLibrary.setScroll(0, 0);
                    } else if (!numPlayersTF.getText().isBlank() && Integer.parseInt(numPlayersTF.getText()) < 0) {
                        errorText.setText("Number of players cannot be less than 0");
                        MediaLibrary.setScroll(0, 0);
                    } else { // Successful case
                        saveState();
                        m.setName(nameTF.getText());
                        m.setDescription(descriptionTA.getText());
                        m.setGenre(genreTF.getText());
                        m.setFormat(formatTF.getText());
                        m.setYear(yearTF.getText().isBlank() ? Year.now().getValue() : Integer.parseInt(yearTF.getText()));
                        m.setYearConsumed(yearConsumedTF.getText().isBlank() ? Year.now().getValue() : Integer.parseInt(yearConsumedTF.getText()));
                        m.setRating(ratingTF.getText().isBlank() ? 0 : Double.parseDouble(ratingTF.getText()));
                        m.setColor(colorPicker.getValue());
                        if (!groupTF.getText().isBlank()) {
                            groups.put(m, groupTF.getText());
                        } else {
                            groups.remove(m);
                        }
                        ((Game) m).setDeveloper(developerTF.getText());
                        ((Game) m).setConsole(consoleTF.getText());
                        ((Game) m).setNumPlayers(numPlayersTF.getText().isBlank() ? 0 : Integer.parseInt(numPlayersTF.getText()));
                        showViewScreen(m);
                        write();
                    }
                } catch (Exception ex) {
                    errorText.setText("Invalid input");
                    MediaLibrary.setScroll(0, 0);
                }
            });
        } else if (m instanceof Music) {
            Text artistText = new Text(200, 475, "Artist: ");
            TextField artistTF = new TextField();
            artistTF.setText(((Music) m).getArtist());
            artistTF.setLayoutX(350);
            artistTF.setLayoutY(460);

            groupText.setY(515);
            groupTF.setLayoutY(500);

            buttonY = 550;
            btSave.setLayoutY(buttonY);
            btCancel.setLayoutY(buttonY);

            this.getChildren().addAll(artistText, artistTF);

            btSave.setOnAction(e -> {
                try {
                    if (nameTF.getText().isBlank()) {
                        errorText.setText("You must enter a music name");
                        MediaLibrary.setScroll(0, 0);
                    } else if (!ratingTF.getText().isBlank() && Media.validateRating(Double.parseDouble(ratingTF.getText()))) {
                        errorText.setText("Rating must be between 0 and 10");
                        MediaLibrary.setScroll(0, 0);
                    } else { // Successful case
                        saveState();
                        m.setName(nameTF.getText());
                        m.setDescription(descriptionTA.getText());
                        m.setGenre(genreTF.getText());
                        m.setFormat(formatTF.getText());
                        m.setYear(yearTF.getText().isBlank() ? Year.now().getValue() : Integer.parseInt(yearTF.getText()));
                        m.setYearConsumed(yearConsumedTF.getText().isBlank() ? Year.now().getValue() : Integer.parseInt(yearConsumedTF.getText()));
                        m.setRating(ratingTF.getText().isBlank() ? 0 : Double.parseDouble(ratingTF.getText()));
                        m.setColor(colorPicker.getValue());
                        if (!groupTF.getText().isBlank()) {
                            groups.put(m, groupTF.getText());
                        } else {
                            groups.remove(m);
                        }
                        ((Music) m).setArtist(artistTF.getText());
                        showViewScreen(m);
                        write();
                    }
                } catch (Exception ex) {
                    errorText.setText("Invalid input");
                    MediaLibrary.setScroll(0, 0);
                }
            });
        } else if (m instanceof Book) {
            Text authorText = new Text(200, 475, "Author: ");
            TextField authorTF = new TextField();
            authorTF.setText(((Book) m).getAuthor());
            authorTF.setLayoutX(350);
            authorTF.setLayoutY(460);

            groupText.setY(515);
            groupTF.setLayoutY(500);

            buttonY = 550;
            btSave.setLayoutY(buttonY);
            btCancel.setLayoutY(buttonY);

            this.getChildren().addAll(authorText, authorTF);

            btSave.setOnAction(e -> {
                try {
                    if (nameTF.getText().isBlank()) {
                        errorText.setText("You must enter a book name");
                        MediaLibrary.setScroll(0, 0);
                    } else if (!ratingTF.getText().isBlank() && Media.validateRating(Double.parseDouble(ratingTF.getText()))) {
                        errorText.setText("Rating must be between 0 and 10");
                        MediaLibrary.setScroll(0, 0);
                    } else { // Successful case
                        saveState();
                        m.setName(nameTF.getText());
                        m.setDescription(descriptionTA.getText());
                        m.setGenre(genreTF.getText());
                        m.setFormat(formatTF.getText());
                        m.setYear(yearTF.getText().isBlank() ? Year.now().getValue() : Integer.parseInt(yearTF.getText()));
                        m.setYearConsumed(yearConsumedTF.getText().isBlank() ? Year.now().getValue() : Integer.parseInt(yearConsumedTF.getText()));
                        m.setRating(ratingTF.getText().isBlank() ? 0 : Double.parseDouble(ratingTF.getText()));
                        m.setColor(colorPicker.getValue());
                        if (!groupTF.getText().isBlank()) {
                            groups.put(m, groupTF.getText());
                        } else {
                            groups.remove(m);
                        }
                        ((Book) m).setAuthor(authorTF.getText());
                        showViewScreen(m);
                        write();
                    }
                } catch (Exception ex) {
                    errorText.setText("Invalid input");
                    MediaLibrary.setScroll(0, 0);
                }
            });
        }

        btCancel.setOnAction(e -> showViewScreen(m));

        Rectangle rect = new Rectangle(50, 0, 125, buttonY + 50);
        rect.setFill(m.getColor());
        this.getChildren().add(rect);
    }

    /** Returns a shortened String of a Text object */
    private static String shortenName(Text t, int pixels) {
        String name = t.getText();
        double length = calculateTextLength(t);

        if (length > pixels) {
            // Calculate the percentage (between 0 and 1) of the name that can fit, multiply by
            // the length of the Text object to find the maximum number of characters that fit
            double limit = (270 / length) * name.length();
            StringBuilder shortName = new StringBuilder();

            for (int i = 0; i < limit && i < name.length(); i++) {
                shortName.append(name.charAt(i));
            }

            shortName.append("..."); // Append ellipses

            return shortName.toString();
        } else {
            return name;
        }
    }

    /** Returns a deep copy of an ArrayList<Media> object */
    private static ArrayList<Media> copy(ArrayList<Media> arr) {
        try {
            ArrayList<Media> newArr = new ArrayList<>();
            for (Media m : arr) {
                newArr.add((Media) m.clone());
            }
            return newArr;
        } catch (Exception ex) {
            return new ArrayList<>(arr); // Shallow copy
        }
    }

    /** Calculates length of a text object */
    private static double calculateTextLength(Text t) {
        return t.getLayoutBounds().getMaxX() - t.getLayoutBounds().getMinX();
    }

    /** Calculates the height of a text object */
    private static double calculateTextHeight(Text t) {
        return t.getLayoutBounds().getMaxY() - t.getLayoutBounds().getMinY();
    }
}
