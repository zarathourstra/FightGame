package com.bilante.fightgame;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class MainController {

    @FXML private SidePanelController sidePanelController;
    @FXML private GameBoxController gameBoxController;

    private GameData gameData;

    public void initialize() {
        gameData = new GameData();
        sidePanelController.setModel(gameData);
        gameBoxController.setModel(gameData);

        sidePanelController.quickStart();

    }


}
