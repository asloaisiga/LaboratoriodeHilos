package ni.uam.edu.hilosjava.concurrencia;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class Aplicacion extends Application implements Controlador.VistaUI {

    private Button btnIniciar;
    private Button btnDetener;
    private Label lblEstado;
    private Label lblContador;
    private TextArea areaLogs;

    private Controlador controlador;

    @Override
    public void start(Stage primaryStage) {
        controlador = new Controlador(this);

        primaryStage.setTitle("Laboratorio Concurrencia - Estación 4 y 5");

        // Componentes visuales
        Label lblTitulo = new Label("Control de Ventanillas Concurrente");
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        lblEstado = new Label("En espera");
        lblEstado.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2a6f97;");

        lblContador = new Label("Atenciones Totales: 0");
        lblContador.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        btnIniciar = new Button("Iniciar");
        btnIniciar.setStyle("-fx-background-color: #2b9348; -fx-text-fill: white; -fx-font-weight: bold;");
        btnIniciar.setOnAction(e -> controlador.iniciarSesion());

        btnDetener = new Button("Detener");
        btnDetener.setStyle("-fx-background-color: #d90429; -fx-text-fill: white; -fx-font-weight: bold;");
        btnDetener.setDisable(true);
        btnDetener.setOnAction(e -> controlador.detenerSesion());

        areaLogs = new TextArea();
        areaLogs.setEditable(false);
        areaLogs.setPrefHeight(250);

        // Layouts
        HBox panelBotones = new HBox(15, btnIniciar, btnDetener);
        panelBotones.setAlignment(Pos.CENTER);

        HBox panelEstado = new HBox(10, new Label("Estado actual:"), lblEstado);
        panelEstado.setAlignment(Pos.CENTER);

        VBox contenedorPrincipal = new VBox(12,
                lblTitulo,
                panelEstado,
                lblContador,
                panelBotones,
                new Label("Registro de actividad:"),
                areaLogs
        );
        contenedorPrincipal.setPadding(new Insets(15));
        contenedorPrincipal.setAlignment(Pos.CENTER);

        Scene escena = new Scene(contenedorPrincipal, 480, 480);
        primaryStage.setScene(escena);
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.show();
    }

    // Implementación de la interfaz Controlador.VistaUI
    @Override
    public void actualizarEstado(String estado) {
        lblEstado.setText(estado);
    }

    @Override
    public void agregarLog(String mensaje) {
        areaLogs.appendText(mensaje + "\n");
    }

    @Override
    public void actualizarContadorUI(int valor) {
        lblContador.setText("Atenciones Totales: " + valor);
    }

    @Override
    public void cambiarEstadoBotones(boolean ejecutando) {
        btnIniciar.setDisable(ejecutando);
        btnDetener.setDisable(!ejecutando);
    }

    public static void main(String[] args) {
        launch(args);
    }
}