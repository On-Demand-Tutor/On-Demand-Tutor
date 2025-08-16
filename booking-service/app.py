from fastapi import FastAPI
from fastapi.responses import JSONResponse

app = FastAPI()

# booking mẫu lưu trong RAM
MOCK_BOOKINGS = {
    "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa": {
        "id": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
        "student_id": "11111111-1111-1111-1111-111111111111",
        "tutor_id":   "22222222-2222-2222-2222-222222222222",
        "status": "done"
    },
    "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb": {
        "id": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
        "student_id": "11111111-1111-1111-1111-111111111111",
        "tutor_id":   "22222222-2222-2222-2222-222222222222",
        "status": "in-progress"
    }
}

@app.get("/bookings/{booking_id}")
def get_booking(booking_id: str):
    if booking_id in MOCK_BOOKINGS:
        return MOCK_BOOKINGS[booking_id]
    return JSONResponse({"detail": "Not found"}, status_code=404)
