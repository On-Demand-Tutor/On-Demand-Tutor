from django.test import TestCase
from unittest.mock import patch
from rest_framework.test import APIClient
from django.urls import reverse
from uuid import uuid4


class RatingCreateTest(TestCase):
    @patch('feedback.serializers.requests.get')
    def test_create_rating_only_when_booking_done(self, mock_get):
        booking_id = uuid4()
        tutor_id = uuid4()
        student_id = uuid4()

        # giả booking DONE
        mock_get.return_value.status_code = 200
        mock_get.return_value.json.return_value = {
            "id": str(booking_id),
            "student_id": str(student_id),
            "tutor_id": str(tutor_id),
            "status": "done"
        }
        mock_get.return_value.raise_for_status = lambda: None

        client = APIClient()
        client.credentials(
            HTTP_X_USER_ROLE='student',
            HTTP_X_USER_ID=str(student_id)
        )
        payload = {
            "booking_id": str(booking_id),
            "student_id": str(student_id),
            "tutor_id": str(tutor_id),
            "score": 5,
            "comment": "Great!"
        }
        res = client.post(reverse('rating-list'), payload, format='json')
        print(">>> DEBUG DONE:", res.status_code, res.data)   # debug
        self.assertEqual(res.status_code, 201)

    @patch('feedback.serializers.requests.get')
    def test_reject_when_booking_not_done(self, mock_get):
        booking_id = uuid4()
        tutor_id = uuid4()
        student_id = uuid4()

        # booking chưa done
        mock_get.return_value.status_code = 200
        mock_get.return_value.json.return_value = {
            "id": str(booking_id),
            "student_id": str(student_id),
            "tutor_id": str(tutor_id),
            "status": "in-progress"
        }
        mock_get.return_value.raise_for_status = lambda: None

        client = APIClient()
        client.credentials(
            HTTP_X_USER_ROLE='student',
            HTTP_X_USER_ID=str(student_id)
        )
        payload = {
            "booking_id": str(booking_id),
            "student_id": str(student_id),
            "tutor_id": str(tutor_id),
            "score": 4,
            "comment": "Not done"
        }
        res = client.post(reverse('rating-list'), payload, format='json')
        print(">>> DEBUG NOT DONE:", res.status_code, res.data)  # debug
        self.assertEqual(res.status_code, 400)
        # check lỗi có chứa "hoàn thành"
        self.assertTrue(any("hoàn thành" in str(msg) for msg in res.data.values()))
