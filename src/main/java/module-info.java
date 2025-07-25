module com.bilante.fightgame {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.bilante.fightgame to javafx.fxml;
    exports com.bilante.fightgame;
}