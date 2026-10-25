package com.arena.ui;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class StatBar extends Pane {

    private final Rectangle bg;
    private final Rectangle fg;
    private final double w, h;
    private Timeline anim;

    public StatBar(double w, double h, Color color) {
        this.w = w; this.h = h;
        setPrefSize(w, h);
        setMinSize(w, h);
        setMaxSize(w, h);

        bg = new Rectangle(w, h);
        bg.setArcWidth(h); bg.setArcHeight(h);
        bg.setFill(Color.web("#0b0d14"));

        fg = new Rectangle(w, h);
        fg.setArcWidth(h); fg.setArcHeight(h);
        fg.setFill(color);

        getChildren().addAll(bg, fg);
    }

    public void setRatio(double ratio, boolean animate) {
        double r = Math.max(0, Math.min(1, ratio));
        double target = w * r;
        if (anim != null) anim.stop();
        if (animate) {
            anim = new Timeline(new KeyFrame(Duration.millis(260),
                    new KeyValue(fg.widthProperty(), target, Interpolator.EASE_BOTH)));
            anim.play();
        } else {
            fg.setWidth(target);
        }
    }
}