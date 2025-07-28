package com.bilante.fightgame;

import java.util.ArrayList;
import java.util.List;

public class GameBox {
    long lastUpdateTime = System.currentTimeMillis();
    List<Player> players;
    private static final double MIN_TARGET_DISTANCE = 2.0; // Minimum distance to prevent getting stuck

    public GameBox() {
        players = new ArrayList<>();
    }

    public void updatePlayers() {
        long now = System.currentTimeMillis();
        double deltaTime = (now - lastUpdateTime) / 1000.0;
        lastUpdateTime = now;
        List<Position> leadVectors = new ArrayList<>();
        List<Position> intendedPositions = new ArrayList<>();

        for (Player player : players) {
            Position leadVector = getLeadVector(player);
            Position intended = calculateNextStep(player, leadVector, deltaTime);

            leadVectors.add(leadVector);
            intendedPositions.add(intended);
        }

        List<CollisionResult> collisionResults = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            Position leadVector = leadVectors.get(i);
            Position intended = intendedPositions.get(i);

            CollisionResult collision = checkCollisions(player.current, intended, i, intendedPositions);
            collisionResults.add(collision);
        }
        for (int i = 0; i < players.size(); i++) {
            CollisionResult collision = collisionResults.get(i);
            Position leadVector = leadVectors.get(i);
            Position intended = intendedPositions.get(i);
            Player player = players.get(i);
            if (collision.hasCollision) {
                player.current = collision.collisionPoint;
                player.target = computeNextTarget(leadVector, collision);
                if (collision.otherPlayerIndex != -1) {
                    if (player.lastHit.compareTo(players.get(collision.otherPlayerIndex).lastHit) < 0 && !players.get(collision.otherPlayerIndex).dead) {
                        player.setHealthPoints(player.healthPoints - players.get(collision.otherPlayerIndex).damage);
                    }
                }
                player.lastHit.setTime(now);
            } else {
                player.current = intended; // Only update if no collision
            }
        }
    }

    private Position getLeadVector(Player player) {
        double dx = player.target.x - player.current.x;
        double dy = player.target.y - player.current.y;
        return new Position(dx, dy);
    }

    private Position calculateNextStep(Player player, Position leadVector, double deltaTime) {
        double distance = Math.sqrt(leadVector.x * leadVector.x + leadVector.y * leadVector.y);

        // If very close to target, generate a new random target to prevent getting stuck
        if (distance < MIN_TARGET_DISTANCE) {
            player.target = generateRandomTarget();
            // Recalculate with new target
            double newDx = player.target.x - player.current.x;
            double newDy = player.target.y - player.current.y;
            distance = Math.sqrt(newDx * newDx + newDy * newDy);
            leadVector = new Position(newDx, newDy);
        }

        if (distance == 0) {
            return new Position(player.current.x, player.current.y);
        }

        double step = player.speed * deltaTime;
        double ratio = Math.min(step / distance, 1.0); // Don't overshoot target

        return new Position(
                player.current.x + leadVector.x * ratio,
                player.current.y + leadVector.y * ratio
        );
    }

    private Position generateRandomTarget() {
        double radius = CONST.PLAYER_RADIUS;
        double minX = radius;
        double maxX = CONST.GAME_WIDTH - radius;
        double minY = radius;
        double maxY = CONST.GAME_HEIGHT - radius;

        double x = minX + Math.random() * (maxX - minX);
        double y = minY + Math.random() * (maxY - minY);

        return new Position(x, y);
    }

    private CollisionResult checkCollisions(Position current, Position intended, int playerIndex, List<Position> allIntendedPositions) {
        // Check wall collisions first
        CollisionResult wallCollision = checkWallCollision(intended);
        if (wallCollision.hasCollision) {
            return wallCollision;
        }
        // Check player collisions
        CollisionResult playerCollision = checkPlayerCollisions(current, intended, playerIndex, allIntendedPositions);

        return playerCollision;
    }

    private CollisionResult checkWallCollision(Position intended) {
        double radius = CONST.PLAYER_RADIUS;
        Position collisionPoint = new Position(intended.x, intended.y); // Start with intended position
        boolean collision = false;

        if (intended.x + radius >= CONST.GAME_WIDTH) { // right wall
            collision = true;
            collisionPoint.x = CONST.GAME_WIDTH - radius;
        }
        if (intended.y + radius >= CONST.GAME_HEIGHT) { // top wall
            collision = true;
            collisionPoint.y = CONST.GAME_HEIGHT - radius;
        }
        if (intended.x - radius <= 0) { // left wall
            collision = true;
            collisionPoint.x = radius;
        }
        if (intended.y - radius <= 0) { // bottom wall
            collision = true;
            collisionPoint.y = radius;
        }

        return new CollisionResult(collision, collisionPoint, -1);
    }

    private CollisionResult checkPlayerCollisions(Position current, Position intended, int playerIndex, List<Position> allIntendedPositions) {
        double radius = CONST.PLAYER_RADIUS;
        double minSquaredDistance = radius * radius * 4;

        for (int i = 0; i < allIntendedPositions.size(); i++) {
            if (i == playerIndex) continue;

            Position otherIntended = allIntendedPositions.get(i);

            // Calculate distance between intended positions
            double dx = intended.x - otherIntended.x;
            double dy = intended.y - otherIntended.y;
            double squaredDistance = dx * dx + dy * dy;

            if (squaredDistance < minSquaredDistance) {
                // Find collision point along the movement path
                Position collisionPoint = findCollisionPoint(current, intended, players.get(i).current, otherIntended, radius);
                return new CollisionResult(true, collisionPoint, i);
            }
        }

        return new CollisionResult(false, intended, -1);
    }

    private Position findCollisionPoint(Position p1Start, Position p1End, Position p2Start, Position p2End, double radius) {
        double dx = p1End.x - p2End.x;
        double dy = p1End.y - p2End.y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance == 0) {
            return new Position(p1Start.x, p1Start.y);
        }
        // Move back from intended position to collision boundary
        double separation = ((radius * 2) - distance) / 2;
        double normalX = dx / distance;
        double normalY = dy / distance;

        return new Position(
                p1End.x + normalX * separation,
                p1End.y + normalY * separation
        );
    }

    public Position computeNextTarget(Position oldLead, CollisionResult collision) {
        if (collision.otherPlayerIndex == -1) {
            // Wall collision - determine which wall and reflect
            return handleWallReflection(oldLead, collision.collisionPoint);
        } else {
            // Player collision - reflect using collision normal
            return handlePlayerReflection(oldLead, collision);
        }
    }

    private Position handleWallReflection(Position oldLead, Position collisionPoint) {
        double radius = CONST.PLAYER_RADIUS;
        Position reflectedLead = new Position(oldLead.x, oldLead.y);

        // Determine which wall(s) we hit based on collision point
        boolean hitLeft = Math.abs(collisionPoint.x - radius) < 0.1;
        boolean hitRight = Math.abs(collisionPoint.x - (CONST.GAME_WIDTH - radius)) < 0.1;
        boolean hitTop = Math.abs(collisionPoint.y - (CONST.GAME_HEIGHT - radius)) < 0.1;
        boolean hitBottom = Math.abs(collisionPoint.y - radius) < 0.1;

        // Reflect the lead vector based on which wall(s) were hit
        if (hitLeft || hitRight) {
            reflectedLead.x = -reflectedLead.x; // Reflect horizontally
        }
        if (hitTop || hitBottom) {
            reflectedLead.y = -reflectedLead.y; // Reflect vertically
        }

        // Find a target point on the opposite wall
        Position target = findTargetOnWall(collisionPoint, reflectedLead);

        // Ensure target is far enough away to prevent immediate stopping
        return ensureMinimumDistance(collisionPoint, target);
    }

    private Position handlePlayerReflection(Position oldLead, CollisionResult collision) {
        // Get the other player's position
        Player otherPlayer = players.get(collision.otherPlayerIndex);

        // Calculate normal vector from other player to collision point
        double dx = collision.collisionPoint.x - otherPlayer.current.x;
        double dy = collision.collisionPoint.y - otherPlayer.current.y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance == 0) {
            // Fallback: use a perpendicular direction to old lead
            dx = -oldLead.y;
            dy = oldLead.x;
            distance = Math.sqrt(dx * dx + dy * dy);
            if (distance == 0) {
                // Ultimate fallback: random direction
                dx = Math.random() - 0.5;
                dy = Math.random() - 0.5;
                distance = Math.sqrt(dx * dx + dy * dy);
            }
        }

        // Normalize the collision normal
        double normalX = dx / distance;
        double normalY = dy / distance;

        // Reflect the old lead vector using the collision normal
        // Reflection formula: reflected = incident - 2 * (incident · normal) * normal
        double dotProduct = oldLead.x * normalX + oldLead.y * normalY;
        Position reflectedLead = new Position(
                oldLead.x - 2 * dotProduct * normalX,
                oldLead.y - 2 * dotProduct * normalY
        );

        // Find a target point on a wall using the reflected direction
        Position target = findTargetOnWall(collision.collisionPoint, reflectedLead);

        // Ensure target is far enough away to prevent immediate stopping
        return ensureMinimumDistance(collision.collisionPoint, target);
    }

    private Position ensureMinimumDistance(Position from, Position to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < MIN_TARGET_DISTANCE) {
            // If target is too close, extend it to minimum distance
            if (distance == 0) {
                // Generate random direction if no direction exists
                return generateRandomTarget();
            }

            double factor = MIN_TARGET_DISTANCE / distance;
            double newX = from.x + dx * factor;
            double newY = from.y + dy * factor;

            // Clamp to game bounds
            double radius = CONST.PLAYER_RADIUS;
            newX = Math.max(radius, Math.min(CONST.GAME_WIDTH - radius, newX));
            newY = Math.max(radius, Math.min(CONST.GAME_HEIGHT - radius, newY));

            return new Position(newX, newY);
        }

        return to;
    }

    private Position findTargetOnWall(Position startPoint, Position direction) {
        double radius = CONST.PLAYER_RADIUS;

        // Normalize direction
        double distance = Math.sqrt(direction.x * direction.x + direction.y * direction.y);
        if (distance == 0) {
            // Fallback to a random direction
            return generateRandomTarget();
        }

        double dirX = direction.x / distance;
        double dirY = direction.y / distance;

        // Calculate intersection with each wall
        double minT = Double.MAX_VALUE;
        Position targetPoint = null;

        // Right wall (x = GAME_WIDTH - radius)
        if (dirX > 0) {
            double t = (CONST.GAME_WIDTH - radius - startPoint.x) / dirX;
            if (t > 0) {
                double y = startPoint.y + t * dirY;
                if (y >= radius && y <= CONST.GAME_HEIGHT - radius && t < minT) {
                    minT = t;
                    targetPoint = new Position(CONST.GAME_WIDTH - radius, y);
                }
            }
        }

        // Left wall (x = radius)
        if (dirX < 0) {
            double t = (radius - startPoint.x) / dirX;
            if (t > 0) {
                double y = startPoint.y + t * dirY;
                if (y >= radius && y <= CONST.GAME_HEIGHT - radius && t < minT) {
                    minT = t;
                    targetPoint = new Position(radius, y);
                }
            }
        }

        // Top wall (y = GAME_HEIGHT - radius)
        if (dirY > 0) {
            double t = (CONST.GAME_HEIGHT - radius - startPoint.y) / dirY;
            if (t > 0) {
                double x = startPoint.x + t * dirX;
                if (x >= radius && x <= CONST.GAME_WIDTH - radius && t < minT) {
                    minT = t;
                    targetPoint = new Position(x, CONST.GAME_HEIGHT - radius);
                }
            }
        }

        // Bottom wall (y = radius)
        if (dirY < 0) {
            double t = (radius - startPoint.y) / dirY;
            if (t > 0) {
                double x = startPoint.x + t * dirX;
                if (x >= radius && x <= CONST.GAME_WIDTH - radius && t < minT) {
                    minT = t;
                    targetPoint = new Position(x, radius);
                }
            }
        }

        // If no valid intersection found, generate random target
        if (targetPoint == null) {
            targetPoint = generateRandomTarget();
        }

        return targetPoint;
    }

    /**
     * @param otherPlayerIndex -1 if wall collision, index in `players` if not
     */
    private record CollisionResult(boolean hasCollision, Position collisionPoint, int otherPlayerIndex) { }
}