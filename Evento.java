public class Evento {
    tipo_evento tipo; //0 - entrada; 1 - saida
    double tempo;
    int fila;

    public Evento(tipo_evento tipo, double tempo, int fila) {
        this.tipo = tipo;
        this.tempo = tempo;
        this.fila = fila;
    }
}
