package com.arena;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.List;

public class Game {
    private final Stage stage;
    private final StackPane root;
    private final StackPane storyLayer;
    private final Settings settings = new Settings();

    public Game(Stage stage) {
        this.stage = stage;
        this.root = new StackPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #0a0c13, #141a2c);");

        storyLayer = new StackPane();
        storyLayer.setVisible(false);
        storyLayer.setPickOnBounds(true);
        root.getChildren().add(storyLayer);
    }

    public Settings getSettings() { return settings; }

    public void start() {
        Scene scene = new Scene(root, 1280, 900);
        stage.setScene(scene);
        stage.setTitle("Arena of Echoes — A Coding RPG");
        stage.setResizable(true);
        stage.setMinWidth(1000);
        stage.setMinHeight(760);
        stage.show();
        showMenu();
    }

    private void setContent(javafx.scene.Node node) {
        root.getChildren().removeIf(n -> n != storyLayer);
        root.getChildren().add(0, node);
    }

    public void showMenu() {
        storyLayer.setVisible(false);
        storyLayer.getChildren().clear();
        setContent(new MenuScreen(this));
    }

    private void startBattleWithIntroDirect(int numPlayers) {
        storyLayer.setVisible(false);
        storyLayer.getChildren().clear();
        setContent(new IntroScene(this, numPlayers));
    }

    public void startBattleWithIntro(int numPlayers) {
        if (!settings.tutorialSeen) {
            // Show tutorial first, then start the battle
            showTutorial(() -> {
                settings.tutorialSeen = true;
                startBattleWithIntroDirect(numPlayers);
            });
        } else {
            startBattleWithIntroDirect(numPlayers);
        }
    }

      public void showTutorial(Runnable onComplete) {
        storyLayer.setVisible(false);
        storyLayer.getChildren().clear();
        setContent(new TutorialScreen(() -> {
            if (onComplete != null) {
                onComplete.run();
            } else {
                showMenu();
            }
        }));
    }

    public void startBattleWithCinematic(int numPlayers) {
        storyLayer.setVisible(false);
        storyLayer.getChildren().clear();
        setContent(new BattleScreen(this, numPlayers));
    }

    public void startBattle(int numPlayers) {
        startBattleWithIntro(numPlayers);
    }

    public void showStory(List<StoryLine> script, Runnable onComplete) {
        StoryScene scene = new StoryScene(script, () -> {
            storyLayer.getChildren().clear();
            storyLayer.setVisible(false);
            if (onComplete != null) onComplete.run();
        });
        storyLayer.getChildren().clear();
        storyLayer.getChildren().add(scene);
        storyLayer.setVisible(true);
    }

    public void showCodex() {
        storyLayer.setVisible(false);
        storyLayer.getChildren().clear();
        setContent(new CodexScreen(this::showMenu));
    }

    public void unlockCodex(String id) {
        StoryCodex.unlock(id);
    }
    
}