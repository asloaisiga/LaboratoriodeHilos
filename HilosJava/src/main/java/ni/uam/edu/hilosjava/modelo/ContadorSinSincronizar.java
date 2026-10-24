package ni.uam.edu.hilosjava.modelo;

public class ContadorSinSincronizar implements Contador {

    private int total;

    @Override
    public void incrementar() {
        total++;
    }

    @Override
    public int getTotal() {
        return total;
    }
}
