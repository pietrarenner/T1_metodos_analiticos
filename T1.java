import java.util.*;

/*
 * para rodar o simulador adicione as informações de cada fila no arquivo modelo.json
 */
public class T1 {
    private static Gerador_Numeros_PeseudoAleatorios gerador = new Gerador_Numeros_PeseudoAleatorios();
    private static ArrayList<Fila> listaDeFilas = new ArrayList<>();
    private static Escalonador escalonador = new Escalonador();
    private static double tempoGlobal;
    private static double ultimoTempo;
    private static int numIteracoes;

    public static void main(String[] args) {
        ParametrosSimulador jsonReader = new ParametrosSimulador();
        Map<String, Object> jsonContent = jsonReader.getJsonValues();

        numIteracoes = (int) jsonContent.get("rndnumbersPerSeed");

        LinkedHashMap<String, Object> probabilities = (LinkedHashMap<String, Object>) jsonContent.get("probabilities");
        ArrayList<HashMap<Integer, Double>> probabilidadesTodasFilas = new ArrayList<>();
        Map<String, Object> queues = (Map<String, Object>) jsonContent.get("queues");

        for (Map.Entry<String, Object> entry : probabilities.entrySet()) {
            HashMap<Integer, Double> probabilidadesDaFila = new HashMap<>();

            Map<String, Object> probData = (Map<String, Object>) entry.getValue();

            for (Map.Entry<String, Object> probEntry : probData.entrySet()) {
                int proxLugar = Integer.parseInt(probEntry.getKey());
                double probability = (double) probEntry.getValue();
                probabilidadesDaFila.put(proxLugar, probability);
            }

            probabilidadesTodasFilas.add(probabilidadesDaFila);
        }

        for (Map.Entry<String, Object> entry : queues.entrySet()) {
            String queueName = entry.getKey();
            Map<String, Object> queueData = (Map<String, Object>) entry.getValue();

            if (queueName.equals("Q1")) {
                int index = (int) queueData.get("index");
                int server = (int) queueData.get("servers");
                int capacity = (int) queueData.get("capacity");

                double minArrival = (double) queueData.get("minArrival");
                double maxArrival = (double) queueData.get("maxArrival");

                double minService = (double) queueData.get("minService");
                double maxService = (double) queueData.get("maxService");

                listaDeFilas.add(new Fila(server, capacity, minArrival, maxArrival,
                        minService, maxService, probabilidadesTodasFilas.get(index), index));
            }

            else {
                int index = (int) queueData.get("index");
                int server = (int) queueData.get("servers");
                int capacity = (int) queueData.get("capacity");

                double minService = (double) queueData.get("minService");
                double maxService = (double) queueData.get("maxService");

                listaDeFilas.add(new Fila(server, capacity, 0, 0, minService, maxService, probabilidadesTodasFilas.get(index), index));
            }
        }

        Map<String, Double> chegada = (Map<String, Double>) jsonContent.get("arrivals");
        double primeiraChegada = (double) chegada.get("0");
        simular(primeiraChegada);
    }

    private static void simular(double primeiraChegada) {
        escalonador.adicionarEvento(new Evento(tipo_evento.CHEGADA, primeiraChegada, 0)); // vem do json

        for (int i = 0; i < numIteracoes; i++) {
            Evento evento = escalonador.removerEvento();

            tempoGlobal = evento.tempo;
            atualizarTempoEstadosFila();

            if (evento.tipo == tipo_evento.CHEGADA) {
                chegada(listaDeFilas.get(evento.fila));
            } else if (evento.tipo == tipo_evento.SAIDA) {
                saida(listaDeFilas.get(evento.fila));
            } else if (evento.tipo == tipo_evento.PASSAGEM) {
                passagem(listaDeFilas.get(evento.fila));
            }
        }
        reportarResultados();
    }

