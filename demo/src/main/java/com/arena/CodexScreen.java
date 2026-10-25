package com.arena;

import com.arena.ui.Sprite;
import com.arena.ui.Sprites;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class CodexScreen extends StackPane {

    private final VBox listBox;
    private final VBox detailBox;

    public CodexScreen(Runnable onBack) {
        setStyle("-fx-background-color: linear-gradient(to bottom, #0a0c13, #1a0e2e, #0a0c13);");

        Label title = new Label("CODEX");
        title.setStyle("-fx-text-fill: #c4b5fd; -fx-font-size: 36px; -fx-font-weight: bold; -fx-font-family: 'Georgia';");
        title.setEffect(new DropShadow(20, Color.web("#7c5cff")));

        Label subtitle = new Label("Lore unlocked: " + StoryCodex.unlockedCount() + " / " + StoryCodex.totalCount());
        subtitle.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");

        Button back = new Button("Back to Menu");
        back.setPrefSize(180, 40);
        back.setStyle(btnStyle("#7c5cff", false));
        back.setOnMouseEntered(e -> back.setStyle(btnStyle("#7c5cff", true)));
        back.setOnMouseExited(e  -> back.setStyle(btnStyle("#7c5cff", false)));
        back.setOnAction(e -> onBack.run());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(14, title, spacer, back);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(24, 40, 10, 40));

        VBox titleBox = new VBox(4, header, subtitle);
        titleBox.setAlignment(Pos.TOP_LEFT);
        VBox.setMargin(subtitle, new Insets(0, 0, 0, 40));

        listBox = new VBox(8);
        listBox.setPadding(new Insets(12));

        var entries = StoryCodex.unlocked();
        if (entries.isEmpty()) {
            Label empty = new Label("No entries unlocked yet.\nPlay through the story to fill the codex.");
            empty.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px; -fx-font-style: italic; -fx-text-alignment: center;");
            empty.setWrapText(true);
            listBox.getChildren().add(empty);
        } else {
            for (StoryCodex.Entry entry : entries) {
                listBox.getChildren().add(makeEntryButton(entry));
            }
        }

        ScrollPane listScroll = new ScrollPane(listBox);
        listScroll.setFitToWidth(true);
        listScroll.setPrefWidth(320);
        listScroll.setStyle("-fx-background: #0e1220; -fx-background-color: #0e1220; " +
                "-fx-border-color: #232a44; -fx-border-width: 0 1 0 0;");

        detailBox = new VBox(20);
        detailBox.setPadding(new Insets(30, 40, 30, 40));
        detailBox.setAlignment(Pos.TOP_LEFT);
        detailBox.setStyle("-fx-background-color: #0e1220;");

        if (entries.isEmpty()) {
            Label placeholder = new Label("Select an entry from the left to read it here.");
            placeholder.setStyle("-fx-text-fill: #475569; -fx-font-size: 14px; -fx-font-style: italic;");
            detailBox.getChildren().add(placeholder);
        } else {
            showEntry(entries.get(0));
        }

        BorderPane body = new BorderPane();
        body.setLeft(listScroll);
        body.setCenter(detailBox);
        BorderPane.setMargin(body, new Insets(0, 20, 20, 20));

        BorderPane root = new BorderPane();
        root.setTop(titleBox);
        root.setCenter(body);

        getChildren().add(root);
    }

    private Button makeEntryButton(StoryCodex.Entry entry) {
        Button b = new Button(entry.title + "\n" + entry.subtitle);
        b.setPrefWidth(280);
        b.setPrefHeight(58);
        b.setAlignment(Pos.CENTER_LEFT);
        b.setStyle(entryStyle(entry.colorHex, false));
        b.setOnMouseEntered(e -> b.setStyle(entryStyle(entry.colorHex, true)));
        b.setOnMouseExited(e  -> b.setStyle(entryStyle(entry.colorHex, false)));
        b.setOnAction(e -> showEntry(entry));
        return b;
    }

    private void showEntry(StoryCodex.Entry entry) {
        detailBox.getChildren().clear();

        Sprite portrait = new Sprite(Sprites.get(entry.spriteKey), Sprites.palette(), 6);
        portrait.setEffect(new DropShadow(28, Color.web(entry.colorHex, 0.85)));

        Label sub = new Label(entry.subtitle);
        sub.setStyle("-fx-text-fill: " + entry.colorHex + "; -fx-font-size: 12px; -fx-font-weight: bold; " +
                "-fx-font-family: 'Consolas';");

        Label title = new Label(entry.title);
        title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 34px; -fx-font-weight: bold; " +
                "-fx-font-family: 'Georgia';");
        title.setEffect(new DropShadow(14, Color.web(entry.colorHex, 0.6)));

        Label body = new Label(entry.body);
        body.setWrapText(true);
        body.setMaxWidth(560);
        body.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 15px; -fx-font-family: 'Georgia'; " +
                "-fx-line-spacing: 6;");

        VBox headerText = new VBox(6, sub, title);
        headerText.setAlignment(Pos.CENTER_LEFT);

        HBox topRow = new HBox(28, portrait, headerText);
        topRow.setAlignment(Pos.CENTER_LEFT);

        detailBox.getChildren().addAll(topRow, body);
    }

    private String entryStyle(String color, boolean hover) {
        String fill = hover ? color + "1a" : "rgba(255,255,255,0.02)";
        String border = hover ? color : "#334155";
        return "-fx-background-color: " + fill + ";" +
               "-fx-border-color: " + border + ";" +
               "-fx-border-width: 1.5;" +
               "-fx-border-radius: 8;" +
               "-fx-background-radius: 8;" +
               "-fx-text-fill: #e2e8f0;" +
               "-fx-font-size: 12px;" +
               "-fx-font-family: 'Georgia';" +
               "-fx-cursor: hand;" +
               "-fx-padding: 8 14;";
    }

    private String btnStyle(String c, boolean hover) {
        return "-fx-background-color: " + (hover ? c : "rgba(255,255,255,0.03)") + ";" +
               "-fx-border-color: " + c + ";" +
               "-fx-border-width: 1.5;" +
               "-fx-border-radius: 8;" +
               "-fx-background-radius: 8;" +
               "-fx-text-fill: " + (hover ? "#0a0c13" : c) + ";" +
               "-fx-font-size: 13px;" +
               "-fx-font-weight: bold;" +
               "-fx-cursor: hand;";
    }
}