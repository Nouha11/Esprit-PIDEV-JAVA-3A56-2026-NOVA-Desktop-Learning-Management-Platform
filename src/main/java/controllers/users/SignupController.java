package controllers.users;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.users.User;
import org.mindrot.jbcrypt.BCrypt;
import services.api.OAuthService;
import services.users.UserService;
import services.users.ValidationUtil;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.UUID;

public class SignupController implements Initializable {

    @FXML private StackPane leftPanel;
    @FXML private VBox      rightPanel;
    @FXML private Pane      animatedSceneContainer;

    @FXML private TextField     tfEmail;
    @FXML private TextField     tfUsername;
    @FXML private PasswordField pfPassword;
    @FXML private PasswordField pfConfirm;
    @FXML private ComboBox<String> cbRole;
    @FXML private Label         lblError;
    @FXML private Button        btnSignup;
    @FXML private Button        btnGoogleSignup;
    @FXML private Button        btnLinkedInSignup;
    @FXML private TextField     tfPasswordVisible;
    @FXML private TextField     tfConfirmVisible;
    @FXML private Button        btnShowPassword;
    @FXML private Button        btnShowConfirm;
    @FXML private Rectangle     bar1, bar2, bar3, bar4;
    @FXML private Label         lblStrength;

    // Tutor application fields
    @FXML private VBox          paneApplication;
    @FXML private VBox          paneTutorBadge;
    @FXML private TextField     tfSpecialty;
    @FXML private ComboBox<String> cbExperience;
    @FXML private TextArea      taBackground;
    @FXML private TextArea      taMotivation;

    private boolean showingPassword = false;
    private boolean showingConfirm  = false;

    private final UserService  userService  = new UserService();
    private final OAuthService oauthService = new OAuthService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbRole.getItems().addAll("Student", "Tutor");
        cbRole.setValue("Student");
        lblError.setText("");

        if (cbExperience != null) {
            cbExperience.getItems().addAll(
                "Less than 1 year",
                "1-2 years",
                "3-5 years",
                "5-10 years",
                "More than 10 years"
            );
            cbExperience.setValue("1-2 years");
        }

