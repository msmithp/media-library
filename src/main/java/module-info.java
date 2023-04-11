module com.matthewsmith.medialibrary {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;


    opens com.matthewsmith.medialibrary to javafx.fxml;
    exports com.matthewsmith.medialibrary;
}