# COLLISION GAME SIMULATOR

A battle royale simulation where circles fight to be the last one standing.

## Overview
Players (represented by circles) move independently in a bounded area. Each player starts with 10 points to allocate across three attributes: health, speed, and damage. The last player alive wins!

## Game Mechanics
- **Point System**: Distribute 10 points among health, speed, and damage stats
- **Combat**: When players collide, whoever attacked most recently takes damage
- **Victory**: Game ends when only one player remains above 0 HP

## Requirements
This is what I used
- Java 21.0.2
- JavaFX 21
- Maven Build

## Quick Start
```bash
mvn clean compile
mvn javafx:run
```
#### Remarks
A lot of validation has been ignored : 
- you can start the game without dispatching all the points.
- access controls on attributes are not reinforced
- the UI is not ocmpletely consistent on the health bar decreasing, but I added the change of color to Black to highlight the death of a player.
- the geometry for movements can be less complex
