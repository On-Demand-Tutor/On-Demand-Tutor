from rest_framework import viewsets, status
from rest_framework.permissions import AllowAny
from rest_framework.decorators import action
from rest_framework.response import Response

from django.db.models import Avg
from .models import Rating, Complaint, Tutor
from .serializers import (
    RatingSerializer,
    ComplaintSerializer,
    ComplaintCreateSerializer,
    ComplaintStatusUpdateSerializer,
)
from .permissions import IsStudent, IsModeratorOrReadOnly

class RatingViewSet(viewsets.ModelViewSet):
    queryset = Rating.objects.all().order_by("-created_at")
    serializer_class = RatingSerializer

    def get_permissions(self):
        if self.action == "create":
            return [IsStudent()]
        return [AllowAny()]  # Cho phép GET/HEAD list & retrieve tự do để test nhanh
    def perform_create(self, serializer):
        rating = serializer.save()
        # Sau khi lưu Rating thì cập nhật điểm trung bình cho Tutor
        avg = Rating.objects.filter(tutor_id=rating.tutor_id).aggregate(avg=Avg("score"))["avg"] or 0.0
        tutor, _ = Tutor.objects.get_or_create(id=rating.tutor_id, defaults={"name": "N/A"})
        tutor.average_score = round(avg, 2)
        tutor.save()

    @action(detail=True, methods=["get"], url_path="average")
    def get_average(self, request, pk=None):
        """GET /ratings/{tutor_id}/average"""
        avg = Rating.objects.filter(tutor_id=pk).aggregate(avg=Avg("score"))["avg"]
        return Response({"tutor_id": pk, "average_score": avg or 0.0})

class ComplaintViewSet(viewsets.ModelViewSet):
    queryset = Complaint.objects.all().order_by("-created_at")
    permission_classes = [IsModeratorOrReadOnly]

    def get_serializer_class(self):
        if self.action == "create":
            return ComplaintCreateSerializer
        elif self.action == "set_status":
            return ComplaintStatusUpdateSerializer
        return ComplaintSerializer

    def create(self, request, *args, **kwargs):
        # dùng serializer tạo (validate role, content, match student_id)
        create_ser = ComplaintCreateSerializer(data=request.data, context={"request": request})
        create_ser.is_valid(raise_exception=True)
        obj = create_ser.save()  # status mặc định = "open"
        # trả về serializer đầy đủ để có id, status, timestamps
        return Response(ComplaintSerializer(obj).data, status=status.HTTP_201_CREATED)

    @action(methods=["put"], detail=True, url_path="status")
    def set_status(self, request, pk=None):
        complaint = self.get_object()
        serializer = ComplaintStatusUpdateSerializer(
            complaint, data=request.data, context={"request": request}
        )
        serializer.is_valid(raise_exception=True)
        serializer.save()
        return Response(ComplaintSerializer(complaint).data, status=status.HTTP_200_OK)
