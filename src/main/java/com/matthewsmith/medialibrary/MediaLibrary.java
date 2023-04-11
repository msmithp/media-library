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
import javafx.scene.text.*;
import javafx.stage.Stage;

import java.time.Year;
import java.util.Comparator;

public class MediaLibrary extends Application {
    private LibraryView view;
    private Library<Media> library;
    private Comparator<Media> sortStyle;
    private static Text sizeText;
    private static Text titleText = new Text(350, 25, "");

    /** Creates and displays JavaFX GUI */
    @Override
    public void start(Stage primaryStage) {
        // ----- MAIN LIBRARY STAGE ----- //

        library = new Library<>();
        library.read(); // Read library from file
        library.sort(Comparator.comparing(Media::getDateAdded).reversed()); // Default sort: sort by date

        view = new LibraryView(library);
        ScrollPane libScroll = new ScrollPane(view);
        view.draw();

        Pane top = new Pane();
        top.setPadding(new Insets(7, 0, 0, 7));

        Button btHome = new Button("Home");
        btHome.setPrefWidth(50);
        btHome.setLayoutX(10);
        btHome.setLayoutY(5);

        Button btUndo = new Button("Undo");
        btUndo.setTooltip(new Tooltip("Undo previous action"));
        btUndo.setPrefWidth(50);
        btUndo.setLayoutX(100);
        btUndo.setLayoutY(5);

        Button btRedo = new Button("Redo");
        btRedo.setTooltip(new Tooltip("Redo undone action"));
        btRedo.setPrefWidth(50);
        btRedo.setLayoutX(155);
        btRedo.setLayoutY(5);
        
        Button btAdd = new Button("+");
        btAdd.setTooltip(new Tooltip("Add new media"));
        btAdd.setPrefWidth(25);
        btAdd.setLayoutX(730);
        btAdd.setLayoutY(5);
        
        Button btOther = new Button("...");
        btOther.setTooltip(new Tooltip("More options"));
        btOther.setPrefWidth(30);
        btOther.setLayoutX(760);
        btOther.setLayoutY(5);

        setTitle("My Media Library");

        ComboBox<String> cboSort = new ComboBox<>();
        cboSort.setLayoutX(545);
        cboSort.setLayoutY(5);

        String[] sortOptions = {"Sort by Name", "Sort by Genre", "Sort by Format", "Sort by Rating",
                "Sort by Color", "Sort by Year", "Sort by Year Consumed", "Sort by Date Added"};
        cboSort.getItems().addAll(sortOptions);
        cboSort.setValue("Sort by Date Added"); // Default sort
        sortStyle = Comparator.comparing(Media::getDateAdded).reversed();
        top.getChildren().addAll(btHome, btUndo, btRedo, btAdd, btOther, titleText, cboSort);

        HBox bottom = new HBox(15);
        bottom.setPadding(new Insets(10, 10, 10, 10));
        sizeText = new Text("Library size: " + library.getSize());
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
            view.undo();
            library = view.getLibrary();
            sizeText.setText("Library size: " + library.getSize());
        });

        btRedo.setOnAction(e -> {
            setTitle("My Media Library");
            view.redo();
            library = view.getLibrary();
            sizeText.setText("Library size: " + library.getSize());
        });

        btHome.setOnAction(e -> {
            setTitle("My Media Library");
            view.draw();
        });

        cboSort.setOnAction(e -> {
            sortLibrary(library, cboSort.getValue());
            view.draw();
        });

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
        btInitialCancel.setOnAction(e -> addStage.close());

        btInitialAdd.setOnAction(e -> {
            addStage.close();

            String type = cboAdd.getValue();
            showAddStage(type);
        });

        // ----- "OTHER" STAGE ----- //

        HBox otherHB = new HBox();
        otherHB.setPadding(new Insets(10, 10, 10, 10));
        otherHB.setAlignment(Pos.CENTER);
        Text otherText = new Text("More Options");
        otherText.setTextAlignment(TextAlignment.CENTER);
        otherHB.getChildren().add(otherText);

        Pane otherPane = new Pane();

        Button btInfo = new Button("More Information");
        btInfo.setPrefWidth(200);
        btInfo.setLayoutX(50);
        btInfo.setLayoutY(20);

        Button btGroup = new Button("Show a group");
        btGroup.setPrefWidth(200);
        btGroup.setLayoutX(50);
        btGroup.setLayoutY(60);

        Button btSearch = new Button("Search for an item");
        btSearch.setPrefWidth(200);
        btSearch.setLayoutX(50);
        btSearch.setLayoutY(100);

        otherPane.getChildren().addAll(btInfo, btGroup, btSearch);

        BorderPane otherBP = new BorderPane();
        otherBP.setTop(otherHB);
        otherBP.setCenter(otherPane);
        Scene otherScene = new Scene(otherBP, 300, 200);
        Stage otherStage = new Stage();
        otherStage.setScene(otherScene);
        otherStage.setResizable(false);
        otherStage.setTitle("More Options");

        btOther.setOnAction(e -> otherStage.show());

        // ----- INFO STAGE ----- //

        HBox infoHB = new HBox();
        infoHB.setPadding(new Insets(10, 10, 10, 10));
        infoHB.setAlignment(Pos.CENTER);
        Text infoTitleText = new Text("More Information");
        infoTitleText.setTextAlignment(TextAlignment.CENTER);
        infoHB.getChildren().add(infoTitleText);

        Pane infoPane = new Pane();
        Text infoText = new Text(30, 20, "This program allows you to compile various media into one " +
                "visually cohesive library. Use the \"+\" button in the top-right corner to add a new media entry. " +
                "To edit or remove a media entry, click on the desired entry and scroll down, or right-click the " +
                "media entry and choose the desired option.");
        infoText.setWrappingWidth(240);
        Button btInfoOK = new Button("OK");
        btInfoOK.setPrefWidth(60);
        btInfoOK.setLayoutX(120);
        btInfoOK.setLayoutY(LibraryView.calculateTextHeight(infoText) + 50);
        infoPane.getChildren().addAll(infoText, btInfoOK);

        BorderPane infoBP = new BorderPane();
        infoBP.setTop(infoHB);
        infoBP.setCenter(infoPane);

        Stage infoStage = new Stage();
        infoStage.setScene(new Scene(infoBP, 300, LibraryView.calculateTextHeight(infoText) + 130));
        infoStage.setTitle("More Information");
        infoStage.setResizable(false);
        btInfo.setOnAction(e -> infoStage.show());
        btInfoOK.setOnAction(e -> infoStage.close());

        // ----- GROUP SEARCH STAGE ----- //

        HBox groupSearchHB = new HBox();
        groupSearchHB.setPadding(new Insets(10, 10, 10, 10));
        groupSearchHB.setAlignment(Pos.CENTER);
        Text groupSearchText = new Text("Search for a group:");
        groupSearchText.setTextAlignment(TextAlignment.CENTER);
        groupSearchHB.getChildren().add(groupSearchText);

        Pane groupSearchPane = new Pane();

        TextField groupSearchTF = new TextField();
        groupSearchTF.setPromptText("Type a group...");
        groupSearchTF.setPrefWidth(130);
        groupSearchTF.setLayoutX(50);
        groupSearchTF.setLayoutY(10);

        Button btGroupSearch = new Button("Search");
        btGroupSearch.setPrefWidth(60);
        btGroupSearch.setLayoutX(190);
        btGroupSearch.setLayoutY(10);

        groupSearchPane.getChildren().addAll(groupSearchTF, btGroupSearch);

        BorderPane groupSearchBP = new BorderPane();
        groupSearchBP.setTop(groupSearchHB);
        groupSearchBP.setCenter(groupSearchPane);

        Stage groupSearchStage = new Stage();
        groupSearchStage.setScene(new Scene(groupSearchBP, 300, 100));
        groupSearchStage.setTitle("Group Search");
        groupSearchStage.setResizable(false);
        btGroup.setOnAction(e -> {
            groupSearchStage.show();
            otherStage.close();
        });

        btGroupSearch.setOnAction(e -> {
            setTitle("Group: " + groupSearchTF.getText());
            groupSearchStage.close();
            view.drawGroup(groupSearchTF.getText());
        });
        
        // ----- GENERAL SEARCH STAGE ----- //

        HBox generalSearchHB = new HBox();
        generalSearchHB.setPadding(new Insets(10, 10, 10, 10));
        generalSearchHB.setAlignment(Pos.CENTER);
        Text generalSearchText = new Text("Search for an item:");
        generalSearchText.setTextAlignment(TextAlignment.CENTER);
        generalSearchHB.getChildren().add(generalSearchText);

        Pane generalSearchPane = new Pane();

        TextField generalSearchTF = new TextField();
        generalSearchTF.setPromptText("Type a name...");
        generalSearchTF.setPrefWidth(130);
        generalSearchTF.setLayoutX(50);
        generalSearchTF.setLayoutY(10);

        Button btGeneralSearch = new Button("Search");
        btGeneralSearch.setPrefWidth(60);
        btGeneralSearch.setLayoutX(190);
        btGeneralSearch.setLayoutY(10);

        generalSearchPane.getChildren().addAll(generalSearchTF, btGeneralSearch);

        FlowPane generalSearchOptions = new FlowPane();
        generalSearchOptions.setPadding(new Insets(10, 10, 10, 10));
        generalSearchOptions.setAlignment(Pos.CENTER);
        generalSearchOptions.setHgap(10);
        generalSearchOptions.setVgap(10);
        CheckBox chkMovie = new CheckBox("Movies");
        chkMovie.setSelected(true);
        CheckBox chkShow = new CheckBox("Shows");
        chkShow.setSelected(true);
        CheckBox chkGame = new CheckBox("Games");
        chkGame.setSelected(true);
        CheckBox chkMusic = new CheckBox("Music");
        chkMusic.setSelected(true);
        CheckBox chkBook = new CheckBox("Books");
        chkBook.setSelected(true);
        generalSearchOptions.getChildren().addAll(chkMovie, chkShow, chkGame, chkMusic, chkBook);

        BorderPane generalSearchBP = new BorderPane();
        generalSearchBP.setTop(generalSearchHB);
        generalSearchBP.setCenter(generalSearchPane);
        generalSearchBP.setBottom(generalSearchOptions);

        Stage generalSearchStage = new Stage();
        generalSearchStage.setScene(new Scene(generalSearchBP, 300, 160));
        generalSearchStage.setTitle("General Search");
        generalSearchStage.setResizable(false);
        btSearch.setOnAction(e -> {
            generalSearchStage.show();
            otherStage.close();
        });

        btGeneralSearch.setOnAction(e -> {
            // general search
        });
    }

    /** Show a stage to add media */
    public void showAddStage(String type) {
        Stage stage = new Stage();

        GridPane addGrid = new GridPane();
        addGrid.setPadding(new Insets(15, 15, 15, 15));
        addGrid.setHgap(15);
        addGrid.setVgap(15);
        addGrid.setAlignment(Pos.TOP_CENTER);

        // Universal input fields
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

        Button btAdd = new Button("Add");
        Button btCancel = new Button("Cancel");
        HBox addButtons = new HBox(30);
        addButtons.getChildren().addAll(btAdd, btCancel);
        addButtons.setAlignment(Pos.CENTER);
        addButtons.setPadding(new Insets(15, 15, 30, 15));
        HBox addError = new HBox(15);
        addError.setAlignment(Pos.CENTER);
        Text errorText = new Text("");
        errorText.setFill(Color.RED);
        addError.getChildren().add(errorText);
        VBox addBottom = new VBox(15);
        addBottom.getChildren().addAll(addError, addButtons);

        BorderPane add = new BorderPane();
        add.setCenter(addGrid);
        add.setBottom(addBottom);
        HBox addTop = new HBox(15);
        addTop.setAlignment(Pos.CENTER);
        addTop.setPadding(new Insets(15, 15, 15, 25));
        Text movieAddText = new Text("Add New " + type);
        addTop.getChildren().add(movieAddText);
        add.setTop(addTop);

        // Type-specific input fields
        // Movie fields
        Text director = new Text("Director: ");
        TextField directorTF = new TextField();
        Text duration = new Text("Duration in minutes: ");
        TextField durationTF = new TextField();

        // Show fields
        Text creator = new Text("Creator: ");
        TextField creatorTF = new TextField();
        Text numSeasons = new Text("Number of seasons: ");
        TextField numSeasonsTF = new TextField();
        Text numEpisodes = new Text("Number of episodes: ");
        TextField numEpisodesTF = new TextField();

        // Game fields
        Text developer = new Text("Developer: ");
        TextField developerTF = new TextField();
        Text console = new Text("Console: ");
        TextField consoleTF = new TextField();
        Text numPlayers = new Text("Number of players: ");
        TextField numPlayersTF = new TextField();

        // Music fields
        Text artist = new Text("Artist: ");
        TextField artistTF = new TextField();

        // Book fields
        Text author = new Text("Author: ");
        TextField authorTF = new TextField();

        double height = 0;
        switch (type) {
            case "Movie":
                height = 630;

                addGrid.addColumn(0, name, genre, director, duration, description,
                        format, year, yearConsumed, rating, color);
                addGrid.addColumn(1, nameTF, genreTF, directorTF, durationTF, descriptionTA,
                        formatTF, yearTF, yearConsumedTF, ratingTF, colorPicker);
                break;
            case "Show":
                height = 650;

                addGrid.addColumn(0, name, genre, creator, numSeasons, numEpisodes, description,
                        format, year, yearConsumed, rating, color);
                addGrid.addColumn(1, nameTF, genreTF, creatorTF, numSeasonsTF, numEpisodesTF,
                        descriptionTA, formatTF, yearTF, yearConsumedTF, ratingTF, colorPicker);
                break;
            case "Game":
                height = 650;

                addGrid.addColumn(0, name, genre, developer, console, numPlayers, description,
                        format, year, yearConsumed, rating, color);
                addGrid.addColumn(1, nameTF, genreTF, developerTF, consoleTF, numPlayersTF,
                        descriptionTA, formatTF, yearTF, yearConsumedTF, ratingTF, colorPicker);
                break;
            case "Music":
                height = 600;

                addGrid.addColumn(0, name, genre, artist, description, format, year,
                        yearConsumed, rating, color);
                addGrid.addColumn(1, nameTF, genreTF, artistTF, descriptionTA, formatTF,
                        yearTF, yearConsumedTF, ratingTF, colorPicker);
                break;
            case "Book":
                height = 600;

                addGrid.addColumn(0, name, genre, author, description, format, year,
                        yearConsumed, rating, color);
                addGrid.addColumn(1, nameTF, genreTF, authorTF, descriptionTA, formatTF,
                        yearTF, yearConsumedTF, ratingTF, colorPicker);
                break;
        }

        btAdd.setOnAction(e -> {
            try {
                if (nameTF.getText().isBlank()) {
                    errorText.setText("You must enter a " + type + " name");
                } else if (!ratingTF.getText().isBlank() && Media.validateRating(Double.parseDouble(ratingTF.getText()))) {
                    errorText.setText("Rating must be between 0 and 10");
                } else {
                    // Set universal media fields
                    String n = nameTF.getText();
                    String g = genreTF.getText();
                    String desc = descriptionTA.getText();
                    String f = formatTF.getText();
                    int y = yearTF.getText().isBlank() ? Year.now().getValue() : Integer.parseInt(yearTF.getText());
                    int yc = yearConsumedTF.getText().isBlank() ? Year.now().getValue() : Integer.parseInt(yearConsumedTF.getText());
                    double r = ratingTF.getText().isBlank() ? 0 : Double.parseDouble(ratingTF.getText());
                    double[] c = {colorPicker.getValue().getRed(), colorPicker.getValue().getGreen(),
                            colorPicker.getValue().getBlue(), colorPicker.getValue().getOpacity()};

                    // Set type-specific media fields
                    switch (type) {
                        case "Movie":
                            if (!durationTF.getText().isBlank() && Integer.parseInt(durationTF.getText()) < 0) {
                                errorText.setText("Duration cannot be less than 0");
                            } else { // Successful case
                                String dir = directorTF.getText();
                                int d = durationTF.getText().isBlank() ? 0 : Integer.parseInt(durationTF.getText());

                                view.add(new Movie(n, g, desc, f, y, yc, r, c, dir, d));
                                stage.close();
                            }
                            break;
                        case "Show":
                            if (!numSeasonsTF.getText().isBlank() && Integer.parseInt(numSeasonsTF.getText()) < 0) {
                                errorText.setText("Number of seasons cannot be less than 0");
                            } else if (!numEpisodesTF.getText().isBlank() && Integer.parseInt(numEpisodesTF.getText()) < 0) {
                                errorText.setText("Number of episodes cannot be less than 0");
                            } else { // Successful case
                                String cr = creatorTF.getText();
                                int ns = numSeasonsTF.getText().isBlank() ? 0 : Integer.parseInt(numSeasonsTF.getText());
                                int ne = numEpisodesTF.getText().isBlank() ? 0 : Integer.parseInt(numEpisodesTF.getText());

                                view.add(new Show(n, g, desc, f, y, yc, r, c, cr, ns, ne));
                                stage.close();
                            }
                            break;
                        case "Game":
                            if (!numPlayersTF.getText().isBlank() && Integer.parseInt(numPlayersTF.getText()) < 0) {
                                errorText.setText("Number of players cannot be less than 0");
                            } else {
                                String dev = developerTF.getText();
                                String con = consoleTF.getText();
                                int np = numPlayersTF.getText().isBlank() ? 0 : Integer.parseInt(numPlayersTF.getText());

                                view.add(new Game(n, g, desc, f, y, yc, r, c, dev, con, np));
                                stage.close();
                            }
                            break;
                        case "Music":
                            String art = artistTF.getText();

                            view.add(new Music(n, g, desc, f, y, yc, r, c, art));
                            stage.close();
                            break;
                        case "Book":
                            String auth = authorTF.getText();

                            view.add(new Book(n, g, desc, f, y, yc, r, c, auth));
                            stage.close();
                            break;
                    }
                }
            } catch (Exception ex) {
                errorText.setText("Invalid input");
            }
        });

        btCancel.setOnAction(e -> stage.close());

        stage.setScene(new Scene(add, 400, height));
        stage.setTitle("Add New " + type);
        stage.setResizable(false);
        stage.show();
    }

    public static void sortLibrary(Library<Media> lib, String sortBy) {
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

    public static void setSize(int size) {
        sizeText.setText("Library size: " + size);
    }

    public static void setTitle(String title) {
        titleText.setText(title);
    }
}
