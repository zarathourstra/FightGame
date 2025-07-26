package com.bilante.fightgame;

import java.util.ArrayList;
import java.util.List;

public class SidePanel {

    int nbPlayers = 0;
    List<PlayerInput> playerInputs = new ArrayList<PlayerInput>();
    List<playerInfoDisplay> displays = new ArrayList<playerInfoDisplay>();

    public SidePanel() {}

    public boolean addInputField() {
        if (nbPlayers == CONST.MAX_PLAYERS) return false;
        playerInputs.add(new PlayerInput()); nbPlayers = playerInputs.size();
        return true;
    }

    public void removeInputField( int index) {
        playerInputs.remove(index); nbPlayers = playerInputs.size();
    }

    /** @class represents the input fields to initialize a player object later on**/
    static class PlayerInput {
            int sharedPoints = CONST.SHARED_POINTS;
            int damagePoints;
            int speedPoints;
            int healthPoints;
            String name;

            public PlayerInput() {
                sharedPoints -= 2;
                damagePoints = 1;
                speedPoints = 0;
                healthPoints = 1;
                name = null;
            }
            public PlayerInput(int damagePoints, int speedPoints, int healthPoints) {
                if ( damagePoints + speedPoints + healthPoints > CONST.SHARED_POINTS ) {
                    throw new IllegalArgumentException("Exceeding amont of points");
                }
                this.damagePoints = damagePoints;
                this.speedPoints = speedPoints;
                this.healthPoints = healthPoints;
            }

            public void setDamagePoints(int damagePoints) { this.sharedPoints -= damagePoints ;this.damagePoints = damagePoints;}
        public void setSpeedPoints(int speedPoints) { this.sharedPoints -= speedPoints ;this.speedPoints = speedPoints;}
        public void setHealthPoints(int healthPoints) {this.sharedPoints -= healthPoints;this.healthPoints = healthPoints;}
        public void setName(String name) {this.name = name;}
    }


    static class playerInfoDisplay {
        final String name;
        final double damage;
        final int speed;
        double health;

        public playerInfoDisplay ( PlayerInput playerInput ) {

            this.damage = CONST.damagesFromPoints(playerInput.damagePoints);
            this.speed = CONST.speedFromPoints(playerInput.speedPoints);
            this.health = CONST.healthFromPoints(playerInput.healthPoints);
            this.name = playerInput.name;
        }

        public void setHealth(double health) {
            this.health = health;
        }
    }

}
