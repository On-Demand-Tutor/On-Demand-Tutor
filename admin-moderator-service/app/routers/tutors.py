from fastapi import APIRouter, Depends, HTTPException
from .. import crud, schemas
from ..database import get_db
from sqlalchemy.orm import Session

router = APIRouter(prefix="/tutors", tags=["tutors"])

@router.get("/", response_model=list[schemas.TutorOut])
def read_tutors(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    tutors = crud.get_tutors(db, skip=skip, limit=limit)
    return tutors

@router.put("/{id}", response_model=schemas.TutorOut)
def update_tutor(id: int, tutor: schemas.TutorUpdate, db: Session = Depends(get_db)):
    db_tutor = crud.update_tutor(db, tutor_id=id, tutor_update=tutor)
    if db_tutor is None:
        raise HTTPException(status_code=404, detail="Tutor not found")
    return db_tutor

@router.patch("/{id}/activity-status", response_model=schemas.TutorOut)
def update_tutor_activity_status(id: int, activity_status: schemas.TutorActivityStatus, db: Session = Depends(get_db)):
    db_tutor = crud.update_tutor_activity_status(db, tutor_id=id, activity_status=activity_status)
    if db_tutor is None:
        raise HTTPException(status_code=404, detail="Tutor not found")
    return db_tutor