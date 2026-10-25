package com.arena;

import com.arena.ui.SettingsPanel;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class MenuScreen extends StackPane {

    public MenuScreen(Game game) {
        setStyle("-fx-background-color: linear-gradient(to bottom, #0a0c13, #1a0e2e, #0a0c13);");

        Label title = new Label("ARENA OF ECHOES");
        title.setStyle("-fx-text-fill: #c4b5fd; -fx-font-size: 62px; -fx-font-weight: bold; -fx-font-family: 'Georgia';");
        title.setEffect(new DropShadow(30, Color.web("#7c5cff")));

        Label subtitle = new Label("A Turn-Based RPG  ·  Defend by Coding");
        subtitle.setStyle("-fx-text-fill: #64748b; -fx-font-size: 16px; -fx-font-family: 'Georgia';");

        Button single = makeBtn("Single Player",    "#7c5cff");
        Button coop   = makeBtn("Local Co-op · 2P", "#38bdf8");
        Button codex  = makeBtn("Codex",            "#a78bfa");
        Button sett   = makeBtn("Settings",         "#f59e0b");
        Button quit   = makeBtn("Quit",             "#ef4444");

        single.setOnAction(e -> game.startBattleWithIntro(1));
        coop  .setOnAction(e -> game.startBattleWithIntro(2));
        codex .setOnAction(e -> game.showCodex());
        sett  .setOnAction(e -> showSettings(game));
        quit  .setOnAction(e -> System.exit(0));

        VBox buttons = new VBox(12, single, coop, codex, sett, quit);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(30, 0, 0, 0));

        Label tip = new Label("Tip: solve the coding puzzles to block enemy attacks. The Codex records every chapter you unlock.");
        tip.setStyle("-fx-text-fill: #475569; -fx-font-size: 11.5px; -fx-font-style: italic;");

        VBox content = new VBox(10, title, subtitle, buttons, tip);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(0, 60, 0, 60));

        getChildren().add(content);
    }

    private void showSettings(Game game) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(5,7,14,0.90);");
        overlay.setPickOnBounds(true);
        SettingsPanel panel = new SettingsPanel(game.getSettings(), () -> getChildren().remove(overlay));
        overlay.getChildren().add(panel);
        getChildren().add(overlay);
    }

    private Button makeBtn(String text, String color) {
        Button b = new Button(text);
        b.setPrefSize(360, 58);
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
               "-fx-font-size: 17px;" +
               "-fx-font-weight: bold;" +
               "-fx-font-family: 'Georgia';" +
               "-fx-cursor: hand;";
    }
}