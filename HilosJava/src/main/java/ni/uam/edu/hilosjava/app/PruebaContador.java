
package ni.uam.edu.hilosjava.app;

import ni.uam.edu.hilosjava.modelo.*;
import ni.uam.edu.hilosjava.tareas.TareaIncremento;

import java.util.function.Supplier;

public class PruebaContador {

    private static final int PRUEBAS = 10;
    private static final int REPETICIONES = 1000;

    public static void main(String[] args)
            throws InterruptedException {

        System.out.println(
                "Resultado esperado: "
                        + (2 * REPETICIONES)
        );

        probar(
                "Sin sincronización",
                ContadorSinSincronizar::new
        );

        probar(
                "synchronized",
                ContadorCompartido::new
        );

        probar(
                "AtomicInteger",
                ContadorAtomico::new
        );
    }

    private static void probar(
            String titulo,
            Supplier<Contador> fabrica)
            throws InterruptedException {

        System.out.println("\n--- " + titulo + " ---");

        for (int i = 1; i <= PRUEBAS; i++) {

            Contador contador = fabrica.get();

            Thread a = new Thread(
                    new TareaIncremento(contador, REPETICIONES)
            );

            Thread b = new Thread(
                    new TareaIncremento(contador, REPETICIONES)
            );

            a.start();
            b.start();

            a.join();
            b.join();

            System.out.println(
                    "Prueba " + i + ": "
                            + contador.getTotal()
            );
        }
    }
}
