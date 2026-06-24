Запуск Streamlit-фронтенда для AgroBank

1) Убедитесь, что запущен python-service (FastAPI) в корне репозитория:
   cd python-service
   uvicorn main:app --reload --port 8000

2) Установите зависимости и запустите Streamlit:
   cd streamlit-frontend
   python -m pip install -r requirements.txt
   streamlit run app.py

3) В боковой панели укажите URL сервиса (по умолчанию http://localhost:8000), выберите ИНН фермера и отправьте заявку.
