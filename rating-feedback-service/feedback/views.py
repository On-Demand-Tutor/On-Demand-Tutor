from django.shortcuts import render
from rest_framework import viewsets
from .models import Rating, Complaint
from .serializers import RatingSerializer, ComplaintSerializer

class RatingViewSet(viewsets.ModelViewSet):
    queryset = Rating.objects.all()
    serializer_class = RatingSerializer

class ComplaintViewSet(viewsets.ModelViewSet):
    queryset = Complaint.objects.all()
    serializer_class = ComplaintSerializer
