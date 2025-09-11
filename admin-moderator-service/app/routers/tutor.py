from fastapi import APIRouter, HTTPException
from typing import List, Dict, Any, Optional
from app.services.tutor_service import tutor_service
from app.services.role_service import role_service

router = APIRouter(
    prefix="/tutors",
    tags=["Tutor Management"]
)

@router.get("/")
def get_all_tutors():
    return role_service.get_all_tutors_with_details()

@router.get("/{user_id}")
def get_tutor_by_user_id(user_id: int):
    tutor_info = tutor_service.get_tutor_by_user_id(user_id)
    if not tutor_info:
        raise HTTPException(
            status_code=404, 
            detail=f"Tutor information not found for user {user_id}"
        )
    return tutor_info

@router.delete("/{user_id}")
def delete_tutor(user_id: int):
    try:
        existing_tutor = tutor_service.get_tutor_by_user_id(user_id)
        if not existing_tutor:
            raise HTTPException(
                status_code=404,
                detail=f"Tutor not found for user {user_id}"
            )
        success = tutor_service.delete_tutor(user_id)
        if not success:
            raise HTTPException(
                status_code=500,
                detail=f"Failed to delete tutor {user_id}"
            )
        
        return {
            "message": f"Tutor profile for user {user_id} deleted successfully"
        }
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Error deleting tutor {user_id}: {str(e)}"
        )


