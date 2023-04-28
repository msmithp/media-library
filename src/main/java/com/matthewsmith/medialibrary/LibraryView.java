// **********************************************************************************
// Title: LibraryView
// Author: Matthew Smith
// Course Section: CMIS202-ONL1 (Seidel) Spring 2023
// File: Library.java
// Description: Handles graphical output for the library in MediaLibrary
// **********************************************************************************

package com.matthewsmith.medialibrary;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.time.Year;
import java.util.ArrayList;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class LibraryView extends Pane {
    private final Library<Media> library;
    private final Stack<Command> history; // History of user actions, for undo function
    private final Stack<Command> undoHistory; // History of undone actions, for redo function
    private final ArrayList<String> excludeList = new ArrayList<>(); // list of excluded classes when drawing

    /** Creates an empty LibraryView */
    public LibraryView() {
        this(new Library<>());
    }

    /** Creates a LibraryView from a library */
    public LibraryView(Library<Media> library) {
        this.library = library;
        this.history = new Stack<>();
        this.undoHistory = new Stack<>();
    }

    /** Adds an element to the library */
    public void add(Media m) {
        pushAction(new Add(library, (Media) m.clone()));
        library.add(m.getName(), m); // Add to library
        library.write();
        draw();
    }

    /** Removes an element from the library */
    public void remove(Media m) {
        pushAction(new Remove(library, (Media) m.clone()));
        library.remove(m.getName(), m); // Remove from library
        MediaLibrary.setSize(library.getSize());
        library.write();
        draw();
    }

    /** Undoes previous action */
    public void undo() {
        if (!history.isEmpty()) {
            Command command = history.pop();
            command.unExecute();
            pushUndoneAction(command);
            library.write();
            draw();
        }
    }

    /** Redoes previously undone action */
    public void redo() {
        if (!undoHistory.isEmpty()) {
            Command command = undoHistory.pop();
            command.execute();
            history.push(command);
            library.write();
            draw();
        }
    }

    /** Add a filter to the exclude list */
    public void addFilter(String className) {
        if (!excludeList.contains(className)) {
            this.excludeList.add(className);
            draw();
        }
    }

    /** Remove a filter from the exclude list */
    public void removeFilter(String className) {
        this.excludeList.remove(className);
        draw();
    }

    /** Reset exclude list to exclude no types */
    public void resetFilters() {
        this.excludeList.clear();
        draw();
    }

    /** Saves current state of library to history stack */
    private void pushAction(Command action) {
        history.push(action);
        undoHistory.clear(); // redo action is only accessible when an action has first been undone
    }

    /** Saves current state of library to undo history stack */
    private void pushUndoneAction(Command action) {
        undoHistory.push(action);
    }

    /** Draws the library */
    public void draw() {
        ArrayList<Media> filteredList = new ArrayList<>();

        // Apply filter to all media in library
        for (int i = 0; i < library.getSize(); i++) {
            Media m = library.getMedia().get(i);
            if (!excludeList.contains(m.getClass().getSimpleName())) {
                filteredList.add(m);
            }
        }
        draw(filteredList);
    }

    /** DrawTask class for parallel draw method implementation */
    private class DrawTask extends RecursiveAction {
        private final int THRESHOLD = 100;
        private ArrayList<Media> list;
        private int start;
        private int end;

        public DrawTask(ArrayList<Media> list, int start, int end) {
            this.list = list;
            this.start = start;
            this.end = end;
        }

        @Override
        protected void compute() {
            if (end - start < THRESHOLD) {
                for (int i = start; i < end; i++) {
                    // Check if media class is being filtered
                    if (!excludeList.contains(list.get(i).getClass().getSimpleName())) {
                        int x = (i * 80) + 60;
                        drawEntry(list.get(i), x);
                    }
                }
            } else {
                int middle = (start + end) / 2;
                invokeAll(new DrawTask(list, start, middle),
                        new DrawTask(list, middle, end));
            }
        }
    }

    /** Draws an ArrayList of Media */
    public void draw(ArrayList<Media> list) {
        this.getChildren().clear();
        RecursiveAction mainTask = new DrawTask(list, 0, list.size());
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(mainTask); // invoke task
        drawShelfBar(library.getSize());
        MediaLibrary.setTitle("My Media Library");
    }

    /** Draws a group */
    public void drawGroup(String name) {
        this.getChildren().clear();

        if (!library.isGroup(name)) {
            return; // entered group does not exist
        }

        Set<Media> keys = library.getGroupKeys(); // Get Media that are in groups

        double x = 60;
        int numEntries = 0;

        // Iterate through Media in groups
        for (Media m : keys) {
            String val = library.getGroup(m);
            if (val != null && val.equals(name)) {
                drawEntry(m, x);
                x += 80;
                numEntries++;
            }
        }

        drawShelfBar(numEntries);
    }

    /** Draws the bottom shelf bar */
    private void drawShelfBar(int numEntries) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = screenBounds.getWidth();

        // Draw shelf bar to be either the width of the full screen or
        // the width of the entire library, whichever is larger
        Rectangle bar = new Rectangle(0, 397, Math.max(screenWidth, 105 + 80 * numEntries), 15);
        bar.setFill(Color.WHITE);
        this.getChildren().add(bar);
    }

    /** Draws one media entry (rectangle and text) */
    private void drawEntry(Media m, double x) {
        Rectangle entry = new Rectangle(x, 70, 65, 327);
        entry.setFill(m.getColor()); // set fill color

        Text nameText = new Text(0, 240, m.getName());

        // Font is set to allow for accurate length calculation
        nameText.setFont(Font.font("Manrope", FontWeight.BOLD, 18));
        nameText.getStyleClass().add("entry");
        double textLength = calculateTextLength(nameText);

        if (textLength > 270) {
            nameText.setText(shortenText(nameText, 270)); // Shorten name
            textLength = calculateTextLength(nameText);
        }

        // Text is centered on rectangle before being rotated
        double leftBound = (x + 32.5) - (textLength / 2);
        nameText.setX(leftBound);
        nameText.setRotate(270); // Text is rotated

        // Name color is set to black or white depending on the lightness of the rectangle color
        nameText.setStyle(m.getColorArray()[0] + m.getColorArray()[1] +
                m.getColorArray()[2] < 1.6 ? "-fx-fill: white;" : "-fx-fill: black;");

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

        Platform.runLater(() -> this.getChildren().addAll(entry, nameText));
    }

    /** Displays a screen to view information about a piece of media */
    private void showViewScreen(Media m) {
        final int wrapWidth = 250;

        // Set up stage and VBox
        Stage stage = new Stage();
        VBox vb = new VBox(20);
        vb.setLayoutX(200);
        vb.setAlignment(Pos.CENTER_LEFT);
        vb.setPadding(new Insets(10, 0, 20, 0));

        // Add group, if one exists
        String group = library.getGroup(m);
        if (group != null) {
            Text groupText = new Text(group);
            groupText.setWrappingWidth(wrapWidth * 2);
            groupText.getStyleClass().add("group");
            HBox groupBox = new HBox();
            groupBox.getChildren().add(groupText);
            groupBox.setPadding(new Insets(0, 0, -30, 0));
            vb.getChildren().add(groupBox);
        }

        // Title text
        Text title = new Text(m.getName());
        title.getStyleClass().add("title");
        title.setWrappingWidth(wrapWidth * 2);

        // Description
        Text description = new Text(m.getDescription());
        description.setWrappingWidth(wrapWidth * 2);

        // Genre and format row
        HBox genreAndFormat = new HBox(10);
        Text genre = new Text("Genre: " + m.getGenre());
        genre.setWrappingWidth(wrapWidth);
        Text format = new Text("Format: " +  m.getFormat());
        format.setWrappingWidth(wrapWidth);
        genreAndFormat.getChildren().addAll(genre, format);

        // Year released and year consumed row
        HBox yearReleasedConsumed = new HBox(10);
        Text year = new Text("Year released: " +
                (m.getYear() >= 0 ? m.getYear() : Math.abs(m.getYear()) + " B.C.E."));
        year.setWrappingWidth(wrapWidth);
        Text yearConsumed = new Text("Year consumed: " + (m.getYearConsumed() >= 0 ?
                m.getYearConsumed() : Math.abs(m.getYearConsumed()) + " B.C.E."));
        yearConsumed.setWrappingWidth(wrapWidth);
        yearReleasedConsumed.getChildren().addAll(year, yearConsumed);

        // Rating out of 10
        Text ratingText = new Text("Rating: " + m.getRating() + "/10");
        ratingText.setWrappingWidth(wrapWidth);

        // Dividing line between general and type-specific fields
        Rectangle line = new Rectangle(0, 0, 500, 3);
        line.setFill(Color.WHITE);
        StackPane rectPane = new StackPane();
        rectPane.getChildren().add(line);

        // Add all to VBox
        vb.getChildren().addAll(title, description, genreAndFormat,
                yearReleasedConsumed, ratingText, rectPane);

        // Type-specific fields
        if (m instanceof Movie) {
            // Director and duration row
            HBox directorDuration = new HBox(10);
            Text director = new Text("Director: " + ((Movie) m).getDirector());
            director.setWrappingWidth(wrapWidth);
            Text duration = new Text("Duration: " + ((Movie) m).getDuration());
            duration.setWrappingWidth(wrapWidth);

            // Add all to VBox
            directorDuration.getChildren().addAll(director, duration);
            vb.getChildren().add(directorDuration);
        } else if (m instanceof Show) {
            // Creator and seasons row
            HBox creatorSeasons = new HBox(10);
            Text creator = new Text("Creator: " + ((Show) m).getCreator());
            creator.setWrappingWidth(wrapWidth);
            Text numSeasons = new Text("Number of seasons: " + ((Show) m).getNumSeasons());
            numSeasons.setWrappingWidth(wrapWidth);

            // Number of episodes
            Text numEpisodes = new Text("Number of episodes: " + ((Show) m).getNumEpisodes());
            numEpisodes.setWrappingWidth(wrapWidth);

            // Add all to VBox
            creatorSeasons.getChildren().addAll(creator, numSeasons);
            vb.getChildren().addAll(creatorSeasons, numEpisodes);
        } else if (m instanceof Game) {
            // Developer and console row
            HBox developerConsole = new HBox(10);
            Text developer = new Text("Developer: " + ((Game) m).getDeveloper());
            developer.setWrappingWidth(wrapWidth);
            Text console = new Text("Console: " + ((Game) m).getConsole());
            console.setWrappingWidth(wrapWidth);

            // Number of players
            Text numPlayers = new Text("Number of players: " + ((Game) m).getNumPlayers());
            numPlayers.setWrappingWidth(wrapWidth);

            // Add all to VBox
            developerConsole.getChildren().addAll(developer, console);
            vb.getChildren().addAll(developerConsole, numPlayers);
        } else if (m instanceof Music) {
            // Artist
            Text artist = new Text("Artist: " + ((Music) m).getArtist());
            artist.setWrappingWidth(wrapWidth);

            // Add to VBox
            vb.getChildren().add(artist);
        } else if (m instanceof Book) {
            // Author
            Text author = new Text("Author: " + ((Book) m).getAuthor());
            author.setWrappingWidth(wrapWidth);

            // Add to VBox
            vb.getChildren().add(author);
        }

        // Date added to library
        Text date = new Text("Date added: " + m.getDateAdded());
        date.setWrappingWidth(wrapWidth * 2);

        // Buttons
        Button btOK = new Button("OK");
        btOK.setPrefWidth(70);
        Button btEdit = new Button("Edit");
        btEdit.setPrefWidth(70);
        Button btDelete = new Button("Delete");
        btDelete.setPrefWidth(70);
        Button btSimilar = new Button("Find Similar Media");
        btSimilar.setPrefWidth(150);
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll(btOK, btEdit, btDelete, btSimilar);

        vb.getChildren().addAll(date, buttons);

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

        btSimilar.setOnAction(e -> {
            showSimilarMedia(m);
            stage.close();
        });

        Rectangle rect = new Rectangle(50, 0, 125, 450);

        Pane pane = new Pane();
        pane.getChildren().addAll(rect, vb);

        Scene scene = new Scene(new ScrollPane(pane), 800, 450);
        scene.getStylesheets().add(MediaLibrary.css);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("View " + m.getClass().getSimpleName() + ": " + m.getName());
        stage.show();

        // Sidebar positioned and filled after showing stage
        rect.setHeight(Math.max(vb.getHeight(), 450));
        rect.setFill(new LinearGradient(0, 0,0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, m.getColor()), new Stop(1, m.getColor().darker())));
    }

    /** Displays a screen to edit information about a piece of media */
    private void showEditScreen(Media m) {
        final int wrapWidth = 200;

        Stage stage = new Stage();
        VBox vb = new VBox(20);
        vb.setLayoutX(200);
        vb.setAlignment(Pos.CENTER_LEFT);
        vb.setPadding(new Insets(10, 0, 20, 0));

        Pane pane = new Pane();
        ScrollPane scroll = new ScrollPane(pane);

        GridPane mainGrid = new GridPane();
        mainGrid.setVgap(15);

        // Error text
        Text errorText = new Text();
        errorText.setStyle("-fx-fill: red;");

        // Name of media
        Text name = new Text("Name:");
        name.setWrappingWidth(wrapWidth);
        TextField nameTF = new TextField(m.getName());

        // Genre
        Text genre = new Text("Genre:");
        genre.setWrappingWidth(wrapWidth);
        TextField genreTF = new TextField(m.getGenre());

        // Description
        Text description = new Text("Description:");
        description.setWrappingWidth(wrapWidth);
        TextArea descriptionTA = new TextArea(m.getDescription());
        descriptionTA.setWrapText(true);
        descriptionTA.setPrefRowCount(3);
        descriptionTA.setPrefColumnCount(19);

        // Format
        Text format = new Text("Format:");
        format.setWrappingWidth(wrapWidth);
        TextField formatTF = new TextField(m.getFormat());

        // Year released
        Text year = new Text("Year released:");
        year.setWrappingWidth(wrapWidth);
        TextField yearTF = new TextField(m.getYear() + "");

        // Year consumed
        Text yearConsumed = new Text("Year consumed:");
        yearConsumed.setWrappingWidth(wrapWidth);
        TextField yearConsumedTF = new TextField(m.getYearConsumed() + "");

        // Rating out of 10
        Text rating = new Text("Your rating out of 10:");
        rating.setWrappingWidth(wrapWidth);
        TextField ratingTF = new TextField(m.getRating() + "");

        // Color
        Text color = new Text("Color:");
        color.setWrappingWidth(wrapWidth);
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setValue(m.getColor());

        // Add columns
        mainGrid.addColumn(0, name, genre, description, format, year, yearConsumed, rating, color);
        mainGrid.addColumn(1, nameTF, genreTF, descriptionTA, formatTF, yearTF, yearConsumedTF, ratingTF, colorPicker);

        Rectangle line = new Rectangle(0, 0, 500, 3);
        line.setFill(Color.WHITE);

        GridPane subGrid = new GridPane();
        subGrid.setVgap(15);

        Text groupText = new Text("Group:");
        groupText.setWrappingWidth(wrapWidth);
        TextField groupTF = new TextField();
        groupTF.setPrefColumnCount(21);
        String group = library.getGroup(m);
        groupTF.setText(group == null ? "" : group);

        // Type-specific fields
        // Movie fields
        Text director = new Text("Director:");
        director.setWrappingWidth(wrapWidth);
        TextField directorTF = new TextField();

        Text duration = new Text("Duration:");
        duration.setWrappingWidth(wrapWidth);
        TextField durationTF = new TextField();

        // Show fields
        Text creator = new Text("Creator:");
        creator.setWrappingWidth(wrapWidth);
        TextField creatorTF = new TextField();

        Text numSeasons = new Text("Number of seasons:");
        numSeasons.setWrappingWidth(wrapWidth);
        TextField numSeasonsTF = new TextField();

        Text numEpisodes = new Text("Number of episodes:");
        numEpisodes.setWrappingWidth(wrapWidth);
        TextField numEpisodesTF = new TextField();

        // Game fields
        Text developer = new Text("Developer:");
        developer.setWrappingWidth(wrapWidth);
        TextField developerTF = new TextField();

        Text console = new Text("Console:");
        console.setWrappingWidth(wrapWidth);
        TextField consoleTF = new TextField();

        Text numPlayers = new Text("Number of players:");
        numPlayers.setWrappingWidth(wrapWidth);
        TextField numPlayersTF = new TextField();

        // Music fields
        Text artist = new Text("Artist:");
        artist.setWrappingWidth(wrapWidth);
        TextField artistTF = new TextField();

        // Book fields
        Text author = new Text("Author:");
        author.setWrappingWidth(wrapWidth);
        TextField authorTF = new TextField();

        // Add type-specific fields
        if (m  instanceof Movie) {
            directorTF.setText(((Movie) m).getDirector());
            durationTF.setText(((Movie) m).getDuration() + "");
            subGrid.addColumn(0, groupText, director, duration);
            subGrid.addColumn(1, groupTF, directorTF, durationTF);
        } else if (m instanceof Show) {
            creatorTF.setText(((Show) m).getCreator());
            numSeasonsTF.setText(((Show) m).getNumSeasons() + "");
            numEpisodesTF.setText(((Show) m).getNumEpisodes() + "");
            subGrid.addColumn(0, groupText, creator, numSeasons, numEpisodes);
            subGrid.addColumn(1, groupTF, creatorTF, numSeasonsTF, numEpisodesTF);
        } else if (m instanceof Game) {
            developerTF.setText(((Game) m).getDeveloper());
            consoleTF.setText(((Game) m).getConsole());
            numPlayersTF.setText(((Game) m).getNumPlayers() + "");
            subGrid.addColumn(0, groupText, developer, console, numPlayers);
            subGrid.addColumn(1, groupTF, developerTF, consoleTF, numPlayersTF);
        } else if (m instanceof Music) {
            artistTF.setText(((Music) m).getArtist());
            subGrid.addColumn(0, groupText, artist);
            subGrid.addColumn(1, groupTF, artistTF);
        } else if (m instanceof Book) {
            authorTF.setText(((Book) m).getAuthor());
            subGrid.addColumn(0, groupText, author);
            subGrid.addColumn(1, groupTF, authorTF);
        }

        // Buttons
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);
        Button btSave = new Button("Save");
        btSave.setPrefWidth(70);
        Button btCancel = new Button("Cancel");
        btCancel.setPrefWidth(70);
        buttons.getChildren().addAll(btSave, btCancel);

        btCancel.setOnAction(e -> stage.close());

        btSave.setOnAction(e -> {
            try {
                if (nameTF.getText().isBlank()) {
                    errorText.setText("You must enter a " + m.getClass().getSimpleName() + " name");
                    scroll.setVvalue(0);
                } else if (!ratingTF.getText().isBlank() && !Media.validateRating(Double.parseDouble(ratingTF.getText()))) {
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
                            String dir = directorTF.getText();
                            int d = durationTF.getText().isBlank() ? 0 : Integer.parseInt(durationTF.getText());

                            Media oldMovie = (Movie) m.clone(); // get original state of media
                            String groupOld = library.getGroup(m);

                            if (gr == null) { // remove from group if group text field is left empty
                                library.removeFromGroup(m);
                            }

                            Movie newMovie = (Movie) m.clone();
                            newMovie.setName(n);
                            newMovie.setGenre(g);
                            newMovie.setDescription(desc);
                            newMovie.setFormat(f);
                            newMovie.setYear(y);
                            newMovie.setYearConsumed(yc);
                            newMovie.setRating(r);
                            newMovie.setColorArray(c);
                            newMovie.setDirector(dir);
                            newMovie.setDuration(d);

                            library.remove(m.getName(), m); // remove old version of entry
                            library.add(n, newMovie); // add new version of entry
                            library.addToGroup(newMovie, gr);

                            pushAction(new Edit(library, oldMovie, (Movie) newMovie.clone(), groupOld, gr));

                            draw();
                            stage.close();
                            library.write();
                            showViewScreen(newMovie);
                        }
                    } else if (m instanceof Show) {
                        if (!numEpisodesTF.getText().isBlank() && Integer.parseInt(numEpisodesTF.getText()) < 0) {
                            errorText.setText("Number of episodes cannot be less than 0");
                            scroll.setVvalue(0);
                        } else if(!numSeasonsTF.getText().isBlank() && Integer.parseInt(numSeasonsTF.getText()) < 0) {
                            errorText.setText("Number of seasons cannot be less than 0");
                            scroll.setVvalue(0);
                        } else {
                            String cr = creatorTF.getText();
                            int ns = numSeasonsTF.getText().isBlank() ? 0 : Integer.parseInt(numSeasonsTF.getText());
                            int ne = numEpisodesTF.getText().isBlank() ? 0 : Integer.parseInt(numEpisodesTF.getText());

                            Media oldShow = (Show) m.clone(); // get original state of media
                            String groupOld = library.getGroup(m);

                            if (gr == null) {
                                library.removeFromGroup(m);
                            }

                            Show newShow = (Show) m.clone();
                            newShow.setName(n);
                            newShow.setGenre(g);
                            newShow.setDescription(desc);
                            newShow.setFormat(f);
                            newShow.setYear(y);
                            newShow.setYearConsumed(yc);
                            newShow.setRating(r);
                            newShow.setColorArray(c);
                            newShow.setCreator(cr);
                            newShow.setNumSeasons(ns);
                            newShow.setNumEpisodes(ne);

                            library.remove(m.getName(), m); // remove old version of entry
                            library.add(n, newShow); // add new version of entry
                            library.addToGroup(newShow, gr);

                            pushAction(new Edit(library, oldShow, (Show) newShow.clone(), groupOld, gr));

                            draw();
                            stage.close();
                            library.write();
                            showViewScreen(newShow);
                        }
                    } else if (m instanceof Game) {
                        if (!numPlayersTF.getText().isBlank() && Integer.parseInt(numPlayersTF.getText()) < 0) {
                            errorText.setText("Number of players cannot be less than 0");
                            scroll.setVvalue(0);
                        } else {
                            String dev = developerTF.getText();
                            String con = consoleTF.getText();
                            int np = numPlayersTF.getText().isBlank() ? 0 : Integer.parseInt(numPlayersTF.getText());

                            Media oldGame = (Game) m.clone(); // get original state of media
                            String groupOld = library.getGroup(m);

                            if (gr == null) {
                                library.removeFromGroup(m);
                            }

                            Game newGame = (Game) m.clone();
                            newGame.setName(n);
                            newGame.setGenre(g);
                            newGame.setDescription(desc);
                            newGame.setFormat(f);
                            newGame.setYear(y);
                            newGame.setYearConsumed(yc);
                            newGame.setRating(r);
                            newGame.setColorArray(c);
                            newGame.setDeveloper(dev);
                            newGame.setConsole(con);
                            newGame.setNumPlayers(np);

                            library.remove(m.getName(), m); // remove old version of entry
                            library.add(n, newGame); // add new version of entry
                            library.addToGroup(newGame, gr);

                            pushAction(new Edit(library, oldGame, (Game) newGame.clone(), groupOld, gr));

                            draw();
                            stage.close();
                            library.write();
                            showViewScreen(newGame);
                        }
                    } else if (m instanceof Music) {
                        String art = artistTF.getText();

                        Media oldMusic = (Music) m.clone(); // get original state of media
                        String groupOld = library.getGroup(m);

                        if (gr == null) {
                            library.removeFromGroup(m);
                        }

                        Music newMusic = (Music) m.clone();
                        newMusic.setName(n);
                        newMusic.setGenre(g);
                        newMusic.setDescription(desc);
                        newMusic.setFormat(f);
                        newMusic.setYear(y);
                        newMusic.setYearConsumed(yc);
                        newMusic.setRating(r);
                        newMusic.setColorArray(c);
                        newMusic.setArtist(art);

                        library.remove(m.getName(), m); // remove old version of entry
                        library.add(n, newMusic); // add new version of entry
                        library.addToGroup(newMusic, gr);

                        pushAction(new Edit(library, oldMusic, (Music) newMusic.clone(), groupOld, gr));

                        draw();
                        stage.close();
                        library.write();
                        showViewScreen(newMusic);
                    } else if (m instanceof Book) {
                        String auth = authorTF.getText();

                        Media oldBook = (Book) m.clone(); // get original state of media
                        String groupOld = library.getGroup(m);

                        if (gr == null) {
                            library.removeFromGroup(m);
                        }

                        Book newBook = (Book) m.clone();
                        newBook.setName(n);
                        newBook.setGenre(g);
                        newBook.setDescription(desc);
                        newBook.setFormat(f);
                        newBook.setYear(y);
                        newBook.setYearConsumed(yc);
                        newBook.setRating(r);
                        newBook.setColorArray(c);
                        newBook.setAuthor(auth);

                        library.remove(m.getName(), m); // remove old version of entry
                        library.add(n, newBook); // add new version of entry
                        library.addToGroup(newBook, gr);

                        pushAction(new Edit(library, oldBook, (Book) newBook.clone(), groupOld, gr));

                        draw();
                        stage.close();
                        library.write();
                        showViewScreen(newBook);
                    }
                }
            } catch (Exception ex) {
                errorText.setText("Invalid input");
                scroll.setVvalue(0);
            }
        });

        vb.getChildren().addAll(errorText, mainGrid, line, subGrid, buttons);

        Rectangle rect = new Rectangle(50, 0, 125, 450);

        pane.getChildren().addAll(rect, vb);

        Scene scene = new Scene(scroll, 800, 450);
        scene.getStylesheets().add(MediaLibrary.css);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Edit " + m.getClass().getSimpleName() + ": " + m.getName());
        stage.show();

        // Sidebar positioned and filled after showing stage
        rect.setHeight(Math.max(vb.getHeight(), 450));
        rect.setFill(new LinearGradient(0, 0,0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, m.getColor()), new Stop(1, m.getColor().darker())));
    }

    private void showSimilarMedia(Media m) {
        Stage stage = new Stage();
        BubbleDiagramPane similar = new BubbleDiagramPane(library, m);

        Button btOK = new Button("OK");
        btOK.setPrefWidth(70);
        Button btBack = new Button("Back");
        btBack.setPrefWidth(70);
        HBox buttons = new HBox(10);
        buttons.setPadding(new Insets(10));
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll(btOK, btBack);

        BorderPane bp = new BorderPane();
        bp.setCenter(similar);
        bp.setBottom(buttons);

        btOK.setOnAction(e -> stage.close());
        btBack.setOnAction(e -> {
            stage.close();
            showViewScreen(m);
        });

        Scene scene = new Scene(bp, 800, 800);
        scene.getStylesheets().add(MediaLibrary.css);

        stage.setTitle("Similar Media: " + m.getName());
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    /** Returns a shortened String of a Text object */
    public static String shortenText(Text t, int pixels) {
        String name = t.getText();
        double length = calculateTextLength(t);

        if (pixels < length) {
            // Calculate the percentage (between 0 and 1) of the name that can fit, multiply by
            // the length of the string to find the maximum number of characters that fit
            int limit = (int) Math.floor((pixels / length) * name.length());
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

    /** Calculates length of a text object */
    public static double calculateTextLength(Text t) {
        if (t == null)
            return 0;
        return t.getBoundsInLocal().getWidth();
    }

    /** Calculates the height of a text object */
    public static double calculateTextHeight(Text t) {
        if (t == null)
            return 0;
        return t.getBoundsInLocal().getHeight();
    }

    // ----- COMMAND PATTERN IMPLEMENTATION ----- //
    private interface Command {
        void execute(); // perform an action
        void unExecute(); // undo an action
    }

    private static class Add implements Command {
        private final Library<Media> library;
        private final Media media;

        public Add(Library<Media> library, Media media) {
            this.library = library;
            this.media = media;
        }

        @Override
        public void execute() {
            library.add(media.getName(), media);
        }

        @Override
        public void unExecute() {
            library.remove(media.getName(), media);
        }
    }

    private static class Remove implements Command {
        private final Library<Media> library;
        private final Media media;
        private final String group;

        public Remove(Library<Media> library, Media media) {
            this.library = library;
            this.media = media;
            this.group = library.getGroup(media);
        }

        @Override
        public void execute() {
            library.remove(media.getName(), media);
        }

        @Override
        public void unExecute() {
            library.add(media.getName(), media);
            if (group != null) {
                library.addToGroup(media, group);
            }
        }
    }

    private static class Edit implements Command {
        private final Library<Media> library;
        private final Media mediaOld;
        private final Media mediaNew;
        private final String groupOld;
        private final String groupNew;

        public Edit(Library<Media> library, Media mediaOld, Media mediaNew, String groupOld, String groupNew) {
            this.library = library;
            this.mediaOld = mediaOld;
            this.mediaNew = mediaNew;
            this.groupOld = groupOld;
            this.groupNew = groupNew;
        }

        @Override
        public void execute() {
            library.remove(mediaOld.getName(), mediaOld);
            library.add(mediaNew.getName(), mediaNew);
            if (groupNew != null) {
                library.addToGroup(mediaNew, groupNew);
            }
        }

        @Override
        public void unExecute() {
            library.remove(mediaNew.getName(), mediaNew);
            library.add(mediaOld.getName(), mediaOld);
            if (groupOld != null) {
                library.addToGroup(mediaOld, groupOld);
            }
        }
    }
}
