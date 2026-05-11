package controllers.gamification;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import services.gamification.LeaderboardService;
import utils.UserSession;

import java.io.File;
import java.util.List;

public class LeaderboardController {

    @FXML private TextField        searchField;
    @FXML private ComboBox<String> sortCombo;
    @FXML private VBox             leaderboardList;
    @FXML private Label            lblTotal;
    @FXML private VBox             myRankSection;

    private final LeaderboardService service = new LeaderboardService();
    private int currentUserId = -1;

    @FXML
    public void initialize() {
        // Resolve current user ID from either session manager
        currentUserId = utils.SessionManager.getCurrentUserId();
        if (currentUserId <= 1) currentUserId = UserSession.getInstance().getUserId();

        sortCombo.getItems().addAll("Top XP", "Top Tokens", "Top Level");
        sortCombo.setValue("Top XP");
        sortCombo.valueProperty().addListener((o, a, b) -> loadLeaderboard());
        searchField.textProperty().addListener((o, a, b) -> {
            // Debounce
            javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(Duration.millis(300));
            pt.setOnFinished(e -> loadLeaderboard());
            pt.play();
        });
        loadMyRank();
        loadLeaderboard();
    }

    @FXML private void handleSearch()       { loadLeaderboard(); }
    @FXML private void handleClearSearch()  { searchField.clear(); loadLeaderboard(); }

