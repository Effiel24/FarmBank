package ru.agrobank.app.model;

import java.util.List;

public class Farmer extends Person {
    private double farmArea;
    private List<Double> historyYield;

    public Farmer(String name, String inn, double farmArea, List<Double> historyYield) {
        super(name, inn);
        this.farmArea = farmArea;
        this.historyYield = historyYield;
    }

    public double getFarmArea() { return farmArea; }
    public List<Double> getHistoryYield() { return historyYield; }

    public double getAverageYield() { // расчет средней урожайности Фермера
        if (historyYield == null || historyYield.isEmpty()) return 0.0;
        double sum = 0;
        for (double y : historyYield) {
            sum += y;
        }
        return sum / historyYield.size();
    }

    public String getRiskLevel() {
        double avg = getAverageYield();
        if (avg >= 30) return "Низкий";
        else if (avg >= 20) return "Средний";
        else return "Высокий";
    }

    @Override
    public String toString() {
        return "Farmer{" +
                name + '\'' +
                ", История урожайности=" + historyYield +
                ", Область грядки=" + farmArea +
                '}';
    }
}