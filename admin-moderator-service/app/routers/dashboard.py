from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from ..database import get_db
from ..crud import SummaryRepository, TutorRepository, ViolationReportRepository

router = APIRouter(prefix="/dashboard", tags=["dashboard"])

@router.get("/summary")
def get_summary(db: Session = Depends(get_db)):
    return SummaryRepository(db).get_summary()

@router.get("/violations")
def get_violations_stats(db: Session = Depends(get_db)):
    return ViolationReportRepository(db).get_violations_stats()

@router.get("/active-tutors")
def get_active_tutors(db: Session = Depends(get_db)):
    return {"active_tutors": TutorRepository(db).get_active_tutors()}
