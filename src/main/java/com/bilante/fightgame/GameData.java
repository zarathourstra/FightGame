package com.bilante.fightgame;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GameData {
    SidePanel sidePanel;
    GameBox gameBox;
    SimpleBooleanProperty dataReady = new SimpleBooleanProperty(false);

    public GameData () {
        sidePanel = new SidePanel();
        gameBox = new GameBox();
    }

}
