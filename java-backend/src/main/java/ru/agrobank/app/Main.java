package ru.agrobank.app;

import ru.agrobank.app.model.Farmer;
import ru.agrobank.app.model.InsuranceProduct;
import ru.agrobank.app.repository.FarmerRepository;
import ru.agrobank.app.repository.ProductRepository;
import ru.agrobank.app.service.BankService;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static FarmerRepository farmerRepo = new FarmerRepository();
    private static ProductRepository productRepo = new ProductRepository();
    private static final BankService bankService = new BankService();

    // Списки для хранения финальной истории за сессию на стороне Java
    private static final List<String> approvedHistory = new ArrayList<>();
    private static int appCounter = 0;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("🌾 Система интеграции AgroBank Java -> Python FastAPI запущена.");

        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("📊 Текущее состояние очереди Java: " + bankService.getQueueSize());
            System.out.println("-".repeat(50));
            System.out.println("1. 📝 Подать новую заявку");
            System.out.println("2. ➕ Добавить нового пользователя");
            System.out.println("3. ⚙️  Обработать рабочий день");
            System.out.println("4. 📈 Отчет по рискам (Сортировка через Stream API)");
            System.out.println("5. 🚨 Показать фермеров высокого риска (Фильтрация)");
            System.out.println("6. 📋 Вывести список всех пользователей");
            System.out.println("0. 🚪 Выход");
            System.out.println("=".repeat(50));
            System.out.print("Выберите действие: ");

            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                System.out.print("Введите ИНН фермера: ");
                String inn = scanner.nextLine().trim();
                Farmer farmer = farmerRepo.findByInn(inn);

                if (farmer == null) {
                    System.out.println("❌ Фермер с таким ИНН не найден в локальной базе!");
                    continue;
                }

                System.out.println("👨‍🌾 Найден фермер: " + farmer.getName() + " | Риск-профиль: " + farmer.getRiskLevel());
                System.out.println("Доступные продукты:");
                List<InsuranceProduct> products = productRepo.getAllProducts();
                for (InsuranceProduct p : products) {
                    System.out.printf("  %d. %s (Тариф: %.1f руб/га)\n", p.getProductId(), p.getName(), p.getRatePerHa());
                }

                System.out.print("Выберите номер продукта: ");
                try {
                    int prodId = Integer.parseInt(scanner.nextLine().trim());
                    InsuranceProduct selectedProduct = productRepo.findById(prodId);

                    if (selectedProduct != null) {
                        appCounter++;
                        bankService.addToQueue(appCounter, farmer, selectedProduct);
                        System.out.println("✅ Заявка #" + appCounter + " успешно встала в очередь ArrayDeque!");
                    } else {
                        System.out.println("❌ Неверный ID продукта.");
                    }
                } catch (Exception e) {
                    System.out.println("❌ Ошибка ввода.");
                }

            }
            else if(choice.equals("2")){
                System.out.println("\n➕ Регистрация нового фермера в системе");
                System.out.println("-".repeat(50));

                System.out.print("Введите ФИО фермера: ");
                String name = scanner.nextLine().trim();

                // Валидация ИНН в цикле, пока пользователь не введет корректный
                String inn = "";
                while (true) {
                    System.out.print("Введите ИНН (10 или 12 цифр): ");
                    inn = scanner.nextLine().trim();

                    // Регулярное выражение: \\d означает цифру, {10} или {12} — количество повторений
                    if (!inn.matches("\\d{10}") && !inn.matches("\\d{12}")) {
                        System.out.println("❌ Ошибка: ИНН должен состоять только из цифр и содержать ровно 10 или 12 символов!");
                        continue;
                    }
                    break; // Если проверка пройдена, выходим из цикла валидации ИНН
                }

                if (farmerRepo.findByInn(inn) != null) {
                    System.out.println("❌ Ошибка: Фермер с таким ИНН уже зарегистрирован!");
                    continue;
                }

                System.out.print("Введите общую площадь полей (га): ");
                double area = 0.0;
                try {
                    area = Double.parseDouble(scanner.nextLine().trim());
                    if (area <= 0) {
                        System.out.println("❌ Ошибка: Площадь должна быть больше нуля!");
                        continue;
                    }
                } catch (Exception e) {
                    System.out.println("❌ Ошибка: Неверный формат площади! Используйте точку для дробей.");
                    continue;
                }

                List<Double> yields = new ArrayList<>();
                System.out.println("📊 Введите историю урожайности (ц/га) за последние годы.");
                System.out.println("(Вводите числа по одному. Чтобы закончить, введите 'stop')");

                while (true) {
                    System.out.print("Урожайность (или 'stop'): ");
                    String input = scanner.nextLine().trim();
                    if (input.equalsIgnoreCase("stop")) {
                        break;
                    }
                    try {
                        double yld = Double.parseDouble(input);
                        if (yld < 0) {
                            System.out.println("❌ Урожайность не может быть отрицательной!");
                            continue;
                        }
                        yields.add(yld);
                    } catch (Exception e) {
                        System.out.println("❌ Неверное число! Используйте точку или введите 'stop'.");
                    }
                }

                Farmer newFarmer = new Farmer(name, inn, area, yields);
                farmerRepo.addNewFarmer(newFarmer);

                System.out.println("\n✅ Фермер успешно добавлен в локальную базу данных!");
                System.out.printf("👨‍🌾 %s | Сформированный риск-профиль Java: %s\n",
                        newFarmer.getName(), newFarmer.getRiskLevel());
            }
            else if (choice.equals("3")) {
                System.out.println("\n⚙️  Отправляем пакет заявок на сетевой скоринг в Python...");
                List<String> responses = bankService.processDayQueue(3);

                if (responses.isEmpty()) {
                    System.out.println("📭 Очередь пуста. Направлять нечего.");
                    continue;
                }

                System.out.println("-".repeat(50));
                for (String json : responses) {
                    String id = BankService.getValueFromJson(json, "app_id");
                    String status = BankService.getValueFromJson(json, "status");
                    String reason = BankService.getValueFromJson(json, "rejection_reason");

                    if (json.contains("\"is_approved\":true")) {
                        String price = BankService.getValueFromJson(json, "final_price");
                        System.out.printf("✅ Заявка #%s: ОДОБРЕНА | Расчитанная цена на Python: %s руб.\n", id, price);
                        approvedHistory.add(json); // Сохраняем успешные в историю для отчетов
                    } else {
                        System.out.printf("❌ Заявка #%s: %s | Причина: %s\n", id, status, reason);
                    }
                }
                System.out.println("-".repeat(50));

            }
            else if (choice.equals("4")) {
                if (approvedHistory.isEmpty()) {
                    System.out.println("\n📭 Нет одобренных полисов для построения отчета.");
                    continue;
                }
                System.out.println("\n📈 АНАЛИТИЧЕСКИЙ ОТЧЕТ (Сортировка по возрастанию цены через Stream API):");
                approvedHistory.stream()
                        .sorted(Comparator.comparingDouble(json -> Double.parseDouble(BankService.getValueFromJson(json, "final_price"))))
                        .forEach(json -> System.out.printf("  Заявка #%s | Стоимость: %s руб.\n",
                                BankService.getValueFromJson(json, "app_id"),
                                BankService.getValueFromJson(json, "final_price")));

            }
            else if (choice.equals("5")) {
                System.out.println("\n🚨 ФИЛЬТРАЦИЯ СИСТЕМЫ: ФЕРМЕРЫ В КРИТИЧЕСКОЙ ЗОНЕ РИСКА:");
                List<Farmer> highRisk = farmerRepo.getAllFarmers().values().stream()
                        .filter(f -> f.getRiskLevel().equals("Высокий"))
                        .collect(Collectors.toList());

                if (highRisk.isEmpty()) {
                    System.out.println("  Клиентов с высоким риском не обнаружено.");
                } else {
                    highRisk.forEach(f -> System.out.printf("  ❌ %s | ИНН: %s | Средняя урожайность: %.1f ц/га\n",
                            f.getName(), f.getInn(), f.getAverageYield()));
                }
            }
            else if(choice.equals("6")){
                farmerRepo.printAllFarmers();
            }
            else if (choice.equals("0")) {
                System.out.println("🌾 Спасибо за работу с AgroBank!");
                break;
            }

        }
    }
}