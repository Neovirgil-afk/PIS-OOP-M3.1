package com.example.ims001;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;

public class ProfileView {

    private final MainApp mainApp;
    private final BorderPane root = new BorderPane();
    private final String username;

    private ImageView imageView;
    private Label lblMsg;

    public ProfileView(MainApp mainApp) {
        this.mainApp = mainApp;
        this.username = Session.getUsername();
        buildUI();
    }

    private void buildUI() {
        Integer userId = UserDAO.getUserIdByUsername(username);
        String profileImagePath = UserDAO.getProfileImageByUsername(username);

        Label title = new Label("Profile");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button btnBack = new Button("Back");
        btnBack.setOnAction(e -> mainApp.showDashboardView());

        HBox topBar = new HBox(10, btnBack, title);
        topBar.setPadding(new Insets(15));
        topBar.setAlignment(Pos.CENTER_LEFT);
        root.setTop(topBar);

        imageView = new ImageView();
        imageView.setFitWidth(140);
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(true);

        loadProfileImage(profileImagePath);

        Button btnUpload = new Button("Change Picture");
        btnUpload.setOnAction(e -> chooseImage());

        Label lblUsername = new Label("Username: " + username);
        lblUsername.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        Label lblUserId = new Label("User ID: " + (userId != null ? userId : "Not found"));
        lblUserId.setStyle("-fx-font-size: 14px;");

        Label passTitle = new Label("Change Password");
        passTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        PasswordField txtNewPass = new PasswordField();
        txtNewPass.setPromptText("Enter new password");

        PasswordField txtConfirmPass = new PasswordField();
        txtConfirmPass.setPromptText("Confirm new password");

        Button btnChangePass = new Button("Update Password");
        btnChangePass.setMaxWidth(Double.MAX_VALUE);
        btnChangePass.setOnAction(e -> {
            String newPass = txtNewPass.getText();
            String confirmPass = txtConfirmPass.getText();

            if (newPass.isBlank() || confirmPass.isBlank()) {
                showMsg("Please fill in both password fields.", "orange");
                return;
            }

            if (!newPass.equals(confirmPass)) {
                showMsg("Passwords do not match.", "orange");
                return;
            }

            boolean ok = UserDAO.updatePassword(username, newPass);
            if (ok) {
                txtNewPass.clear();
                txtConfirmPass.clear();
                showMsg("Password updated successfully ✅", "#03DE82");
            } else {
                showMsg("Failed to update password.", "red");
            }
        });

        lblMsg = new Label("");
        lblMsg.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        VBox profileBox = new VBox(
                12,
                imageView,
                btnUpload,
                lblUsername,
                lblUserId,
                new Separator(),
                passTitle,
                txtNewPass,
                txtConfirmPass,
                btnChangePass,
                lblMsg
        );

        profileBox.setPadding(new Insets(20));
        profileBox.setAlignment(Pos.TOP_CENTER);
        profileBox.setMaxWidth(350);
        profileBox.setStyle("""
                -fx-background-color: rgba(255,255,255,0.06);
                -fx-background-radius: 14;
                -fx-border-color: rgba(255,255,255,0.12);
                -fx-border-radius: 14;
                """);

        StackPane centerWrap = new StackPane(profileBox);
        centerWrap.setPadding(new Insets(20));
        root.setCenter(centerWrap);
    }

    private void loadProfileImage(String path) {
        try {
            if (path != null && !path.isBlank()) {
                File file = new File(path);
                if (file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                    return;
                }
            }

            imageView.setImage(new Image(getClass().getResource("/default-profile.png").toExternalForm()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void chooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Profile Picture");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = chooser.showOpenDialog(root.getScene().getWindow());
        if (file == null) return;

        boolean ok = UserDAO.updateProfileImage(username, file.getAbsolutePath());
        if (ok) {
            loadProfileImage(file.getAbsolutePath());
            showMsg("Profile picture updated ✅", "#03DE82");
        } else {
            showMsg("Failed to update profile picture.", "red");
        }
    }

    private void showMsg(String text, String color) {
        lblMsg.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        lblMsg.setText(text);
    }

    public Parent getView() {
        return root;
    }
}