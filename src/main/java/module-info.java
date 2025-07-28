module com.bilante.fightgame {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires jdk.unsupported;


    opens com.bilante.fightgame to javafx.fxml;
    exports com.bilante.fightgame;
}