// **********************************************************************************
// Title: Media Library
// Author: Matthew Smith
// Course Section: CMIS202-ONL1 (Seidel) Spring 2023
// File: MediaLibrary.java
// Description: Main class for Media Library. Handles user input and graphical output
// **********************************************************************************

package com.matthewsmith.medialibrary;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.time.Year;
import java.util.Comparator;

public class MediaLibrary extends Application {
    private static ScrollPane libScroll;
    private static Text sizeText;
    private static Text titleText;

    /** Creates and displays JavaFX GUI */
    @Override
    public void start(Stage primaryStage) {
        // ----- MAIN LIBRARY STAGE ----- //

        Pane top = new Pane();
        top.setPadding(new Insets(7, 0, 0, 7));

        Button btHome = new Button("Home");
        btHome.setPrefWidth(50);
        btHome.setLayoutX(20);
        btHome.setLayoutY(5);

        Button btUndo = new Button("Undo");
        btUndo.setPrefWidth(50);
        btUndo.setLayoutX(100);
        btUndo.setLayoutY(5);

        Button btRedo = new Button("Redo");
        btRedo.setPrefWidth(50);
        btRedo.setLayoutX(155);
        btRedo.setLayoutY(5);
        
        Button btAdd = new Button("+");
        btAdd.setPrefWidth(25);
        btAdd.setLayoutX(730);
        btAdd.setLayoutY(5);
        
        Button btOther = new Button("...");
        btOther.setPrefWidth(30);
        btOther.setLayoutX(760);
        btOther.setLayoutY(5);
        
        titleText = new Text(350, 25, "My Media Library");

        ComboBox<String> cboSort = new ComboBox<>();
        cboSort.setLayoutX(545);
        cboSort.setLayoutY(5);

        String[] sortOptions = {"Sort by Name", "Sort by Genre", "Sort by Format", "Sort by Rating",
                "Sort by Color", "Sort by Year", "Sort by Year Consumed", "Sort by Date Added"};
        cboSort.getItems().addAll(sortOptions);
        cboSort.setValue("Sort by Date Added");
        top.getChildren().addAll(btHome, btUndo, btRedo, btAdd, btOther, titleText, cboSort);

        Library lib = new Library();
        libScroll = new ScrollPane(lib);
        lib.read(); // Read library from file
        lib.sort(Comparator.comparing(Media::getDateAdded).reversed()); // Default sort: sort by date

        HBox bottom = new HBox(15);
        bottom.setPadding(new Insets(10, 10, 10, 10));
        sizeText = new Text("Library size: " + lib.getSize());
        bottom.getChildren().addAll(sizeText);

        BorderPane bp = new BorderPane();
        bp.setTop(top);
        bp.setCenter(libScroll);
        bp.setBottom(bottom);

        Scene scene = new Scene(bp, 800, 550);
        primaryStage.setTitle("Media Library");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        btUndo.setOnAction(e -> {
            setTitle("My Media Library");
            lib.undo();
        });
        btRedo.setOnAction(e -> {
            setTitle("My Media Library");
            lib.redo();
        });
        btHome.setOnAction(e -> {
            setTitle("My Media Library");
            lib.draw();
        });
        cboSort.setOnAction(e -> sortLibrary(lib, cboSort.getValue()));

        // ----- INITIAL "ADD" STAGE ----- //

        HBox addBoxTop = new HBox(15);
        addBoxTop.setPadding(new Insets(15, 15, 15, 15));
        addBoxTop.setAlignment(Pos.CENTER);
        Text addText = new Text("Select a type of media to add: ");
        String[] addOptions = {"Movie", "Show", "Game", "Music", "Book"};
        ComboBox<String> cboAdd = new ComboBox<>();
        cboAdd.getItems().addAll(addOptions);
        cboAdd.setValue("Movie");
        addBoxTop.getChildren().addAll(addText, cboAdd);

        HBox addBoxBottom = new HBox(15);
        addBoxBottom.setPadding(new Insets(15, 15, 15, 15));
        addBoxBottom.setAlignment(Pos.CENTER);
        Button btInitialAdd = new Button("OK");
        Button btInitialCancel = new Button("Cancel");
        addBoxBottom.getChildren().addAll(btInitialAdd, btInitialCancel);

        VBox addBox = new VBox(30);
        addBox.setAlignment(Pos.CENTER);
        addBox.getChildren().addAll(addBoxTop, addBoxBottom);

        Stage addStage = new Stage();
        Scene addScene = new Scene(addBox, 300, 150);
        addStage.setResizable(false);
        addStage.setTitle("Add New Media");
        addStage.setScene(addScene);

        btAdd.setOnAction(e -> addStage.show());
        btInitialCancel.setOnAction(e -> addStage.hide());

        // ----- MOVIE "ADD" STAGE ----- //

        GridPane movieAddGrid = new GridPane();
        movieAddGrid.setPadding(new Insets(15, 15, 15, 15));
        movieAddGrid.setHgap(15);
        movieAddGrid.setVgap(15);
        movieAddGrid.setAlignment(Pos.TOP_CENTER);

        Text name = new Text("Name: ");
        TextField nameTF = new TextField();

        Text genre = new Text("Genre: ");
        TextField genreTF = new TextField();

        Text description = new Text("Description: ");
        TextArea descriptionTA = new TextArea();
        descriptionTA.setPrefRowCount(3);
        descriptionTA.setPrefColumnCount(20);
        descriptionTA.setWrapText(true);

        Text format = new Text("Format: ");
        TextField formatTF = new TextField();

        Text year = new Text("Year released: ");
        TextField yearTF = new TextField();

        Text yearConsumed = new Text("Year consumed: ");
        TextField yearConsumedTF = new TextField();

        Text rating = new Text("Your rating out of 10: ");
        TextField ratingTF = new TextField();

        Text color = new Text("Color: ");
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setPrefHeight(30);

        Text director = new Text("Director: ");
        TextField directorTF = new TextField();

        Text duration = new Text("Duration in minutes: ");
        TextField durationTF = new TextField();

        Button btAddMovie = new Button("Add");
        Button btCancelMovie = new Button("Cancel");
        HBox addMovieButtons = new HBox(30);
        addMovieButtons.getChildren().addAll(btAddMovie, btCancelMovie);
        addMovieButtons.setAlignment(Pos.CENTER);
        addMovieButtons.setPadding(new Insets(15, 15, 30, 15));
        HBox addMovieError = new HBox(15);
        addMovieError.setAlignment(Pos.CENTER);
        Text movieError = new Text("");
        movieError.setFill(Color.RED);
        addMovieError.getChildren().add(movieError);
        VBox movieAddBottom = new VBox(15);
        movieAddBottom.getChildren().addAll(addMovieError, addMovieButtons);

        BorderPane movieAddBox = new BorderPane();
        movieAddBox.setCenter(movieAddGrid);
        movieAddBox.setBottom(movieAddBottom);
        HBox movieAddTop = new HBox(15);
        movieAddTop.setAlignment(Pos.CENTER);
        movieAddTop.setPadding(new Insets(15, 15, 15, 25));
        Text movieAddText = new Text("Add New Movie");
        movieAddTop.getChildren().add(movieAddText);
        movieAddBox.setTop(movieAddTop);

        Stage movieAddStage = new Stage();
        Scene movieAddScene = new Scene(movieAddBox, 400, 630);
        movieAddStage.setTitle("Add New Movie");
        movieAddStage.setResizable(false);
        movieAddStage.setScene(movieAddScene);
        
        // ----- SHOW "ADD" STAGE ----- //

        GridPane showAddGrid = new GridPane();
        showAddGrid.setPadding(new Insets(15, 15, 15, 15));
        showAddGrid.setHgap(15);
        showAddGrid.setVgap(15);
        showAddGrid.setAlignment(Pos.TOP_CENTER);

        Text creator = new Text("Creator: ");
        TextField creatorTF = new TextField();

        Text numSeasons = new Text("Number of seasons: ");
        TextField numSeasonsTF = new TextField();

        Text numEpisodes = new Text("Number of episodes: ");
        TextField numEpisodesTF = new TextField();

        Button btAddShow = new Button("Add");
        Button btCancelShow = new Button("Cancel");
        HBox addShowButtons = new HBox(30);
        addShowButtons.getChildren().addAll(btAddShow, btCancelShow);
        addShowButtons.setAlignment(Pos.CENTER);
        addShowButtons.setPadding(new Insets(15, 15, 30, 15));
        HBox addShowError = new HBox(15);
        addShowError.setAlignment(Pos.CENTER);
        Text showError = new Text("");
        showError.setFill(Color.RED);
        addShowError.getChildren().add(showError);
        VBox showAddBottom = new VBox(15);
        showAddBottom.getChildren().addAll(addShowError, addShowButtons);

        BorderPane showAddBox = new BorderPane();
        showAddBox.setCenter(showAddGrid);
        showAddBox.setBottom(showAddBottom);
        HBox showAddTop = new HBox(15);
        showAddTop.setAlignment(Pos.CENTER);
        showAddTop.setPadding(new Insets(15, 15, 15, 25));
        Text showAddText = new Text("Add New show");
        showAddTop.getChildren().add(showAddText);
        showAddBox.setTop(showAddTop);

        Stage showAddStage = new Stage();
        Scene showAddScene = new Scene(showAddBox, 400, 650);
        showAddStage.setTitle("Add New show");
        showAddStage.setResizable(false);
        showAddStage.setScene(showAddScene);

        // ----- GAME "ADD" STAGE ----- //

        GridPane gameAddGrid = new GridPane();
        gameAddGrid.setPadding(new Insets(15, 15, 15, 15));
        gameAddGrid.setHgap(15);
        gameAddGrid.setVgap(15);
        gameAddGrid.setAlignment(Pos.TOP_CENTER);

        Button btAddGame = new Button("Add");
        Button btCancelGame = new Button("Cancel");
        HBox addGameButtons = new HBox(30);
        addGameButtons.getChildren().addAll(btAddGame, btCancelGame);
        addGameButtons.setAlignment(Pos.CENTER);
        addGameButtons.setPadding(new Insets(15, 15, 30, 15));
        HBox addGameError = new HBox(15);
        addGameError.setAlignment(Pos.CENTER);
        Text gameError = new Text("");
        gameError.setFill(Color.RED);
        addGameError.getChildren().add(gameError);
        VBox gameAddBottom = new VBox(15);
        gameAddBottom.getChildren().addAll(addGameError, addGameButtons);

        Text developer = new Text("Developer: ");
        TextField developerTF = new TextField();

        Text console = new Text("Console: ");
        TextField consoleTF = new TextField();

        Text numPlayers = new Text("Number of players: ");
        TextField numPlayersTF = new TextField();

        BorderPane gameAddBox = new BorderPane();
        gameAddBox.setCenter(gameAddGrid);
        gameAddBox.setBottom(addGameButtons);
        HBox gameAddTop = new HBox(15);
        gameAddTop.setAlignment(Pos.CENTER);
        gameAddTop.setPadding(new Insets(15, 15, 15, 25));
        Text gameAddText = new Text("Add New Game");
        gameAddTop.getChildren().add(gameAddText);
        gameAddBox.setTop(gameAddTop);

        Stage gameAddStage = new Stage();
        Scene gameAddScene = new Scene(gameAddBox, 400, 650);
        gameAddStage.setTitle("Add New Game");
        gameAddStage.setResizable(false);
        gameAddStage.setScene(gameAddScene);


        // ----- MUSIC "ADD" STAGE ----- //

        GridPane musicAddGrid = new GridPane();
        musicAddGrid.setPadding(new Insets(15, 15, 15, 15));
        musicAddGrid.setHgap(15);
        musicAddGrid.setVgap(15);
        musicAddGrid.setAlignment(Pos.TOP_CENTER);

        Text artist = new Text("Artist: ");
        TextField artistTF = new TextField();

        Button btAddMusic = new Button("Add");
        Button btCancelMusic = new Button("Cancel");
        HBox addMusicButtons = new HBox(30);
        addMusicButtons.getChildren().addAll(btAddMusic, btCancelMusic);
        addMusicButtons.setAlignment(Pos.CENTER);
        addMusicButtons.setPadding(new Insets(15, 15, 30, 15));
        HBox addMusicError = new HBox(15);
        addMusicError.setAlignment(Pos.CENTER);
        Text musicError = new Text("");
        musicError.setFill(Color.RED);
        addMusicError.getChildren().add(musicError);
        VBox musicAddBottom = new VBox(15);
        musicAddBottom.getChildren().addAll(addMusicError, addMusicButtons);

        BorderPane musicAddBox = new BorderPane();
        musicAddBox.setCenter(musicAddGrid);
        musicAddBox.setBottom(addMusicButtons);
        HBox musicAddTop = new HBox(15);
        musicAddTop.setAlignment(Pos.CENTER);
        musicAddTop.setPadding(new Insets(15, 15, 15, 25));
        Text musicAddText = new Text("Add New Music");
        musicAddTop.getChildren().add(musicAddText);
        musicAddBox.setTop(musicAddTop);

        Stage musicAddStage = new Stage();
        Scene musicAddScene = new Scene(musicAddBox, 400, 600);
        musicAddStage.setTitle("Add New Music");
        musicAddStage.setResizable(false);
        musicAddStage.setScene(musicAddScene);

        // ----- BOOK "ADD" STAGE ----- //

        GridPane bookAddGrid = new GridPane();
        bookAddGrid.setPadding(new Insets(15, 15, 15, 15));
        bookAddGrid.setHgap(15);
        bookAddGrid.setVgap(15);
        bookAddGrid.setAlignment(Pos.TOP_CENTER);

        Text author = new Text("Author: ");
        TextField authorTF = new TextField();

        Button btAddBook = new Button("Add");
        Button btCancelBook = new Button("Cancel");
        HBox addBookButtons = new HBox(30);
        addBookButtons.getChildren().addAll(btAddBook, btCancelBook);
        addBookButtons.setAlignment(Pos.CENTER);
        addBookButtons.setPadding(new Insets(15, 15, 30, 15));
        HBox addBookError = new HBox(15);
        addBookError.setAlignment(Pos.CENTER);
        Text bookError = new Text("");
        bookError.setFill(Color.RED);
        addBookError.getChildren().add(bookError);
        VBox bookAddBottom = new VBox(15);
        bookAddBottom.getChildren().addAll(addBookError, addBookButtons);

        BorderPane bookAddBox = new BorderPane();
        bookAddBox.setCenter(bookAddGrid);
        bookAddBox.setBottom(addBookButtons);
        HBox bookAddTop = new HBox(15);
        bookAddTop.setAlignment(Pos.CENTER);
        bookAddTop.setPadding(new Insets(15, 15, 15, 25));
        Text bookAddText = new Text("Add New Book");
        bookAddTop.getChildren().add(bookAddText);
        bookAddBox.setTop(bookAddTop);

        Stage bookAddStage = new Stage();
        Scene bookAddScene = new Scene(bookAddBox, 400, 600);
        bookAddStage.setTitle("Add New Book");
        bookAddStage.setResizable(false);
        bookAddStage.setScene(bookAddScene);
        

        btInitialAdd.setOnAction(e -> {
            addStage.hide();

            String type = cboAdd.getValue();
            switch (type) {
                case "Movie":
                    movieAddGrid.getChildren().clear();
                    movieAddGrid.addColumn(0, name, genre, director, duration, description, format, year,
                            yearConsumed, rating, color);
                    movieAddGrid.addColumn(1, nameTF, genreTF, directorTF, durationTF, descriptionTA, formatTF, yearTF,
                            yearConsumedTF, ratingTF, colorPicker);
                    movieAddStage.show();
                    break;
                case "Show":
                    showAddGrid.getChildren().clear();
                    showAddGrid.addColumn(0, name, genre, creator, numSeasons, numEpisodes, description, format, year,
                            yearConsumed, rating, color);
                    showAddGrid.addColumn(1, nameTF, genreTF, creatorTF, numSeasonsTF, numEpisodesTF, descriptionTA, formatTF, yearTF,
                            yearConsumedTF, ratingTF, colorPicker);
                    showAddStage.show();
                    break;
                case "Game":
                    gameAddGrid.getChildren().clear();
                    gameAddGrid.addColumn(0, name, genre, developer, console, numPlayers, description, format, year,
                            yearConsumed, rating, color);
                    gameAddGrid.addColumn(1, nameTF, genreTF, developerTF, consoleTF, numPlayersTF, descriptionTA, formatTF, yearTF,
                            yearConsumedTF, ratingTF, colorPicker);
                    gameAddStage.show();
                    break;
                case "Music":
                    musicAddGrid.getChildren().clear();
                    musicAddGrid.addColumn(0, name, genre, artist, description, format, year,
                            yearConsumed, rating, color);
                    musicAddGrid.addColumn(1, nameTF, genreTF, artistTF, descriptionTA, formatTF, yearTF,
                            yearConsumedTF, ratingTF, colorPicker);
                    musicAddStage.show();
                    break;
                case "Book":
                    bookAddGrid.getChildren().clear();
                    bookAddGrid.addColumn(0, name, genre, author, description, format, year,
                            yearConsumed, rating, color);
                    bookAddGrid.addColumn(1, nameTF, genreTF, authorTF, descriptionTA, formatTF, yearTF,
                            yearConsumedTF, ratingTF, colorPicker);
                    bookAddStage.show();
                    break;
            }
        });

        btAddMovie.setOnAction(e -> {
            try {
                if (nameTF.getText().isBlank()) {
                    movieError.setText("You must enter a movie name");
                } else if (!ratingTF.getText().isBlank() && Media.validateRating(Double.parseDouble(ratingTF.getText()))) {
                    movieError.setText("Rating must be between 0 and 10");
                } else if (!durationTF.getText().isBlank() && Integer.parseInt(durationTF.getText()) < 0) {
                    movieError.setText("Duration cannot be less than 0");
                } else { // Successful case
                    String n = nameTF.getText();
                    String g = genreTF.getText();
                    String desc = descriptionTA.getText();
                    String f = formatTF.getText();
                    int y = yearTF.getText().isBlank() ? Year.now().getValue() : Integer.parseInt(yearTF.getText());
                    int yc = yearConsumedTF.getText().isBlank() ? Year.now().getValue() : Integer.parseInt(yearConsumedTF.getText());
                    double r = ratingTF.getText().isBlank() ? 0 : Double.parseDouble(ratingTF.getText());
                    double[] c = {colorPicker.getValue().getRed(), colorPicker.getValue().getGreen(), colorPicker.getValue().getBlue(), colorPicker.getValue().getOpacity()};
                    String dir = directorTF.getText();
                    int d = durationTF.getText().isBlank() ? 0 : Integer.parseInt(durationTF.getText());

                    lib.add(new Movie(n, g, desc, f, y, yc, r, c, dir, d));
                    sortLibrary(lib, cboSort.getValue());
                    movieAddStage.hide();
                }
            } catch (Exception ex) {
                movieError.setText("Invalid input");
            }
        });

        btAddShow.setOnAction(e -> {
            try {
                if (nameTF.getText().isBlank()) {
                    showError.setText("You must enter a show name");
                } else if (!ratingTF.getText().isBlank() && Media.validateRating(Double.parseDouble(ratingTF.getText()))) {
                    showError.setText("Rating must be between 0 and 10");
                } else if (!numSeasonsTF.getText().isBlank() && Integer.parseInt(numSeasonsTF.getText()) < 0) {
                    showError.setText("Number of seasons cannot be less than 0");
                } else if (!numEpisodesTF.getText().isBlank() && Integer.parseInt(numEpisodesTF.getText()) < 0) {
                    showError.setText("Number of episodes cannot be less than 0");
                } else { // Successful case
                    String n = nameTF.getText();
                    String g = genreTF.getText();
                    String desc = descriptionTA.getText();
                    String f = formatTF.getText();
                    int y = yearTF.getText().isBlank() ? Year.now().getValue() : Integer.parseInt(yearTF.getText());
                    int yc = yearConsumedTF.getText().isBlank() ? Year.now().getValue() : Integer.parseInt(yearConsumedTF.getText());
                    double r = ratingTF.getText().isBlank() ? 0 : Double.parseDouble(ratingTF.getText());
                    double[] c = {colorPicker.getValue().getRed(), colorPicker.getValue().getGreen(), colorPicker.getValue().getBlue(), colorPicker.getValue().getOpacity()};
                    String cr = creatorTF.getText();
                    int ns = numSeasonsTF.getText().isBlank() ? 0 : Integer.parseInt(numSeasonsTF.getText());
                    int ne = numEpisodesTF.getText().isBlank() ? 0 : Integer.parseInt(numEpisodesTF.getText());

                    lib.add(new Show(n, g, desc, f, y, yc, r, c, cr, ns, ne));
                    sortLibrary(lib, cboSort.getValue());
                    showAddStage.hide();
                }
            } catch (Exception ex) {
                showError.setText("Invalid input");
            }
        });

        btAddGame.setOnAction(e -> {
            try {
                if (nameTF.getText().isBlank()) {
                    gameError.setText("You must enter a game name");
                } else if (!ratingTF.getText().isBlank() && Media.validateRating(Double.parseDouble(ratingTF.getText()))) {
                    gameError.setText("Rating must be between 0 and 10");
                } else if (!numPlayersTF.getText().isBlank() && Integer.parseInt(numPlayersTF.getText()) < 0) {
                    gameError.setText("Number of players cannot be less than 0");
                } else { // Successful case
                    String n = nameTF.getText();
                    String g = genreTF.getText();
                    String desc = descriptionTA.getText();
                    String f = formatTF.getText();
                    int y = yearTF.getText().isBlank() ? Year.now().getValue() : Integer.parseInt(yearTF.getText());
                    int yc = yearConsumedTF.getText().isBlank() ? Year.now().getValue() : Integer.parseInt(yearConsumedTF.getText());
                    double r = ratingTF.getText().isBlank() ? 0 : Double.parseDouble(ratingTF.getText());
                    double[] c = {colorPicker.getValue().getRed(), colorPicker.getValue().getGreen(), colorPicker.getValue().getBlue(), colorPicker.getValue().getOpacity()};
                    String dev = developerTF.getText();
                    String con = consoleTF.getText();
                    int np = numPlayersTF.getText().isBlank() ? 0 : Integer.parseInt(numPlayersTF.getText());

                    lib.add(new Game(n, g, desc, f, y, yc, r, c, dev, con, np));
                    sortLibrary(lib, cboSort.getValue());
                    gameAddStage.hide();
                }
            } catch (Exception ex) {
                gameError.setText("Invalid input");
            }
        });

        btAddMusic.setOnAction(e -> {
            try {
                if (nameTF.getText().isBlank()) {
                    musicError.setText("You must enter a music name");
                } else if (!ratingTF.getText().isBlank() && Media.validateRating(Double.parseDouble(ratingTF.getText()))) {
                    musicError.setText("Rating must be between 0 and 10");
                } else { // Successful case
                    String n = nameTF.getText();
                    String g = genreTF.getText();
                    String desc = descriptionTA.getText();
                    String f = formatTF.getText();
                    int y = yearTF.getText().isBlank() ? Year.now().getValue() : Integer.parseInt(yearTF.getText());
                    int yc = yearConsumedTF.getText().isBlank() ? Year.now().getValue() : Integer.parseInt(yearConsumedTF.getText());
                    double r = ratingTF.getText().isBlank() ? 0 : Double.parseDouble(ratingTF.getText());
                    double[] c = {colorPicker.getValue().getRed(), colorPicker.getValue().getGreen(), colorPicker.getValue().getBlue(), colorPicker.getValue().getOpacity()};
                    String art = artistTF.getText();

                    lib.add(new Music(n, g, desc, f, y, yc, r, c, art));
                    sortLibrary(lib, cboSort.getValue());
                    musicAddStage.hide();
                }
            } catch (Exception ex) {
                musicError.setText("Invalid input");
            }
        });

        btAddBook.setOnAction(e -> {
            try {
                if (nameTF.getText().isBlank()) {
                    bookError.setText("You must enter a book name");
                } else if (!ratingTF.getText().isBlank() && Media.validateRating(Double.parseDouble(ratingTF.getText()))) {
                    bookError.setText("Rating must be between 0 and 10");
                } else { // Successful case
                    String n = nameTF.getText();
                    String g = genreTF.getText();
                    String desc = descriptionTA.getText();
                    String f = formatTF.getText();
                    int y = yearTF.getText().isBlank() ? Year.now().getValue() : Integer.parseInt(yearTF.getText());
                    int yc = yearConsumedTF.getText().isBlank() ? Year.now().getValue() : Integer.parseInt(yearConsumedTF.getText());
                    double r = ratingTF.getText().isBlank() ? 0 : Double.parseDouble(ratingTF.getText());
                    double[] c = {colorPicker.getValue().getRed(), colorPicker.getValue().getGreen(), colorPicker.getValue().getBlue(), colorPicker.getValue().getOpacity()};
                    String auth = authorTF.getText();

                    lib.add(new Book(n, g, desc, f, y, yc, r, c, auth));
                    sortLibrary(lib, cboSort.getValue());
                    bookAddStage.hide();
                }
            } catch (Exception ex) {
                bookError.setText("Invalid input");
            }
        });

        movieAddStage.setOnHidden(e -> {
            movieError.setText("");
            clearFields(nameTF, genreTF, descriptionTA, formatTF, yearTF, yearConsumedTF, ratingTF, directorTF, durationTF);
            colorPicker.setValue(Color.WHITE);
        });

        showAddStage.setOnHidden(e -> {
            showError.setText("");
            clearFields(nameTF, genreTF, descriptionTA, formatTF, yearTF, yearConsumedTF, ratingTF, creatorTF, numSeasonsTF, numEpisodesTF);
            colorPicker.setValue(Color.WHITE);
        });

        gameAddStage.setOnHidden(e -> {
            gameError.setText("");
            clearFields(nameTF, genreTF, descriptionTA, formatTF, yearTF, yearConsumedTF, ratingTF, developerTF, consoleTF, numPlayersTF);
            colorPicker.setValue(Color.WHITE);
        });

        musicAddStage.setOnHidden(e -> {
            musicError.setText("");
            clearFields(nameTF, genreTF, descriptionTA, formatTF, yearTF, yearConsumedTF, ratingTF, artistTF);
            colorPicker.setValue(Color.WHITE);
        });

        bookAddStage.setOnHidden(e -> {
            bookError.setText("");
            clearFields(nameTF, genreTF, descriptionTA, formatTF, yearTF, yearConsumedTF, ratingTF, authorTF);
            colorPicker.setValue(Color.WHITE);
        });

        btCancelMovie.setOnAction(e -> movieAddStage.hide());
        btCancelShow.setOnAction(e -> showAddStage.hide());
        btCancelGame.setOnAction(e -> gameAddStage.hide());
        btCancelMusic.setOnAction(e -> musicAddStage.hide());
        btCancelBook.setOnAction(e -> bookAddStage.hide());

        // ----- "OTHER" STAGE ----- //

        HBox otherHB = new HBox();
        otherHB.setPadding(new Insets(10, 10, 10, 10));
        otherHB.setAlignment(Pos.CENTER);
        Text otherText = new Text("More Options");
        otherText.setTextAlignment(TextAlignment.CENTER);
        otherHB.getChildren().add(otherText);

        Pane otherPane = new Pane();
        Button btGroup = new Button("Show a group");
        btGroup.setPrefWidth(200);
        btGroup.setLayoutX(50);
        btGroup.setLayoutY(20);

        Button btSearch = new Button("Search for an item");
        btSearch.setPrefWidth(200);
        btSearch.setLayoutX(50);
        btSearch.setLayoutY(60);

        otherPane.getChildren().addAll(btGroup, btSearch);

        BorderPane otherBP = new BorderPane();
        otherBP.setTop(otherHB);
        otherBP.setCenter(otherPane);
        Scene otherScene = new Scene(otherBP, 300, 150);
        Stage otherStage = new Stage();
        otherStage.setScene(otherScene);
        otherStage.setResizable(false);
        otherStage.setTitle("More Options");

        btOther.setOnAction(e -> otherStage.show());

        TextField otherGroupSearch = new TextField();
        otherGroupSearch.setPromptText("Type a group...");
        otherGroupSearch.setPrefWidth(130);
        otherGroupSearch.setLayoutX(50);
        otherGroupSearch.setLayoutY(20);

        Button btGroupSearch = new Button("Search");
        btGroupSearch.setPrefWidth(60);
        btGroupSearch.setLayoutX(190);
        btGroupSearch.setLayoutY(20);

        TextField otherGeneralSearch = new TextField();
        otherGeneralSearch.setPromptText("Type a name...");
        otherGeneralSearch.setPrefWidth(130);
        otherGeneralSearch.setLayoutX(50);
        otherGeneralSearch.setLayoutY(60);

        Button btGeneralSearch = new Button("Search");
        btGeneralSearch.setPrefWidth(60);
        btGeneralSearch.setLayoutX(190);
        btGeneralSearch.setLayoutY(60);

        btGroup.setOnAction(e -> {
           otherPane.getChildren().remove(btGroup);
           otherPane.getChildren().addAll(otherGroupSearch, btGroupSearch);
            if (!otherPane.getChildren().contains(btSearch)) {
                otherPane.getChildren().removeAll(otherGeneralSearch, btGeneralSearch);
                otherPane.getChildren().add(btSearch);
            }
        });

        btSearch.setOnAction(e -> {
           otherPane.getChildren().remove(btSearch);
           otherPane.getChildren().addAll(otherGeneralSearch, btGeneralSearch);
           if (!otherPane.getChildren().contains(btGroup)) {
               otherPane.getChildren().removeAll(otherGroupSearch, btGroupSearch);
               otherPane.getChildren().add(btGroup);
           }
        });

        btGroupSearch.setOnAction(e -> {
            setTitle("Group: " + otherGroupSearch.getText());
            otherStage.close();
            lib.drawGroup(otherGroupSearch.getText());
        });

        btGeneralSearch.setOnAction(e -> {
            // general search
        });
    }

