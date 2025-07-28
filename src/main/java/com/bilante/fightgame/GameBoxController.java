package com.bilante.fightgame;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import sun.misc.Signal;

import java.util.ArrayList;
import java.util.List;

public class GameBoxController {

    @FXML
    private Pane gameBox;

    @FXML
    private Circle startLine;

    private AnimationTimer gameLoop;
    private AnimationTimer gameEndLoop;
    private GameData gameData;
    private List<PlayerUI> playerUIs;
    private static int nbPlayers = 0;

    public void setModel(GameData gameData) {
        this.gameData = gameData;
        gameData.dataReady.addListener((observable, oldValue, newValue) -> {
            if (oldValue == false && newValue == true) {
                quickStart(gameData.sidePanel.displays);
            }
            if (oldValue == true && newValue == false) {
                endGame();
            }
        });
    }

    public void gameOverLoop(){
        gameEndLoop = new AnimationTimer() {
            @Override
            public void handle(long l) {
                List<Double> HPs = new ArrayList<>();
                for (Player player : gameData.gameBox.players) {
                    if (player.healthPoints > 0) HPs.add(player.healthPoints);
                }
                if (HPs.size() < 2 ) {
                    gameData.dataReady.set(false); // acts like a switch for the gameController to identify an endGame
                }
            }
        };
        gameEndLoop.start();
    }

    private void endGame() {
        gameLoop.stop();
        if (gameEndLoop != null) gameEndLoop.stop();

        for (PlayerUI ui : playerUIs) {
            gameBox.getChildren().remove(ui.getNode());
        }

        String winner = winner().name;
        gameData.gameBox.players.clear();
        playerUIs.clear();
        nbPlayers = 0;

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("GAME ENDED");
            alert.setHeaderText(null);
            alert.setContentText(winner +" wins!. Click OK to start over");
            alert.show();
        });
    }



    private Player winner() {
        for (Player player :gameData.gameBox.players) {
            if(player.healthPoints > 0) return player;
        }
        return null;
    }

    public void quickStart(List<SidePanel.PlayerInfoDisplay> playersData) {
        playerUIs = new ArrayList<>();

        for (SidePanel.PlayerInfoDisplay playerData : playersData) {
            Player p = new Player(
                    playerData.name,
                    playerData.health,
                    playerData.damage,
                    playerData.speed,
                    new Position(CONST.centerXY[0], CONST.centerXY[1]),
                    new Position(CONST.centerXY[0], CONST.centerXY[1])
            );
            PlayerUI playerUI = new PlayerUI(p);

            gameData.gameBox.players.add(p);
            playerUIs.add(playerUI);
            gameBox.getChildren().add(playerUI.getNode());
        }
        positionPlayersUIOnStart();
        startGameLoop();
        gameOverLoop();
    }

    public void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateGame();
            }
        };
        gameLoop.start();
    }

    public void positionPlayersUIOnStart () {
        List<Position> positionsAtStart = new ArrayList<>(getCirclePoints(nbPlayers, CONST.startLineRadius, CONST.centerXY[0], CONST.centerXY[1]));
        List<Position> targetsAtStart = new ArrayList<>(computeEdgeTargetsFromCenter(250, 250, 500, nbPlayers));
        for ( int i = 0; i < nbPlayers; i++ ) {
            gameData.gameBox.players.get(i).setCurrent(positionsAtStart.get(i));
            gameData.gameBox.players.get(i).setTarget(targetsAtStart.get(i));
            playerUIs.get(i).updatePositionFromModel();
        }
    }

    public List<Position> getCirclePoints(int n, double r, double cx, double cy) {
        List<Position> points = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n;
            double x = cx + r * Math.cos(angle);
            double y = cy + r * Math.sin(angle);
            points.add(new Position(x, y));
        }
        return points;
    }

    public static List<Position> computeEdgeTargetsFromCenter(double cx, double cy, double boxSize, int numPlayers) {
        List<Position> targets = new ArrayList<>();

        for (int i = 0; i < numPlayers; i++) {
            double angle = 2 * Math.PI * i / numPlayers;

            double dirX = Math.cos(angle);
            double dirY = Math.sin(angle);

            double tMaxX = Double.POSITIVE_INFINITY;
            double tMaxY = Double.POSITIVE_INFINITY;

            if (dirX != 0) {
                tMaxX = (dirX > 0) ? (boxSize - cx) / dirX : (0 - cx) / dirX;
            }

            if (dirY != 0) {
                tMaxY = (dirY > 0) ? (boxSize - cy) / dirY : (0 - cy) / dirY;
            }

            double t = Math.min(tMaxX, tMaxY);

            double x = cx + dirX * t;
            double y = cy + dirY * t;

            // Clamp to box bounds in case of precision error
            x = Math.max(0, Math.min(boxSize, x));
            y = Math.max(0, Math.min(boxSize, y));

            targets.add(new Position(x, y));
        }

        return targets;
    }




    public void updateGame() {
        gameData.gameBox.updatePlayers();

        // Update UI
        for (int i = 0; i < gameData.gameBox.players.size(); i++) {
            playerUIs.get(i).updatePositionFromModel();
            Player p = gameData.gameBox.players.get(i);
            if (p.healthPoints <= 0) {
                gameData.sidePanel.playerDisplaySets.get(i).healthBarForeground.setFill(Color.rgb(0,0,0));
            }
        }

    }

    private static class PlayerUI {
        private static final Color[] colors = {
                Color.rgb(35, 199, 0),
                Color.rgb(0, 199, 146),
                Color.rgb(206, 16, 16),
                Color.rgb(118, 10, 122),
                Color.rgb(150, 50, 10)
        };

        private final Player player;
        private final Circle playerBall;

        public PlayerUI(Player p) {
            this.player = p;
            this.playerBall = new Circle(CONST.PLAYER_RADIUS, colors[nbPlayers % colors.length]);
            updatePositionFromModel();
            nbPlayers++;
        }

        public Circle getNode() {
            return playerBall;
        }

        public void updatePositionFromModel() {
            playerBall.setCenterX(player.current.x);
            playerBall.setCenterY(player.current.y);
        }
    }
}