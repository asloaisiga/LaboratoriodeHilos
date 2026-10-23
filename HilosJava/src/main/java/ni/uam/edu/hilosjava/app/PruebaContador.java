package ni.uam.edu.hilosjava.app;

import ni.uam.edu.hilosjava.modelo.Contador;
import ni.uam.edu.hilosjava.modelo.ContadorCompartido;
import ni.uam.edu.hilosjava.tareas.TareaIncremento;

public class PruebaContador {
    private static final int PRUEBAS = 10;
    private static final int REPETICIONES = 100000;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Esperado por ejecución: " + (2 * REPETICIONES));
        for (int n = 1; n <= PRUEBAS; n++) {
            Contador contador = new ContadorCompartido();
            Thread a = new Thread(new TareaIncremento(contador, REPETICIONES), "Ventanilla-A");
            Thread b = new Thread(new TareaIncremento(contador, REPETICIONES), "Ventanilla-B");
            a.start();
            b.start();
            a.join();
            b.join();
            System.out.println("Ejecución " + n + ": " + contador.getTotal());
        }
    }
}