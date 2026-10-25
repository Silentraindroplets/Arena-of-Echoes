package com.arena;

import com.arena.model.Combatant;
import com.arena.model.Enemy;
import com.arena.model.Hero;
import com.arena.model.Skill;
import com.arena.puzzles.Puzzle;
import com.arena.puzzles.PuzzleBank;
import com.arena.ui.Battler;
import com.arena.ui.PauseMenu;
import com.arena.ui.PuzzleModal;
import com.arena.ui.SkillFx;
import com.arena.ui.Sprite;
import com.arena.ui.Sprites;
import com.arena.ui.StatBar;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BattleScreen extends StackPane {

    private static final int MAX_WAVES    = 5;
    private static final int SPRITE_SCALE = 6;

    private final Random rng = new Random();
    private final Game game;
    private final Settings settings;

    private final List<Hero> heroes = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Battler> heroBattlers = new ArrayList<>();
    private final List<Battler> enemyBattlers = new ArrayList<>();

    private List<Combatant> turnQueue = new ArrayList<>();
    private int turnIndex = 0;
    private int round = 0;
    private int wave = 0;
    private Combatant current;
    private Enemy selectedEnemy;
    private boolean lastWasCrit = false;

    private boolean awaitingInput = false;
    private boolean gameOver = false;
    private boolean paused = false;
    private boolean showingIntro = false;

    private StackPane overlayLayer;
    private StackPane modalLayer;
    private PuzzleModal puzzleModal;
    private PauseMenu pauseMenu;

    private Pane arenaPane;
    private Ellipse heroPlatform, enemyPlatform;
    
    private HBox turnOrderBar;
    private VBox logBox;
    private ScrollPane logScroll;
    private Label waveLabel, roundLabel, turnLabel;
    private Button pauseBtn;

    // ── new Pokémon-style UI ──
    private VBox enemyInfoPanel;
    private Label enemyInfoName, enemyInfoHpText;
    private Rectangle enemyHpBg, enemyHpFill;

    private VBox heroInfoContainer;
    private final List<HeroInfoStrip> heroStrips = new ArrayList<>();

    private GridPane actionGrid;
    private Label promptLabel;
    private MenuState menuState = MenuState.ROOT;

    private enum MenuState { ROOT, SKILLS, ITEMS }

    private static class HeroInfoStrip {
        VBox root;
        StatBar hpBar, mpBar;
        Label hpText, mpText, ultText;
        Hero hero;

        void refresh() {
            hpBar.setRatio(hero.hpRatio(), true);
            mpBar.setRatio(hero.mpRatio(), true);
            hpText.setText(hero.hp + " / " + hero.maxHp);
            mpText.setText(hero.mp + " / " + hero.maxMp);
            boolean ult = hero.hasUlt();
            ultText.setText(ult ? "★ ULT READY" : "ULT " + (int) (hero.ultRatio() * 100) + "%");
            ultText.setStyle("-fx-text-fill: " + (ult ? "#facc15" : "#64748b") + ";" +
                    "-fx-font-size: 10px; -fx-font-weight: bold;");
        }
    }

    public BattleScreen(Game game, int numPlayers) {
        this.game = game;
        this.settings = game.getSettings();

        BorderPane main = new BorderPane();
        main.setTop(buildHeader());
        main.setCenter(buildArena());
        main.setBottom(buildBottom());

        overlayLayer = new StackPane();
        overlayLayer.setVisible(false);
        overlayLayer.setPickOnBounds(true);

        puzzleModal = new PuzzleModal();
        pauseMenu = new PauseMenu(settings, this::resumeFromPause, () -> game.showMenu());

        modalLayer = new StackPane(puzzleModal, pauseMenu);
        modalLayer.setPickOnBounds(false);

        getChildren().addAll(main, overlayLayer, modalLayer);

        heroes.add(Hero.knight());
        if (numPlayers >= 2) heroes.add(Hero.mage());

        setFocusTraversable(true);
        setOnKeyPressed(this::handleKey);

        initGame();
        Platform.runLater(() -> { requestFocus(); layoutBattlers(); });
    }

    private Node buildHeader() {
        HBox header = new HBox(14);
        header.setPadding(new Insets(10, 22, 8, 22));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #0a0d1a; -fx-border-color: #3d0a10; -fx-border-width: 0 0 1 0;");

        Label title = new Label("ARENA OF ECHOES");
        title.setStyle("-fx-text-fill: #fca5a5; -fx-font-size: 15px; -fx-font-weight: bold; -fx-font-family: 'Georgia';");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        turnLabel  = headLabel("—", "#ff6b6b");
        roundLabel = headLabel("ROUND 1", "#94a3b8");
        waveLabel  = headLabel("WAVE 1 / " + MAX_WAVES, "#f97316");

        pauseBtn = new Button("❚❚  Pause  (Esc)");
        pauseBtn.setStyle(pauseStyle(false));
        pauseBtn.setOnMouseEntered(e -> { if (!pauseBtn.isDisabled()) pauseBtn.setStyle(pauseStyle(true)); });
        pauseBtn.setOnMouseExited(e  -> pauseBtn.setStyle(pauseStyle(false)));
        pauseBtn.setOnAction(e -> togglePause());
        pauseBtn.setDisable(true);

        header.getChildren().addAll(title, spacer, turnLabel, sep(), roundLabel, sep(), waveLabel, pauseBtn);
        return header;
    }

    private Label sep() {
        Label l = new Label("·");
        l.setStyle("-fx-text-fill: #6b2020; -fx-font-size: 13px;");
        return l;
    }

    private Label headLabel(String text, String color) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12.5px; -fx-font-weight: bold;");
        return l;
    }

    private String pauseStyle(boolean hover) {
        String c = "#94a3b8";
        return "-fx-background-color: " + (hover ? c : "rgba(255,255,255,0.03)") + ";" +
               "-fx-border-color: " + c + ";" +
               "-fx-border-width: 1;" +
               "-fx-border-radius: 6;" +
               "-fx-background-radius: 6;" +
               "-fx-text-fill: " + (hover ? "#0a0c13" : c) + ";" +
               "-fx-font-size: 11px;" +
               "-fx-font-weight: bold;" +
               "-fx-padding: 4 12;" +
               "-fx-cursor: hand;";
    }

    // ==================================================================
    //  ARENA (Pokémon-style Hellish Background)
    // ==================================================================
    private Node buildArena() {
        arenaPane = new Pane();
        arenaPane.setStyle("-fx-background-color: #0a0205;");

        // Sky gradient (Blood moon backdrop)
        Rectangle sky = new Rectangle();
        sky.widthProperty().bind(arenaPane.widthProperty());
        sky.heightProperty().bind(arenaPane.heightProperty().multiply(0.75));
        sky.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0a0205")),
                new Stop(0.5, Color.web("#2a0812")),
                new Stop(1, Color.web("#5c0606"))));
        arenaPane.getChildren().add(sky);

        // Blood Moon & Glow
        Circle moonGlow = new Circle();
        moonGlow.radiusProperty().bind(arenaPane.widthProperty().multiply(0.18));
        moonGlow.centerXProperty().bind(arenaPane.widthProperty().multiply(0.5));
        moonGlow.centerYProperty().bind(arenaPane.heightProperty().multiply(0.3));
        moonGlow.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#ff4040", 0.45)),
                new Stop(1, Color.TRANSPARENT)));
        moonGlow.setMouseTransparent(true);
        arenaPane.getChildren().add(moonGlow);

        Circle moon = new Circle();
        moon.radiusProperty().bind(arenaPane.widthProperty().multiply(0.06));
        moon.centerXProperty().bind(arenaPane.widthProperty().multiply(0.5));
        moon.centerYProperty().bind(arenaPane.heightProperty().multiply(0.3));
        moon.setFill(new RadialGradient(0, 0, 0.4, 0.4, 0.65, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#ff6060")),
                new Stop(0.55, Color.web("#c41010")),
                new Stop(1, Color.web("#5c0606"))));
        moon.setEffect(new DropShadow(60, Color.web("#ff2020", 0.95)));
        arenaPane.getChildren().add(moon);

        // Jagged Hell Mountains (Silhouette)
        Polygon mountains = new Polygon();
        mountains.setFill(Color.web("#0f0205"));
        arenaPane.getChildren().add(mountains);

        Runnable buildMountains = () -> {
            double W = arenaPane.getWidth();
            double H = arenaPane.getHeight();
            if (W <= 0 || H <= 0) return;
            mountains.getPoints().clear();
            mountains.getPoints().addAll(
                0.0, H * 0.75, 
                W * 0.1, H * 0.45, 
                W * 0.2, H * 0.65, 
                W * 0.35, H * 0.35, 
                W * 0.45, H * 0.60,
                W * 0.55, H * 0.40,
                W * 0.70, H * 0.70,
                W * 0.85, H * 0.50,
                W,      H * 0.75,
                W,      H, 
                0.0,    H
            );
        };
        arenaPane.widthProperty().addListener((o, ov, nv) -> buildMountains.run());
        arenaPane.heightProperty().addListener((o, ov, nv) -> buildMountains.run());

        // Ground Arena Floor
        Rectangle ground = new Rectangle();
        ground.widthProperty().bind(arenaPane.widthProperty());
        ground.yProperty().bind(arenaPane.heightProperty().multiply(0.75));
        ground.heightProperty().bind(arenaPane.heightProperty().multiply(0.25));
        ground.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#3a1005")),
                new Stop(1, Color.web("#0a0205"))));
        arenaPane.getChildren().add(ground);

        // Horizon Line
        Line horizon = new Line();
        horizon.startXProperty().set(0);
        horizon.endXProperty().bind(arenaPane.widthProperty());
        horizon.startYProperty().bind(arenaPane.heightProperty().multiply(0.75));
        horizon.endYProperty().bind(arenaPane.heightProperty().multiply(0.75));
        horizon.setStroke(Color.web("#ff6020", 0.6));
        horizon.setStrokeWidth(2);
        horizon.setEffect(new DropShadow(15, Color.web("#ff4010")));
        arenaPane.getChildren().add(horizon);

        // Perspective Grid
        for (int i = -5; i <= 5; i++) {
            Line tile = new Line();
            tile.setStroke(Color.web("#8b3010", 0.25));
            tile.setStrokeWidth(1);
            tile.startXProperty().bind(arenaPane.widthProperty().multiply(0.5));
            tile.startYProperty().bind(arenaPane.heightProperty().multiply(0.75));
            tile.endXProperty().bind(arenaPane.widthProperty().multiply(0.5 + i * 0.2));
            tile.endYProperty().bind(arenaPane.heightProperty());
            arenaPane.getChildren().add(tile);
        }
        for (int i = 1; i <= 4; i++) {
            Line depth = new Line();
            depth.setStroke(Color.web("#8b3010", 0.25));
            depth.setStrokeWidth(1);
            depth.startXProperty().set(0);
            depth.endXProperty().bind(arenaPane.widthProperty());
            depth.startYProperty().bind(arenaPane.heightProperty().multiply(0.75 + i * 0.0625));
            depth.endYProperty().bind(arenaPane.heightProperty().multiply(0.75 + i * 0.0625));
            arenaPane.getChildren().add(depth);
        }

        // Pokémon-style Platforms
        heroPlatform = new Ellipse();
        heroPlatform.centerXProperty().bind(arenaPane.widthProperty().multiply(0.22));
        heroPlatform.centerYProperty().bind(arenaPane.heightProperty().multiply(0.76));
        heroPlatform.radiusXProperty().bind(arenaPane.widthProperty().multiply(0.18));
        heroPlatform.radiusYProperty().bind(arenaPane.heightProperty().multiply(0.07));
        heroPlatform.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#7c5cff", 0.25)),
                new Stop(0.7, Color.web("#2a1010", 0.8)),
                new Stop(1, Color.TRANSPARENT)));
        heroPlatform.setStroke(Color.web("#7c5cff", 0.4));
        heroPlatform.setStrokeWidth(2);

        enemyPlatform = new Ellipse();
        enemyPlatform.centerXProperty().bind(arenaPane.widthProperty().multiply(0.72));
        enemyPlatform.centerYProperty().bind(arenaPane.heightProperty().multiply(0.66));
        enemyPlatform.radiusXProperty().bind(arenaPane.widthProperty().multiply(0.20));
        enemyPlatform.radiusYProperty().bind(arenaPane.heightProperty().multiply(0.08));
        enemyPlatform.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#dc2626", 0.25)),
                new Stop(0.7, Color.web("#2a1010", 0.8)),
                new Stop(1, Color.TRANSPARENT)));
        enemyPlatform.setStroke(Color.web("#dc2626", 0.4));
        enemyPlatform.setStrokeWidth(2);

        arenaPane.getChildren().addAll(heroPlatform, enemyPlatform);

        // Embers
        for (int i = 0; i < 25; i++) {
            Circle ember = new Circle(1 + rng.nextDouble() * 2);
            ember.setFill(Color.web(rng.nextBoolean() ? "#ff6020" : "#ff2020", 0.85));
            ember.setEffect(new DropShadow(8, Color.web("#ff4010")));
            ember.setLayoutX(rng.nextDouble() * 1200);
            ember.setLayoutY(rng.nextDouble() * 600);
            ember.setMouseTransparent(true);
            arenaPane.getChildren().add(ember);

            TranslateTransition rise = new TranslateTransition(Duration.millis(3500 + rng.nextInt(3000)), ember);
            rise.setByY(-500 - rng.nextInt(200));
            rise.setByX((rng.nextDouble() - 0.5) * 100);
            rise.setCycleCount(TranslateTransition.INDEFINITE);
            rise.play();
            FadeTransition flick = new FadeTransition(Duration.millis(800 + rng.nextInt(900)), ember);
            flick.setFromValue(0.2); flick.setToValue(1.0);
            flick.setAutoReverse(true);
            flick.setCycleCount(FadeTransition.INDEFINITE);
            flick.play();
        }

        // UI Elements floating over arena
        turnOrderBar = new HBox(6);
        turnOrderBar.setAlignment(Pos.CENTER);
        turnOrderBar.setPadding(new Insets(6, 10, 6, 10));
        turnOrderBar.setStyle("-fx-background-color: rgba(30,8,15,0.72); " +
                "-fx-background-radius: 10; -fx-border-color: #6b2020; " +
                "-fx-border-radius: 10; -fx-border-width: 1;");
        turnOrderBar.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        enemyInfoPanel = buildEnemyInfoPanel();
        enemyInfoPanel.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        enemyInfoPanel.setTranslateX(20);
        enemyInfoPanel.setTranslateY(20);

        heroInfoContainer = new VBox(6);
        heroInfoContainer.setAlignment(Pos.BOTTOM_RIGHT);
        heroInfoContainer.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        heroInfoContainer.setTranslateX(-20);
        heroInfoContainer.setTranslateY(-20);

        StackPane wrapper = new StackPane(arenaPane, turnOrderBar, enemyInfoPanel, heroInfoContainer);
        StackPane.setAlignment(turnOrderBar,      Pos.TOP_CENTER);
        StackPane.setAlignment(enemyInfoPanel,    Pos.TOP_LEFT);
        StackPane.setAlignment(heroInfoContainer, Pos.BOTTOM_RIGHT);

        arenaPane.widthProperty().addListener((o, ov, nv) -> layoutBattlers());
        arenaPane.heightProperty().addListener((o, ov, nv) -> layoutBattlers());

        Platform.runLater(buildMountains::run);
        return wrapper;
    }

    private void layoutBattlers() {
        double w = arenaPane.getWidth();
        double h = arenaPane.getHeight();
        if (w <= 0 || h <= 0) return;

        int nh = heroBattlers.size();
        if (nh > 0) {
            double[][] hr = nh == 1
                    ? new double[][] { { 0.22, 0.60 } }
                    : new double[][] { { 0.14, 0.63 }, { 0.30, 0.60 } };
            for (int i = 0; i < nh; i++) {
                heroBattlers.get(i).setLayoutX(w * hr[i][0]);
                heroBattlers.get(i).setLayoutY(h * hr[i][1]);
            }
        }

        int ne = enemyBattlers.size();
        if (ne > 0) {
            double[][] er;
            if (ne == 1)      er = new double[][] { { 0.72, 0.50 } };
            else if (ne == 2) er = new double[][] { { 0.62, 0.55 }, { 0.82, 0.48 } };
            else              er = new double[][] { { 0.56, 0.58 }, { 0.72, 0.50 }, { 0.88, 0.55 } };
            for (int i = 0; i < ne; i++) {
                enemyBattlers.get(i).setLayoutX(w * er[i][0]);
                enemyBattlers.get(i).setLayoutY(h * er[i][1]);
            }
        }
    }

    private double bx(Battler b) { return b.getBoundsInParent().getCenterX(); }
    private double by(Battler b) { return b.getBoundsInParent().getCenterY(); }

    // ==================================================================
    //  BOTTOM UI (Fixed Height + Action Grid)
    // ==================================================================
    private Node buildBottom() {
        VBox bottom = new VBox(0);
        bottom.setStyle("-fx-background-color: #0a0d1a;");
        // HARD FIX: Lock the height so it doesn't stretch
        bottom.setPrefHeight(170);
        bottom.setMinHeight(170);
        bottom.setMaxHeight(170);

        // ── battle log ──
        logBox = new VBox(2);
        logBox.setPadding(new Insets(10, 20, 10, 20));
        logBox.setStyle("-fx-background-color: #10050a;");

        logScroll = new ScrollPane(logBox);
        logScroll.setFitToWidth(true);
        logScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        logScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        logScroll.setStyle("-fx-background: #10050a; -fx-background-color: #10050a; " +
                "-fx-border-color: #3d0a10; -fx-border-width: 2 1 0 0;");

        // ── action area (prompt + 2×2 grid) ──
        promptLabel = new Label("What will you do?");
        promptLabel.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 15px; " +
                "-fx-font-family: 'Georgia'; -fx-font-style: italic; -fx-font-weight: bold;");

        actionGrid = new GridPane();
        actionGrid.setHgap(10);
        actionGrid.setVgap(10);
        actionGrid.setAlignment(Pos.CENTER);

        VBox actionArea = new VBox(12, promptLabel, actionGrid);
        actionArea.setAlignment(Pos.CENTER);
        actionArea.setPadding(new Insets(12, 20, 16, 20));
        
        // HARD FIX: Lock the width so the buttons don't stretch weirdly
        actionArea.setMinWidth(400);
        actionArea.setPrefWidth(400);
        actionArea.setMaxWidth(400);
        actionArea.setStyle("-fx-background-color: #0e1220; " +
                "-fx-border-color: #3d0a10; -fx-border-width: 2 0 0 2;");

        HBox row = new HBox(0);
        HBox.setHgrow(logScroll, Priority.ALWAYS);
        row.getChildren().addAll(logScroll, actionArea);
        row.setStyle("-fx-background-color: #10050a; -fx-border-color: #3d0a10; -fx-border-width: 1 0 0 0;");
        
        VBox.setVgrow(row, Priority.ALWAYS);
        bottom.getChildren().add(row);
        return bottom;
    }

    private void initGame() {
        gameOver = false; awaitingInput = false; paused = false;
        wave = 0; round = 0; turnIndex = 0;
        selectedEnemy = null; current = null;

        StoryCodex.unlock("prologue");

        heroBattlers.clear();
        for (Hero h : heroes) {
            heroBattlers.add(new Battler(h, true, null, SPRITE_SCALE, "P" + (heroBattlers.size() + 1)));
        }

        log("The black doors of the Demon Castle grind shut behind you.", "#94a3b8");
        log("Embers drift up from cracks in the stone. Somewhere far below, something laughs.", "#f97316");
        log("When an enemy attacks, a CODE CHALLENGE appears.", "#facc15");
        log("Answer correctly to block the blow.", "#94a3b8");
        log("", "#000000");

        nextWave();
        Platform.runLater(this::buildHeroStrips);
    }

    // ---------------- chapter metadata ----------------
    private String chapterNumber(int waveIndex) {
        return switch (waveIndex) { case 1 -> "I"; case 2 -> "II"; case 3 -> "III"; case 4 -> "IV"; default -> "V"; };
    }
    private String chapterTitle(int waveIndex) {
        return switch (waveIndex) { case 1 -> "THE GATE"; case 2 -> "THE WARDEN"; case 3 -> "THE FALLEN LEGION"; case 4 -> "THE ELITE GUARD"; default -> "THE ASHEN KING"; };
    }
    private String chapterSubtitle(int waveIndex) {
        return switch (waveIndex) { case 1 -> "Where every story in this Arena begins"; case 2 -> "A crest on the beast's chest"; case 3 -> "The ones who wore your colors"; case 4 -> "The last line before the throne"; default -> "He has been waiting for you"; };
    }

    private void nextWave() {
        wave++;
        waveLabel.setText("WAVE " + wave + " / " + MAX_WAVES);

        StoryCodex.unlock(switch (wave) {
            case 2 -> "warden-crest";
            case 3 -> "fallen-knight";
            case 4 -> "the-ask";
            default -> null;
        });

        showingIntro = true;
        ChapterSplash splash = new ChapterSplash(
                chapterNumber(wave),
                chapterTitle(wave),
                chapterSubtitle(wave),
                () -> {
                    getChildren().remove(getChildren().size() - 1);
                    playWaveIntro();
                });
        getChildren().add(splash);
    }

    private void playWaveIntro() {
        double s = (1.0 + 0.13 * (wave - 1)) * settings.enemyScale();
        List<Enemy> spawn = new ArrayList<>();
        switch (wave) {
            case 1 -> { spawn.add(Enemy.goblin(s)); spawn.add(Enemy.goblin(s)); }
            case 2 -> { spawn.add(Enemy.orc(s));    spawn.add(Enemy.goblin(s)); }
            case 3 -> { spawn.add(Enemy.skeleton(s)); spawn.add(Enemy.skeleton(s)); spawn.add(Enemy.darkMage(s)); }
            case 4 -> { spawn.add(Enemy.orc(s));    spawn.add(Enemy.darkMage(s)); spawn.add(Enemy.skeleton(s)); }
            default -> { spawn.add(Enemy.demon(s)); spawn.add(Enemy.goblin(s));   spawn.add(Enemy.goblin(s)); }
        }

        for (Battler b : heroBattlers) arenaPane.getChildren().remove(b);
        for (Battler b : enemyBattlers) arenaPane.getChildren().remove(b);

        List<String> spriteKeys = new ArrayList<>();
        List<String> colors = new ArrayList<>();
        for (Enemy e : spawn) { spriteKeys.add(e.spriteKey()); colors.add(e.colorHex()); }

        BattleIntro intro = new BattleIntro("WAVE " + wave, spriteKeys, colors, () -> {
            getChildren().remove(getChildren().size() - 1);
            showingIntro = false;
            doSpawnWave(spawn);
            beginTurn();
        });
        getChildren().add(intro);
    }

    private void doSpawnWave(List<Enemy> spawn) {
        for (Battler b : enemyBattlers) arenaPane.getChildren().remove(b);
        enemies.clear(); enemyBattlers.clear();

        for (Enemy e : spawn) {
            enemies.add(e);
            Battler b = new Battler(e, false, this::selectEnemy, SPRITE_SCALE, null);
            enemyBattlers.add(b);
            arenaPane.getChildren().add(b);
        }
        selectedEnemy = enemies.get(0);

        for (Battler b : heroBattlers) if (!arenaPane.getChildren().contains(b)) {
            arenaPane.getChildren().add(b);
        }

        layoutBattlers();

        StringBuilder sb = new StringBuilder("Wave " + wave + " — ");
        for (int i = 0; i < spawn.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(spawn.get(i).name);
        }
        sb.append(" emerge from the shadows!");
        log(sb.toString(), wave == MAX_WAVES ? "#ff2020" : "#f87171");
        log("", "#000000");

        refreshUI();
    }

    // ==================================================================
    //  TURN ENGINE
    // ==================================================================

    private void buildQueue() {
        turnQueue = new ArrayList<>();
        for (Hero h : heroes) if (h.isAlive()) turnQueue.add(h);
        for (Enemy e : enemies) if (e.isAlive()) turnQueue.add(e);
        turnQueue.sort((a, b) -> Integer.compare(b.spd, a.spd));
        turnIndex = 0;
    }

    private void beginTurn() {
        if (gameOver || paused || showingIntro) return;
        if (checkBattleEnd()) return;

        while (true) {
            if (turnQueue == null || turnQueue.isEmpty() || turnIndex >= turnQueue.size()) {
                round++;
                roundLabel.setText("ROUND " + round);
                buildQueue();
                log("── Round " + round + " ──", "#6b2020");
            }
            if (turnIndex >= turnQueue.size()) return;
            if (turnQueue.get(turnIndex).isAlive()) break;
            turnIndex++;
        }

        current = turnQueue.get(turnIndex);
        if (current.defending) current.defending = false;
        current.tickCooldowns();

        if (current.poison > 0) {
            current.poison--;
            int pd = 8;
            current.takeDamage(pd);
            log(current.name + " suffers " + pd + " poison damage (" + current.poison + " turns left).", "#a3e635");
            Battler b = battlerFor(current);
            if (b != null) {
                SkillFx.damageNumber(arenaPane, bx(b), by(b), pd, Color.web("#a3e635"), false);
                SkillFx.impact(arenaPane, bx(b), by(b), Color.web("#a3e635"));
            }
            refreshUI();

            if (!current.isAlive()) {
                log(current.name + " succumbs to the poison!", "#a3e635");
                refreshUI();
                if (checkBattleEnd()) return;
                turnIndex++;
                PauseTransition p = new PauseTransition(Duration.millis(550));
                p.setOnFinished(e -> beginTurn());
                p.play();
                return;
            }
        }

        highlightCurrent();
        updateTurnBanner(current);
        updateTurnOrderPreview();

        if (current instanceof Hero hero) {
            awaitingInput = true;
            pauseBtn.setDisable(false);
            buildActionBar(hero);
            refreshUI();
            Platform.runLater(this::requestFocus);
        } else {
            awaitingInput = false;
            pauseBtn.setDisable(true);
            showWaitingBar();
            refreshUI();
            PauseTransition p = new PauseTransition(Duration.millis(600));
            p.setOnFinished(e -> enemyAction((Enemy) current));
            p.play();
        }
    }

    private void endTurn() {
        refreshUI();
        if (checkBattleEnd()) return;
        turnIndex++;
        PauseTransition p = new PauseTransition(Duration.millis(450));
        p.setOnFinished(e -> beginTurn());
        p.play();
    }

    private boolean checkBattleEnd() {
        boolean allHeroesDead = heroes.stream().noneMatch(Combatant::isAlive);
        if (allHeroesDead) { defeat(); return true; }

        boolean anyEnemyAlive = enemies.stream().anyMatch(Combatant::isAlive);
        if (anyEnemyAlive) return false;

        if (wave >= MAX_WAVES) { victory(); return true; }

        log("", "#000000");
        log("Wave " + wave + " cleared!", "#4ade80");
        for (Hero h : heroes) {
            if (!h.isAlive()) continue;
            int heal = (int) (h.maxHp * 0.30);
            h.heal(heal);
            h.restoreMp(15);
            h.potions++;
        }
        log("The party recovers: +30% HP, +15 MP, +1 Potion each.", "#4ade80");
        log("", "#000000");

        StoryCodex.unlock(switch (wave) { case 1 -> "malgoroth-first"; default -> null; });

        awaitingInput = false;
        refreshUI();

        List<StoryLine> interlude = StoryScript.interludeAfterWave(wave, heroes.size());
        game.showStory(interlude, () -> {
            turnQueue = new ArrayList<>();
            turnIndex = 0;
            nextWave();
        });
        return true;
    }

    private void updateTurnBanner(Combatant c) {
        if (c instanceof Hero h) {
            int idx = heroes.indexOf(h) + 1;
            promptLabel.setText("What will " + h.name + " do?   (Player " + idx + ")");
            promptLabel.setStyle("-fx-text-fill: " + h.colorHex() + "; -fx-font-size: 15px; " +
                    "-fx-font-family: 'Georgia'; -fx-font-style: italic; -fx-font-weight: bold;");
            turnLabel.setText("PLAYER " + idx);
            turnLabel.setStyle("-fx-text-fill: " + h.colorHex() + "; -fx-font-size: 12.5px; -fx-font-weight: bold;");
        } else {
            promptLabel.setText(c.name + " is acting…");
            promptLabel.setStyle("-fx-text-fill: #f87171; -fx-font-size: 15px; " +
                    "-fx-font-family: 'Georgia'; -fx-font-style: italic; -fx-font-weight: bold;");
            turnLabel.setText("ENEMY");
            turnLabel.setStyle("-fx-text-fill: #f87171; -fx-font-size: 12.5px; -fx-font-weight: bold;");
        }
        menuState = MenuState.ROOT;
    }

    // ==================================================================
    //  ACTION MENUS
    // ==================================================================

    private void buildActionBar(Hero hero) {
        actionGrid.getChildren().clear();
        switch (menuState) {
            case ROOT   -> buildRootMenu(hero);
            case SKILLS -> buildSkillMenu(hero);
            case ITEMS  -> buildItemMenu(hero);
        }
    }

    private void buildRootMenu(Hero hero) {
        Button fight = pkmnButton("⚔  FIGHT", "#ef4444", "#7f1d1d", false, 160, 55);
        fight.setOnAction(e -> { menuState = MenuState.SKILLS; buildActionBar(hero); });

        Button defend = pkmnButton("🛡  DEFEND", "#22c55e", "#14532d", false, 160, 55);
        defend.setOnAction(e -> doHeroDefend(hero));

        Button items = pkmnButton("🎒  ITEMS", "#f59e0b", "#78350f",
                hero.potions <= 0 && hero.ethers <= 0, 160, 55);
        items.setOnAction(e -> { menuState = MenuState.ITEMS; buildActionBar(hero); });

        Button ult = pkmnButton("★  ULT", "#38bdf8", "#0369a1",
                !hero.hasUlt(), 160, 55);
        ult.setOnAction(e -> doHeroUltimate(hero));

        actionGrid.add(fight,  0, 0);
        actionGrid.add(defend, 1, 0);
        actionGrid.add(items,  0, 1);
        actionGrid.add(ult,    1, 1);
    }

    private void buildSkillMenu(Hero hero) {
        int col = 0, row = 0;
        for (Skill s : hero.skills) {
            int cd = hero.cooldownOf(s);
            boolean canUse = hero.mp >= s.mpCost && cd == 0;
            String label = s.name
                    + (s.mpCost > 0 ? "\n" + s.mpCost + " MP" : "")
                    + (cd > 0       ? "  (CD " + cd + ")"   : "");
            Button b = pkmnButton(label, hero.colorHex(), "#1e1b4b", !canUse, 160, 55);
            if (canUse) {
                final Skill sk = s;
                b.setOnAction(e -> doHeroSkill(hero, sk));
            }
            actionGrid.add(b, col, row);
            col++;
            if (col > 1) { col = 0; row++; }
        }
        Button back = pkmnButton("◄  BACK", "#475569", "#1e293b", false, 160, 42);
        back.setOnAction(e -> { menuState = MenuState.ROOT; buildActionBar(hero); });
        actionGrid.add(back, 0, row + 1, 2, 1);
    }

    private void buildItemMenu(Hero hero) {
        Button potion = pkmnButton("Potion ×" + hero.potions, "#22c55e", "#14532d",
                hero.potions <= 0, 160, 55);
        potion.setOnAction(e -> doHeroPotion(hero));

        Button ether = pkmnButton("Ether ×" + hero.ethers, "#38bdf8", "#0369a1",
                hero.ethers <= 0, 160, 55);
        ether.setOnAction(e -> doHeroEther(hero));

        Button back = pkmnButton("◄  BACK", "#475569", "#1e293b", false, 160, 42);
        back.setOnAction(e -> { menuState = MenuState.ROOT; buildActionBar(hero); });

        actionGrid.add(potion, 0, 0);
        actionGrid.add(ether,  1, 0);
        actionGrid.add(back,   0, 1, 2, 1);
    }

    private Button pkmnButton(String text, String bg, String border,
                              boolean disabled, double w, double h) {
        Button b = new Button(text);
        b.setPrefSize(w, h);
        b.setWrapText(true);
        b.setDisable(disabled);
        b.setStyle(pkmnStyle(bg, border, disabled, false));
        if (!disabled) {
            b.setOnMouseEntered(e -> b.setStyle(pkmnStyle(bg, border, false, true)));
            b.setOnMouseExited(e  -> b.setStyle(pkmnStyle(bg, border, false, false)));
        }
        return b;
    }

    private String pkmnStyle(String bg, String border, boolean disabled, boolean hover) {
        if (disabled) {
            return "-fx-background-color: #1f2937;" +
                   "-fx-border-color: #111827;" +
                   "-fx-border-width: 3;" +
                   "-fx-border-radius: 12;" +
                   "-fx-background-radius: 12;" +
                   "-fx-text-fill: #4b5563;" +
                   "-fx-font-size: 13px;" +
                   "-fx-font-weight: bold;" +
                   "-fx-font-family: 'Consolas', monospace;";
        }
        
        // Gradient based on the base color
        String fill = hover 
            ? "-fx-background-color: #ffffff;" 
            : "-fx-background-color: linear-gradient(to bottom, " + bg + ", derive(" + bg + ", -30%));";
            
        String edge = hover ? border : "derive(" + border + ", 20%)";
        String text = hover ? border : "#ffffff";

        return fill +
               "-fx-border-color: " + edge + ";" +
               "-fx-border-width: 3;" +
               "-fx-border-radius: 10;" +
               "-fx-background-radius: 10;" +
               "-fx-text-fill: " + text + ";" +
               "-fx-font-size: 13px;" +
               "-fx-font-weight: bold;" +
               "-fx-font-family: 'Georgia';" +
               "-fx-text-alignment: center;" +
               "-fx-cursor: hand;" +
               "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.65), 8, 0, 0, 4);";
    }

    private void doHeroDefend(Hero hero) {
        if (!awaitingInput || paused || showingIntro) return;
        awaitingInput = false;
        pauseBtn.setDisable(true);
        hero.defending = true;
        hero.restoreMp(10);
        log(hero.name + " braces for impact. (+10 MP)", "#38bdf8");
        Battler b = battlerFor(hero);
        if (b != null) {
            SkillFx.shield(arenaPane, bx(b), by(b), 90, Color.web("#38bdf8"));
            SkillFx.statusWord(arenaPane, bx(b), by(b), "GUARD", Color.web("#38bdf8"));
        }
        menuState = MenuState.ROOT;
        refreshUI();
        endTurn();
    }

    private void showWaitingBar() {
        actionGrid.getChildren().clear();
        Label l = new Label("· · ·  enemy is acting  · · ·");
        l.setStyle("-fx-text-fill: #6b2020; -fx-font-size: 14px; -fx-font-style: italic;");
        actionGrid.add(l, 0, 0, 2, 2);
        promptLabel.setText("The enemy makes its move…");
    }

    // ==================================================================
    //  HERO ACTIONS
    // ==================================================================

    private void doHeroSkill(Hero hero, Skill s) {
        if (!awaitingInput || paused || showingIntro) return;
        awaitingInput = false;
        pauseBtn.setDisable(true);

        hero.mp -= s.mpCost;
        if (s.cooldown > 0) hero.cooldowns.put(s, s.cooldown + 1);
        hero.grantUltCharge(10);

        Battler hb = battlerFor(hero);
        Color fxColor = Color.web(hero.colorHex());
        boolean knightStyle = "Tank".equals(hero.role);
        log("", "#000000");

        switch (s.type) {
            case ATTACK_ONE -> {
                Enemy target = currentEnemyTarget();
                if (target == null) { awaitingInput = true; return; }
                log(hero.name + " uses " + s.name + " on " + target.name + "!", hero.colorHex());

                Battler tb = battlerFor(target);
                if (hb != null && tb != null) {
                    SkillFx.castRunes(arenaPane, bx(hb), by(hb), fxColor);
                    if (knightStyle) {
                        SkillFx.knightSlash(arenaPane, bx(hb), by(hb), bx(tb), by(tb), fxColor);
                    } else {
                        SkillFx.slash(arenaPane, bx(hb), by(hb), bx(tb), by(tb), fxColor);
                    }
                }

                Runnable impact = () -> {
                    if (tb != null) SkillFx.impact(arenaPane, bx(tb), by(tb), fxColor);
                    impactEnemy(hero, target, s);
                };
                if (hb != null) hb.playAttack(impact);
                else impact.run();
            }
            case ATTACK_ALL -> {
                log(hero.name + " uses " + s.name + "!", hero.colorHex());

                if (hb != null) {
                    SkillFx.castRunes(arenaPane, bx(hb), by(hb), fxColor);
                    if (knightStyle) {
                        for (Battler eb : enemyBattlers) {
                            if (!eb.getCombatant().isAlive()) continue;
                            SkillFx.knightSlash(arenaPane, bx(hb), by(hb), bx(eb), by(eb), fxColor);
                        }
                    } else {
                        SkillFx.wave(arenaPane, bx(hb), by(hb), arenaPane.getWidth(), fxColor);
                    }
                }
                Rectangle flash = new Rectangle();
                flash.widthProperty().bind(arenaPane.widthProperty());
                flash.heightProperty().bind(arenaPane.heightProperty());
                flash.setFill(Color.web(hero.colorHex(), 0.18));
                flash.setMouseTransparent(true);
                arenaPane.getChildren().add(flash);
                FadeTransition ff = new FadeTransition(Duration.millis(420), flash);
                ff.setToValue(0);
                ff.setOnFinished(ev -> arenaPane.getChildren().remove(flash));
                ff.play();

                Runnable impact = () -> {
                    for (Battler eb : enemyBattlers) {
                        if (!eb.getCombatant().isAlive()) continue;
                        SkillFx.impact(arenaPane, bx(eb), by(eb), fxColor);
                    }
                    impactAllEnemies(hero, s);
                };
                if (hb != null) hb.playAttack(impact);
                else impact.run();
            }
            case HEAL_SELF -> {
                hero.heal(s.healAmount);
                log(hero.name + " uses " + s.name + " and recovers " + s.healAmount + " HP.", "#4ade80");
                Color green = Color.web("#4ade80");
                if (hb != null) {
                    SkillFx.heal(arenaPane, bx(hb), by(hb), green);
                    SkillFx.healNumber(arenaPane, bx(hb), by(hb), "+" + s.healAmount, green);
                }
                refreshUI(); endTurn();
            }
            case GUARD -> {
                hero.defending = true;
                hero.restoreMp(10);
                log(hero.name + " raises a guard. (+10 MP)", "#38bdf8");
                Color cyan = Color.web("#38bdf8");
                if (hb != null) {
                    SkillFx.shield(arenaPane, bx(hb), by(hb), 90, cyan);
                    SkillFx.statusWord(arenaPane, bx(hb), by(hb), "GUARD", cyan);
                }
                refreshUI(); endTurn();
            }
            default -> endTurn();
        }
    }

    private void doHeroUltimate(Hero hero) {
        if (!awaitingInput || !hero.hasUlt() || paused || showingIntro) return;
        awaitingInput = false;
        pauseBtn.setDisable(true);
        hero.spendUlt();
        playUltimateCinematic(hero);
    }

    private String incantationFor(Hero hero) {
        return switch (hero.name) {
            case "Knight" ->
                "By the oath I swore beneath a dying sun — let every Echo in this hall bear witness to the vow I keep.";
            case "Mage" ->
                "Embers of the forgotten sky, hear your daughter's cry — fall, and make the wicked kneel.";
            default ->
                "Let the silence break, and let my will be done.";
        };
    }

    private void playUltimateCinematic(Hero hero) {
        String heroColor = hero.colorHex();
        Color fxColor = Color.web(heroColor);
        double w = getWidth()  > 0 ? getWidth()  : 1280;
        double h = getHeight() > 0 ? getHeight() : 900;
        double cx = w / 2;
        double cy = h / 2;

        Rectangle topBar = new Rectangle();
        topBar.widthProperty().bind(widthProperty());
        topBar.heightProperty().bind(heightProperty().multiply(0.13));
        topBar.setFill(Color.web("#05070f", 0.95));
        topBar.setTranslateY(-220);
        topBar.setMouseTransparent(true);

        Rectangle botBar = new Rectangle();
        botBar.widthProperty().bind(widthProperty());
        botBar.heightProperty().bind(heightProperty().multiply(0.13));
        botBar.yProperty().bind(heightProperty().subtract(botBar.heightProperty()));
        botBar.setFill(Color.web("#05070f", 0.95));
        botBar.setTranslateY(220);
        botBar.setMouseTransparent(true);

        Pane fxLayer = new Pane();
        fxLayer.prefWidthProperty().bind(widthProperty());
        fxLayer.prefHeightProperty().bind(heightProperty());
        fxLayer.setMaxWidth(Double.MAX_VALUE);
        fxLayer.setMaxHeight(Double.MAX_VALUE);
        fxLayer.setMouseTransparent(true);

        Rectangle wash = new Rectangle();
        wash.widthProperty().bind(widthProperty());
        wash.heightProperty().bind(heightProperty());
        wash.setFill(fxColor);
        wash.setOpacity(0);

        List<Line> speedLines = new ArrayList<>();
        List<double[]> speedData = new ArrayList<>();
        int lineCount = 34;
        for (int i = 0; i < lineCount; i++) {
            double angle = (Math.PI * 2 / lineCount) * i + (rng.nextDouble() - 0.5) * 0.10;
            double innerR = 130 + rng.nextDouble() * 70;
            double outerR = 950 + rng.nextDouble() * 380;

            Line l = new Line();
            l.setStartX(cx + Math.cos(angle) * innerR);
            l.setStartY(cy + Math.sin(angle) * innerR);
            l.setEndX(cx + Math.cos(angle) * innerR);
            l.setEndY(cy + Math.sin(angle) * innerR);
            l.setStroke(i % 5 == 0 ? Color.WHITE : fxColor);
            l.setStrokeWidth(1.2 + rng.nextDouble() * 1.8);
            l.setOpacity(0);
            l.setEffect(new DropShadow(10, fxColor));
            speedLines.add(l);
            speedData.add(new double[]{ angle, outerR });
        }

        Circle pulse = new Circle(cx, cy, 24);
        pulse.setFill(Color.WHITE);
        pulse.setOpacity(0);
        pulse.setEffect(new DropShadow(60, fxColor));

        Circle glow = new Circle(cx, cy, 280);
        glow.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(heroColor, 0.85)),
                new Stop(0.5, Color.web(heroColor, 0.22)),
                new Stop(1, Color.TRANSPARENT)));
        glow.setOpacity(0);

        List<Circle> rings = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Circle ring = new Circle(cx, cy, 40);
            ring.setFill(Color.TRANSPARENT);
            ring.setStroke(Color.web(heroColor, 0.75));
            ring.setStrokeWidth(2.5);
            ring.setOpacity(0);
            ring.setEffect(new DropShadow(16, Color.web(heroColor, 0.7)));
            rings.add(ring);
        }

        List<Circle> particles = new ArrayList<>();
        List<double[]> particleDirs = new ArrayList<>();
        int N = 22;
        for (int i = 0; i < N; i++) {
            Circle p = new Circle(3 + rng.nextDouble() * 3);
            p.setFill(i % 3 == 0 ? Color.WHITE : fxColor);
            p.setCenterX(cx);
            p.setCenterY(cy);
            p.setOpacity(0);
            p.setEffect(new DropShadow(10, fxColor));
            particles.add(p);
            double angle = (Math.PI * 2 / N) * i + (rng.nextDouble() - 0.5) * 0.20;
            double dist  = 220 + rng.nextDouble() * 200;
            particleDirs.add(new double[]{ Math.cos(angle) * dist, Math.sin(angle) * dist });
        }

        fxLayer.getChildren().add(wash);
        fxLayer.getChildren().addAll(speedLines);
        fxLayer.getChildren().add(pulse);
        fxLayer.getChildren().add(glow);
        fxLayer.getChildren().addAll(rings);
        fxLayer.getChildren().addAll(particles);

        String incantation = incantationFor(hero);

        Label chantHeader = new Label("✦  I N C A N T A T I O N  ✦");
        chantHeader.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
        chantHeader.setStyle("-fx-text-fill: " + heroColor + ";");

        TextFlow chantFlow = new TextFlow();
        chantFlow.setMaxWidth(w * 0.62);
        chantFlow.setTextAlignment(TextAlignment.CENTER);
        chantFlow.setLineSpacing(6);

        List<Text> chantWords = new ArrayList<>();
        String[] words = incantation.split(" ");
        for (int i = 0; i < words.length; i++) {
            Text word = new Text(words[i] + (i < words.length - 1 ? " " : ""));
            word.setFont(Font.font("Georgia", FontPosture.ITALIC, 26));
            word.setFill(Color.web("#f0e6d2"));
            word.setEffect(new DropShadow(16, fxColor));
            word.setOpacity(0);
            chantWords.add(word);
            chantFlow.getChildren().add(word);
        }

        Rectangle chantRuleTop = new Rectangle();
        chantRuleTop.setWidth(60);
        chantRuleTop.setHeight(1.5);
        chantRuleTop.setFill(Color.web(heroColor, 0.8));
        chantRuleTop.setEffect(new DropShadow(10, fxColor));

        Rectangle chantRuleBot = new Rectangle();
        chantRuleBot.setWidth(60);
        chantRuleBot.setHeight(1.5);
        chantRuleBot.setFill(Color.web(heroColor, 0.8));
        chantRuleBot.setEffect(new DropShadow(10, fxColor));

        VBox chantStack = new VBox(14, chantRuleTop, chantHeader, chantFlow, chantRuleBot);
        chantStack.setAlignment(Pos.CENTER);
        chantStack.setMaxWidth(w * 0.72);
        chantStack.setOpacity(0);
        chantStack.setMouseTransparent(true);
        chantStack.setTranslateY(-10);

        Label ultName = new Label(hero.ultimate.name.toUpperCase());
        ultName.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 68px; -fx-font-weight: bold; " +
                "-fx-font-family: 'Georgia';");
        ultName.setEffect(new DropShadow(36, fxColor));
        ultName.setOpacity(0);
        ultName.setScaleX(1.9); ultName.setScaleY(1.9);
        ultName.setMouseTransparent(true);

        Label ultOwner = new Label(hero.name.toUpperCase() + "  ·  " + hero.role.toUpperCase() + "  ·  ULTIMATE");
        ultOwner.setStyle("-fx-text-fill: " + heroColor + "; -fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-font-family: 'Consolas';");
        ultOwner.setOpacity(0);
        ultOwner.setMouseTransparent(true);
        ultOwner.setTranslateY(58);

        VBox textStack = new VBox(6, ultName, ultOwner);
        textStack.setAlignment(Pos.CENTER);
        textStack.setMouseTransparent(true);

        getChildren().addAll(topBar, botBar, fxLayer, chantStack, textStack);

        long T_SILENCE_END  = 700;

        TranslateTransition topIn = new TranslateTransition(Duration.millis(420), topBar);
        topIn.setToY(0);
        topIn.setInterpolator(Interpolator.EASE_OUT);
        TranslateTransition botIn = new TranslateTransition(Duration.millis(420), botBar);
        botIn.setToY(0);
        botIn.setInterpolator(Interpolator.EASE_OUT);
        topIn.play(); botIn.play();

        FadeTransition washIn = new FadeTransition(Duration.millis(500), wash);
        washIn.setFromValue(0); washIn.setToValue(0.26);
        FadeTransition washOut = new FadeTransition(Duration.millis(900), wash);
        washOut.setFromValue(0.26); washOut.setToValue(0);
        washOut.setDelay(Duration.millis(5000));
        new ParallelTransition(washIn, washOut).play();

        long T_CARD_IN_START  = T_SILENCE_END;
        long T_CARD_IN_LEN    = 340;
        FadeTransition chantIn = new FadeTransition(Duration.millis(T_CARD_IN_LEN), chantStack);
        chantIn.setFromValue(0); chantIn.setToValue(1);
        chantIn.setDelay(Duration.millis(T_CARD_IN_START));
        chantIn.play();

        TranslateTransition chantSlide = new TranslateTransition(Duration.millis(560), chantStack);
        chantSlide.setFromY(-10);
        chantSlide.setToY(0);
        chantSlide.setDelay(Duration.millis(T_CARD_IN_START));
        chantSlide.setInterpolator(Interpolator.EASE_OUT);
        chantSlide.play();

        long T_WORD_START = T_CARD_IN_START + T_CARD_IN_LEN + 120;
        long WORD_STEP_FIRST = 260L;
        long WORD_STEP_LAST  = 85L;

        int totalWords = chantWords.size();
        long cumulative = T_WORD_START;

        for (int i = 0; i < totalWords; i++) {
            Text word = chantWords.get(i);
            boolean isLast = (i == totalWords - 1);

            Duration fadeDur = Duration.millis(isLast ? 460 : 260);
            FadeTransition wf = new FadeTransition(fadeDur, word);
            wf.setToValue(1);
            wf.setDelay(Duration.millis(cumulative));
            wf.play();

            if (isLast) {
                ScaleTransition ws = new ScaleTransition(Duration.millis(520), word);
                ws.setFromX(1.0); ws.setFromY(1.0);
                ws.setToX(1.14);  ws.setToY(1.14);
                ws.setAutoReverse(true);
                ws.setCycleCount(2);
                ws.setDelay(Duration.millis(cumulative + 260));
                ws.setInterpolator(Interpolator.EASE_BOTH);
                ws.play();

                DropShadow peakGlow = new DropShadow(30, fxColor);
                Timeline glowUp = new Timeline(
                    new KeyFrame(Duration.ZERO,
                        new KeyValue(word.effectProperty(), word.getEffect())),
                    new KeyFrame(Duration.millis(cumulative + 500),
                        new KeyValue(word.effectProperty(), peakGlow, Interpolator.EASE_BOTH))
                );
                glowUp.play();
            }

            double t = (totalWords <= 1) ? 0.0 : (double) i / (totalWords - 1);
            long step = (long) (WORD_STEP_FIRST + (WORD_STEP_LAST - WORD_STEP_FIRST) * t);
            cumulative += step;
        }

        long T_LAST_WORD = cumulative;

        long T_HOLD_MS       = 700;
        long T_CHANT_OUT     = T_LAST_WORD + T_HOLD_MS;
        long T_LINES_BURST   = T_CHANT_OUT + 100;
        long T_NAME_IN       = T_LINES_BURST + 260;
        long T_PULSE_IN      = T_NAME_IN + 320;
        long T_RINGS_IN      = T_PULSE_IN;
        long T_PARTICLES_IN  = T_RINGS_IN + 140;

        FadeTransition chantOut = new FadeTransition(Duration.millis(420), chantStack);
        chantOut.setFromValue(1); chantOut.setToValue(0);
        chantOut.setDelay(Duration.millis(T_CHANT_OUT));
        chantOut.play();

        for (int i = 0; i < speedLines.size(); i++) {
            Line l = speedLines.get(i);
            double[] data = speedData.get(i);
            double angle  = data[0];
            double outerR = data[1];
            long delay = T_LINES_BURST + rng.nextInt(180);

            Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO,
                    new KeyValue(l.opacityProperty(), 0.0)),
                new KeyFrame(Duration.millis(delay + 90),
                    new KeyValue(l.opacityProperty(), 0.85, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.millis(delay + 520),
                    new KeyValue(l.endXProperty(), cx + Math.cos(angle) * outerR, Interpolator.EASE_OUT),
                    new KeyValue(l.endYProperty(), cy + Math.sin(angle) * outerR, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.millis(delay + 900),
                    new KeyValue(l.opacityProperty(), 0.0))
            );
            tl.play();
        }

        FadeTransition glowIn = new FadeTransition(Duration.millis(400), glow);
        glowIn.setFromValue(0); glowIn.setToValue(0.85);
        glowIn.setDelay(Duration.millis(T_LINES_BURST + 60));
        glowIn.play();
        ScaleTransition glowScale = new ScaleTransition(Duration.millis(1600), glow);
        glowScale.setFromX(0.55); glowScale.setFromY(0.55);
        glowScale.setToX(1.25);   glowScale.setToY(1.25);
        glowScale.setDelay(Duration.millis(T_LINES_BURST + 60));
        glowScale.play();

        FadeTransition pulseIn = new FadeTransition(Duration.millis(260), pulse);
        pulseIn.setFromValue(0); pulseIn.setToValue(0.28);
        pulseIn.setDelay(Duration.millis(T_PULSE_IN));
        FadeTransition pulseOut = new FadeTransition(Duration.millis(500), pulse);
        pulseOut.setFromValue(0.28); pulseOut.setToValue(0);
        pulseOut.setDelay(Duration.millis(T_PULSE_IN + 260));
        ScaleTransition pulseScale = new ScaleTransition(Duration.millis(900), pulse);
        pulseScale.setFromX(0.6); pulseScale.setFromY(0.6);
        pulseScale.setToX(7);     pulseScale.setToY(7);
        pulseScale.setDelay(Duration.millis(T_PULSE_IN));
        pulseIn.play(); pulseOut.play(); pulseScale.play();

        for (int i = 0; i < rings.size(); i++) {
            Circle ring = rings.get(i);
            long delay = T_RINGS_IN + i * 220L;
            FadeTransition rIn = new FadeTransition(Duration.millis(200), ring);
            rIn.setFromValue(0); rIn.setToValue(0.8);
            rIn.setDelay(Duration.millis(delay));
            FadeTransition rOut = new FadeTransition(Duration.millis(800), ring);
            rOut.setFromValue(0.8); rOut.setToValue(0);
            rOut.setDelay(Duration.millis(delay + 300));
            ScaleTransition rScale = new ScaleTransition(Duration.millis(1100), ring);
            rScale.setFromX(0.4); rScale.setFromY(0.4);
            rScale.setToX(10);    rScale.setToY(10);
            rScale.setDelay(Duration.millis(delay));
            rIn.play(); rOut.play(); rScale.play();
        }

        for (int i = 0; i < particles.size(); i++) {
            Circle p = particles.get(i);
            double[] dir = particleDirs.get(i);
            FadeTransition pIn = new FadeTransition(Duration.millis(180), p);
            pIn.setFromValue(0); pIn.setToValue(1);
            pIn.setDelay(Duration.millis(T_PARTICLES_IN));
            FadeTransition pOut = new FadeTransition(Duration.millis(700), p);
            pOut.setFromValue(1); pOut.setToValue(0);
            pOut.setDelay(Duration.millis(T_PARTICLES_IN + 250));
            TranslateTransition pMove = new TranslateTransition(Duration.millis(1000), p);
            pMove.setByX(dir[0]);
            pMove.setByY(dir[1]);
            pMove.setDelay(Duration.millis(T_PARTICLES_IN));
            pMove.setInterpolator(Interpolator.EASE_OUT);
            pIn.play(); pOut.play(); pMove.play();
        }

        FadeTransition nameIn = new FadeTransition(Duration.millis(420), ultName);
        nameIn.setFromValue(0); nameIn.setToValue(1);
        nameIn.setDelay(Duration.millis(T_NAME_IN));
        ScaleTransition nameScaleIn = new ScaleTransition(Duration.millis(620), ultName);
        nameScaleIn.setFromX(1.9); nameScaleIn.setFromY(1.9);
        nameScaleIn.setToX(1.0);   nameScaleIn.setToY(1.0);
        nameScaleIn.setDelay(Duration.millis(T_NAME_IN));
        nameScaleIn.setInterpolator(Interpolator.EASE_OUT);
        nameIn.play(); nameScaleIn.play();

        FadeTransition ownerIn = new FadeTransition(Duration.millis(420), ultOwner);
        ownerIn.setFromValue(0); ownerIn.setToValue(1);
        ownerIn.setDelay(Duration.millis(T_NAME_IN + 220));
        ownerIn.play();

        long T_EXIT = T_NAME_IN + 1500;
        long T_BARS_OUT = T_EXIT + 220;
        long T_CLEANUP = T_BARS_OUT + 700;

        FadeTransition nameOut = new FadeTransition(Duration.millis(500), ultName);
        nameOut.setFromValue(1); nameOut.setToValue(0);
        nameOut.setDelay(Duration.millis(T_EXIT));
        ScaleTransition nameScaleOut = new ScaleTransition(Duration.millis(500), ultName);
        nameScaleOut.setFromX(1.0); nameScaleOut.setFromY(1.0);
        nameScaleOut.setToX(1.35);  nameScaleOut.setToY(1.35);
        nameScaleOut.setDelay(Duration.millis(T_EXIT));
        nameScaleOut.setInterpolator(Interpolator.EASE_IN);
        FadeTransition ownerOut = new FadeTransition(Duration.millis(500), ultOwner);
        ownerOut.setFromValue(1); ownerOut.setToValue(0);
        ownerOut.setDelay(Duration.millis(T_EXIT));
        nameOut.play(); nameScaleOut.play(); ownerOut.play();

        TranslateTransition topOut = new TranslateTransition(Duration.millis(500), topBar);
        topOut.setToY(-220);
        topOut.setDelay(Duration.millis(T_BARS_OUT));
        topOut.setInterpolator(Interpolator.EASE_IN);
        TranslateTransition botOut = new TranslateTransition(Duration.millis(500), botBar);
        botOut.setToY(220);
        botOut.setDelay(Duration.millis(T_BARS_OUT));
        botOut.setInterpolator(Interpolator.EASE_IN);
        topOut.play(); botOut.play();

        PauseTransition cleanup = new PauseTransition(Duration.millis(T_CLEANUP));
        cleanup.setOnFinished(e -> {
            getChildren().remove(topBar);
            getChildren().remove(botBar);
            getChildren().remove(fxLayer);
            getChildren().remove(chantStack);
            getChildren().remove(textStack);
            applyUltimateDamage(hero);
        });
        cleanup.play();
    }

    private void applyUltimateDamage(Hero hero) {
        Battler hb = battlerFor(hero);
        Color gold = Color.web("#facc15");
        Runnable impact = () -> {
            log("★ " + hero.name + " unleashes " + hero.ultimate.name + "! ★", "#facc15");

            if (hero.ultimate.type == Skill.Type.ULTIMATE) {
                for (Enemy e : enemies) {
                    if (!e.isAlive()) continue;
                    int dmg = rollDamage(hero, e, hero.ultimate.multiplier, hero.ultimate.pierce, true);
                    boolean wasCrit = lastWasCrit;
                    e.takeDamage(dmg);
                    log("   → " + e.name + " takes " + dmg + " damage.", "#e2e8f0");
                    Battler b = battlerFor(e);
                    if (b != null) {
                        SkillFx.damageNumber(arenaPane, bx(b), by(b), dmg, gold, wasCrit);
                        b.playHurt();
                        SkillFx.impact(arenaPane, bx(b), by(b), gold);
                    }
                    if (hero.ultimate.poisonTurns > 0 && e.isAlive()) {
                        e.poison = hero.ultimate.poisonTurns;
                        log("   → " + e.name + " is poisoned!", "#a3e635");
                    }
                }
                if (hero.ultimate.healAmount > 0) {
                    hero.heal(hero.ultimate.healAmount);
                    log("   → " + hero.name + " recovers " + hero.ultimate.healAmount + " HP.", "#4ade80");
                    if (hb != null) {
                        SkillFx.healNumber(arenaPane, bx(hb), by(hb),
                                "+" + hero.ultimate.healAmount, Color.web("#4ade80"));
                        SkillFx.heal(arenaPane, bx(hb), by(hb), Color.web("#4ade80"));
                    }
                }
            }

            refreshUI();
            for (Enemy e : enemies) handleDeath(e);
            endTurn();
        };

        if (hb != null) hb.playAttack(impact);
        else impact.run();
    }

    private void impactEnemy(Hero hero, Enemy target, Skill s) {
        int dmg = rollDamage(hero, target, s.multiplier, s.pierce, true);
        boolean wasCrit = lastWasCrit;
        target.takeDamage(dmg);
        log("   → " + target.name + " takes " + dmg + " damage.", "#e2e8f0");
        Battler tb = battlerFor(target);
        if (tb != null) {
            SkillFx.damageNumber(arenaPane, bx(tb), by(tb), dmg, Color.web("#f87171"), wasCrit);
            tb.playHurt();
        }
        if (s.poisonTurns > 0 && target.isAlive()) {
            target.poison = s.poisonTurns;
            log("   → " + target.name + " is poisoned!", "#a3e635");
        }
        hero.grantUltCharge(15);
        refreshUI();
        handleDeath(target);
        endTurn();
    }

    private void impactAllEnemies(Hero hero, Skill s) {
        List<Enemy> hit = new ArrayList<>();
        for (Enemy e : enemies) if (e.isAlive()) hit.add(e);
        if (hit.isEmpty()) { endTurn(); return; }
        for (Enemy e : hit) {
            int dmg = rollDamage(hero, e, s.multiplier, s.pierce, true);
            boolean wasCrit = lastWasCrit;
            e.takeDamage(dmg);
            log("   → " + e.name + " takes " + dmg + " damage.", "#e2e8f0");
            Battler b = battlerFor(e);
            if (b != null) {
                SkillFx.damageNumber(arenaPane, bx(b), by(b), dmg, Color.web("#f87171"), wasCrit);
                b.playHurt();
            }
        }
        hero.grantUltCharge(15);
        refreshUI();
        for (Enemy e : hit) handleDeath(e);
        endTurn();
    }

    private void doHeroPotion(Hero hero) {
        if (!awaitingInput || hero.potions <= 0) return;
        awaitingInput = false; pauseBtn.setDisable(true);
        hero.potions--;
        int heal = 55;
        hero.heal(heal);
        log(hero.name + " drinks a potion and recovers " + heal + " HP.", "#4ade80");
        Battler b = battlerFor(hero);
        if (b != null) {
            SkillFx.heal(arenaPane, bx(b), by(b), Color.web("#4ade80"));
            SkillFx.healNumber(arenaPane, bx(b), by(b), "+" + heal, Color.web("#4ade80"));
        }
        menuState = MenuState.ROOT;
        refreshUI(); endTurn();
    }

    private void doHeroEther(Hero hero) {
        if (!awaitingInput || hero.ethers <= 0) return;
        awaitingInput = false; pauseBtn.setDisable(true);
        hero.ethers--;
        int restore = 40;
        hero.restoreMp(restore);
        log(hero.name + " drinks an ether and restores " + restore + " MP.", "#38bdf8");
        Battler b = battlerFor(hero);
        if (b != null) {
            SkillFx.heal(arenaPane, bx(b), by(b), Color.web("#38bdf8"));
            SkillFx.healNumber(arenaPane, bx(b), by(b), "+" + restore + " MP", Color.web("#38bdf8"));
        }
        menuState = MenuState.ROOT;
        refreshUI(); endTurn();
    }

    private void enemyAction(Enemy en) {
        if (gameOver || paused || showingIntro || !en.isAlive()) { turnIndex++; beginTurn(); return; }

        if (en.healCd > 0) en.healCd--;

        if (en.healer && en.hp < en.maxHp * 0.45 && en.healCd <= 0) {
            int amount = 26;
            en.heal(amount);
            en.healCd = 3;
            log(en.name + " chants a DARK MEND and recovers " + amount + " HP.", "#f472b6");
            Battler b = battlerFor(en);
            if (b != null) {
                SkillFx.healNumber(arenaPane, bx(b), by(b), "+" + amount, Color.web("#4ade80"));
                SkillFx.heal(arenaPane, bx(b), by(b), Color.web("#f472b6"));
            }
            refreshUI();
            endEnemyTurn();
            return;
        }

        List<Hero> living = new ArrayList<>();
        for (Hero h : heroes) if (h.isAlive()) living.add(h);
        if (living.isEmpty()) { endEnemyTurn(); return; }
        Hero victim = living.get(rng.nextInt(living.size()));

        boolean useSpecial = en.special != null
                && en.cooldownOf(en.special) == 0
                && rng.nextDouble() < en.specialChance;

        if (settings.shouldTriggerPuzzle(rng)) {
            Puzzle p = PuzzleBank.random(settings.difficulty, rng);
            puzzleModal.show(p, settings.puzzleTimer, settings.puzzleTimeSeconds,
                    correct -> resolveEnemyAttack(en, victim, useSpecial, correct == 1));
        } else {
            resolveEnemyAttack(en, victim, useSpecial, false);
        }
    }

    private void resolveEnemyAttack(Enemy en, Hero victim, boolean useSpecial, boolean blocked) {
        if (useSpecial) en.cooldowns.put(en.special, en.special.cooldown + 1);
        Battler eb = battlerFor(en);
        Battler vb = battlerFor(victim);
        Color enemyColor = Color.web(en.colorHex());

        if (eb != null && vb != null) {
            SkillFx.castRunes(arenaPane, bx(eb), by(eb), enemyColor);
            SkillFx.slash(arenaPane, bx(eb), by(eb), bx(vb), by(vb), enemyColor);
            if (useSpecial) {
                SkillFx.wave(arenaPane, bx(eb), by(eb), arenaPane.getWidth(), enemyColor);
            }
        }

        Runnable impact = () -> {
            if (useSpecial) applyEnemySpecial(en, victim, blocked);
            else            applyEnemyBasic(en, victim, blocked);

            if (blocked) {
                if (vb != null) {
                    SkillFx.statusWord(arenaPane, bx(vb), by(vb), "BLOCK!", Color.web("#4ade80"));
                    SkillFx.shield(arenaPane, bx(vb), by(vb), 85, Color.web("#4ade80"));
                }
                log("   → The attack is BLOCKED by your coding!", "#4ade80");
                for (Hero h : heroes) if (h.isAlive()) h.grantXp(5);
            } else if (vb != null) {
                SkillFx.impact(arenaPane, bx(vb), by(vb), enemyColor);
            }

            refreshUI();
            for (Hero h : heroes) if (!h.isAlive()) log(h.name + " has fallen!", "#ef4444");
            endEnemyTurn();
        };
        if (eb != null) eb.playAttack(impact);
        else impact.run();
    }

    private void applyEnemyBasic(Enemy en, Hero victim, boolean blocked) {
        if (blocked) return;
        int dmg = rollDamage(en, victim, 1.0, false, false);
        boolean wasCrit = lastWasCrit;
        victim.takeDamage(dmg);
        if (victim instanceof Hero h) h.grantUltCharge(8);
        log(en.name + " attacks " + victim.name + " for " + dmg + " damage.", "#fca5a5");
        Battler vb = battlerFor(victim);
        if (vb != null) {
            SkillFx.damageNumber(arenaPane, bx(vb), by(vb), dmg, Color.web("#f87171"), wasCrit);
            vb.playHurt();
        }
    }

    private void applyEnemySpecial(Enemy en, Hero victim, boolean blocked) {
        Skill s = en.special;
        log(en.name + " uses " + s.name + "!", "#ef4444");
        if (blocked) return;
        switch (s.type) {
            case ATTACK_ONE -> {
                int dmg = rollDamage(en, victim, s.multiplier, s.pierce, false);
                boolean wasCrit = lastWasCrit;
                victim.takeDamage(dmg);
                if (victim instanceof Hero h) h.grantUltCharge(8);
                log("   → " + victim.name + " takes " + dmg + " damage.", "#e2e8f0");
                Battler vb = battlerFor(victim);
                if (vb != null) {
                    SkillFx.damageNumber(arenaPane, bx(vb), by(vb), dmg, Color.web("#f87171"), wasCrit);
                    vb.playHurt();
                }
                if (s.poisonTurns > 0 && victim.isAlive()) {
                    victim.poison = s.poisonTurns;
                    log("   → " + victim.name + " is poisoned!", "#a3e635");
                }
            }
            case ATTACK_ALL -> {
                for (Hero h : heroes) {
                    if (!h.isAlive()) continue;
                    int dmg = rollDamage(en, h, s.multiplier, s.pierce, false);
                    boolean wasCrit = lastWasCrit;
                    h.takeDamage(dmg);
                    h.grantUltCharge(8);
                    log("   → " + h.name + " takes " + dmg + " damage.", "#e2e8f0");
                    Battler vb = battlerFor(h);
                    if (vb != null) {
                        SkillFx.damageNumber(arenaPane, bx(vb), by(vb), dmg, Color.web("#f87171"), wasCrit);
                        vb.playHurt();
                        SkillFx.impact(arenaPane, bx(vb), by(vb), Color.web(en.colorHex()));
                    }
                    if (s.poisonTurns > 0 && h.isAlive()) h.poison = s.poisonTurns;
                }
            }
            default -> { }
        }
    }

    private void endEnemyTurn() {
        refreshUI();
        if (checkBattleEnd()) return;
        turnIndex++;
        PauseTransition p = new PauseTransition(Duration.millis(600));
        p.setOnFinished(e -> beginTurn());
        p.play();
    }

    private void togglePause() {
        if (!awaitingInput || gameOver || showingIntro) return;
        paused = true;
        pauseMenu.setVisible(true);
    }

    private void resumeFromPause() {
        paused = false;
        pauseMenu.setVisible(false);
        Platform.runLater(this::requestFocus);
    }

    private void handleKey(javafx.scene.input.KeyEvent e) {
        if (e.getCode() == KeyCode.ESCAPE && awaitingInput && !gameOver) {
            togglePause();
            return;
        }
        if (!awaitingInput || paused || gameOver || showingIntro) return;
        if (!(current instanceof Hero hero)) return;

        if ((e.getCode() == KeyCode.Q || e.getCode() == KeyCode.DIGIT0) && hero.hasUlt()) {
            doHeroUltimate(hero);
            return;
        }

        int idx = -1;
        switch (e.getCode()) {
            case DIGIT1 -> idx = 0;
            case DIGIT2 -> idx = 1;
            case DIGIT3 -> idx = 2;
            case DIGIT4 -> idx = 3;
            case DIGIT5 -> idx = 4;
            case DIGIT6 -> idx = 5;
        }
        if (idx >= 0 && idx < hero.skills.size()) {
            Skill s = hero.skills.get(idx);
            if (hero.mp >= s.mpCost && hero.cooldownOf(s) == 0) doHeroSkill(hero, s);
        }
    }

    private void selectEnemy(Combatant c) {
        if (c instanceof Enemy e && e.isAlive()) {
            selectedEnemy = e;
            updateTargetHighlight();
        }
    }

    private Enemy currentEnemyTarget() {
        if (selectedEnemy != null && selectedEnemy.isAlive()) return selectedEnemy;
        for (Enemy e : enemies) if (e.isAlive()) return e;
        return null;
    }

    private void handleDeath(Enemy e) {
        if (e.isAlive()) return;
        log(e.name + " is defeated!", "#4ade80");
        int xp = (int) (e.xpReward * settings.xpScale());

        Battler b = battlerFor(e);
        if (b != null) {
            SkillFx.impact(arenaPane, bx(b), by(b), Color.web(e.colorHex()));
            b.playDeath();
        }

        for (Hero h : heroes) {
            if (!h.isAlive()) continue;
            int levels = h.grantXp(xp);
            h.grantUltCharge(20);
            log("   → " + h.name + " gains " + xp + " XP.", "#a78bfa");
            if (levels > 0) {
                log("   → " + h.name + " LEVELS UP! Now level " + h.level + ".", "#facc15");
                Battler hb = battlerFor(h);
                if (hb != null) {
                    SkillFx.statusWord(arenaPane, bx(hb), by(hb), "LEVEL UP!", Color.web("#facc15"));
                    SkillFx.heal(arenaPane, bx(hb), by(hb), Color.web("#facc15"));
                }
            }
        }

        if (selectedEnemy == e) {
            selectedEnemy = null;
            for (Enemy other : enemies) if (other.isAlive()) { selectedEnemy = other; break; }
        }
        refreshUI();
    }

    private int rollDamage(Combatant attacker, Combatant target, double mult, boolean pierce, boolean isHeroAttack) {
        lastWasCrit = false;
        if (mult == 0) return 0;
        int offense = isHeroAttack ? Math.max(attacker.atk, attacker.mag) : attacker.atk;
        double base = offense * mult;
        double mitigation = pierce ? target.def * 0.15 : target.def * 0.55;
        double raw = base - mitigation;
        raw *= 0.88 + rng.nextDouble() * 0.24;

        boolean crit = rng.nextDouble() < 0.12;
        if (crit) { raw *= 1.75; lastWasCrit = true; }
        if (target.defending) raw *= 0.45;

        return (int) Math.max(1, Math.round(raw));
    }

    private void victory() {
        gameOver = true;
        awaitingInput = false;
        pauseBtn.setDisable(true);
        actionGrid.getChildren().clear();

        log("", "#000000");
        log(">>> VICTORY! The Ashen King falls. <<<", "#4ade80");

        StoryCodex.unlock("victory");
        StoryCodex.unlock("prologue");

        List<StoryLine> epilogue = StoryScript.victoryEpilogue(heroes.size());
        game.showStory(epilogue, () -> showOverlay(
                "VICTORY",
                "The Arena of Echoes closes behind you. The story ends here.",
                "#4ade80"));
    }

    private void defeat() {
        gameOver = true;
        awaitingInput = false;
        pauseBtn.setDisable(true);
        actionGrid.getChildren().clear();

        log("", "#000000");
        log(">>> Your party has fallen... <<<", "#ef4444");

        StoryCodex.unlock("defeat");

        List<StoryLine> epilogue = StoryScript.defeatEpilogue(heroes.size());
        game.showStory(epilogue, () -> showOverlay(
                "DEFEAT",
                "Your armor hangs on the wall of the Arena, waiting for the next knight.",
                "#ef4444"));
    }

    private void showOverlay(String title, String subtitle, String colorHex) {
        overlayLayer.getChildren().clear();
        Region dim = new Region();
        dim.setStyle("-fx-background-color: rgba(5,2,5,0.9);");
        Label t = new Label(title);
        t.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 56px; -fx-font-weight: bold; -fx-font-family: 'Georgia';");
        Label s = new Label(subtitle);
        s.setStyle("-fx-text-fill: #cbb0b0; -fx-font-size: 15px;");

        Button again = new Button("PLAY AGAIN");
        again.setPrefSize(240, 52);
        again.setStyle(overlayButtonStyle("#a78bfa", false));
        again.setOnMouseEntered(e -> again.setStyle(overlayButtonStyle("#a78bfa", true)));
        again.setOnMouseExited(e  -> again.setStyle(overlayButtonStyle("#a78bfa", false)));
        again.setOnAction(e -> game.showMenu());

        Button menu = new Button("MAIN MENU");
        menu.setPrefSize(240, 52);
        menu.setStyle(overlayButtonStyle("#38bdf8", false));
        menu.setOnMouseEntered(e -> menu.setStyle(overlayButtonStyle("#38bdf8", true)));
        menu.setOnMouseExited(e  -> menu.setStyle(overlayButtonStyle("#38bdf8", false)));
        menu.setOnAction(e -> game.showMenu());

        HBox buttons = new HBox(14, again, menu);
        buttons.setAlignment(Pos.CENTER);
        VBox box = new VBox(18, t, s, buttons);
        box.setAlignment(Pos.CENTER);
        overlayLayer.getChildren().addAll(dim, box);
        overlayLayer.setVisible(true);
    }

    private String overlayButtonStyle(String c, boolean hover) {
        return "-fx-background-color: " + (hover ? c : "rgba(255,255,255,0.02)") + ";" +
               "-fx-border-color: " + c + ";" +
               "-fx-border-width: 2;" +
               "-fx-border-radius: 10;" +
               "-fx-background-radius: 10;" +
               "-fx-text-fill: " + (hover ? "#0a0c13" : c) + ";" +
               "-fx-font-size: 15px;" +
               "-fx-font-weight: bold;" +
               "-fx-cursor: hand;";
    }

    // ==================================================================
    //  REFRESH / PANELS
    // ==================================================================

    private void refreshUI() {
        for (Battler b : heroBattlers) {
            Hero h = (Hero) b.getCombatant();
            b.getHpBar().setRatio(h.hpRatio(), true);
            if (b.getMpBar() != null) b.getMpBar().setRatio(h.mpRatio(), true);
            b.setDead(!h.isAlive());
            b.getStatusLabel().setText(statusOf(h));
            b.refreshUlt();
            b.setUltReady(h.hasUlt());
        }
        for (Battler b : enemyBattlers) {
            Enemy e = (Enemy) b.getCombatant();
            b.getHpBar().setRatio(e.hpRatio(), true);
            b.setDead(!e.isAlive());
            b.getStatusLabel().setText(statusOf(e));
        }

        updateEnemyInfoPanel();
        for (HeroInfoStrip s : heroStrips) s.refresh();

        if (awaitingInput && current instanceof Hero h) buildActionBar(h);

        updateTargetHighlight();
        updateTurnOrderPreview();
    }

    private String statusOf(Combatant c) {
        if (!c.isAlive()) return "Down";
        if (c.poison > 0) return "Poisoned (" + c.poison + ")";
        if (c.defending)  return "Guarding";
        if (c instanceof Hero h && h.hasUlt()) return "★ ULT READY";
        if (c instanceof Enemy e && e.special != null && e.cooldownOf(e.special) > 0)
            return "Charging " + e.special.name;
        return "Ready";
    }

    private void updateTargetHighlight() {
        Enemy t = currentEnemyTarget();
        for (Battler b : enemyBattlers) b.setSelected(b.getCombatant() == t && awaitingInput);
        updateEnemyInfoPanel();
    }

    private void highlightCurrent() {
        for (Battler b : heroBattlers)  b.setActive(b.getCombatant() == current);
        for (Battler b : enemyBattlers) b.setActive(b.getCombatant() == current);
    }

    private void updateTurnOrderPreview() {
        if (turnOrderBar == null) return;
        turnOrderBar.getChildren().clear();

        Label title = new Label("TURN ORDER");
        title.setStyle("-fx-text-fill: #cbb0b0; -fx-font-size: 9px; -fx-font-weight: bold;");
        turnOrderBar.getChildren().add(title);

        List<Combatant> preview = new ArrayList<>();
        for (int i = turnIndex; i < turnQueue.size() && preview.size() < 6; i++) {
            Combatant c = turnQueue.get(i);
            if (c.isAlive()) preview.add(c);
        }

        for (int i = 0; i < preview.size(); i++) {
            Combatant c = preview.get(i);
            boolean isNow = i == 0;

            Sprite mini = new Sprite(Sprites.get(c.spriteKey()), Sprites.palette(), 2);
            mini.setMirror(!(c instanceof Hero));
            mini.setOpacity(isNow ? 1.0 : 0.55);

            StackPane holder = new StackPane(mini);
            holder.setPrefSize(30, 34);
            holder.setStyle(isNow
                    ? "-fx-border-color: #facc15; -fx-border-width: 2; -fx-border-radius: 5; -fx-padding: 2; " +
                      "-fx-background-color: rgba(250,204,21,0.12); -fx-background-radius: 5;"
                    : "-fx-border-color: #6b2020; -fx-border-width: 1; -fx-border-radius: 5; -fx-padding: 2;");

            turnOrderBar.getChildren().add(holder);
        }
    }

    // ==================================================================
    //  HERO INFO STRIPS + ENEMY INFO PANEL
    // ==================================================================

    private VBox buildEnemyInfoPanel() {
        enemyInfoName = new Label("—");
        enemyInfoName.setStyle("-fx-text-fill: #0a0a0a; -fx-font-size: 14px; " +
                "-fx-font-weight: bold; -fx-font-family: 'Georgia';");

        Label hpTag = new Label("HP");
        hpTag.setStyle("-fx-text-fill: #166534; -fx-font-size: 11px; -fx-font-weight: bold;");

        enemyHpBg = new Rectangle(170, 10);
        enemyHpBg.setFill(Color.web("#cbd5e1"));
        enemyHpBg.setArcWidth(6); enemyHpBg.setArcHeight(6);

        enemyHpFill = new Rectangle(170, 10);
        enemyHpFill.setFill(Color.web("#4ade80"));
        enemyHpFill.setArcWidth(6); enemyHpFill.setArcHeight(6);

        StackPane hpStack = new StackPane(enemyHpBg, enemyHpFill);
        hpStack.setAlignment(Pos.CENTER_LEFT);
        hpStack.setPrefWidth(170);
        hpStack.setMaxWidth(170);

        enemyInfoHpText = new Label("—");
        enemyInfoHpText.setStyle("-fx-text-fill: #1a1a1a; -fx-font-size: 10px; " +
                "-fx-font-family: 'Consolas';");

        HBox hpRow = new HBox(6, hpTag, hpStack, enemyInfoHpText);
        hpRow.setAlignment(Pos.CENTER_LEFT);

        VBox panel = new VBox(4, enemyInfoName, hpRow);
        panel.setPadding(new Insets(8, 14, 8, 14));
        panel.setStyle("-fx-background-color: rgba(232,232,232,0.95); " +
                "-fx-border-color: #1a1a1a; -fx-border-width: 2; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, #000, 12, 0.45, 0, 3);");
        panel.setVisible(false);
        return panel;
    }

    private void updateEnemyInfoPanel() {
        if (enemyInfoPanel == null) return;
        Enemy target = currentEnemyTarget();
        if (target == null) { enemyInfoPanel.setVisible(false); return; }
        enemyInfoPanel.setVisible(true);
        enemyInfoName.setText(target.name);
        enemyInfoHpText.setText(target.hp + " / " + target.maxHp);
        double ratio = Math.max(0, target.hpRatio());
        enemyHpFill.setWidth(enemyHpBg.getWidth() * ratio);
        enemyHpFill.setFill(ratio > 0.5 ? Color.web("#4ade80")
                : ratio > 0.2 ? Color.web("#facc15")
                : Color.web("#ef4444"));
    }

    private void buildHeroStrips() {
        heroInfoContainer.getChildren().clear();
        heroStrips.clear();
        for (int i = 0; i < heroes.size(); i++) {
            HeroInfoStrip s = buildHeroStrip(heroes.get(i), i + 1);
            heroStrips.add(s);
            heroInfoContainer.getChildren().add(s.root);
        }
    }

    private HeroInfoStrip buildHeroStrip(Hero h, int playerNumber) {
        HeroInfoStrip s = new HeroInfoStrip();
        s.hero = h;

        Label pBadge = new Label("P" + playerNumber);
        pBadge.setStyle("-fx-background-color: " + h.colorHex() + ";" +
                "-fx-text-fill: #0a0c13; -fx-font-size: 10px; -fx-font-weight: bold;" +
                "-fx-padding: 2 6; -fx-background-radius: 4;");

        Label name = new Label(h.name.toUpperCase() + "  Lv." + h.level);
        name.setStyle("-fx-text-fill: #0a0a0a; -fx-font-size: 12px; -fx-font-weight: bold; " +
                "-fx-font-family: 'Georgia';");

        s.ultText = new Label();
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        HBox titleRow = new HBox(6, pBadge, name, sp, s.ultText);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        s.hpText = new Label(); s.hpText.setStyle("-fx-text-fill: #1a1a1a; -fx-font-size: 10px; -fx-font-family: 'Consolas';");
        s.mpText = new Label(); s.mpText.setStyle("-fx-text-fill: #1a1a1a; -fx-font-size: 10px; -fx-font-family: 'Consolas';");

        s.hpBar = new StatBar(140, 8, Color.web("#22c55e"));
        s.mpBar = new StatBar(140, 6, Color.web("#3b82f6"));

        Label hpTag = new Label("HP"); hpTag.setStyle("-fx-text-fill: #166534; -fx-font-size: 10px; -fx-font-weight: bold;");
        Label mpTag = new Label("MP"); mpTag.setStyle("-fx-text-fill: #1e40af; -fx-font-size: 10px; -fx-font-weight: bold;");

        HBox hpRow = new HBox(6, hpTag, s.hpBar, s.hpText); hpRow.setAlignment(Pos.CENTER_LEFT);
        HBox mpRow = new HBox(6, mpTag, s.mpBar, s.mpText); mpRow.setAlignment(Pos.CENTER_LEFT);

        s.root = new VBox(3, titleRow, hpRow, mpRow);
        s.root.setPadding(new Insets(8, 12, 8, 12));
        s.root.setStyle("-fx-background-color: rgba(232,232,232,0.95); " +
                "-fx-border-color: #1a1a1a; -fx-border-width: 2; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, #000, 10, 0.4, 0, 3);");

        s.refresh();
        return s;
    }

    // ==================================================================
    //  HELPERS
    // ==================================================================

    private Battler battlerFor(Combatant c) {
        for (Battler b : heroBattlers)  if (b.getCombatant() == c) return b;
        for (Battler b : enemyBattlers) if (b.getCombatant() == c) return b;
        return null;
    }

    private void log(String message, String colorHex) {
        Label l = new Label(message);
        l.setWrapText(true);
        l.setStyle("-fx-text-fill: " + colorHex + ";" +
                   "-fx-font-size: 12px;" +
                   "-fx-font-family: 'Consolas', 'Menlo', monospace;");
        l.setOpacity(0);
        logBox.getChildren().add(l);

        FadeTransition ft = new FadeTransition(Duration.millis(180), l);
        ft.setToValue(1.0);
        ft.play();

        while (logBox.getChildren().size() > 180) logBox.getChildren().remove(0);
        Platform.runLater(() -> logScroll.setVvalue(1.0));
    }
}