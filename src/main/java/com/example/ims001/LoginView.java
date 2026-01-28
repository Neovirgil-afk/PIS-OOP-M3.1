package com.example.ims001;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LoginView {

    private MainApp mainApp;
    private StackPane root;

    private TextField txtUsername;
    private PasswordField txtPassword;
    private TextField txtPasswordText;
    private Label lblMessage;
    private CheckBox showPassword;

    public LoginView(MainApp mainApp) {
        this.mainApp = mainApp;

        root = new StackPane();
        root.getStyleClass().add("login-root");

        //Background
        Image bgGif = new Image(getClass().getResource("/images/blackbg.jpg").toExternalForm());
        ImageView bgView = new ImageView(bgGif);
        bgView.setFitWidth(1920);
        bgView.setFitHeight(1080);
        bgView.setPreserveRatio(false);
        bgView.setOpacity(0.3);

        //Main HBox with left and right cards
        HBox mainBox = new HBox();
        mainBox.setAlignment(Pos.CENTER);
        mainBox.setSpacing(0);

        VBox leftCard = createLeftCard();
        VBox rightCard = createLoginCard();

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

        // Make innerBox responsive to leftCard size
        innerBox.prefWidthProperty().bind(leftCard.widthProperty().multiply(0.9));
        innerBox.prefHeightProperty().bind(leftCard.heightProperty().multiply(0.8));
        innerBox.setAlignment(Pos.BOTTOM_CENTER);

        //Text container inside black box
        VBox textContainer = new VBox(10);
        textContainer.setAlignment(Pos.BOTTOM_CENTER);
        textContainer.setStyle("-fx-padding: 20;");

        //Logo placeholder
        Label logo = new Label(" *"); // placeholder logo
        logo.getStyleClass().add("inner-title");
        logo.setStyle("-fx-text-fill: #03DE82; -fx-font-size: 100px; -fx-font-weight: bold;" );
        StackPane.setAlignment(logo, Pos.TOP_LEFT);
        StackPane.setMargin(logo, new javafx.geometry.Insets(15, 0, 0, 15)); // top-left margin

        //title
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

        //description
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

        // Added title and description to text container
        textContainer.getChildren().addAll(title, description);

        // Addd logo and text container to inner box
        innerBox.getChildren().addAll(logo, textContainer);

        // Adde inner box to left card
        leftCard.getChildren().add(innerBox);
        VBox.setVgrow(innerBox, javafx.scene.layout.Priority.ALWAYS);

        return leftCard;
    }







    private VBox createLoginCard() {
        VBox rightCard = new VBox(12);
        rightCard.setPrefWidth(700);
        rightCard.setAlignment(Pos.CENTER_LEFT);
        rightCard.getStyleClass().add("right-card");

        Label title = new Label("Login form");
        title.getStyleClass().add("title");

        Label quote = new Label("“Know what you have, sell with confidence, and never run out unexpectedly.”");
        quote.getStyleClass().add("title_2");
        quote.setWrapText(true);

        Label user = new Label("Username");
        user.getStyleClass().add("title_3");

        txtUsername = new TextField();
        txtUsername.setPromptText("Enter your Username");
        txtUsername.setMaxWidth(480);
        txtUsername.getStyleClass().add("input-field");

        Label pass = new Label("Password");
        pass.getStyleClass().add("title_3");

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

        showPassword = new CheckBox("Show Password");
        showPassword.getStyleClass().add("pass-button");
        showPassword.setOnAction(e -> {
            boolean show = showPassword.isSelected();
            txtPassword.setVisible(!show);
            txtPassword.setManaged(!show);
            txtPasswordText.setVisible(show);
            txtPasswordText.setManaged(show);
        });

        Button btnForgot = new Button("Forgot Password?");
        btnForgot.getStyleClass().add("link-button");
        btnForgot.setOnAction(e -> mainApp.showForgotPasswordView());

        // Show password + forgot password on same row
        HBox passwordRow = new HBox(200);
        passwordRow.setAlignment(Pos.CENTER_LEFT);
        passwordRow.getChildren().addAll(showPassword, btnForgot);

        Button btnLogin = new Button("Get Started");
        btnLogin.getStyleClass().add("primary-button");
        btnLogin.setOnAction(e -> handleLogin());

        Button btnRegister = new Button("Sign up");
        btnRegister.getStyleClass().add("secondary-button");
        btnRegister.setOnAction(e -> mainApp.showRegisterView());

        lblMessage = new Label();
        lblMessage.getStyleClass().add("message-label");

        rightCard.getChildren().addAll(
                title,
                quote,
                user,
                txtUsername,
                pass,
                txtPassword,
                txtPasswordText,
                passwordRow,
                btnLogin,
                btnRegister,
                lblMessage
        );

        return rightCard;
    }

    private void handleLogin() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Username and password cannot be empty.");
            lblMessage.getStyleClass().setAll("message-error");
            return;
        }

        if (UserDAO.login(username, password)) {
            lblMessage.setText("Login successful!");
            lblMessage.getStyleClass().setAll("message-success");
            mainApp.showDashboardView(username);
        } else {
            lblMessage.setText("Invalid username or password.");
            lblMessage.getStyleClass().setAll("message-error");
        }
    }

    public Parent getView() {
        return root;
    }
}
