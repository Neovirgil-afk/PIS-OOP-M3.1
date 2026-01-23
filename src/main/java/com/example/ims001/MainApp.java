package com.example.ims001;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        stage.setTitle("Inventory Management System");
        showLogin();
        stage.show();
    }

    public void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/ims001/LoginView.fxml"));
            Parent root = loader.load();
            LoginController controller = loader.getController();
            controller.setMainApp(this);
            stage.setScene(new Scene(root, 400, 300));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showRegister() {
        RegisterView registerView = new RegisterView(this);
        stage.setScene(new Scene(registerView.getView(), 400, 300));
    }

    public void showDashboard() {
        DashboardView dashboardView = new DashboardView(this);
        stage.setScene(new Scene(dashboardView.getView(), 600, 400));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
