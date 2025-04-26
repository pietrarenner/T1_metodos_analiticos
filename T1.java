import java.util.*;

enum tipo_evento {
    CHEGADA,
    SAIDA,
    PASSAGEM
};

public class T1 {
    private static final double M = 153764;
    private static final double a = 4;
    private static final double c = 4;
    private static final double semente = 7;
    private static double anterior = semente;
    private static ArrayList<Fila> listaDeFilas = new ArrayList<>();
    //private static Fila fila1;
    //private static Fila fila2;
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
        HashMap<Integer, Double> map1 = new HashMap<>();
        map1.put(1, 0.8);
        map1.put(2, 0.2);
        
        HashMap<Integer, Double> map2 = new HashMap<>();
        map2.put(-1, 0.2); // vem do json
        map2.put(0, 0.3); // vem do json
        map2.put(1, 0.5); // vem do json



        Fila fila1 = new Fila(qtdMaxServidores1, capacityFila1, 1, 4, 3, 4, map1); // vem do json
        Fila fila2 = new Fila(qtdMaxServidores2, capacityFila2, 0, 0, 2, 3, map2); // vem do json
        listaDeFilas.add(fila1);
        listaDeFilas.add(fila2);
        simular();

        // minArrival = 0 significa q a fila nao recebe de fora
    }
    
    private static void simular() {
        escalonador = new ArrayList<>();
        
        escalonador.add(new Evento(tipo_evento.CHEGADA, 2, 0)); // vem do json
        
        for (int i = 0; i < 100000; i++) {
            escalonador.sort(Comparator.comparingDouble(e -> e.tempo));
            Evento evento = escalonador.remove(0);
            
            tempoGlobal = evento.tempo;
            atualizarTempoEstadosFila();

            // System.out.printf("evento: %s no tempo %.2f\n", evento.tipo ? "saida" : "chegada", evento.tempo);
            // System.out.printf("tam fila: %d\n", fila.size());
            
            if (evento.tipo == tipo_evento.CHEGADA) {
                chegada(listaDeFilas.get(evento.fila));
            } else if(evento.tipo == tipo_evento.SAIDA) {
                saida(listaDeFilas.get(evento.fila)); 
            } else if(evento.tipo == tipo_evento.PASSAGEM) {
                passagem(listaDeFilas.get(evento.fila));
            }
        }
        reportarResultados();
    }
    
    private static void chegada(Fila filaDestino) { // no nosso caso sempre a fila 1
        // System.out.printf("chegada no tempo %.2f\n", tempoGlobal);
        if (filaDestino.getCustomers() < filaDestino.getCapacity()) {
            filaDestino.in();
            if (filaDestino.getCustomers() <= filaDestino.getServer()) {
                double sum = 0.0;
                double prob = NextRandom();
                for (Map.Entry<Integer, Double> entry : filaDestino.getFilas().entrySet()) {
                    sum += entry.getValue();
                    if (prob < sum) {
                        if (entry.getKey() != -1) {
                            double tempoPassagem = tempoGlobal + calculaTempo(filaDestino.getMinService(), filaDestino.getMaxService());
                            escalonador.add(new Evento(tipo_evento.PASSAGEM, tempoPassagem, entry.getKey()));
                        }
                        else {
                            double tempoSaida = tempoGlobal + calculaTempo(filaDestino.getMinService(), filaDestino.getMaxService());
                            escalonador.add(new Evento(tipo_evento.SAIDA, tempoSaida, entry.getKey()));
                        }
                        break;
                    }
                }
            }
        } else {
            filaDestino.loss();
            // System.out.println("cliente perdido");
        }
        double tempoProxChegada = tempoGlobal + calculaTempo(filaDestino.getMinArrival(), filaDestino.getMaxArrival());
        escalonador.add(new Evento(tipo_evento.CHEGADA, tempoProxChegada, 0));  // TODO: pegar o indice do json no ARRIVALS
        // System.out.printf("agendou proxima chegada para %.2f\n", tempoProxChegada);
    }

    private static void saida(Fila filaOrigem) {
        filaOrigem.out(); // pessoa foi atendida
        double sum = 0.0;
        double prob = NextRandom();

        if (filaOrigem.getCustomers() >= filaOrigem.getServer()) { // 5 pessoas ainda não foram atendidas e temos 2 servidores 
            for (Map.Entry<Integer, Double> entry : filaOrigem.getFilas().entrySet()) {
                sum += entry.getValue();
                if (prob < sum) {
                    if (entry.getKey() != -1) {
                        double tempoPassagem = tempoGlobal + calculaTempo(filaOrigem.getMinService(), filaOrigem.getMaxService());
                        escalonador.add(new Evento(tipo_evento.PASSAGEM, tempoPassagem, entry.getKey()));
                    }
                    else {
                        double tempoSaida = tempoGlobal + calculaTempo(filaOrigem.getMinService(), filaOrigem.getMaxService());
                        escalonador.add(new Evento(tipo_evento.SAIDA, tempoSaida, entry.getKey()));
                    }
                    break;
                }
            }
        }
    }

    private static void passagem(Fila filaOrigem) {
        filaOrigem.out();
        Fila filaDestino = filaOrigem;
        double sum = 0.0;
        double prob = NextRandom();
        
        if (filaOrigem.getCustomers() >= filaOrigem.getServer()) {
            for (Map.Entry<Integer, Double> entry : filaOrigem.getFilas().entrySet()) {
                sum += entry.getValue();
                if (prob < sum) {
                    if (entry.getKey() != -1) {
                        double tempoPassagem = tempoGlobal + calculaTempo(filaOrigem.getMinService(), filaOrigem.getMaxService());
                        escalonador.add(new Evento(tipo_evento.PASSAGEM, tempoPassagem, entry.getKey()));
                    }
                    else {
                        double tempoSaida = tempoGlobal + calculaTempo(filaOrigem.getMinService(), filaOrigem.getMaxService());
                        escalonador.add(new Evento(tipo_evento.SAIDA, tempoSaida, entry.getKey()));
                    }
                    filaDestino = listaDeFilas.get(entry.getKey());
                    break;
                }
            }
        }
        if (filaDestino.getCustomers() < filaDestino.getCapacity()) {
            filaDestino.in();
            if (filaDestino.getCustomers() <= filaDestino.getServer()) {
                for (Map.Entry<Integer, Double> entry : filaDestino.getFilas().entrySet()) {
                    sum += entry.getValue();
                    if (prob < sum) {
                        if (entry.getKey() != -1) {
                            double tempoPassagem = tempoGlobal + calculaTempo(filaDestino.getMinService(), filaDestino.getMaxService());
                            escalonador.add(new Evento(tipo_evento.PASSAGEM, tempoPassagem, entry.getKey()));
                        }
                        else {
                            double tempoSaida = tempoGlobal + calculaTempo(filaDestino.getMinService(), filaDestino.getMaxService());
                            escalonador.add(new Evento(tipo_evento.SAIDA, tempoSaida, entry.getKey()));
                        }
                        break;
                    }
                }
            }
        }
        else {
            filaDestino.loss();
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

        for(Fila fila : listaDeFilas) {
            int ultimoTamFila = fila.getCustomers();

            if (ultimoTamFila >= 0 && ultimoTamFila <= fila.getCapacity()) {
                fila.setTime(ultimoTamFila, delta);
            }
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
    int fila;
    
    public Evento(tipo_evento tipo, double tempo, int fila) {
        this.tipo = tipo;
        this.tempo = tempo;
        this.fila = fila;
    }
}
