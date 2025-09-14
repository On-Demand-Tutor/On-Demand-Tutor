from django.test import TestCase
from unittest.mock import patch
from rest_framework.test import APIClient
from django.urls import reverse
from uuid import uuid4
from rest_framework import status


class RatingCreateTest(TestCase):
    @patch('feedback.serializers.requests.get')
    def test_create_rating_only_when_booking_done(self, mock_get):
        booking_id = uuid4()
        tutor_id = uuid4()
        student_id = uuid4()

        # Giả booking DONE
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
        self.assertEqual(res.status_code, status.HTTP_201_CREATED)

    @patch('feedback.serializers.requests.get')
    def test_reject_when_booking_not_done(self, mock_get):
        booking_id = uuid4()
        tutor_id = uuid4()
        student_id = uuid4()

        # Booking chưa done
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
        self.assertEqual(res.status_code, status.HTTP_400_BAD_REQUEST)
        # check lỗi có chứa "hoàn thành"
        self.assertTrue(any("hoàn thành" in str(msg) for msg in res.data.values()))


class ComplaintAPITests(TestCase):
    def setUp(self):
        self.client = APIClient()
        self.student_id = uuid4()
        self.tutor_id = uuid4()

    def _as_student(self):
        self.client.credentials(
            HTTP_X_USER_ROLE='student',
            HTTP_X_USER_ID=str(self.student_id)
        )

    def _as_moderator(self):
        # mặc định IsModeratorOrReadOnly cho phép role=moderator/admin
        self.client.credentials(
            HTTP_X_USER_ROLE='moderator',
            HTTP_X_USER_ID=str(uuid4())
        )

    def test_student_can_create_complaint(self):
        self._as_student()
        payload = {
            "student_id": str(self.student_id),
            "tutor_id": str(self.tutor_id),
            "content": "Tutor trả lời chậm."
        }
        res = self.client.post(reverse('complaint-list'), payload, format='json')
        self.assertEqual(res.status_code, status.HTTP_201_CREATED)
        self.assertEqual(res.data["status"], "open")
        self.assertEqual(res.data["student_id"], str(self.student_id))

    def test_only_moderator_can_update_status(self):
        # 1) student tạo complaint
        self._as_student()
        create_res = self.client.post(reverse('complaint-list'), {
            "student_id": str(self.student_id),
            "tutor_id": str(self.tutor_id),
            "content": "Ứng xử chưa tốt."
        }, format='json')
        self.assertEqual(create_res.status_code, status.HTTP_201_CREATED)
        cid = create_res.data["id"]

        # 2) student cố đổi status -> bị chặn (403)
        self._as_student()
        res_forbidden = self.client.put(
            reverse('complaint-set-status', kwargs={"pk": cid}),
            {"status": "in-review"},
            format='json'
        )
        self.assertIn(res_forbidden.status_code, (status.HTTP_403_FORBIDDEN, status.HTTP_400_BAD_REQUEST))

        # 3) moderator đổi status -> OK
        self._as_moderator()
        res_ok = self.client.put(
            reverse('complaint-set-status', kwargs={"pk": cid}),
            {"status": "in-review"},
            format='json'
        )
        self.assertEqual(res_ok.status_code, status.HTTP_200_OK)
        self.assertEqual(res_ok.data["status"], "in-review")
