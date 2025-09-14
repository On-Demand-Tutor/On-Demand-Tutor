import requests
import logging
from app.config import config
from app.services.jwt_service import jwt_service

logger = logging.getLogger(__name__)

class AuthService:
    def __init__(self):
        self.user_service_url = config.USER_SERVICE_URL
        self.token = None
    
    def login_admin(self) -> str:
        try:
            login_data = {
                "email": config.ADMIN_EMAIL,
                "password": config.ADMIN_PASSWORD
            }
            
            response = requests.post(
                f"{self.user_service_url}/api/users/login",
                json=login_data,
                timeout=10
            )
            
            if response.status_code == 200:
                result = response.json()
                self.token = result.get("result", {}).get("token")
                if self.token and jwt_service.is_token_valid(self.token):
                    return self.token
                else:
                    raise Exception("Invalid token received")
            else:
                raise Exception(f"Login failed: {response.status_code}")
                
        except Exception as e:
            raise Exception(f"Authentication error: {str(e)}")
    
    def get_headers(self) -> dict:
        if not self.token or not jwt_service.is_token_valid(self.token):
            self.login_admin()
        
        return {
            "Authorization": f"Bearer {self.token}",
            "Content-Type": "application/json"
        }
    
    def refresh_token_if_needed(self, response) -> bool:
        if response.status_code == 401:
            try:
                self.token = None
                self.login_admin()
                return True
            except Exception as e:
                return False
        return False
    
    def get_token_info(self) -> dict:
        if not self.token:
            return {"error": "No token available"}
        
        payload = jwt_service.get_token_payload(self.token)
        is_valid = jwt_service.is_token_valid(self.token)
        
        return {
            "token_preview": self.token[:50] + "...",
            "is_valid": is_valid,
            "payload": payload
        }

auth_service = AuthService()