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

import java.util.ArrayList;
import java.util.List;

/** Full-screen visual-novel intro that plays before each battle. */
public class IntroScene extends StackPane {

    private static class Line {
        String speaker, text, spriteKey, colorHex;
        Line(String speaker, String text, String spriteKey, String colorHex) {
            this.speaker = speaker; this.text = text;
            this.spriteKey = spriteKey; this.colorHex = colorHex;
        }
    }

    private final List<Line> script = new ArrayList<>();
    private int lineIndex = 0;

    private final StackPane spriteArea;
    private final Label speakerLabel;
    private final Label textLabel;
    private final Sprite currentSprite;

    public IntroScene(Game game, int numPlayers) {
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

        currentSprite = new Sprite(Sprites.get("knight"), Sprites.palette(), 12);
        currentSprite.setEffect(new DropShadow(40, Color.web("#7c5cff", 0.85)));
        spriteArea = new StackPane(currentSprite);
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

        // ============================================================
        //  EXPANDED STORY SCRIPT
        // ============================================================
        if (numPlayers == 1) {
            script.add(new Line("Narrator",
                "They say the Arena of Echoes has been sealed for one hundred years. Tonight, for the first time since the old wars, its gates have opened.",
                "knight", "#94a3b8"));
            script.add(new Line("Narrator",
                "You walk the long approach alone. Rusted armor hangs from iron hooks along the walls — remnants of every knight who came before you and did not leave.",
                "knight", "#94a3b8"));
            script.add(new Line("Narrator",
                "Every torch in the corridor gutters as you pass, as if the flame itself is afraid to look at you.",
                "knight", "#94a3b8"));
            script.add(new Line("Knight (Player 1)",
                "Just me and the dark, then. Suits me fine.",
                "knight", "#7c5cff"));
            script.add(new Line("Knight (Player 1)",
                "The old masters said every knight ends his story in a corridor like this one. I never thought mine would be so... quiet.",
                "knight", "#7c5cff"));
            script.add(new Line("Knight (Player 1)",
                "I've trained for this. Let the Echoes sing.",
                "knight", "#7c5cff"));
            script.add(new Line("Narrator",
                "The corridor ends. Beyond it, a vast hall opens like a wound in the mountain. A red moon hangs low through a shattered ceiling.",
                "knight", "#94a3b8"));
            script.add(new Line("???",
                "Foolish mortal. The Demon Lord waits at the end of this arena.",
                "demon", "#dc2626"));
            script.add(new Line("???",
                "Malgoroth the Ashen King — he has drained a thousand knights who spoke as bravely as you. Their armor hangs in his hall like wind-chimes.",
                "demon", "#dc2626"));
            script.add(new Line("???",
                "Turn back now, and I will let you keep your name. Stay, and he will take even that.",
                "demon", "#dc2626"));
            script.add(new Line("Knight (Player 1)",
                "Then he can choke on a thousand and one.",
                "knight", "#7c5cff"));
            script.add(new Line("Narrator",
                "The gates slam shut behind you. From far below, something enormous stirs in its sleep.",
                "knight", "#94a3b8"));
            script.add(new Line("Narrator",
                "The first wave of the Arena's servants rises from cracks in the floor — goblins, their teeth wet with torchlight.",
                "knight", "#94a3b8"));
            script.add(new Line("Knight (Player 1)",
                "Come on, then. Show me what a hundred years of hunger looks like.",
                "knight", "#7c5cff"));
        } else {
            script.add(new Line("Narrator",
                "Two figures approach the Arena of Echoes side by side. One in silver, one in shadow. Neither speaks of what waits inside.",
                "knight", "#94a3b8"));
            script.add(new Line("Narrator",
                "Above them a blood moon hangs low, swollen and red. Broken chains dangle from the crumbling battlements, swaying without wind.",
                "knight", "#94a3b8"));
            script.add(new Line("Knight (Player 1)",
                "Stay close, mage. The Echoes are treacherous this time of year.",
                "knight", "#7c5cff"));
            script.add(new Line("Mage (Player 2)",
                "I can smell the brimstone from here. Something big woke up tonight.",
                "mage", "#38bdf8"));
            script.add(new Line("Knight (Player 1)",
                "Something big is about to go back to sleep.",
                "knight", "#7c5cff"));
            script.add(new Line("Mage (Player 2)",
                "I'll burn anything that gets within arm's reach. And if it doesn't get close — I'll burn it anyway.",
                "mage", "#38bdf8"));
            script.add(new Line("Knight (Player 1)",
                "Good. Then let's begin.",
                "knight", "#7c5cff"));
            script.add(new Line("Narrator",
                "The great iron doors groan open. Behind them: a hall of black stone, ringed by pillars carved with a thousand screaming faces.",
                "knight", "#94a3b8"));
            script.add(new Line("???",
                "Ah... the little heroes come to die together. How quaint.",
                "demon", "#dc2626"));
            script.add(new Line("???",
                "Malgoroth the Ashen King has been so very bored. Do try to last longer than the last pair — they managed four minutes.",
                "demon", "#dc2626"));
            script.add(new Line("Mage (Player 2)",
                "Four minutes, huh? We'll aim for five.",
                "mage", "#38bdf8"));
            script.add(new Line("???",
                "Cocky. The Demon Lord does so enjoy breaking the cocky ones.",
                "demon", "#dc2626"));
            script.add(new Line("Mage (Player 2)",
                "We'll see who dies today, demon. I hear your kind burns rather well.",
                "mage", "#38bdf8"));
            script.add(new Line("Narrator",
                "A tremor rolls through the stones. Every torch on the wall flares red at once. The first wave has been summoned.",
                "knight", "#94a3b8"));
            script.add(new Line("Knight (Player 1)",
                "Eyes forward. Here they come.",
                "knight", "#7c5cff"));
        }

        setFocusTraversable(true);
        setOnMouseClicked(e -> advance(game, numPlayers));
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.ENTER
                    || e.getCode() == KeyCode.ESCAPE) {
                advance(game, numPlayers);
            }
        });

        showLine();
        javafx.application.Platform.runLater(this::requestFocus);
    }

    private void showLine() {
        if (lineIndex >= script.size()) return;
        Line line = script.get(lineIndex);

        speakerLabel.setText(line.speaker);
        speakerLabel.setStyle("-fx-text-fill: " + line.colorHex + "; -fx-font-size: 19px; " +
                "-fx-font-weight: bold; -fx-font-family: 'Georgia';");
        textLabel.setText(line.text);

        if ("demon".equals(line.spriteKey)) {
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

    private void advance(Game game, int numPlayers) {
        lineIndex++;
        if (lineIndex >= script.size()) {
            game.startBattleWithCinematic(numPlayers);
        } else {
            showLine();
        }
    }
}