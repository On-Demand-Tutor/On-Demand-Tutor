import logging
from datetime import datetime, timedelta
from apscheduler.schedulers.asyncio import AsyncIOScheduler
from apscheduler.triggers.cron import CronTrigger
from apscheduler.triggers.interval import IntervalTrigger
from app.services.daily_summary_service import daily_summary_service

logger = logging.getLogger(__name__)

class SchedulerService:
    def __init__(self):
        self.scheduler = AsyncIOScheduler()
        self.is_running = False
    
    def start(self):
        if not self.is_running:
            self._add_jobs()
            self.scheduler.start()
            self.is_running = True
    
    def stop(self):
        if self.is_running:
            self.scheduler.shutdown()
            self.is_running = False
    
    def _add_jobs(self):
        self.scheduler.add_job(
            func=self._generate_daily_summary,
            trigger=CronTrigger(hour=2, minute=0),
            id='daily_payment_summary',
            name='Generate Daily Payment Summary',
            replace_existing=True,
            max_instances=1,
            misfire_grace_time=3600  
        )

        self.scheduler.add_job(
            func=self._cleanup_old_summaries,
            trigger=CronTrigger(day_of_week=6, hour=3, minute=0),  # Sunday 3 AM
            id='weekly_cleanup',
            name='Weekly Cleanup Old Summaries',
            replace_existing=True,
            max_instances=1,
            misfire_grace_time=3600
        )

        self.scheduler.add_job(
            func=self._health_check,
            trigger=IntervalTrigger(minutes=30),
            id='health_check',
            name='Payment Service Health Check',
            replace_existing=True,
            max_instances=1
        )
    
    def _generate_daily_summary(self):
        try:
            yesterday = datetime.now() - timedelta(days=1)           
            result = daily_summary_service.generate_and_save_daily_summary(yesterday)
            
            if result.get("saved"):
                logger.info(f"Daily summary completed: Amount={result.get('total_amount', 0)}, "
                           f"Transactions={result.get('total_transactions', 0)}")
            else:
                logger.error(f"Daily summary failed: {result.get('error', 'Unknown error')}")
                
        except Exception as e:
            logger.error(f"Error in scheduled daily summary generation: {str(e)}")
    
    def trigger_daily_summary_now(self, date: datetime = None):
        """Trigger daily summary generation ngay lập tức (for manual trigger)"""
        try:
            target_date = date or (datetime.now() - timedelta(days=1))
            logger.info(f"Manually triggering daily summary for {target_date.date()}")
            
            result = daily_summary_service.generate_and_save_daily_summary(target_date)
            return result
            
        except Exception as e:
            logger.error(f"Error in manual daily summary trigger: {str(e)}")
            return {"error": str(e), "saved": False}
    
    def get_job_status(self) -> dict:
        """Lấy status của các scheduled jobs"""
        if not self.is_running:
            return {"scheduler_running": False, "jobs": []}
        
        jobs_status = []
        for job in self.scheduler.get_jobs():
            jobs_status.append({
                "id": job.id,
                "name": job.name,
                "next_run_time": job.next_run_time.isoformat() if job.next_run_time else None,
                "trigger": str(job.trigger)
            })
        
        return {
            "scheduler_running": True,
            "jobs": jobs_status
        }

scheduler_service = SchedulerService()