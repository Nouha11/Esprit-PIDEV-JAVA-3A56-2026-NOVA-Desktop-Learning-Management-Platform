package controllers.users;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.users.User;
import services.users.UserService;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TutorRequestsController implements Initializable {

    @FXML private Label   lblCount;
    @FXML private VBox    cardsContainer;
    @FXML private VBox    paneEmpty;
    @FXML private ScrollPane scrollPane;

    private final UserService userService = new UserService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadRequests();
    }

    @FXML
    private void onRefresh() {
        loadRequests();
    }

    private void loadRequests() {
        cardsContainer.getChildren().clear();
        try {
            List<User> pending = userService.getAllUsers().stream()
                .filter(u -> u.getRole() == User.Role.ROLE_TUTOR && !u.isActive())
                .collect(Collectors.toList());

            lblCount.setText(pending.size() + " pending application" + (pending.size() == 1 ? "" : "s"));

            if (pending.isEmpty()) {
                paneEmpty.setVisible(true); paneEmpty.setManaged(true);
                scrollPane.setVisible(false); scrollPane.setManaged(false);
                return;
            }

            paneEmpty.setVisible(false); paneEmpty.setManaged(false);
            scrollPane.setVisible(true); scrollPane.setManaged(true);

            for (User u : pending) {
                cardsContainer.getChildren().add(buildCard(u));
            }

        } catch (SQLException e) {
            lblCount.setText("Error loading applications");
        }
    }

    private VBox buildCard(User user) {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14; " +
                      "-fx-border-color: #e2e8f0; -fx-border-radius: 14; -fx-border-width: 1; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");

        // ── Header ──────────────────────────────────────────────────────────
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 20, 16, 20));
        header.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 14 14 0 0; " +
                        "-fx-border-color: transparent transparent #e2e8f0 transparent; -fx-border-width: 0 0 1 0;");

        // Avatar initials
        StackPane avatar = new StackPane();
        avatar.setPrefSize(44, 44); avatar.setMinSize(44, 44);
        avatar.setStyle("-fx-background-color: #0d6efd; -fx-background-radius: 50;");
        String initials = user.getUsername().length() >= 2
            ? user.getUsername().substring(0, 2).toUpperCase()
            : user.getUsername().toUpperCase();
        Label lblInitials = new Label(initials);
        lblInitials.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        avatar.getChildren().add(lblInitials);

        VBox userInfo = new VBox(3);
        HBox.setHgrow(userInfo, Priority.ALWAYS);
        Label lblName = new Label(user.getUsername());
        lblName.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Label lblEmail = new Label(user.getEmail());
        lblEmail.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        Label lblDate = new Label("Applied: " + (user.getCreatedAt() != null
            ? user.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
            : "Unknown"));
        lblDate.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        userInfo.getChildren().addAll(lblName, lblEmail, lblDate);

        Label badge = new Label("PENDING");
        badge.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-font-size: 10px; " +
                       "-fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 4 10;");

        header.getChildren().addAll(avatar, userInfo, badge);

        // ── Application fields ───────────────────────────────────────────────
        VBox body = new VBox(10);
        body.setPadding(new Insets(16, 20, 16, 20));

        String raw = user.getTutorMotivation();
        if (raw != null && !raw.isBlank()) {
            for (String line : raw.split("\n")) {
                if (line.isBlank()) continue;
                int colon = line.indexOf(":");
                if (colon > 0) {
                    String key = line.substring(0, colon).trim();
                    String val = line.substring(colon + 1).trim();
                    VBox field = buildField(key, val);
                    body.getChildren().add(field);
                }
            }
        } else {
            Label noData = new Label("No application data provided.");
            noData.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");
            body.getChildren().add(noData);
        }

        // ── Action buttons ───────────────────────────────────────────────────
        HBox actions = new HBox(10);
        actions.setPadding(new Insets(12, 20, 16, 20));
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setStyle("-fx-border-color: #e2e8f0 transparent transparent transparent; -fx-border-width: 1 0 0 0;");

        Button btnDeny = new Button("✕  Deny Application");
        btnDeny.setStyle("-fx-background-color: #fff1f2; -fx-text-fill: #e11d48; -fx-font-weight: bold; " +
                         "-fx-font-size: 13px; -fx-padding: 9 20; -fx-background-radius: 8; -fx-cursor: hand; " +
                         "-fx-border-color: #fecdd3; -fx-border-radius: 8; -fx-border-width: 1;");

        Button btnAccept = new Button("✓  Accept & Activate");
        btnAccept.setStyle("-fx-background-color: #0d6efd; -fx-text-fill: white; -fx-font-weight: bold; " +
                           "-fx-font-size: 13px; -fx-padding: 9 20; -fx-background-radius: 8; -fx-cursor: hand;");

        btnDeny.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Deny and delete the application from \"" + user.getUsername() + "\"?",
                ButtonType.YES, ButtonType.NO);
            confirm.setTitle("Deny Application"); confirm.setHeaderText(null);
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.YES) {
                    try { userService.deleteUser(user.getId()); loadRequests(); refreshAdminBadge(); }
                    catch (Exception ex) { showAlert("Error", ex.getMessage()); }
                }
            });
        });

        btnAccept.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Accept \"" + user.getUsername() + "\" as a tutor?\nTheir account will be activated immediately.",
                ButtonType.YES, ButtonType.NO);
            confirm.setTitle("Accept Tutor"); confirm.setHeaderText(null);
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.YES) {
                    try {
                        user.setActive(true); user.setVerified(true);
                        userService.updateUser(user);
                        loadRequests(); refreshAdminBadge();
                        showAlert("Accepted", user.getUsername() + " is now an active tutor!");
                    } catch (Exception ex) { showAlert("Error", ex.getMessage()); }
                }
            });
        });

        actions.getChildren().addAll(btnDeny, btnAccept);
        card.getChildren().addAll(header, body, actions);
        return card;
    }

    private VBox buildField(String key, String value) {
        VBox box = new VBox(4);
        box.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8; -fx-padding: 10 12;");
        Label lKey = new Label(key.toUpperCase());
        lKey.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #64748b; -fx-letter-spacing: 0.5px;");
        Label lVal = new Label(value);
        lVal.setWrapText(true); lVal.setMaxWidth(Double.MAX_VALUE);
        lVal.setStyle("-fx-font-size: 13px; -fx-text-fill: #1e293b; -fx-line-spacing: 2px;");
        box.getChildren().addAll(lKey, lVal);
        return box;
    }

    private void refreshAdminBadge() {
        // Refresh the badge in the admin dashboard if accessible
        try {
            javafx.scene.Node node = cardsContainer.getScene() != null
                ? cardsContainer.getScene().lookup("#lblPendingCount") : null;
            if (node instanceof Label lbl) {
                long count = userService.getAllUsers().stream()
                    .filter(u -> u.getRole() == User.Role.ROLE_TUTOR && !u.isActive())
                    .count();
                lbl.setText(String.valueOf(count));
                lbl.setVisible(count > 0);
            }
        } catch (Exception ignored) {}
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setTitle(title); alert.setHeaderText(null); alert.showAndWait();
    }
}