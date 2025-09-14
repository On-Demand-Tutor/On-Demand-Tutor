from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
import logging

from app.database import engine, Base
from app.db_init import wait_for_mysql, create_database_if_not_exists
from app.routers import user_router, tutor_router, student_router, payment_router
from app.services.auth_service import auth_service

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Starting Admin Moderator Service...")
    wait_for_mysql()
    create_database_if_not_exists()
    Base.metadata.create_all(bind=engine)
    logger.info("Database tables created")
    try:
        auth_service.login_admin()
        logger.info("Admin auto-login successful")
    except Exception as e:
        logger.error(f"Admin auto-login failed: {str(e)}")
    
    yield
    logger.info("Shutting down Admin Moderator Service...")


app = FastAPI(
    title="Admin Moderator Service",
    description="Service quản lý admin và moderator với khả năng quản lý dịch vụ và thống kê người dùng",
    version="1.0.0",
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:4200"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


app.include_router(user_router, prefix="/api")
app.include_router(tutor_router, prefix="/api")
app.include_router(student_router, prefix="/api")
app.include_router(payment_router, prefix="/api")

@app.get("/")
def root():
    return {
        "message": "Welcome to Admin Moderator Service",
        "version": "1.0.0",
        "status": "running"
    }


