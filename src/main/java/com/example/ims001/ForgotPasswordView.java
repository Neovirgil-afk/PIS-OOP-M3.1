package com.example.ims001;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

public class ForgotPasswordView {

    private final MainApp mainApp;
    private final StackPane root = new StackPane();

    private TextField txtUsername;
    private TextField txtResetCode;
    private TextField txtNewPasswordText;
    private PasswordField txtNewPassword;
    private CheckBox showPassword;
    private Label lblMessage;

    public ForgotPasswordView(MainApp mainApp) {

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

        StackPane right = FxGlass.glassCard(authForgotBlock());
        right.getStyleClass().add("auth-right");
        right.setPrefWidth(520);

        shell.getChildren().addAll(left,right);
        overlay.setCenter(shell);

        root.getChildren().addAll(bgView,overlay);
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
            System.out.println("Logo image not found: /images/logo.png");
        }

        Label brand = new Label("Prestige\nInventory Suites");
        brand.getStyleClass().add("auth-brand");

        Label sub = new Label("Reset your password and get back into your account.");
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

    private VBox authForgotBlock(){

        Label title = new Label("Reset Password");
        title.getStyleClass().add("auth-title");

        Label subtitle = new Label("Use your reset code to create a new password.");
        subtitle.getStyleClass().add("auth-sub");

        Label uLbl = new Label("Username");
        uLbl.getStyleClass().add("field-label");

        txtUsername = new TextField();
        txtUsername.setPromptText("Enter username");
        txtUsername.getStyleClass().add("ctl");

        Label codeLbl = new Label("Reset Code");
        codeLbl.getStyleClass().add("field-label");

        txtResetCode = new TextField();
        txtResetCode.setPromptText("Enter reset code");
        txtResetCode.getStyleClass().add("ctl");

        Label passLbl = new Label("New Password");
        passLbl.getStyleClass().add("field-label");

        txtNewPassword = new PasswordField();
        txtNewPassword.setPromptText("Enter new password");
        txtNewPassword.getStyleClass().add("ctl");

        txtNewPasswordText = new TextField();
        txtNewPasswordText.setPromptText("Enter new password");
        txtNewPasswordText.getStyleClass().add("ctl");
        txtNewPasswordText.setVisible(false);
        txtNewPasswordText.setManaged(false);

        txtNewPassword.textProperty().bindBidirectional(txtNewPasswordText.textProperty());

        showPassword = new CheckBox("Show Password");
        showPassword.getStyleClass().add("chk");

        showPassword.setOnAction(e->{

            boolean show = showPassword.isSelected();

            txtNewPassword.setVisible(!show);
            txtNewPassword.setManaged(!show);

            txtNewPasswordText.setVisible(show);
            txtNewPasswordText.setManaged(show);

        });

        Button btnReset = new Button("Reset Password");
        btnReset.getStyleClass().addAll("btn","btn-primary");
        btnReset.setOnAction(e->handleReset());

        Button btnBack = new Button("Back to Login");
        btnBack.getStyleClass().addAll("btn","btn-ghost");
        btnBack.setOnAction(e->mainApp.showLoginView());

        lblMessage = new Label();
        lblMessage.getStyleClass().add("msg");

        VBox box = new VBox(
                12,
                title,subtitle,
                uLbl,txtUsername,
                codeLbl,txtResetCode,
                passLbl,txtNewPassword,txtNewPasswordText,
                showPassword,
                btnReset,btnBack,
                lblMessage
        );

        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(26));

        return box;
    }

    private void handleReset(){

        String username = txtUsername.getText();
        String resetCode = txtResetCode.getText();
        String newPassword = showPassword.isSelected() ? txtNewPasswordText.getText() : txtNewPassword.getText();

        if(username.isBlank()||resetCode.isBlank()||newPassword.isBlank()){
            setMessage("All fields are required.",true);
            return;
        }

        if(!UserDAO.checkResetCode(username,resetCode)){
            setMessage("Invalid reset code.",true);
            return;
        }

        if(UserDAO.updatePassword(username,newPassword)){
            setMessage("Password reset successful!",false);
        }else{
            setMessage("Password reset failed.",true);
        }
    }

    private void setMessage(String msg, boolean err){

        lblMessage.setText(msg);
        lblMessage.getStyleClass().removeAll("msg-err","msg-ok");
        lblMessage.getStyleClass().add(err ? "msg-err":"msg-ok");
    }

    public Parent getView(){
        return root;
    }
}