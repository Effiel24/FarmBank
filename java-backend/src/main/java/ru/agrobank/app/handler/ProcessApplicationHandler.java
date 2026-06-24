package ru.agrobank.app.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.agrobank.app.service.BankService;
import ru.agrobank.app.repository.FarmerRepository;
import ru.agrobank.app.repository.ProductRepository;
import ru.agrobank.app.model.Farmer;
import ru.agrobank.app.model.InsuranceProduct;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class ProcessApplicationHandler implements HttpHandler {
    private FarmerRepository farmerRepo = new FarmerRepository();
    private ProductRepository productRepo = new ProductRepository();
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        
        if (!exchange.getRequestMethod().equals("POST")) {
            String error = "{\"error\": \"Только POST запросы разрешены\"}";
            exchange.sendResponseHeaders(405, error.getBytes().length);
            exchange.getResponseBody().write(error.getBytes());
            exchange.close();
            return;
        }
        
        StringBuilder requestBody = new StringBuilder();
        try (Scanner scanner = new Scanner(exchange.getRequestBody())) {
            while (scanner.hasNextLine()) {
                requestBody.append(scanner.nextLine());
            }
        }
        
        String jsonRequest = requestBody.toString();
        
        // Отправляем заявку на Python сервис для скоринга
        String pythonResponse = forwardToPython(jsonRequest);
        
        exchange.sendResponseHeaders(200, pythonResponse.getBytes().length);
        exchange.getResponseBody().write(pythonResponse.getBytes());
        exchange.close();
    }
    
    private String forwardToPython(String jsonRequest) {
        try {
            URL url = new URL("http://localhost:8000/api/v1/process-application");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonRequest.getBytes());
                os.flush();
            }
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                return readResponse(conn.getInputStream());
            } else {
                return "{\"error\": \"Python сервис вернул ошибку " + responseCode + "\"}";
            }
        } catch (Exception e) {
            return "{\"error\": \"Не удалось подключиться к Python сервису: " + e.getMessage() + "\"}";
        }
    }
    
    private String readResponse(InputStream inputStream) throws IOException {
        StringBuilder response = new StringBuilder();
        try (Scanner scanner = new Scanner(inputStream)) {
            while (scanner.hasNextLine()) {
                response.append(scanner.nextLine());
            }
        }
        return response.toString();
    }
}
