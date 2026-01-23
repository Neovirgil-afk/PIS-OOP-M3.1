module com.example.ims001 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;         // Required for JDBC/MySQL

    opens com.example.ims001 to javafx.fxml;
    exports com.example.ims001;
}
