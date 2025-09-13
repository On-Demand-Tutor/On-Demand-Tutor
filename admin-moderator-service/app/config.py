import os
from dotenv import load_dotenv

load_dotenv()

class Config:
    DB_HOST = os.getenv("DB_HOST", "mysql")
    DB_PORT = os.getenv("DB_PORT", "3306")
    DB_NAME = os.getenv("DB_NAME", "AdminDB")
    DB_USER = os.getenv("DB_USER", "root")
    DB_PASSWORD = os.getenv("DB_PASSWORD", "123456")
    
    USER_SERVICE_URL = os.getenv("USER_SERVICE_URL", "http://user-service:8080")
    
    ADMIN_EMAIL = os.getenv("ADMIN_EMAIL", "adminadmin@gmail.com")
    ADMIN_PASSWORD = os.getenv("ADMIN_PASSWORD", "Strong@123")
    
    JWT_SECRET_KEY = os.getenv("JWT_SECRET_KEY", "llOf5biHJ32ZeeIZZ4XiHsCg3JM2UNBxMqXhxGxVrMEoo5hmi4C+hJZ9hO0T5MP6")
    JWT_ALGORITHM = "HS512"
    
    @property
    def DATABASE_URL(self):
        return f"mysql+pymysql://{self.DB_USER}:{self.DB_PASSWORD}@{self.DB_HOST}:{self.DB_PORT}/{self.DB_NAME}?charset=utf8mb4"
    
    @property 
    def DATABASE_URL_WITHOUT_DB(self):
        return f"mysql+pymysql://{self.DB_USER}:{self.DB_PASSWORD}@{self.DB_HOST}:{self.DB_PORT}/"

config = Config()