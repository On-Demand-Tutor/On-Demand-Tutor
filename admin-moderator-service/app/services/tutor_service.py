import requests
import logging
from typing import Dict, Any, Optional
from app.config import config
from app.services.auth_service import auth_service

logger = logging.getLogger(__name__)

class TutorService:
    def __init__(self):
        self.tutor_service_url = "http://tutor-service:8080"
        self.timeout = 10
    
    def _make_request(self, method: str, endpoint: str, **kwargs) -> requests.Response:
        headers = auth_service.get_headers()
        url = f"{self.tutor_service_url}{endpoint}"
        
        try:
            response = requests.request(method, url, headers=headers, timeout=self.timeout, **kwargs)
            if response.status_code == 401:
                if auth_service.refresh_token_if_needed(response):
                    headers = auth_service.get_headers()
                    response = requests.request(method, url, headers=headers, timeout=self.timeout, **kwargs)
            
            return response
            
        except requests.exceptions.RequestException as e:
            raise
    
    def get_tutor_by_user_id(self, user_id: int) -> Optional[Dict[str, Any]]:
        try:
            response = self._make_request("GET", f"/api/tutors/user/{user_id}")
            
            if response.status_code == 200:
                return response.json()
            elif response.status_code == 404:
                return None
            else:
                return None        
        except Exception as e:
            return None
    
    def delete_tutor(self, user_id: int) -> bool:
        try:
            response = self._make_request("DELETE", f"/api/tutors/user/{user_id}")
            if response.status_code in [200, 204]:
                return True
            else:
                return False        
        except Exception as e:
            return False
    
tutor_service = TutorService()