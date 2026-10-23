package ni.uam.edu.hilosjava.app;

import ni.uam.edu.hilosjava.modelo.Contador;
import ni.uam.edu.hilosjava.modelo.ContadorAtomico;
import ni.uam.edu.hilosjava.modelo.ContadorCompartido;
import ni.uam.edu.hilosjava.tareas.TareaIncremento;

import java.util.function.Supplier;

public class PruebaContador {
    private static final int PRUEBAS = 10;
    private static final int REPETICIONES = 100000;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Esperado por ejecución: " + (2 * REPETICIONES));
        ejecutar("synchronized", ContadorCompartido::new);
        ejecutar("AtomicInteger", ContadorAtomico::new);
    }

    private static void ejecutar(String nombre, Supplier<Contador> fabrica) throws InterruptedException {
        System.out.println("--- " + nombre + " ---");
        long inicio = System.nanoTime();
        for (int n = 1; n <= PRUEBAS; n++) {
            Contador contador = fabrica.get();
            Thread a = new Thread(new TareaIncremento(contador, REPETICIONES), "Ventanilla-A");
            Thread b = new Thread(new TareaIncremento(contador, REPETICIONES), "Ventanilla-B");
            a.start();
            b.start();
            a.join();
            b.join();
            System.out.println("Ejecución " + n + ": " + contador.getTotal());
        }
        System.out.println("Tiempo total: " + (System.nanoTime() - inicio) / 1_000_000 + " ms");
    }
}