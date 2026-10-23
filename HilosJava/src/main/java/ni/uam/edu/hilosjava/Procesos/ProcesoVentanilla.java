package ni.uam.edu.hilosjava.Procesos;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static javafx.application.Application.launch;

public class ProcesoVentanilla implements Runnable {
    private final String nombre;

    public ProcesoVentanilla(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public void run() {
        for (int i = 1; i <= 5; i++) {

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            System.out.println(
                    nombre + " procesa " + i
                            + " | Hilo: "
                            + Thread.currentThread().getName()
            );

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("vista.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Laboratorio Concurrencia JavaFX - Estación 4 y 5");
        primaryStage.setScene(new Scene(root, 500, 500));
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
