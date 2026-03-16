package com.example.ims001;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

public class LoginView {

    private final MainApp mainApp;
    private final StackPane root = new StackPane();

    private TextField txtUsername;
    private PasswordField txtPassword;
    private TextField txtPasswordText;
    private CheckBox showPassword;
    private Label lblMessage;

    public LoginView(MainApp mainApp) {
        this.mainApp = mainApp;

        root.getStyleClass().add("auth-root");

        Image bg = new Image(getClass().getResource("/images/blackbg.jpg").toExternalForm());
        ImageView bgView = new ImageView(bg);
        bgView.setFitWidth(1920);
        bgView.setFitHeight(1080);
        bgView.setPreserveRatio(false);
        bgView.setOpacity(0.35);

        BorderPane overlay = new BorderPane();
        overlay.setPadding(new Insets(24));

        Button themeBtn = new Button(ThemeManager.isDarkMode() ? "☀" : "🌙");
        themeBtn.getStyleClass().addAll("btn", "btn-ghost");
        themeBtn.setOnAction(e -> {
            if (root.getScene() != null) {
                ThemeManager.toggle(root.getScene());
                themeBtn.setText(ThemeManager.isDarkMode() ? "☀" : "🌙");
            }
        });

        HBox topRight = new HBox(themeBtn);
        topRight.setAlignment(Pos.TOP_RIGHT);
        overlay.setTop(topRight);

        HBox shell = new HBox(18);
        shell.setAlignment(Pos.CENTER);

        StackPane left = FxGlass.glassCard(authLeftBlock());
        left.getStyleClass().add("auth-left");
        left.setPrefWidth(520);

        StackPane right = FxGlass.glassCard(authLoginBlock());
        right.getStyleClass().add("auth-right");
        right.setPrefWidth(520);

        shell.getChildren().addAll(left, right);
        overlay.setCenter(shell);

        root.getChildren().addAll(bgView, overlay);
    }

    private VBox authLeftBlock() {
        ImageView logoView = null;
        try {
            Image logo = new Image(getClass().getResource("/images/malopitang logo .png").toExternalForm());
            logoView = new ImageView(logo);
            logoView.setFitWidth(280);
            logoView.setFitHeight(280);
            logoView.setPreserveRatio(true);
            logoView.setSmooth(true);
        } catch (Exception e) {
            System.out.println("Logo image not found: /images/malopitang logo .png");
        }

        Label brand = new Label("Prestige\nInventory Suites");
        brand.getStyleClass().add("auth-brand");

        Label sub = new Label("Welcome to Inventory Management System");
        sub.getStyleClass().add("auth-sub");

        VBox box;
        if (logoView != null) {
            box = new VBox(18, logoView, brand, sub);
        } else {
            box = new VBox(10, brand, sub);
        }

        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(26));
        return box;
    }

    private VBox authLoginBlock() {
        Label title = new Label("Sign In");
        title.getStyleClass().add("auth-title");

        Label subtitle = new Label("Enter your credentials to continue.");
        subtitle.getStyleClass().add("auth-sub");

        Label uLbl = new Label("Username");
        uLbl.getStyleClass().add("field-label");

        txtUsername = new TextField();
        txtUsername.setPromptText("Enter username");
        txtUsername.getStyleClass().add("ctl");

        Label pLbl = new Label("Password");
        pLbl.getStyleClass().add("field-label");

        txtPassword = new PasswordField();
        txtPassword.setPromptText("Enter password");
        txtPassword.getStyleClass().add("ctl");

        txtPasswordText = new TextField();
        txtPasswordText.setPromptText("Enter password");
        txtPasswordText.getStyleClass().add("ctl");
        txtPasswordText.setVisible(false);
        txtPasswordText.setManaged(false);

        txtPassword.textProperty().bindBidirectional(txtPasswordText.textProperty());

        showPassword = new CheckBox("Show Password");
        showPassword.getStyleClass().add("chk");
        showPassword.setOnAction(e -> {
            boolean show = showPassword.isSelected();
            txtPassword.setVisible(!show);
            txtPassword.setManaged(!show);
            txtPasswordText.setVisible(show);
            txtPasswordText.setManaged(show);
        });

        Hyperlink forgot = new Hyperlink("Forgot password?");
        forgot.getStyleClass().add("link");
        forgot.setOnAction(e -> mainApp.showForgotPasswordView());

        Button btnLogin = new Button("Sign In");
        btnLogin.getStyleClass().addAll("btn", "btn-primary", "auth-btn");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setOnAction(e -> handleLogin());

        Button btnRegister = new Button("Create Account");
        btnRegister.getStyleClass().addAll("btn", "btn-ghost", "auth-btn");
        btnRegister.setMaxWidth(Double.MAX_VALUE);
        btnRegister.setOnAction(e -> mainApp.showRegisterView());

        lblMessage = new Label();
        lblMessage.getStyleClass().add("msg");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(10, showPassword, spacer, forgot);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(
                12,
                title, subtitle,
                uLbl, txtUsername,
                pLbl, txtPassword, txtPasswordText,
                row,
                btnLogin, btnRegister,
                lblMessage
        );

        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(26));
        return box;
    }

    private void handleLogin() {
        String u = txtUsername.getText() == null ? "" : txtUsername.getText().trim();
        String p = showPassword.isSelected()
                ? (txtPasswordText.getText() == null ? "" : txtPasswordText.getText())
                : (txtPassword.getText() == null ? "" : txtPassword.getText());

        if (u.isBlank() || p.isBlank()) {
            setMsg("All fields are required.", true);
            return;
        }

        boolean ok = UserDAO.login(u, p);
        if (ok) {
            Session.setUsername(u);
            mainApp.showDashboardView(u);
        } else {
            setMsg("Invalid username or password.", true);
        }
    }

    private void setMsg(String msg, boolean err) {
        lblMessage.setText(msg);
        lblMessage.getStyleClass().removeAll("msg-err", "msg-ok");
        lblMessage.getStyleClass().add(err ? "msg-err" : "msg-ok");
    }

    public Parent getView() {
        return root;
    }
}