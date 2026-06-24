package ru.agrobank.app.repository;

import ru.agrobank.app.model.Farmer;
import java.util.*;

public class FarmerRepository {
    private final Map<String, Farmer> farmersDb = new HashMap<>();

    public FarmerRepository() {
        // Заполняем базу данных фермеров (данные из вашего data.py)
        farmersDb.put("1234567890", new Farmer("Иван Петров", "1234567890", 120.5, Arrays.asList(28.5, 32.1, 30.4, 29.8)));
        farmersDb.put("9876543210", new Farmer("Мария Смирнова", "9876543210", 85.0, Arrays.asList(18.2, 16.5, 19.0, 17.8)));
        farmersDb.put("5555555555", new Farmer("Алексей Кузнецов", "5555555555", 200.0, Arrays.asList(12.5, 14.0, 11.8, 13.2)));
        farmersDb.put("1111111111", new Farmer("Елена Васильева", "1111111111", 45.0, Arrays.asList(35.0, 38.5, 40.2, 37.8)));
    }

    public Farmer findByInn(String inn) {
        return farmersDb.get(inn);
    }

    public Map<String, Farmer> getAllFarmers() {
        return farmersDb;
    }
    public void printAllFarmers(){
        Set<String> iins = farmersDb.keySet();
        for(String iin : iins){
            System.out.println(iin + " " + farmersDb.get(iin));
        }
    }

    public void addNewFarmer(Farmer farmer){
        farmersDb.put(farmer.getInn(), farmer);
    }

}