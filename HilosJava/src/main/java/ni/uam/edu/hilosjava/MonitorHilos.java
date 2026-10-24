package ni.uam.edu.hilosjava;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;


public final class MonitorHilos {

    public static final Duration INTERVALO_POR_DEFECTO = Duration.millis(200);

    private final List<Thread> hilos;
    private final Duration intervalo;

    private Consumer<List<Muestra>> observador = muestras -> { };
    private Runnable alFinalizar = () -> { };

    private Timeline lineaTiempo;
    private boolean monitoreando;
    private boolean finalizado;
    private volatile List<Muestra> ultimaMuestra = List.of();

    /**
     * Instantánea del estado de un hilo.
     */
    public record Muestra(
            String nombre,
            long id,
            Thread.State estado,
            Instant instante) {

        public Muestra {
            Objects.requireNonNull(nombre, "El nombre es obligatorio");
            Objects.requireNonNull(estado, "El estado es obligatorio");
            Objects.requireNonNull(instante, "El instante es obligatorio");
        }

        public String estadoLegible() {
            return nombreEstado(estado);
        }

        public String descripcion() {
            return describir(estado);
        }
    }

    public MonitorHilos(Thread... hilos) {
        this(List.of(hilos), INTERVALO_POR_DEFECTO);
    }

    public MonitorHilos(Collection<? extends Thread> hilos) {
        this(hilos, INTERVALO_POR_DEFECTO);
    }

    public MonitorHilos(Collection<? extends Thread> hilos, Duration intervalo) {
        Objects.requireNonNull(hilos, "La colección de hilos es obligatoria");
        Objects.requireNonNull(intervalo, "El intervalo es obligatorio");

        if (hilos.isEmpty()) {
            throw new IllegalArgumentException("Debe haber al menos un hilo");
        }

        if (intervalo.isUnknown() || intervalo.lessThanOrEqualTo(Duration.ZERO)) {
            throw new IllegalArgumentException("El intervalo debe ser mayor que cero");
        }

        List<Thread> copia = new ArrayList<>(hilos.size());
        Set<Long> ids = new HashSet<>();

        for (Thread hilo : hilos) {
            Objects.requireNonNull(hilo, "Ningún hilo puede ser null");

            if (!ids.add(hilo.threadId())) {
                throw new IllegalArgumentException(
                        "El hilo '" + hilo.getName() + "' está repetido");
            }

            copia.add(hilo);
        }

        this.hilos = List.copyOf(copia);
        this.intervalo = intervalo;
    }

    /**
     * Recibe cada instantánea en el hilo de JavaFX.
     */
    public void setObservador(Consumer<List<Muestra>> observador) {
        this.observador = Objects.requireNonNull(observador);
    }

    /**
     * Se ejecuta una sola vez cuando todos los hilos están TERMINATED.
     */
    public void setAlFinalizar(Runnable alFinalizar) {
        this.alFinalizar = Objects.requireNonNull(alFinalizar);
    }

    /**
     * Inicia el muestreo periódico. Debe llamarse desde el hilo de JavaFX.
     */
    public void iniciar() {
        exigirHiloJavaFx();

        if (monitoreando) {
            return;
        }

        if (finalizado) {
            throw new IllegalStateException(
                    "El monitor ya finalizó; cree uno nuevo para otra sesión");
        }

        monitoreando = true;

        lineaTiempo = new Timeline(
                new KeyFrame(intervalo, evento -> actualizar()));

        lineaTiempo.setCycleCount(Animation.INDEFINITE);

        // Primera muestra inmediata para observar NEW antes del start().
        actualizar();

        if (monitoreando) {
            lineaTiempo.play();
        }
    }

    /**
     * Detiene el muestreo sin modificar los hilos.
     */
    public void detener() {
        exigirHiloJavaFx();
        detenerInternamente();
    }

    /**
     * Toma una instantánea en este momento. No bloquea.
     */
    public List<Muestra> tomarMuestra() {
        Instant ahora = Instant.now();

        List<Muestra> muestras = hilos.stream()
                .map(hilo -> new Muestra(
                        hilo.getName(),
                        hilo.threadId(),
                        hilo.getState(),
                        ahora))
                .toList();

        ultimaMuestra = muestras;
        return muestras;
    }

    public List<Muestra> ultimaMuestra() {
        return ultimaMuestra;
    }

    public boolean estaMonitoreando() {
        return monitoreando;
    }

    public boolean todosTerminados() {
        return hilos.stream()
                .allMatch(hilo -> hilo.getState() == Thread.State.TERMINATED);
    }

    public List<Thread> hilos() {
        return hilos;
    }

    public Duration intervalo() {
        return intervalo;
    }

    public static String nombreEstado(Thread.State estado) {
        Objects.requireNonNull(estado);

        return switch (estado) {
            case NEW -> "NUEVO";
            case RUNNABLE -> "EJECUTABLE";
            case BLOCKED -> "BLOQUEADO";
            case WAITING -> "EN ESPERA";
            case TIMED_WAITING -> "EN ESPERA CON TIEMPO";
            case TERMINATED -> "TERMINADO";
        };
    }

    public static String describir(Thread.State estado) {
        Objects.requireNonNull(estado);

        return switch (estado) {
            case NEW ->
                    "Creado, todavía no se ha llamado a start().";
            case RUNNABLE ->
                    "Se está ejecutando o está listo para usar la CPU.";
            case BLOCKED ->
                    "Espera entrar a un bloque synchronized retenido por otro hilo.";
            case WAITING ->
                    "Espera indefinidamente hasta que otro hilo lo despierte o lo interrumpa.";
            case TIMED_WAITING ->
                    "Espera con un tiempo máximo: sleep, join con tiempo o wait con tiempo.";
            case TERMINATED ->
                    "El método run() terminó y el hilo ya no se ejecutará.";
        };
    }

    private void actualizar() {
        List<Muestra> muestra = tomarMuestra();
        observador.accept(muestra);

        if (todosTerminados()) {
            detenerInternamente();

            if (!finalizado) {
                finalizado = true;
                alFinalizar.run();
            }
        }
    }

    private void detenerInternamente() {
        if (lineaTiempo != null) {
            lineaTiempo.stop();
            lineaTiempo = null;
        }

        monitoreando = false;
    }

    private static void exigirHiloJavaFx() {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException(
                    "El monitor debe iniciarse desde el hilo de JavaFX");
        }
    }
}