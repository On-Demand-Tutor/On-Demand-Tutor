import { Component, OnInit, HostListener } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { AuthService, Role } from '../auth';

@Component({
  selector: 'app-update',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterModule],
  templateUrl: './update.html',
  styleUrls: ['./update.css']
})
export class UpdateComponent implements OnInit {
  // common fields
  username = '';
  password = '';

  // role-specific
  grade: number | null = null;      // student
  qualifications = '';              // tutor
  skills = '';                      // tutor
  teachingGrades = '';              // tutor
  price: number | null = null;

  userRole: Role = '';
  isLoggedIn = false;

  isFetching = false;  // load profile
  isLoading  = false;  // save

  successMessage = '';
  errorMessage = '';
  isAvatarMenuOpen = false;

  constructor(public authService: AuthService, private router: Router) {}

  ngOnInit() {
    this.isLoggedIn = this.authService.isLoggedIn();
    if (!this.isLoggedIn) return;

    // đảm bảo có role & userId
    this.userRole = this.authService.getUserRole() || this.authService.ensureRoleFromTokenIfMissing();
    this.authService.ensureUserIdFromTokenIfMissing();

    const uid = this.authService.getUserId();
    if (uid != null) this.loadProfile(uid);
  }

  // (tuỳ chọn) nếu BE có GET /api/users/{id}
  loadProfile(userId: number) {
    this.isFetching = true;
    this.authService.getUserById(userId)
      .pipe(finalize(() => (this.isFetching = false)))
      .subscribe({
        next: (u: any) => {
          if (!this.userRole && u?.role) {
            this.userRole = String(u.role).toLowerCase() as Role;
            this.authService.setUserRole(this.userRole);
          }
          this.username = u?.username ?? this.username;

          if (this.userRole === 'student') {
            const g = u?.grade;
            this.grade = (g === null || g === undefined) ? this.grade : Number(g);
          } else if (this.userRole === 'tutor') {
            this.qualifications = u?.qualifications ?? this.qualifications;
            this.skills = u?.skills ?? this.skills;
            this.teachingGrades = u?.teachingGrades ?? this.teachingGrades;
            this.price = u?.price != null ? Number(u.price) : this.price;
          }
        },
        error: (_err: any) => { /* bỏ qua nếu BE chưa có endpoint */ }
      });
  }

  private buildPayload(): any {
    const payload: any = {};
    const username = this.username?.trim();
    const password = this.password?.trim();

    if (username) payload.username = username;
    if (password) payload.password = password;

    if (this.userRole === 'student') {
      if (this.grade !== null && !Number.isNaN(Number(this.grade))) {
        payload.grade = Number(this.grade);
      }
    } else if (this.userRole === 'tutor') {
      if (this.qualifications.trim()) payload.qualifications = this.qualifications.trim();
      if (this.skills.trim())          payload.skills = this.skills.trim();
      if (this.teachingGrades.trim())  payload.teachingGrades = this.teachingGrades.trim();
      if (this.price !== null && !Number.isNaN(this.price)) {
        payload.price = Number(this.price);
      }
    }
    return payload;
  }

  updateProfile() {
    this.successMessage = '';
    this.errorMessage = '';

    const userId = this.authService.ensureUserIdFromTokenIfMissing() ?? this.authService.getUserId();
    if (userId == null) {
      this.errorMessage = 'Thiếu userId, vui lòng đăng nhập lại.';
      return;
    }

    // validate client (giúp tránh 400 không đáng có)
    if (this.userRole === 'student' && this.grade !== null) {
      const g = Number(this.grade);
      if (Number.isNaN(g) || g < 1 || g > 12) {
        this.errorMessage = 'Grade phải là số từ 1 đến 12.';
        return;
      }
    }

    const payload = this.buildPayload();
    if (Object.keys(payload).length === 0) {
      this.errorMessage = 'Không có thay đổi để lưu.';
      return;
    }

    this.isLoading = true;
    this.authService.updateProfile(userId, payload)
      .pipe(finalize(() => (this.isLoading = false)))
      .subscribe({
        next: () => {
          this.successMessage = 'Cập nhật hồ sơ thành công!';
          this.password = ''; // không lưu mật khẩu ở UI
        },
        error: (err: any) => {
          const raw =
            err?.error?.message ||
            (typeof err?.error === 'string' ? err.error : '') ||
            err?.statusText ||
            '';

          if (err?.status === 0)        this.errorMessage = 'Không kết nối được máy chủ.';
          else if (err?.status === 400) this.errorMessage = raw || 'Dữ liệu không hợp lệ.';
          else if (err?.status === 401) this.errorMessage = raw || 'Bạn chưa đăng nhập.';
          else if (err?.status === 404) this.errorMessage = raw || 'Không tìm thấy tài nguyên.';
          else if (err?.status === 500) this.errorMessage = raw || 'Lỗi máy chủ khi cập nhật.';
          else this.errorMessage = raw || 'Cập nhật thất bại.';
        }
      });
  }

  toggleAvatarMenu() { this.isAvatarMenuOpen = !this.isAvatarMenuOpen; }
  logout() { this.authService.logout(); this.router.navigate(['/login']); }

  @HostListener('document:click', ['$event'])
  onDocClick(_e: MouseEvent) { if (this.isAvatarMenuOpen) this.isAvatarMenuOpen = false; }
}
