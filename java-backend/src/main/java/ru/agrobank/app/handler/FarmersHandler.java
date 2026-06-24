package ru.agrobank.app.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.agrobank.app.repository.FarmerRepository;
import ru.agrobank.app.model.Farmer;
import java.io.IOException;

public class FarmersHandler implements HttpHandler {
    private FarmerRepository farmerRepo = new FarmerRepository();
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        
        if (parts.length >= 5) {
            String inn = parts[4];
            Farmer farmer = farmerRepo.findByInn(inn);
            
            if (farmer != null) {
                String json = String.format(
                    "{\"name\": \"%s\", \"inn\": \"%s\", \"farm_area\": %.1f, \"history_yield\": [%s], \"risk_level\": \"%s\", \"average_yield\": %.2f}",
                    farmer.getName(),
                    farmer.getInn(),
                    farmer.getFarmArea(),
                    formatArray(farmer.getHistoryYield()),
                    farmer.getRiskLevel(),
                    farmer.getAverageYield()
                );
                exchange.sendResponseHeaders(200, json.getBytes().length);
                exchange.getResponseBody().write(json.getBytes());
            } else {
                String error = "{\"error\": \"Фермер не найден\"}";
                exchange.sendResponseHeaders(404, error.getBytes().length);
                exchange.getResponseBody().write(error.getBytes());
            }
        } else {
            String error = "{\"error\": \"Неверный запрос\"}";
            exchange.sendResponseHeaders(400, error.getBytes().length);
            exchange.getResponseBody().write(error.getBytes());
        }
        exchange.close();
    }
    
    private String formatArray(java.util.List<Double> yields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < yields.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(yields.get(i));
        }
        return sb.toString();
    }
}
