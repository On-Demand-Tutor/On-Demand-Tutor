from sqlalchemy.orm import Session
from sqlalchemy import and_, desc, asc
from typing import List, Optional, Dict, Any
from datetime import date, datetime
from app.models.payment_summary import PaymentDailySummary
from decimal import Decimal

class PaymentSummaryCRUD:
    def create_daily_summary(
        self, 
        db: Session, 
        summary_data: Dict[str, Any]
    ) -> PaymentDailySummary:
        """Tạo daily summary mới"""
        db_summary = PaymentDailySummary(
            summary_date=summary_data["date"],
            total_amount=summary_data.get("total_amount", 0),
            total_transactions=summary_data.get("total_transactions", 0),
            payments_by_status=summary_data.get("payments_by_status", {}),
            payments_by_tutor=summary_data.get("payments_by_tutor", {}),
            payments_by_student=summary_data.get("payments_by_student", {}),
            average_transaction_amount=summary_data.get("average_transaction_amount", 0),
            max_transaction_amount=summary_data.get("max_transaction_amount", 0),
            min_transaction_amount=summary_data.get("min_transaction_amount", 0),
            generated_by=summary_data.get("generated_by", "admin-service")
        )
        
        db.add(db_summary)
        db.commit()
        db.refresh(db_summary)
        return db_summary
    
    def get_by_date(self, db: Session, summary_date: date) -> Optional[PaymentDailySummary]:
        """Lấy summary theo ngày"""
        return db.query(PaymentDailySummary).filter(
            PaymentDailySummary.summary_date == summary_date
        ).first()
    
    def get_by_date_range(
        self, 
        db: Session, 
        start_date: date, 
        end_date: date
    ) -> List[PaymentDailySummary]:
        """Lấy summaries trong khoảng thời gian"""
        return db.query(PaymentDailySummary).filter(
            and_(
                PaymentDailySummary.summary_date >= start_date,
                PaymentDailySummary.summary_date <= end_date
            )
        ).order_by(PaymentDailySummary.summary_date).all()
    
    def get_latest(self, db: Session, limit: int = 30) -> List[PaymentDailySummary]:
        """Lấy các summaries gần nhất"""
        return db.query(PaymentDailySummary).order_by(
            desc(PaymentDailySummary.summary_date)
        ).limit(limit).all()
    
    def update_summary(
        self, 
        db: Session, 
        summary_date: date, 
        update_data: Dict[str, Any]
    ) -> Optional[PaymentDailySummary]:
        """Cập nhật summary"""
        db_summary = self.get_by_date(db, summary_date)
        
        if db_summary:
            for field, value in update_data.items():
                if hasattr(db_summary, field):
                    setattr(db_summary, field, value)
            
            db.commit()
            db.refresh(db_summary)
        
        return db_summary
    
    def upsert_daily_summary(
        self, 
        db: Session, 
        summary_data: Dict[str, Any]
    ) -> PaymentDailySummary:
        """Tạo mới hoặc cập nhật summary"""
        summary_date = summary_data["date"]
        existing = self.get_by_date(db, summary_date)
        
        if existing:
            # Update existing
            for field, value in summary_data.items():
                if field != "date" and hasattr(existing, field):
                    setattr(existing, field, value)
            
            db.commit()
            db.refresh(existing)
            return existing
        else:
            # Create new
            return self.create_daily_summary(db, summary_data)
    
    def delete_by_date(self, db: Session, summary_date: date) -> bool:
        """Xóa summary theo ngày"""
        db_summary = self.get_by_date(db, summary_date)
        if db_summary:
            db.delete(db_summary)
            db.commit()
            return True
        return False
    
    def get_monthly_totals(
        self, 
        db: Session, 
        year: int, 
        month: int
    ) -> Dict[str, Any]:
        """Tính tổng cho tháng"""
        from datetime import date as date_cls, calendar
        
        first_day = date_cls(year, month, 1)
        last_day = date_cls(year, month, calendar.monthrange(year, month)[1])
        
        summaries = self.get_by_date_range(db, first_day, last_day)
        
        total_amount = sum(s.total_amount or 0 for s in summaries)
        total_transactions = sum(s.total_transactions or 0 for s in summaries)
        
        return {
            "month": f"{year}-{month:02d}",
            "total_amount": float(total_amount),
            "total_transactions": total_transactions,
            "days_with_data": len(summaries),
            "average_daily_amount": float(total_amount / len(summaries)) if summaries else 0,
            "summaries": summaries
        }
    
    def get_top_tutors_by_period(
        self, 
        db: Session, 
        start_date: date, 
        end_date: date, 
        limit: int = 10
    ) -> List[Dict[str, Any]]:
        """Lấy top tutors theo doanh thu trong khoảng thời gian"""
        summaries = self.get_by_date_range(db, start_date, end_date)
        
        tutor_totals = {}
        for summary in summaries:
            if summary.payments_by_tutor:
                for tutor_id, amount in summary.payments_by_tutor.items():
                    tutor_totals[tutor_id] = tutor_totals.get(tutor_id, 0) + float(amount)
        
        # Sort by amount desc
        sorted_tutors = sorted(
            tutor_totals.items(), 
            key=lambda x: x[1], 
            reverse=True
        )[:limit]
        
        return [
            {"tutor_id": tutor_id, "total_amount": amount}
            for tutor_id, amount in sorted_tutors
        ]
    
    def cleanup_old_summaries(self, db: Session, days_to_keep: int = 365) -> int:
        """Xóa summaries cũ"""
        from datetime import timedelta
        
        cutoff_date = datetime.now().date() - timedelta(days=days_to_keep)
        
        deleted_count = db.query(PaymentDailySummary).filter(
            PaymentDailySummary.summary_date < cutoff_date
        ).count()
        
        db.query(PaymentDailySummary).filter(
            PaymentDailySummary.summary_date < cutoff_date
        ).delete()
        
        db.commit()
        return deleted_count

# Singleton instance
payment_summary_crud = PaymentSummaryCRUD()