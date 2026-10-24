package ni.uam.edu.hilosjava;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Demostración de la Estación 2.
 *
 * Muestra:
 * - NEW antes de iniciar los hilos.
 * - Estados activos como RUNNABLE, BLOCKED y TIMED_WAITING.
 * - TERMINATED al finalizar.
 * - Un experimento aislado de TIMED_WAITING con Thread.sleep(3000).
 *
 * La interfaz nunca llama a sleep() ni a join(). El monitor usa una Timeline
 * y los hilos de trabajo publican sus mensajes con Platform.runLater().
 */
public final class MonitorHilosapp extends Application {

    private static final DateTimeFormatter HORA =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final TableView<MonitorHilos.Muestra> tabla = new TableView<>();
    private final ObservableList<MonitorHilos.Muestra> filas =
            FXCollections.observableArrayList();
    private final TextArea bitacora = new TextArea();
    private final Label etiquetaSesion = new Label("En espera");
    private final Label detalleSesion = new Label("Presione un botón para comenzar.");

    private final Button botonDemo =
            new Button("Iniciar demostración de estados");
    private final Button botonTimedWaiting =
            new Button("Experimento TIMED_WAITING");
    private final Button botonLimpiar =
            new Button("Limpiar bitácora");

    private final Map<Long, Thread.State> estadosAnteriores = new HashMap<>();

    private MonitorHilos monitor;
    private Timeline lanzamiento;
    private boolean sesionActiva;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Estación 2 · Monitoreo de estados de hilos");
        stage.setScene(new Scene(construirVista(), 1050, 700));
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setOnCloseRequest(evento -> cerrarSesion());
        stage.show();

