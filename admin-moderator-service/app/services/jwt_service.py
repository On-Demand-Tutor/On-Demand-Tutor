import jwt
import logging
from datetime import datetime, timezone
from typing import Dict, Any, Optional
from app.config import config

logger = logging.getLogger(__name__)

class JWTService:
    def __init__(self):
        self.secret_key = config.JWT_SECRET_KEY
        self.algorithm = config.JWT_ALGORITHM
    
    def verify_token(self, token: str) -> Optional[Dict[str, Any]]:
        try:
            if token.startswith('Bearer '):
                token = token[7:]
            
            payload = jwt.decode(
                token, 
                self.secret_key, 
                algorithms=[self.algorithm]
            )

            if 'exp' in payload:
                exp_timestamp = payload['exp']
                current_timestamp = datetime.now(timezone.utc).timestamp()
                
                if current_timestamp > exp_timestamp:
                    logger.warning("Token has expired")
                    return None
            return payload
            
        except jwt.ExpiredSignatureError:
            return None
        except jwt.InvalidTokenError as e:
            return None
        except Exception as e:
            return None
    
    def is_token_valid(self, token: str) -> bool:
        return self.verify_token(token) is not None
    
    def get_token_payload(self, token: str) -> Optional[Dict[str, Any]]:
        try:
            if token.startswith('Bearer '):
                token = token[7:]
            payload = jwt.decode(
                token, 
                options={"verify_signature": False, "verify_exp": False}
            )
            return payload
        except Exception as e:
            return None

jwt_service = JWTService()