package com.arena.ui;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.Random;

/** One-shot combat visual effects rendered into the arena pane. */
public final class SkillFx {

    private static final Random RNG = new Random();
    private SkillFx() {}

    // =================================================================
    //  GENERIC SLASH — a light-arc projectile
    // =================================================================
    public static void slash(Pane pane, double x1, double y1, double x2, double y2, Color color) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double angle = Math.toDegrees(Math.atan2(dy, dx));

        Rectangle arc = new Rectangle(90, 8);
        arc.setArcWidth(10); arc.setArcHeight(10);
        arc.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.TRANSPARENT),
                new Stop(0.5, color),
                new Stop(1, Color.TRANSPARENT)));
        arc.setEffect(new DropShadow(24, color));
        arc.setRotate(angle);
        arc.setTranslateX(x1 - 45);
        arc.setTranslateY(y1 - 4);
        arc.setMouseTransparent(true);
        pane.getChildren().add(arc);

        TranslateTransition move = new TranslateTransition(Duration.millis(220), arc);
        move.setToX(x2 - 45);
        move.setToY(y2 - 4);
        FadeTransition fade = new FadeTransition(Duration.millis(160), arc);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setDelay(Duration.millis(160));
        ParallelTransition pt = new ParallelTransition(move, fade);
        pt.setOnFinished(e -> pane.getChildren().remove(arc));
        pt.play();
    }

    // =================================================================
    //  KNIGHT SLASH — triple-cross anime-style strike + lingering X mark
    // =================================================================
    public static void knightSlash(Pane pane, double x1, double y1, double x2, double y2, Color color) {
        // Primary long diagonal
        slash(pane, x1, y1, x2, y2, color);
        // Two secondary cross-cuts with a brighter tint
        Color bright = color.deriveColor(0, 1, 1.35, 0.85);
        slash(pane, x1 + 26, y1 - 34, x2 - 4, y2 + 26, bright);
        slash(pane, x1 - 26, y1 + 34, x2 + 4, y2 - 26, bright);

        // Lingering X mark on the target
        crossMark(pane, x2, y2, color);

        // Spark shrapnel
        for (int i = 0; i < 14; i++) {
            Circle spark = new Circle(1.8 + RNG.nextDouble() * 2.4);
            spark.setFill(RNG.nextBoolean() ? Color.WHITE : color);
            spark.setCenterX(x2);
            spark.setCenterY(y2);
            spark.setEffect(new DropShadow(10, color));
            spark.setMouseTransparent(true);
            pane.getChildren().add(spark);

            double angle = RNG.nextDouble() * Math.PI * 2;
            double dist  = 40 + RNG.nextDouble() * 70;
            TranslateTransition tt = new TranslateTransition(Duration.millis(380 + RNG.nextInt(220)), spark);
            tt.setByX(Math.cos(angle) * dist);
            tt.setByY(Math.sin(angle) * dist);
            tt.setInterpolator(Interpolator.EASE_OUT);
            FadeTransition ff = new FadeTransition(Duration.millis(380 + RNG.nextInt(220)), spark);
            ff.setToValue(0);
            ParallelTransition pt = new ParallelTransition(tt, ff);
            pt.setOnFinished(e -> pane.getChildren().remove(spark));
            pt.play();
        }
    }

    /** Lingering "X" cut mark that flashes bright then fades. */
    public static void crossMark(Pane pane, double x, double y, Color color) {
        double size = 62;
        Line l1 = new Line(x - size, y - size, x + size, y + size);
        Line l2 = new Line(x - size, y + size, x + size, y - size);
        for (Line l : new Line[]{ l1, l2 }) {
            l.setStroke(Color.WHITE);
            l.setStrokeWidth(4.5);
            l.setEffect(new DropShadow(22, color));
            l.setOpacity(0);
            l.setMouseTransparent(true);
            pane.getChildren().add(l);

            FadeTransition fi = new FadeTransition(Duration.millis(80), l);
            fi.setToValue(1);
            FadeTransition fo = new FadeTransition(Duration.millis(560), l);
            fo.setToValue(0);
            fo.setDelay(Duration.millis(180));
            ParallelTransition pt = new ParallelTransition(fi, fo);
            pt.setOnFinished(e -> pane.getChildren().remove(l));
            pt.play();
        }
    }

    // =================================================================
    //  IMPACT — expanding ring + radial particle burst
    // =================================================================
    public static void impact(Pane pane, double x, double y, Color color) {
        Circle ring = new Circle(14);
        ring.setFill(Color.TRANSPARENT);
        ring.setStroke(color);
        ring.setStrokeWidth(3.5);
        ring.setCenterX(x);
        ring.setCenterY(y);
        ring.setEffect(new DropShadow(20, color));
        ring.setMouseTransparent(true);
        pane.getChildren().add(ring);

        ScaleTransition rs = new ScaleTransition(Duration.millis(440), ring);
        rs.setToX(5.5); rs.setToY(5.5);
        FadeTransition rf = new FadeTransition(Duration.millis(440), ring);
        rf.setToValue(0);
        ParallelTransition rp = new ParallelTransition(rs, rf);
        rp.setOnFinished(e -> pane.getChildren().remove(ring));
        rp.play();

        int N = 16;
        for (int i = 0; i < N; i++) {
            Circle p = new Circle(2.5 + RNG.nextDouble() * 3.5);
            p.setFill(i % 3 == 0 ? Color.WHITE : color);
            p.setCenterX(x);
            p.setCenterY(y);
            p.setEffect(new DropShadow(10, color));
            p.setMouseTransparent(true);
            pane.getChildren().add(p);

            double angle = (Math.PI * 2 / N) * i + RNG.nextDouble() * 0.2;
            double dist  = 60 + RNG.nextDouble() * 70;

            TranslateTransition tp = new TranslateTransition(Duration.millis(520 + RNG.nextInt(200)), p);
            tp.setByX(Math.cos(angle) * dist);
            tp.setByY(Math.sin(angle) * dist);
            FadeTransition tf = new FadeTransition(Duration.millis(520 + RNG.nextInt(200)), p);
            tf.setToValue(0);
            ParallelTransition pp = new ParallelTransition(tp, tf);
            pp.setOnFinished(e -> pane.getChildren().remove(p));
            pp.play();
        }
    }

    // =================================================================
    //  WAVE — expanding blast sweeping across the arena (AoE)
    // =================================================================
    public static void wave(Pane pane, double originX, double originY, double arenaWidth, Color color) {
        Rectangle blast = new Rectangle(0, 140);
        blast.setY(originY - 70);
        blast.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, color.deriveColor(0, 1, 1, 0.0)),
                new Stop(0.5, color.deriveColor(0, 1, 1, 0.65)),
                new Stop(1.0, color.deriveColor(0, 1, 1, 0.0))));
        blast.setEffect(new DropShadow(30, color));
        blast.setX(originX);
        blast.setWidth(0);
        blast.setMouseTransparent(true);
        pane.getChildren().add(blast);

        Timeline tl = new Timeline(
            new KeyFrame(Duration.millis(420),
                new KeyValue(blast.widthProperty(), arenaWidth, Interpolator.EASE_OUT)),
            new KeyFrame(Duration.millis(420),
                new KeyValue(blast.opacityProperty(), 0, Interpolator.EASE_IN))
        );
        tl.setOnFinished(e -> pane.getChildren().remove(blast));
        tl.play();
    }

    // =================================================================
    //  HEAL — rising sparkles + soft glow
    // =================================================================
    public static void heal(Pane pane, double x, double y, Color color) {
        for (int i = 0; i < 22; i++) {
            Circle spark = new Circle(2 + RNG.nextDouble() * 3);
            spark.setFill(i % 4 == 0 ? Color.WHITE : color);
            spark.setCenterX(x + (RNG.nextDouble() - 0.5) * 90);
            spark.setCenterY(y + (RNG.nextDouble() - 0.5) * 40);
            spark.setEffect(new DropShadow(12, color));
            spark.setMouseTransparent(true);
            pane.getChildren().add(spark);

            TranslateTransition up = new TranslateTransition(Duration.millis(700 + RNG.nextInt(400)), spark);
            up.setByY(-110 - RNG.nextInt(80));
            up.setByX((RNG.nextDouble() - 0.5) * 40);
            FadeTransition f = new FadeTransition(Duration.millis(700 + RNG.nextInt(400)), spark);
            f.setFromValue(1.0);
            f.setToValue(0.0);
            f.setDelay(Duration.millis(RNG.nextInt(220)));
            ParallelTransition pt = new ParallelTransition(up, f);
            pt.setOnFinished(e -> pane.getChildren().remove(spark));
            pt.play();
        }

        Circle glow = new Circle(40);
        glow.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0, color.deriveColor(0, 1, 1, 0.75)),
                new Stop(1, Color.TRANSPARENT)));
        glow.setCenterX(x);
        glow.setCenterY(y);
        glow.setMouseTransparent(true);
        pane.getChildren().add(glow);

        FadeTransition gf = new FadeTransition(Duration.millis(900), glow);
        gf.setFromValue(1.0);
        gf.setToValue(0.0);
        ScaleTransition gs = new ScaleTransition(Duration.millis(900), glow);
        gs.setFromX(0.4); gs.setFromY(0.4);
        gs.setToX(2.0);   gs.setToY(2.0);
        ParallelTransition gp = new ParallelTransition(gf, gs);
        gp.setOnFinished(e -> pane.getChildren().remove(glow));
        gp.play();
    }

    // =================================================================
    //  SHIELD — hexagonal barrier that pulses
    // =================================================================
    public static void shield(Pane pane, double x, double y, double radius, Color color) {
        Polygon hex = new Polygon();
        for (int i = 0; i < 6; i++) {
            double a = Math.PI / 3 * i - Math.PI / 2;
            hex.getPoints().add(x + Math.cos(a) * radius);
            hex.getPoints().add(y + Math.sin(a) * radius);
        }
        hex.setFill(color.deriveColor(0, 1, 1, 0.18));
        hex.setStroke(color);
        hex.setStrokeWidth(3);
        hex.setEffect(new DropShadow(22, color));
        hex.setMouseTransparent(true);
        pane.getChildren().add(hex);

        FadeTransition in = new FadeTransition(Duration.millis(200), hex);
        in.setFromValue(0); in.setToValue(1);
        FadeTransition out = new FadeTransition(Duration.millis(800), hex);
        out.setFromValue(1); out.setToValue(0);
        out.setDelay(Duration.millis(700));
        ScaleTransition pulse = new ScaleTransition(Duration.millis(900), hex);
        pulse.setFromX(0.8); pulse.setFromY(0.8);
        pulse.setToX(1.15); pulse.setToY(1.15);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);

        ParallelTransition pt = new ParallelTransition(in, out, pulse);
        pt.setOnFinished(e -> pane.getChildren().remove(hex));
        pt.play();
    }

    // =================================================================
    //  CAST — rotating rune circle beneath the caster
    // =================================================================
    public static void castRunes(Pane pane, double x, double y, Color color) {
        Circle base = new Circle(60);
        base.setFill(Color.TRANSPARENT);
        base.setStroke(color);
        base.setStrokeWidth(2.5);
        base.setCenterX(x);
        base.setCenterY(y + 46);
        base.setEffect(new DropShadow(24, color));
        base.setMouseTransparent(true);
        pane.getChildren().add(base);

        RotateTransition rot = new RotateTransition(Duration.millis(700), base);
        rot.setByAngle(360);
        FadeTransition fade = new FadeTransition(Duration.millis(700), base);
        fade.setFromValue(1);
        fade.setToValue(0);
        ScaleTransition scale = new ScaleTransition(Duration.millis(700), base);
        scale.setFromX(0.5); scale.setFromY(0.5);
        scale.setToX(1.4);   scale.setToY(1.4);
        ParallelTransition pt = new ParallelTransition(rot, fade, scale);
        pt.setOnFinished(e -> pane.getChildren().remove(base));
        pt.play();
    }

    // =================================================================
    //  JRPG DAMAGE NUMBER
    // =================================================================
    public static void damageNumber(Pane pane, double x, double y, int amount, Color color, boolean crit) {
        String text = "-" + amount;
        double size = crit ? 40 : 26;

        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + toHex(color) + ";" +
                   "-fx-font-size: " + size + "px;" +
                   "-fx-font-weight: bold;" +
                   "-fx-font-family: 'Georgia';");
        l.setEffect(new DropShadow(crit ? 22 : 12, Color.web("#000000")));
        l.setMouseTransparent(true);
        l.setOpacity(0);
        l.setScaleX(crit ? 1.8 : 1.5);
        l.setScaleY(crit ? 1.8 : 1.5);
        pane.getChildren().add(l);

        // Center horizontally by measuring after layout pass
        l.applyCss();
        double w = l.getWidth();
        if (w <= 0) w = size * 1.4;
        l.setLayoutX(x - w / 2 - 5 + (RNG.nextDouble() - 0.5) * 20);
        l.setLayoutY(y - 30);

        // Pop in
        FadeTransition fin = new FadeTransition(Duration.millis(130), l);
        fin.setToValue(1);
        ScaleTransition sIn = new ScaleTransition(Duration.millis(240), l);
        sIn.setToX(1.0); sIn.setToY(1.0);
        sIn.setInterpolator(Interpolator.EASE_OUT);

        // Rise + fade
        TranslateTransition rise = new TranslateTransition(Duration.millis(crit ? 1000 : 850), l);
        rise.setByY(crit ? -130 : -85);
        rise.setDelay(Duration.millis(140));
        rise.setInterpolator(Interpolator.EASE_OUT);
        FadeTransition fout = new FadeTransition(Duration.millis(crit ? 520 : 440), l);
        fout.setToValue(0);
        fout.setDelay(Duration.millis(crit ? 620 : 500));

        ParallelTransition pt = new ParallelTransition(fin, sIn, rise, fout);
        pt.setOnFinished(e -> pane.getChildren().remove(l));
        pt.play();

        // Extra burst for crits
        if (crit) impact(pane, x, y, color);
    }

    // =================================================================
    //  JRPG HEAL NUMBER (green with rising sparkle)
    // =================================================================
    public static void healNumber(Pane pane, double x, double y, String text, Color color) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + toHex(color) + ";" +
                   "-fx-font-size: 28px;" +
                   "-fx-font-weight: bold;" +
                   "-fx-font-family: 'Georgia';");
        l.setEffect(new DropShadow(12, Color.web("#000000")));
        l.setMouseTransparent(true);
        l.setOpacity(0);
        l.setScaleX(1.4); l.setScaleY(1.4);
        pane.getChildren().add(l);

        l.applyCss();
        double w = l.getWidth();
        if (w <= 0) w = 70;
        l.setLayoutX(x - w / 2);
        l.setLayoutY(y - 30);

        FadeTransition fin = new FadeTransition(Duration.millis(140), l);
        fin.setToValue(1);
        ScaleTransition sIn = new ScaleTransition(Duration.millis(240), l);
        sIn.setToX(1.0); sIn.setToY(1.0);
        sIn.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition rise = new TranslateTransition(Duration.millis(900), l);
        rise.setByY(-90);
        rise.setDelay(Duration.millis(150));
        rise.setInterpolator(Interpolator.EASE_OUT);
        FadeTransition fout = new FadeTransition(Duration.millis(460), l);
        fout.setToValue(0);
        fout.setDelay(Duration.millis(520));

        ParallelTransition pt = new ParallelTransition(fin, sIn, rise, fout);
        pt.setOnFinished(e -> pane.getChildren().remove(l));
        pt.play();
    }

    // =================================================================
    //  STATUS WORD — big JRPG status word (BLOCK!, GUARD, CRIT!)
    // =================================================================
    public static void statusWord(Pane pane, double x, double y, String text, Color color) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + toHex(color) + ";" +
                   "-fx-font-size: 22px;" +
                   "-fx-font-weight: bold;" +
                   "-fx-font-family: 'Georgia';");
        l.setEffect(new DropShadow(14, color));
        l.setMouseTransparent(true);
        l.setOpacity(0);
        l.setScaleX(0.6); l.setScaleY(0.6);
        pane.getChildren().add(l);

        l.applyCss();
        double w = l.getWidth();
        if (w <= 0) w = 90;
        l.setLayoutX(x - w / 2);
        l.setLayoutY(y - 46);

        FadeTransition fin = new FadeTransition(Duration.millis(120), l);
        fin.setToValue(1);
        ScaleTransition sIn = new ScaleTransition(Duration.millis(260), l);
        sIn.setToX(1.0); sIn.setToY(1.0);
        sIn.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition rise = new TranslateTransition(Duration.millis(900), l);
        rise.setByY(-60);
        rise.setDelay(Duration.millis(200));
        FadeTransition fout = new FadeTransition(Duration.millis(400), l);
        fout.setToValue(0);
        fout.setDelay(Duration.millis(600));

        ParallelTransition pt = new ParallelTransition(fin, sIn, rise, fout);
        pt.setOnFinished(e -> pane.getChildren().remove(l));
        pt.play();
    }

    private static String toHex(Color c) {
        return String.format("#%02X%02X%02X",
                (int) Math.round(c.getRed()   * 255),
                (int) Math.round(c.getGreen() * 255),
                (int) Math.round(c.getBlue()  * 255));
    }
}