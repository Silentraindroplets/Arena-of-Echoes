package com.arena;

import com.arena.ui.Sprite;
import com.arena.ui.Sprites;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.List;

public class StoryScene extends StackPane {

    private final List<StoryLine> script;
    private final Runnable onComplete;
    private int lineIndex = 0;

    private final StackPane spriteArea;
    private final Label speakerLabel;
    private final Label textLabel;

    public StoryScene(List<StoryLine> script, Runnable onComplete) {
        this.script = script;
        this.onComplete = onComplete;

        setStyle("-fx-background-color: #0a0508;");

        Rectangle bg = new Rectangle();
        bg.widthProperty().bind(widthProperty());
        bg.heightProperty().bind(heightProperty());
        bg.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0a0508")),
                new Stop(0.5, Color.web("#2a0812")),
                new Stop(1, Color.web("#0a0508"))));

        Rectangle spotlight = new Rectangle(600, 520);
        spotlight.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#dc2626", 0.20)),
                new Stop(1, Color.TRANSPARENT)));
        spotlight.setMouseTransparent(true);

        Sprite initial = new Sprite(Sprites.get("knight"), Sprites.palette(), 12);
        initial.setEffect(new DropShadow(40, Color.web("#7c5cff", 0.85)));
        spriteArea = new StackPane(initial);
        spriteArea.setAlignment(Pos.CENTER);
        spriteArea.setPrefHeight(560);

        VBox topContent = new VBox(20, spotlight, spriteArea);
        topContent.setAlignment(Pos.CENTER);

        speakerLabel = new Label();
        speakerLabel.setStyle("-fx-text-fill: #facc15; -fx-font-size: 19px; -fx-font-weight: bold; " +
                "-fx-font-family: 'Georgia';");

        textLabel = new Label();
        textLabel.setWrapText(true);
        textLabel.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 18px; -fx-font-family: 'Georgia'; " +
                "-fx-line-spacing: 4;");
        textLabel.setMaxWidth(940);
        textLabel.setMinHeight(60);

        Label continueHdr = new Label("▼  Click or press SPACE to continue");
        continueHdr.setStyle("-fx-text-fill: #6b2020; -fx-font-size: 11px; -fx-font-style: italic;");

        VBox textInner = new VBox(10, speakerLabel, textLabel, continueHdr);
        textInner.setAlignment(Pos.TOP_LEFT);
        textInner.setPadding(new Insets(22, 32, 20, 32));
        textInner.setMaxWidth(1000);
        textInner.setStyle("-fx-background-color: rgba(20,6,12,0.94); " +
                "-fx-border-color: #b91c1c; -fx-border-width: 2; " +
                "-fx-border-radius: 14; -fx-background-radius: 14; " +
                "-fx-effect: dropshadow(gaussian, #000, 30, 0.5, 0, -6);");

        VBox textHolder = new VBox(textInner);
        textHolder.setAlignment(Pos.CENTER);
        textHolder.setPadding(new Insets(0, 0, 40, 0));

        StackPane lowerLayer = new StackPane(topContent, textHolder);
        lowerLayer.setAlignment(Pos.CENTER);

        Rectangle vignette = new Rectangle();
        vignette.widthProperty().bind(widthProperty());
        vignette.heightProperty().bind(heightProperty());
        vignette.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.9, true,
                CycleMethod.NO_CYCLE,
                new Stop(0.55, Color.TRANSPARENT),
                new Stop(1, Color.web("#000000", 0.9))));
        vignette.setMouseTransparent(true);

        getChildren().addAll(bg, lowerLayer, vignette);

        setFocusTraversable(true);
        setOnMouseClicked(e -> advance());
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.ENTER
                    || e.getCode() == KeyCode.ESCAPE) {
                advance();
            }
        });

        showLine();
        javafx.application.Platform.runLater(this::requestFocus);
    }

    private void showLine() {
        if (lineIndex >= script.size()) return;
        StoryLine line = script.get(lineIndex);

        speakerLabel.setText(line.speaker);
        speakerLabel.setStyle("-fx-text-fill: " + line.colorHex + "; -fx-font-size: 19px; " +
                "-fx-font-weight: bold; -fx-font-family: 'Georgia';");
        textLabel.setText(line.text);

        if (line.dramatic) {
            Rectangle flash = new Rectangle();
            flash.widthProperty().bind(widthProperty());
            flash.heightProperty().bind(heightProperty());
            flash.setFill(Color.web("#dc2626"));
            flash.setOpacity(0);
            flash.setMouseTransparent(true);
            getChildren().add(flash);

            FadeTransition in = new FadeTransition(Duration.millis(90), flash);
            in.setFromValue(0); in.setToValue(0.45);
            FadeTransition out = new FadeTransition(Duration.millis(500), flash);
            out.setFromValue(0.45); out.setToValue(0);
            SequentialTransition seq = new SequentialTransition(in, out);
            seq.setOnFinished(e -> getChildren().remove(flash));
            seq.play();

            TranslateTransition shake = new TranslateTransition(Duration.millis(40), spriteArea);
            shake.setFromX(0);
            shake.setByX(10);
            shake.setCycleCount(8);
            shake.setAutoReverse(true);
            shake.setOnFinished(e -> spriteArea.setTranslateX(0));
            shake.play();
        }

        spriteArea.getChildren().clear();

        Sprite sprite = new Sprite(Sprites.get(line.spriteKey), Sprites.palette(), 12);
        sprite.setEffect(new DropShadow(40, Color.web(line.colorHex, 0.85)));
        sprite.setOpacity(0);
        sprite.setTranslateY(24);
        spriteArea.getChildren().add(sprite);

        FadeTransition ft = new FadeTransition(Duration.millis(420), sprite);
        ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(420), sprite);
        tt.setToY(0);

        textLabel.setOpacity(0);
        speakerLabel.setOpacity(0);
        FadeTransition ftText = new FadeTransition(Duration.millis(300), textLabel);
        ftText.setToValue(1);
        FadeTransition ftSpk = new FadeTransition(Duration.millis(300), speakerLabel);
        ftSpk.setToValue(1);

        new ParallelTransition(ft, tt, ftText, ftSpk).play();
    }

    private void advance() {
        lineIndex++;
        if (lineIndex >= script.size()) {
            if (onComplete != null) onComplete.run();
        } else {
            showLine();
        }
    }
}