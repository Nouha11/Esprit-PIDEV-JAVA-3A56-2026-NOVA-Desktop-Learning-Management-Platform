package controllers.admin;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.ParallelTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.List;

/**
 * Unified Statistics page — hosts all module stats in one place.
 * Each tab loads its existing FXML into the shared StackPane content area.
 */
public class AdminStatsController {

    @FXML private StackPane statsContent;
    @FXML private HBox      tabBar;

    @FXML private Button btnTabStudy;
    @FXML private Button btnTabGame;
    @FXML private Button btnTabQuiz;
    @FXML private Button btnTabForum;

    private List<Button> tabs;

    @FXML
    public void initialize() {
        tabs = List.of(btnTabStudy, btnTabGame, btnTabQuiz, btnTabForum);
        // Load Study Session stats by default
        showStudyStats();
    }

    @FXML private void showStudyStats() {
        setActiveTab(btnTabStudy);
        loadStats("/views/admin/AdminAnalyticsDashboardView.fxml");
    }

    @FXML private void showGameStats() {
        setActiveTab(btnTabGame);
        loadStats("/views/gamification/stats.fxml");
    }

    @FXML private void showQuizStats() {
        setActiveTab(btnTabQuiz);
        loadStats("/views/quiz/quiz_stats.fxml");
    }

    @FXML private void showForumStats() {
        setActiveTab(btnTabForum);
        loadStats("/views/forum/admin/forum_stats.fxml");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void setActiveTab(Button active) {
        for (Button tab : tabs) {
            tab.getStyleClass().removeAll("stats-tab-active", "stats-tab");
            tab.getStyleClass().add(tab == active ? "stats-tab-active" : "stats-tab");
        }
    }

    private void loadStats(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));

            // Animate in
            view.setOpacity(0);
            view.setTranslateY(12);
            statsContent.getChildren().setAll(view);

            FadeTransition ft = new FadeTransition(Duration.millis(300), view);
            ft.setToValue(1);
            TranslateTransition tt = new TranslateTransition(Duration.millis(300), view);
            tt.setToY(0);
            new ParallelTransition(ft, tt).play();

        } catch (Exception e) {
            System.err.println("[AdminStatsController] Failed to load " + fxmlPath + ": " + e.getMessage());
        }
    }
}
