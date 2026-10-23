module ni.uam.edu.hilosjava {
    requires javafx.controls;
    requires javafx.fxml;


    opens ni.uam.edu.hilosjava to javafx.fxml;
    exports ni.uam.edu.hilosjava;
}