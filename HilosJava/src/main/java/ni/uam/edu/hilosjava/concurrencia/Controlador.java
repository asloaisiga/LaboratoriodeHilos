
package ni.uam.edu.hilosjava.concurrencia;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;

import ni.uam.edu.hilosjava.monitoreo.MonitorHilos;
import ni.uam.edu.hilosjava.Procesos.ProcesoVentanilla;
import ni.uam.edu.hilosjava.modelo.ContadorCompartido;

public class Controlador {

    @FXML private Label lblEstado;
    @FXML private Label lblContador;
    @FXML private Button btnIniciar;
    @FXML private Button btnDetener;
    @FXML private TextArea areaLogs;

    private ContadorCompartido contador;
    private ControlDetencion control;

    private Thread hiloA;
    private Thread hiloB;

    private MonitorHilos monitor;

    private boolean sesionActiva;

    @FXML
    private void initialize() {
        lblEstado.setText("En espera");
        lblContador.setText("Atenciones Totales: 0");
        btnDetener.setDisable(true);
    }

    @FXML
    public void handleIniciar() {

        if (sesionActiva) {
            return;
        }

        sesionActiva = true;

        contador = new ContadorCompartido();
        control = new ControlDetencion();

        areaLogs.clear();
        lblContador.setText("Atenciones Totales: 0");
        lblEstado.setText("Ejecutando");

        btnIniciar.setDisable(true);
        btnDetener.setDisable(false);

        hiloA = new Thread(
                new ProcesoVentanilla(
                        "Ventanilla A",
                        contador,
                        control,
                        this::notificarUI
                ),
                "Ventanilla-A"
        );

        hiloB = new Thread(
                new ProcesoVentanilla(
                        "Ventanilla B",
                        contador,
                        control,
                        this::notificarUI
                ),
                "Ventanilla-B"
        );

        monitor = new MonitorHilos(hiloA, hiloB);

        monitor.setObservador(muestras -> {

            StringBuilder estados =
                    new StringBuilder("Estados: ");

            for (MonitorHilos.Muestra muestra : muestras) {
                estados.append(muestra.nombre())
                        .append("=")
                        .append(muestra.estado())
                        .append(" ");
            }

            lblEstado.setTooltip(
                    new Tooltip(estados.toString())
            );
        });

        monitor.iniciar();

        hiloA.start();
        hiloB.start();

        Thread supervisor = new Thread(() -> {

            try {
                hiloA.join();
                hiloB.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            Platform.runLater(() -> {

                if (monitor != null) {
                    monitor.detener();
                }

                lblContador.setText(
                        "Atenciones Totales: "
                                + contador.getTotal()
                );

                if (control.esDetencionSolicitada()) {
                    lblEstado.setText("Detenido");
                } else {
                    lblEstado.setText("Finalizado");
                }

                agregarLog(
                        "Proceso "
                                + lblEstado.getText().toLowerCase()
                );

                sesionActiva = false;

                btnIniciar.setDisable(false);
                btnDetener.setDisable(true);
            });

        }, "Supervisor");

        supervisor.setDaemon(true);
        supervisor.start();
    }

    @FXML
    public void handleDetener() {

        if (!sesionActiva
                || control.esDetencionSolicitada()) {
            return;
        }

        lblEstado.setText("Deteniendo");
        btnDetener.setDisable(true);

        control.solicitarDetencion();

        hiloA.interrupt();
        hiloB.interrupt();

        agregarLog(
                "Se solicitó la cancelación cooperativa"
        );
    }

    private void notificarUI(String mensaje) {

        Platform.runLater(() -> {

            agregarLog(mensaje);

            lblContador.setText(
                    "Atenciones Totales: "
                            + contador.getTotal()
            );
        });
    }

    private void agregarLog(String mensaje) {
        areaLogs.appendText(mensaje + "\n");
    }

    public void cerrar() {

        if (control != null) {
            control.solicitarDetencion();
        }

        if (hiloA != null) {
            hiloA.interrupt();
        }

        if (hiloB != null) {
            hiloB.interrupt();
        }

        if (monitor != null) {
            monitor.detener();
        }
    }
}
