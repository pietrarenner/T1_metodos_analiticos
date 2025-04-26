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
    private static List<Evento> escalonador;
    private static double tempoGlobal;
    private static double ultimoTempo;
    
    public static void main(String[] args) {
        System.out.println("Simulacao G/G/1/5");
        HashMap<Integer, Double> map1 = new HashMap<>();
        map1.put(1, 0.8);
        map1.put(2, 0.2);
        
        HashMap<Integer, Double> map2 = new HashMap<>();
        map2.put(-1, 0.2); // vem do json
        map2.put(0, 0.3); // vem do json
        map2.put(2, 0.5); // vem do json
        
        HashMap<Integer, Double> map3 = new HashMap<>();
        map3.put(-1, 0.3); // vem do json
        map3.put(1, 0.7); // vem do json

        Fila fila1 = new Fila(1, 100001, 2, 4, 1, 2, map1, 0); // vem do json
        Fila fila2 = new Fila(2, 5, 0, 0, 4, 8, map2, 1); // vem do json
        Fila fila3 = new Fila(2, 10, 0, 0, 5, 15, map3, 2); // vem do json
        listaDeFilas.add(fila1);
        listaDeFilas.add(fila2);
        listaDeFilas.add(fila3);
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
                            escalonador.add(new Evento(tipo_evento.PASSAGEM, tempoPassagem, filaDestino.getIndex()));
                        }
                        else {
                            double tempoSaida = tempoGlobal + calculaTempo(filaDestino.getMinService(), filaDestino.getMaxService());
                            escalonador.add(new Evento(tipo_evento.SAIDA, tempoSaida, filaDestino.getIndex()));
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
                        escalonador.add(new Evento(tipo_evento.PASSAGEM, tempoPassagem, filaOrigem.getIndex()));
                    }
                    else {
                        double tempoSaida = tempoGlobal + calculaTempo(filaOrigem.getMinService(), filaOrigem.getMaxService());
                        escalonador.add(new Evento(tipo_evento.SAIDA, tempoSaida, filaOrigem.getIndex()));
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
        boolean isExit = false;
        
        if (filaOrigem.getCustomers() >= filaOrigem.getServer()) {
            for (Map.Entry<Integer, Double> entry : filaOrigem.getFilas().entrySet()) {
                sum += entry.getValue();
                if (prob < sum) {
                    if (entry.getKey() != -1) {
                        double tempoPassagem = tempoGlobal + calculaTempo(filaOrigem.getMinService(), filaOrigem.getMaxService());
                        escalonador.add(new Evento(tipo_evento.PASSAGEM, tempoPassagem, filaOrigem.getIndex()));
                        filaDestino = listaDeFilas.get(entry.getKey()); // fila seguinte
                    }
                    else {
                        double tempoSaida = tempoGlobal + calculaTempo(filaOrigem.getMinService(), filaOrigem.getMaxService());
                        escalonador.add(new Evento(tipo_evento.SAIDA, tempoSaida, filaOrigem.getIndex()));
                        // ele vai sair, não deve ter fila destino 
                        isExit = true;
                    }
                    break;
                }
            }
        }
        if(!isExit) {
            if (filaDestino.getCustomers() < filaDestino.getCapacity()) {
                filaDestino.in();
                if (filaDestino.getCustomers() <= filaDestino.getServer()) {
                    for (Map.Entry<Integer, Double> entry : filaDestino.getFilas().entrySet()) {
                        sum += entry.getValue();
                        if (prob < sum) {
                            if (entry.getKey() != -1) {
                                double tempoPassagem = tempoGlobal + calculaTempo(filaDestino.getMinService(), filaDestino.getMaxService());
                                escalonador.add(new Evento(tipo_evento.PASSAGEM, tempoPassagem, filaDestino.getIndex()));
                            }
                            else {
                                double tempoSaida = tempoGlobal + calculaTempo(filaDestino.getMinService(), filaDestino.getMaxService());
                                escalonador.add(new Evento(tipo_evento.SAIDA, tempoSaida, filaDestino.getIndex()));
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
        // System.out.println("delta: " + delta);

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
        
        int totalPerdidos = 0;
        int j = 1;
        for (Fila fila : listaDeFilas) {
            System.out.println("Clientes perdidos na fila " + j + ": " + fila.getLoss());
            j++;
            totalPerdidos += fila.getLoss();
        }
        // System.out.println("Clientes Totais Perdidos: " + totalPerdidos);
    
        for (int idx = 0; idx < listaDeFilas.size(); idx++) {
            Fila fila = listaDeFilas.get(idx);
            System.out.println("Distribuicao de Probabilidades dos Estados da Fila " + (idx + 1) + ":");
            double[] times = fila.getTimes();
            for (int i = 0; i < times.length; i++) {
                if (times[i] > 0) System.out.printf("Fila %d estado %d: %.5f%%\n", idx + 1, i, ((times[i] / tempoGlobal) * 100));
            }
        }
    }
}

class Evento {
    tipo_evento tipo;
    double tempo;
    int fila;
    
    public Evento(tipo_evento tipo, double tempo, int fila) {
        this.tipo = tipo;
        this.tempo = tempo;
        this.fila = fila;
    }
}
