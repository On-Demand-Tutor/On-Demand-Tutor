from fastapi import APIRouter, HTTPException
from typing import List, Dict, Any, Optional
from app.services.student_service import student_service
from app.services.role_service import role_service

router = APIRouter(
    prefix="/students",
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

@router.delete("/{user_id}")
def delete_student(user_id: int):
    try:
        existing_student = student_service.get_student_by_user_id(user_id)
        if not existing_student:
            raise HTTPException(
                status_code=404,
                detail=f"Student not found for user {user_id}"
            )
        success = student_service.delete_student(user_id)
        if not success:
            raise HTTPException(
                status_code=500,
                detail=f"Failed to delete student {user_id}"
            )
        
        return {
            "message": f"Student profile for user {user_id} deleted successfully"
        }
        
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Error deleting student {user_id}: {str(e)}"
        )