    /** Clears all specified text fields and text areas */
    public static void clearFields(TextInputControl... fields) {
        for (TextInputControl f : fields) {
            f.clear();
        }
    }

    public static void sortLibrary(Library lib, String sortBy) {
        switch (sortBy) {
            case "Sort by Name":
                lib.sort((m1, m2) -> (m1.getName().compareToIgnoreCase(m2.getName())));
                break;
            case "Sort by Genre":
                lib.sort((m1, m2) -> (m1.getGenre().compareToIgnoreCase(m2.getGenre())));
                break;
            case "Sort by Format":
                lib.sort((m1, m2) -> (m1.getFormat().compareToIgnoreCase(m2.getFormat())));
                break;
            case "Sort by Rating":
                lib.sort(Comparator.comparing(Media::getRating).reversed());
                break;
            case "Sort by Color":
                lib.sort(Comparator.comparing((Media m) -> (int) m.getColor().getHue())
                        .thenComparing((Media m) -> (int) m.getColor().getSaturation())
                        .thenComparing((Media m) -> (int) m.getColor().getBrightness()));
                break;
            case "Sort by Year":
                lib.sort(Comparator.comparing(Media::getYear));
                break;
            case "Sort by Year Consumed":
                lib.sort(Comparator.comparing(Media::getYearConsumed));
                break;
            default:
                lib.sort(Comparator.comparing(Media::getDateAdded).reversed());
        }
    }

    public static void setScroll(double h, double v) {
        libScroll.setHvalue(h);
        libScroll.setVvalue(v);
    }

    public static void setSize(int size) {
        sizeText.setText("Library size: " + size);
    }

    public static void setTitle(String title) {
        titleText.setText(title);
    }
}
