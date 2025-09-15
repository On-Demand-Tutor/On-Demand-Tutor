// src/app/core/rating.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class RatingService {
  constructor(private http: HttpClient) {}

  // Kiểm tra quyền đánh giá từ BE
  canRate(tutorUserId: number) {
    return this.http.get<boolean>(`/api/bookings/can-rate/${tutorUserId}`);
  }

  // Gửi đánh giá
  rate(tutorUserId: number, rating: number, comment: string) {
    return this.http.post(
      `/api/students/rating/${tutorUserId}`,
      { rating, comment },
      { responseType: 'text' }
    );
  }
}
