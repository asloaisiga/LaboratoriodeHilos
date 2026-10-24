module ni.uam.edu.hilosjava {

    requires javafx.controls;
    requires javafx.fxml;

    exports ni.uam.edu.hilosjava;
    exports ni.uam.edu.hilosjava.app;
    exports ni.uam.edu.hilosjava.Procesos;

    opens ni.uam.edu.hilosjava.concurrencia
            to javafx.fxml;
    exports ni.uam.edu.hilosjava.monitoreo;
}
