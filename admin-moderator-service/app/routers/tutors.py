from fastapi import APIRouter, Depends, HTTPException
from .. import schemas
from ..database import get_db
from sqlalchemy.orm import Session
from ..crud import TutorRepository

router = APIRouter(prefix="/tutors", tags=["tutors"])

@router.get("/", response_model=list[schemas.TutorOut])
def read_tutors(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    return TutorRepository(db).get_tutors(skip=skip, limit=limit)

@router.put("/{id}", response_model=schemas.TutorOut)
def update_tutor(id: int, tutor: schemas.TutorUpdate, db: Session = Depends(get_db)):
    db_tutor = TutorRepository(db).update_tutor(tutor_id=id, tutor_update=tutor)
    if db_tutor is None:
        raise HTTPException(status_code=404, detail="Tutor not found")
    return db_tutor

@router.patch("/{id}/activity-status", response_model=schemas.TutorOut)
def update_tutor_activity_status(id: int, activity_status: schemas.TutorActivityStatus, db: Session = Depends(get_db)):
    db_tutor = TutorRepository(db).update_tutor_activity_status(tutor_id=id, activity_status=activity_status)
    if db_tutor is None:
        raise HTTPException(status_code=404, detail="Tutor not found")
    return db_tutor
