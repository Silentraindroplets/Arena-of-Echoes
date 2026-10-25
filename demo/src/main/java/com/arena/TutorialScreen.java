package com.arena;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class TutorialScreen extends StackPane {

    private final Runnable onComplete;
    private final VBox contentBox;
    private final Label titleLabel;
    private final Label bodyLabel;
    private final Label pageLabel;
    private int currentPage = 0;

    private final String[] titles = {
        "WELCOME TO THE ARENA",
        "CODE CHALLENGES",
        "YOUR TURN (ACTIONS)",
        "THE INTERFACE",
        "TIPS FOR SURVIVAL"
    };

    private final String[] bodies = {
        "The Arena of Echoes is a turn-based RPG where you must fight through five waves of enemies to reach the Ashen King.\n\n" +
        "Combat is turn-based. The character with the highest SPEED (SPD) acts first. You control the heroes; the game controls the enemies.",

        "When an enemy attacks, a CODE CHALLENGE will appear.\n\n" +
        "Answer the programming puzzle correctly to completely BLOCK the incoming damage. Fail, or run out of time, and you will take full damage.\n\n" +
        "You can toggle puzzles on/off in the Settings menu, but puzzles are the best way to survive!",

        "On your turn, select an action:\n\n" +
        "⚔ FIGHT: Use skills. Some hit one target, some hit all. Watch your MP (Mana).\n" +
        "🛡 DEFEND: Halve incoming damage and restore 10 MP.\n" +
        "🎒 ITEMS: Drink Potions (Heal HP) or Ethers (Restore MP).\n" +
        "★ ULT: Once your Ultimate gauge is full, unleash a devastating cinematic attack.",

        "• TOP LEFT: Your Heroes' HP, MP, and Ultimate Charge.\n" +
        "• TOP CENTER: Turn Order. Shows who acts next.\n" +
        "• BOTTOM RIGHT: The targeted Enemy's info. Click an enemy to target them.\n" +
        "• BOTTOM CENTER: The Action Menu and Battle Log.",

        "1. Use DEFEND when you are low on HP or need MP to cast a spell.\n\n" +
        "2. Piercing skills ignore enemy armor (DEF).\n\n" +
        "3. Keep an eye on the enemy's status. If they are 'Charging Special', brace for a big hit or use Defend.\n\n" +
        "4. Level up your heroes by defeating enemies to increase your stats."
    };

    public TutorialScreen(Runnable onComplete) {
        this.onComplete = onComplete;

        // Background
        Rectangle bg = new Rectangle();
        bg.widthProperty().bind(widthProperty());
        bg.heightProperty().bind(heightProperty());
        bg.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0a0508")),
                new Stop(0.5, Color.web("#1a0e2e")),
                new Stop(1, Color.web("#0a0508"))));

        // Glow effect
        Rectangle spotlight = new Rectangle(800, 600);
        spotlight.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#facc15", 0.10)),
                new Stop(1, Color.TRANSPARENT)));
        spotlight.setMouseTransparent(true);

        // Content Card
        titleLabel = new Label();
        titleLabel.setStyle("-fx-text-fill: #facc15; -fx-font-size: 26px; " +
                "-fx-font-weight: bold; -fx-font-family: 'Georgia';");
        titleLabel.setEffect(new DropShadow(15, Color.web("#facc15", 0.5)));

        bodyLabel = new Label();
        bodyLabel.setWrapText(true);
        bodyLabel.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 16px; " +
                "-fx-font-family: 'Consolas', monospace; -fx-line-spacing: 4;");
        bodyLabel.setMinHeight(250);
        bodyLabel.setAlignment(Pos.TOP_LEFT);

        pageLabel = new Label();
        pageLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px; -fx-font-family: 'Consolas';");

        contentBox = new VBox(20, titleLabel, bodyLabel, pageLabel);
        contentBox.setAlignment(Pos.TOP_LEFT);
        contentBox.setPadding(new Insets(35, 45, 35, 45));
        contentBox.setMaxWidth(800);
        contentBox.setStyle("-fx-background-color: rgba(20,6,12,0.95); " +
                "-fx-border-color: #facc15; -fx-border-width: 2; " +
                "-fx-border-radius: 12; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, #000, 30, 0.5, 0, 10);");

        // Navigation Buttons
        Button prevBtn = makeButton("◄ PREVIOUS", "#475569");
        Button nextBtn = makeButton("NEXT ►", "#38bdf8");
        Button skipBtn = makeButton("SKIP TUTORIAL", "#ef4444");

        prevBtn.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                updatePage();
            }
        });

        nextBtn.setOnAction(e -> {
            if (currentPage < titles.length - 1) {
                currentPage++;
                updatePage();
            } else {
                finishTutorial();
            }
        });

        skipBtn.setOnAction(e -> finishTutorial());

        HBox navBar = new HBox(15, skipBtn, new StackPane() {{ getChildren().add(new javafx.scene.layout.Region()); HBox.setHgrow(this, javafx.scene.layout.Priority.ALWAYS); }}, prevBtn, nextBtn);
        navBar.setAlignment(Pos.CENTER);
        navBar.setPadding(new Insets(20, 0, 0, 0));
        navBar.setMaxWidth(800);

        // Store references to update later
        this.getProperties().put("prevBtn", prevBtn);
        this.getProperties().put("nextBtn", nextBtn);

        VBox mainLayout = new VBox(20, contentBox, navBar);
        mainLayout.setAlignment(Pos.CENTER);

        getChildren().addAll(bg, spotlight, mainLayout);

        updatePage();
    }

    private void updatePage() {
        // Fade animation for text change
        FadeTransition ft = new FadeTransition(Duration.millis(200), contentBox);
        ft.setFromValue(0.3);
        ft.setToValue(1.0);
        TranslateTransition tt = new TranslateTransition(Duration.millis(200), contentBox);
        tt.setFromY(10);
        tt.setToY(0);
        new ParallelTransition(ft, tt).play();

        titleLabel.setText(titles[currentPage]);
        bodyLabel.setText(bodies[currentPage]);
        pageLabel.setText("Page " + (currentPage + 1) + " of " + titles.length);

        Button prevBtn = (Button) this.getProperties().get("prevBtn");
        Button nextBtn = (Button) this.getProperties().get("nextBtn");

        prevBtn.setDisable(currentPage == 0);
        prevBtn.setOpacity(currentPage == 0 ? 0.3 : 1.0);

        if (currentPage == titles.length - 1) {
            nextBtn.setText("FINISH");
            nextBtn.setStyle(btnStyle("#22c55e", true));
        } else {
            nextBtn.setText("NEXT ►");
            nextBtn.setStyle(btnStyle("#38bdf8", false));
        }
    }

    private void finishTutorial() {
        if (onComplete != null) {
            FadeTransition ft = new FadeTransition(Duration.millis(400), this);
            ft.setToValue(0);
            ft.setOnFinished(e -> onComplete.run());
            ft.play();
        }
    }

    private Button makeButton(String text, String color) {
        Button b = new Button(text);
        b.setPrefSize(160, 45);
        b.setStyle(btnStyle(color, false));
        b.setOnMouseEntered(e -> b.setStyle(btnStyle(color, true)));
        b.setOnMouseExited(e -> b.setStyle(btnStyle(color, false)));
        return b;
    }

    private String btnStyle(String c, boolean hover) {
        return "-fx-background-color: " + (hover ? c : "rgba(255,255,255,0.03)") + ";" +
               "-fx-border-color: " + c + ";" +
               "-fx-border-width: 2;" +
               "-fx-border-radius: 8;" +
               "-fx-background-radius: 8;" +
               "-fx-text-fill: " + (hover ? "#0a0c13" : c) + ";" +
               "-fx-font-size: 14px;" +
               "-fx-font-weight: bold;" +
               "-fx-font-family: 'Consolas', monospace;" +
               "-fx-cursor: hand;";
    }
}