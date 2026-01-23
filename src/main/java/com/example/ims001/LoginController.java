package com.example.ims001;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    private MainApp mainApp;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblMessage;

    @FXML
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

    @FXML
    private void handleRegister() {
        if (mainApp != null) {
            mainApp.showRegister();
        }
    }
}
