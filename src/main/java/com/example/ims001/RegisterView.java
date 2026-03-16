package com.example.ims001;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.security.SecureRandom;

public class RegisterView {

    private final MainApp mainApp;
    private final StackPane root = new StackPane();

    private TextField txtUsername;
    private PasswordField txtPassword, txtConfirm;
    private TextField txtPasswordText, txtConfirmText;
    private Label lblMessage;
    private CheckBox showPassword;

    public RegisterView(MainApp mainApp) {
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

        HBox shell = new HBox(18);
        shell.setAlignment(Pos.CENTER);

        StackPane left = FxGlass.glassCard(authLeftBlock());
        left.getStyleClass().add("auth-left");
        left.setPrefWidth(520);

        StackPane right = FxGlass.glassCard(authRegisterBlock());
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

    private VBox authRegisterBlock() {

        Label title = new Label("Register");
        title.getStyleClass().add("auth-title");

        Label subtitle = new Label("Create your account to manage inventory efficiently.");
        subtitle.getStyleClass().add("auth-sub");

        Label userLbl = new Label("Username");
        userLbl.getStyleClass().add("field-label");

        txtUsername = new TextField();
        txtUsername.setPromptText("Enter username");
        txtUsername.getStyleClass().add("ctl");

        Label passLbl = new Label("Password");
        passLbl.getStyleClass().add("field-label");

        txtPassword = new PasswordField();
        txtPassword.setPromptText("Enter password");
        txtPassword.getStyleClass().add("ctl");

        txtPasswordText = new TextField();
        txtPasswordText.setPromptText("Enter password");
        txtPasswordText.getStyleClass().add("ctl");
        txtPasswordText.setVisible(false);
        txtPasswordText.setManaged(false);

        txtPassword.textProperty().bindBidirectional(txtPasswordText.textProperty());

        Label confirmLbl = new Label("Confirm Password");
        confirmLbl.getStyleClass().add("field-label");

        txtConfirm = new PasswordField();
        txtConfirm.setPromptText("Confirm password");
        txtConfirm.getStyleClass().add("ctl");

        txtConfirmText = new TextField();
        txtConfirmText.setPromptText("Confirm password");
        txtConfirmText.getStyleClass().add("ctl");
        txtConfirmText.setVisible(false);
        txtConfirmText.setManaged(false);

        txtConfirm.textProperty().bindBidirectional(txtConfirmText.textProperty());

        showPassword = new CheckBox("Show Password");
        showPassword.getStyleClass().add("chk");

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

        Button btnRegister = new Button("Create Account");
        btnRegister.getStyleClass().addAll("btn","btn-primary");
        btnRegister.setOnAction(e -> handleRegister());

        Button btnBack = new Button("Back to Login");
        btnBack.getStyleClass().addAll("btn","btn-ghost");
        btnBack.setOnAction(e -> mainApp.showLoginView());

        lblMessage = new Label();
        lblMessage.getStyleClass().add("msg");

        VBox box = new VBox(
                12,
                title, subtitle,
                userLbl, txtUsername,
                passLbl, txtPassword, txtPasswordText,
                confirmLbl, txtConfirm, txtConfirmText,
                showPassword,
                btnRegister, btnBack,
                lblMessage
        );



        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(26));

        return box;
    }

    private void handleRegister() {

        String username = txtUsername.getText() == null ? "" : txtUsername.getText().trim();
        String password = showPassword.isSelected() ? txtPasswordText.getText() : txtPassword.getText();
        String confirm = showPassword.isSelected() ? txtConfirmText.getText() : txtConfirm.getText();

        String resetCode = generateResetCode();

        if(username.isBlank() || password.isBlank() || confirm.isBlank()){
            setMessage("All fields are required.", true);
            return;
        }

        if(!password.equals(confirm)){
            setMessage("Passwords do not match.", true);
            return;
        }

        if(!isStrongPassword(password)){
            setMessage("Password must be strong.", true);
            return;
        }

        boolean success = UserDAO.register(username,password,resetCode);

        if(success){
            setMessage("Registered! Reset code: "+resetCode,false);
        }else{
            setMessage("Username already exists.",true);
        }
    }

    private void setMessage(String message, boolean error){
        lblMessage.setText(message);
        lblMessage.getStyleClass().removeAll("msg-err","msg-ok");
        lblMessage.getStyleClass().add(error ? "msg-err":"msg-ok");
    }

    private boolean isStrongPassword(String password){
        return password.length()>=8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*") &&
                password.matches(".*[!@#$%^&*()].*");
    }

    private String generateResetCode(){

        SecureRandom rand = new SecureRandom();
        String chars="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb=new StringBuilder();

        for(int i=0;i<12;i++){
            sb.append(chars.charAt(rand.nextInt(chars.length())));
        }

        return sb.toString();
    }

    public Parent getView(){
        return root;
    }
}