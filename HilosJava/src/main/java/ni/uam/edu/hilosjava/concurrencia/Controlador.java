package ni.uam.edu.hilosjava.concurrencia;

import javafx.application.Platform;

public class Controlador {

    public interface VistaUI {
        void actualizarEstado(String estado);
        void agregarLog(String mensaje);
        void actualizarContadorUI(int valor);
        void cambiarEstadoBotones(boolean ejecutando);
    }

    private final VistaUI vista;
    private final ControlDetencion controlDetencion;

    // Referencias a los componentes creados por los Integrantes 1, 2 y 3
    private ContadorCompartido contadorCompartido;
    private Thread hiloVentanillaA;
    private Thread hiloVentanillaB;
    private Thread hiloMonitor;

    private boolean sesionEnCurso = false;

    public Controlador(VistaUI vista) {
        this.vista = vista;
        this.controlDetencion = new ControlDetencion();
    }

    public synchronized void iniciarSesion() {
        // Bloquea el reinicio si hay una sesión activa en curso
        if (sesionEnCurso) {
            return;
        }

        sesionEnCurso = true;
        controlDetencion.reiniciar();

        // Instanciación del contador del Integrante 3
        this.contadorCompartido = new ContadorCompartido();

        vista.actualizarEstado("Ejecutando");
        vista.cambiarEstadoBotones(true);
        vista.actualizarContadorUI(0);
        vista.agregarLog("--- Inicio de procesamiento en ventanillas ---");

        // Instanciación de Runnables del Integrante 1
        ProcesoVentanilla ventanillaA = new ProcesoVentanilla("Ventanilla A", contadorCompartido, controlDetencion, this::notificarLog);
        ProcesoVentanilla ventanillaB = new ProcesoVentanilla("Ventanilla B", contadorCompartido, controlDetencion, this::notificarLog);

        hiloVentanillaA = new Thread(ventanillaA, "Hilo-VentanillaA");
        hiloVentanillaB = new Thread(ventanillaB, "Hilo-VentanillaB");

        // Instanciación del monitor del Integrante 2
        MonitorHilos monitor = new MonitorHilos(hiloVentanillaA, hiloVentanillaB, controlDetencion, this::notificarLog);
        hiloMonitor = new Thread(monitor, "Hilo-Monitor");

        // Hilo orquestador para no bloquear el JavaFX Application Thread durante la espera (join)
        Thread hiloOrquestador = new Thread(() -> {
            hiloVentanillaA.start();
            hiloVentanillaB.start();
            hiloMonitor.start();

            try {
                hiloVentanillaA.join();
                hiloVentanillaB.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Actualización del estado final en el hilo gráfico
            Platform.runLater(() -> {
                if (controlDetencion.esDetencionSolicitada()) {
                    vista.actualizarEstado("Detenido");
                    vista.agregarLog("--- Procesamiento detenido por el usuario ---");
                } else {
                    vista.actualizarEstado("Finalizado");
                    vista.agregarLog("--- Procesamiento finalizado exitosamente ---");
                }
                sesionEnCurso = false;
                vista.cambiarEstadoBotones(false);
            });
        }, "Hilo-Orquestador");

        hiloOrquestador.start();
    }

    public synchronized void detenerSesion() {
        if (!sesionEnCurso) {
            return;
        }

        vista.actualizarEstado("Deteniendo");
        vista.agregarLog(">>> Enviando señal de detención a los hilos...");

        // 1. Cancelación cooperativa con volatile
        controlDetencion.solicitarDetencion();

        // 2. Interrupción para sacar a los hilos de estados bloqueados (Thread.sleep)
        if (hiloVentanillaA != null) hiloVentanillaA.interrupt();
        if (hiloVentanillaB != null) hiloVentanillaB.interrupt();
        if (hiloMonitor != null) hiloMonitor.interrupt();
    }

    private void notificarLog(String mensaje) {
        // Envío coordinado de datos a la UI evitando saturación de eventos
        Platform.runLater(() -> {
            vista.agregarLog(mensaje);
            if (contadorCompartido != null) {
                vista.actualizarContadorUI(contadorCompartido.getValor());
            }
        });
    }
}