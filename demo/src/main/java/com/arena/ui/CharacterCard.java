package com.arena.ui;

import com.arena.model.Combatant;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.function.Consumer;

/** Visual card for a single combatant (hero or enemy) on the battlefield. */
public class CharacterCard extends StackPane {

    private final Combatant combatant;
    private final Sprite sprite;
    private final StatBar hpBar;
    private final StatBar mpBar;   // only used if showMp
    private final Label hpText;
    private final Label mpText;
    private final Label statusLabel;
    private final VBox card;

    private boolean selected = false;
    private boolean active = false;

    public CharacterCard(Combatant c, Consumer<Combatant> onSelect, boolean showMp, int spriteScale) {
        this.combatant = c;
        setAlignment(Pos.TOP_CENTER);

        sprite = new Sprite(Sprites.get(c.spriteKey()), Sprites.palette(), spriteScale);
        StackPane spriteHolder = new StackPane(sprite);
        spriteHolder.setPrefHeight(sprite.getPrefHeight() + 10);
        spriteHolder.setEffect(new DropShadow(14, Color.web(c.colorHex(), 0.55)));

        Label nameLabel = new Label(c.name.toUpperCase());
        nameLabel.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 12.5px; -fx-font-weight: bold;");

        Label lvlLabel = new Label("Lv " + c.level);
        lvlLabel.setStyle("-fx-text-fill: #facc15; -fx-font-size: 10px; -fx-font-weight: bold;");

        VBox nameBox = new VBox(1, nameLabel, lvlLabel);
        nameBox.setAlignment(Pos.CENTER);

        hpBar = new StatBar(150, 9, Color.web("#22c55e"));
        mpBar = new StatBar(150, 7, Color.web("#3b82f6"));

        hpText = new Label();
        hpText.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px; -fx-font-family: 'Consolas';");

        mpText = new Label();
        mpText.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px; -fx-font-family: 'Consolas';");

        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 10px;");

        card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(11));
        card.setPrefWidth(190);
        card.setStyle(cardStyle());

        card.getChildren().addAll(spriteHolder, nameBox, hpBar, hpText);
        if (showMp) card.getChildren().addAll(mpBar, mpText);
        card.getChildren().add(statusLabel);

        getChildren().add(card);

        setOnMouseClicked(e -> { if (onSelect != null) onSelect.accept(c); });

        sprite.startIdle();
    }

    private String cardStyle() {
        String border = selected ? "#7c5cff" : "#232a44";
        double bw = selected ? 2.5 : 1;
        String bg = selected ? "#1b1f36" : "#151a29";
        return "-fx-background-color: " + bg + ";" +
               "-fx-background-radius: 12;" +
               "-fx-border-color: " + border + ";" +
               "-fx-border-radius: 12;" +
               "-fx-border-width: " + bw + ";";
    }

    public Combatant getCombatant() { return combatant; }
    public Sprite getSprite() { return sprite; }
    public StatBar getHpBar() { return hpBar; }
    public StatBar getMpBar() { return mpBar; }
    public Label getHpText() { return hpText; }
    public Label getMpText() { return mpText; }
    public Label getStatusLabel() { return statusLabel; }

    public void setSelected(boolean s) {
        if (selected == s) return;
        selected = s;
        card.setStyle(cardStyle());
    }

    public void setActive(boolean a) {
        if (active == a) return;
        active = a;
        card.setEffect(a ? new Glow(0.7) : null);
    }

    public void setDead(boolean dead) {
        card.setOpacity(dead ? 0.30 : 1.0);
    }

    public void popText(String text, Color color) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + toHex(color) + "; -fx-font-size: 20px; -fx-font-weight: bold;");
        l.setTranslateY(78);
        l.setMouseTransparent(true);
        l.setEffect(new DropShadow(8, Color.web("#000000", 0.9)));
        getChildren().add(l);

        TranslateTransition tt = new TranslateTransition(Duration.millis(950), l);
        tt.setByY(-78);
        FadeTransition ft = new FadeTransition(Duration.millis(950), l);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);

        ParallelTransition pt = new ParallelTransition(tt, ft);
        pt.setOnFinished(e -> getChildren().remove(l));
        pt.play();
    }

    private static String toHex(Color c) {
        return String.format("#%02X%02X%02X",
                (int) Math.round(c.getRed() * 255),
                (int) Math.round(c.getGreen() * 255),
                (int) Math.round(c.getBlue() * 255));
    }
}