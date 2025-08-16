from django.db import models
from django.core.validators import MinValueValidator, MaxValueValidator

class Rating(models.Model):
    booking_id = models.IntegerField(null=True, blank=True)  # ID của booking bên service Booking
    tutor_id = models.IntegerField()  # ID của tutor
    student_id = models.IntegerField()  # ID của student
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
    tutor_id = models.IntegerField()
    student_id = models.IntegerField()
    description = models.TextField()
    status = models.CharField(
        max_length=20,
        choices=[
            ('pending', 'Pending'),
            ('resolved', 'Resolved'),
            ('rejected', 'Rejected'),
        ],
        default='pending'
    )
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"Complaint {self.id} - {self.status}"
