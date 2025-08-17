from sqlalchemy.orm import Session
from . import models, schemas

def get_users(db: Session, skip: int = 0, limit: int = 100):
    return db.query(models.User).offset(skip).limit(limit).all()

def get_user(db: Session, user_id: int):
    return db.query(models.User).filter(models.User.id == user_id).first()

def update_user(db: Session, user_id: int, user_update: schemas.UserUpdate):
    db_user = get_user(db, user_id)
    if db_user:
        update_data = user_update.dict(exclude_unset=True)
        for key, value in update_data.items():
            setattr(db_user, key, value)
        db.commit()
        db.refresh(db_user)
    return db_user

def delete_user(db: Session, user_id: int):
    db_user = get_user(db, user_id)
    if db_user:
        db_user.is_active = False
        db_user.status = 'locked'
        db.commit()
        db.refresh(db_user)
    return db_user

def get_services(db: Session, skip: int = 0, limit: int = 100):
    return db.query(models.Service).offset(skip).limit(limit).all()

def get_service(db: Session, service_id: int):
    return db.query(models.Service).filter(models.Service.id == service_id).first()

def create_service(db: Session, service: schemas.ServiceCreate):
    db_service = models.Service(**service.dict())
    db.add(db_service)
    db.commit()
    db.refresh(db_service)
    return db_service

def update_service(db: Session, service_id: int, service_update: dict):
    db_service = get_service(db, service_id)
    if db_service:
        update_data = service_update.dict(exclude_unset=True)  # Chỉ lấy các trường được gửi lên
        for key, value in update_data.items():
            setattr(db_service, key, value)
        db.commit()
        db.refresh(db_service)
        return db_service
    return None

def delete_service(db: Session, service_id: int):
    db_service = db.query(models.Service).filter(models.Service.id == service_id).first()
    if db_service:
        db.delete(db_service)  
        db.commit()
        return {"message": "Service deleted successfully"}
    return None

def get_violation_reports(db: Session):
    return db.query(models.ViolationReport).all()

def get_violation_report(db: Session, report_id: int):
    return db.query(models.ViolationReport).filter(models.ViolationReport.id == report_id).first()

def resolve_violation_report(db: Session, report_id: int, update: schemas.ViolationReportUpdate):
    db_report = get_violation_report(db, report_id)
    if db_report:
        db_report.status = update.status
        db.commit()
        db.refresh(db_report)
        print(f"Notification: Báo cáo {report_id} đã được xử lý. Gửi đến reporter {db_report.reporter_id} và reported {db_report.reported_id}")
    return db_report

def delete_violation_report(db: Session, report_id: int):
    db_report = get_violation_report(db, report_id)
    if db_report:
        db.delete(db_report)
        db.commit()
    return db_report

def get_summary(db: Session):
    total_users = db.query(models.User).count()
    tutors = db.query(models.User).filter(models.User.role == 'tutor').count()
    students = db.query(models.User).filter(models.User.role == 'student').count()
    services = db.query(models.Service).count()
    return {
        "total_users": total_users,
        "tutors": tutors,
        "students": students,
        "services": services
    }

def get_violations_stats(db: Session):
    from sqlalchemy import func
    return db.query(
        func.date(models.ViolationReport.created_at).label('date'),
        func.count(models.ViolationReport.id).label('count')
    ).group_by('date').all()

def get_active_tutors(db: Session):
    return db.query(models.User).filter(models.User.role == 'tutor', models.User.status == 'active').count()

def get_tutors(db: Session, skip: int = 0, limit: int = 100):
    return db.query(models.Tutor).offset(skip).limit(limit).all()

def get_tutor(db: Session, tutor_id: int):
    return db.query(models.Tutor).filter(models.Tutor.id == tutor_id).first()

def create_tutor(db: Session, tutor: schemas.TutorCreate):
    db_tutor = models.Tutor(**tutor.dict())
    db.add(db_tutor)
    db.commit()
    db.refresh(db_tutor)
    return db_tutor

def update_tutor(db: Session, tutor_id: int, tutor_update: schemas.TutorUpdate):
    db_tutor = get_tutor(db, tutor_id)
    if db_tutor:
        update_data = tutor_update.dict(exclude_unset=True)
        for key, value in update_data.items():
            setattr(db_tutor, key, value)
        db.commit()
        db.refresh(db_tutor)
        return db_tutor
    return None

def update_tutor_activity_status(db: Session, tutor_id: int, activity_status: schemas.TutorActivityStatus):
    db_tutor = get_tutor(db, tutor_id)
    if db_tutor:
        db_tutor.activity_status = activity_status
        db.commit()
        db.refresh(db_tutor)
        return db_tutor
    return None