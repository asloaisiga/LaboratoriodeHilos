
package ni.uam.edu.hilosjava;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import ni.uam.edu.hilosjava.concurrencia.Controlador;
import ni.uam.edu.hilosjava.monitoreo.MonitorHilosapp;

public class HilosApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // Estación 5: interfaz principal de ventanillas
        FXMLLoader loader = new FXMLLoader(
                HilosApplication.class.getResource("vista.fxml")
        );

        Parent vistaVentanillas = loader.load();
        Controlador controlador = loader.getController();

        // Estación 2: interfaz del monitor de hilos
        MonitorHilosapp vistaMonitor = new MonitorHilosapp();

        // Primera pestaña
        Tab pestanaVentanillas = new Tab(
                "Ventanillas",
                vistaVentanillas
        );

        // Segunda pestaña
        Tab pestanaMonitor = new Tab(
                "Estados de los hilos",
                vistaMonitor.crearContenido()
        );

        // Impedir cerrar las pestañas
        pestanaVentanillas.setClosable(false);
        pestanaMonitor.setClosable(false);

        // Contenedor de ambas pestañas
        TabPane pestanas = new TabPane(
                pestanaVentanillas,
                pestanaMonitor
        );

        // Configuración de la ventana
        stage.setTitle(
                "HilosJava - Laboratorio de Concurrencia"
        );

        stage.setScene(
                new Scene(pestanas, 1100, 760)
        );

        stage.setMinWidth(900);
        stage.setMinHeight(620);

        // Detener los procesos al cerrar
        stage.setOnCloseRequest(evento -> {
            controlador.cerrar();
            vistaMonitor.cerrarSesion();
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
