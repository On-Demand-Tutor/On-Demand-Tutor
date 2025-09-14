import requests
import logging
from typing import List, Dict, Any
from app.config import config
from app.services.auth_service import auth_service

logger = logging.getLogger(__name__)

class UserService:
    def __init__(self):
        self.user_service_url = config.USER_SERVICE_URL
    
    def _make_request(self, method: str, endpoint: str, **kwargs) -> requests.Response:
        headers = auth_service.get_headers()
        url = f"{self.user_service_url}{endpoint}"
        response = requests.request(method, url, headers=headers, **kwargs)
        if response.status_code == 401:
            if auth_service.refresh_token_if_needed(response):
                headers = auth_service.get_headers()
                response = requests.request(method, url, headers=headers, **kwargs)
        
        return response
    
    def get_all_users(self, page: int = 0) -> List[Dict[str, Any]]:
        try:
            response = self._make_request("GET", f"/api/users/getAllUser?page={page}")
            
            if response.status_code == 200:
                result = response.json()
                return result.get("result", [])
            else:
                return []      
        except Exception as e:
            return []
    
    def get_user_stats_by_role(self) -> Dict[str, int]:
        all_users = []
        page = 0
        while True:
            users = self.get_all_users(page)
            if not users:
                break
            all_users.extend(users)
            if len(users) < len(all_users):
                break
            page += 1
        role_counts = {
            "ADMIN": 0,
            "TUTOR": 0,
            "STUDENT": 0
        }
        for user in all_users:
            role = user.get("role", "")
            if role in role_counts:
                role_counts[role] += 1
        return role_counts
    
    def update_user(self, user_id: int, update_data: Dict[str, Any]) -> Dict[str, Any]:
        try:
            response = self._make_request("PUT", f"/api/users/update/{user_id}", json=update_data)
            
            if response.status_code == 200:
                return response.json()
            else:
                return {}        
        except Exception as e:
            return {}
        
    def delete_user(self, user_id: int) -> bool:
        try:
            response = self._make_request("DELETE", f"/api/users/{user_id}")
            
            if response.status_code == 200:
                return True
            else:
                return False        
        except Exception as e:
            return False
        
user_service = UserService()