from fastapi import APIRouter, HTTPException
from typing import List, Dict, Any, Optional
from app.services.student_service import student_service
from app.services.role_service import role_service

router = APIRouter(
    prefix="/admin/students",
    tags=["Student Management"]
)

@router.get("/")
def get_all_students():
    return role_service.get_all_students_with_details()

@router.get("/{user_id}")
def get_student_by_user_id(user_id: int):
    student_info = student_service.get_student_by_user_id(user_id)
    if not student_info:
        raise HTTPException(
            status_code=404, 
            detail=f"Tutor information not found for user {user_id}"
        )
    return student_info