        playEntranceAnimation();
        createBackgroundParticles();
        pfPassword.textProperty().addListener((obs, oldVal, newVal) -> updateStrengthMeter(newVal));
        if (tfPasswordVisible != null)
            tfPasswordVisible.textProperty().addListener((obs, oldVal, newVal) -> updateStrengthMeter(newVal));
    }

    // â”€â”€ Role change â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @FXML
    private void onRoleChanged() {
        boolean isTutor = "Tutor".equals(cbRole.getValue());
        if (paneApplication != null) { paneApplication.setVisible(isTutor); paneApplication.setManaged(isTutor); }
        if (paneTutorBadge  != null) { paneTutorBadge.setVisible(isTutor);  paneTutorBadge.setManaged(isTutor); }
        btnSignup.setText(isTutor ? "SUBMIT APPLICATION" : "CREATE ACCOUNT");
    }

    // â”€â”€ OAuth signup â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @FXML
    private void onGoogleSignup() {
        setOAuthButtonsDisabled(true);
        showInfo("Opening Google sign-up...");
        oauthService.loginWithGoogle(
            oauthUser -> Platform.runLater(() -> handleOAuthSignup(oauthUser)),
            err       -> Platform.runLater(() -> { showError(err); setOAuthButtonsDisabled(false); })
        );
    }

    @FXML
    private void onLinkedInSignup() {
        setOAuthButtonsDisabled(true);
        showInfo("Opening LinkedIn sign-up...");
        oauthService.loginWithLinkedIn(
            oauthUser -> Platform.runLater(() -> handleOAuthSignup(oauthUser)),
            err       -> Platform.runLater(() -> { showError(err); setOAuthButtonsDisabled(false); })
        );
    }

    private void handleOAuthSignup(OAuthService.OAuthUser oauthUser) {
        if (oauthUser == null || oauthUser.email == null) {
            showError("Could not retrieve account info."); setOAuthButtonsDisabled(false); return;
        }
        try {
            User existing = userService.findByEmail(oauthUser.email);
            if (existing != null) {
                showInfo("Account found! Logging you in...");
                PauseTransition p1 = new PauseTransition(Duration.seconds(1)); p1.setOnFinished(e -> launchDashboard(existing)); p1.play();
                return;
            }
            String base = buildUsername(oauthUser.name, oauthUser.email);
            String uname = makeUniqueUsername(base);
            User newUser = new User();
            newUser.setEmail(oauthUser.email); newUser.setUsername(uname);
            newUser.setPassword(BCrypt.hashpw(UUID.randomUUID().toString(), BCrypt.gensalt(13)));
            newUser.setRole(User.Role.ROLE_STUDENT); newUser.setActive(true);
            newUser.setVerified(true); newUser.setBanned(false); newUser.setXp(0);
            userService.addUser(newUser);
            User saved = userService.findByEmail(oauthUser.email);
            if (saved == null) { showError("Account creation failed."); setOAuthButtonsDisabled(false); return; }
            showInfo("Account created via " + oauthUser.provider + "! Welcome, " + uname + "!");
            PauseTransition p2 = new PauseTransition(Duration.seconds(1.5)); p2.setOnFinished(e -> launchDashboard(saved)); p2.play();
        } catch (SQLException e) { showError("Database error: " + e.getMessage()); setOAuthButtonsDisabled(false); }
    }

    private String buildUsername(String name, String email) {
        if (name != null && !name.isBlank())
            return name.trim().toLowerCase().replaceAll("[^a-z0-9_]", "_").replaceAll("_+", "_");
        return email.split("@")[0].replaceAll("[^a-z0-9_]", "_");
    }

    private String makeUniqueUsername(String base) throws SQLException {
        if (!userService.usernameExists(base)) return base;
        int i = 2; while (userService.usernameExists(base + i)) i++; return base + i;
    }

    private void launchDashboard(User user) {
        try {
            String fxml = user.getRole() == User.Role.ROLE_ADMIN ? "/views/admin/AdminDashboard.fxml" : "/views/NovaDashboard.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            if (user.getRole() != User.Role.ROLE_ADMIN) {
                ((controllers.NovaDashboardController) loader.getController()).setCurrentUser(user);
            } else {
                ((controllers.admin.AdminDashboardController) loader.getController()).setCurrentUser(user);
            }
            Stage stage = (Stage) btnSignup.getScene().getWindow();
            Scene scene = new Scene(root, 1280, 800);
            scene.getStylesheets().add(getClass().getResource("/css/users.css").toExternalForm());
            stage.setTitle("NOVA"); stage.setScene(scene); stage.setResizable(true);
            stage.setMaximized(true); stage.centerOnScreen();
        } catch (Exception e) { showError("Navigation error: " + e.getMessage()); }
    }

    // â”€â”€ Regular signup â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @FXML
    private void onSignup() {
        lblError.setStyle("-fx-text-fill: #ef4444;");
        lblError.setText("");

        String email       = tfEmail.getText().trim();
        String username    = tfUsername.getText().trim();
        String password    = pfPassword.getText();
        String confirm     = pfConfirm.getText();
        boolean isTutor    = "Tutor".equals(cbRole.getValue());
        String role        = isTutor ? "ROLE_TUTOR" : "ROLE_STUDENT";

        // Validate tutor application fields
        String specialty = "", experience = "", background = "", motivation = "";
        if (isTutor) {
            specialty   = tfSpecialty  != null ? tfSpecialty.getText().trim()  : "";
            experience  = cbExperience != null ? (cbExperience.getValue() != null ? cbExperience.getValue() : "") : "";
            background  = taBackground != null ? taBackground.getText().trim() : "";
            motivation  = taMotivation != null ? taMotivation.getText().trim() : "";

            if (specialty.isBlank())        { showError("Please enter what subjects you teach."); return; }
            if (background.length() < 20)   { showError("Please describe your professional background (min 20 characters)."); return; }
            if (motivation.length() < 20)   { showError("Please explain why you want to teach on NOVA (min 20 characters)."); return; }
        }

        List<String> errors = ValidationUtil.validateUser(email, username, password, role, true);
        if (!password.equals(confirm)) errors.add("Passwords do not match.");
        if (!errors.isEmpty()) { lblError.setText(errors.get(0)); return; }

        try {
            if (userService.emailExists(email))       { lblError.setText("This email is already registered."); return; }
            if (userService.usernameExists(username)) { lblError.setText("This username is already taken."); return; }

            User newUser = new User();
            newUser.setEmail(email); newUser.setUsername(username);
            newUser.setPassword(BCrypt.hashpw(password, BCrypt.gensalt(13)));
            newUser.setRole(User.Role.valueOf(role));
            newUser.setBanned(false); newUser.setXp(0);

            if (isTutor) {
                newUser.setActive(false);
                newUser.setVerified(false);
                // Store full application as structured text
                String application =
                    "SPECIALTY: " + specialty + "\n" +
                    "EXPERIENCE: " + experience + "\n" +
                    "BACKGROUND: " + background + "\n" +
                    "MOTIVATION: " + motivation;
                newUser.setTutorMotivation(application);
            } else {
                newUser.setActive(true);
                newUser.setVerified(false);
            }

            userService.addUser(newUser);

            if (isTutor) {
                lblError.setStyle("-fx-text-fill: #f59e0b;");
                lblError.setText("Application submitted! An admin will review your application within 24-48 hours.");
                PauseTransition p3 = new PauseTransition(Duration.seconds(3)); p3.setOnFinished(e -> goToLogin()); p3.play();
            } else {
                showInfo("Account created! Redirecting to login...");
                PauseTransition p4 = new PauseTransition(Duration.seconds(1.5)); p4.setOnFinished(e -> goToLogin()); p4.play();
            }

        } catch (SQLException e) { lblError.setText("Database error: " + e.getMessage()); }
    }

    @FXML private void onGoToLogin() { goToLogin(); }

    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/users/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnSignup.getScene().getWindow();
            Scene scene = new Scene(root, 900, 580);
            stage.setTitle("NOVA - Login"); stage.setScene(scene);
            stage.setResizable(false); stage.centerOnScreen();
        } catch (Exception e) { lblError.setText("Navigation error: " + e.getMessage()); }
    }

    // â”€â”€ Show/hide password â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @FXML
    private void onTogglePassword() {
        showingPassword = !showingPassword;
        if (showingPassword) {
            tfPasswordVisible.setText(pfPassword.getText());
            tfPasswordVisible.setVisible(true); tfPasswordVisible.setManaged(true);
            pfPassword.setVisible(false); pfPassword.setManaged(false);
            btnShowPassword.setText("Hide");
        } else {
            pfPassword.setText(tfPasswordVisible.getText());
            pfPassword.setVisible(true); pfPassword.setManaged(true);
            tfPasswordVisible.setVisible(false); tfPasswordVisible.setManaged(false);
            btnShowPassword.setText("Show");
        }
    }

    @FXML
    private void onToggleConfirm() {
        showingConfirm = !showingConfirm;
        if (showingConfirm) {
            tfConfirmVisible.setText(pfConfirm.getText());
            tfConfirmVisible.setVisible(true); tfConfirmVisible.setManaged(true);
            pfConfirm.setVisible(false); pfConfirm.setManaged(false);
            btnShowConfirm.setText("Hide");
        } else {
            pfConfirm.setText(tfConfirmVisible.getText());
            pfConfirm.setVisible(true); pfConfirm.setManaged(true);
            tfConfirmVisible.setVisible(false); tfConfirmVisible.setManaged(false);
            btnShowConfirm.setText("Show");
        }
    }

    // â”€â”€ Helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private void showError(String msg) { lblError.setStyle("-fx-text-fill: #ef4444;"); lblError.setText(msg); }
    private void showInfo(String msg)  { lblError.setStyle("-fx-text-fill: #16a34a;"); lblError.setText(msg); }

    private void setOAuthButtonsDisabled(boolean d) {
        if (btnGoogleSignup   != null) btnGoogleSignup.setDisable(d);
        if (btnLinkedInSignup != null) btnLinkedInSignup.setDisable(d);
    }

    private void updateStrengthMeter(String password) {
        if (bar1 == null) return;
        int score = 0;
        if (password.length() >= 8)                         score++;
        if (password.matches(".*[A-Z].*"))                  score++;
        if (password.matches(".*[0-9].*"))                  score++;
        if (password.matches(".*[!@#%^&*()_+=\\[\\]-].*")) score++;
        String[] colors = {"#e5e7eb","#e5e7eb","#e5e7eb","#e5e7eb"};
        String label = "", labelColor = "#9ca3af";
        switch (score) {
            case 1 -> { colors[0] = "#ef4444"; label = "Weak";   labelColor = "#ef4444"; }
            case 2 -> { colors[0] = "#f59e0b"; colors[1] = "#f59e0b"; label = "Fair"; labelColor = "#f59e0b"; }
            case 3 -> { colors[0] = "#3b82f6"; colors[1] = "#3b82f6"; colors[2] = "#3b82f6"; label = "Good"; labelColor = "#3b82f6"; }
            case 4 -> { colors[0] = "#22c55e"; colors[1] = "#22c55e"; colors[2] = "#22c55e"; colors[3] = "#22c55e"; label = "Strong"; labelColor = "#22c55e"; }
        }
        Rectangle[] bars = {bar1, bar2, bar3, bar4};
        for (int i = 0; i < 4; i++) bars[i].setFill(Color.web(colors[i]));
        if (lblStrength != null) {
            lblStrength.setText(password.isEmpty() ? "" : label);
            lblStrength.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:" + labelColor + ";");
        }
    }

    private void createBackgroundParticles() {
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            int size = random.nextInt(15) + 5;
            Rectangle rect = new Rectangle(size, size);
            rect.setFill(Color.web("#ffffff", random.nextDouble() * 0.15 + 0.05));
            rect.setX(random.nextInt(400)); rect.setY(random.nextInt(700));
            rect.setRotate(random.nextInt(360));
            animatedSceneContainer.getChildren().add(rect);
            TranslateTransition tt = new TranslateTransition(Duration.seconds(random.nextInt(15) + 15), rect);
            tt.setByY(-150 - random.nextInt(200)); tt.setByX((random.nextDouble() - 0.5) * 100);
            tt.setCycleCount(TranslateTransition.INDEFINITE); tt.setAutoReverse(true); tt.play();
            RotateTransition rt = new RotateTransition(Duration.seconds(random.nextInt(10) + 10), rect);
            rt.setByAngle(360); rt.setCycleCount(RotateTransition.INDEFINITE); rt.play();
            FadeTransition ft = new FadeTransition(Duration.seconds(random.nextInt(8) + 5), rect);
            ft.setFromValue(0.1); ft.setToValue(0.6); ft.setCycleCount(FadeTransition.INDEFINITE); ft.setAutoReverse(true); ft.play();
        }
    }

    private void playEntranceAnimation() {
        leftPanel.setOpacity(0); rightPanel.setOpacity(0);
        TranslateTransition st = new TranslateTransition(Duration.millis(600), rightPanel);
        st.setFromY(30); st.setToY(0);
        FadeTransition fr = new FadeTransition(Duration.millis(600), rightPanel); fr.setFromValue(0); fr.setToValue(1);
        FadeTransition fl = new FadeTransition(Duration.millis(800), leftPanel); fl.setFromValue(0); fl.setToValue(1);
        st.play(); fr.play(); fl.play();
    }
}