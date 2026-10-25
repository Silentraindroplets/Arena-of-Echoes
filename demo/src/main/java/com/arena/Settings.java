package com.arena;

public class Settings {

    public enum Difficulty { EASY, NORMAL, HARD }

    public Difficulty difficulty = Difficulty.NORMAL;

    public double enemyScale() {
        return switch (difficulty) { case EASY -> 0.78; case HARD -> 1.22; default -> 1.0; };
    }
    public double xpScale() {
        return switch (difficulty) { case EASY -> 0.85; case HARD -> 1.15; default -> 1.0; };
    }

    public boolean tutorialSeen = false;
}