import os
from rest_framework.permissions import BasePermission

ROLE_HEADER = os.getenv("ROLE_HEADER", "X-User-Role")

class IsStudent(BasePermission):
    def has_permission(self, request, view):
        return (request.headers.get(ROLE_HEADER) or "").lower() == "student"
