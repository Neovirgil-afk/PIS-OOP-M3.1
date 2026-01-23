package com.example.ims001;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class RegisterView {

    private MainApp mainApp;
    private VBox root;

    private TextField txtUsername;
    private PasswordField txtPassword;
    private Label lblMessage;

    public RegisterView(MainApp mainApp) {
        this.mainApp = mainApp;
        createView();
    }

    private void createView() {
        root = new VBox(12);
        root.setAlignment(Pos.CENTER);

        Label title = new Label("Register New Account");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        txtUsername = new TextField();
        txtUsername.setPromptText("Username");
        txtUsername.setMaxWidth(250);

        txtPassword = new PasswordField();
        txtPassword.setPromptText("Password");
        txtPassword.setMaxWidth(250);

        lblMessage = new Label();

        Button btnRegister = new Button("Create Account");
        btnRegister.setOnAction(e -> handleRegister());

        Button btnBack = new Button("Back to Login");
        btnBack.setOnAction(e -> mainApp.showLogin());

        root.getChildren().addAll(title, txtUsername, txtPassword, btnRegister, btnBack, lblMessage);
    }

    private void handleRegister() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("Username and password cannot be empty.");
            return;
        }

        boolean success = UserDAO.register(username, password);
        if (success) {
            lblMessage.setStyle("-fx-text-fill: green;");
            lblMessage.setText("Registration successful! Redirecting to login...");
            mainApp.showLogin();
        } else {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("Registration failed. Username might already exist.");
        }
    }

    public Parent getView() {
        return root;
    }
}
