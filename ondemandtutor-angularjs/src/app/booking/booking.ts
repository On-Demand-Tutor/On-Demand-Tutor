import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { HttpClient, HttpClientModule, HttpHeaders } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

type TutorDto = {
  id: number;
  userId?: number;
  user_id?: number;
  username?: string;
  email?: string;
  promoFile?: string;
};

@Component({
  selector: 'app-booking',
  standalone: true,
  imports: [CommonModule, HttpClientModule, FormsModule],
  templateUrl: './booking.html',
  styleUrls: ['./booking.css']
})
export class BookingComponent implements OnInit {
  tutor: TutorDto | null = null;
  tutorUserId: number | null = null;

  startTime = '';
  endTime = '';
  message = '';
  isSubmitting = false;

  constructor(private route: ActivatedRoute, private http: HttpClient) {}

  ngOnInit(): void {
    const userId = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isFinite(userId) || userId <= 0) {
      this.message = 'ID gia sư không hợp lệ.'; return;
    }

    // Lấy record tutor từ tutor-service theo userId
    this.http.get<any>(`/api/tutors/user/${userId}`).subscribe({
      next: (res) => {
        const t: TutorDto = res?.result ?? res;
        this.tutor = t;
        this.tutorUserId = t?.userId ?? t?.user_id ?? userId;
        this.prefillTimes();
      },
      error: (err: any) => {
        console.error('Load tutor error:', err);
        this.message = 'Không tải được thông tin gia sư .';
      }
    });
  }

  private prefillTimes() {
    const now = new Date();
    now.setMinutes(now.getMinutes() + (15 - (now.getMinutes() % 15)) % 15, 0, 0);
    const later = new Date(now.getTime() + 60 * 60 * 1000);
    const fmt = (d: Date) => {
      const p = (n: number) => String(n).padStart(2, '0');
      return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}`;
    };
    this.startTime = fmt(now);
    this.endTime = fmt(later);
  }

  private withSeconds(v: string) { return v && v.length === 16 ? `${v}:00` : v; }

  onSubmit(): void {
    if (!this.tutorUserId) { this.message = 'Thiếu tutorUserId để tạo booking.'; return; }
    if (!this.startTime || !this.endTime) { this.message = 'Vui lòng nhập đầy đủ thời gian.'; return; }
    if (new Date(this.startTime) >= new Date(this.endTime)) { this.message = 'Giờ kết thúc phải sau giờ bắt đầu.'; return; }

    const payload = {
      startTime: this.withSeconds(this.startTime),
      endTime: this.withSeconds(this.endTime)
    };

    const token =
      localStorage.getItem('jwt') ||
      localStorage.getItem('access_token') || '';

    const headers = token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : undefined;

    this.isSubmitting = true;
    this.message = 'Đang gửi...';

    this.http.post(`/api/bookings/create/${this.tutorUserId}`, payload, { headers }).subscribe({
      next: (res: any) => {
        this.message = res?.message || 'Đặt lịch thành công!';
        this.isSubmitting = false;
      },
      error: (err: any) => {
        console.error('Booking error:', err);
        this.message = err?.error?.message || 'Không kết nối được .';
        this.isSubmitting = false;
      }
    });
  }
}
