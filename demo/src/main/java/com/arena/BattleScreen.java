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
    private HBox turnOrderBar;
    private VBox turnBanner;
    private Label turnBannerMain, turnBannerSub;
    private VBox logBox;
    private ScrollPane logScroll;
    private HBox actionBar;
    private HBox heroStatBar;
    private Label waveLabel, roundLabel, turnLabel;
    private Button pauseBtn;

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

    private Node buildArena() {
        arenaPane = new Pane();
        arenaPane.setStyle("-fx-background-color: linear-gradient(to bottom, #200505 0%, #3d0a1a 30%, #1a0510 65%, #0a0205 100%);");

        Circle moonGlow = new Circle();
        moonGlow.radiusProperty().bind(arenaPane.widthProperty().multiply(0.10));
        moonGlow.centerXProperty().bind(arenaPane.widthProperty().multiply(0.5));
        moonGlow.centerYProperty().bind(arenaPane.heightProperty().multiply(0.16));
        moonGlow.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#ff4040", 0.45)),
                new Stop(0.55, Color.web("#ff2020", 0.15)),
                new Stop(1, Color.TRANSPARENT)));
        moonGlow.setMouseTransparent(true);
        arenaPane.getChildren().add(moonGlow);

        Circle moon = new Circle();
        moon.radiusProperty().bind(arenaPane.widthProperty().multiply(0.058));
        moon.centerXProperty().bind(arenaPane.widthProperty().multiply(0.5));
        moon.centerYProperty().bind(arenaPane.heightProperty().multiply(0.16));
        moon.setFill(new RadialGradient(0, 0, 0.4, 0.4, 0.65, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#ff6060")),
                new Stop(0.55, Color.web("#c41010")),
                new Stop(1, Color.web("#5c0606"))));
        moon.setEffect(new DropShadow(70, Color.web("#ff2020", 0.95)));
        moon.setMouseTransparent(true);
        arenaPane.getChildren().add(moon);

        Polygon spires = new Polygon();
        spires.setFill(Color.web("#0a0205"));
        spires.setMouseTransparent(true);
        arenaPane.getChildren().add(spires);

        Runnable buildSpires = () -> {
            double W = arenaPane.getWidth();
            double H = arenaPane.getHeight();
            if (W <= 0 || H <= 0) return;
            spires.getPoints().clear();
            spires.getPoints().addAll(
                0.0,       H * 0.76,
                W * 0.05,  H * 0.55,
                W * 0.08,  H * 0.72,
                W * 0.12,  H * 0.60,
                W * 0.15,  H * 0.74,
                W * 0.22,  H * 0.50,
                W * 0.24,  H * 0.30,
                W * 0.26,  H * 0.48,
                W * 0.32,  H * 0.58,
                W * 0.36,  H * 0.68,
                W * 0.42,  H * 0.44,
                W * 0.45,  H * 0.28,
                W * 0.48,  H * 0.42,
                W * 0.50,  H * 0.38,
                W * 0.52,  H * 0.26,
                W * 0.55,  H * 0.40,
                W * 0.58,  H * 0.46,
                W * 0.64,  H * 0.60,
                W * 0.68,  H * 0.52,
                W * 0.72,  H * 0.30,
                W * 0.74,  H * 0.52,
                W * 0.78,  H * 0.44,
                W * 0.82,  H * 0.66,
                W * 0.88,  H * 0.48,
                W * 0.91,  H * 0.68,
                W * 0.95,  H * 0.56,
                W,         H * 0.76,
                W,         H,
                0.0,       H
            );
        };
        arenaPane.widthProperty().addListener((o, ov, nv) -> buildSpires.run());
        arenaPane.heightProperty().addListener((o, ov, nv) -> buildSpires.run());

        Rectangle ground = new Rectangle();
        ground.widthProperty().bind(arenaPane.widthProperty());
        ground.yProperty().bind(arenaPane.heightProperty().multiply(0.75));
        ground.heightProperty().bind(arenaPane.heightProperty().multiply(0.25));
        ground.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#3a1a0a", 0.95)),
                new Stop(0.5, Color.web("#1a0810", 0.92)),
                new Stop(1, Color.web("#0a0205", 0.98))));
        arenaPane.getChildren().add(ground);

        Rectangle lavaGlow = new Rectangle();
        lavaGlow.widthProperty().bind(arenaPane.widthProperty());
        lavaGlow.yProperty().bind(arenaPane.heightProperty().multiply(0.72));
        lavaGlow.heightProperty().bind(arenaPane.heightProperty().multiply(0.10));
        lavaGlow.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#ff5010", 0.40)),
                new Stop(1, Color.TRANSPARENT)));
        lavaGlow.setMouseTransparent(true);
        arenaPane.getChildren().add(lavaGlow);

        Rectangle horizon = new Rectangle();
        horizon.widthProperty().bind(arenaPane.widthProperty());
        horizon.yProperty().bind(arenaPane.heightProperty().multiply(0.748));
        horizon.setHeight(2);
        horizon.setFill(Color.web("#ff6020", 0.8));
        horizon.setEffect(new DropShadow(10, Color.web("#ff4010")));
        arenaPane.getChildren().add(horizon);

        for (int i = 1; i <= 10; i++) {
            Line tile = new Line();
            tile.setStroke(Color.web("#8b3010", 0.30));
            tile.setStrokeWidth(1);
            tile.startYProperty().bind(arenaPane.heightProperty().multiply(0.75));
            tile.endYProperty().bind(arenaPane.heightProperty());
            tile.startXProperty().bind(arenaPane.widthProperty().multiply(0.5));
            final double xi = i;
            tile.endXProperty().bind(arenaPane.widthProperty().multiply(0.5 + (xi - 5.5) * 0.14));
            arenaPane.getChildren().add(tile);
        }
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            Line depth = new Line();
            depth.setStroke(Color.web("#8b3010", 0.20));
            depth.setStrokeWidth(1);
            depth.startXProperty().bind(arenaPane.widthProperty().multiply(0.02));
            depth.endXProperty().bind(arenaPane.widthProperty().multiply(0.98));
            depth.startYProperty().bind(arenaPane.heightProperty().multiply(0.75 + idx * 0.065));
            depth.endYProperty().bind(arenaPane.heightProperty().multiply(0.75 + idx * 0.065));
            arenaPane.getChildren().add(depth);
        }

        for (int i = 0; i < 12; i++) {
            Line crack = new Line();
            crack.setStroke(Color.web("#ff4010", 0.55 + rng.nextDouble() * 0.35));
            crack.setStrokeWidth(1.2);
            crack.setEffect(new DropShadow(10, Color.web("#ff2000", 0.9)));
            double xBase = 0.08 + rng.nextDouble() * 0.84;
            double yBase = 0.78 + rng.nextDouble() * 0.19;
            crack.startXProperty().bind(arenaPane.widthProperty().multiply(xBase));
            crack.startYProperty().bind(arenaPane.heightProperty().multiply(yBase));
            final double dx = (rng.nextDouble() - 0.5) * 0.10;
            final double dy = rng.nextDouble() * 0.08;
            crack.endXProperty().bind(arenaPane.widthProperty().multiply(xBase + dx));
            crack.endYProperty().bind(arenaPane.heightProperty().multiply(yBase + dy));
            crack.setMouseTransparent(true);
            arenaPane.getChildren().add(crack);
        }

        for (int side = 0; side < 2; side++) {
            Rectangle pillar = new Rectangle(50, 420);
            pillar.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#1a0808", 0.0)),
                    new Stop(0.35, Color.web("#2a1010", 0.98)),
                    new Stop(0.65, Color.web("#3a1515", 0.98)),
                    new Stop(1, Color.web("#1a0808", 0.0))));
            pillar.xProperty().bind(side == 0
                    ? arenaPane.widthProperty().multiply(0.0)
                    : arenaPane.widthProperty().subtract(50));
            pillar.yProperty().bind(arenaPane.heightProperty().multiply(0.20));
            arenaPane.getChildren().add(pillar);

            for (int r = 0; r < 3; r++) {
                Circle rune = new Circle(5);
                rune.setFill(Color.web("#ff2525", 0.9));
                rune.setEffect(new DropShadow(18, Color.web("#ff1010")));
                rune.centerXProperty().bind(side == 0
                        ? arenaPane.widthProperty().multiply(0.021)
                        : arenaPane.widthProperty().subtract(27));
                final double yOff = 0.32 + r * 0.11;
                rune.centerYProperty().bind(arenaPane.heightProperty().multiply(yOff));
                rune.setMouseTransparent(true);

                ScaleTransition pulse = new ScaleTransition(Duration.millis(1200 + r * 220), rune);
                pulse.setFromX(0.80); pulse.setFromY(0.80);
                pulse.setToX(1.30);   pulse.setToY(1.30);
                pulse.setAutoReverse(true);
                pulse.setCycleCount(ScaleTransition.INDEFINITE);
                pulse.play();

                FadeTransition flicker = new FadeTransition(Duration.millis(900 + r * 180), rune);
                flicker.setFromValue(0.65); flicker.setToValue(1.0);
                flicker.setAutoReverse(true);
                flicker.setCycleCount(FadeTransition.INDEFINITE);
                flicker.play();

                arenaPane.getChildren().add(rune);
            }
        }

        for (int i = 0; i < 9; i++) {
            Line chain = new Line();
            chain.setStroke(Color.web("#2a1010"));
            chain.setStrokeWidth(3);
            double xBase = 0.06 + rng.nextDouble() * 0.88;
            chain.startXProperty().bind(arenaPane.widthProperty().multiply(xBase));
            chain.endXProperty().bind(arenaPane.widthProperty().multiply(xBase));
            chain.setStartY(0);
            chain.endYProperty().bind(arenaPane.heightProperty().multiply(0.12 + rng.nextDouble() * 0.30));
            chain.setMouseTransparent(true);
            arenaPane.getChildren().add(chain);
        }

        for (int i = 0; i < 28; i++) {
            Circle ember = new Circle(1 + rng.nextDouble() * 1.7);
            ember.setFill(Color.web(rng.nextBoolean() ? "#ff6020" : "#ff2020", 0.85));
            ember.setEffect(new DropShadow(10, Color.web("#ff4010")));
            ember.setLayoutX(rng.nextDouble() * 1200);
            ember.setLayoutY(rng.nextDouble() * 500);
            ember.setMouseTransparent(true);
            arenaPane.getChildren().add(ember);

            TranslateTransition rise = new TranslateTransition(Duration.millis(3800 + rng.nextInt(3200)), ember);
            rise.setByY(-380 - rng.nextInt(220));
            rise.setByX((rng.nextDouble() - 0.5) * 90);
            rise.setCycleCount(TranslateTransition.INDEFINITE);
            rise.play();

            FadeTransition flick = new FadeTransition(Duration.millis(800 + rng.nextInt(900)), ember);
            flick.setFromValue(0.25); flick.setToValue(1.0);
            flick.setAutoReverse(true);
            flick.setCycleCount(FadeTransition.INDEFINITE);
            flick.play();
        }

        turnOrderBar = new HBox(6);
        turnOrderBar.setAlignment(Pos.CENTER_LEFT);
        turnOrderBar.setLayoutX(16);
        turnOrderBar.setLayoutY(12);
        turnOrderBar.setPadding(new Insets(6, 10, 6, 10));
        turnOrderBar.setStyle("-fx-background-color: rgba(30,8,15,0.85); " +
                "-fx-background-radius: 10; -fx-border-color: #6b2020; " +
                "-fx-border-radius: 10; -fx-border-width: 1;");
        arenaPane.getChildren().add(turnOrderBar);

        turnBannerMain = new Label("READY");
        turnBannerMain.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 22px; -fx-font-weight: bold; " +
                "-fx-font-family: 'Georgia';");
        turnBannerSub = new Label("");
        turnBannerSub.setStyle("-fx-text-fill: #cbb0b0; -fx-font-size: 12px;");

        turnBanner = new VBox(2, turnBannerMain, turnBannerSub);
        turnBanner.setAlignment(Pos.CENTER);
        turnBanner.setPadding(new Insets(8, 30, 10, 30));
        turnBanner.setMouseTransparent(true);
        turnBanner.setStyle("-fx-background-color: rgba(30,8,15,0.94); " +
                "-fx-background-radius: 12; -fx-border-color: #ff6b6b; " +
                "-fx-border-width: 2; -fx-border-radius: 12;");

        turnBanner.layoutXProperty().bind(
                arenaPane.widthProperty().subtract(turnBanner.widthProperty()).divide(2));
        turnBanner.layoutYProperty().bind(arenaPane.heightProperty().multiply(0.03));
        arenaPane.getChildren().add(turnBanner);

        arenaPane.widthProperty().addListener((o, ov, nv) -> layoutBattlers());
        arenaPane.heightProperty().addListener((o, ov, nv) -> layoutBattlers());

        Platform.runLater(buildSpires::run);

        return arenaPane;
    }

    private void layoutBattlers() {
        double w = arenaPane.getWidth();
        double h = arenaPane.getHeight();
        if (w <= 0 || h <= 0) return;

        int nh = heroBattlers.size();
        if (nh > 0) {
            double[][] hr = nh == 1
                    ? new double[][] { { 0.06, 0.53 } }
                    : new double[][] { { 0.03, 0.56 }, { 0.24, 0.48 } };
            for (int i = 0; i < nh; i++) {
                heroBattlers.get(i).setLayoutX(w * hr[i][0]);
                heroBattlers.get(i).setLayoutY(h * hr[i][1]);
            }
        }

        int ne = enemyBattlers.size();
        if (ne > 0) {
            double[][] er;
            if (ne == 1)      er = new double[][] { { 0.72, 0.10 } };
            else if (ne == 2) er = new double[][] { { 0.58, 0.17 }, { 0.80, 0.09 } };
            else              er = new double[][] { { 0.47, 0.20 }, { 0.68, 0.11 }, { 0.87, 0.20 } };
            for (int i = 0; i < ne; i++) {
                enemyBattlers.get(i).setLayoutX(w * er[i][0]);
                enemyBattlers.get(i).setLayoutY(h * er[i][1]);
            }
        }
    }

    private double bx(Battler b) { return b.getBoundsInParent().getCenterX(); }
    private double by(Battler b) { return b.getBoundsInParent().getCenterY(); }

    private Node buildBottom() {
        VBox bottom = new VBox(0);
        bottom.setStyle("-fx-background-color: #0a0d1a;");

        logBox = new VBox(2);
        logBox.setPadding(new Insets(6, 18, 6, 18));
        logBox.setStyle("-fx-background-color: #140610;");

        logScroll = new ScrollPane(logBox);
        logScroll.setFitToWidth(true);
        logScroll.setPrefHeight(74);
        logScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        logScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        logScroll.setStyle("-fx-background: #140610; -fx-background-color: #140610; " +
                "-fx-border-color: #3d0a10; -fx-border-width: 1 0 0 0;");

        heroStatBar = new HBox(14);
        heroStatBar.setAlignment(Pos.CENTER_LEFT);
        heroStatBar.setPadding(new Insets(10, 12, 12, 22));
        HBox.setHgrow(heroStatBar, Priority.ALWAYS);

        actionBar = new HBox(8);
        actionBar.setAlignment(Pos.CENTER_RIGHT);
        actionBar.setPadding(new Insets(10, 22, 12, 12));
        actionBar.setMinWidth(520);

        HBox row = new HBox(0, heroStatBar, actionBar);
        row.setStyle("-fx-background-color: #140610; -fx-border-color: #3d0a10; -fx-border-width: 1 0 0 0;");
        row.setAlignment(Pos.CENTER_LEFT);

        bottom.getChildren().addAll(logScroll, row);
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
    }

    // ---------------- chapter metadata ----------------
    private String chapterNumber(int waveIndex) {
        return switch (waveIndex) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            default -> "V";
        };
    }
    private String chapterTitle(int waveIndex) {
        return switch (waveIndex) {
            case 1 -> "THE GATE";
            case 2 -> "THE WARDEN";
            case 3 -> "THE FALLEN LEGION";
            case 4 -> "THE ELITE GUARD";
            default -> "THE ASHEN KING";
        };
    }
    private String chapterSubtitle(int waveIndex) {
        return switch (waveIndex) {
            case 1 -> "Where every story in this Arena begins";
            case 2 -> "A crest on the beast's chest";
            case 3 -> "The ones who wore your colors";
            case 4 -> "The last line before the throne";
            default -> "He has been waiting for you";
        };
    }

    private void nextWave() {
        wave++;
        waveLabel.setText("WAVE " + wave + " / " + MAX_WAVES);

        // Codex unlock as chapters begin
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

    // -------------------------------------------------------------------
    //  TURN ENGINE
    // -------------------------------------------------------------------

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

        StoryCodex.unlock(switch (wave) {
            case 1 -> "malgoroth-first";
            default -> null;
        });

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
        String color, main, sub;
        if (c instanceof Hero h) {
            int pidx = heroes.indexOf(h) + 1;
            main = "▶  PLAYER " + pidx + "'S TURN";
            sub  = h.name + "  ·  Lv " + h.level;
            color = h.colorHex();
        } else {
            main = "▶  ENEMY'S TURN";
            sub  = c.name;
            color = "#f87171";
        }

        turnBannerMain.setText(main);
        turnBannerMain.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 22px; " +
                "-fx-font-weight: bold; -fx-font-family: 'Georgia';");
        turnBannerSub.setText(sub);
        turnBanner.setStyle("-fx-background-color: rgba(30,8,15,0.94); " +
                "-fx-background-radius: 12; -fx-border-color: " + color + "; " +
                "-fx-border-width: 2; -fx-border-radius: 12;");

        FadeTransition ft = new FadeTransition(Duration.millis(220), turnBanner);
        ft.setFromValue(0.35); ft.setToValue(1.0);
        ScaleTransition st = new ScaleTransition(Duration.millis(220), turnBanner);
        st.setFromX(0.95); st.setFromY(0.95);
        st.setToX(1.0); st.setToY(1.0);
        ft.play(); st.play();

        turnLabel.setText(c instanceof Hero h
                ? "PLAYER " + (heroes.indexOf(h) + 1)
                : "ENEMY");
        turnLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12.5px; -fx-font-weight: bold;");
    }

    private void buildActionBar(Hero hero) {
        actionBar.getChildren().clear();

        if (hero.ultimate != null) {
            boolean ready = hero.hasUlt();
            Button ult = new Button(ready
                    ? "★ " + hero.ultimate.name.toUpperCase() + "  (ULT)"
                    : "★ ULT  " + (int) (hero.ultRatio() * 100) + "%");
            ult.setPrefHeight(46);
            ult.setStyle(ultStyle(ready, false));
            ult.setOnMouseEntered(e -> ult.setStyle(ultStyle(ready, true)));
            ult.setOnMouseExited(e  -> ult.setStyle(ultStyle(ready, false)));
            ult.setDisable(!ready || !awaitingInput || gameOver);
            ult.setOnAction(e -> doHeroUltimate(hero));
            actionBar.getChildren().add(ult);
        }

        int shortcut = 1;
        for (Skill s : hero.skills) {
            int cd = hero.cooldownOf(s);
            boolean canUse = !gameOver && hero.mp >= s.mpCost && cd == 0;
            String label = "[" + shortcut + "] " + s.name + (s.mpCost > 0 ? "  ·  " + s.mpCost + " MP" : "");
            if (cd > 0) label += "  (CD " + cd + ")";
            Button b = makeActionButton(label, hero.colorHex(), canUse);
            b.setDisable(!canUse);
            b.setOnAction(e -> doHeroSkill(hero, s));
            actionBar.getChildren().add(b);
            shortcut++;
        }

        Button pot = makeActionButton("Potion ×" + hero.potions, "#4ade80", hero.potions > 0);
        pot.setDisable(hero.potions <= 0);
        pot.setOnAction(e -> doHeroPotion(hero));
        actionBar.getChildren().add(pot);

        Button eth = makeActionButton("Ether ×" + hero.ethers, "#38bdf8", hero.ethers > 0);
        eth.setDisable(hero.ethers <= 0);
        eth.setOnAction(e -> doHeroEther(hero));
        actionBar.getChildren().add(eth);
    }

    private String ultStyle(boolean ready, boolean hover) {
        String c = ready ? "#facc15" : "#475569";
        String fill = hover && ready ? c : "rgba(250,204,21,0.08)";
        String fg = hover && ready ? "#0a0c13" : c;
        return "-fx-background-color: " + fill + ";" +
               "-fx-border-color: " + c + ";" +
               "-fx-border-width: 2;" +
               "-fx-border-radius: 8;" +
               "-fx-background-radius: 8;" +
               "-fx-text-fill: " + fg + ";" +
               "-fx-font-size: 11.5px;" +
               "-fx-font-weight: bold;" +
               "-fx-padding: 6 14;" +
               "-fx-cursor: hand;";
    }

    private void showWaitingBar() {
        actionBar.getChildren().clear();
        Label l = new Label("· · ·  enemy is acting  · · ·");
        l.setStyle("-fx-text-fill: #6b2020; -fx-font-size: 14px; -fx-font-style: italic;");
        actionBar.getChildren().add(l);
    }

    private Button makeActionButton(String text, String color, boolean enabled) {
        Button b = new Button(text);
        b.setPrefHeight(40);
        b.setStyle(actionStyle(color, false, enabled));
        b.setOnMouseEntered(e -> { if (!b.isDisabled()) b.setStyle(actionStyle(color, true, true)); });
        b.setOnMouseExited(e  -> b.setStyle(actionStyle(color, false, !b.isDisabled())));
        return b;
    }

    private String actionStyle(String c, boolean hover, boolean enabled) {
        String fill = hover ? c : "rgba(255,255,255,0.02)";
        String fg   = hover ? "#0a0c13" : c;
        return "-fx-background-color: " + fill + ";" +
               "-fx-border-color: " + (enabled ? c : "#475569") + ";" +
               "-fx-border-width: 1.5;" +
               "-fx-border-radius: 8;" +
               "-fx-background-radius: 8;" +
               "-fx-text-fill: " + (enabled ? fg : "#475569") + ";" +
               "-fx-font-size: 11.5px;" +
               "-fx-font-weight: bold;" +
               "-fx-padding: 6 12;" +
               "-fx-cursor: hand;";
    }

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
        actionBar.getChildren().clear();

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
        actionBar.getChildren().clear();

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

        heroStatBar.getChildren().clear();
        for (int i = 0; i < heroes.size(); i++) {
            heroStatBar.getChildren().add(buildHeroStatPanel(heroes.get(i), i + 1));
        }

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

    private Node buildHeroStatPanel(Hero h, int playerNumber) {
        boolean isActive = h == current && awaitingInput;

        Label pBadge = new Label("P" + playerNumber);
        pBadge.setStyle("-fx-background-color: " + h.colorHex() + ";" +
                "-fx-text-fill: #0a0c13; -fx-font-size: 10px; -fx-font-weight: bold;" +
                "-fx-padding: 2 6; -fx-background-radius: 4;");

        Label name = new Label(h.name.toUpperCase() + "  Lv" + h.level);
        name.setStyle("-fx-text-fill: " + h.colorHex() + "; -fx-font-size: 11.5px; -fx-font-weight: bold;");

        Label roleBadge = new Label(h.role.toUpperCase());
        roleBadge.setStyle("-fx-background-color: " + h.colorHex() + "22;" +
                "-fx-text-fill: " + h.colorHex() + ";" +
                "-fx-font-size: 9px; -fx-font-weight: bold;" +
                "-fx-padding: 1 6; -fx-background-radius: 4;" +
                "-fx-border-color: " + h.colorHex() + "77;" +
                "-fx-border-width: 1; -fx-border-radius: 4;");

        HBox titleRow = new HBox(6, pBadge, name, roleBadge);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label hpText = new Label(h.hp + " / " + h.maxHp);
        hpText.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px; -fx-font-family: 'Consolas';");
        Label mpText = new Label(h.mp + " / " + h.maxMp);
        mpText.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px; -fx-font-family: 'Consolas';");

        StatBar hpBar = new StatBar(160, 8, Color.web("#22c55e"));
        hpBar.setRatio(h.hpRatio(), false);
        StatBar mpBar = new StatBar(160, 6, Color.web("#3b82f6"));
        mpBar.setRatio(h.mpRatio(), false);

        HBox hpRow = new HBox(6, smallTag("HP", "#22c55e"), hpBar, hpText);
        hpRow.setAlignment(Pos.CENTER_LEFT);
        HBox mpRow = new HBox(6, smallTag("MP", "#3b82f6"), mpBar, mpText);
        mpRow.setAlignment(Pos.CENTER_LEFT);

        Label ultTag = new Label(h.hasUlt() ? "★ ULT READY" : "ULT " + (int) (h.ultRatio() * 100) + "%");
        ultTag.setStyle("-fx-text-fill: " + (h.hasUlt() ? "#facc15" : "#64748b") + ";" +
                "-fx-font-size: 10px; -fx-font-weight: bold;");

        VBox panel = new VBox(3, titleRow, hpRow, mpRow, ultTag);
        panel.setPadding(new Insets(6, 10, 6, 10));

        String borderColor = isActive ? "#facc15" : h.colorHex() + "44";
        double borderWidth = isActive ? 2 : 1;
        String bg = isActive ? "rgba(250,204,21,0.10)" : "rgba(255,255,255,0.025)";
        panel.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 8; " +
                "-fx-border-color: " + borderColor + "; -fx-border-radius: 8; -fx-border-width: " + borderWidth + ";");

        return panel;
    }

    private Label smallTag(String t, String c) {
        Label l = new Label(t);
        l.setStyle("-fx-text-fill: " + c + "; -fx-font-size: 10px; -fx-font-weight: bold;");
        l.setMinWidth(20);
        return l;
    }

    private void updateTargetHighlight() {
        Enemy t = currentEnemyTarget();
        for (Battler b : enemyBattlers) b.setSelected(b.getCombatant() == t && awaitingInput);
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