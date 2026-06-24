package ru.agrobank.app.service;

import ru.agrobank.app.model.Farmer;
import ru.agrobank.app.model.InsuranceProduct;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class BankService {

    // Внутренний объект для хранения элементов в очереди
    public static class QueuedApplication {
        public int appId;
        public Farmer farmer;
        public InsuranceProduct product;

        public QueuedApplication(int appId, Farmer farmer, InsuranceProduct product) {
            this.appId = appId;
            this.farmer = farmer;
            this.product = product;
        }
    }

    // Двусторонняя очередь согласно ТЗ хакатона
    private final Queue<QueuedApplication> queue = new ArrayDeque<>();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private static final String PYTHON_URL = "http://127.0.0.1:8000/api/v1/process-application";

    public void addToQueue(int appId, Farmer farmer, InsuranceProduct product) {
        queue.add(new QueuedApplication(appId, farmer, product));
    }

    public int getQueueSize() {
        return queue.size();
    }

    // Обработка очереди дня: Java упаковывает данные в JSON и шлет по сети на Python
    public List<String> processDayQueue(int maxProcess) {
        List<String> results = new ArrayList<>();
        int count = 0;

        while (!queue.isEmpty() && count < maxProcess) {
            QueuedApplication app = queue.poll();
            try {
                // Преобразуем массив истории урожайности в строку JSON вида [28.5, 32.1...]
                String historyYieldJson = app.farmer.getHistoryYield().toString();

                // Собираем JSON-строку для FastAPI с использованием Locale.US, чтобы разделителем дробей была точка
                String jsonPayload = String.format(Locale.US,
                        "{\"app_id\":%d,\"farmer\":{\"name\":\"%s\",\"inn\":\"%s\",\"farm_area\":%.2f,\"history_yield\":%s}," +
                                "\"product\":{\"product_id\":%d,\"name\":\"%s\",\"rate_per_ha\":%.2f}}",
                        app.appId, app.farmer.getName(), app.farmer.getInn(), app.farmer.getFarmArea(), historyYieldJson,
                        app.product.getProductId(), app.product.getName(), app.product.getRatePerHa()
                );

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(PYTHON_URL))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                results.add(response.body());

            } catch (Exception e) {
                // Если питон выключен, аккуратно имитируем JSON-ответ с ошибкой
                results.add(String.format("{\"app_id\":%d,\"status\":\"Ошибка\",\"is_approved\":false,\"rejection_reason\":\"Python-сервер оффлайн\"}", app.appId));
            }
            count++;
        }
        return results;
    }

    // --- УМНЫЙ СЕЙФ-ПАРСЕР JSON (ДЛЯ РАБОТЫ С СЕТЕВЫМИ СТРОКАМИ БЕЗ БИБЛИОТЕК) ---
    public static String getValueFromJson(String json, String key) {
        if (json == null || !json.contains("\"" + key + "\"")) return "";
        try {
            String[] parts = json.split("\"" + key + "\":");
            String valuePart = parts[1].split(",")[0].replace("}", "").replace("]", "").trim();
            if (valuePart.startsWith("\"")) {
                valuePart = valuePart.substring(1, valuePart.length() - 1);
            }
            return valuePart;
        } catch (Exception e) {
            return "";
        }
    }
}