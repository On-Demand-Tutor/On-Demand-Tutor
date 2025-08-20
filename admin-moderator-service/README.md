admin_moderation_service/
├── app/
│   ├── __init__.py
│   ├── main.py          # Ứng dụng FastAPI chính
│   ├── database.py      # Kết nối DB
│   ├── models.py        # Models SQLAlchemy
│   ├── schemas.py       # Schemas Pydantic
│   ├── crud.py          # Logic CRUD
│   └── routers/         # Các router cho API (tách riêng cho sạch)
│       ├── __init__.py
│       ├── services.py
│       ├── violations.py
│       ├── dashboard.py
│       ├── users.py
│       └── tutors.py
├── Dockerfile           # Docker cho app
├── docker-compose.yml   # Compose cho app + DB
└── requirements.txt     # Dependencies