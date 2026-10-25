package com.arena.puzzles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Puzzle {
    public final String question;
    public final String correct;
    public final String[] wrong;
    public final String explanation;
    public final int difficulty;

    public Puzzle(String question, String correct, String[] wrong, String explanation, int difficulty) {
        this.question = question;
        this.correct = correct;
        this.wrong = wrong;
        this.explanation = explanation;
        this.difficulty = difficulty;
    }

    /** Returns [String[] choices, Integer correctIndex], shuffled. */
    public Object[] shuffled(Random rng) {
        List<String> all = new ArrayList<>();
        all.add(correct);
        Collections.addAll(all, wrong);
        Collections.shuffle(all, rng);
        int idx = all.indexOf(correct);
        return new Object[]{ all.toArray(new String[0]), idx };
    }
}