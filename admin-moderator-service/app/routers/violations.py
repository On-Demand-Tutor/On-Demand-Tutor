from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from .. import crud, schemas
from ..database import get_db

router = APIRouter(prefix="/violation-reports", tags=["violations"])

@router.get("/", response_model=list[schemas.ViolationReportOut])
def get_violation_reports(db: Session = Depends(get_db)):
    return crud.get_violation_reports(db)

@router.get("/{id}", response_model=schemas.ViolationReportOut)
def get_violation_report(id: int, db: Session = Depends(get_db)):
    db_report = crud.get_violation_report(db, id)
    if not db_report:
        raise HTTPException(status_code=404, detail="Report not found")
    return db_report

@router.patch("/{id}/resolve", response_model=schemas.ViolationReportOut)
def resolve_violation_report(id: int, update: schemas.ViolationReportUpdate, db: Session = Depends(get_db)):
    db_report = crud.resolve_violation_report(db, id, update)
    if not db_report:
        raise HTTPException(status_code=404, detail="Report not found")
    return db_report

@router.delete("/{id}")
def delete_violation_report(id: int, db: Session = Depends(get_db)):
    db_report = crud.delete_violation_report(db, id)
    if not db_report:
        raise HTTPException(status_code=404, detail="Report not found")
    return {"message": "Report deleted"}