    private static void chegada(Fila filaDestino) { 
        if (filaDestino.getCustomers() < filaDestino.getCapacity()) {
            filaDestino.in();
            if (filaDestino.getCustomers() <= filaDestino.getServer()) {
                double sum = 0.0;
                double prob = gerador.NextRandom();
                for (Map.Entry<Integer, Double> entry : filaDestino.getFilas().entrySet()) {
                    sum += entry.getValue();
                    if (prob < sum) {
                        if (entry.getKey() != -1) {
                            double tempoPassagem = tempoGlobal
                                    + calculaTempo(filaDestino.getMinService(), filaDestino.getMaxService());
                            escalonador.adicionarEvento(new Evento(tipo_evento.PASSAGEM, tempoPassagem, filaDestino.getIndex()));
                        } else {
                            double tempoSaida = tempoGlobal
                                    + calculaTempo(filaDestino.getMinService(), filaDestino.getMaxService());
                            escalonador.adicionarEvento(new Evento(tipo_evento.SAIDA, tempoSaida, filaDestino.getIndex()));
                        }
                        break;
                    }
                }
            }
        } else {
            filaDestino.loss();
        }
        double tempoProxChegada = tempoGlobal + calculaTempo(filaDestino.getMinArrival(), filaDestino.getMaxArrival());
        escalonador.adicionarEvento(new Evento(tipo_evento.CHEGADA, tempoProxChegada, 0)); 
    }

    private static void saida(Fila filaOrigem) {
        filaOrigem.out(); 
        double sum = 0.0;
        double prob = gerador.NextRandom();

        if (filaOrigem.getCustomers() >= filaOrigem.getServer()) { 
            for (Map.Entry<Integer, Double> entry : filaOrigem.getFilas().entrySet()) {
                sum += entry.getValue();
                if (prob < sum) {
                    if (entry.getKey() != -1) {
                        double tempoPassagem = tempoGlobal
                                + calculaTempo(filaOrigem.getMinService(), filaOrigem.getMaxService());
                        escalonador.adicionarEvento(new Evento(tipo_evento.PASSAGEM, tempoPassagem, filaOrigem.getIndex()));
                    } else {
                        double tempoSaida = tempoGlobal
                                + calculaTempo(filaOrigem.getMinService(), filaOrigem.getMaxService());
                        escalonador.adicionarEvento(new Evento(tipo_evento.SAIDA, tempoSaida, filaOrigem.getIndex()));
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
        double prob = gerador.NextRandom();
        boolean isExit = false;
        
        if (filaOrigem.getCustomers() >= filaOrigem.getServer()) {
            for (Map.Entry<Integer, Double> entry : filaOrigem.getFilas().entrySet()) {
                sum += entry.getValue();
                if (prob < sum) {
                    if (entry.getKey() != -1) {
                        double tempoPassagem = tempoGlobal
                                + calculaTempo(filaOrigem.getMinService(), filaOrigem.getMaxService());
                        escalonador.adicionarEvento(new Evento(tipo_evento.PASSAGEM, tempoPassagem, filaOrigem.getIndex()));
                        filaDestino = listaDeFilas.get(entry.getKey()); 
                    }
                    else {
                        double tempoSaida = tempoGlobal + calculaTempo(filaOrigem.getMinService(), filaOrigem.getMaxService());
                        escalonador.adicionarEvento(new Evento(tipo_evento.SAIDA, tempoSaida, filaOrigem.getIndex()));
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
                                escalonador.adicionarEvento(new Evento(tipo_evento.PASSAGEM, tempoPassagem, filaDestino.getIndex()));
                            }
                            else {
                                double tempoSaida = tempoGlobal + calculaTempo(filaDestino.getMinService(), filaDestino.getMaxService());
                                escalonador.adicionarEvento(new Evento(tipo_evento.SAIDA, tempoSaida, filaDestino.getIndex()));
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
        return a + ((b - a) * gerador.NextRandom());
    }

    private static void atualizarTempoEstadosFila() {
        double delta = tempoGlobal - ultimoTempo;

        for (Fila fila : listaDeFilas) {
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
        System.out.println("Clientes Totais Perdidos: " + totalPerdidos);

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