package ni.uam.edu.hilosjava.concurrencia;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class Controlador {

    @FXML private Label lblEstado;
    @FXML private Label lblContador;
    @FXML private Button btnIniciar;
    @FXML private Button btnDetener;
    @FXML private TextArea areaLogs;

    private final ControlDetencion controlDetencion = new ControlDetencion();
    private ContadorCompartido contadorCompartido;

    private Thread hiloVentanillaA;
    private Thread hiloVentanillaB;
    private Thread hiloMonitor;

    private volatile boolean sesionActiva = false;

    @FXML
    public void handleIniciar() {
        if (sesionActiva) return;

        sesionActiva = true;
        controlDetencion.reiniciar();
        contadorCompartido = new ContadorCompartido();

        actualizarEstado("Ejecutando");
        gestionarEstadoBotones(true, false);
        actualizarContador(0);
        agregarLog("=== INICIO DE SESIÓN DE VENTANILLAS ===");

        ProcesoVentanilla ventanillaA = new ProcesoVentanilla("Ventanilla A", contadorCompartido, controlDetencion, this::notificarUI);
        ProcesoVentanilla ventanillaB = new ProcesoVentanilla("Ventanilla B", contadorCompartido, controlDetencion, this::notificarUI);

        hiloVentanillaA = new Thread(ventanillaA, "Hilo-VentanillaA");
        hiloVentanillaB = new Thread(ventanillaB, "Hilo-VentanillaB");

        MonitorHilos monitor = new MonitorHilos(hiloVentanillaA, hiloVentanillaB, controlDetencion, this::notificarUI);
        hiloMonitor = new Thread(monitor, "Hilo-Monitor");

        Thread orquestador = new Thread(() -> {
            hiloVentanillaA.start();
            hiloVentanillaB.start();
            hiloMonitor.start();

            try {
                hiloVentanillaA.join();
                hiloVentanillaB.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Platform.runLater(() -> {
                if (controlDetencion.esDetencionSolicitada()) {
                    actualizarEstado("Detenido");
                    agregarLog(">>> Proceso detenido por el usuario.");
                } else {
                    actualizarEstado("Finalizado");
                    agregarLog("=== PROCESAMIENTO FINALIZADO EXITOSAMENTE ===");
                }
                sesionActiva = false;
                gestionarEstadoBotones(false, false);
            });
        }, "Hilo-Orquestador");

        orquestador.start();
    }

    @FXML
    public void handleDetener() {
        if (!sesionActiva || controlDetencion.esDetencionSolicitada()) return;

        actualizarEstado("Deteniendo");
        gestionarEstadoBotones(false, true);
        agregarLog(">>> Solicitando detención cooperativa de hilos...");

        controlDetencion.solicitarDetencion();

        if (hiloVentanillaA != null) hiloVentanillaA.interrupt();
        if (hiloVentanillaB != null) hiloVentanillaB.interrupt();
        if (hiloMonitor != null) hiloMonitor.interrupt();
    }

    private void notificarUI(String mensaje) {
        Platform.runLater(() -> {
            agregarLog(mensaje);
            if (contadorCompartido != null) {
                actualizarContador(contadorCompartido.getValor());
            }
        });
    }

    private void actualizarEstado(String estado) {
        lblEstado.setText(estado);
    }

    private void agregarLog(String mensaje) {
        areaLogs.appendText(mensaje + "\n");
    }

    private void actualizarContador(int valor) {
        lblContador.setText("Atenciones Totales: " + valor);
    }

    private void gestionarEstadoBotones(boolean ejecutando, boolean deteniendo) {
        if (ejecutando) {
            btnIniciar.setDisable(true);
            btnDetener.setDisable(false);
        } else if (deteniendo) {
            btnIniciar.setDisable(true);
            btnDetener.setDisable(true);
        } else {
            btnIniciar.setDisable(false);
            btnDetener.setDisable(true);
        }
    }
}