import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Escalonador {
    private List<Evento> escalonador;

    public Escalonador() {
        this.escalonador = new ArrayList<Evento>();
    }

    public void adicionarEvento(Evento evento) {
        escalonador.add(evento);
        escalonador.sort(Comparator.comparingDouble(e -> e.tempo));
    }

    public Evento removerEvento() {
        return escalonador.remove(0);
    }
}