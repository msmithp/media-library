// **********************************************************************************
// Title: LibraryView
// Author: Matthew Smith
// Course Section: CMIS202-ONL1 (Seidel) Spring 2023
// File: Library.java
// Description: Handles graphical output for the library in MediaLibrary
// **********************************************************************************

package com.matthewsmith.medialibrary;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.Stage;

import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class LibraryView extends Pane {
    private Library<Media> library;
    private Stack<Library<Media>> history; // History of user actions, for undo function
    private Stack<Library<Media>> undoHistory; // History of undone actions, for redo function
    private final double WIDTH = 798;
    private final double HEIGHT = 467;

    /** Constructors */
    public LibraryView() {
        this(new Library<>());
    }

    public LibraryView(Library<Media> library) {
        this.library = library;
        this.history = new Stack<>();
        this.undoHistory = new Stack<>();
    }

    /** Returns the Library object */
    public Library<Media> getLibrary() {
        return library;
    }

    /** Adds an element to the library */
    public void add(Media m) {
        saveState();
        library.add(m);
        library.write();
        draw();
    }

    /** Removes an element from the library */
    public void remove(Media m) {
        saveState();
        library.remove(m);
        MediaLibrary.setSize(library.getSize());
        library.write();
        draw();
    }

    /** Undoes previous action */
    public void undo() {
        if (!history.isEmpty()) {
            saveUndoneState();
            this.library = history.pop();
            library.write();
            draw();
        }
    }

    /** Redoes previously undone action */
    public void redo() {
        if (!undoHistory.isEmpty()) {
            history.push(new Library<>(copy(library.getMedia()), copy(library.getGroups()), library.getFile()));
            this.library = undoHistory.pop();
            library.write();
            draw();
        }
    }

    /** Saves current state of library to history stack */
    private void saveState() {
        history.push(new Library<>(copy(library.getMedia()), copy(library.getGroups()), library.getFile()));
        undoHistory.clear();
    }

    /** Saves current state of library to undo history stack */
    private void saveUndoneState() {
        undoHistory.push(new Library<>(copy(library.getMedia()), copy(library.getGroups()), library.getFile()));
    }

    /** Draws the library */
    public void draw() {
        this.getChildren().clear();

        double x = 60; // Starting x-value
        for (Media m : library) {
            drawEntry(m, x);
            x += 80;
        }

        MediaLibrary.setTitle("My Media Library");
        // Bottom shelf bar
        this.getChildren().add(new Rectangle(0, HEIGHT - 70, Math.max(WIDTH, 60 + x), 15));
        this.setPrefWidth(Math.max(WIDTH, 60 + x));
    }

    /** Draws a group */
    public void drawGroup(String name) {
        this.getChildren().clear();

        HashMap<Media, String> groups = library.getGroups();
        if (!groups.containsValue(name)) {
            return;
        }

        double x = 60;
        for (Media m : groups.keySet()) {
            String val = groups.get(m);
            if (val != null && val.equals(name)) {
                drawEntry(m, x);
                x += 80;
            }
        }

        // Bottom shelf bar
        this.getChildren().add(new Rectangle(0, HEIGHT - 70, Math.max(WIDTH, 60 + x), 15));
    }

    /** Draws one media entry (rectangle and text) */
    private void drawEntry(Media m, double x) {
        Rectangle entry = new Rectangle(x, 70, 65, 327);
        entry.setFill(Color.color(m.getColorArray()[0], m.getColorArray()[1], m.getColorArray()[2], m.getColorArray()[3]));

        Text nameText = new Text(0, 233.5, m.getName());
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
        nameText.setFill(m.getColorArray()[0] + m.getColorArray()[1] +
                m.getColorArray()[2] < 1.6 ? Color.WHITE : Color.BLACK);

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

        // Entry context menu
        MenuItem itemView = new MenuItem("View");
        MenuItem itemEdit = new MenuItem("Edit");
        MenuItem itemDel = new MenuItem("Delete");

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(itemView, itemEdit, itemDel);

        entry.setOnContextMenuRequested(e -> contextMenu.show(entry, e.getScreenX(), e.getScreenY()));
        nameText.setOnContextMenuRequested(e -> contextMenu.show(entry, e.getScreenX(), e.getScreenY()));

        itemView.setOnAction(e -> showViewScreen(m));
        itemEdit.setOnAction(e -> showEditScreen(m));
        itemDel.setOnAction(e -> {
            remove(m);
            draw();
        });

        // Show "view" screen when an entry is left-clicked
        entry.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                showViewScreen(m);
            }
        });

        nameText.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                showViewScreen(m);
            }
        });

        this.getChildren().addAll(entry, nameText);
    }

    /** Displays a screen to view information about a piece of media */
    private void showViewScreen(Media m) {
        double currentY = 25; // Starting y-value
        final double leftX = 200;
        final double rightX = 500;
        final double wrapWidth = 225;

        Pane mediaPane = new Pane();
        Stage stage = new Stage();

        String group = library.getGroups().get(m);

        if (group != null) {
            Text groupText = new Text(leftX, currentY, group);
            groupText.setFont(Font.font("Arial", FontWeight.LIGHT, FontPosture.ITALIC, 14));
            groupText.setWrappingWidth(wrapWidth * 2);
            mediaPane.getChildren().add(groupText);
            currentY += Math.max(40, LibraryView.calculateTextHeight(groupText) + 25);
        } else {
            currentY += 30;
        }

        Text nameText = new Text(leftX, currentY, m.getName());
        nameText.setFont(Font.font("Arial", FontWeight.BOLD, 35));
        nameText.setWrappingWidth(500);
        currentY += (LibraryView.calculateTextHeight(nameText));

        Text descriptionText = new Text(leftX, currentY, m.getDescription());
        descriptionText.setWrappingWidth(wrapWidth * 2);
        currentY += (LibraryView.calculateTextHeight(descriptionText) + 25);

        Text genreText = new Text(leftX, currentY, "Genre: " + m.getGenre());
        genreText.setWrappingWidth(wrapWidth);
        Text formatText = new Text(rightX, currentY, "Format: " + m.getFormat());
        formatText.setWrappingWidth(wrapWidth);
        currentY += (Math.max(LibraryView.calculateTextHeight(genreText), LibraryView.calculateTextHeight(formatText)) + 25);

        Text yearText = new Text(leftX, currentY, "Year released: " + m.getYear());
        yearText.setWrappingWidth(wrapWidth);
        Text yearConsumedText = new Text(rightX, currentY, "Year consumed: " + m.getYearConsumed());
        yearConsumedText.setWrappingWidth(wrapWidth);
        currentY += (Math.max(LibraryView.calculateTextHeight(yearText), LibraryView.calculateTextHeight(yearConsumedText)) + 25);

        Text ratingText = new Text(leftX, currentY, "Rating: " + m.getRating() + "/10");
        ratingText.setWrappingWidth(wrapWidth);
        currentY += (Math.max(LibraryView.calculateTextHeight(yearText), LibraryView.calculateTextHeight(yearConsumedText)) + 75);

        Rectangle line = new Rectangle(leftX, currentY - 50, 500, 3);

        mediaPane.getChildren().addAll(nameText, descriptionText, genreText, formatText,
                yearText, yearConsumedText, ratingText, line);

        if (m instanceof Movie) {
            Text directorText = new Text(leftX, currentY, "Director: " + ((Movie) m).getDirector());
            directorText.setWrappingWidth(wrapWidth);

            Text durationText = new Text(rightX, currentY, "Duration: " + ((Movie) m).getDuration() + " minutes");
            durationText.setWrappingWidth(wrapWidth);

            currentY += (Math.max(LibraryView.calculateTextHeight(directorText), LibraryView.calculateTextHeight(durationText)) + 25);
            mediaPane.getChildren().addAll(directorText, durationText);
        } else if (m instanceof Show) {
            Text creatorText = new Text(leftX, currentY, "Creator: " + ((Show) m).getCreator());
            creatorText.setWrappingWidth(wrapWidth);

            Text numSeasonsText = new Text(rightX, currentY, "Number of seasons: " + ((Show) m).getNumSeasons());
            numSeasonsText.setWrappingWidth(wrapWidth);

            currentY += (Math.max(LibraryView.calculateTextHeight(creatorText), LibraryView.calculateTextHeight(numSeasonsText)) + 25);

            Text numEpisodesText = new Text(leftX, currentY, "Number of episodes: " + ((Show) m).getNumEpisodes());
            numEpisodesText.setWrappingWidth(wrapWidth);

            currentY += (LibraryView.calculateTextHeight(numEpisodesText) + 25);
            mediaPane.getChildren().addAll(creatorText, numSeasonsText, numEpisodesText);
        } else if (m instanceof Game) {
            Text developerText = new Text(leftX, currentY, "Developer: " + ((Game) m).getDeveloper());
            developerText.setWrappingWidth(wrapWidth);

            Text consoleText = new Text(rightX, currentY, "Console: " + ((Game) m).getConsole());
            consoleText.setWrappingWidth(wrapWidth);

            currentY += (Math.max(LibraryView.calculateTextHeight(developerText), LibraryView.calculateTextHeight(consoleText)) + 25);

            Text numPlayersText = new Text(leftX, currentY, "Number of players: " + ((Game) m).getNumPlayers());
            numPlayersText.setWrappingWidth(wrapWidth);

            currentY += (LibraryView.calculateTextHeight(numPlayersText) + 25);
            mediaPane.getChildren().addAll(developerText, consoleText, numPlayersText);
        } else if (m instanceof Music) {
            Text artistText = new Text(leftX, currentY, "Artist: " + ((Music) m).getArtist());
            artistText.setWrappingWidth(wrapWidth);

            currentY += (LibraryView.calculateTextHeight(artistText) + 25);
            mediaPane.getChildren().add(artistText);
        } else if (m instanceof Book) {
            Text authorText = new Text(leftX, currentY, "Author: " + ((Book) m).getAuthor());
            authorText.setWrappingWidth(wrapWidth);

            currentY += (LibraryView.calculateTextHeight(authorText) + 25);
            mediaPane.getChildren().add(authorText);
        }

        Text dateText = new Text(leftX, currentY, "Date added: " + m.getDateAdded());
        mediaPane.getChildren().add(dateText);
        currentY += (LibraryView.calculateTextHeight(dateText) + 25);

        Button btOK = new Button("OK");
        btOK.setPrefWidth(60);
        btOK.setLayoutX(300);
        btOK.setLayoutY(currentY);

        Button btEdit = new Button("Edit");
        btEdit.setPrefWidth(60);
        btEdit.setLayoutX(370);
        btEdit.setLayoutY(currentY);

        Button btDelete = new Button("Delete");
        btDelete.setPrefWidth(60);
        btDelete.setLayoutX(440);
        btDelete.setLayoutY(currentY);

        btOK.setOnAction(e -> stage.close());

        btEdit.setOnAction(e -> {
           showEditScreen(m);
           stage.close();
        });

        btDelete.setOnAction(e -> {
            remove(m);
            draw();
            stage.close();
        });

        Rectangle rect = new Rectangle(50, 0, 125, Math.max(currentY + 40, 450));
        rect.setFill(new LinearGradient(
                rect.getX(), rect.getY(), // start x, y
                rect.getX(), rect.getY() + rect.getHeight(), // end x, y
                false,
                CycleMethod.NO_CYCLE,
                new Stop(0, m.getColor()), new Stop(1, m.getColor().darker())));

        mediaPane.getChildren().addAll(rect, btOK, btEdit, btDelete);

        ScrollPane scroll = new ScrollPane(mediaPane);
        stage.setScene(new Scene(scroll, 800, 450));
        stage.setTitle("View media: " + m.getName());
        stage.setResizable(false);
        stage.show();
    }

    /** Displays a screen to edit information about a piece of media */
    private void showEditScreen(Media m) {
        Pane mediaPane = new Pane();
        ScrollPane scroll = new ScrollPane(mediaPane);
        Stage stage = new Stage();

        Text errorText = new Text(200, 25, "");
        errorText.setFill(Color.RED);

        double currentY = 55;
        double increment = 40;

        Text name = new Text(200, currentY, "Name: ");
        TextField nameTF = new TextField();
        nameTF.setText(m.getName());
        nameTF.setLayoutX(350);
        nameTF.setLayoutY(currentY - 15);

        currentY += increment;

        Text genre = new Text(200, currentY, "Genre: ");
        TextField genreTF = new TextField();
        genreTF.setText(m.getGenre());
        genreTF.setLayoutX(350);
        genreTF.setLayoutY(currentY - 15);

        currentY += increment;

        Text description = new Text(200, currentY, "Description: ");
        TextArea descriptionTA = new TextArea();
        descriptionTA.setText(m.getDescription());
        descriptionTA.setPrefRowCount(3);
        descriptionTA.setPrefColumnCount(20);
        descriptionTA.setWrapText(true);
        descriptionTA.setLayoutX(350);
        descriptionTA.setLayoutY(currentY - 15);

        currentY += increment * 2;

        Text format = new Text(200, currentY, "Format: ");
        TextField formatTF = new TextField();
        formatTF.setText(m.getFormat());
        formatTF.setLayoutX(350);
        formatTF.setLayoutY(currentY - 15);

        currentY += increment;

        Text year = new Text(200, currentY, "Year released: ");
        TextField yearTF = new TextField();
        yearTF.setText(m.getYear() + "");
        yearTF.setLayoutX(350);
        yearTF.setLayoutY(currentY - 15);

        currentY += increment;

        Text yearConsumed = new Text(200, currentY, "Year consumed: ");
        TextField yearConsumedTF = new TextField();
        yearConsumedTF.setText(m.getYearConsumed() + "");
        yearConsumedTF.setLayoutX(350);
        yearConsumedTF.setLayoutY(currentY - 15);

        currentY += increment;

        Text rating = new Text(200, currentY, "Your rating out of 10: ");
        TextField ratingTF = new TextField();
        ratingTF.setText(m.getRating() + "");
        ratingTF.setLayoutX(350);
        ratingTF.setLayoutY(currentY - 15);

        currentY += increment;

        Text color = new Text(200, currentY, "Color: ");
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setValue(m.getColor());
        colorPicker.setPrefHeight(30);
        colorPicker.setLayoutX(350);
        colorPicker.setLayoutY(currentY - 20);

        currentY += increment;

        Rectangle line = new Rectangle(200, currentY, 500, 3);

        currentY += increment;

        Text groupText = new Text(200, currentY, "Group: ");
        TextField groupTF = new TextField();
        String group = library.getGroups().get(m);
        groupTF.setText(group == null ? "" : group);
        groupTF.setLayoutX(350);
        groupTF.setLayoutY(currentY - 15);

        mediaPane.getChildren().addAll(errorText, name, nameTF, genre, genreTF, description, descriptionTA, format, formatTF,
                year, yearTF, yearConsumed, yearConsumedTF, rating, ratingTF, color, colorPicker, line, groupText, groupTF);

        currentY += increment;

        // Type-specific fields
        // Movie fields
        Text directorText = new Text("Director: ");
        TextField directorTF = new TextField();

        Text durationText = new Text("Duration: ");
        TextField durationTF = new TextField();

        // Show fields
        Text creatorText = new Text("Creator: ");
        TextField creatorTF = new TextField();

        Text numSeasonsText = new Text("Number of seasons: ");
        TextField numSeasonsTF = new TextField();

        Text numEpisodesText = new Text("Number of episodes: ");
        TextField numEpisodesTF = new TextField();

        // Game fields
        Text developerText = new Text("Developer: ");
        TextField developerTF = new TextField();

        Text consoleText = new Text("Console: ");
        TextField consoleTF = new TextField();

        Text numPlayersText = new Text("Number of players: ");
        TextField numPlayersTF = new TextField();

        // Music fields
        Text artistText = new Text("Artist: ");
        TextField artistTF = new TextField();

        // Book fields
        Text authorText = new Text("Author: ");
        TextField authorTF = new TextField();

        // Position and add type-specific fields
        if (m instanceof Movie) {
            directorText.setLayoutX(200);
            directorText.setLayoutY(currentY);
            directorTF.setLayoutX(350);
            directorTF.setLayoutY(currentY - 15);
            directorTF.setText(((Movie) m).getDirector());

            currentY += increment;

            durationText.setLayoutX(200);
            durationText.setLayoutY(currentY);
            durationTF.setLayoutX(350);
            durationTF.setLayoutY(currentY - 15);
            durationTF.setText(((Movie) m).getDuration() + "");

            mediaPane.getChildren().addAll(directorText, directorTF, durationText, durationTF);
        } else if (m instanceof Show) {
            creatorText.setLayoutX(200);
            creatorText.setLayoutY(currentY);
            creatorTF.setLayoutX(350);
            creatorTF.setLayoutY(currentY - 15);
            creatorTF.setText(((Show) m).getCreator());

            currentY += increment;

            numSeasonsText.setLayoutX(200);
            numSeasonsText.setLayoutY(currentY);
            numSeasonsTF.setLayoutX(350);
            numSeasonsTF.setLayoutY(currentY - 15);
            numSeasonsTF.setText(((Show) m).getNumSeasons() + "");

            currentY += increment;

            numEpisodesText.setLayoutX(200);
            numEpisodesText.setLayoutY(currentY);
            numEpisodesTF.setLayoutX(350);
            numEpisodesTF.setLayoutY(currentY - 15);
            numEpisodesTF.setText(((Show) m).getNumEpisodes() + "");

            mediaPane.getChildren().addAll(creatorText, creatorTF, numSeasonsText, numSeasonsTF,
                    numEpisodesText, numEpisodesTF);
        } else if (m instanceof Game) {
            developerText.setLayoutX(200);
            developerText.setLayoutY(currentY);
            developerTF.setLayoutX(350);
            developerTF.setLayoutY(currentY - 15);
            developerTF.setText(((Game) m).getDeveloper());

            currentY += increment;

            consoleText.setLayoutX(200);
            consoleText.setLayoutY(currentY);
            consoleTF.setLayoutX(350);
            consoleTF.setLayoutY(currentY - 15);
            consoleTF.setText(((Game) m).getConsole());

            currentY += increment;

            numPlayersText.setLayoutX(200);
            numPlayersText.setLayoutY(currentY);
            numPlayersTF.setLayoutX(350);
            numPlayersTF.setLayoutY(currentY - 15);
            numPlayersTF.setText(((Game) m).getNumPlayers() + "");

            mediaPane.getChildren().addAll(developerText, developerTF, consoleText, consoleTF,
                    numPlayersText, numPlayersTF);
        } else if (m instanceof Music) {
            artistText.setLayoutX(200);
            artistText.setLayoutY(currentY);
            artistTF.setLayoutX(350);
            artistTF.setLayoutY(currentY - 15);
            artistTF.setText(((Music) m).getArtist());

            mediaPane.getChildren().addAll(artistText, artistTF);
        } else if (m instanceof Book) {
            authorText.setLayoutX(200);
            authorText.setLayoutY(currentY);
            authorTF.setLayoutX(350);
            authorTF.setLayoutY(currentY - 15);
            authorTF.setText(((Book) m).getAuthor());

            mediaPane.getChildren().addAll(authorText, authorTF);
        }

        currentY += increment;

        Button btSave = new Button("Save");
        btSave.setPrefWidth(60);
        btSave.setLayoutX(335);
        btSave.setLayoutY(currentY);

        Button btCancel = new Button("Cancel");
        btCancel.setPrefWidth(60);
        btCancel.setLayoutX(405);
        btCancel.setLayoutY(currentY);

        mediaPane.getChildren().addAll(btSave, btCancel);

        btCancel.setOnAction(e -> stage.close());

        btSave.setOnAction(e -> {
            try {
                if (nameTF.getText().isBlank()) {
                    errorText.setText("You must enter a " + m.getClass().getSimpleName() + " name");
                    scroll.setVvalue(0);
                } else if (!ratingTF.getText().isBlank() && Media.validateRating(Double.parseDouble(ratingTF.getText()))) {
                    errorText.setText("Rating must be between 0 and 10");
                    scroll.setVvalue(0);
                } else {
                    String n = nameTF.getText();
                    String g = genreTF.getText();
                    String desc = descriptionTA.getText();
                    String f = formatTF.getText();
                    int y = yearTF.getText().isBlank() ? Year.now().getValue() : Integer.parseInt(yearTF.getText());
                    int yc = yearConsumedTF.getText().isBlank() ? Year.now().getValue() : Integer.parseInt(yearConsumedTF.getText());
                    double r = ratingTF.getText().isBlank() ? 0 : Double.parseDouble(ratingTF.getText());
                    double[] c = {colorPicker.getValue().getRed(), colorPicker.getValue().getGreen(),
                            colorPicker.getValue().getBlue(), colorPicker.getValue().getOpacity()};
                    String gr = groupTF.getText().isBlank() ? null : groupTF.getText();

                    if (m instanceof Movie) {
                        if (!durationTF.getText().isBlank() && Integer.parseInt(durationTF.getText()) < 0) {
                            errorText.setText("Duration cannot be less than 0");
                            scroll.setVvalue(0);
                        } else { // Successful case
                            saveState();
                            String dir = directorTF.getText();
                            int d = durationTF.getText().isBlank() ? 0 : Integer.parseInt(durationTF.getText());

                            if (gr != null) {
                                library.getGroups().remove(m);
                            }

                            m.setName(n);
                            m.setGenre(g);
                            m.setDescription(desc);
                            m.setFormat(f);
                            m.setYear(y);
                            m.setYearConsumed(yc);
                            m.setRating(r);
                            m.setColorArray(c);
                            ((Movie) m).setDirector(dir);
                            ((Movie) m).setDuration(d);

                            if (gr != null) {
                                library.getGroups().put(m, gr);
                            }

                            draw();
                            stage.close();
                            library.write();
                            showViewScreen(m);
                        }
                    } else if (m instanceof Show) {
                        if (!numEpisodesTF.getText().isBlank() && Integer.parseInt(numEpisodesTF.getText()) < 0) {
                            errorText.setText("Number of episodes cannot be less than 0");
                            scroll.setVvalue(0);
                        } else if(!numSeasonsTF.getText().isBlank() && Integer.parseInt(numSeasonsTF.getText()) < 0) {
                            errorText.setText("Number of seasons cannot be less than 0");
                            scroll.setVvalue(0);
                        } else {
                            saveState();
                            String cr = creatorTF.getText();
                            int ns = numSeasonsTF.getText().isBlank() ? 0 : Integer.parseInt(numSeasonsTF.getText());
                            int ne = numEpisodesTF.getText().isBlank() ? 0 : Integer.parseInt(numEpisodesTF.getText());

                            if (gr != null) {
                                library.getGroups().remove(m);
                            }

                            m.setName(n);
                            m.setGenre(g);
                            m.setDescription(desc);
                            m.setFormat(f);
                            m.setYear(y);
                            m.setYearConsumed(yc);
                            m.setRating(r);
                            m.setColorArray(c);
                            ((Show) m).setCreator(cr);
                            ((Show) m).setNumSeasons(ns);
                            ((Show) m).setNumEpisodes(ne);

                            if (gr != null) {
                                library.getGroups().put(m, gr);
                            }

                            draw();
                            stage.close();
                            library.write();
                            showViewScreen(m);
                        }
                    } else if (m instanceof Game) {
                        if (!numPlayersTF.getText().isBlank() && Integer.parseInt(numPlayersTF.getText()) < 0) {
                            errorText.setText("Number of players cannot be less than 0");
                            scroll.setVvalue(0);
                        } else {
                            saveState();
                            String dev = developerTF.getText();
                            String con = consoleTF.getText();
                            int np = numPlayersTF.getText().isBlank() ? 0 : Integer.parseInt(numPlayersTF.getText());

                            if (gr != null) {
                                library.getGroups().remove(m);
                            }

                            m.setName(n);
                            m.setGenre(g);
                            m.setDescription(desc);
                            m.setFormat(f);
                            m.setYear(y);
                            m.setYearConsumed(yc);
                            m.setRating(r);
                            m.setColorArray(c);
                            ((Game) m).setDeveloper(dev);
                            ((Game) m).setConsole(con);
                            ((Game) m).setNumPlayers(np);

                            if (gr != null) {
                                library.getGroups().put(m, gr);
                            }

                            draw();
                            stage.close();
                            library.write();
                            showViewScreen(m);
                        }
                    } else if (m instanceof Music) {
                        saveState();
                        String art = artistTF.getText();

                        if (gr != null) {
                            library.getGroups().remove(m);
                        }

                        m.setName(n);
                        m.setGenre(g);
                        m.setDescription(desc);
                        m.setFormat(f);
                        m.setYear(y);
                        m.setYearConsumed(yc);
                        m.setRating(r);
                        m.setColorArray(c);
                        ((Music) m).setArtist(art);

                        if (gr != null) {
                            library.getGroups().put(m, gr);
                        }

                        draw();
                        stage.close();
                        library.write();
                        showViewScreen(m);
                    } else if (m instanceof Book) {
                        saveState();
                        String auth = authorTF.getText();

                        if (gr != null) {
                            library.getGroups().remove(m);
                        }

                        m.setName(n);
                        m.setGenre(g);
                        m.setDescription(desc);
                        m.setFormat(f);
                        m.setYear(y);
                        m.setYearConsumed(yc);
                        m.setRating(r);
                        m.setColorArray(c);
                        ((Book) m).setAuthor(auth);

                        if (gr != null) {
                            library.getGroups().put(m, gr);
                        }

                        draw();
                        stage.close();
                        library.write();
                        showViewScreen(m);
                    }
                }
            } catch (Exception ex) {
                errorText.setText("Invalid input");
            }
        });

        Rectangle rect = new Rectangle(50, 0, 125, Math.max(currentY + 40, 450));
        rect.setFill(new LinearGradient(
                rect.getX(), rect.getY(), // start x, y
                rect.getX(), rect.getY() + rect.getHeight(), // end x, y
                false,
                CycleMethod.NO_CYCLE,
                new Stop(0, m.getColor()), new Stop(1, m.getColor().darker())));

        mediaPane.getChildren().add(rect);

        stage.setScene(new Scene(scroll, 800, 450));
        stage.setTitle("Edit media: " + m.getName());
        stage.setResizable(false);
        stage.show();
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

    /** Returns a deep copy of a HashMap<Media, String> object */
    private static HashMap<Media, String> copy(HashMap<Media, String> map) {
        try {
            HashMap<Media, String> newMap = new HashMap<>();
            for (Media m : map.keySet()) {
                newMap.put((Media) m.clone(), map.get(m));
            }
            return newMap;
        } catch (Exception ex) {
            return new HashMap<>(map); // Shallow copy
        }
    }

    /** Calculates length of a text object */
    public static double calculateTextLength(Text t) {
        if (t == null)
            return 0;
        return t.getLayoutBounds().getMaxX() - t.getLayoutBounds().getMinX();
    }

    /** Calculates the height of a text object */
    public static double calculateTextHeight(Text t) {
        if (t == null)
            return 0;
        return t.getLayoutBounds().getMaxY() - t.getLayoutBounds().getMinY();
    }
}
