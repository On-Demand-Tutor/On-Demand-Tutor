from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import RatingViewSet, ComplaintViewSet

router = DefaultRouter()
router.register(r'ratings', RatingViewSet, basename="rating")
router.register(r'complaints', ComplaintViewSet, basename="complaint")

urlpatterns = [
    path('', include(router.urls)),
]
