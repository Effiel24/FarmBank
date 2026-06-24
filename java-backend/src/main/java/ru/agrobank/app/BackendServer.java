package ru.agrobank.app;

import ru.agrobank.app.handler.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class BackendServer {
    public static void main(String[] args) throws IOException {
        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(8080), 0);
        
        server.createContext("/api/v1/farmers", new FarmersHandler());
        server.createContext("/api/v1/products", new ProductsHandler());
        server.createContext("/api/v1/process-application", new ProcessApplicationHandler());
        server.createContext("/", new HealthCheckHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("🌾 Java Backend HTTP Server запущен на http://localhost:8080");
    }
}
