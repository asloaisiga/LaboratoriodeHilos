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


}