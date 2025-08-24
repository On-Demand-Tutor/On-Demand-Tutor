from rest_framework import viewsets
from rest_framework.permissions import AllowAny
from .models import Rating, Complaint
from .serializers import RatingSerializer, ComplaintSerializer
from .permissions import IsStudent

class RatingViewSet(viewsets.ModelViewSet):
    queryset = Rating.objects.all().order_by('-created_at')
    serializer_class = RatingSerializer

    def get_permissions(self):
        if self.action == 'create':
            return [IsStudent()]
        return [AllowAny()] # tuỳ có thể AllowAny để test nhanh, hoặc chặn bớt

class ComplaintViewSet(viewsets.ModelViewSet):
    queryset = Complaint.objects.all().order_by('-created_at')
    serializer_class = ComplaintSerializer
