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
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.File;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;

public class MediaLibrary extends Application {
    public static final String CSS = new File("./application.css").toURI().toString(); // css stylesheet
    private LibraryView view;
    private Library<Media> library;
    private static Text sizeText;
    private static Text titleText = new Text(350, 25, "");

    /** Creates and displays JavaFX GUI */
    @Override
    public void start(Stage primaryStage) {
        // ----- MAIN LIBRARY STAGE ----- //
        library = new Library<>();
        library.read(); // Read library from file
        library.sort(Comparator.comparing(Media::getDateAdded).reversed()); // Default sort: sort by date

        // Build search tree
        for (Media m : library) {
            library.addToTree(m.getName(), m);
        }

        view = new LibraryView(library);
        ScrollPane libScroll = new ScrollPane(view);

        HBox top = new HBox(70);
        top.setPadding(new Insets(10, 10, 10, 10));
        HBox topLeft = new HBox(10);
        HBox topRight = new HBox(10);
        StackPane topMiddle = new StackPane();

        Button btHome = new Button("Home");
        btHome.setTooltip(new Tooltip("Return home"));
        btHome.setPrefWidth(65);

        Button btUndo = new Button("Undo");
        btUndo.setTooltip(new Tooltip("Undo previous action"));
        btUndo.setPrefWidth(65);

        Button btRedo = new Button("Redo");
        btRedo.setTooltip(new Tooltip("Redo undone action"));
        btRedo.setPrefWidth(65);
        topLeft.getChildren().addAll(btHome, btUndo, btRedo);
        
        Button btAdd = new Button("+");
        btAdd.setTooltip(new Tooltip("Add new media"));
        btAdd.setPrefWidth(30);
        
        Button btOther = new Button("...");
        btOther.setTooltip(new Tooltip("More options"));
        btOther.setPrefWidth(30);

        ComboBox<String> cboSort = new ComboBox<>();
        String[] sortOptions = {"Sort by Name", "Sort by Genre", "Sort by Format", "Sort by Rating",
                "Sort by Color", "Sort by Year", "Sort by Year Consumed", "Sort by Date Added"};
        cboSort.getItems().addAll(sortOptions);
        cboSort.setValue("Sort by Date Added"); // Default sort
        topRight.getChildren().addAll(cboSort, btAdd, btOther);
        topMiddle.getChildren().add(titleText);

        top.getChildren().addAll(topLeft, topMiddle, topRight);
        top.setAlignment(Pos.CENTER);

        setTitle("My Media Library");

        HBox bottom = new HBox(15);
        bottom.setPadding(new Insets(10, 10, 10, 10));
        sizeText = new Text("Library size: " + library.getSize());
        bottom.getChildren().addAll(sizeText);

        BorderPane bp = new BorderPane();
        bp.setTop(top);
        bp.setCenter(libScroll);
        bp.setBottom(bottom);

        Scene scene = new Scene(bp, 800, 550);
        scene.getStylesheets().add(CSS);

        primaryStage.setTitle("Media Library");
        primaryStage.setScene(scene);
        primaryStage.show();
        view.draw(); // draw library

        btUndo.setOnAction(e -> {
            setTitle("My Media Library");
            view.undo();
            setSize(library.getSize());
        });

        btRedo.setOnAction(e -> {
            setTitle("My Media Library");
            view.redo();
            setSize(library.getSize());
        });

        btHome.setOnAction(e -> {
            setTitle("My Media Library");
            view.draw();
            libScroll.setHvalue(0);
        });

        cboSort.setOnAction(e -> {
            sortLibrary(library, cboSort.getValue());
            view.draw();
        });

        // ----- INITIAL "ADD" STAGE ----- //

        HBox addBoxTop = new HBox(15);
        addBoxTop.setPadding(new Insets(15, 15, 0, 15));
        addBoxTop.setAlignment(Pos.CENTER);
        Text addText = new Text("Select a type of media to add: ");
        String[] addOptions = {"Movie", "Show", "Game", "Music", "Book"};
        ComboBox<String> cboAdd = new ComboBox<>();
        cboAdd.getItems().addAll(addOptions);
        cboAdd.setValue("Movie");
        addBoxTop.getChildren().addAll(addText, cboAdd);

        HBox addBoxBottom = new HBox(15);
        addBoxBottom.setPadding(new Insets(0, 15, 15, 15));
        addBoxBottom.setAlignment(Pos.CENTER);
        Button btInitialAdd = new Button("OK");
        Button btInitialCancel = new Button("Cancel");
        addBoxBottom.getChildren().addAll(btInitialAdd, btInitialCancel);

        VBox addBox = new VBox(30);
        addBox.setAlignment(Pos.CENTER);
        addBox.getChildren().addAll(addBoxTop, addBoxBottom);

        Stage addStage = new Stage();
        Scene addScene = new Scene(addBox, 360, 120);
        addScene.getStylesheets().add(CSS);
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

        VBox other = new VBox(15);
        other.setAlignment(Pos.CENTER);

        Button btInfo = new Button("More information");
        btInfo.setPrefWidth(200);

        Button btFilter = new Button("Filter by type");
        btFilter.setPrefWidth(200);

        Button btGroup = new Button("Show a group");
        btGroup.setPrefWidth(200);

        Button btSearch = new Button("Search for an item");
        btSearch.setPrefWidth(200);

        Button btClear = new Button("Clear library");
        btClear.setPrefWidth(200);

        other.getChildren().addAll(btInfo, btFilter, btGroup, btSearch, btClear);

        BorderPane otherBP = new BorderPane();
        otherBP.setTop(otherHB);
        otherBP.setCenter(other);
        Scene otherScene = new Scene(otherBP, 300, 280);
        otherScene.getStylesheets().add(CSS);
        Stage otherStage = new Stage();
        otherStage.setScene(otherScene);
        otherStage.setResizable(false);
        otherStage.setTitle("More Options");

        btOther.setOnAction(e -> otherStage.show());

        // ----- INFO STAGE ----- //

        VBox infoVB = new VBox(20);
        infoVB.setPadding(new Insets(10, 10, 10, 10));
        infoVB.setAlignment(Pos.CENTER);
        Text infoTitleText = new Text("More Information");
        infoTitleText.setTextAlignment(TextAlignment.CENTER);

        Text infoText = new Text(30, 20, "This program allows you to compile various media into one " +
                "visually cohesive library. Use the \"+\" button in the top-right corner to add a new media entry. " +
                "To edit or remove a media entry, click on the desired entry and scroll down, or right-click the " +
                "media entry and choose the desired option.");
        infoText.setWrappingWidth(300);
        Button btInfoOK = new Button("OK");
        btInfoOK.setPrefWidth(60);
        infoVB.getChildren().addAll(infoTitleText, infoText, btInfoOK);

        Stage infoStage = new Stage();
        Scene infoScene = new Scene(infoVB, 400, 300);
        infoScene.getStylesheets().add(CSS);
        infoStage.setScene(infoScene);
        infoStage.setTitle("More Information");
        infoStage.setResizable(false);
        btInfo.setOnAction(e -> infoStage.show());
        btInfoOK.setOnAction(e -> infoStage.close());
        
        // ----- FILTER STAGE ----- //
        HBox filterHB = new HBox();
        filterHB.setPadding(new Insets(10, 10, 0, 10));
        filterHB.setAlignment(Pos.CENTER);
        Text filterText = new Text("Choose the media types to include:");
        filterText.setTextAlignment(TextAlignment.CENTER);
        filterHB.getChildren().add(filterText);
        
        FlowPane filterOptions = new FlowPane();
        filterOptions.setPadding(new Insets(10, 10, 0, 10));
        filterOptions.setAlignment(Pos.CENTER);
        filterOptions.setHgap(10);
        filterOptions.setVgap(10);
        CheckBox chkMovieFilter = new CheckBox("Movies");
        chkMovieFilter.setSelected(true);
        CheckBox chkShowFilter = new CheckBox("Shows");
        chkShowFilter.setSelected(true);
        CheckBox chkGameFilter = new CheckBox("Games");
        chkGameFilter.setSelected(true);
        CheckBox chkMusicFilter = new CheckBox("Music");
        chkMusicFilter.setSelected(true);
        CheckBox chkBookFilter = new CheckBox("Books");
        chkBookFilter.setSelected(true);
        filterOptions.getChildren().addAll(chkMovieFilter, chkShowFilter, chkGameFilter, chkMusicFilter, chkBookFilter);

        HBox filterButtons = new HBox(10);
        filterButtons.setPadding(new Insets(0, 10, 10, 10));
        filterButtons.setAlignment(Pos.CENTER);
        Button btFilterOK = new Button("OK");
        btFilterOK.setPrefWidth(60);
        Button btFilterReset = new Button("Reset");
        btFilterReset.setPrefWidth(60);
        filterButtons.getChildren().addAll(btFilterOK, btFilterReset);

        BorderPane filterBP = new BorderPane();
        filterBP.setTop(filterHB);
        filterBP.setCenter(filterOptions);
        filterBP.setBottom(filterButtons);

        Stage filterStage = new Stage();
        Scene filterScene = new Scene(filterBP, 350, 250);
        filterScene.getStylesheets().add(CSS);
        filterStage.setScene(filterScene);
        filterStage.setTitle("Filter Library");
        filterStage.setResizable(false);

        btFilter.setOnAction(e -> {
            filterStage.show();
            otherStage.close();
        });

        btFilterOK.setOnAction(e -> filterStage.close());

        btFilterReset.setOnAction(e -> {
            chkMovieFilter.setSelected(true);
            chkShowFilter.setSelected(true);
            chkGameFilter.setSelected(true);
            chkMusicFilter.setSelected(true);
            chkBookFilter.setSelected(true);
            view.resetFilters();
        });

        chkMovieFilter.setOnAction(e -> {
            if (chkMovieFilter.isSelected()) {
                view.removeFilter("Movie");
            } else {
                view.addFilter("Movie");
            }
        });

        chkShowFilter.setOnAction(e -> {
            if (chkShowFilter.isSelected()) {
                view.removeFilter("Show");
            } else {
                view.addFilter("Show");
            }
        });

        chkGameFilter.setOnAction(e -> {
            if (chkGameFilter.isSelected()) {
                view.removeFilter("Game");
            } else {
                view.addFilter("Game");
            }
        });

        chkMusicFilter.setOnAction(e -> {
            if (chkMusicFilter.isSelected()) {
                view.removeFilter("Music");
            } else {
                view.addFilter("Music");
            }
        });

        chkBookFilter.setOnAction(e -> {
            if (chkBookFilter.isSelected()) {
                view.removeFilter("Book");
            } else {
                view.addFilter("Book");
            }
        });

        // ----- GROUP SEARCH STAGE ----- //

        HBox groupSearchHB = new HBox();
        groupSearchHB.setPadding(new Insets(10, 10, 0, 10));
        groupSearchHB.setAlignment(Pos.CENTER);
        Text groupSearchText = new Text("Search for a group:");
        groupSearchText.setTextAlignment(TextAlignment.CENTER);
        groupSearchHB.getChildren().add(groupSearchText);

        HBox groupSearchBox = new HBox(10);
        groupSearchBox.setAlignment(Pos.CENTER);

        TextField groupSearchTF = new TextField();
        groupSearchTF.setPromptText("Type a group...");
        groupSearchTF.setPrefWidth(150);

        Button btGroupSearch = new Button("Search");
        btGroupSearch.setPrefWidth(80);

        groupSearchBox.getChildren().addAll(groupSearchTF, btGroupSearch);

        BorderPane groupSearchBP = new BorderPane();
        groupSearchBP.setTop(groupSearchHB);
        groupSearchBP.setCenter(groupSearchBox);

        Stage groupSearchStage = new Stage();
        Scene groupSearchScene = new Scene(groupSearchBP, 350, 120);
        groupSearchScene.getStylesheets().add(CSS);
        groupSearchStage.setScene(groupSearchScene);
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
            libScroll.setHvalue(0);
        });
        
        // ----- GENERAL SEARCH STAGE ----- //

        HBox generalSearchHB = new HBox();
        generalSearchHB.setPadding(new Insets(10, 10, 0, 10));
        generalSearchHB.setAlignment(Pos.CENTER);
        Text generalSearchText = new Text("Search for an item:");
        generalSearchText.setTextAlignment(TextAlignment.CENTER);
        generalSearchHB.getChildren().add(generalSearchText);

        HBox generalSearchBox = new HBox(10);
        generalSearchBox.setAlignment(Pos.CENTER);

        TextField generalSearchTF = new TextField();
        generalSearchTF.setPromptText("Type a name...");
        generalSearchTF.setPrefWidth(140);

        Button btGeneralSearch = new Button("Search");
        btGeneralSearch.setPrefWidth(80);

        generalSearchBox.getChildren().addAll(generalSearchTF, btGeneralSearch);

        FlowPane generalSearchOptions = new FlowPane();
        generalSearchOptions.setPadding(new Insets(0, 10, 10, 10));
        generalSearchOptions.setAlignment(Pos.CENTER);
        generalSearchOptions.setHgap(10);
        generalSearchOptions.setVgap(10);
        CheckBox chkMovieSearch = new CheckBox("Movies");
        chkMovieSearch.setSelected(true);
        CheckBox chkShowSearch = new CheckBox("Shows");
        chkShowSearch.setSelected(true);
        CheckBox chkGameSearch = new CheckBox("Games");
        chkGameSearch.setSelected(true);
        CheckBox chkMusicSearch = new CheckBox("Music");
        chkMusicSearch.setSelected(true);
        CheckBox chkBookSearch = new CheckBox("Books");
        chkBookSearch.setSelected(true);
        generalSearchOptions.getChildren().addAll(chkMovieSearch, chkShowSearch, chkGameSearch, chkMusicSearch, chkBookSearch);

        BorderPane generalSearchBP = new BorderPane();
        generalSearchBP.setTop(generalSearchHB);
        generalSearchBP.setCenter(generalSearchBox);
        generalSearchBP.setBottom(generalSearchOptions);

        Stage generalSearchStage = new Stage();
        Scene generalSearchScene = new Scene(generalSearchBP, 300, 200);
        generalSearchScene.getStylesheets().add(CSS);
        generalSearchStage.setScene(generalSearchScene);
        generalSearchStage.setTitle("General Search");
        generalSearchStage.setResizable(false);
        btSearch.setOnAction(e -> {
            generalSearchStage.show();
            otherStage.close();
        });

        btGeneralSearch.setOnAction(e -> {
            generalSearchStage.close();

            // Set up exclusion list
            ArrayList<String> exclude = new ArrayList<>();
            if (!chkMovieSearch.isSelected()) {
                exclude.add("Movie");
            }
            if (!chkShowSearch.isSelected()) {
                exclude.add("Show");
            }
            if (!chkGameSearch.isSelected()) {
                exclude.add("Game");
            }
            if (!chkMusicSearch.isSelected()) {
                exclude.add("Music");
            }
            if (!chkBookSearch.isSelected()) {
                exclude.add("Book");
            }

            ArrayList<Media> results = library.treeSearch(generalSearchTF.getText().toLowerCase());
            if (results != null) {
                // Remove all results whose classes have been excluded
                results.removeIf(m -> exclude.contains(m.getClass().getSimpleName()));
            }

            if (results == null || results.isEmpty()) {
                showPopup("No results were found with your search criteria.");
            } else {
                view.draw(results);
                setTitle("Search: " + generalSearchTF.getText());
                libScroll.setHvalue(0);
            }
        });

        // ----- CLEAR STAGE ----- //

        BorderPane clearBP = new BorderPane();
        HBox clearTop = new HBox();
        clearTop.setPadding(new Insets(15, 15, 15, 15));
        clearTop.setAlignment(Pos.CENTER);
        Text text = new Text("Are you sure you want to clear your library? This cannot be undone.");
        text.setWrappingWidth(250);
        text.setTextAlignment(TextAlignment.CENTER);
        clearTop.getChildren().add(text);

        HBox clearBottom = new HBox(10);
        clearBottom.setPadding(new Insets(15, 15, 15, 15));
        clearBottom.setAlignment(Pos.CENTER);
        Button btClearOK = new Button("Clear");
        Button btClearCancel = new Button("Cancel");
        clearBottom.getChildren().addAll(btClearOK, btClearCancel);

        clearBP.setTop(clearTop);
        clearBP.setBottom(clearBottom);
        Scene clearScene = new Scene(clearBP, 300, 160);
        clearScene.getStylesheets().add(CSS);

        Stage clearStage = new Stage();
        clearStage.setScene(clearScene);
        clearStage.setTitle("Clear library?");
        clearStage.setResizable(false);

        btClear.setOnAction(e -> {
            otherStage.close();
            clearStage.show();
        });

        btClearOK.setOnAction(e -> {
            library.clear();
            view.draw();
            setSize(library.getSize());
            clearStage.close();
        });

        btClearCancel.setOnAction(e -> clearStage.close());
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

        Button btAdd = new Button("Add");
        Button btCancel = new Button("Cancel");
        HBox addButtons = new HBox(30);
        addButtons.getChildren().addAll(btAdd, btCancel);
        addButtons.setAlignment(Pos.CENTER);
        addButtons.setPadding(new Insets(0, 15, 30, 15));
        HBox addError = new HBox(15);
        addError.setAlignment(Pos.CENTER);
        Text errorText = new Text("");
        errorText.setStyle("-fx-fill: red;");
        addError.getChildren().add(errorText);
        VBox addBottom = new VBox(15);
        addBottom.getChildren().addAll(addError, addButtons);

        BorderPane add = new BorderPane();
        add.setCenter(addGrid);
        add.setBottom(addBottom);
        HBox addTop = new HBox(15);
        addTop.setAlignment(Pos.CENTER);
        addTop.setPadding(new Insets(15, 15, 0, 15));
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
                height = 660;

                addGrid.addColumn(0, name, genre, director, duration, description,
                        format, year, yearConsumed, rating, color);
                addGrid.addColumn(1, nameTF, genreTF, directorTF, durationTF, descriptionTA,
                        formatTF, yearTF, yearConsumedTF, ratingTF, colorPicker);
                break;
            case "Show":
                height = 710;

                addGrid.addColumn(0, name, genre, creator, numSeasons, numEpisodes, description,
                        format, year, yearConsumed, rating, color);
                addGrid.addColumn(1, nameTF, genreTF, creatorTF, numSeasonsTF, numEpisodesTF,
                        descriptionTA, formatTF, yearTF, yearConsumedTF, ratingTF, colorPicker);
                break;
            case "Game":
                height = 710;

                addGrid.addColumn(0, name, genre, developer, console, numPlayers, description,
                        format, year, yearConsumed, rating, color);
                addGrid.addColumn(1, nameTF, genreTF, developerTF, consoleTF, numPlayersTF,
                        descriptionTA, formatTF, yearTF, yearConsumedTF, ratingTF, colorPicker);
                break;
            case "Music":
                height = 610;

                addGrid.addColumn(0, name, genre, artist, description, format, year,
                        yearConsumed, rating, color);
                addGrid.addColumn(1, nameTF, genreTF, artistTF, descriptionTA, formatTF,
                        yearTF, yearConsumedTF, ratingTF, colorPicker);
                break;
            case "Book":
                height = 610;

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
                } else if (!ratingTF.getText().isBlank() && !Media.validateRating(Double.parseDouble(ratingTF.getText()))) {
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
                                setSize(library.getSize());
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
                                setSize(library.getSize());
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
                                setSize(library.getSize());
                                stage.close();
                            }
                            break;
                        case "Music":
                            String art = artistTF.getText();

                            view.add(new Music(n, g, desc, f, y, yc, r, c, art));
                            setSize(library.getSize());
                            stage.close();
                            break;
                        case "Book":
                            String auth = authorTF.getText();

                            view.add(new Book(n, g, desc, f, y, yc, r, c, auth));
                            setSize(library.getSize());
                            stage.close();
                            break;
                    }
                }
            } catch (Exception ex) {
                errorText.setText("Invalid input");
            }
        });

        btCancel.setOnAction(e -> stage.close());
        Scene scene = new Scene(add, 400, height);
        scene.getStylesheets().add(CSS);

        stage.setScene(scene);
        stage.setTitle("Add New " + type);
        stage.setResizable(false);
        stage.show();
    }

    /** Displays a popup with a specified message */
    public void showPopup(String t) {
        BorderPane bp = new BorderPane();
        HBox top = new HBox();
        top.setPadding(new Insets(15, 15, 15, 15));
        top.setAlignment(Pos.CENTER);
        Text text = new Text(t);
        text.setWrappingWidth(250);
        text.setTextAlignment(TextAlignment.CENTER);
        top.getChildren().add(text);

        HBox bottom = new HBox();
        bottom.setPadding(new Insets(15, 15, 15, 15));
        bottom.setAlignment(Pos.CENTER);
        Button btOK = new Button("OK");
        bottom.getChildren().add(btOK);

        bp.setTop(top);
        bp.setBottom(bottom);

        Stage stage = new Stage();
        stage.setResizable(false);
        stage.setTitle("Alert");
        Scene scene = new Scene(bp, 300, LibraryView.calculateTextHeight(text) + 120);
        scene.getStylesheets().add(CSS);
        stage.setScene(scene);
        stage.show();

        btOK.setOnAction(e -> stage.close());
    }

    /** Sorts a Library of Media by a specified means */
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
                lib.sort(Comparator.comparing((Media m) -> m.getColor().getHue())
                        .thenComparing((Media m) -> m.getColor().getSaturation())
                        .thenComparing((Media m) -> m.getColor().getBrightness()));
                break;
            case "Sort by Year":
                lib.sort(Comparator.comparing(Media::getYear));
                break;
            case "Sort by Year Consumed":
                lib.sort(Comparator.comparing(Media::getYearConsumed));
                break;
            default:
                lib.sort(Comparator.comparing(Media::getDateAdded).reversed());
                break;
        }
    }

    /** Sets the size text of the library */
    public static void setSize(int size) {
        sizeText.setText("Library size: " + size);
    }

    /** Sets the title of the library */
    public static void setTitle(String title) {
        String shortenedTitle = LibraryView.shortenText(new Text(title), 130);
        titleText.setText(shortenedTitle);
    }
}
