package com.example.ims001;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.security.SecureRandom;

public class RegisterView {

    private MainApp mainApp;
    private StackPane root;

    private TextField txtUsername;
    private PasswordField txtPassword, txtConfirm;
    private TextField txtPasswordText, txtConfirmText;
    private Label lblMessage;
    private CheckBox showPassword;

    public RegisterView(MainApp mainApp) {
        this.mainApp = mainApp;

        root = new StackPane();
        root.getStyleClass().add("login-root");

        //background GIF
        Image bgGif = new Image(getClass().getResource("/images/blackbg.jpg").toExternalForm());
        ImageView bgView = new ImageView(bgGif);
        bgView.setFitWidth(1920);
        bgView.setFitHeight(1080);
        bgView.setPreserveRatio(false);
        bgView.setOpacity(0.3);

        //Main Layout
        HBox mainBox = new HBox();
        mainBox.setAlignment(Pos.CENTER);

        VBox leftCard = createLeftCard();
        VBox rightCard = createRegisterCard();

        mainBox.getChildren().addAll(leftCard, rightCard);
        root.getChildren().addAll(bgView, mainBox);
    }

    private VBox createLeftCard() {
        VBox leftCard = new VBox();
        leftCard.setPrefWidth(500);
        leftCard.setAlignment(Pos.CENTER);
        leftCard.getStyleClass().addAll("left-card", "frosted-glass");

        //Inner black box
        StackPane innerBox = new StackPane();
        innerBox.getStyleClass().addAll("inner-black-box");

        //innerBox responsive to leftCard size
        innerBox.prefWidthProperty().bind(leftCard.widthProperty().multiply(0.9));
        innerBox.prefHeightProperty().bind(leftCard.heightProperty().multiply(0.8));
        innerBox.setAlignment(Pos.BOTTOM_CENTER);

        //Text container inside the black box
        VBox textContainer = new VBox(10);
        textContainer.setAlignment(Pos.BOTTOM_CENTER);
        textContainer.setStyle("-fx-padding: 20;");

        //Logo placeholder
        Label logo = new Label(" *"); // placeholder logo
        logo.getStyleClass().add("inner-title");
        logo.setStyle("-fx-text-fill: #03DE82; -fx-font-size: 100px; -fx-font-weight: bold;");
        StackPane.setAlignment(logo, Pos.TOP_LEFT);
        StackPane.setMargin(logo, new javafx.geometry.Insets(15, 0, 0, 15)); // top-left margin

        //Title
        Label title = new Label("Prestige Inventory Suites");
        title.getStyleClass().add("inner-title");
        title.styleProperty().bind(
                innerBox.heightProperty().multiply(0.035).asString(
                        "-fx-text-fill: #03DE82; -fx-font-weight: bold; -fx-font-family: Inter; -fx-font-size: %.0fpx;"
                )
        );
        title.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(title, new javafx.geometry.Insets(0,0,0,10));
        title.setAlignment(Pos.CENTER_LEFT);

        //Description
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

        // added title and description to text container
        textContainer.getChildren().addAll(title, description);

        // added logo and text container to inner box
        innerBox.getChildren().addAll(logo, textContainer);

        // added inner box ngek
        leftCard.getChildren().add(innerBox);
        VBox.setVgrow(innerBox, javafx.scene.layout.Priority.ALWAYS);

        return leftCard;
    }

