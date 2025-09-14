import requests
import logging
from typing import Dict, Any, Optional, List
from datetime import datetime, timedelta
from app.config import config
from app.services.auth_service import auth_service

logger = logging.getLogger(__name__)

class PaymentService:
    def __init__(self):
        self.payment_service_url = "http://payment-service:8080"
        self.timeout = 15
    
    def _make_request(self, method: str, endpoint: str, **kwargs) -> requests.Response:
        headers = auth_service.get_headers()
        url = f"{self.payment_service_url}{endpoint}"
        
        try:
            response = requests.request(method, url, headers=headers, timeout=self.timeout, **kwargs)

            if response.status_code == 401:
                if auth_service.refresh_token_if_needed(response):
                    headers = auth_service.get_headers()
                    response = requests.request(method, url, headers=headers, timeout=self.timeout, **kwargs)
            
            return response
            
        except requests.exceptions.Timeout:
            raise
        except requests.exceptions.RequestException as e:
            raise
    
    def get_all_payments(
        self, 
        page: int = 0, 
        size: int = 10,
        status: Optional[str] = None,
        student_id: Optional[int] = None,
        tutor_id: Optional[int] = None,
        sort_by: str = "createdAt",
        sort_dir: str = "desc"
    ) -> Optional[Dict[str, Any]]:
        try:
            params = {
                "page": page,
                "size": size,
                "sortBy": sort_by,
                "sortDir": sort_dir
            }
            
            if status:
                params["status"] = status
            if student_id:
                params["studentId"] = student_id
            if tutor_id:
                params["tutorId"] = tutor_id
            
            response = self._make_request("GET", "/api/payments/getAllPayment", params=params)
            
            if response.status_code == 200:
                data = response.json()
                return data
            else:
                return None
                
        except Exception as e:
            return None
    
    def get_payment_by_id(self, payment_id: int) -> Optional[Dict[str, Any]]:
        try:
            response = self._make_request("GET", f"/api/payments/{payment_id}")
            
            if response.status_code == 200:
                return response.json()
            elif response.status_code == 404:
                return None
            else:
                return None
                
        except Exception as e:
            return None
        
    def get_payments_by_date_range(
        self, 
        start_date: datetime, 
        end_date: datetime,
        status: str = "COMPLETED"
    ) -> List[Dict[str, Any]]:
        """Lấy payments trong khoảng thời gian (cho daily summary)"""
        try:
            all_payments = []
            page = 0
            size = 100
            
            while True:
                result = self.get_all_payments(
                    page=page,
                    size=size,
                    status=status,
                    sort_by="createdAt",
                    sort_dir="asc"
                )
                
                if not result or not result.get("content"):
                    break
                
                payments = result.get("content", [])

                filtered_payments = []
                for payment in payments:
                    created_at_str = payment.get("createdAt")
                    if created_at_str:
                        try:
                            created_at = datetime.fromisoformat(created_at_str.replace('Z', '+00:00'))
                            if start_date <= created_at <= end_date:
                                filtered_payments.append(payment)
                        except:
                            continue
                
                all_payments.extend(filtered_payments)

                if result.get("last", True):
                    break
                    
                page += 1
            
            return all_payments
            
        except Exception as e:
            return []
    
    def calculate_daily_summary(self, target_date: datetime) -> Dict[str, Any]:
        """Tính tổng payment trong ngày"""
        try:
            start_date = target_date.replace(hour=0, minute=0, second=0, microsecond=0)
            end_date = start_date + timedelta(days=1) - timedelta(microseconds=1)
            
            payments = self.get_payments_by_date_range(start_date, end_date, "COMPLETED")
            
            if not payments:
                return {
                    "date": target_date.date().isoformat(),
                    "total_amount": 0,
                    "total_transactions": 0,
                    "payments_by_status": {},
                    "payments_by_tutor": {},
                    "payments_by_student": {}
                }
            
            total_amount = 0
            payments_by_status = {}
            payments_by_tutor = {}
            payments_by_student = {}
            
            for payment in payments:
                amount = payment.get("amount", 0) or payment.get("totalAmount", 0)
                total_amount += amount
                
                status = payment.get("status", "UNKNOWN")
                payments_by_status[status] = payments_by_status.get(status, 0) + amount
                
                tutor_id = payment.get("tutorId")
                if tutor_id:
                    payments_by_tutor[str(tutor_id)] = payments_by_tutor.get(str(tutor_id), 0) + amount
                
                student_id = payment.get("studentId")
                if student_id:
                    payments_by_student[str(student_id)] = payments_by_student.get(str(student_id), 0) + amount
            
            return {
                "date": target_date.date().isoformat(),
                "total_amount": total_amount,
                "total_transactions": len(payments),
                "payments_by_status": payments_by_status,
                "payments_by_tutor": payments_by_tutor,
                "payments_by_student": payments_by_student,
                "summary_generated_at": datetime.now().isoformat()
            }
            
        except Exception as e:
            return {
                "date": target_date.date().isoformat(),
                "total_amount": 0,
                "total_transactions": 0,
                "error": str(e)
            }
    
    def get_payment_statistics(self, days: int = 7) -> Dict[str, Any]:
        """Lấy thống kê payment trong X ngày gần nhất"""
        try:
            end_date = datetime.now()
            start_date = end_date - timedelta(days=days)
            
            daily_summaries = []
            current_date = start_date
            
            while current_date <= end_date:
                daily_summary = self.calculate_daily_summary(current_date)
                daily_summaries.append(daily_summary)
                current_date += timedelta(days=1)

            total_amount = sum(summary.get("total_amount", 0) for summary in daily_summaries)
            total_transactions = sum(summary.get("total_transactions", 0) for summary in daily_summaries)
            
            return {
                "period": f"Last {days} days",
                "start_date": start_date.date().isoformat(),
                "end_date": end_date.date().isoformat(),
                "total_amount": total_amount,
                "total_transactions": total_transactions,
                "average_daily_amount": total_amount / days if days > 0 else 0,
                "average_daily_transactions": total_transactions / days if days > 0 else 0,
                "daily_summaries": daily_summaries
            }
            
        except Exception as e:
            return {"error": str(e)}
    
payment_service = PaymentService()