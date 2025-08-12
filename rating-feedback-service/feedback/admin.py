from django.contrib import admin
from .models import Rating, Complaint

@admin.register(Rating)
class RatingAdmin(admin.ModelAdmin):
    list_display = ('id', 'tutor_id', 'student_id', 'score', 'comment', 'created_at')
    list_filter = ('score', 'created_at')

@admin.register(Complaint)
class ComplaintAdmin(admin.ModelAdmin):
    list_display = ('id', 'tutor_id', 'student_id', 'status', 'created_at')
    list_filter = ('status', 'created_at')