package com.example.ims001;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class AppShell {

    private final MainApp mainApp;
    private final String username;

    private final BorderPane root = new BorderPane();
    private final BorderPane shell = new BorderPane();

    private final VBox sidebar = new VBox();
    private final HBox topBar = new HBox();

    private final StackPane contentHolder = new StackPane();

    public AppShell(MainApp mainApp, String username, String activeNav, String pageTitle, Node contentNode) {
        this.mainApp = mainApp;
        this.username = username;

        build(activeNav, pageTitle, contentNode);
        applyThemeClass();
    }

    private void build(String activeNav, String pageTitle, Node contentNode) {
        root.getStyleClass().add("app-root");
        shell.getStyleClass().add("app-shell");
        sidebar.getStyleClass().add("sidebar");
        topBar.getStyleClass().add("topbar");
        contentHolder.getStyleClass().add("content-holder");

        buildSidebar(activeNav);
        buildTopBar(pageTitle);

        contentHolder.getChildren().add(contentNode);
        StackPane.setAlignment(contentNode, Pos.TOP_LEFT);

        shell.setLeft(sidebar);
        shell.setTop(topBar);
        shell.setCenter(contentHolder);

        StackPane wrapper = new StackPane(shell);
        wrapper.setPadding(new Insets(18));

        root.setCenter(wrapper);
    }

    private void buildSidebar(String activeNav) {
        Label logo = new Label("IMS");
        logo.getStyleClass().add("logo-label");

        Label sub = new Label("Inventory System");
        sub.getStyleClass().add("logo-sub");

        VBox brand = new VBox(0, logo, sub);
        brand.getStyleClass().add("brand-box");

        Button btnHome = createNavButton("Home", "home".equalsIgnoreCase(activeNav));
        btnHome.setOnAction(e -> mainApp.showDashboardView());

        Button btnInventory = createNavButton("Inventory", "inventory".equalsIgnoreCase(activeNav));
        btnInventory.setOnAction(e -> mainApp.showInventoryView());

        Button btnHistory = createNavButton("History", "history".equalsIgnoreCase(activeNav));
        btnHistory.setOnAction(e -> mainApp.showHistoryView());

        VBox navBox = new VBox(10, btnHome, btnInventory, btnHistory);
        navBox.getStyleClass().add("nav-box");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(brand, navBox, spacer);
    }

    private void buildTopBar(String pageTitleText) {
        Label pageTitle = new Label(pageTitleText);
        pageTitle.getStyleClass().add("page-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnTheme = new Button(ThemeManager.isDarkMode() ? "Light Mode" : "Dark Mode");
        btnTheme.getStyleClass().add("topbar-btn");
        btnTheme.setOnAction(e -> {
            ThemeManager.toggle(root.getScene());
            btnTheme.setText(ThemeManager.isDarkMode() ? "Light Mode" : "Dark Mode");
            applyThemeClass();
        });

        Button btnProfile = new Button(username);
        btnProfile.getStyleClass().add("topbar-btn");
        btnProfile.setOnAction(e -> mainApp.showProfileView());

        Button btnLogout = new Button("Log Out");
        btnLogout.getStyleClass().add("topbar-btn");
        btnLogout.setOnAction(e -> mainApp.showLoginView());

        HBox right = new HBox(10, btnTheme, btnProfile, btnLogout);
        right.setAlignment(Pos.CENTER_RIGHT);

        topBar.getChildren().addAll(pageTitle, spacer, right);
        topBar.setAlignment(Pos.CENTER_LEFT);
    }

    private Button createNavButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-btn");
        if (active) {
            btn.getStyleClass().add("nav-btn-active");
        }
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        return btn;
    }

    private void applyThemeClass() {
        root.getStyleClass().removeAll("light-view", "dark-view");
        root.getStyleClass().add(ThemeManager.isDarkMode() ? "dark-view" : "light-view");
    }

    public BorderPane getRoot() {
        return root;
    }
}