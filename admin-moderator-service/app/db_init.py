import logging
import time
from sqlalchemy import create_engine, text
from app.config import config

logger = logging.getLogger(__name__)

def create_database_if_not_exists():
    """Tự động tạo database nếu chưa tồn tại"""
    max_retries = 30
    retry_delay = 2
    
    for attempt in range(max_retries):
        try:
            # Kết nối đến MySQL server (không chỉ định database)
            engine = create_engine(
                config.DATABASE_URL_WITHOUT_DB,
                pool_pre_ping=True,
                pool_recycle=300
            )
            
            with engine.connect() as conn:
                # Kiểm tra database có tồn tại không
                result = conn.execute(text(f"SHOW DATABASES LIKE '{config.DB_NAME}'"))
                db_exists = result.fetchone() is not None
                
                if not db_exists:
                    # Tạo database mới
                    conn.execute(text(f"CREATE DATABASE {config.DB_NAME} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"))
                    logger.info(f"Database '{config.DB_NAME}' created successfully")
                else:
                    logger.info(f"Database '{config.DB_NAME}' already exists")
                
                conn.commit()
            
            engine.dispose()
            return True
            
        except Exception as e:
            if attempt < max_retries - 1:
                logger.warning(f"Database connection attempt {attempt + 1} failed: {str(e)}")
                logger.info(f"Retrying in {retry_delay} seconds...")
                time.sleep(retry_delay)
            else:
                logger.error(f"Failed to create database after {max_retries} attempts: {str(e)}")
                raise
    
    return False

def wait_for_mysql():
    """Đợi MySQL server sẵn sàng"""
    max_retries = 30
    retry_delay = 2
    
    logger.info("Waiting for MySQL server to be ready...")
    
    for attempt in range(max_retries):
        try:
            engine = create_engine(
                config.DATABASE_URL_WITHOUT_DB,
                pool_pre_ping=True,
                connect_args={"connect_timeout": 5}
            )
            
            with engine.connect() as conn:
                conn.execute(text("SELECT 1"))
            
            engine.dispose()
            logger.info("MySQL server is ready!")
            return True
            
        except Exception as e:
            if attempt < max_retries - 1:
                logger.info(f"MySQL not ready (attempt {attempt + 1}/{max_retries})")
                time.sleep(retry_delay)
            else:
                logger.error(f"MySQL server not available after {max_retries} attempts")
                raise
    
    return False