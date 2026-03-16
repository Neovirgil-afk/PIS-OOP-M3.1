package com.example.ims001;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public final class FxGlass {



    // Utility class lang ito, kaya bawal gumawa ng instance
    private FxGlass() {}

    /*
     * Binabalot ang kahit anong JavaFX Node sa loob ng isang StackPane
     * na may "glass-card" style (glass / frosted effect via CSS).
     *
     * content → ang actual UI component (form, VBox, GridPane, etc.)
     */



    public static StackPane glassCard(Node content) {

        // Gumawa ng StackPane at ilagay sa loob ang content
        StackPane p = new StackPane(content);

        // Lagyan ng CSS class para ang glass effect ay kontrolado sa stylesheet
        p.getStyleClass().add("glass-card");

        // IMPORTANT:
        // Hindi tayo naglalagay ng blur effect dito sa Java code,
        // dahil kapag na-blur ang container, pati text at icons ay lalabo.
        // Ang tamang glass effect ay ginagawa lang sa CSS background.
        p.setEffect(null);

        // I-align ang content sa top-left ng card
        StackPane.setAlignment(content, Pos.TOP_LEFT);
        p.setAlignment(Pos.TOP_LEFT);

        // Maglagay ng padding para hindi dumikit ang content sa gilid
        p.setPadding(new Insets(14));

        // Ibalik ang fully styled glass card
        return p;
    }
}