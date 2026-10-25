package com.arena.ui;

import com.arena.Settings;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Full-screen pause menu with Resume / Settings / Leave. */
public class PauseMenu extends StackPane {

    private final Settings settings;
    private final VBox mainBox;
    private final VBox settingsBox;

    public PauseMenu(Settings settings, Runnable onResume, Runnable onLeave) {
        this.settings = settings;

        setStyle("-fx-background-color: rgba(5,7,14,0.86);");
        setVisible(false);
        setPickOnBounds(true);

        Label title = new Label("PAUSED");
        title.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 48px; -fx-font-weight: bold; -fx-font-family: 'Georgia';");

        Button resume   = makeBtn("Resume",        "#4ade80");
        Button setts    = makeBtn("Settings",      "#7c5cff");
        Button leave    = makeBtn("Leave to Menu", "#ef4444");

        resume.setOnAction(e -> { hide(); if (onResume != null) onResume.run(); });
        leave .setOnAction(e -> { hide(); if (onLeave  != null) onLeave.run(); });
        setts .setOnAction(e -> showSettings());

        mainBox = new VBox(16, title, resume, setts, leave);
        mainBox.setAlignment(Pos.CENTER);

        settingsBox = new VBox();
        settingsBox.setAlignment(Pos.CENTER);

        getChildren().add(mainBox);
    }

    private void showSettings() {
        getChildren().clear();
        settingsBox.getChildren().clear();
        settingsBox.getChildren().add(new SettingsPanel(settings, () -> {
            getChildren().clear();
            getChildren().add(mainBox);
        }));
        getChildren().add(settingsBox);
    }

    private Button makeBtn(String text, String color) {
        Button b = new Button(text);
        b.setPrefSize(280, 56);
        b.setStyle(btnStyle(color, false));
        b.setOnMouseEntered(e -> b.setStyle(btnStyle(color, true)));
        b.setOnMouseExited(e  -> b.setStyle(btnStyle(color, false)));
        return b;
    }

    private String btnStyle(String c, boolean hover) {
        return "-fx-background-color: " + (hover ? c : "rgba(255,255,255,0.03)") + ";" +
               "-fx-border-color: " + c + ";" +
               "-fx-border-width: 2;" +
               "-fx-border-radius: 10;" +
               "-fx-background-radius: 10;" +
               "-fx-text-fill: " + (hover ? "#0a0c13" : c) + ";" +
               "-fx-font-size: 16px;" +
               "-fx-font-weight: bold;" +
               "-fx-font-family: 'Georgia';" +
               "-fx-cursor: hand;";
    }

    public void hide() { setVisible(false); getChildren().clear(); getChildren().add(mainBox); }

    public Region spacer() { Region r = new Region(); return r; }
}