    private VBox createRegisterCard() {
        VBox rightCard = new VBox(12);
        rightCard.setPrefWidth(700);
        rightCard.setAlignment(Pos.CENTER_LEFT);
        rightCard.getStyleClass().add("right-card");

        Label title = new Label("Register");
        title.getStyleClass().add("title");

        Label subtitle = new Label("''Create your account to manage inventory efficiently.''");
        subtitle.getStyleClass().add("title_2");
        subtitle.setWrapText(true);

        Label userLbl = new Label("Username");
        userLbl.getStyleClass().add("title_3");

        txtUsername = new TextField();
        txtUsername.setPromptText("Enter your Username");
        txtUsername.setMaxWidth(480);
        txtUsername.getStyleClass().add("input-field");

        Label passLbl = new Label("Password");
        passLbl.getStyleClass().add("title_3");

        txtPassword = new PasswordField();
        txtPassword.setPromptText("Enter your Password");
        txtPassword.setMaxWidth(480);
        txtPassword.getStyleClass().add("input-field");

        txtPasswordText = new TextField();
        txtPasswordText.setPromptText("Enter your Password");
        txtPasswordText.setMaxWidth(480);
        txtPasswordText.getStyleClass().add("input-field");
        txtPasswordText.setVisible(false);
        txtPasswordText.setManaged(false);

        txtPassword.textProperty().bindBidirectional(txtPasswordText.textProperty());

        txtConfirm = new PasswordField();
        txtConfirm.setPromptText("Confirm Password");
        txtConfirm.setMaxWidth(480);
        txtConfirm.getStyleClass().add("input-field");

        txtConfirmText = new TextField();
        txtConfirmText.setPromptText("Confirm Password");
        txtConfirmText.setMaxWidth(480);
        txtConfirmText.getStyleClass().add("input-field");
        txtConfirmText.setVisible(false);
        txtConfirmText.setManaged(false);

        txtConfirm.textProperty().bindBidirectional(txtConfirmText.textProperty());

        showPassword = new CheckBox("Show Password");
        showPassword.getStyleClass().add("pass-button");
        showPassword.setOnAction(e -> {
            boolean show = showPassword.isSelected();

            txtPassword.setVisible(!show);
            txtPassword.setManaged(!show);
            txtPasswordText.setVisible(show);
            txtPasswordText.setManaged(show);

            txtConfirm.setVisible(!show);
            txtConfirm.setManaged(!show);
            txtConfirmText.setVisible(show);
            txtConfirmText.setManaged(show);
        });

        Button btnSuggest = new Button("Suggest Password");
        btnSuggest.getStyleClass().add("link-button");
        btnSuggest.setOnAction(e -> txtPassword.setText(generatePassword(10)));

        HBox optionRow = new HBox(15, showPassword, btnSuggest);
        optionRow.setAlignment(Pos.CENTER_LEFT);

        Button btnRegister = new Button("Create Account");
        btnRegister.getStyleClass().add("primary-button");
        btnRegister.setOnAction(e -> handleRegister());

        Button btnBack = new Button("Back to Login");
        btnBack.getStyleClass().add("secondary-button");
        btnBack.setOnAction(e -> mainApp.showLoginView());

        lblMessage = new Label();
        lblMessage.getStyleClass().add("message-label");

        rightCard.getChildren().addAll(
                title,
                subtitle,
                userLbl,
                txtUsername,
                passLbl,
                txtPassword,
                txtPasswordText,
                txtConfirm,
                txtConfirmText,
                optionRow,
                btnRegister,
                btnBack,
                lblMessage
        );

        return rightCard;
    }

    //LOGIC

    private void handleRegister() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        String confirm = txtConfirm.getText();
        String resetCode = generateResetCode();

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            lblMessage.setText("All fields are required.");
            lblMessage.getStyleClass().setAll("message-error");
            return;
        }

        if (!password.equals(confirm)) {
            lblMessage.setText("Passwords do not match.");
            lblMessage.getStyleClass().setAll("message-error");
            return;
        }

        if (!isStrongPassword(password)) {
            lblMessage.setText("Password must be 8+ chars with number & symbol.");
            lblMessage.getStyleClass().setAll("message-error");
            return;
        }

        boolean success = UserDAO.register(username, password, resetCode);
        if (success) {
            lblMessage.setText("Registered! Reset code: " + resetCode);
            lblMessage.getStyleClass().setAll("message-success");
        } else {
            lblMessage.setText("Registration failed. Username may exist.");
            lblMessage.getStyleClass().setAll("message-error");
        }
    }

    private boolean isStrongPassword(String password) {
        return password.length() >= 8 &&
                password.matches(".*\\d.*") &&
                password.matches(".*[!@#$%^&*()].*");
    }

    private String generateResetCode() {
        SecureRandom rand = new SecureRandom();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++)
            sb.append(chars.charAt(rand.nextInt(chars.length())));
        return sb.toString();
    }

    private String generatePassword(int length) {
        SecureRandom rand = new SecureRandom();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++)
            sb.append(chars.charAt(rand.nextInt(chars.length())));
        return sb.toString();
    }

    public Parent getView() {
        return root;
    }
}
