package com.example.ims001;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ForgotPasswordView {

    private MainApp mainApp;
    private StackPane root;

    private TextField txtUsername, txtResetCode, txtNewPasswordText;
    private PasswordField txtNewPassword;
    private CheckBox showPassword;
    private Label lblMessage;

    public ForgotPasswordView(MainApp mainApp) {
        this.mainApp = mainApp;

        root = new StackPane();
        root.getStyleClass().add("login-root");

        // Background
        Image bgGif = new Image(getClass().getResource("/images/blackbg.jpg").toExternalForm());
        ImageView bgView = new ImageView(bgGif);
        bgView.setFitWidth(1920);
        bgView.setFitHeight(1080);
        bgView.setPreserveRatio(false);
        bgView.setOpacity(0.3);

        // Main HBox
        HBox mainBox = new HBox();
        mainBox.setAlignment(Pos.CENTER);
        mainBox.setSpacing(0);

        VBox leftCard = createLeftCard();
        VBox rightCard = createForgotCard();

        mainBox.getChildren().addAll(leftCard, rightCard);
        root.getChildren().addAll(bgView, mainBox);
    }

    private VBox createLeftCard() {
        VBox leftCard = new VBox();
        leftCard.setPrefWidth(500);
        leftCard.setAlignment(Pos.CENTER);
        leftCard.getStyleClass().addAll("left-card", "frosted-glass");

        StackPane innerBox = new StackPane();
        innerBox.getStyleClass().addAll("inner-black-box");
        innerBox.prefWidthProperty().bind(leftCard.widthProperty().multiply(0.9));
        innerBox.prefHeightProperty().bind(leftCard.heightProperty().multiply(0.8));
        innerBox.setAlignment(Pos.BOTTOM_CENTER);

        VBox textContainer = new VBox(10);
        textContainer.setAlignment(Pos.BOTTOM_CENTER);
        textContainer.setPadding(new Insets(20));

        Label logo = new Label(" *");
        logo.getStyleClass().add("inner-title");
        logo.setStyle("-fx-text-fill: #03DE82; -fx-font-size: 100px; -fx-font-weight: bold;");
        StackPane.setAlignment(logo, Pos.TOP_LEFT);
        StackPane.setMargin(logo, new Insets(15, 0, 0, 15));

        Label title = new Label("Prestige Inventory Suites");
        title.getStyleClass().add("inner-title");
        title.styleProperty().bind(
                innerBox.heightProperty().multiply(0.035).asString(
                        "-fx-text-fill: #03DE82; -fx-font-weight: bold; -fx-font-family: Inter; -fx-font-size: %.0fpx;"
                )
        );
        title.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(title, new Insets(0, 0, 0, 10));
        title.setAlignment(Pos.CENTER_LEFT);

        Label description = new Label(
                "This is a powerful inventory management app. " +
                        "Keep track of your stock effortlessly. " +
                        "Designed to make your business operations smooth."
        );
        description.getStyleClass().add("inner-description");
        description.setWrapText(true);
        description.styleProperty().bind(
                innerBox.heightProperty().multiply(0.03).asString(
                        "-fx-text-fill: #03DE82; -fx-font-family: Inter; -fx-font-size: %.0fpx;"
                )
        );
        description.maxWidthProperty().bind(innerBox.widthProperty().multiply(0.85));
        description.setAlignment(Pos.CENTER);

        textContainer.getChildren().addAll(title, description);
        innerBox.getChildren().addAll(logo, textContainer);
        leftCard.getChildren().add(innerBox);
        VBox.setVgrow(innerBox, javafx.scene.layout.Priority.ALWAYS);

        return leftCard;
    }

    private VBox createForgotCard() {
        VBox rightCard = new VBox(12);
        rightCard.setPrefWidth(700);
        rightCard.setAlignment(Pos.CENTER_LEFT);
        rightCard.setPadding(new Insets(40, 90, 40, 90));
        rightCard.getStyleClass().add("right-card");

        Label title = new Label("Forgot Password");
        title.getStyleClass().add("title");

        Label quote = new Label("Reset your password using your permanent reset code.");
        quote.getStyleClass().add("title_2");
        quote.setWrapText(true);

        Label userLbl = new Label("Username");
        userLbl.getStyleClass().add("title_3");

        txtUsername = new TextField();
        txtUsername.setPromptText("Enter your Username");
        txtUsername.setMaxWidth(480);
        txtUsername.getStyleClass().add("input-field");

        Label codeLbl = new Label("Reset Code");
        codeLbl.getStyleClass().add("title_3");

        txtResetCode = new TextField();
        txtResetCode.setPromptText("Enter your Reset Code");
        txtResetCode.setMaxWidth(480);
        txtResetCode.getStyleClass().add("input-field");

        Label passLbl = new Label("New Password");
        passLbl.getStyleClass().add("title_3");

        txtNewPassword = new PasswordField();
        txtNewPassword.setPromptText("Enter New Password");
        txtNewPassword.setMaxWidth(480);
        txtNewPassword.getStyleClass().add("input-field");

        txtNewPasswordText = new TextField();
        txtNewPasswordText.setPromptText("Enter New Password");
        txtNewPasswordText.setMaxWidth(480);
        txtNewPasswordText.getStyleClass().add("input-field");
        txtNewPasswordText.setVisible(false);
        txtNewPasswordText.setManaged(false);

        txtNewPassword.textProperty().bindBidirectional(txtNewPasswordText.textProperty());

        // Show password checkbox on its own row
        showPassword = new CheckBox("Show Password");
        showPassword.getStyleClass().add("pass-button");
        showPassword.setOnAction(e -> {
            boolean show = showPassword.isSelected();
            txtNewPassword.setVisible(!show);
            txtNewPassword.setManaged(!show);
            txtNewPasswordText.setVisible(show);
            txtNewPasswordText.setManaged(show);
        });

        // Reset button
        Button btnReset = new Button("Reset Password");
        btnReset.getStyleClass().add("primary-button");
        btnReset.setOnAction(e -> handleReset());

        // Back to Login button
        Button btnBack = new Button("Back to Login");
        btnBack.getStyleClass().add("secondary-button");
        btnBack.setOnAction(e -> mainApp.showLoginView());

        lblMessage = new Label();
        lblMessage.getStyleClass().add("message-label");

        // Add all elements vertically like LoginView
        rightCard.getChildren().addAll(
                title,
                quote,
                userLbl,
                txtUsername,
                codeLbl,
                txtResetCode,
                passLbl,
                txtNewPassword,
                txtNewPasswordText,
                showPassword,  // checkbox row
                btnReset,      // primary button
                btnBack,       // secondary button
                lblMessage
        );

        return rightCard;
    }

    // LOGIC
    private void handleReset() {
        String username = txtUsername.getText();
        String resetCode = txtResetCode.getText();
        String newPassword = showPassword.isSelected()
                ? txtNewPasswordText.getText()
                : txtNewPassword.getText();

        if (username.isEmpty() || resetCode.isEmpty() || newPassword.isEmpty()) {
            lblMessage.setText("All fields are required.");
            lblMessage.getStyleClass().setAll("message-error");
            return;
        }

        if (!isStrongPassword(newPassword)) {
            lblMessage.setText("Password must be at least 8 chars, include upper, lower, number & symbol.");
            lblMessage.getStyleClass().setAll("message-error");
            return;
        }

        if (!UserDAO.checkResetCode(username, resetCode)) {
            lblMessage.setText("Invalid username or reset code.");
            lblMessage.getStyleClass().setAll("message-error");
            return;
        }

        if (UserDAO.updatePassword(username, newPassword)) {
            lblMessage.setText("Password reset successful! Go back to login.");
            lblMessage.getStyleClass().setAll("message-success");
        } else {
            lblMessage.setText("Failed to reset password. Try again later.");
            lblMessage.getStyleClass().setAll("message-error");
        }
    }

    private boolean isStrongPassword(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*") &&
                password.matches(".*[!@#$%^&*()].*");
    }

    public Parent getView() {
        return root;
    }
}
