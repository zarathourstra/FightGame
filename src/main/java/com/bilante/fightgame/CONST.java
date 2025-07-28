package com.bilante.fightgame;

import javafx.scene.paint.Color;

public class CONST {
    public static final int MAX_PLAYERS = 5;
    public static final int SHARED_POINTS = 10;
    public static final int PLAYER_RADIUS = 12;
    public static final double[] centerXY = {250, 250};
    public static final double startLineRadius = 75;
    public static final double GAME_WIDTH = 500;
    public static final double GAME_HEIGHT = 500;
    public static final Color[] colors = {
            Color.rgb(35, 199, 0),
            Color.rgb(0, 199, 146),
            Color.rgb(206, 16, 16),
            Color.rgb(118, 10, 122),
            Color.rgb(150, 50, 10)
    };

    /** @param points : the share of points of the given 10 that goes to damage evaluation
     *                we use the rule that follows : there is a minimum of one point on damage
     *                the hit damage on health equals to 1/7 of the damage points.
     *                we round up to 2 digits up
     *  @return damage : the double value a hit produces for the amount of points**/
    public static double damagesFromPoints( int points) {
        if (points >= SHARED_POINTS) { throw new IllegalArgumentException("Exceeding amont of points"); }
        double rawDamages = points / 7.0;
        return Math.ceil(rawDamages * 100 ) / 100.0;
    }

    /**
     * @param points the number of points (out of 10) allocated to speed evaluation.
     *               Speed is computed based on the following rule:
     *               - 0 pts  → speed = 80 px/s (malus)
     *               - 1–3 pts → for each point, add +20 px/s to base speed (100 px/s)
     *               - 4+ pts → first 3 points add +20 px/s each (i.e. +60),
     *                          then each extra point beyond 3 adds +30 px/s.
     * @return the constant speed of movement in px/s
     * @throws IllegalArgumentException if points < 0 or >= SHARED_POINTS
     */
    public static int speedFromPoints(int points) {
        int baseSpeed = 80;

        if (points < 0) { throw new IllegalArgumentException("negative points passed"); }
        if (points >= SHARED_POINTS) { // SHARED_POINTS must be defined somewhere (like 10)
            throw new IllegalArgumentException("Exceeding amount of points");
        }

        if (points == 0) { return 70; }
        if (points <= 3) {
            baseSpeed += 20 * points;
        } else {
            baseSpeed += (20 * 3) + ((points - 3) * 30); // +60 for first 3, then +30 per extra
        }

        return baseSpeed;
    }
    /** @param points the number of points out of 10 allocated to health
     *                  the rule goes as follows : there is a minimum of one point of health
     *                  the points are exactly the amount of health but as a double
     *  @return a double value of the given points**/
    public static double healthFromPoints(int points) {
        if (points > SHARED_POINTS) { throw new IllegalArgumentException("Exceeding amount of points"); }
        return (double) points;
    }

    static class Position {
        double x;
        double y;

        public Position(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() { return x; }
        public double getY() { return y; }
        public void setX(double x) { this.x = x; }
        public void setY(double y) { this.y = y; }
    }
}
