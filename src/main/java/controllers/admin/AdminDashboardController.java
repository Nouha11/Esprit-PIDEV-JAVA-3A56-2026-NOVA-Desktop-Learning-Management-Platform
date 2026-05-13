package controllers.admin;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.users.User;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AdminDashboardController {

    @FXML private StackPane contentArea;
    @FXML private Label     lblCurrentUser;
    @FXML private Label     lblCurrentRole;
    @FXML private Label     lblPageTitle;
    @FXML private Label     lblAvatarInitial;
    @FXML private VBox      navContainer;

    @FXML private Button btnToggleUsers, btnToggleLibrary, btnToggleStudy,
            btnToggleGame, btnToggleQuiz, btnToggleForum;
    @FXML private VBox   usersGroup, libraryGroup, studyGroup,
            gameGroup, quizGroup, forumGroup;

    @FXML private HBox navHome;
    @FXML private HBox navAllStats;
    @FXML private HBox navUsers;
    @FXML private HBox navTutorRequests;
    @FXML private HBox navBooks, navLoans, navPayments;
    @FXML private HBox navCourses, navPlannings, navSessions, navCalendar;
    @FXML private HBox navGames, navRewards;
    @FXML private HBox navQuizzes;
    @FXML private HBox navForum, navForumReports;





    @FXML private Label iconHome, iconUsers, iconTutorRequests, iconBooks, iconLoans, iconPayments;
    @FXML private Label iconAllStats;
    @FXML private Label iconCourses, iconPlannings, iconSessions, iconAnalytics, iconCalendar;
    @FXML private Label iconGames, iconRewards, iconGameStats;
    @FXML private Label iconQuizzes, iconQuizStats, iconForum, iconForumReports, iconForumStats; // ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â°ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â¦ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚ÂÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¥ Added iconForumReports

    @FXML private Label lblPendingCount;
    private List<HBox> allNavItems;
    private String adminUsername = "Admin";
    /** Stored so showCalendar() can pass the user to CalendarPlannerController. */
    private User currentUser;

    @FXML
    public void initialize() {
        // Set icons
        iconHome.setText("\u2B21");          // hexagon
        iconAllStats.setText("\uD83D\uDCCA"); // bar chart — unified stats
        iconUsers.setText("\uD83D\uDC65");   // people
        iconTutorRequests.setText("\uD83D\uDCCB"); // clipboard-check for tutor requests
        iconBooks.setText("\uD83D\uDCDA");   // books
        iconLoans.setText("\uD83D\uDCCB");   // clipboard
        iconPayments.setText("\uD83D\uDCB3"); // card
        iconCourses.setText("\uD83D\uDCD8"); // blue book
        iconPlannings.setText("\uD83D\uDCC5"); // calendar
        iconSessions.setText("\u23F1");      // stopwatch
        if (iconAnalytics  != null) iconAnalytics.setText("\uD83D\uDCCA"); // bar chart
        iconCalendar.setText("\uD83D\uDCC5");  // calendar
        iconGames.setText("\uD83C\uDFAE");   // game controller
        iconRewards.setText("\uD83C\uDFC6"); // trophy
        if (iconGameStats  != null) iconGameStats.setText("\uD83D\uDCC8"); // chart up
        iconQuizzes.setText("\uD83D\uDCDD"); // memo
        if (iconQuizStats  != null) iconQuizStats.setText("\uD83D\uDCCA"); // bar chart
        iconForum.setText("\uD83D\uDCAC");   // speech bubble
        iconForumReports.setText("\uD83D\uDEA9"); // flag
        if (iconForumStats != null) iconForumStats.setText("\uD83D\uDCCA"); // bar chart

        setToggleText(btnToggleUsers,   "USERS",         false);
        setToggleText(btnToggleLibrary, "LIBRARY",       false);
        setToggleText(btnToggleStudy,   "STUDY SESSION", false);
        setToggleText(btnToggleGame,    "GAMIFICATION",  false);
        setToggleText(btnToggleQuiz,    "QUIZ",          false);
        setToggleText(btnToggleForum,   "FORUM",         false);

        //  Added navForumReports to the list
        allNavItems = java.util.Arrays.asList(
                navHome, navAllStats, navUsers, navTutorRequests,
                navBooks, navLoans, navPayments,
                navCourses, navPlannings, navSessions, navCalendar,
                navGames, navRewards,
                navQuizzes, navForum, navForumReports
        );

        loadView("/views/admin/AdminHome.fxml");
        lblPageTitle.setText("Overview");
        refreshPendingBadge();

        Platform.runLater(() -> {
            if (contentArea.getScene() != null) {
                Stage stage = (Stage) contentArea.getScene().getWindow();
                stage.setMaximized(true);
                contentArea.getScene().getRoot().getProperties()
                        .put("adminDashboardController", this);
                playEntranceAnimation();
            }
        });
    }

    private void playEntranceAnimation() {
        if (navContainer == null) return;
        navContainer.setOpacity(0);
        navContainer.setTranslateX(-20);

        FadeTransition fade = new FadeTransition(Duration.millis(500), navContainer);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(500), navContainer);
        slide.setToX(0);
        slide.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(fade, slide).play();

        contentArea.setOpacity(0);
        contentArea.setTranslateY(16);
        PauseTransition delay = new PauseTransition(Duration.millis(200));
        delay.setOnFinished(e -> {
            FadeTransition cf = new FadeTransition(Duration.millis(400), contentArea);
            cf.setToValue(1);
            TranslateTransition ct = new TranslateTransition(Duration.millis(400), contentArea);
            ct.setToY(0);
            ct.setInterpolator(Interpolator.EASE_OUT);
            new ParallelTransition(cf, ct).play();
        });
        delay.play();
    }

    public void setCurrentUser(User user) {
        if (user == null) return;

        // Subtask 14.2: Role-based access guard for Admin Dashboard
        if (user.getRole() != User.Role.ROLE_ADMIN) {
            System.err.println("[ACCESS DENIED] Role " + user.getRole() + " attempted to access Admin Dashboard");
            redirectToCorrectDashboard(user);
            return;
        }

        this.currentUser = user;

        adminUsername = user.getUsername();
        lblCurrentUser.setText(adminUsername);
        lblCurrentRole.setText(user.getRole().name());
        if (lblAvatarInitial != null && !adminUsername.isEmpty())
            lblAvatarInitial.setText(String.valueOf(adminUsername.charAt(0)).toUpperCase());
    }

    /**
     * Subtask 14.2: Redirect users to their correct dashboard based on role
     */
    private void redirectToCorrectDashboard(User user) {
        try {
            Stage stage = (Stage) contentArea.getScene().getWindow();
            FXMLLoader loader;
            Parent root;
            javafx.scene.Scene scene;

            if (user.getRole() == User.Role.ROLE_TUTOR) {
                loader = new FXMLLoader(getClass().getResource("/views/studysession/TutorDashboard.fxml"));
                root = loader.load();
                controllers.studysession.TutorDashboardController tutorCtrl = loader.getController();
                tutorCtrl.setCurrentUser(user);
                scene = new javafx.scene.Scene(root, 1280, 800);
                scene.getStylesheets().add(getClass().getResource("/css/study.css").toExternalForm());
                stage.setTitle("NOVA - Tutor Dashboard");
            } else {
                // Default to Student Dashboard
                loader = new FXMLLoader(getClass().getResource("/views/NovaDashboard.fxml"));
                root = loader.load();
                controllers.NovaDashboardController dashCtrl = loader.getController();
                dashCtrl.setCurrentUser(user);
                scene = new javafx.scene.Scene(root, 1300, 800);
                stage.setTitle("NOVA - Student Hub");
            }

            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("Redirect error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML public void toggleUsers()   { toggle(usersGroup,   btnToggleUsers,   "USERS"); }
    @FXML public void toggleLibrary() { toggle(libraryGroup, btnToggleLibrary, "LIBRARY"); }
    @FXML public void toggleStudy()   { toggle(studyGroup,   btnToggleStudy,   "STUDY SESSION"); }
    @FXML public void toggleGame()    { toggle(gameGroup,    btnToggleGame,    "GAMIFICATION"); }
    @FXML public void toggleQuiz()    { toggle(quizGroup,    btnToggleQuiz,    "QUIZ"); }
    @FXML public void toggleForum()   { toggle(forumGroup,   btnToggleForum,   "FORUM"); }

    private void toggle(VBox group, Button btn, String label) {
        boolean open = !group.isVisible();
        group.setVisible(open);
        group.setManaged(open);
        setToggleText(btn, label, open);

        if (open) {
            group.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(200), group);
            ft.setToValue(1);
            ft.play();
        }
    }

    private void setToggleText(Button btn, String label, boolean open) {
        btn.setText(label + (open ? "  v" : "  >"));
    }

    @FXML public void showHome()       { nav(navHome,       "Overview",         "/views/admin/AdminHome.fxml"); }
    @FXML public void showAllStats()   { nav(navAllStats,   "Statistics",       "/views/admin/AdminStats.fxml"); }
    @FXML public void showUsers()      { nav(navUsers,      "Users",            "/views/users/user-list.fxml"); }
    @FXML public void showTutorRequests() { nav(navTutorRequests, "Tutor Requests", "/views/users/tutor-requests.fxml"); }
    @FXML public void showBooks()      { nav(navBooks,      "Books",            "/views/library/BookView.fxml"); }
    @FXML public void showLoans()      { nav(navLoans,      "Loans",            "/views/library/LoanView.fxml"); }
    @FXML public void showPayments()   { nav(navPayments,   "Payments",         "/views/library/PaymentView.fxml"); }
    @FXML public void showCourses()    { nav(navCourses,    "Courses",          "/views/studysession/CourseView.fxml"); }
    @FXML public void showPlannings()  { nav(navPlannings,  "Plannings",        "/views/studysession/PlanningView.fxml"); }
    @FXML public void showSessions()   { nav(navSessions,   "Study Sessions",   "/views/studysession/StudySessionView.fxml"); }

    @FXML public void showCalendar() {
        setActive(navCalendar);
        lblPageTitle.setText("Calendar");
        loadCalendarViewAnimated();
    }
    @FXML public void showGames()      { nav(navGames,      "Games",            "/views/gamification/game_list.fxml"); }
    @FXML public void showRewards()    { nav(navRewards,    "Rewards",          "/views/gamification/reward_list.fxml"); }
    @FXML public void showGameStats()  { nav(navAllStats,   "Statistics",       "/views/admin/AdminStats.fxml"); }

    @FXML public void showQuizzes()    { nav(navQuizzes,    "Quizzes",          "/views/quiz/quiz_list.fxml"); }
    @FXML public void showQuizStats()  { nav(navAllStats,   "Statistics",       "/views/admin/AdminStats.fxml"); }

    @FXML public void showAnalytics()  { nav(navAllStats,   "Statistics",       "/views/admin/AdminStats.fxml"); }
    @FXML public void showForumStats() { nav(navAllStats,   "Statistics",       "/views/admin/AdminStats.fxml"); }


    //  FORUM ROUTES 
    @FXML public void showForum()      { nav(navForum,      "Forum Management", "/views/forum/admin/admin_forum.fxml"); }
    @FXML public void showForumReports(){ nav(navForumReports,"Reported Content", "/views/forum/admin/admin_reports.fxml"); }


    private void nav(HBox active, String title, String fxmlPath) {
        setActive(active);
        lblPageTitle.setText(title);
        loadViewAnimated(fxmlPath);
    }

    private void setActive(HBox active) {
        for (HBox item : allNavItems) {
            item.getStyleClass().removeAll("admin-nav-row-active");
            if (!item.getStyleClass().contains("admin-nav-row"))
                item.getStyleClass().add("admin-nav-row");
            item.getChildren().forEach(n -> {
                if (n instanceof Label lbl) {
                    lbl.getStyleClass().removeAll("admin-nav-icon-active", "admin-nav-text-active");
                }
            });
        }
        active.getStyleClass().remove("admin-nav-row");
        active.getStyleClass().add("admin-nav-row-active");
        boolean first = true;
        for (var n : active.getChildren()) {
            if (n instanceof Label lbl) {
                if (first) {
                    lbl.getStyleClass().removeAll("admin-nav-icon");
                    lbl.getStyleClass().add("admin-nav-icon-active");
                    first = false;
                } else {
                    lbl.getStyleClass().removeAll("admin-nav-text");
                    lbl.getStyleClass().add("admin-nav-text-active");
                }
            }
        }
    }

    private void loadViewAnimated(String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent view = loader.load();
            Object ctrl = loader.getController();
            if (ctrl instanceof AdminHomeController homeCtrl)
                homeCtrl.setAdminUsername(adminUsername);
            if (ctrl instanceof controllers.users.UserListController userListCtrl)
                userListCtrl.setCurrentUser(currentUser);
            view.setOpacity(0);
            view.setTranslateY(20);
            contentArea.getChildren().setAll(view);
            FadeTransition ft = new FadeTransition(Duration.millis(350), view);
            ft.setToValue(1);
            TranslateTransition tt = new TranslateTransition(Duration.millis(350), view);
            tt.setToY(0);
            tt.setInterpolator(Interpolator.EASE_OUT);
            new ParallelTransition(ft, tt).play();
        } catch (Exception e) {
            System.err.println("Admin nav error [" + path + "]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads CalendarPlannerView.fxml and passes the current admin user to
     * CalendarPlannerController via setCurrentUser() for role-based data scoping.
     *
     * Requirements: 13.3, 17.1
     */
    private void loadCalendarViewAnimated() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/studysession/CalendarPlannerView.fxml"));
            Parent view = loader.load();

            // Pass current user so CalendarPlannerController scopes data to ROLE_ADMIN
            Object ctrl = loader.getController();
            if (ctrl instanceof controllers.studysession.CalendarPlannerController calendarCtrl
                    && currentUser != null) {
                calendarCtrl.setCurrentUser(currentUser);
            }

            view.setOpacity(0);
            view.setTranslateY(20);
            contentArea.getChildren().setAll(view);
            FadeTransition ft = new FadeTransition(Duration.millis(350), view);
            ft.setToValue(1);
            TranslateTransition tt = new TranslateTransition(Duration.millis(350), view);
            tt.setToY(0);
            tt.setInterpolator(Interpolator.EASE_OUT);
            new ParallelTransition(ft, tt).play();
        } catch (IOException e) {
            System.err.println("Admin nav error [CalendarPlannerView.fxml]: " + e.getMessage());
        }
    }

    private void loadView(String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent view = loader.load();
            Object ctrl = loader.getController();
            if (ctrl instanceof AdminHomeController homeCtrl && adminUsername != null)
                homeCtrl.setAdminUsername(adminUsername);
            if (ctrl instanceof controllers.users.UserListController userListCtrl)
                userListCtrl.setCurrentUser(currentUser);
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {}
    }

    @FXML
    private void onLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/users/login.fxml"));
            Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 900, 580);
            scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setMaximized(false);
            stage.setTitle("NOVA - Sign In");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.centerOnScreen();
        } catch (IOException e) {}
    }

    public void refreshPendingBadge() {
        try {
            long count = new services.users.UserService().getAllUsers().stream()
                .filter(u -> u.getRole() == models.users.User.Role.ROLE_TUTOR && !u.isActive())
                .count();
            if (lblPendingCount != null) {
                lblPendingCount.setText(String.valueOf(count));
                lblPendingCount.setVisible(count > 0);
            }
        } catch (Exception ignored) {}
    }
}



