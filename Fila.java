import java.util.ArrayList;
import java.util.HashMap;

public class Fila {
    private int index;
    private int server;
    private int capacity;
    private double minArrival;
    private double maxArrival;
    private double minService;
    private double maxService;
    private int customers;
    private int loss;
    private double[] times;
    private HashMap<Integer, Double> possiveisProximasFilas;

    public Fila(int server, int capacity, double minArrival, double maxArrival,
                double minService, double maxService, HashMap<Integer, Double> possiveisProximasFilas, int index) {
        this.index = index;
        this.server = server;
        this.capacity = capacity;
        this.minArrival = minArrival;
        this.maxArrival = maxArrival;
        this.minService = minService;
        this.maxService = maxService;
        this.customers = 0;
        this.loss = 0;
        this.times = new double [capacity+1];
        this.possiveisProximasFilas = possiveisProximasFilas;
    }

    public HashMap<Integer, Double> getFilas() {
        return possiveisProximasFilas;
    }

    public int getIndex() {
        return index;
    }

    public int getServer() {
        return server;
    }

    public void setServer(int server) {
        this.server = server;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public double getMinArrival() {
        return minArrival;
    }

    public void setMinArrival(double minArrival) {
        this.minArrival = minArrival;
    }

    public double getMaxArrival() {
        return maxArrival;
    }

    public void setMaxArrival(double maxArrival) {
        this.maxArrival = maxArrival;
    }

    public double getMinService() {
        return minService;
    }

    public void setMinService(double minService) {
        this.minService = minService;
    }

    public double getMaxService() {
        return maxService;
    }

    public void setMaxService(double maxService) {
        this.maxService = maxService;
    }

    public int getCustomers() {
        return customers;
    }

    public void setCustomers(int customers) {
        this.customers = customers;
    }

    public int getLoss() {
        return loss;
    }

    public void loss() {
        this.loss++;
    }

    public double[] getTimes() {
        return times;
    }

    public void setTime(int pos, double val) {
        this.times[pos] += val;
    }

    public void in() {
        this.customers++;
    }

    public void out() {
        this.customers--;
    }
}
