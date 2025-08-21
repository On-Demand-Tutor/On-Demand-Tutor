from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from .. import schemas
from ..database import get_db
from ..crud import ServiceRepository

router = APIRouter(prefix="/services", tags=["services"])

@router.get("/", response_model=list[schemas.ServiceOut])
def read_services(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    return ServiceRepository(db).get_services(skip=skip, limit=limit)

@router.get("/{id}", response_model=schemas.ServiceOut)
def get_service(id: int, db: Session = Depends(get_db)):
    db_service = ServiceRepository(db).get_service(service_id=id)
    if db_service is None:
        raise HTTPException(status_code=404, detail="Service not found")
    return db_service

@router.post("/", response_model=schemas.ServiceOut)
def create_service(service: schemas.ServiceCreate, db: Session = Depends(get_db)):
    return ServiceRepository(db).create_service(service)

@router.put("/{id}", response_model=schemas.ServiceOut)
def update_service(id: int, service: schemas.ServiceUpdate, db: Session = Depends(get_db)):
    db_service = ServiceRepository(db).update_service(service_id=id, service_update=service)
    if db_service is None:
        raise HTTPException(status_code=404, detail="Service not found")
    return db_service

@router.delete("/{id}")
def delete_service(id: int, db: Session = Depends(get_db)):
    result = ServiceRepository(db).delete_service(id)
    if result is None:
        raise HTTPException(status_code=404, detail="Service not found")
    return result
