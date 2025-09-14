import logging
from datetime import datetime, timedelta, date
from typing import Dict, Any, List, Optional
from sqlalchemy.orm import Session
from app.database import SessionLocal
from app.services.payment_service import payment_service
from app.crud.payment_summary import payment_summary_crud
from statistics import mean

logger = logging.getLogger(__name__)

class DailySummaryService:
    def __init__(self):
        pass
        
    def _get_db(self) -> Session:
        return SessionLocal()
    
    def generate_and_save_daily_summary(self, target_date: datetime) -> Dict[str, Any]:
        db = self._get_db()
        try:
            payment_summary = payment_service.calculate_daily_summary(target_date)
            
            if not payment_summary or payment_summary.get("total_amount", 0) == 0:
                summary_data = {
                    "date": target_date.date(),
                    "total_amount": 0,
                    "total_transactions": 0,
                    "payments_by_status": {},
                    "payments_by_tutor": {},
                    "payments_by_student": {},
                    "generated_by": "admin-moderator-service",
                    "average_transaction_amount": 0,
                    "max_transaction_amount": 0,
                    "min_transaction_amount": 0
                }
            else:
                payments = payment_service.get_payments_by_date_range(
                    target_date.replace(hour=0, minute=0, second=0, microsecond=0),
                    target_date.replace(hour=23, minute=59, second=59, microsecond=999999),
                    status="COMPLETED"
                )
                amounts = []
                for payment in payments:
                    amount = payment.get("amount", 0) or payment.get("totalAmount", 0)
                    if amount and amount > 0:
                        amounts.append(float(amount))
                
                additional_stats = {
                    "average_transaction_amount": mean(amounts) if amounts else 0,
                    "max_transaction_amount": max(amounts) if amounts else 0,
                    "min_transaction_amount": min(amounts) if amounts else 0
                }

                summary_data = {
                    "date": target_date.date(),
                    "total_amount": payment_summary.get("total_amount", 0),
                    "total_transactions": payment_summary.get("total_transactions", 0),
                    "payments_by_status": payment_summary.get("payments_by_status", {}),
                    "payments_by_tutor": payment_summary.get("payments_by_tutor", {}),
                    "payments_by_student": payment_summary.get("payments_by_student", {}),
                    "generated_by": "admin-moderator-service",
                    **additional_stats
                }
            
            db_summary = payment_summary_crud.upsert_daily_summary(db, summary_data)
            
            return {
                "id": db_summary.id,
                "date": db_summary.summary_date.isoformat(),
                "total_amount": float(db_summary.total_amount),
                "total_transactions": db_summary.total_transactions,
                "payments_by_status": db_summary.payments_by_status,
                "payments_by_tutor": db_summary.payments_by_tutor,
                "payments_by_student": db_summary.payments_by_student,
                "average_transaction_amount": float(db_summary.average_transaction_amount or 0),
                "max_transaction_amount": float(db_summary.max_transaction_amount or 0),
                "min_transaction_amount": float(db_summary.min_transaction_amount or 0),
                "created_at": db_summary.created_at.isoformat(),
                "updated_at": db_summary.updated_at.isoformat(),
                "saved": True,
                "saved_to": "database"
            }
            
        except Exception as e:
            return {
                "date": target_date.date().isoformat(),
                "error": str(e),
                "generated_at": datetime.now().isoformat(),
                "saved": False
            }
        finally:
            db.close()
    
    def get_daily_summary(self, target_date: date) -> Optional[Dict[str, Any]]:
        db = self._get_db()
        try:
            db_summary = payment_summary_crud.get_by_date(db, target_date)
            
            if not db_summary:
                return None
            
            return {
                "id": db_summary.id,
                "date": db_summary.summary_date.isoformat(),
                "total_amount": float(db_summary.total_amount),
                "total_transactions": db_summary.total_transactions,
                "payments_by_status": db_summary.payments_by_status or {},
                "payments_by_tutor": db_summary.payments_by_tutor or {},
                "payments_by_student": db_summary.payments_by_student or {},
                "average_transaction_amount": float(db_summary.average_transaction_amount or 0),
                "max_transaction_amount": float(db_summary.max_transaction_amount or 0),
                "min_transaction_amount": float(db_summary.min_transaction_amount or 0),
                "created_at": db_summary.created_at.isoformat(),
                "updated_at": db_summary.updated_at.isoformat(),
                "source": "database"
            }
            
        except Exception as e:
            return None
        finally:
            db.close()
    
    def get_or_create_daily_summary(self, target_date: datetime) -> Dict[str, Any]:
        date_only = target_date.date()
        existing_summary = self.get_daily_summary(date_only)
        
        if existing_summary:
            return existing_summary
        return self.generate_and_save_daily_summary(target_date)
    
    def get_summaries_for_period(self, start_date: date, end_date: date) -> List[Dict[str, Any]]:
        db = self._get_db()
        try:
            db_summaries = payment_summary_crud.get_by_date_range(db, start_date, end_date)
            
            return [
                {
                    "id": summary.id,
                    "date": summary.summary_date.isoformat(),
                    "total_amount": float(summary.total_amount),
                    "total_transactions": summary.total_transactions,
                    "payments_by_status": summary.payments_by_status or {},
                    "payments_by_tutor": summary.payments_by_tutor or {},
                    "payments_by_student": summary.payments_by_student or {},
                    "average_transaction_amount": float(summary.average_transaction_amount or 0),
                    "max_transaction_amount": float(summary.max_transaction_amount or 0),
                    "min_transaction_amount": float(summary.min_transaction_amount or 0),
                    "created_at": summary.created_at.isoformat(),
                    "updated_at": summary.updated_at.isoformat()
                }
                for summary in db_summaries
            ]
            
        except Exception as e:
            return []
        finally:
            db.close()
    
    def get_monthly_aggregation(self, year: int, month: int) -> Dict[str, Any]:
        db = self._get_db()
        try:
            monthly_data = payment_summary_crud.get_monthly_totals(db, year, month)
            summaries = [
                {
                    "date": summary.summary_date.isoformat(),
                    "total_amount": float(summary.total_amount),
                    "total_transactions": summary.total_transactions
                }
                for summary in monthly_data["summaries"]
            ]

            valid_summaries = [s for s in summaries if s["total_amount"] > 0]
            best_day = max(valid_summaries, key=lambda x: x["total_amount"]) if valid_summaries else None
            worst_day = min(valid_summaries, key=lambda x: x["total_amount"]) if valid_summaries else None
            
            return {
                "month": monthly_data["month"],
                "total_amount": monthly_data["total_amount"],
                "total_transactions": monthly_data["total_transactions"],
                "days_with_transactions": monthly_data["days_with_data"],
                "total_days": len(summaries),
                "average_daily_amount": monthly_data["average_daily_amount"],
                "best_day": best_day,
                "worst_day": worst_day,
                "daily_summaries": summaries,
                "generated_at": datetime.now().isoformat(),
                "source": "database"
            }
            
        except Exception as e:
            return {"error": str(e)}
        finally:
            db.close()
    
    def get_top_tutors_by_period(self, start_date: date, end_date: date, limit: int = 10) -> List[Dict[str, Any]]:
        db = self._get_db()
        try:
            top_tutors = payment_summary_crud.get_top_tutors_by_period(db, start_date, end_date, limit)
            return top_tutors
        except Exception as e:
            return []
        finally:
            db.close()
    
    def get_latest_summaries(self, limit: int = 30) -> List[Dict[str, Any]]:
        db = self._get_db()
        try:
            db_summaries = payment_summary_crud.get_latest(db, limit)
            
            logger.info(f"Retrieved {len(db_summaries)} latest summaries from database")
            
            return [
                {
                    "id": summary.id,
                    "date": summary.summary_date.isoformat(),
                    "total_amount": float(summary.total_amount),
                    "total_transactions": summary.total_transactions,
                    "average_transaction_amount": float(summary.average_transaction_amount or 0),
                    "created_at": summary.created_at.isoformat()
                }
                for summary in db_summaries
            ]      
        except Exception as e:
            return []
        finally:
            db.close()
            
daily_summary_service = DailySummaryService()