    private void loadLeaderboard() {
        String search = searchField.getText().trim();
        String sort   = switch (sortCombo.getValue()) {
            case "Top Tokens" -> "tokens";
            case "Top Level"  -> "level";
            default           -> "xp";
        };

        leaderboardList.getChildren().clear();
        Label loading = new Label("Loading...");
        loading.setStyle("-fx-text-fill:#a0aec0;-fx-font-size:14px;-fx-padding:20;");
        leaderboardList.getChildren().add(loading);

        Thread t = new Thread(() -> {
            try {
                List<LeaderboardService.PlayerEntry> entries = service.getLeaderboard(search, sort, 50);
                Platform.runLater(() -> {
                    leaderboardList.getChildren().clear();
                    if (entries.isEmpty()) {
                        Label empty = new Label("No players found.");
                        empty.setStyle("-fx-text-fill:#a0aec0;-fx-font-size:14px;-fx-padding:20;");
                        leaderboardList.getChildren().add(empty);
                    } else {
                        if (lblTotal != null) lblTotal.setText(entries.size() + " players");
                        for (LeaderboardService.PlayerEntry e : entries) {
                            VBox row = buildRow(e);
                            row.setOpacity(0);
                            leaderboardList.getChildren().add(row);
                            FadeTransition ft = new FadeTransition(Duration.millis(200), row);
                            ft.setDelay(Duration.millis(e.rank * 30L));
                            ft.setToValue(1); ft.play();
                        }
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    leaderboardList.getChildren().clear();
                    Label err = new Label("Error: " + e.getMessage());
                    err.setStyle("-fx-text-fill:#e53e3e;-fx-font-size:13px;-fx-padding:20;");
                    leaderboardList.getChildren().add(err);
                });
            }
        });
        t.setDaemon(true); t.start();
    }

    private void loadMyRank() {
        // Try both session managers — use whichever gives a valid userId > 1
        if (currentUserId <= 0 || myRankSection == null) return;

        Thread t = new Thread(() -> {
            try {
                LeaderboardService.PlayerEntry me = service.getPlayerStats(currentUserId);
                Platform.runLater(() -> buildMyRankCard(me));
            } catch (Exception e) {
                System.err.println("[Leaderboard] loadMyRank error: " + e.getMessage());
            }
        });
        t.setDaemon(true); t.start();
    }

    private void buildMyRankCard(LeaderboardService.PlayerEntry me) {
        myRankSection.getChildren().clear();

        // ── Header ────────────────────────────────────────────────────────────
        HBox headerRow = new HBox(8);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setPadding(new Insets(0, 0, 14, 0));
        Label icon  = new Label("👤");  icon.setStyle("-fx-font-size:15px;");
        Label title = new Label("Your Ranking");
        title.setStyle("-fx-text-fill:rgba(255,255,255,0.9);-fx-font-size:14px;-fx-font-weight:bold;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        headerRow.getChildren().addAll(icon, title, spacer);

        if (me == null) {
            Label noProfile = new Label("🎮  Play games to appear on the leaderboard!");
            noProfile.setStyle("-fx-text-fill:rgba(255,255,255,0.75);-fx-font-size:13px;-fx-padding:8 0 0 0;");
            myRankSection.getChildren().addAll(headerRow, noProfile);
            myRankSection.setVisible(true); myRankSection.setManaged(true);
            return;
        }

        // ── Stats row ─────────────────────────────────────────────────────────
        HBox statsRow = new HBox(0);
        statsRow.setAlignment(Pos.CENTER);

        // Avatar + name + progress
        HBox playerBlock = new HBox(14);
        playerBlock.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(playerBlock, Priority.ALWAYS);

        StackPane avatarPane = buildAvatar(me, 52);

        VBox nameBox = new VBox(3);
        Label usernameLbl = new Label(me.username);
        usernameLbl.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:white;");
        Label levelLbl = new Label(me.levelName + "  ·  Level " + me.level);
        levelLbl.setStyle("-fx-font-size:12px;-fx-text-fill:rgba(255,255,255,0.65);");
        double progress = Math.max(0.0, Math.min(1.0, me.progressPct / 100.0));
        ProgressBar pb = new ProgressBar(progress);
        pb.setPrefWidth(160); pb.setStyle("-fx-accent:#f6d365;-fx-pref-height:6;");
        Label progLbl = new Label(me.progressPct + "% to next level");
        progLbl.setStyle("-fx-text-fill:rgba(255,255,255,0.5);-fx-font-size:10px;");
        nameBox.getChildren().addAll(usernameLbl, levelLbl, pb, progLbl);
        playerBlock.getChildren().addAll(avatarPane, nameBox);

        // Rank stat
        String rankColor = me.rank == 1 ? "#FFD700" : me.rank == 2 ? "#C0C0C0" : me.rank == 3 ? "#CD7F32" : "white";
        VBox rankBox = statCard(me.rank <= 0 ? "—" : "#" + me.rank, "Global Rank", rankColor, me.rank <= 3);
        VBox xpBox   = statCard(formatNum(me.totalXp),     "Total XP", "#f6d365", false);
        VBox tokBox  = statCard(formatNum(me.totalTokens), "Tokens",   "#a78bfa", false);

        statsRow.getChildren().addAll(playerBlock, rankBox, xpBox, tokBox);
        myRankSection.getChildren().addAll(headerRow, statsRow);
        myRankSection.setVisible(true); myRankSection.setManaged(true);

        // Entrance animation
        myRankSection.setOpacity(0); myRankSection.setTranslateY(-8);
        FadeTransition ft = new FadeTransition(Duration.millis(400), myRankSection); ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(400), myRankSection); tt.setToY(0);
        new ParallelTransition(ft, tt).play();
    }

    private VBox statCard(String value, String label, String valueColor, boolean glow) {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER);
        box.setMinWidth(100);
        box.setPadding(new Insets(0, 16, 0, 16));
        box.setStyle("-fx-border-color:rgba(255,255,255,0.12) transparent rgba(255,255,255,0.12) rgba(255,255,255,0.12);-fx-border-width:0 0 0 1;");
        Label v = new Label(value);
        v.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:" + valueColor + ";" +
            (glow ? "-fx-effect:dropshadow(gaussian," + valueColor + ",8,0.5,0,0);" : ""));
        Label l = new Label(label);
        l.setStyle("-fx-font-size:10px;-fx-text-fill:rgba(255,255,255,0.55);-fx-font-weight:bold;");
        box.getChildren().addAll(v, l);
        return box;
    }

    /** Circular avatar — profile picture or initials fallback. */
    private StackPane buildAvatar(LeaderboardService.PlayerEntry entry, double size) {
        double radius = size / 2.0;
        StackPane pane = new StackPane();
        pane.setPrefSize(size, size); pane.setMinSize(size, size); pane.setMaxSize(size, size);

        // Initials fallback (shown while image loads or if no picture)
        Circle bg = new Circle(radius);
        bg.setFill(Color.web(avatarColor(entry.rank)));
        Label initials = new Label(entry.initials);
        initials.setStyle("-fx-font-size:" + (int)(radius * 0.55) + "px;-fx-font-weight:bold;-fx-text-fill:white;");
        pane.getChildren().addAll(bg, initials);

        String pic = entry.profilePicture;
        if (pic != null && !pic.isBlank()) {
            try {
                String imageUrl;
                if      (pic.startsWith("http://") || pic.startsWith("https://")) imageUrl = pic;
                else if (pic.startsWith("/"))  imageUrl = "https://nova-learning-management-platform.onrender.com" + pic;
                else if (pic.contains("/") || pic.contains("\\")) {
                    File f = new File(pic); imageUrl = f.exists() ? f.toURI().toString() : null;
                } else imageUrl = "https://nova-learning-management-platform.onrender.com/uploads/avatars/" + pic;

                if (imageUrl != null) {
                    Image img = new Image(imageUrl, size, size, true, true, true);
                    img.progressProperty().addListener((obs, o, n) -> {
                        if (n.doubleValue() >= 1.0 && !img.isError()) {
                            ImageView iv = new ImageView(img);
                            iv.setFitWidth(size); iv.setFitHeight(size);
                            iv.setClip(new Circle(radius, radius, radius));
                            Platform.runLater(() -> pane.getChildren().setAll(iv));
                        }
                    });
                }
            } catch (Exception ignored) {}
        }
        return pane;
    }

    // ── Row builder ───────────────────────────────────────────────────────────
    private VBox buildRow(LeaderboardService.PlayerEntry e) {
        boolean isMe = (e.userId == currentUserId);

        // Rank badge
        StackPane rankBadge = new StackPane();
        rankBadge.setPrefSize(44, 44); rankBadge.setMaxSize(44, 44);
        Circle circle = new Circle(22);
        Label rankLbl = new Label();
        switch (e.rank) {
            case 1 -> { circle.setFill(Color.web("#FFD700")); rankLbl.setText("\uD83C\uDFC6"); rankLbl.setStyle("-fx-font-size:20px;"); }
            case 2 -> { circle.setFill(Color.web("#C0C0C0")); rankLbl.setText("\uD83E\uDD48"); rankLbl.setStyle("-fx-font-size:20px;"); }
            case 3 -> { circle.setFill(Color.web("#CD7F32")); rankLbl.setText("\uD83E\uDD49"); rankLbl.setStyle("-fx-font-size:20px;"); }
            default -> {
                circle.setFill(isMe ? Color.web("#667eea") : Color.web("#f0f2f8"));
                rankLbl.setText(String.valueOf(e.rank));
                rankLbl.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:" + (isMe ? "white" : "#718096") + ";");
            }
        }
        rankBadge.getChildren().addAll(circle, rankLbl);

        // Avatar with profile picture support
        StackPane avatar = buildAvatar(e, 44);

        // Username + optional "You" badge
        Label usernameLbl = new Label(e.username);
        usernameLbl.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:" + (isMe ? "#667eea" : "#1e2a5e") + ";");
        HBox usernameRow = new HBox(8);
        usernameRow.setAlignment(Pos.CENTER_LEFT);
        usernameRow.getChildren().add(usernameLbl);
        if (isMe) {
            Label youBadge = new Label("You");
            youBadge.setStyle("-fx-background-color:#667eea;-fx-text-fill:white;-fx-font-size:10px;" +
                "-fx-font-weight:bold;-fx-background-radius:8;-fx-padding:2 7;");
            usernameRow.getChildren().add(youBadge);
        }

        Label levelLbl = new Label(e.levelName + " · Lv." + e.level);
        levelLbl.setStyle("-fx-font-size:11px;-fx-text-fill:#718096;");
        ProgressBar pb = new ProgressBar(e.progressPct / 100.0);
        pb.setPrefWidth(120);
        pb.setStyle("-fx-accent:" + levelColor(e.level) + ";-fx-pref-height:5;");

        VBox playerInfo = new VBox(3, usernameRow, levelLbl, pb);
        playerInfo.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(playerInfo, Priority.ALWAYS);

        VBox xpBox  = statBox(formatNum(e.totalXp),     "XP",     isMe ? "#667eea" : "#3b4fd8");
        VBox tokBox = statBox(formatNum(e.totalTokens), "Tokens", isMe ? "#a78bfa" : "#b7791f");

        HBox row = new HBox(14, rankBadge, avatar, playerInfo, xpBox, tokBox);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));

        String bg = isMe
            ? "linear-gradient(to right,rgba(102,126,234,0.12),rgba(118,75,162,0.08))"
            : e.rank <= 3
                ? (e.rank == 1 ? "linear-gradient(to right,#fffbeb,#fff8e1)"
                 : e.rank == 2 ? "linear-gradient(to right,#f8f9ff,#f0f2f8)"
                 : "linear-gradient(to right,#fff5f0,#fff0eb)")
                : "white";

        String borderStyle = isMe
            ? "-fx-border-color:#667eea;-fx-border-width:0 0 0 3;"
            : "-fx-border-color:#e4e8f0;-fx-border-width:0 0 1 0;";

        VBox wrapper = new VBox(row);
        wrapper.setStyle("-fx-background-color:" + bg + ";" + borderStyle);
        if (!isMe) {
            wrapper.setOnMouseEntered(ev -> wrapper.setStyle(
                "-fx-background-color:#f5f7ff;-fx-border-color:#e4e8f0;-fx-border-width:0 0 1 0;"));
            wrapper.setOnMouseExited(ev -> wrapper.setStyle(
                "-fx-background-color:" + bg + ";" + borderStyle));
        }
        return wrapper;
    }

    private VBox statBox(String value, String label, String color) {
        Label v = new Label(value); v.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        Label l = new Label(label); l.setStyle("-fx-font-size:10px;-fx-text-fill:#a0aec0;");
        VBox b = new VBox(2, v, l); b.setAlignment(Pos.CENTER); b.setMinWidth(70);
        return b;
    }

    private String formatNum(int n) {
        if (n >= 1000) return String.format("%.1fk", n / 1000.0);
        return String.valueOf(n);
    }

    private String avatarColor(int rank) {
        String[] colors = {"#667eea","#27ae60","#e53e3e","#d97706","#805ad5","#2b6cb0","#276749"};
        return colors[(Math.max(rank, 1) - 1) % colors.length];
    }

    private String levelColor(int level) {
        if (level >= 15) return "#f6d365";
        if (level >= 10) return "#3b4fd8";
        if (level >= 5)  return "#27ae60";
        return "#a0aec0";
    }
}
