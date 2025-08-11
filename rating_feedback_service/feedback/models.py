from django.db import models

class Rating(models.Model):
    tutor_id = models.IntegerField()  # ID của tutor
    student_id = models.IntegerField()  # ID của student
    score = models.IntegerField()  # 1-5
    comment = models.TextField(blank=True)
    created_at = models.DateTimeField(auto_now_add=True)

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
