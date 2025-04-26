import java.util.*;

enum tipo_evento {
    CHEGADA,
    SAIDA1,
    SAIDA2,
    PASSAGEM
};

public class T1 {
    private static final double M = 153764;
    private static final double a = 4;
    private static final double c = 4;
    private static final double semente = 7;
    private static double anterior = semente;
    private static Fila fila1;
    private static Fila fila2;
    private static List<Evento> escalonador;
    private static double tempoGlobal;
    private static int capacityFila1 = 2;
    private static int capacityFila2 = 1;
    private static int qtdMaxServidores1 = 3;
    private static int qtdMaxServidores2 = 5;
    private static int ultimoTamFila1;
    private static int ultimoTamFila2;
    private static double ultimoTempo;
    
    public static void main(String[] args) {
        System.out.println("Simulacao G/G/1/5");
        fila1 = new Fila(qtdMaxServidores1, capacityFila1, 1, 4, 3, 4);
        fila2 = new Fila(qtdMaxServidores2, capacityFila2, 0, 0, 2, 3);
        simular();
    }
    
    private static void simular() {
        escalonador = new ArrayList<>();
        
        escalonador.add(new Evento(tipo_evento.CHEGADA, 2));
        
        for (int i = 0; i < 100000; i++) {
            escalonador.sort(Comparator.comparingDouble(e -> e.tempo));
            Evento evento = escalonador.remove(0);
            
            tempoGlobal = evento.tempo;
            atualizarTempoEstadosFila();

            // System.out.printf("evento: %s no tempo %.2f\n", evento.tipo ? "saida" : "chegada", evento.tempo);
            // System.out.printf("tam fila: %d\n", fila.size());
            
            if (evento.tipo == tipo_evento.CHEGADA) {
                chegada();
            } else if(evento.tipo == tipo_evento.SAIDA1) {
                saida1(); 
            } else if(evento.tipo == tipo_evento.SAIDA2) {
                saida2(); 
            } else if(evento.tipo == tipo_evento.PASSAGEM) {
                passagem();
            }
        }
        reportarResultados();
    }
    
    private static void chegada() {
        // System.out.printf("chegada no tempo %.2f\n", tempoGlobal);
        if (fila1.getCustomers() < fila1.getCapacity()) {
            fila1.in();
            if (fila1.getCustomers() <= fila1.getServer()) {
                if (NextRandom() < 0.7) {
                    double tempoPassagem = tempoGlobal + calculaTempo(fila1.getMinService(), fila1.getMaxService());
                    escalonador.add(new Evento(tipo_evento.PASSAGEM, tempoPassagem));
                }
                else {
                    double tempoSaida = tempoGlobal + calculaTempo(fila1.getMinService(), fila1.getMaxService());
                    escalonador.add(new Evento(tipo_evento.SAIDA1, tempoSaida));
                }
                // System.out.printf("agendou saida para %.2f\n", tempoSaida);
            }
        } else {
            fila1.loss();
            // System.out.println("cliente perdido");
        }
        double tempoProxChegada = tempoGlobal + calculaTempo(fila1.getMinArrival(), fila1.getMaxArrival());
        escalonador.add(new Evento(tipo_evento.CHEGADA, tempoProxChegada));
        // System.out.printf("agendou proxima chegada para %.2f\n", tempoProxChegada);
    }

    private static void saida1() {
        fila1.out();
        if (fila1.getCustomers() >= fila1.getServer()) {
            if(NextRandom() < 0.7) {
                double tempoPassagem = tempoGlobal + calculaTempo(fila2.getMinService(), fila2.getMaxService());
                escalonador.add(new Evento(tipo_evento.PASSAGEM, tempoPassagem));
                // System.out.printf("agendou nova saida para %.2f\n", tempoSaida);
            }
            else {
                double tempoSaida = tempoGlobal + calculaTempo(fila2.getMinService(), fila2.getMaxService());
                escalonador.add(new Evento(tipo_evento.SAIDA1, tempoSaida));
            }
        }
    }
    
    private static void saida2() {
        // System.out.printf("saida no tempo %.2f\n", tempoGlobal);
        fila2.out();
        if (fila2.getCustomers() >= fila2.getServer()) {
            double tempoSaida = tempoGlobal + calculaTempo(fila2.getMinService(), fila2.getMaxService());
            escalonador.add(new Evento(tipo_evento.SAIDA2, tempoSaida));
            // System.out.printf("agendou nova saida para %.2f\n", tempoSaida);
        }
    }

    private static void passagem() {
        fila1.out();
        
        if (fila1.getCustomers() >= fila1.getServer()) {
            if (NextRandom() < 0.7) {
                double tempoPassagem = tempoGlobal + calculaTempo(fila1.getMinService(), fila1.getMaxService());
                escalonador.add(new Evento(tipo_evento.PASSAGEM, tempoPassagem));
            }
            else {
                double tempoSaida = tempoGlobal + calculaTempo(fila1.getMinService(), fila1.getMaxService());
                escalonador.add(new Evento(tipo_evento.SAIDA1, tempoSaida));
            }
        }
        if (fila2.getCustomers() < fila2.getCapacity()) {
            fila2.in();
            if (fila2.getCustomers() <= fila2.getServer()) {
                double tempoSaida = tempoGlobal + calculaTempo(fila2.getMinService(), fila2.getMaxService());
                escalonador.add(new Evento(tipo_evento.SAIDA2, tempoSaida));
            }
        } 
        else {
            fila2.loss();
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

        ultimoTamFila1 = fila1.getCustomers();
        ultimoTamFila2 = fila2.getCustomers();

        if (ultimoTamFila1 >= 0) {
            fila1.setTime(ultimoTamFila1, delta);
        }
        if (ultimoTamFila2 >= 0) {
            fila2.setTime(ultimoTamFila2, delta);
        }

        ultimoTempo = tempoGlobal;
    }

    private static void reportarResultados() {
        System.out.println("Tempo Global da Simulacao: " + tempoGlobal);
        System.out.println("Clientes Totais Perdidos: " + (fila1.getLoss() + fila2.getLoss()));
        System.out.println("Distribuicao de Probabilidades dos Estados da Fila 1:");
        double [] timesFila1 = fila1.getTimes();
        for (int i = 0; i < timesFila1.length; i++) {
            System.out.printf("Fila 1 %d: %.5f%%\n", i, (timesFila1[i] / tempoGlobal)*100);
        }
        System.out.println("Distribuicao de Probabilidades dos Estados da Fila 2:");
        double [] timesFila2 = fila2.getTimes();
        for (int i = 0; i < timesFila2.length; i++) {
            System.out.printf("Fila 1 %d: %.5f%%\n", i, (timesFila2[i] / tempoGlobal)*100);
        }
    }
}

class Evento {
    tipo_evento tipo; // false para chegada, true para saída
    double tempo;
    
    public Evento(tipo_evento tipo, double tempo) {
        this.tipo = tipo;
        this.tempo = tempo;
    }
}
