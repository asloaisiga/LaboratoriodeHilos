package ni.uam.edu.hilosjava.modelo;

public class ContadorCompartido implements Contador {
    private int total = 0;

    @Override
    public void incrementar() { total++; }

    @Override
    public int getTotal() { return total; }
}