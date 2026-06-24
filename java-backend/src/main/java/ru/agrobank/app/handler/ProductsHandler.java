package ru.agrobank.app.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.agrobank.app.repository.ProductRepository;
import ru.agrobank.app.model.InsuranceProduct;
import java.io.IOException;
import java.util.List;

public class ProductsHandler implements HttpHandler {
    private ProductRepository productRepo = new ProductRepository();
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        
        List<InsuranceProduct> products = productRepo.getAllProducts();
        StringBuilder json = new StringBuilder("[");
        
        for (int i = 0; i < products.size(); i++) {
            InsuranceProduct p = products.get(i);
            if (i > 0) json.append(",");
            json.append("{\"product_id\": ").append(p.getProductId())
                .append(", \"name\": \"").append(p.getName())
                .append("\", \"rate_per_ha\": ").append((int)p.getRatePerHa()).append("}");
        }
        json.append("]");
        
        String response = json.toString();
        exchange.sendResponseHeaders(200, response.getBytes().length);
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }
}
