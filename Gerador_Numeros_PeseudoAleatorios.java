public class Gerador_Numeros_PeseudoAleatorios {
    private final double M = 33554432;
    private final double a = 1664525;
    private final double c = 3;
    private double semente;
    private double anterior;

    public Gerador_Numeros_PeseudoAleatorios(double semente) {
        if(semente < this.M-1) this.semente = semente;
        else this.semente = 7;
        this.anterior = this.semente;
    }

    public Gerador_Numeros_PeseudoAleatorios() {
        this.semente = 7;
        this.anterior = semente;
    }

    public double NextRandom() {
        anterior = ((a * anterior) + c) % M;
        return anterior / M;
    }
}
