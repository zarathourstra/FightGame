package com.bilante.fightgame;

import javafx.animation.*;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class SidePanelController {

    @FXML
    private VBox playerTemplatesContainer;

    @FXML
    private HBox removeAddPlayer;

    @FXML
    private Button startButton;

    AnimationTimer displayLoop;

    private GameData gameData;
    private List<PlayerFieldSet> playerFieldSets = new ArrayList<>();

    public void setModel(GameData gameData) {
        this.gameData = gameData;

        gameData.dataReady.addListener((observable, oldValue, newValue) -> {
            if (!newValue && oldValue) {
                if (displayLoop != null) {
                    displayLoop.stop();
                    displayLoop = null;
                }

                removeAddPlayer.setDisable(false);
                removeAddPlayer.setVisible(true);
                startButton.setDisable(false);
                startButton.setVisible(true);

                playerTemplatesContainer.getChildren().clear();
                gameData.sidePanel.playerDisplaySets.clear();
                playerFieldSets.clear();

                List<SidePanel.PlayerInput> inputs = gameData.sidePanel.playerInputs;
                for (int i = 0; i < inputs.size(); i++) {
                    SidePanel.PlayerInput input = inputs.get(i);
                    PlayerFieldSet fieldSet = new PlayerFieldSet(i + 1);
                    fieldSet.nameField.setText(input.name);
                    fieldSet.damageSpinner.getValueFactory().setValue(input.damagePoints);
                    fieldSet.speedSpinner.getValueFactory().setValue(input.damagePoints);
                    fieldSet.healthSpinner.getValueFactory().setValue(input.healthPoints);

                    playerFieldSets.add(fieldSet);
                    playerTemplatesContainer.getChildren().add(fieldSet.container);
                }
            }
        });

    }


    public void quickStart() {
        addPlayerField();
        addPlayerField();
    }

    @FXML
    private void onAddPlayer() {
        addPlayerField();
    }

    @FXML
    private void onRemovePlayer() {
        if (playerFieldSets.size() > 2) {
            int lastIndex = playerFieldSets.size() - 1;
            PlayerFieldSet lastFieldSet = playerFieldSets.get(lastIndex);
            playerTemplatesContainer.getChildren().remove(lastFieldSet.container);
            playerFieldSets.remove(lastIndex);
            gameData.sidePanel.removeInputField(lastIndex);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Minimum players");
            alert.setHeaderText(null);
            alert.setContentText("At least two players!");
            alert.showAndWait();
        }
    }

    @FXML
    private void onValidate() {
        gameData.sidePanel.displays.clear();
        gameData.sidePanel.playerDisplaySets.clear();
        playerTemplatesContainer.getChildren().clear();

        for (int i = 0; i < playerFieldSets.size(); i++) {
            PlayerFieldSet fieldSet = playerFieldSets.get(i);
            SidePanel.PlayerInput playerInput = gameData.sidePanel.playerInputs.get(i);
            playerInput.setName(fieldSet.nameField.getText());
            playerInput.setDamagePoints(fieldSet.damageSpinner.getValue());
            playerInput.setSpeedPoints(fieldSet.speedSpinner.getValue());
            playerInput.setHealthPoints(fieldSet.healthSpinner.getValue());
            gameData.sidePanel.displays.add(new SidePanel.PlayerInfoDisplay(playerInput));
        }

        removeAddPlayer.setDisable(true);
        startButton.setDisable(true);
        startButton.setVisible(false);
        removeAddPlayer.setVisible(false);
        inflateLiveDisplay();

        gameData.dataReady.set(true);
    }

    private void inflateLiveDisplay() {
        playerTemplatesContainer.getChildren().clear();
        gameData.sidePanel.playerDisplaySets.clear();

        for (SidePanel.PlayerInfoDisplay info : gameData.sidePanel.displays) {
            PlayerDisplaySet display = new PlayerDisplaySet(info.name, info.speed, info.damage, info.health, gameData.sidePanel.displays.indexOf(info));
            gameData.sidePanel.playerDisplaySets.add(display);
            playerTemplatesContainer.getChildren().add(display.container);
        }
        startLoopDisplay();

    }

    public void startLoopDisplay () {
        displayLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                for (int i = 0; i < gameData.sidePanel.playerDisplaySets.size(); i++) {
                    PlayerDisplaySet display = gameData.sidePanel.playerDisplaySets.get(i);
                    if(gameData.gameBox.players.get(i).healthPoints > 0) {
                        display.updateHealthBar(gameData.gameBox.players.get(i).healthPoints, gameData.sidePanel.playerInputs.get(i).healthPoints);
                    }
                }
            }
        };
        displayLoop.start();
    }

    private void addPlayerField() {
        if (gameData.sidePanel.addInputField()) {
            int playerIndex = playerFieldSets.size();
            PlayerFieldSet fieldSet = new PlayerFieldSet(playerIndex + 1);
            playerFieldSets.add(fieldSet);
            playerTemplatesContainer.getChildren().add(fieldSet.container);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Max players");
            alert.setHeaderText(null);
            alert.setContentText("Maximum number of players reached");
            alert.showAndWait();
        }
    }

    /** Inner class to manage each player's editable form **/
    private static class PlayerFieldSet {
        VBox container;
        Label playerLabel;
        TextField nameField;
        Spinner<Integer> damageSpinner;
        Spinner<Integer> speedSpinner;
        Spinner<Integer> healthSpinner;
        Label sharedPointsLabel;

        public PlayerFieldSet(int playerNumber) {
            createFieldSet(playerNumber);
            setupListeners();
            updateStatLabels();
            updateRemainingPoints();
        }

        private void createFieldSet(int playerNumber) {
            container = new VBox(5);
            container.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");

            playerLabel = new Label("PLAYER " + playerNumber);
            playerLabel.setStyle("-fx-font-weight: bold;");

            nameField = new TextField("PLAYER " + playerNumber);
            nameField.setPromptText("Player's name");

            HBox nameBox = new HBox(5, new Label("Name:"), nameField);

            sharedPointsLabel = new Label();
            sharedPointsLabel.setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");

            damageSpinner = createSpinner(1, CONST.SHARED_POINTS, 1);
            Label damageLabel = new Label("DAMAGE:");
            Label damageValueLabel = new Label();
            HBox damageBox = new HBox(5, damageLabel, damageSpinner, damageValueLabel);

            speedSpinner = createSpinner(0, CONST.SHARED_POINTS, 0);
            Label speedLabel = new Label("SPEED:");
            Label speedValueLabel = new Label();
            HBox speedBox = new HBox(5, speedLabel, speedSpinner, speedValueLabel);

            healthSpinner = createSpinner(1, CONST.SHARED_POINTS, 1);
            Label healthLabel = new Label("HEALTH:");
            Label healthValueLabel = new Label();
            HBox healthBox = new HBox(5, healthLabel, healthSpinner, healthValueLabel);

            container.getChildren().addAll(
                    playerLabel, nameBox, sharedPointsLabel,
                    damageBox, speedBox, healthBox
            );
        }

        private Spinner<Integer> createSpinner(int min, int max, int initial) {
            Spinner<Integer> spinner = new Spinner<>(min, max, initial);
            spinner.setEditable(true);
            spinner.setPrefWidth(80);
            restrictSpinnerToValidInt(spinner);
            return spinner;
        }

        private void setupListeners() {
            damageSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                updateStatLabels();
                updateRemainingPoints();
            });
            speedSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                updateStatLabels();
                updateRemainingPoints();
            });
            healthSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                updateStatLabels();
                updateRemainingPoints();
            });
        }

        private void updateStatLabels() {
            HBox dmgBox = (HBox) container.getChildren().get(3);
            HBox spdBox = (HBox) container.getChildren().get(4);
            HBox hpBox = (HBox) container.getChildren().get(5);

            ((Label) dmgBox.getChildren().get(2)).setText("= " + CONST.damagesFromPoints(damageSpinner.getValue()) + " dmg");
            ((Label) spdBox.getChildren().get(2)).setText("= " + CONST.speedFromPoints(speedSpinner.getValue()) + " pixel/sec");
            ((Label) hpBox.getChildren().get(2)).setText("= " + CONST.healthFromPoints(healthSpinner.getValue()) + " hp");
        }

        private void updateRemainingPoints() {
            int total = damageSpinner.getValue() + speedSpinner.getValue() + healthSpinner.getValue();
            int remaining = CONST.SHARED_POINTS - total;
            sharedPointsLabel.setText("REMAINING POINTS: " + remaining);

            ((SpinnerValueFactory.IntegerSpinnerValueFactory) damageSpinner.getValueFactory()).setMax(damageSpinner.getValue() + remaining);
            ((SpinnerValueFactory.IntegerSpinnerValueFactory) speedSpinner.getValueFactory()).setMax(speedSpinner.getValue() + remaining);
            ((SpinnerValueFactory.IntegerSpinnerValueFactory) healthSpinner.getValueFactory()).setMax(healthSpinner.getValue() + remaining);
        }

        private void restrictSpinnerToValidInt(Spinner<Integer> spinner) {
            SpinnerValueFactory.IntegerSpinnerValueFactory factory = (SpinnerValueFactory.IntegerSpinnerValueFactory) spinner.getValueFactory();
            UnaryOperator<TextFormatter.Change> filter = change -> {
                String newText = change.getControlNewText();
                if (newText.isEmpty()) return change;
                try {
                    int value = Integer.parseInt(newText);
                    if (value >= factory.getMin() && value <= factory.getMax()) {
                        return change;
                    }
                } catch (NumberFormatException ignored) {}
                return null;
            };
            TextFormatter<String> formatter = new TextFormatter<>(filter);
            spinner.getEditor().setTextFormatter(formatter);
            formatter.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.isEmpty()) {
                    try {
                        int value = Integer.parseInt(newVal);
                        spinner.getValueFactory().setValue(value);
                    } catch (NumberFormatException ignored) {}
                }
            });
        }
    }

    /** UI display for live game info **/
    static class PlayerDisplaySet {
        final VBox container;
        final Rectangle healthBarBackground;
        final Rectangle healthBarForeground;
        final Label nameLabel;
        final Label speedLabel;
        final Label damageLabel;

        private final double MAX_BAR_WIDTH = 200;
        private final double BAR_HEIGHT = 20;

        public PlayerDisplaySet(String name, int speed, double damage, double health, int index) {
            healthBarBackground = new Rectangle(MAX_BAR_WIDTH, BAR_HEIGHT);
            healthBarBackground.setFill(Color.LIGHTGRAY);
            healthBarBackground.setArcWidth(10);
            healthBarBackground.setArcHeight(10);

            healthBarForeground = new Rectangle(MAX_BAR_WIDTH, BAR_HEIGHT);
            healthBarForeground.setFill(CONST.colors[index]);
            healthBarForeground.setArcWidth(10);
            healthBarForeground.setArcHeight(10);
            updateHealthBar(health, health);

            StackPane healthBarStack = new StackPane(healthBarBackground, healthBarForeground);
            healthBarStack.setAlignment(Pos.CENTER_LEFT);
            healthBarStack.setMaxWidth(MAX_BAR_WIDTH);

            nameLabel = new Label(name);
            speedLabel = new Label(""+speed+" px/s");
            damageLabel = new Label("" + damage + " dmg/h");

            HBox infoBox = new HBox(10, nameLabel, speedLabel, damageLabel);
            infoBox.setAlignment(Pos.CENTER_LEFT);

            container = new VBox(5, healthBarStack, infoBox);
            container.setPadding(new Insets(10));
            container.setStyle("-fx-border-color: black; -fx-border-radius: 5; -fx-background-color: #f4f4f4;");

        }

        public void updateHealthBar(double currentHealth, double maxHealth) {
            double ratio = Math.max(0, Math.min(1.0, currentHealth / maxHealth));
            double targetWidth;

            if (currentHealth <= 0) { // avoid border effect
                targetWidth = 1;
            } else {
                targetWidth = ratio * MAX_BAR_WIDTH;
            }
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.millis(300),
                            new KeyValue(healthBarForeground.widthProperty(), targetWidth, Interpolator.EASE_BOTH)
                    )
            );
            timeline.play();
        }

    }
}
