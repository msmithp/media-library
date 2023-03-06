module com.matthewsmith.medialibrary {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.matthewsmith.medialibrary to javafx.fxml;
    exports com.matthewsmith.medialibrary;
}