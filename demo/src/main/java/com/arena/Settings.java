package com.arena;

public class Settings {

    public enum Difficulty { EASY, NORMAL, HARD }
    public enum PuzzleMode { ALWAYS, OFTEN, OFF }

    public Difficulty difficulty  = Difficulty.NORMAL;
    public PuzzleMode puzzleMode  = PuzzleMode.ALWAYS;
    public boolean    puzzleTimer = true;
    public int        puzzleTimeSeconds = 20;

    public double enemyScale() {
        return switch (difficulty) { case EASY -> 0.78; case HARD -> 1.22; default -> 1.0; };
    }
    public double xpScale() {
        return switch (difficulty) { case EASY -> 0.85; case HARD -> 1.15; default -> 1.0; };
    }
    public boolean shouldTriggerPuzzle(java.util.Random rng) {
        return switch (puzzleMode) {
            case ALWAYS -> true;
            case OFTEN  -> rng.nextDouble() < 0.6;
            case OFF    -> false;
        };
    }
}