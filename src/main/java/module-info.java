module lk.ijse.chatappn {
    requires javafx.controls;
    requires javafx.fxml;


    opens lk.ijse.chatappn to javafx.fxml;
    exports lk.ijse.chatappn;
}