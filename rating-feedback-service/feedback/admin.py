from django.contrib import admin
from .models import Rating, Complaint


@admin.register(Rating)
class RatingAdmin(admin.ModelAdmin):
    list_display = ("id", "tutor_id", "student_id", "score", "comment", "created_at")
    list_filter = ("score", "created_at")
    search_fields = ("tutor_id", "student_id", "comment")
    ordering = ("-created_at",)


@admin.register(Complaint)
class ComplaintAdmin(admin.ModelAdmin):
    list_display = ("id", "student_id", "tutor_id", "status", "created_at", "updated_at")
    list_filter = ("status", "created_at")
    search_fields = ("id", "student_id", "tutor_id", "content")
    ordering = ("-created_at",)
    actions = ["mark_in_review", "mark_resolved", "mark_open"]

    @admin.action(description="Mark selected as In Review")
    def mark_in_review(self, request, queryset):
        queryset.update(status=Complaint.Status.IN_REVIEW)

    @admin.action(description="Mark selected as Resolved")
    def mark_resolved(self, request, queryset):
        queryset.update(status=Complaint.Status.RESOLVED)

    @admin.action(description="Mark selected as Open")
    def mark_open(self, request, queryset):
        queryset.update(status=Complaint.Status.OPEN)
