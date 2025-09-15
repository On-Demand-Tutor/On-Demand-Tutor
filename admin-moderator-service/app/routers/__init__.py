from .user import router as user_router
from .tutor import router as tutor_router
from .student import router as student_router
from .payment import router as payment_router

__all__ = ["user_router", "tutor_router", "student_router", "payment_router"]