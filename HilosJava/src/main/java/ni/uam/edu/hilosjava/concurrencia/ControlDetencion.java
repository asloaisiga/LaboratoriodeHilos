package ni.uam.edu.hilosjava.concurrencia;

public class ControlDetencion {
    private volatile boolean detencionSolicitada = false;

    public void solicitarDetencion() {
        this.detencionSolicitada = true;
    }

    public boolean esDetencionSolicitada() {
        return detencionSolicitada;
    }

    public void reiniciar() {
        this.detencionSolicitada = false;
    }
}