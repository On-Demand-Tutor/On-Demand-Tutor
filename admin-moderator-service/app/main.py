from fastapi import FastAPI
from .database import engine, Base
from .routers import services, violations, dashboard, users, tutors

app = FastAPI(title="Admin & Moderation Service")
Base.metadata.create_all(bind=engine)
app.include_router(services.router)
app.include_router(violations.router)
app.include_router(dashboard.router)
app.include_router(users.router)
app.include_router(tutors.router)

@app.get("/")
def root():
    return {"message": "Admin & Moderation Service is running"}