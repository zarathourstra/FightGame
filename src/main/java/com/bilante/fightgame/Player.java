package com.bilante.fightgame;


import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.sql.Timestamp;

public class Player {
    String name;
    double healthPoints;
    final double damage;
    final int speed;
    Position current;
    Position target;
    Timestamp lastHit = new Timestamp(System.currentTimeMillis());
    boolean dead = false;

    public Player(String name, double healthPoints, double damage, int speed,  Position current, Position target) {
        this.name = name;
        this.healthPoints = healthPoints;
        this.damage = damage;
        this.speed = speed;
        this.current = current;
        this.target = target;
    }


    public void setCurrent(Position current) {
        this.current = current;
    }
    public void setTarget(Position target) {
        this.target = target;
    }
    public void setHealthPoints(double healthPoints) {
        this.healthPoints = healthPoints;
        if (this.healthPoints <= 0) {
            dead = true;
        }
    }

}
