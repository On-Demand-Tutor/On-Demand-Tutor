import os
import requests
from rest_framework import serializers
from .models import Rating, Complaint

BOOKING_SERVICE_URL = os.getenv("BOOKING_SERVICE_URL", "http://localhost:9000")
ROLE_HEADER = os.getenv("ROLE_HEADER", "X-User-Role")
USER_ID_HEADER = os.getenv("USER_ID_HEADER", "X-User-Id")
# Cho phép nhiều role có quyền moderator (ngăn cách bằng dấu phẩy)
MODERATOR_ROLES = {
    r.strip().lower()
    for r in (os.getenv("MODERATOR_ROLES", "moderator,admin").split(","))
    if r.strip()
}


# ========
# RATINGS
# ========
class RatingSerializer(serializers.ModelSerializer):
    class Meta:
        model = Rating
        fields = "__all__"
        read_only_fields = ["created_at"]

    def validate(self, attrs):
        request = self.context.get("request")

        # 1) Chỉ student mới được tạo rating & student_id phải khớp user đang đăng nhập
        if request:
            role = (request.headers.get(ROLE_HEADER) or "").lower()
            user_id = request.headers.get(USER_ID_HEADER)
            if role != "student":
                raise serializers.ValidationError("Chỉ student mới được tạo rating.")
            if str(attrs.get("student_id")) != str(user_id):
                raise serializers.ValidationError("student_id không trùng người đang đăng nhập.")

        # 2) Kiểm tra booking từ Booking Service
        booking_id = attrs.get("booking_id")
        try:
            resp = requests.get(f"{BOOKING_SERVICE_URL}/bookings/{booking_id}", timeout=5)
            resp.raise_for_status()
            data = resp.json()
        except Exception:
            raise serializers.ValidationError("Không xác thực được booking từ Booking Service.")

        # 3) Booking phải thuộc đúng student/tutor
        if str(data.get("student_id")) != str(attrs.get("student_id")) or \
           str(data.get("tutor_id"))   != str(attrs.get("tutor_id")):
            raise serializers.ValidationError("Booking không khớp student/tutor.")

        # 4) Chỉ được đánh giá khi status = done
        if (data.get("status") or "").lower() != "done":
            print(">>> DEBUG status fail:", data.get("status"))
            raise serializers.ValidationError("Chỉ được đánh giá sau khi buổi học đã hoàn thành.")

        return attrs


# ===========
# COMPLAINTS
# ===========
class ComplaintSerializer(serializers.ModelSerializer):
    """Serializer cho response (list/detail)."""
    class Meta:
        model = Complaint
        fields = ["id", "student_id", "tutor_id", "content", "status", "created_at", "updated_at"]
        read_only_fields = ["id", "status", "created_at", "updated_at"]


class ComplaintCreateSerializer(serializers.ModelSerializer):
    """Serializer cho POST /complaints — status mặc định = open."""
    class Meta:
        model = Complaint
        fields = ["student_id", "tutor_id", "content"]  # status set mặc định từ model

    def validate(self, attrs):
        request = self.context.get("request")
        if request:
            role = (request.headers.get(ROLE_HEADER) or "").lower()
            user_id = request.headers.get(USER_ID_HEADER)
            if role != "student":
                raise serializers.ValidationError("Chỉ student mới được tạo complaint.")
            if str(attrs.get("student_id")) != str(user_id):
                raise serializers.ValidationError("student_id không trùng người đang đăng nhập.")

        # Bắt buộc phải có nội dung không được để toàn khoảng trắng
        content = (attrs.get("content") or "").strip()
        if not content:
            raise serializers.ValidationError("Nội dung complaint (content) không được để trống.")
        return attrs

class ComplaintStatusUpdateSerializer(serializers.ModelSerializer):
    """Serializer cho PUT /complaints/{id}/status — chỉ cập nhật field status."""
    class Meta:
        model = Complaint
        fields = ["status"]

    def validate_status(self, value):
        allowed = {c[0] for c in Complaint.Status.choices}
        if value not in allowed:
            raise serializers.ValidationError(f"Status phải thuộc: {', '.join(sorted(allowed))}")
        return value

    def validate(self, attrs):
        # (Tùy chọn) Check quyền moderator qua header.
        request = self.context.get("request")
        if request:
            role = (request.headers.get(ROLE_HEADER) or "").lower()
            if role not in MODERATOR_ROLES:
                raise serializers.ValidationError("Chỉ moderator mới được cập nhật trạng thái complaint.")
        return attrs
