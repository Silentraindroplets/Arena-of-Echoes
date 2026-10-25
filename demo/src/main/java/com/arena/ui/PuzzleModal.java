package com.arena.ui;

import com.arena.puzzles.Puzzle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.Random;
import java.util.function.IntConsumer;

/** A modal dialog that appears during enemy turns. Correct = block. */
public class PuzzleModal extends StackPane {

    private final Random rng = new Random();
    private Timeline timer;
    private int timeLeft;

    public PuzzleModal() {
        setStyle("-fx-background-color: rgba(5,7,14,0.90);");
        setVisible(false);
        setPickOnBounds(true);
    }

    /** Show the puzzle. onResult receives 1 for correct, 0 for wrong/timeout. */
    public void show(Puzzle puzzle, boolean timerEnabled, int seconds, IntConsumer onResult) {
        getChildren().clear();
        setVisible(true);

        // ---- Puzzle card ----
        Label banner = new Label("CODE CHALLENGE");
        banner.setStyle("-fx-text-fill: #facc15; -fx-font-size: 14px; -fx-font-weight: bold; -fx-font-family: 'Consolas';");

        Label q = new Label(puzzle.question);
        q.setWrapText(true);
        q.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: 'Consolas';");
        q.setMaxWidth(700);
        q.setAlignment(Pos.CENTER);

        Label hint = new Label("Answer correctly to block the attack");
        hint.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

        VBox headerBox = new VBox(6, banner, q, hint);
        headerBox.setAlignment(Pos.CENTER);

        // ---- Choices ----
        Object[] shuffled = puzzle.shuffled(rng);
        String[] choices = (String[]) shuffled[0];
        int correctIdx = (Integer) shuffled[1];

        VBox choicesBox = new VBox(10);
        choicesBox.setAlignment(Pos.CENTER);
        choicesBox.setPadding(new Insets(20, 0, 10, 0));

        final boolean[] answered = { false };
        Button[] buttons = new Button[choices.length];

        for (int i = 0; i < choices.length; i++) {
            final int idx = i;
            Button b = new Button(choices[i]);
            b.setPrefSize(560, 48);
            b.setStyle(choiceStyle(false, false));
            b.setStyle(choiceStyle(false, false));
            b.setOnAction(e -> {
                if (answered[0]) return;
                answered[0] = true;
                stopTimer();

                boolean correct = idx == correctIdx;
                for (int j = 0; j < buttons.length; j++) {
                    if (j == correctIdx) buttons[j].setStyle(choiceStyle(true, false));
                    else if (j == idx && !correct) buttons[j].setStyle(choiceStyle(false, true));
                }

                Label feedback = new Label(correct ? "Correct!  Attack blocked." : "Wrong answer — the attack lands.");
                feedback.setStyle("-fx-text-fill: " + (correct ? "#4ade80" : "#ef4444") + ";" +
                        "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 0 0 0;");

                Label expl = new Label(puzzle.explanation);
                expl.setWrapText(true);
                expl.setMaxWidth(620);
                expl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px; -fx-font-family: 'Consolas';");

                headerBox.getChildren().addAll(feedback, expl);

                Timeline close = new Timeline(new KeyFrame(Duration.millis(1500), ev -> {
                    setVisible(false);
                    onResult.accept(correct ? 1 : 0);
                }));
                close.play();
            });
            buttons[i] = b;
            choicesBox.getChildren().add(b);
        }

        // ---- Timer ----
        Rectangle timerBar = new Rectangle(560, 5);
        timerBar.setFill(Color.web("#38bdf8"));
        StackPane timerHolder = new StackPane(timerBar);
        timerHolder.setAlignment(Pos.CENTER_LEFT);
        timerHolder.setPrefSize(560, 5);

        if (timerEnabled && seconds > 0) {
            timeLeft = seconds;
            int totalTicks = seconds * 20;
            final int[] ticks = { 0 };
            timer = new Timeline(new KeyFrame(Duration.millis(50), e -> {
                ticks[0]++;
                double ratio = 1.0 - (double) ticks[0] / totalTicks;
                if (ratio <= 0) {
                    timerBar.setWidth(0);
                    timerBar.setFill(Color.web("#ef4444"));
                    stopTimer();
                    if (!answered[0]) {
                        answered[0] = true;
                        Label fb = new Label("Time's up!  The attack lands.");
                        fb.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 0 0 0;");
                        headerBox.getChildren().add(fb);
                        Timeline close = new Timeline(new KeyFrame(Duration.millis(1400), ev -> {
                            setVisible(false);
                            onResult.accept(0);
                        }));
                        close.play();
                    }
                } else {
                    timerBar.setWidth(560 * ratio);
                    if (ratio < 0.3) timerBar.setFill(Color.web("#f59e0b"));
                    if (ratio < 0.15) timerBar.setFill(Color.web("#ef4444"));
                }
            }));
            timer.setCycleCount(totalTicks);
            timer.play();
        } else {
            timerBar.setWidth(0);
        }

        VBox card = new VBox(10, headerBox, timerHolder, choicesBox);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30, 40, 30, 40));
        card.setMaxWidth(700);
        card.setStyle("-fx-background-color: #12172a;" +
                "-fx-border-color: #facc15;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 14;" +
                "-fx-background-radius: 14;" +
                "-fx-effect: dropshadow(gaussian, #000000, 30, 0.4, 0, 6);");

        getChildren().add(card);
    }

    private void stopTimer() {
        if (timer != null) { timer.stop(); timer = null; }
    }

    private String choiceStyle(boolean correct, boolean wrong) {
        String bg = correct ? "rgba(74,222,128,0.20)" : wrong ? "rgba(239,68,68,0.20)" : "rgba(255,255,255,0.03)";
        String border = correct ? "#4ade80" : wrong ? "#ef4444" : "#334155";
        String text = correct ? "#4ade80" : wrong ? "#ef4444" : "#e2e8f0";
        return "-fx-background-color: " + bg + ";" +
               "-fx-border-color: " + border + ";" +
               "-fx-border-width: 1.5;" +
               "-fx-border-radius: 8;" +
               "-fx-background-radius: 8;" +
               "-fx-text-fill: " + text + ";" +
               "-fx-font-size: 14px;" +
               "-fx-font-family: 'Consolas', monospace;" +
               "-fx-font-weight: bold;" +
               "-fx-cursor: hand;";
    }
}