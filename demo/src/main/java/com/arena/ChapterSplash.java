package com.arena;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class ChapterSplash extends StackPane {

    public ChapterSplash(String chapterNumber,
                         String chapterTitle,
                         String subtitle,
                         Runnable onComplete) {

        setStyle("-fx-background-color: #05070f;");
        setPickOnBounds(true);
        setMouseTransparent(true);

        Rectangle bg = new Rectangle();
        bg.widthProperty().bind(widthProperty());
        bg.heightProperty().bind(heightProperty());
        bg.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0a0205")),
                new Stop(0.5, Color.web("#1a0408")),
                new Stop(1, Color.web("#0a0205"))));

        Rectangle ruleTop = new Rectangle(180, 2);
        ruleTop.setFill(Color.web("#facc15"));
        ruleTop.setEffect(new DropShadow(14, Color.web("#facc15")));
        ruleTop.setOpacity(0);

        Rectangle ruleBot = new Rectangle(180, 2);
        ruleBot.setFill(Color.web("#facc15"));
        ruleBot.setEffect(new DropShadow(14, Color.web("#facc15")));
        ruleBot.setOpacity(0);

        Label chapterNum = new Label("CHAPTER " + chapterNumber);
        chapterNum.setStyle(
                "-fx-text-fill: #facc15; " +
                "-fx-font-size: 20px; " +
                "-fx-font-weight: bold; " +
                "-fx-font-family: 'Consolas', monospace;");
        chapterNum.setEffect(new DropShadow(18, Color.web("#facc15", 0.7)));
        chapterNum.setOpacity(0);

        Label chapterName = new Label(chapterTitle);
        chapterName.setStyle(
                "-fx-text-fill: #ffffff; " +
                "-fx-font-size: 68px; " +
                "-fx-font-weight: bold; " +
                "-fx-font-family: 'Georgia';");
        chapterName.setEffect(new DropShadow(30, Color.web("#dc2626", 0.85)));
        chapterName.setOpacity(0);
        chapterName.setScaleX(1.25);
        chapterName.setScaleY(1.25);

        Label sub = new Label(subtitle);
        sub.setStyle(
                "-fx-text-fill: #b8a0a0; " +
                "-fx-font-size: 14px; " +
                "-fx-font-style: italic; " +
                "-fx-font-family: 'Georgia';");
        sub.setOpacity(0);

        VBox column = new VBox(18, chapterNum, chapterName, ruleBot, sub);
        column.setAlignment(Pos.CENTER);
        column.setMaxWidth(1000);

        VBox stack = new VBox(22, ruleTop, column);
        stack.setAlignment(Pos.CENTER);

        getChildren().addAll(bg, stack);

        FadeTransition rulesIn = new FadeTransition(Duration.millis(280), ruleTop);
        rulesIn.setToValue(1);
        FadeTransition ruleBotIn = new FadeTransition(Duration.millis(280), ruleBot);
        ruleBotIn.setToValue(1);
        FadeTransition numIn = new FadeTransition(Duration.millis(340), chapterNum);
        numIn.setToValue(1);

        FadeTransition nameIn = new FadeTransition(Duration.millis(400), chapterName);
        nameIn.setToValue(1);
        ScaleTransition nameScale = new ScaleTransition(Duration.millis(620), chapterName);
        nameScale.setFromX(1.25); nameScale.setFromY(1.25);
        nameScale.setToX(1.0);    nameScale.setToY(1.0);

        FadeTransition subIn = new FadeTransition(Duration.millis(380), sub);
        subIn.setDelay(Duration.millis(220));
        subIn.setToValue(1);

        ParallelTransition inSeq = new ParallelTransition(
                rulesIn, ruleBotIn, numIn, nameIn, nameScale, subIn);
        inSeq.play();

        PauseTransition hold = new PauseTransition(Duration.millis(1150));
        hold.setOnFinished(e -> {
            FadeTransition out = new FadeTransition(Duration.millis(480), stack);
            out.setToValue(0);
            TranslateTransition slide = new TranslateTransition(Duration.millis(480), stack);
            slide.setByY(-14);
            ParallelTransition outro = new ParallelTransition(out, slide);
            outro.setOnFinished(ev -> onComplete.run());
            outro.play();
        });
        hold.play();
    }
}