from fastapi import FastAPI
from fastapi.responses import JSONResponse

app = FastAPI()

# booking mẫu lưu trong RAM
MOCK_BOOKINGS = {
    # Case 1: DONE - hợp lệ
    "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa": {
        "id": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
        "student_id": "11111111-1111-1111-1111-111111111111",
        "tutor_id":   "22222222-2222-2222-2222-222222222222",
        "status": "done"
    },
    # Case 2: IN-PROGRESS - chưa hoàn thành
    "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb": {
        "id": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
        "student_id": "11111111-1111-1111-1111-111111111111",
        "tutor_id":   "22222222-2222-2222-2222-222222222222",
        "status": "in-progress"
    },
    # Case 3: CANCELED - đã hủy
    "cccccccc-cccc-cccc-cccc-cccccccccccc": {
        "id": "cccccccc-cccc-cccc-cccc-cccccccccccc",
        "student_id": "11111111-1111-1111-1111-111111111111",
        "tutor_id":   "22222222-2222-2222-2222-222222222222",
        "status": "canceled"
    },
    # Case 4: PENDING - chưa bắt đầu
    "dddddddd-dddd-dddd-dddd-dddddddddddd": {
        "id": "dddddddd-dddd-dddd-dddd-dddddddddddd",
        "student_id": "11111111-1111-1111-1111-111111111111",
        "tutor_id":   "22222222-2222-2222-2222-222222222222",
        "status": "pending"
    },
    # Case 5: Sai student/tutor
    "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee": {
        "id": "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee",
        "student_id": "99999999-9999-9999-9999-999999999999",  # khác student
        "tutor_id":   "88888888-8888-8888-8888-888888888888",  # khác tutor
        "status": "done"
    },
    # Tạo thêm đánh giá với status = done:
    "ffffffff-ffff-ffff-ffff-ffffffffffff": {
    "id": "ffffffff-ffff-ffff-ffff-ffffffffffff",
    "student_id": "33333333-3333-3333-3333-333333333333",
    "tutor_id":   "22222222-2222-2222-2222-222222222222",
    "status": "done"
    }
}

@app.get("/bookings/{booking_id}")
def get_booking(booking_id: str):
    if booking_id in MOCK_BOOKINGS:
        return MOCK_BOOKINGS[booking_id]
    return JSONResponse({"detail": "Not found"}, status_code=404)
