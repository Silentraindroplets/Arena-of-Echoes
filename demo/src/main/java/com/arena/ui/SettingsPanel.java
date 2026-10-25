package com.arena.ui;

import com.arena.Settings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** A reusable settings editor with a Back button. */
public class SettingsPanel extends VBox {

    public SettingsPanel(Settings settings, Runnable onBack) {
        setAlignment(Pos.CENTER);
        setSpacing(18);
        setPadding(new Insets(30));

        Label title = new Label("SETTINGS");
        title.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 34px; -fx-font-weight: bold; -fx-font-family: 'Georgia';");

        VBox diffRow    = row("Difficulty",  settings, Settings.Difficulty.values(),
                () -> settings.difficulty.ordinal(), v -> settings.difficulty = Settings.Difficulty.values()[v]);
        VBox puzzleRow  = row("Puzzles",     settings, Settings.PuzzleMode.values(),
                () -> settings.puzzleMode.ordinal(), v -> settings.puzzleMode = Settings.PuzzleMode.values()[v]);
        VBox timerRow   = toggleRow("Puzzle Timer", settings.puzzleTimer, v -> settings.puzzleTimer = v);

        Button back = new Button("Back");
        back.setPrefSize(220, 48);
        back.setStyle(btnStyle("#7c5cff", false));
        back.setOnMouseEntered(e -> back.setStyle(btnStyle("#7c5cff", true)));
        back.setOnMouseExited(e  -> back.setStyle(btnStyle("#7c5cff", false)));
        back.setOnAction(e -> onBack.run());

        getChildren().addAll(title, diffRow, puzzleRow, timerRow, back);
    }

    private <E extends Enum<E>> VBox row(String label, Settings s, E[] values,
                                         java.util.function.IntSupplier getter,
                                         java.util.function.IntConsumer setter) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px; -fx-font-weight: bold;");
        lbl.setMinWidth(110);

        HBox buttons = new HBox(8);
        buttons.setAlignment(Pos.CENTER_LEFT);

        Button[] btns = new Button[values.length];
        for (int i = 0; i < values.length; i++) {
            final int idx = i;
            Button b = new Button(values[i].name());
            b.setPrefWidth(130);
            b.setPrefHeight(38);
            b.setOnAction(e -> {
                setter.accept(idx);
                for (int j = 0; j < btns.length; j++) {
                    btns[j].setStyle(btnStyle("#7c5cff", j == idx));
                }
            });
            btns[i] = b;
            buttons.getChildren().add(b);
        }
        for (int j = 0; j < btns.length; j++) {
            btns[j].setStyle(btnStyle("#7c5cff", j == getter.getAsInt()));
        }
        HBox full = new HBox(14, lbl, buttons);
        full.setAlignment(Pos.CENTER_LEFT);
        VBox v = new VBox(full);
        return v;
    }

    private VBox toggleRow(String label, boolean initial, java.util.function.Consumer<Boolean> onChange) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px; -fx-font-weight: bold;");
        lbl.setMinWidth(110);

        Button on  = new Button("On");
        Button off = new Button("Off");
        on.setPrefSize(130, 38);
        off.setPrefSize(130, 38);

        final boolean[] state = { initial };
        Runnable refresh = () -> {
            on .setStyle(btnStyle("#7c5cff", state[0]));
            off.setStyle(btnStyle("#7c5cff", !state[0]));
        };
        on .setOnAction(e -> { state[0] = true;  onChange.accept(true);  refresh.run(); });
        off.setOnAction(e -> { state[0] = false; onChange.accept(false); refresh.run(); });
        refresh.run();

        HBox full = new HBox(14, lbl, on, off);
        full.setAlignment(Pos.CENTER_LEFT);
        return new VBox(full);
    }

    private String btnStyle(String c, boolean active) {
        return "-fx-background-color: " + (active ? c : "rgba(255,255,255,0.02)") + ";" +
               "-fx-border-color: " + c + ";" +
               "-fx-border-width: 1.5;" +
               "-fx-border-radius: 8;" +
               "-fx-background-radius: 8;" +
               "-fx-text-fill: " + (active ? "#0a0c13" : c) + ";" +
               "-fx-font-size: 12px;" +
               "-fx-font-weight: bold;" +
               "-fx-cursor: hand;";
    }
}