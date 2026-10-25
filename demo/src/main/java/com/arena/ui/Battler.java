package com.arena.ui;

import com.arena.model.Combatant;
import com.arena.model.Hero;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.function.Consumer;

public class Battler extends VBox {

    private final Combatant combatant;
    private final Sprite sprite;
    private final StatBar hpBar;
    private final StatBar mpBar;
    private final Rectangle ultFill;
    private final Rectangle ultBack;
    private final Label nameLabel;
    private final Label statusLabel;
    private final boolean heroSide;
    private final StackPane spriteStack;

    private final Polygon activeArrow;
    private final TranslateTransition arrowBounce;

    private boolean selected = false;
    private boolean active = false;

    public Battler(Combatant c, boolean heroSide, Consumer<Combatant> onSelect,
                   int spriteScale, String playerBadge) {
        this.combatant = c;
        this.heroSide = heroSide;

        setAlignment(Pos.CENTER);
        setSpacing(5);
        setPrefWidth(200);
        setMaxWidth(200);

        sprite = new Sprite(Sprites.get(c.spriteKey()), Sprites.palette(), spriteScale);
        sprite.setMirror(heroSide);

        Ellipse shadow = new Ellipse(58, 11);
        shadow.setFill(Color.web("#000000", 0.5));
        shadow.setTranslateY(sprite.getPrefHeight() / 2 - 2);

        activeArrow = new Polygon(0.0, 0.0, -14.0, -18.0, 14.0, -18.0);
        activeArrow.setFill(Color.web(c.colorHex()));
        activeArrow.setStroke(Color.WHITE);
        activeArrow.setStrokeWidth(1.5);
        activeArrow.setTranslateY(-(sprite.getPrefHeight() / 2) - 22);
        activeArrow.setVisible(false);
        activeArrow.setEffect(new DropShadow(12, Color.web(c.colorHex(), 0.95)));

        arrowBounce = new TranslateTransition(Duration.millis(480), activeArrow);
        arrowBounce.setByY(9);
        arrowBounce.setAutoReverse(true);
        arrowBounce.setCycleCount(TranslateTransition.INDEFINITE);

        spriteStack = new StackPane(shadow, sprite, activeArrow);
        spriteStack.setPrefSize(sprite.getPrefWidth() + 40, sprite.getPrefHeight() + 16);
        sprite.setEffect(new DropShadow(22, Color.web(c.colorHex(), 0.6)));

        if (playerBadge != null) {
            Label badge = new Label(playerBadge);
            badge.setStyle("-fx-background-color: " + c.colorHex() + ";" +
                    "-fx-text-fill: #0a0c13; -fx-font-size: 11px; -fx-font-weight: bold;" +
                    "-fx-padding: 3 8; -fx-background-radius: 5; -fx-border-radius: 5;");
            badge.setMouseTransparent(true);
            spriteStack.getChildren().add(badge);
            StackPane.setAlignment(badge, Pos.TOP_LEFT);
            StackPane.setMargin(badge, new Insets(4, 0, 0, 4));
        }

        nameLabel = new Label(c.name.toUpperCase() + "  Lv" + c.level);
        nameLabel.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 11px; -fx-font-weight: bold;");

        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 9.5px;");

        hpBar = new StatBar(150, 9, Color.web("#22c55e"));
        mpBar = heroSide ? new StatBar(150, 7, Color.web("#3b82f6")) : null;

        ultBack = new Rectangle(150, 5);
        ultBack.setArcWidth(5); ultBack.setArcHeight(5);
        ultBack.setFill(Color.web("#0b0d14"));
        ultFill = new Rectangle(0, 5);
        ultFill.setArcWidth(5); ultFill.setArcHeight(5);
        ultFill.setFill(Color.web("#facc15"));
        StackPane ultHolder = new StackPane(ultBack, ultFill);
        ultHolder.setAlignment(Pos.CENTER_LEFT);
        ultHolder.setPrefSize(150, 5);
        ultHolder.setMaxSize(150, 5);

        VBox infoBox = new VBox(3);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.getChildren().addAll(nameLabel, hpBar);
        if (mpBar != null) infoBox.getChildren().add(mpBar);
        if (heroSide) infoBox.getChildren().add(ultHolder);
        infoBox.getChildren().add(statusLabel);

        if (heroSide) getChildren().addAll(spriteStack, infoBox);
        else          getChildren().addAll(infoBox, spriteStack);

        if (!heroSide) {
            setOnMouseClicked(e -> { if (onSelect != null && c.isAlive()) onSelect.accept(c); });
            setOnMouseEntered(e -> { if (c.isAlive()) setStyle("-fx-cursor: hand;"); });
        }

        sprite.startIdle();
        sprite.playSpawn();
    }

