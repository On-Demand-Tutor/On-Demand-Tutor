from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from .database import engine, Base
from .routers import services, violations, dashboard, users, tutors



app = FastAPI(title="Admin & Moderation Service")
Base.metadata.create_all(bind=engine)

# Bật CORS cho phép frontend truy cập
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Có thể thay bằng domain frontend nếu muốn bảo mật hơn
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(services.router)
app.include_router(violations.router)
app.include_router(dashboard.router)
app.include_router(users.router)
app.include_router(tutors.router)


@app.get("/")
def root():
    return {"message": "Admin & Moderation Service is running"}

