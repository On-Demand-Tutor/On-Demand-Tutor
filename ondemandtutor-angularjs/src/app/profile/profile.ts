import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { AuthService, Role } from '../auth';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './profile.html',
  styleUrls: ['./profile.css']
})
export class ProfileComponent implements OnInit {
  // dữ liệu user
  userId: number | null = null;
  userRole: Role = '';
  username = '';
  grade: number | null = null;
  qualifications = '';
  skills = '';
  teachingGrades = '';
  price: number | null = null;
  description = '';
  avatar = '';

  // trạng thái UI
  isLoading = false;
  errorMessage = '';

  constructor(private auth: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.userRole = this.auth.getUserRole() || this.auth.ensureRoleFromTokenIfMissing();
    this.userId = this.auth.ensureUserIdFromTokenIfMissing();

    if (!this.userId) {
      this.errorMessage = 'Không tìm thấy userId. Vui lòng đăng nhập lại.';
      return;
    }
    this.loadProfile(this.userId);
  }

  private loadProfile(id: number): void {
    this.isLoading = true;
    this.auth.getUserById(id)
      .pipe(finalize(() => (this.isLoading = false)))
      .subscribe({
        next: (res: any) => {
          const u = res?.result ?? res?.data ?? res;
          this.username       = u?.username ?? '';
          this.grade          = u?.grade ?? null;
          this.qualifications = u?.qualifications ?? '';
          this.skills         = u?.skills ?? '';
          this.teachingGrades = u?.teachingGrades ?? '';
          this.price          = u?.price ?? null;
          this.description    = u?.description ?? '';
          this.avatar         = u?.avatar ?? '';
        },
        error: () => this.errorMessage = 'Không tải được thông tin người dùng.'
      });
  }

  goUpdateProfile(): void {
    this.router.navigate(['/update']);
  }

  // ✅ Điều hướng tới màn chat danh sách (không truyền id)
  goChatList(): void {
    this.router.navigate(['/chat']); // cần route '/chat' (không id)
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
