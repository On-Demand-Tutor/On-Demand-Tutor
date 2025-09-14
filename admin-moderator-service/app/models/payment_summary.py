from sqlalchemy import Column, Integer, String, Text, DateTime, Boolean, Date, Numeric, JSON
from sqlalchemy.sql import func
from app.database import Base

class PaymentDailySummary(Base):
    __tablename__ = "payment_daily_summaries"
    
    id = Column(Integer, primary_key=True, index=True)
    summary_date = Column(Date, nullable=False, unique=True, index=True)
    total_amount = Column(Numeric(15, 2), default=0, nullable=False)
    total_transactions = Column(Integer, default=0, nullable=False)
    
    # JSON fields for detailed breakdown
    payments_by_status = Column(JSON, nullable=True)  # {"COMPLETED": 1000000, "PENDING": 50000}
    payments_by_tutor = Column(JSON, nullable=True)   # {"1": 500000, "2": 300000}
    payments_by_student = Column(JSON, nullable=True) # {"1": 100000, "2": 200000}
    
    # Metadata
    created_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)
    updated_at = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now(), nullable=False)
    generated_by = Column(String(50), default="admin-service", nullable=False)
    
    # Additional stats
    average_transaction_amount = Column(Numeric(15, 2), default=0)
    max_transaction_amount = Column(Numeric(15, 2), default=0)
    min_transaction_amount = Column(Numeric(15, 2), default=0)
    
    def __repr__(self):
        return f"<PaymentDailySummary(date={self.summary_date}, amount={self.total_amount}, transactions={self.total_transactions})>"