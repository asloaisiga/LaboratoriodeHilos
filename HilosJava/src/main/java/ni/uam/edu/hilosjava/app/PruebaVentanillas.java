package ni.uam.edu.hilosjava.app;

import ni.uam.edu.hilosjava.Procesos.ProcesoVentanilla;

public class PruebaVentanillas {
    public static void main(String[] args) {

        ProcesoVentanilla procesoA =
                new ProcesoVentanilla("Ventanilla A");

        ProcesoVentanilla procesoB =
                new ProcesoVentanilla("Ventanilla B");

        Thread hiloA = new Thread(
                procesoA, "Hilo-Ventanilla-A"
        );

        Thread hiloB = new Thread(
                procesoB, "Hilo-Ventanilla-B"
        );

        System.out.println("Iniciando ventanillas...");

        hiloA.start();
        hiloB.start();

        System.out.println(
                "El hilo principal continúa ejecutándose."
        );
    }
}
