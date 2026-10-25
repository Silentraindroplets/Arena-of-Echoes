package com.arena.ui;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.Map;

public class Sprite extends Pane {

    private TranslateTransition idle;

    public Sprite(String[] pattern, Map<Character, Color> palette, int scale) {
        int rows = pattern.length;
        int cols = pattern[0].length();

        setPrefSize(cols * scale, rows * scale);
        setMinSize(cols * scale, rows * scale);
        setMaxSize(cols * scale, rows * scale);
        setMouseTransparent(true);

        for (int r = 0; r < rows; r++) {
            String row = pattern[r];
            for (int c = 0; c < row.length(); c++) {
                Color color = palette.getOrDefault(row.charAt(c), Color.TRANSPARENT);
                if (color == Color.TRANSPARENT) continue;

                Rectangle pixel = new Rectangle(scale, scale);
                pixel.setX(c * scale);
                pixel.setY(r * scale);
                pixel.setFill(color);
                getChildren().add(pixel);
            }
        }
    }

    public void setMirror(boolean mirror) { setScaleX(mirror ? -1 : 1); }

    public void startIdle() {
        idle = new TranslateTransition(Duration.millis(1400), this);
        idle.setByY(-3);
        idle.setAutoReverse(true);
        idle.setCycleCount(TranslateTransition.INDEFINITE);
        idle.play();
    }

    public void stopIdle() { if (idle != null) idle.stop(); }

    public void playHurt() {
        TranslateTransition t = new TranslateTransition(Duration.millis(45), this);
        t.setFromX(0);
        t.setByX(8);
        t.setCycleCount(4);
        t.setAutoReverse(true);
        t.setOnFinished(e -> setTranslateX(0));
        t.play();
    }

    public void playDirectionalLunge(double dx, double dy, Runnable onImpact) {
        if (idle != null) idle.pause();

        TranslateTransition forward = new TranslateTransition(Duration.millis(170), this);
        forward.setByX(dx);
        forward.setByY(dy);

        TranslateTransition back = new TranslateTransition(Duration.millis(170), this);
        back.setByX(-dx);
        back.setByY(-dy);

        forward.setOnFinished(e -> {
            if (onImpact != null) onImpact.run();
            back.play();
        });
        back.setOnFinished(e -> { if (idle != null) idle.play(); });

        forward.play();
    }

    public void playDeath() {
        FadeTransition ft = new FadeTransition(Duration.millis(600), this);
        ft.setToValue(0.05);
        TranslateTransition tt = new TranslateTransition(Duration.millis(600), this);
        tt.setByY(28);
        new ParallelTransition(ft, tt).play();
    }

    public void playSpawn() {
        setOpacity(0);
        setTranslateY(24);
        FadeTransition ft = new FadeTransition(Duration.millis(550), this);
        ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(550), this);
        tt.setToY(0);
        new ParallelTransition(ft, tt).play();
    }
}