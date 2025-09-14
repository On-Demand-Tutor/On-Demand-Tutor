import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { AuthService, Role } from '../auth';

@Component({
  selector: 'app-update',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './update.html',
  styleUrls: ['./update.css']
})
export class UpdateComponent implements OnInit {
  userId: number | null = null;
  userRole: Role = '';

  username = ''; password = '';
  grade: number | null = null;
  qualifications = ''; skills = ''; teachingGrades = '';
  price: number | null = null; description = ''; avatar = '';

  isLoading = false; successMessage = ''; errorMessage = '';
  showAvatarMenu = false; toggleAvatarMenu() { this.showAvatarMenu = !this.showAvatarMenu; }

  constructor(private auth: AuthService) {}

  ngOnInit(): void {
    this.userRole = this.auth.ensureRoleFromTokenIfMissing();

    // 1) thử lấy userId từ token/localStorage
    const tryId = this.auth.ensureUserIdFromTokenIfMissing();
    if (tryId) { this.userId = tryId; this.loadUser(tryId); return; }

    // 2) fallback theo email
    const email = localStorage.getItem('login_email');
    if (!email) { this.errorMessage = 'Thiếu userId và email. Vui lòng đăng nhập lại.'; return; }

    this.isLoading = true;
    this.auth.getUserIdByEmail(email)
      .pipe(finalize(() => (this.isLoading = false)))
      .subscribe({
        next: (res: any) => {
          const u = res?.result ?? res?.data ?? res;
          const id = Number(u?.id ?? u?.userId ?? 0);
          if (!id) { this.errorMessage = 'Không tìm thấy user theo email.'; return; }
          localStorage.setItem('userId', String(id));
          this.userId = id;
          this.loadUser(id);
        },
        error: () => this.errorMessage = 'Không lấy được userId theo email.'
      });
  }

  private loadUser(id: number): void {
    this.isLoading = true;
    this.auth.getUserById(id)
      .pipe(finalize(() => (this.isLoading = false)))
      .subscribe({
        next: (res: any) => {
          const u = res?.result ?? res?.data ?? res;
          this.username       = u?.username ?? u?.name ?? '';
          this.grade          = u?.grade ?? null;
          this.qualifications = u?.qualifications ?? '';
          this.skills         = u?.skills ?? '';
          this.teachingGrades = u?.teachingGrades ?? '';
          this.price          = u?.price ?? null;
          this.description    = u?.description ?? '';
          this.avatar         = u?.avatar ?? '';
        },
        error: (err) => {
          console.error('getUser error', err);
          this.errorMessage = err?.status === 401
            ? 'Bạn chưa đăng nhập hoặc token đã hết hạn.'
            : 'Không tải được thông tin người dùng.';
        }
      });
  }

  updateProfile(): void {
    if (!this.userId) { this.errorMessage = 'Không tìm thấy userId.'; return; }
    this.successMessage = ''; this.errorMessage = ''; this.isLoading = true;

    const body: any = { username: this.username?.trim() };
    if (this.password?.trim()) body.password = this.password.trim();
    if (this.userRole === 'student') body.grade = this.grade ?? null;
    if (this.userRole === 'tutor') Object.assign(body, {
      qualifications: this.qualifications?.trim(),
      skills: this.skills?.trim(),
      teachingGrades: this.teachingGrades?.trim(),
      price: this.price ?? 0,
      description: this.description?.trim(),
      avatar: this.avatar?.trim()
    });

    this.auth.updateProfile(this.userId, body)
      .pipe(finalize(() => (this.isLoading = false)))
      .subscribe({
        next: () => { this.successMessage = 'Cập nhật hồ sơ thành công!'; this.password = ''; },
        error: (err) => {
          console.error('update error', err);
          this.errorMessage =
            err?.status === 401 ? 'Bạn chưa đăng nhập hoặc token đã hết hạn.'
          : err?.status === 400 ? 'Dữ liệu không hợp lệ.'
          : 'Cập nhật thất bại. Vui lòng thử lại.';
        }
      });
  }

  logout(): void {
    this.auth.logout();
    window.location.href = '/login';
  }
}