from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from .. import crud, schemas
from ..database import get_db
from .. import models

router = APIRouter(prefix="/services", tags=["services"])

@router.get("/", response_model=list[schemas.ServiceOut])
def read_services(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    services = crud.get_services(db, skip=skip, limit=limit)
    return services

@router.get("/{id}", response_model=schemas.ServiceOut)
def get_service(id: int, db: Session = Depends(get_db)):
    db_service = crud.get_service(db, service_id=id)
    if db_service is None:
        raise HTTPException(status_code=404, detail="Service not found")
    return db_service

@router.post("/", response_model=schemas.ServiceOut)
def create_service(service: schemas.ServiceCreate, db: Session = Depends(get_db)):
    return crud.create_service(db, service)

@router.put("/{id}", response_model=schemas.ServiceOut)
def update_service(id: int, service: schemas.ServiceUpdate, db: Session = Depends(get_db)):
    db_service = crud.update_service(db, service_id=id, service_update=service)
    if db_service is None:
        raise HTTPException(status_code=404, detail="Service not found")
    return db_service

@router.delete("/{id}")
def delete_service(id: int, db: Session = Depends(get_db)):
    result = crud.delete_service(db, id)
    if result is None:
        raise HTTPException(status_code=404, detail="Service not found")
    return result

def get_service(db: Session, service_id: int):
    return db.query(models.Service).filter(models.Service.id == service_id).first()

def get_services(db: Session, skip: int = 0, limit: int = 100):
    return db.query(models.Service).offset(skip).limit(limit).all()