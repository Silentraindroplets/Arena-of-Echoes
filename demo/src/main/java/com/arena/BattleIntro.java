package com.arena;

import com.arena.ui.Sprite;
import com.arena.ui.Sprites;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
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

/** HSR-style cinematic shown at the start of each wave. */
public class BattleIntro extends StackPane {

    public BattleIntro(String waveTitle,
                       List<String> enemySprites,
                       List<String> enemyColors,
                       Runnable onComplete) {

        setStyle("-fx-background-color: #05070f;");
        setPickOnBounds(true);

        Rectangle bg = new Rectangle();
        bg.widthProperty().bind(widthProperty());
        bg.heightProperty().bind(heightProperty());
        bg.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0a0c13")),
                new Stop(0.5, Color.web("#1e1b4b")),
                new Stop(1, Color.web("#0a0c13"))));

        HBox enemyRow = new HBox(40);
        enemyRow.setAlignment(Pos.CENTER);

        for (int i = 0; i < enemySprites.size(); i++) {
            Sprite s = new Sprite(Sprites.get(enemySprites.get(i)), Sprites.palette(), 8);
            s.setEffect(new DropShadow(26, Color.web(enemyColors.get(i), 0.85)));
            s.setOpacity(0);
            s.setTranslateY(70);
            enemyRow.getChildren().add(s);

            FadeTransition ft = new FadeTransition(Duration.millis(420), s);
            ft.setToValue(1);
            ft.setDelay(Duration.millis(240 + i * 180));
            TranslateTransition tt = new TranslateTransition(Duration.millis(500), s);
            tt.setToY(0);
            tt.setDelay(Duration.millis(240 + i * 180));
            ft.play();
            tt.play();
        }

        Rectangle lineTop = new Rectangle();
        lineTop.widthProperty().bind(widthProperty());
        lineTop.setHeight(3);
        lineTop.setFill(Color.web("#facc15"));
        lineTop.setOpacity(0);

        Rectangle lineBot = new Rectangle();
        lineBot.widthProperty().bind(widthProperty());
        lineBot.setHeight(3);
        lineBot.setFill(Color.web("#facc15"));
        lineBot.setOpacity(0);

        VBox content = new VBox(30, lineTop, enemyRow, lineBot);
        content.setAlignment(Pos.CENTER);

        Label waveTxt = new Label(waveTitle);
        waveTxt.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 92px; -fx-font-weight: bold; " +
                "-fx-font-family: 'Georgia';");
        waveTxt.setEffect(new DropShadow(30, Color.web("#7c5cff")));
        waveTxt.setOpacity(0);
        waveTxt.setScaleX(1.4);
        waveTxt.setScaleY(1.4);

        Rectangle vig = new Rectangle();
        vig.widthProperty().bind(widthProperty());
        vig.heightProperty().bind(heightProperty());
        vig.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.85, true, CycleMethod.NO_CYCLE,
                new Stop(0.55, Color.TRANSPARENT),
                new Stop(1, Color.web("#000000", 0.9))));
        vig.setMouseTransparent(true);

        getChildren().addAll(bg, content, waveTxt, vig);

        play(waveTxt, lineTop, lineBot, onComplete);
    }

    private void play(Label waveTxt, Rectangle l1, Rectangle l2, Runnable onComplete) {
        PauseTransition delay1 = new PauseTransition(Duration.millis(700));
        delay1.setOnFinished(e -> {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(320), waveTxt);
            fadeIn.setToValue(1);
            ScaleTransition scale = new ScaleTransition(Duration.millis(520), waveTxt);
            scale.setToX(1.0); scale.setToY(1.0);
            fadeIn.play(); scale.play();

            FadeTransition f1 = new FadeTransition(Duration.millis(220), l1);
            f1.setFromValue(0); f1.setToValue(1); f1.setAutoReverse(true); f1.setCycleCount(2);
            f1.play();

            FadeTransition f2 = new FadeTransition(Duration.millis(220), l2);
            f2.setFromValue(0); f2.setToValue(1); f2.setAutoReverse(true); f2.setCycleCount(2);
            f2.play();
        });
        delay1.play();

        PauseTransition delay2 = new PauseTransition(Duration.millis(1900));
        delay2.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(380), waveTxt);
            fadeOut.setToValue(0);
            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(380), waveTxt);
            scaleOut.setToX(0.85); scaleOut.setToY(0.85);
            fadeOut.play(); scaleOut.play();
        });
        delay2.play();

        PauseTransition end = new PauseTransition(Duration.millis(2450));
        end.setOnFinished(e -> onComplete.run());
        end.play();
    }
}