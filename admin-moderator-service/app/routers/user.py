from fastapi import APIRouter, HTTPException
from typing import List, Dict, Any

from app.services.user_service import user_service

router = APIRouter(
    prefix="/users",
    tags=["User Management"]
)

@router.get("/", response_model=List[Dict[str, Any]])
def get_all_users(page: int = 0):
    users = user_service.get_all_users(page)
    return users

@router.get("/stats_by_role", response_model=Dict[str, int])
def get_user_stats_by_role():
    stats = user_service.get_user_stats_by_role()
    return stats

@router.put("/{user_id}")
def update_user(user_id: int, update_data: Dict[str, Any]):
    result = user_service.update_user(user_id, update_data)
    if not result:
        raise HTTPException(status_code=404, detail="User not found or update failed")
    return result

@router.delete("/{user_id}")
def delete_user(user_id: int):
    success = user_service.delete_user(user_id)
    if not success:
        raise HTTPException(status_code=404, detail="User not found or deletion failed")
    return {"detail": "User deleted successfully"}



