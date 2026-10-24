
package ni.uam.edu.hilosjava.Procesos;

import ni.uam.edu.hilosjava.concurrencia.ControlDetencion;
import ni.uam.edu.hilosjava.modelo.Contador;
import java.util.function.Consumer;

public class ProcesoVentanilla implements Runnable {

    private final String nombre;
    private final Contador contador;
    private final ControlDetencion control;
    private final Consumer<String> mensajes;

    public ProcesoVentanilla(String nombre) {
        this(nombre, null, null, System.out::println);
    }

    public ProcesoVentanilla(
            String nombre,
            Contador contador,
            ControlDetencion control,
            Consumer<String> mensajes) {

        this.nombre = nombre;
        this.contador = contador;
        this.control = control;
        this.mensajes = mensajes;
    }

    @Override
    public void run() {

        for (int i = 1; i <= 5; i++) {

            if (Thread.currentThread().isInterrupted()
                    || (control != null
                    && control.esDetencionSolicitada())) {
                return;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            if (Thread.currentThread().isInterrupted()
                    || (control != null
                    && control.esDetencionSolicitada())) {
                return;
            }

            if (contador != null) {
                contador.incrementar();
            }

            mensajes.accept(
                    nombre + " procesa " + i
                            + " | "
                            + Thread.currentThread().getName()
            );
        }
    }
}
