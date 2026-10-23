
package ni.uam.edu.hilosjava.app;

import ni.uam.edu.hilosjava.Procesos.ProcesoVentanilla;

public class ComparacionHilos {

    public static void main(String[] args) {

        System.out.println("===== PRUEBA CON RUN() =====");

        Thread hiloA = new Thread(
                new ProcesoVentanilla("Ventanilla A"),
                "Hilo-A"
        );

        Thread hiloB = new Thread(
                new ProcesoVentanilla("Ventanilla B"),
                "Hilo-B"
        );

        hiloA.run();
        hiloB.run();

        System.out.println("===== PRUEBA CON START() =====");

        Thread nuevoA = new Thread(
                new ProcesoVentanilla("Ventanilla A"),
                "Hilo-A"
        );

        Thread nuevoB = new Thread(
                new ProcesoVentanilla("Ventanilla B"),
                "Hilo-B"
        );

        nuevoA.start();
        nuevoB.start();
    }
}
