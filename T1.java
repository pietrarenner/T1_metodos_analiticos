import java.util.*;

public class T1 {
    private static final double M = 153764;
    private static final double a = 4;
    private static final double c = 4;
    private static final double semente = 7;
    private static double anterior = semente;
    private static Queue<Evento> fila;
    private static List<Evento> escalonador;
    private static double tempoGlobal;
    private static int tamMaxFila;
    private static int qtdMaxServidores;
    private static int clientesPerdidos;
    private static Map<Integer, Double> tempoEstadosFila;
    private static int ultimoTamFila;
    private static double ultimoTempo;
    
    public static void main(String[] args) {
        System.out.println("Simulacao G/G/1/5");
        simular(1);
        System.out.println();
        System.out.println("Simulacao G/G/2/5");
        simular(2);       
    }
    
    private static void simular(int servidores) {
        fila = new LinkedList<>();
        escalonador = new ArrayList<>();
        tempoEstadosFila = new HashMap<>();
        
        tamMaxFila = 5;
        qtdMaxServidores = servidores;
        clientesPerdidos = 0;
        tempoGlobal = 2;
        ultimoTamFila = 0;
        ultimoTempo = 0;
        
        escalonador.add(new Evento(false, 2));
        
        for (int i = 0; i < 100000; i++) {
            escalonador.sort(Comparator.comparingDouble(e -> e.tempo));
            Evento evento = escalonador.remove(0);
            
            tempoGlobal = evento.tempo;
            atualizarTempoEstadosFila();

            // System.out.printf("evento: %s no tempo %.2f\n", evento.tipo ? "saida" : "chegada", evento.tempo);
            // System.out.printf("tam fila: %d\n", fila.size());
            
            if (!evento.tipo) {
                chegada();
            } else {
                saida();
            }
        }
        reportarResultados();
    }
    
    private static void chegada() {
        // System.out.printf("chegada no tempo %.2f\n", tempoGlobal);
        if (fila.size() < tamMaxFila) {
            fila.add(new Evento(true, tempoGlobal));
            if (fila.size() <= qtdMaxServidores) {
                double tempoSaida = tempoGlobal + calculaTempo(3, 5);
                escalonador.add(new Evento(true, tempoSaida));
                // System.out.printf("agendou saida para %.2f\n", tempoSaida);
            }
        } else {
            clientesPerdidos++;
            // System.out.println("cliente perdido");
        }
        double tempoProxChegada = tempoGlobal + calculaTempo(2, 5);
        escalonador.add(new Evento(false, tempoProxChegada));
        // System.out.printf("agendou proxima chegada para %.2f\n", tempoProxChegada);
    }
    
    private static void saida() {
        // System.out.printf("saida no tempo %.2f\n", tempoGlobal);
        if (!fila.isEmpty()) {
            fila.remove();
            if (fila.size() >= qtdMaxServidores) {
                double tempoSaida = tempoGlobal + calculaTempo(3, 5);
                escalonador.add(new Evento(true, tempoSaida));
                // System.out.printf("agendou nova saida para %.2f\n", tempoSaida);
            }
        }
    }
    
    private static double calculaTempo(double a, double b) {
        return a + ((b - a) * NextRandom());
    }   
    
    private static double NextRandom() {
        anterior = ((a * anterior) + c) % M;
        return anterior / M;
    }

    private static void atualizarTempoEstadosFila() {
        double delta = tempoGlobal - ultimoTempo;
        ultimoTamFila = fila.size();
        tempoEstadosFila.put(ultimoTamFila, tempoEstadosFila.getOrDefault(ultimoTamFila, 0.0) + delta);
        ultimoTempo = tempoGlobal;
    }

    private static void reportarResultados() {
        System.out.println("Tempo Global da Simulacao: " + tempoGlobal);
        System.out.println("Clientes Perdidos: " + clientesPerdidos);
        System.out.println("Distribuicao de Probabilidades dos Estados da Fila:");
        for (int estado : tempoEstadosFila.keySet()) {
            System.out.printf("Fila %d: %.5f%%\n", estado, (tempoEstadosFila.get(estado) / tempoGlobal)*100);
        }
    }   
}

class Evento {
    boolean tipo; // false para chegada, true para saída
    double tempo;
    
    public Evento(boolean tipo, double tempo) {
        this.tipo = tipo;
        this.tempo = tempo;
    }
}
