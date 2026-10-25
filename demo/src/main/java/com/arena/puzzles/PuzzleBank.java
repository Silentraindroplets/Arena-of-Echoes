package com.arena.puzzles;

import com.arena.Settings;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PuzzleBank {

    private static final List<Puzzle> EASY   = new ArrayList<>();
    private static final List<Puzzle> MEDIUM = new ArrayList<>();
    private static final List<Puzzle> HARD   = new ArrayList<>();

    static {
        // ---------- EASY ----------
        EASY.add(new Puzzle("What is the value of 2 + 3 * 4?",
                "14", new String[]{"20", "24", "9"}, "Multiplication runs before addition.", 1));
        EASY.add(new Puzzle("Which of these is a valid variable name?",
                "myVar", new String[]{"2var", "my-var", "class"}, "Variable names start with a letter or underscore.", 1));
        EASY.add(new Puzzle("What does 10 % 3 evaluate to?",
                "1", new String[]{"3", "0", "3.33"}, "% gives the remainder.", 1));
        EASY.add(new Puzzle("What is the result of  true AND false ?",
                "false", new String[]{"true", "null", "error"}, "AND is true only when both sides are true.", 1));
        EASY.add(new Puzzle("How many times does  for (i = 0; i < 5; i++)  run?",
                "5", new String[]{"4", "6", "0"}, "It runs for i = 0, 1, 2, 3, 4.", 1));
        EASY.add(new Puzzle("What is 5 / 2 in integer division?",
                "2", new String[]{"2.5", "3", "1"}, "Integer division drops the fractional part.", 1));
        EASY.add(new Puzzle("Which symbol checks equality in most languages?",
                "==", new String[]{"=", "!=", "<>"}, "== compares; = assigns.", 1));
        EASY.add(new Puzzle("What does  print(\"hi\" + \"there\")  output?",
                "hithere", new String[]{"hi there", "hi+there", "error"}, "+ concatenates strings.", 1));

        // ---------- MEDIUM ----------
        MEDIUM.add(new Puzzle("What is 2 ** 3 ?",
                "8", new String[]{"6", "9", "23"}, "** is exponentiation: 2 × 2 × 2.", 2));
        MEDIUM.add(new Puzzle("What is the length of  [1, 2, 3, 4] ?",
                "4", new String[]{"3", "5", "1"}, "There are four elements.", 2));
        MEDIUM.add(new Puzzle("What does  !true || false  evaluate to?",
                "false", new String[]{"true", "null", "error"}, "!true is false; false || false is false.", 2));
        MEDIUM.add(new Puzzle("What does  7 // 2  evaluate to (floor division)?",
                "3", new String[]{"3.5", "4", "2"}, "Floor division rounds down.", 2));
        MEDIUM.add(new Puzzle("What does this output?   x = 5; x += 3; print(x)  ",
                "8", new String[]{"5", "3", "53"}, "+= adds to the existing value.", 2));
        MEDIUM.add(new Puzzle("For array  [10, 20, 30] , what is  arr[1] ?",
                "20", new String[]{"10", "30", "index error"}, "Arrays are 0-indexed.", 2));
        MEDIUM.add(new Puzzle("What does  (5 > 3) && (2 < 4)  evaluate to?",
                "true", new String[]{"false", "5", "error"}, "Both comparisons are true.", 2));

        // ---------- HARD ----------
        HARD.add(new Puzzle("For  f(n) = n <= 1 ? 1 : n * f(n-1) , what is  f(4) ?",
                "24", new String[]{"4", "16", "120"}, "This is factorial: 4! = 24.", 3));
        HARD.add(new Puzzle("What does  0.1 + 0.2 == 0.3  return in most languages?",
                "false", new String[]{"true", "0.3", "error"}, "Floating-point rounding makes them unequal.", 3));
        HARD.add(new Puzzle("In JavaScript, what is  [1, 2, 3].map(x => x * 2) ?",
                "[2, 4, 6]", new String[]{"[1, 2, 3]", "6", "[3, 6, 9]"}, "map applies the function to each item.", 3));
        HARD.add(new Puzzle("What is the time complexity of binary search?",
                "O(log n)", new String[]{"O(n)", "O(n log n)", "O(1)"}, "The search space halves each step.", 3));
        HARD.add(new Puzzle("In Python, what does  len(set([1,1,2,3,3]))  output?",
                "3", new String[]{"5", "2", "4"}, "set removes duplicates.", 3));
    }

    public static Puzzle random(Settings.Difficulty diff, Random rng) {
        List<Puzzle> pool = new ArrayList<>();
        switch (diff) {
            case EASY   -> pool.addAll(EASY);
            case NORMAL -> { pool.addAll(EASY); pool.addAll(MEDIUM); }
            case HARD   -> { pool.addAll(MEDIUM); pool.addAll(HARD); }
        }
        return pool.get(rng.nextInt(pool.size()));
    }
}