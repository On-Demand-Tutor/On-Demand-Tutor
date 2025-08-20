from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from .. import crud
from ..database import get_db

router = APIRouter(prefix="/dashboard", tags=["dashboard"])

@router.get("/summary")
def get_summary(db: Session = Depends(get_db)):
    return crud.get_summary(db)

@router.get("/violations")
def get_violations_stats(db: Session = Depends(get_db)):
    return crud.get_violations_stats(db)

@router.get("/active-tutors")
def get_active_tutors(db: Session = Depends(get_db)):
    return {"active_tutors": crud.get_active_tutors(db)}