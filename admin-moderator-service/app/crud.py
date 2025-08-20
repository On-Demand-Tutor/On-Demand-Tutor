from sqlalchemy.orm import Session
from sqlalchemy import func
from . import models, schemas

# ---------------- USER ----------------
class UserRepository:
    def __init__(self, db: Session):
        self.db = db

    def get_users(self, skip: int = 0, limit: int = 100):
        return self.db.query(models.User).offset(skip).limit(limit).all()

    def get_user(self, user_id: int):
        return self.db.query(models.User).filter(models.User.id == user_id).first()

    def update_user(self, user_id: int, user_update: schemas.UserUpdate):
        db_user = self.get_user(user_id)
        if db_user:
            update_data = user_update.dict(exclude_unset=True)
            for key, value in update_data.items():
                setattr(db_user, key, value)
            self.db.commit()
            self.db.refresh(db_user)
        return db_user

    def delete_user(self, user_id: int):
        db_user = self.get_user(user_id)
        if db_user:
            db_user.is_active = False
            db_user.status = 'locked'
            self.db.commit()
            self.db.refresh(db_user)
        return db_user


# ---------------- SERVICE ----------------
class ServiceRepository:
    def __init__(self, db: Session):
        self.db = db

    def get_services(self, skip: int = 0, limit: int = 100):
        return self.db.query(models.Service).offset(skip).limit(limit).all()

    def get_service(self, service_id: int):
        return self.db.query(models.Service).filter(models.Service.id == service_id).first()

    def create_service(self, service: schemas.ServiceCreate):
        db_service = models.Service(**service.dict())
        self.db.add(db_service)
        self.db.commit()
        self.db.refresh(db_service)
        return db_service

    def update_service(self, service_id: int, service_update: schemas.ServiceCreate):
        db_service = self.get_service(service_id)
        if db_service:
            update_data = service_update.dict(exclude_unset=True)
            for key, value in update_data.items():
                setattr(db_service, key, value)
            self.db.commit()
            self.db.refresh(db_service)
            return db_service
        return None

    def delete_service(self, service_id: int):
        db_service = self.get_service(service_id)
        if db_service:
            self.db.delete(db_service)
            self.db.commit()
            return {"message": "Service deleted successfully"}
        return None


# ---------------- VIOLATION REPORT ----------------
class ViolationReportRepository:
    def __init__(self, db: Session):
        self.db = db

    def get_violation_reports(self):
        return self.db.query(models.ViolationReport).all()

    def get_violation_report(self, report_id: int):
        return self.db.query(models.ViolationReport).filter(models.ViolationReport.id == report_id).first()

    def resolve_violation_report(self, report_id: int, update: schemas.ViolationReportUpdate):
        db_report = self.get_violation_report(report_id)
        if db_report:
            db_report.status = update.status
            self.db.commit()
            self.db.refresh(db_report)
            print(
                f"Notification: Báo cáo {report_id} đã được xử lý. "
                f"Gửi đến reporter {db_report.reporter_id} và reported {db_report.reported_id}"
            )
        return db_report

    def delete_violation_report(self, report_id: int):
        db_report = self.get_violation_report(report_id)
        if db_report:
            self.db.delete(db_report)
            self.db.commit()
        return db_report

    def get_violations_stats(self):
        return self.db.query(
            func.date(models.ViolationReport.created_at).label('date'),
            func.count(models.ViolationReport.id).label('count')
        ).group_by('date').all()


# ---------------- TUTOR ----------------
class TutorRepository:
    def __init__(self, db: Session):
        self.db = db

    def get_active_tutors(self):
        return self.db.query(models.User).filter(
            models.User.role == 'tutor',
            models.User.status == 'active'
        ).count()

    def get_tutors(self, skip: int = 0, limit: int = 100):
        return self.db.query(models.Tutor).offset(skip).limit(limit).all()

    def get_tutor(self, tutor_id: int):
        return self.db.query(models.Tutor).filter(models.Tutor.id == tutor_id).first()

    def create_tutor(self, tutor: schemas.TutorCreate):
        db_tutor = models.Tutor(**tutor.dict())
        self.db.add(db_tutor)
        self.db.commit()
        self.db.refresh(db_tutor)
        return db_tutor

    def update_tutor(self, tutor_id: int, tutor_update: schemas.TutorUpdate):
        db_tutor = self.get_tutor(tutor_id)
        if db_tutor:
            update_data = tutor_update.dict(exclude_unset=True)
            for key, value in update_data.items():
                setattr(db_tutor, key, value)
            self.db.commit()
            self.db.refresh(db_tutor)
            return db_tutor
        return None

    def update_tutor_activity_status(self, tutor_id: int, activity_status: schemas.TutorActivityStatus):
        db_tutor = self.get_tutor(tutor_id)
        if db_tutor:
            db_tutor.activity_status = activity_status
            self.db.commit()
            self.db.refresh(db_tutor)
            return db_tutor
        return None


# ---------------- SUMMARY ----------------
class SummaryRepository:
    def __init__(self, db: Session):
        self.db = db

    def get_summary(self):
        total_users = self.db.query(models.User).count()
        tutors = self.db.query(models.User).filter(models.User.role == 'tutor').count()
        students = self.db.query(models.User).filter(models.User.role == 'student').count()
        services = self.db.query(models.Service).count()
        return {
            "total_users": total_users,
            "tutors": tutors,
            "students": students,
            "services": services
        }
