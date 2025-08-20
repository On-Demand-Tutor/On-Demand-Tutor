from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from .. import schemas
from ..database import get_db
from ..crud import UserRepository

router = APIRouter(prefix="/users", tags=["users"])

@router.get("/", response_model=list[schemas.UserOut])
def get_users(db: Session = Depends(get_db)):
    return UserRepository(db).get_users()

@router.get("/{id}", response_model=schemas.UserOut)
def get_user(id: int, db: Session = Depends(get_db)):
    db_user = UserRepository(db).get_user(id)
    if not db_user:
        raise HTTPException(status_code=404, detail="User not found")
    return db_user

@router.put("/{id}", response_model=schemas.UserOut)
def update_user(id: int, user: schemas.UserUpdate, db: Session = Depends(get_db)):
    db_user = UserRepository(db).update_user(id, user)
    if not db_user:
        raise HTTPException(status_code=404, detail="User not found")
    return db_user

@router.delete("/{id}", response_model=schemas.UserOut)
def delete_user(id: int, db: Session = Depends(get_db)):
    db_user = UserRepository(db).delete_user(id)
    if not db_user:
        raise HTTPException(status_code=404, detail="User not found")
    return db_user
