package ni.uam.edu.hilosjava.modelo;

public class ContadorCompartido implements Contador {
    private int total = 0;

    @Override
    public synchronized void incrementar() { total++; }

    @Override
    public synchronized int getTotal() { return total; }
}