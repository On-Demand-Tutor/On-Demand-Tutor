from fastapi import APIRouter, HTTPException, Query, BackgroundTasks
from typing import Optional
from datetime import datetime, timedelta
from app.services.payment_service import payment_service
from app.services.daily_summary_service import daily_summary_service

router = APIRouter(
    prefix="/admin/payments",
    tags=["Payment Management"]
)

@router.get("/")
def get_all_payments(
    page: int = Query(0, ge=0),
    size: int = Query(10, ge=1, le=100),
    status: Optional[str] = None,
    student_id: Optional[int] = None,
    tutor_id: Optional[int] = None,
    sort_by: str = Query("createdAt"),
    sort_dir: str = Query("desc")
):
    """Lấy danh sách tất cả payments với filter và pagination"""
    try:
        result = payment_service.get_all_payments(
            page=page,
            size=size,
            status=status,
            student_id=student_id,
            tutor_id=tutor_id,
            sort_by=sort_by,
            sort_dir=sort_dir
        )
        
        if not result:
            raise HTTPException(
                status_code=503,
                detail="Unable to fetch payments from payment-service"
            )
        
        return result
        
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Error fetching payments: {str(e)}"
        )

@router.get("/{payment_id}")
def get_payment_by_id(payment_id: int):
    """Lấy thông tin payment theo ID"""
    try:
        payment = payment_service.get_payment_by_id(payment_id)
        
        if not payment:
            raise HTTPException(
                status_code=404,
                detail=f"Payment with ID {payment_id} not found"
            )
        
        return payment
        
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Error fetching payment {payment_id}: {str(e)}"
        )

@router.get("/statistics/summary")
def get_payment_statistics(days: int = Query(7, ge=1, le=90)):
    """Lấy thống kê payment trong X ngày gần nhất"""
    try:
        stats = payment_service.get_payment_statistics(days)
        
        if "error" in stats:
            raise HTTPException(
                status_code=500,
                detail=f"Error generating statistics: {stats['error']}"
            )
        
        return stats
        
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Error getting payment statistics: {str(e)}"
        )

@router.get("/daily-summary/{date}")
def get_daily_summary(date: str):
    """
    Lấy tổng hợp payment trong ngày cụ thể
    
    - date: YYYY-MM-DD format
    """
    try:
        target_date = datetime.strptime(date, "%Y-%m-%d")
        summary = daily_summary_service.get_or_create_daily_summary(target_date)
        return summary
        
    except ValueError:
        raise HTTPException(
            status_code=400,
            detail="Invalid date format. Use YYYY-MM-DD"
        )
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Error getting daily summary: {str(e)}"
        )

@router.post("/daily-summary/generate")
def generate_daily_summary(
    date: str,
    background_tasks: BackgroundTasks
):
    """
    Tạo daily summary cho ngày cụ thể (background task)
    
    - date: YYYY-MM-DD format
    """
    try:
        target_date = datetime.strptime(date, "%Y-%m-%d")
        background_tasks.add_task(
            daily_summary_service.generate_and_save_daily_summary,
            target_date
        )
        
        return {
            "message": f"Daily summary generation started for {date}",
            "date": date,
            "status": "processing"
        }
        
    except ValueError:
        raise HTTPException(
            status_code=400,
            detail="Invalid date format. Use YYYY-MM-DD"
        )

@router.get("/daily-summary/period/{start_date}/{end_date}")
def get_daily_summaries_for_period(start_date: str, end_date: str):
    """
    Lấy tổng hợp payment cho khoảng thời gian
    
    - start_date: YYYY-MM-DD
    - end_date: YYYY-MM-DD
    """
    try:
        start = datetime.strptime(start_date, "%Y-%m-%d")
        end = datetime.strptime(end_date, "%Y-%m-%d")
        
        if start > end:
            raise HTTPException(
                status_code=400,
                detail="Start date must be before or equal to end date"
            )
        
        if (end - start).days > 90:
            raise HTTPException(
                status_code=400,
                detail="Period cannot exceed 90 days"
            )
        
        summaries = daily_summary_service.get_summaries_for_period(start, end)

        total_amount = sum(s.get('total_amount', 0) for s in summaries)
        total_transactions = sum(s.get('total_transactions', 0) for s in summaries)
        
        return {
            "period": f"{start_date} to {end_date}",
            "total_days": len(summaries),
            "total_amount": total_amount,
            "total_transactions": total_transactions,
            "average_daily_amount": total_amount / len(summaries) if summaries else 0,
            "daily_summaries": summaries
        }
        
    except ValueError:
        raise HTTPException(
            status_code=400,
            detail="Invalid date format. Use YYYY-MM-DD"
        )
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Error getting period summaries: {str(e)}"
        )

@router.get("/monthly-summary/{year}/{month}")
def get_monthly_summary(year: int, month: int):
    """
    Lấy tổng hợp payment theo tháng
    
    - year: YYYY
    - month: 1-12
    """
    try:
        if not (1 <= month <= 12):
            raise HTTPException(
                status_code=400,
                detail="Month must be between 1 and 12"
            )
        
        if not (2020 <= year <= datetime.now().year + 1):
            raise HTTPException(
                status_code=400,
                detail="Invalid year"
            )
        
        summary = daily_summary_service.get_monthly_aggregation(year, month)
        
        if "error" in summary:
            raise HTTPException(
                status_code=500,
                detail=f"Error generating monthly summary: {summary['error']}"
            )
        
        return summary
        
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Error getting monthly summary: {str(e)}"
        )

@router.post("/maintenance/generate-yesterday-summary")
def generate_yesterday_summary(background_tasks: BackgroundTasks):
    """Tạo daily summary cho ngày hôm qua (thường chạy tự động hàng ngày)"""
    try:
        yesterday = datetime.now() - timedelta(days=1)
        
        background_tasks.add_task(
            daily_summary_service.generate_and_save_daily_summary,
            yesterday
        )
        
        return {
            "message": "Yesterday's daily summary generation started",
            "date": yesterday.strftime("%Y-%m-%d"),
            "status": "processing"
        }
        
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Error starting yesterday summary generation: {str(e)}"
        )
