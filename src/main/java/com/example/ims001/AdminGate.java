package com.example.ims001;

import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

import java.util.Optional;

public class AdminGate {

    // 🔐 Change this anytime (Manager password)
    private static final String ADMIN_PASSWORD = "Admin123#";

    private AdminGate() {}

    public static boolean verify() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Manager Access");
        dialog.setHeaderText("Enter admin/manager password to continue.");

        ButtonType btnOk = new ButtonType("Continue", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);

        PasswordField pf = new PasswordField();
        pf.setPromptText("Manager password");

        dialog.getDialogPane().setContent(pf);

        // Disable OK until user types something
        dialog.getDialogPane().lookupButton(btnOk).setDisable(true);
        pf.textProperty().addListener((obs, oldV, newV) ->
                dialog.getDialogPane().lookupButton(btnOk).setDisable(newV == null || newV.isBlank())
        );

        dialog.setResultConverter(button -> {
            if (button == btnOk) return pf.getText();
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return false;

        boolean ok = ADMIN_PASSWORD.equals(result.get());

        if (!ok) {
            Alert a = new Alert(AlertType.ERROR);
            a.setTitle("Access Denied");
            a.setHeaderText("Wrong password");
            a.setContentText("Manager password is incorrect.");
            a.showAndWait();
        }

        return ok;
    }
}
