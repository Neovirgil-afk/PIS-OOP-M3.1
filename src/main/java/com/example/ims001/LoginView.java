package com.example.ims001;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class LoginView {

    private MainApp mainApp;
    private VBox root;

    private TextField txtUsername;
    private PasswordField txtPassword;
    private Label lblMessage;

    public LoginView(MainApp mainApp) {
        this.mainApp = mainApp;
        createView();
    }

    private void createView() {
        root = new VBox(10);
        root.setAlignment(Pos.CENTER);

        Label title = new Label("Login");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        txtUsername = new TextField();
        txtUsername.setPromptText("Username");
        txtUsername.setMaxWidth(250);

        txtPassword = new PasswordField();
        txtPassword.setPromptText("Password");
        txtPassword.setMaxWidth(250);

        lblMessage = new Label();

        Button btnLogin = new Button("Login");
        btnLogin.setOnAction(e -> handleLogin());

        Button btnRegister = new Button("Register");
        btnRegister.setOnAction(e -> mainApp.showRegister());

        root.getChildren().addAll(title, txtUsername, txtPassword, btnLogin, btnRegister, lblMessage);
    }

    private void handleLogin() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Username and password cannot be empty.");
            return;
        }

        if (UserDAO.login(username, password)) {
            lblMessage.setStyle("-fx-text-fill: green;");
            lblMessage.setText("Login successful!");
            mainApp.showDashboard();
        } else {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("Invalid username or password.");
        }
    }

    public Parent getView() {
        return root;
    }
}
