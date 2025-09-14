from django.db import models
from django.core.validators import MinValueValidator, MaxValueValidator
import uuid

class Rating(models.Model):
    booking_id = models.UUIDField(null=True, blank=True)  # ID của booking bên service Booking
    tutor_id = models.UUIDField()  # ID của tutor
    student_id = models.UUIDField()  # ID của student
    score = models.IntegerField(validators=[MinValueValidator(1), MaxValueValidator(5)])  # 1-5
    comment = models.TextField(blank=True)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = "ratings"
        indexes = [
            models.Index(fields=["tutor_id"]),
            models.Index(fields=["student_id"]),
            models.Index(fields=["booking_id"]),
        ]
        constraints = [
            models.UniqueConstraint(
                fields=["booking_id", "student_id"], name="uniq_rating_per_booking_student"
            )
        ]

    def __str__(self):
        return f"Tutor {self.tutor_id} - {self.score} stars"

class Complaint(models.Model):
    class Status(models.TextChoices):
        OPEN = "open", "Open"
        IN_REVIEW = "in-review", "In Review"
        RESOLVED = "resolved", "Resolved"

    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    tutor_id = models.UUIDField()
    student_id = models.UUIDField()
    content = models.TextField()
    status = models.CharField(
        max_length=20,
        choices=Status.choices,
        default=Status.OPEN,
    )
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = "complaints"
        ordering = ["-created_at"]
        indexes = [
            models.Index(fields=["tutor_id"]),
            models.Index(fields=["student_id"]),
            models.Index(fields=["status"]),
        ]

    def __str__(self):
        return f"{self.id} • {self.status}"

class Tutor(models.Model):
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    name = models.CharField(max_length=255)  # Giữ lại id nếu đồng bộ từ user-service
    average_score = models.FloatField(default=0.0)

    class Meta:
        db_table = "tutors"

    def __str__(self):
        return f"{self.id} - {self.average_score}"
