from typing import List
from pydantic import BaseModel, Field

# --- 1. Базовые ООП-классы (Бизнес-логика) ---

class Person:
    def __init__(self, name: str, inn: str):
        self.name = name
        self.inn = inn

class Farmer(Person):
    def __init__(self, name: str, inn: str, farm_area: float, history_yield: List[float]):
        super().__init__(name, inn)
        self.farm_area = farm_area
        self.history_yield = history_yield

    def get_average_yield(self) -> float:
        if not self.history_yield:
            return 0.0
        return sum(self.history_yield) / len(self.history_yield)

    def get_risk_level(self) -> str:
        avg = self.get_average_yield()
        if avg >= 30:
            return "Низкий"
        elif avg >= 20:
            return "Средний"
        else:
            return "Высокий"


# Обмен данными с Java backend

class FarmerSchema(BaseModel):
    name: str
    inn: str
    farm_area: float
    history_yield: List[float]

class ProductSchema(BaseModel):
    product_id: int
    name: str
    rate_per_ha: float

class ScoringRequest(BaseModel):
    app_id: int
    farmer: FarmerSchema
    product: ProductSchema