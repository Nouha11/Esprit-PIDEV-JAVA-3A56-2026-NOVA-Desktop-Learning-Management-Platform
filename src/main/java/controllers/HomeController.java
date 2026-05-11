package controllers;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import services.gamification.LeaderboardService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HomeController {

    // The container from FXML
    @FXML private Pane animationPane;

    @FXML private VBox cardCourses;
    @FXML private VBox cardForum;
    @FXML private VBox cardLibrary;
    @FXML private VBox cardGames;
    @FXML private VBox cardQuiz;

    @FXML private VBox leaderboardPreview;

    private final LeaderboardService leaderboardService = new LeaderboardService();

    // Animation variables
    private Canvas canvas;
    private List<Particle> particles = new ArrayList<>();
    private final int NUM_PARTICLES = 50; // Number of floating dots
    private final double MAX_DISTANCE = 110.0; // How close they need to be to draw a line

    @FXML
    public void initialize() {
        // 1. Start the Network Nodes Animation
        setupNetworkAnimation();

        // 2. Staggered Entry Animation for the Module Cards
        VBox[] cards = {cardCourses, cardForum, cardLibrary, cardGames, cardQuiz};
        for (int i = 0; i < cards.length; i++) {
            VBox card = cards[i];
            if (card == null) continue;

            card.setOpacity(0);
            card.setTranslateY(40);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(600), card);
            fadeIn.setToValue(1.0);

            TranslateTransition slideUp = new TranslateTransition(Duration.millis(600), card);
            slideUp.setToY(0);
            slideUp.setInterpolator(Interpolator.SPLINE(0.25, 1, 0.5, 1));

            ParallelTransition popIn = new ParallelTransition(fadeIn, slideUp);
            popIn.setDelay(Duration.millis(100 * i));
            popIn.play();
        }

        // 3. Load top 3 leaderboard
        loadLeaderboardPreview();
    }

    // ── Leaderboard Preview ───────────────────────────────────────────────────

    private void loadLeaderboardPreview() {
        if (leaderboardPreview == null) return;

        Thread t = new Thread(() -> {
            try {
                List<LeaderboardService.PlayerEntry> top3 = leaderboardService.getLeaderboard("", "xp", 3);
                Platform.runLater(() -> {
                    leaderboardPreview.getChildren().clear();
                    if (top3 == null || top3.isEmpty()) {
                        Label empty = new Label("No players yet. Start playing to appear here!");
                        empty.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 13px; -fx-padding: 20;");
                        leaderboardPreview.getChildren().add(empty);
                        return;
                    }
                    for (int i = 0; i < top3.size(); i++) {
                        HBox row = buildPreviewRow(top3.get(i));
                        row.setOpacity(0);
                        leaderboardPreview.getChildren().add(row);
                        FadeTransition ft = new FadeTransition(Duration.millis(300), row);
                        ft.setDelay(Duration.millis(i * 80L));
                        ft.setToValue(1);
                        ft.play();
                    }
                });
            } catch (Exception e) {
                System.err.println("[HomeController] Leaderboard load failed: " + e.getMessage());
            }
        }, "LeaderboardPreview");
        t.setDaemon(true);
        t.start();
    }

    private HBox buildPreviewRow(LeaderboardService.PlayerEntry entry) {
        // Dark theme colors matching app-dark.css
        String[] rowBg = {
            "-fx-background-color: rgba(246,201,14,0.08);",   // gold tint  — rank 1
            "-fx-background-color: rgba(168,178,193,0.06);",  // silver tint — rank 2
            "-fx-background-color: rgba(205,127,50,0.07);"    // bronze tint — rank 3
        };
        String[] medalColors = { "#f6c90e", "#a8b2c1", "#cd7f32" };
        String[] medals      = { "🥇", "🥈", "🥉" };

        int idx = Math.min(entry.rank - 1, 2);

        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 20, 12, 20));
        row.setStyle(rowBg[idx] +
            "-fx-border-color: transparent transparent rgba(255,255,255,0.05) transparent;" +
            "-fx-border-width: 0 0 1 0;");
        row.setCursor(javafx.scene.Cursor.HAND);
        row.setOnMouseClicked(e -> goToLeaderboard(null));

        // Hover effect — dark surface highlight
        row.setOnMouseEntered(e -> row.setStyle(
            "-fx-background-color: rgba(0,242,254,0.06);" +
            "-fx-border-color: transparent transparent rgba(255,255,255,0.05) transparent;" +
            "-fx-border-width: 0 0 1 0;"));
        row.setOnMouseExited(e -> row.setStyle(rowBg[idx] +
            "-fx-border-color: transparent transparent rgba(255,255,255,0.05) transparent;" +
            "-fx-border-width: 0 0 1 0;"));

        // Medal / rank badge
        Label medal = new Label(medals[idx]);
        medal.setStyle("-fx-font-size: 20px; -fx-min-width: 55; -fx-alignment: CENTER;");

        // Avatar — profile picture if available, else initials circle
        javafx.scene.layout.StackPane avatarPane = buildAvatar(entry, medalColors[idx]);

        // Username + level name
        VBox nameBox = new VBox(2);
        nameBox.setPadding(new Insets(0, 0, 0, 12));
        HBox.setHgrow(nameBox, javafx.scene.layout.Priority.ALWAYS);
        Label username = new Label(entry.username);
        username.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #f8fafc;");
        Label levelName = new Label(entry.levelName);
        levelName.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        nameBox.getChildren().addAll(username, levelName);

        // Level badge — dark style
        Label levelBadge = new Label("Lv. " + entry.level);
        levelBadge.setStyle(
            "-fx-background-color: rgba(59,79,216,0.25); -fx-text-fill: #818cf8;" +
            "-fx-font-weight: bold; -fx-font-size: 11px;" +
            "-fx-background-radius: 10; -fx-padding: 3 10;" +
            "-fx-min-width: 90; -fx-alignment: CENTER;");

        // XP — warm gold
        Label xp = new Label("⭐ " + entry.totalXp);
        xp.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #fbbf24; -fx-min-width: 80; -fx-alignment: CENTER;");

        // Tokens — cyan accent
        Label tokens = new Label("🪙 " + entry.totalTokens);
        tokens.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #00f2fe; -fx-min-width: 80; -fx-alignment: CENTER;");

        row.getChildren().addAll(medal, avatarPane, nameBox, levelBadge, xp, tokens);
        return row;
    }

    /** Builds a 36×36 circular avatar — profile picture if available, initials fallback. */
    private javafx.scene.layout.StackPane buildAvatar(LeaderboardService.PlayerEntry entry, String fallbackColor) {
        javafx.scene.layout.StackPane pane = new javafx.scene.layout.StackPane();
        pane.setPrefSize(36, 36);
        pane.setMinSize(36, 36);
        pane.setMaxSize(36, 36);

        String pic = entry.profilePicture;
        if (pic != null && !pic.isBlank()) {
            try {
                String imageUrl;
                if (pic.startsWith("http://") || pic.startsWith("https://")) {
                    // Already a full URL
                    imageUrl = pic;
                } else if (pic.startsWith("/")) {
                    // Absolute path from Symfony — prepend the Render base URL
                    imageUrl = "https://nova-learning-management-platform.onrender.com" + pic;
                } else if (pic.contains("/") || pic.contains("\\")) {
                    // Local file path (Java app uploads)
                    java.io.File f = new java.io.File(pic);
                    imageUrl = f.exists() ? f.toURI().toString() : null;
                } else {
                    // Just a filename — Symfony avatar stored as filename only
                    imageUrl = "https://nova-learning-management-platform.onrender.com/uploads/avatars/" + pic;
                }

                if (imageUrl != null) {
                    javafx.scene.image.Image img = new javafx.scene.image.Image(
                        imageUrl, 36, 36, true, true, true); // background loading
                    javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(img);
                    iv.setFitWidth(36);
                    iv.setFitHeight(36);
                    // Circular clip
                    Circle clip = new Circle(18, 18, 18);
                    iv.setClip(clip);
                    // Show initials while loading, swap to image when ready
                    Circle bg = new Circle(18);
                    bg.setFill(javafx.scene.paint.Color.web(fallbackColor));
                    Label initials = new Label(entry.initials);
                    initials.setStyle("-fx-text-fill: #0b1121; -fx-font-weight: bold; -fx-font-size: 12px;");
                    pane.getChildren().addAll(bg, initials);
                    img.progressProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal.doubleValue() >= 1.0 && !img.isError()) {
                            javafx.application.Platform.runLater(() -> {
                                pane.getChildren().setAll(iv);
                            });
                        }
                    });
                    return pane;
                }
            } catch (Exception ignored) {}
        }

        // Fallback: colored circle with initials
        Circle bg = new Circle(18);
        bg.setFill(javafx.scene.paint.Color.web(fallbackColor));
        Label initials = new Label(entry.initials);
        initials.setStyle("-fx-text-fill: #0b1121; -fx-font-weight: bold; -fx-font-size: 12px;");
        pane.getChildren().addAll(bg, initials);
        return pane;
    }

    @FXML void goToLeaderboard(ActionEvent event) {
        NovaDashboardController.loadPage("/views/gamification/leaderboard.fxml");
    }

    // ── Network Animation ─────────────────────────────────────────────────────

    private void setupNetworkAnimation() {
        if (animationPane == null) return;

        canvas = new Canvas();
        // Bind canvas size to the pane so it resizes dynamically
        canvas.widthProperty().bind(animationPane.widthProperty());
        canvas.heightProperty().bind(animationPane.heightProperty());
        animationPane.getChildren().add(canvas);

        // Generate random particles
        Random rand = new Random();
        for (int i = 0; i < NUM_PARTICLES; i++) {
            particles.add(new Particle(
                    rand.nextDouble() * 600,
                    rand.nextDouble() * 340,
                    (rand.nextDouble() - 0.5) * 1.0, // X velocity
                    (rand.nextDouble() - 0.5) * 1.0  // Y velocity
            ));
        }

        // The animation loop (runs at 60fps)
        javafx.animation.AnimationTimer timer = new javafx.animation.AnimationTimer() {
            @Override
            public void handle(long now) {
                drawAnimation();
            }
        };
        timer.start();
    }

    private void drawAnimation() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        if (width == 0 || height == 0) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Clear previous frame
        gc.clearRect(0, 0, width, height);

        // Update positions and draw connecting lines
        for (int i = 0; i < particles.size(); i++) {
            Particle p1 = particles.get(i);
            p1.update(width, height);

            for (int j = i + 1; j < particles.size(); j++) {
                Particle p2 = particles.get(j);
                double dx = p1.x - p2.x;
                double dy = p1.y - p2.y;
                double dist = Math.sqrt(dx * dx + dy * dy);

                // If particles are close enough, draw a glowing line
                if (dist < MAX_DISTANCE) {
                    double opacity = 1.0 - (dist / MAX_DISTANCE);
                    // Glowing cyan color matching your theme
                    gc.setStroke(Color.color(0.0, 0.95, 1.0, opacity * 0.4));
                    gc.setLineWidth(1.2);
                    gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
        }

        // Draw the solid particle dots on top
        gc.setFill(Color.web("#00f2fe"));
        for (Particle p : particles) {
            gc.fillOval(p.x - p.radius, p.y - p.radius, p.radius * 2, p.radius * 2);
        }
    }

    // Inner class representing a single floating dot
    private static class Particle {
        double x, y, vx, vy;
        double radius = 2.5;

        Particle(double x, double y, double vx, double vy) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy;
        }

        void update(double width, double height) {
            x += vx;
            y += vy;

            // Bounce off walls softly
            if (x < 0 || x > width) vx *= -1;
            if (y < 0 || y > height) vy *= -1;

            // Safety bounds to prevent escaping
            if (x < 0) x = 0;
            if (x > width) x = width;
            if (y < 0) y = 0;
            if (y > height) y = height;
        }
    }

    @FXML void handleStartLearning(ActionEvent event) {
        NovaDashboardController.loadPage("/views/studysession/UserStudyDashboard.fxml");
    }

    @FXML void goToCourses(MouseEvent event) {
        NovaDashboardController.loadPage("/views/studysession/UserStudyDashboard.fxml");
    }

    @FXML void goToForum(MouseEvent event) {
        NovaDashboardController.loadPage("/views/forum/forum_feed.fxml");
    }

    @FXML void goToLibrary(MouseEvent event) {
        NovaDashboardController.loadPage("/views/library/BookListView.fxml");
    }

    @FXML void goToGames(MouseEvent event) {
        NovaDashboardController.loadPage("/views/gamification/game_launcher.fxml");
    }

    @FXML void goToQuiz(MouseEvent event) {
        NovaDashboardController.loadPage("/views/quiz/quiz_play_list.fxml");
    }
}