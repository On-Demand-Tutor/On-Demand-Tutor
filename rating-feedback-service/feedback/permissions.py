import os
from rest_framework.permissions import BasePermission, SAFE_METHODS

ROLE_HEADER = os.getenv("ROLE_HEADER", "X-User-Role")

# Lấy danh sách role moderator từ env (ngăn cách bởi dấu phẩy)
MODERATOR_ROLES = {
    r.strip().lower()
    for r in (os.getenv("MODERATOR_ROLES", "moderator,admin").split(","))
    if r.strip()
}


class IsStudent(BasePermission):
    """Chỉ cho phép user có role=student."""
    def has_permission(self, request, view):
        return (request.headers.get(ROLE_HEADER) or "").lower() == "student"


class IsModeratorOrReadOnly(BasePermission):
    """
    Cho phép:
      - GET/HEAD/OPTIONS (đọc) luôn được phép
      - POST: vẫn cho phép (student tạo complaint)
      - PUT/PATCH/DELETE: chỉ moderator/admin mới được phép
    """
    def has_permission(self, request, view):
        role = (request.headers.get(ROLE_HEADER) or "").lower()

        if request.method in SAFE_METHODS or request.method == "POST":
            return True
        return role in MODERATOR_ROLES
