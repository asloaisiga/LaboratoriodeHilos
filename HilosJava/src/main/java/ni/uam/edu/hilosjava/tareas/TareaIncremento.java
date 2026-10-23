package ni.uam.edu.hilosjava.tareas;

import ni.uam.edu.hilosjava.modelo.Contador;

public class TareaIncremento implements Runnable {
    private final Contador contador;
    private final int repeticiones;

    public TareaIncremento(Contador contador, int repeticiones) {
        this.contador = contador;
        this.repeticiones = repeticiones;
    }

    @Override
    public void run() {
        for (int i = 0; i < repeticiones; i++) {
            contador.incrementar();
        }
    }
}