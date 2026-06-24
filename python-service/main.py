from fastapi import FastAPI, HTTPException
import random
from typing import List
from models import ScoringRequest, Farmer, FarmerSchema, ProductSchema

app = FastAPI(
    title="AgroBank Analytics Engine",
    description="Микросервис скоринга рисков и андеррайтинга для страхования урожая"
)

# --- Локальная база данных (In-Memory DB, переехавшая из data.py) ---
FARMERS_DB = {
    "1234567890": Farmer("Иван Петров", "1234567890", 120.5, [28.5, 32.1, 30.4, 29.8]),
    "9876543210": Farmer("Мария Смирнова", "9876543210", 85.0, [18.2, 16.5, 19.0, 17.8]),
    "5555555555": Farmer("Алексей Кузнецов", "5555555555", 200.0, [12.5, 14.0, 11.8, 13.2]),
    "1111111111": Farmer("Елена Васильева", "1111111111", 45.0, [35.0, 38.5, 40.2, 37.8]),
}

PRODUCTS_DB = [
    {"product_id": 1, "name": "Защита от града", "rate_per_ha": 1500.0},
    {"product_id": 2, "name": "Страхование урожая", "rate_per_ha": 2000.0},
    {"product_id": 3, "name": "Экспресс-страховка", "rate_per_ha": 1200.0},
]


# --- ЭНДПОИНТЫ API ---

@app.get("/")
def health_check():
    """Проверка доступности сервера"""
    return {"status": "online", "message": "Python-сервис аналитики AgroBank успешно запущен!"}


@app.get("/api/v1/farmers/{inn}")
def get_farmer_by_inn(inn: str):
    """Поиск фермера по ИНН для Java-клиента (Алгоритм O(1))"""
    if inn not in FARMERS_DB:
        raise HTTPException(status_code=404, detail="Фермер не найден")

    farmer = FARMERS_DB[inn]
    return {
        "name": farmer.name,
        "inn": farmer.inn,
        "farm_area": farmer.farm_area,
        "risk_level": farmer.get_risk_level(),
        "average_yield": round(farmer.get_average_yield(), 2)
    }


@app.get("/api/v1/products", response_model=List[ProductSchema])
def get_insurance_products():
    """Передача списка доступных продуктов на Java-интерфейс"""
    return PRODUCTS_DB


@app.post("/api/v1/process-application")
def process_application(request: ScoringRequest):
    """Главный алгоритм сетевого скоринга рисков"""
    farmer_data = request.farmer
    product_data = request.product

    # Создаем объект Farmer для вычисления внутренней математики
    farmer = Farmer(
        name=farmer_data.name,
        inn=farmer_data.inn,
        farm_area=farmer_data.farm_area,
        history_yield=farmer_data.history_yield
    )

    avg_yield = farmer.get_average_yield()

    # 1. Проверка жесткого лимита урожайности (Бизнес-логика скоринга)
    if avg_yield < 15:
        return {
            "app_id": request.app_id,
            "is_approved": False,
            "status": "Отклонена",
            "rejection_reason": f"Слишком низкая урожайность ({avg_yield:.1f} ц/га)",
            "final_price": 0.0,
            "farmer_name": farmer.name,
            "average_yield": round(avg_yield, 2)
        }

    # 2. Имитация динамического метеорологического риска (15% вероятность форс-мажора)
    if random.random() < 0.15:
        return {
            "app_id": request.app_id,
            "is_approved": False,
            "status": "Отклонена",
            "rejection_reason": "Прогнозируется высокий риск града (Аналитика Python)",
            "final_price": 0.0,
            "farmer_name": farmer.name,
            "average_yield": round(avg_yield, 2)
        }

    # 3. Расчет стоимости страхового полиса при успешном прохождении проверок
    final_price = round(product_data.rate_per_ha * farmer_data.farm_area, 2)

    return {
        "app_id": request.app_id,
        "is_approved": True,
        "status": "Одобрена",
        "rejection_reason": "",
        "final_price": final_price,
        "farmer_name": farmer.name,
        "average_yield": round(avg_yield, 2)
    }