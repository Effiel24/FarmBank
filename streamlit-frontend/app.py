import streamlit as st
import requests
import random

st.set_page_config(page_title="AgroBank — Streamlit Frontend", layout="centered")

st.title("AgroBank — Интерактивный фронтенд (Streamlit)")

BASE_URL = st.sidebar.text_input("Java Backend URL", "http://localhost:8080")

# Известные тестовые ИНН (возьмутся из локальной БД python-service)
sample_inns = ["1234567890", "9876543210", "5555555555", "1111111111"]
inn_choice = st.sidebar.selectbox("Выберите ИНН фермера", sample_inns)

if st.sidebar.button("Загрузить данные фермера"):
    try:
        r = requests.get(f"{BASE_URL}/api/v1/farmers/{inn_choice}")
        r.raise_for_status()
        farmer_data = r.json()
        st.session_state['farmer_data'] = farmer_data
    except Exception as e:
        st.error(f"Не удалось получить данные фермера: {e}")

farmer_data = st.session_state.get('farmer_data') if 'farmer_data' in st.session_state else None

st.header("Данные фермера / Ввод вручную")
if farmer_data:
    st.write(farmer_data)
    name = farmer_data.get('name')
    inn = farmer_data.get('inn')
    farm_area = st.number_input("Площадь (га)", value=float(farmer_data.get('farm_area', 0.0)))
    history_yield = st.text_input("История урожайности (через запятую)", ",".join([str(v) for v in farmer_data.get('history_yield', [])]))
else:
    name = st.text_input("Имя фермера", "")
    inn = st.text_input("ИНН", inn_choice)
    farm_area = st.number_input("Площадь (га)", value=10.0)
    history_yield = st.text_input("История урожайности (через запятую)", "20,22,19")

# Парсинг истории урожайности
try:
    history_list = [float(x.strip()) for x in history_yield.split(",") if x.strip()]
except Exception:
    history_list = []
    st.warning("Неверный формат истории урожайности; ожидаются числа через запятую")

st.header("Выбор страхового продукта")
# Загрузка продуктов
products = []
try:
    r = requests.get(f"{BASE_URL}/api/v1/products", timeout=3)
    r.raise_for_status()
    products = r.json()
except Exception as e:
    st.warning(f"Не удалось загрузить продукты: {e}")

product_map = {p['name']: p for p in products} if products else {}
product_names = list(product_map.keys()) if product_map else ["(нет данных)"]
selected_product_name = st.selectbox("Продукт", product_names)
selected_product = product_map.get(selected_product_name)

st.write("---")
if st.button("Отправить заявку на скоринг"):
    if not name or not inn or not selected_product:
        st.error("Заполните имя, ИНН и выберите продукт")
    else:
        payload = {
            "app_id": random.randint(1000, 9999),
            "farmer": {
                "name": name,
                "inn": inn,
                "farm_area": float(farm_area),
                "history_yield": history_list
            },
            "product": selected_product
        }
        try:
            r = requests.post(f"{BASE_URL}/api/v1/process-application", json=payload, timeout=5)
            r.raise_for_status()
            result = r.json()
            st.success(f"Статус: {result.get('status')}")
            st.json(result)
        except Exception as e:
            st.error(f"Ошибка при отправке заявки: {e}")

st.write('\n')
st.caption("Примечание: для корректной работы убедитесь, что Java Backend запущен на 8080, а Python сервис на 8000.")
