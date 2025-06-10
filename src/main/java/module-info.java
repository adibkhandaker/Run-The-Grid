module org.example.nfldata {
    requires javafx.controls;
    requires javafx.fxml;
    requires json.simple;


    opens org.example.nfldata to javafx.fxml;
    exports org.example.nfldata;
}