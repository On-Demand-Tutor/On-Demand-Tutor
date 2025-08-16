import os
import requests
from rest_framework import serializers
from .models import Rating, Complaint

BOOKING_SERVICE_URL = os.getenv("BOOKING_SERVICE_URL")
ROLE_HEADER = os.getenv("ROLE_HEADER", "X-User-Role")
USER_ID_HEADER = os.getenv("USER_ID_HEADER", "X-User-Id")

class RatingSerializer(serializers.ModelSerializer):
    class Meta:
        model = Rating
        fields = '__all__'
        read_only_fields = ['created_at']

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
            print(">>> DEBUG status fail:", data.get("status")) # In lỗi ra khi test fail
            raise serializers.ValidationError("Chỉ được đánh giá sau khi buổi học đã hoàn thành.")

        return attrs

class ComplaintSerializer(serializers.ModelSerializer):
    class Meta:
        model = Complaint
        fields = '__all__'