        // Permite iniciar la demostración automáticamente para tomar evidencias.
        if (getParameters().getRaw().contains("--auto")) {
            PauseTransition pausa = new PauseTransition(Duration.millis(600));
            pausa.setOnFinished(evento -> iniciarDemoEstados());
            pausa.play();
        }
    }

    private VBox construirVista() {
        Label titulo = new Label("Estación 2 · Estados de los hilos");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label subtitulo = new Label(
                "getState() se consulta cada 200 ms con una Timeline. "
                        + "La interfaz de JavaFX nunca espera a los hilos.");
        subtitulo.setWrapText(true);

        FlowPane leyenda = new FlowPane(8, 8);
        for (Thread.State estado : Thread.State.values()) {
            Label chip = new Label(MonitorHilos.nombreEstado(estado));
            chip.setStyle(
                    "-fx-padding: 4 10; "
                            + "-fx-background-color: #e2e8f0; "
                            + "-fx-background-radius: 999; "
                            + "-fx-font-size: 11px; "
                            + "-fx-font-weight: bold;");
            chip.setTooltip(new Tooltip(MonitorHilos.describir(estado)));
            leyenda.getChildren().add(chip);
        }

        etiquetaSesion.setStyle(
                "-fx-font-weight: bold; "
                        + "-fx-padding: 5 10; "
                        + "-fx-background-color: #e2e8f0; "
                        + "-fx-background-radius: 6;");

        HBox barraEstado = new HBox(12, etiquetaSesion, detalleSesion);
        barraEstado.setAlignment(Pos.CENTER_LEFT);

        botonDemo.setStyle("-fx-font-weight: bold;");
        botonDemo.setTooltip(new Tooltip(
                "Dos hilos: NEW, TIMED_WAITING, BLOCKED, RUNNABLE y TERMINATED"));
        botonTimedWaiting.setTooltip(new Tooltip(
                "Un hilo que pasa 3 segundos dentro de Thread.sleep(3000)"));

        botonDemo.setOnAction(evento -> iniciarDemoEstados());
        botonTimedWaiting.setOnAction(evento -> iniciarExperimentoTimedWaiting());
        botonLimpiar.setOnAction(evento -> bitacora.clear());

        HBox botones = new HBox(10, botonDemo, botonTimedWaiting, botonLimpiar);

        VBox encabezado = new VBox(10, titulo, subtitulo, leyenda, barraEstado, botones);
        encabezado.setPadding(new Insets(20, 20, 10, 20));

        configurarTabla();

        Label etiquetaTabla = new Label("Instantáneas de getState()");
        etiquetaTabla.setStyle("-fx-font-weight: bold;");

        Label etiquetaBitacora = new Label("Bitácora de transiciones");
        etiquetaBitacora.setStyle("-fx-font-weight: bold;");

        bitacora.setEditable(false);
        bitacora.setWrapText(false);
        bitacora.setPrefHeight(220);
        bitacora.setPromptText(
                "Aquí aparecerán las transiciones observadas y los hitos de los hilos...");

        VBox raiz = new VBox(10,
                encabezado,
                etiquetaTabla,
                tabla,
                etiquetaBitacora,
                bitacora);

        raiz.setPadding(new Insets(0, 20, 20, 20));
        raiz.setStyle("-fx-background-color: #f8fafc;");
        VBox.setVgrow(tabla, Priority.ALWAYS);

        return raiz;
    }

    private void configurarTabla() {
        tabla.setItems(filas);
        tabla.setPlaceholder(
                new Label("Todavía no hay hilos. Inicie una demostración."));
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<MonitorHilos.Muestra, String> columnaNombre =
                new TableColumn<>("Hilo");
        columnaNombre.setCellValueFactory(dato ->
                new ReadOnlyStringWrapper(dato.getValue().nombre()));
        columnaNombre.setMinWidth(150);

        TableColumn<MonitorHilos.Muestra, String> columnaId =
                new TableColumn<>("ID");
        columnaId.setCellValueFactory(dato ->
                new ReadOnlyStringWrapper(Long.toString(dato.getValue().id())));
        columnaId.setMaxWidth(80);

        TableColumn<MonitorHilos.Muestra, Thread.State> columnaEstado =
                new TableColumn<>("Estado");
        columnaEstado.setCellValueFactory(dato ->
                new ReadOnlyObjectWrapper<>(dato.getValue().estado()));
        columnaEstado.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(Thread.State estado, boolean vacia) {
                super.updateItem(estado, vacia);

                if (vacia || estado == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(MonitorHilos.nombreEstado(estado));
                setStyle("-fx-font-weight: bold; -fx-text-fill: "
                        + colorEstado(estado) + ";");
            }
        });
        columnaEstado.setMinWidth(190);

        TableColumn<MonitorHilos.Muestra, String> columnaDescripcion =
                new TableColumn<>("Descripción");
        columnaDescripcion.setCellValueFactory(dato ->
                new ReadOnlyStringWrapper(dato.getValue().descripcion()));
        columnaDescripcion.setMinWidth(330);

        TableColumn<MonitorHilos.Muestra, String> columnaHora =
                new TableColumn<>("Hora");
        columnaHora.setCellValueFactory(dato ->
                new ReadOnlyStringWrapper(formatearHora(dato.getValue().instante())));
        columnaHora.setMaxWidth(120);

        tabla.getColumns().addAll(
                columnaNombre,
                columnaId,
                columnaEstado,
                columnaDescripcion,
                columnaHora);
    }

    private void iniciarDemoEstados() {
        if (sesionActiva) {
            return;
        }

        prepararSesion(
                "Los hilos se crearon en NEW; start() se ejecutará en un momento.");

        Object candado = new Object();

        Thread hiloA = new Thread(
                () -> ejecutarVentanillaA(candado),
                "Ventanilla-A");

        Thread hiloB = new Thread(
                () -> ejecutarVentanillaB(candado),
                "Ventanilla-B");

        hiloA.setDaemon(true);
        hiloB.setDaemon(true);

        iniciarMonitor(List.of(hiloA, hiloB));
        registrar("Monitor: hilos creados. Ambos deben observarse primero en NEW.");

        lanzamiento = new Timeline(
                new KeyFrame(Duration.seconds(1.0), evento -> {
                    registrar("Controlador: hiloA.start()");
                    hiloA.start();
                }),
                new KeyFrame(Duration.seconds(1.8), evento -> {
                    registrar("Controlador: hiloB.start()");
                    hiloB.start();
                }));

        lanzamiento.play();
    }

    private void iniciarExperimentoTimedWaiting() {
        if (sesionActiva) {
            return;
        }

        prepararSesion(
                "Experimento: el hilo se creó en NEW y dormirá 3 s al iniciar.");

        Thread hilo = new Thread(() -> {
            registrarHilo("inicia y llama a Thread.sleep(3000)");
            dormir(3000);
            registrarHilo("despertó del sleep y termina");
        }, "Hilo-TIMED_WAITING");

        hilo.setDaemon(true);

        iniciarMonitor(List.of(hilo));
        registrar("Monitor: el hilo está en NEW; se iniciará en 1 segundo.");

        lanzamiento = new Timeline(new KeyFrame(Duration.seconds(1.0), evento -> {
            registrar("Controlador: hilo.start()");
            hilo.start();
        }));

        lanzamiento.play();
    }

    private void ejecutarVentanillaA(Object candado) {
        registrarHilo("inicia y solicita el candado");

        synchronized (candado) {
            registrarHilo("obtuvo el candado; dormirá 2500 ms (TIMED_WAITING)");
            dormir(2500);
            registrarHilo("despertó y liberará el candado");
        }

        registrarHilo("procesa durante 700 ms (RUNNABLE)");
        trabajar(700);
        registrarHilo("termina");
    }

    private void ejecutarVentanillaB(Object candado) {
        registrarHilo(
                "inicia e intenta entrar al candado (puede quedar BLOCKED)");

        synchronized (candado) {
            registrarHilo("obtuvo el candado; dormirá 1200 ms (TIMED_WAITING)");
            dormir(1200);
        }

        registrarHilo("procesa durante 700 ms (RUNNABLE)");
        trabajar(700);
        registrarHilo("termina");
    }

    private void iniciarMonitor(List<Thread> hilos) {
        monitor = new MonitorHilos(hilos);
        monitor.setObservador(this::actualizarMonitor);
        monitor.setAlFinalizar(this::finalizarSesion);
        monitor.iniciar();
    }

    private void actualizarMonitor(List<MonitorHilos.Muestra> muestras) {
        filas.setAll(muestras);

        for (MonitorHilos.Muestra muestra : muestras) {
            Thread.State anterior =
                    estadosAnteriores.put(muestra.id(), muestra.estado());

            if (anterior == null) {
                registrar("Monitor: " + muestra.nombre() + " aparece en "
                        + MonitorHilos.nombreEstado(muestra.estado()) + ".");
            } else if (anterior != muestra.estado()) {
                registrar("Monitor: " + muestra.nombre() + " pasó de "
                        + MonitorHilos.nombreEstado(anterior) + " a "
                        + MonitorHilos.nombreEstado(muestra.estado()) + ".");
            }
        }

        if (!sesionActiva) {
            return;
        }

        long nuevos = muestras.stream()
                .filter(muestra -> muestra.estado() == Thread.State.NEW)
                .count();

        long terminados = muestras.stream()
                .filter(muestra -> muestra.estado() == Thread.State.TERMINATED)
                .count();

        if (terminados == muestras.size()) {
            cambiarSesion("Finalizado", "#dcfce7");
        } else if (nuevos == muestras.size()) {
            cambiarSesion("En espera", "#e2e8f0");
        } else {
            cambiarSesion("Ejecutando", "#dbeafe");
        }

        detalleSesion.setText("NUEVO: " + nuevos
                + " · TERMINATED: " + terminados
                + " de " + muestras.size() + " hilos");
    }

    private void finalizarSesion() {
        sesionActiva = false;

        cambiarSesion("Finalizado", "#dcfce7");
        detalleSesion.setText(
                "Todos los hilos están en TERMINATED. El monitoreo se detuvo solo.");

        botonDemo.setDisable(false);
        botonTimedWaiting.setDisable(false);
        botonLimpiar.setDisable(false);

        registrar("Monitor: todos los hilos terminaron. "
                + "Timeline detenida sin bloquear JavaFX.");
    }

    private void prepararSesion(String detalle) {
        sesionActiva = true;
        estadosAnteriores.clear();
        filas.clear();
        bitacora.clear();

        cambiarSesion("En espera", "#e2e8f0");
        detalleSesion.setText(detalle);

        botonDemo.setDisable(true);
        botonTimedWaiting.setDisable(true);
        botonLimpiar.setDisable(true);

        if (lanzamiento != null) {
            lanzamiento.stop();
            lanzamiento = null;
        }

        if (monitor != null && monitor.estaMonitoreando()) {
            monitor.detener();
        }
    }

    private void cambiarSesion(String texto, String color) {
        etiquetaSesion.setText(texto);
        etiquetaSesion.setStyle(
                "-fx-font-weight: bold; "
                        + "-fx-padding: 5 10; "
                        + "-fx-background-color: " + color + "; "
                        + "-fx-background-radius: 6;");
    }

    private void registrarHilo(String mensaje) {
        String nombre = Thread.currentThread().getName();
        Platform.runLater(() -> registrar(nombre + ": " + mensaje));
    }

    /** Se ejecuta en el hilo de JavaFX. */
    private void registrar(String mensaje) {
        bitacora.appendText("[" + LocalTime.now().format(HORA) + "] "
                + mensaje + System.lineSeparator());
    }

    private void cerrarSesion() {
        if (lanzamiento != null) {
            lanzamiento.stop();
        }

        if (monitor != null && monitor.estaMonitoreando()) {
            monitor.detener();
        }
    }

    private static void dormir(long milisegundos) {
        try {
            Thread.sleep(milisegundos);
        } catch (InterruptedException excepcion) {
            Thread.currentThread().interrupt();
        }
    }

    private static void trabajar(long milisegundos) {
        long limite = System.nanoTime() + milisegundos * 1_000_000L;

        while (System.nanoTime() < limite) {
            Thread.onSpinWait();
        }
    }

    private static String formatearHora(Instant instante) {
        return instante.atZone(ZoneId.systemDefault()).format(HORA);
    }

    private static String colorEstado(Thread.State estado) {
        return switch (estado) {
            case NEW -> "#64748b";
            case RUNNABLE -> "#15803d";
            case BLOCKED -> "#dc2626";
            case WAITING -> "#7c3aed";
            case TIMED_WAITING -> "#b45309";
            case TERMINATED -> "#1e3a8a";
        };
    }

    public static void main(String[] args) {
        launch(args);
    }
}