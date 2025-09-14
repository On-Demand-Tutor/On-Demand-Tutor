import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { UserService, UserProfile } from '../core/user.service';

@Component({
  selector: 'app-tutor-profile',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './tutor_profile.html',
  styleUrls: ['./tutor_profile.css']
})
export class TutorProfileComponent implements OnInit {
  tutor: (UserProfile & { promoFiles: string[] }) | null = null;
  isLoading = false;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const userId = Number(params.get('id'));
      if (!userId) {
        this.errorMessage = 'ID không hợp lệ';
        return;
      }

      this.isLoading = true;
      this.userService.getUserById(userId).subscribe({
        next: (res: any) => {
          console.log('User API raw:', res);
          const u = res?.result ?? res;


          // // lấy danh sách file fake trong localStorage
           const stored = localStorage.getItem(`promoFiles_user_${userId}`);
           const fakeUrls: string[] = stored ? JSON.parse(stored) : [];

          // // gán tutor, luôn khởi tạo promoFiles thành mảng
           this.tutor = {
             ...u,
             promoFiles: fakeUrls.length > 0 ? fakeUrls : (u?.promoFiles ?? [])
           };

          this.isLoading = false;
        },
        error: (err) => {
          console.error('User API error:', err);
          this.errorMessage = 'Không tải được thông tin người dùng';
          this.isLoading = false;
        }
      });
    });
  }
}