    public Combatant getCombatant() { return combatant; }
    public Sprite getSprite() { return sprite; }
    public StatBar getHpBar() { return hpBar; }
    public StatBar getMpBar() { return mpBar; }
    public Label getNameLabel() { return nameLabel; }
    public Label getStatusLabel() { return statusLabel; }

    public void setSelected(boolean s) {
        if (selected == s) return;
        selected = s;
        updateEffect();
    }

    public void setActive(boolean a) {
        if (active == a) return;
        active = a;
        updateEffect();
        if (a) { activeArrow.setVisible(true); arrowBounce.playFromStart(); }
        else   { activeArrow.setVisible(false); arrowBounce.stop();
                 activeArrow.setTranslateY(-(sprite.getPrefHeight() / 2) - 22); }
    }

    public void setUltReady(boolean ready) {
        if (!heroSide || !(combatant instanceof Hero h)) return;
        if (ready && combatant.isAlive()) {
            if (!active && !selected) {
                sprite.setEffect(new DropShadow(36, Color.web("#facc15", 0.95)));
            }
        } else {
            updateEffect();
        }
    }

    private void updateEffect() {
        if (active) {
            sprite.setEffect(new DropShadow(38, Color.web("#facc15", 0.95)));
        } else if (selected) {
            sprite.setEffect(new DropShadow(28, Color.web("#7c5cff", 0.95)));
        } else if (heroSide && combatant instanceof Hero h && h.hasUlt()) {
            sprite.setEffect(new DropShadow(36, Color.web("#facc15", 0.95)));
        } else {
            sprite.setEffect(new DropShadow(22, Color.web(combatant.colorHex(), 0.6)));
        }
    }

    public void setDead(boolean dead) {
        setOpacity(dead ? 0.22 : 1.0);
        if (dead) { activeArrow.setVisible(false); arrowBounce.stop(); }
    }

    public void refreshUlt() {
        if (!heroSide || !(combatant instanceof Hero h) || h.ultimate == null) return;
        double ratio = h.ultRatio();
        ultFill.setWidth(150 * ratio);
        ultFill.setFill(h.hasUlt() ? Color.web("#facc15") : Color.web("#a16207"));
        updateEffect();
    }

    public void popText(String text, Color color) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + toHex(color) + "; -fx-font-size: 24px; -fx-font-weight: bold;");
        l.setTranslateY(-40);
        l.setMouseTransparent(true);
        l.setEffect(new DropShadow(10, Color.web("#000000", 1.0)));
        getChildren().add(l);

        TranslateTransition tt = new TranslateTransition(Duration.millis(950), l);
        tt.setByY(-70);
        FadeTransition ft = new FadeTransition(Duration.millis(950), l);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ParallelTransition pt = new ParallelTransition(tt, ft);
        pt.setOnFinished(e -> getChildren().remove(l));
        pt.play();
    }

    public void playAttack(Runnable onImpact) {
        double dx = heroSide ? 70 : -70;
        double dy = heroSide ? -50 : 50;
        sprite.playDirectionalLunge(dx, dy, onImpact);
    }

    public void playHurt()  { sprite.playHurt(); }
    public void playDeath() { sprite.playDeath(); }

    private static String toHex(Color c) {
        return String.format("#%02X%02X%02X",
                (int) Math.round(c.getRed() * 255),
                (int) Math.round(c.getGreen() * 255),
                (int) Math.round(c.getBlue() * 255));
    }
}