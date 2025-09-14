import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { AuthService } from '../auth';

type RawRole = 'STUDENT' | 'TUTOR' | 'MODERATOR';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterModule],
  templateUrl: './register.html',
  styleUrls: ['./register.css']
})
export class RegisterComponent {
  email = '';
  username = '';
  password = '';
  newUser: { role: RawRole } = { role: 'STUDENT' };

  isLoading = false;
  errorMessage = '';
  successMessage = '';
  registrationSuccess = false;

  emailError = '';
  usernameError = '';
  passwordError = '';

  constructor(private auth: AuthService, private router: Router) {}

  clearMessages() {
    this.errorMessage = '';
    this.successMessage = '';
    this.emailError = this.usernameError = this.passwordError = '';
  }

  private validateForm(): boolean {
    this.emailError = this.usernameError = this.passwordError = '';
    const email = (this.email || '').trim();
    const username = (this.username || '').trim();
    const password = (this.password || '').trim();

    let ok = true;
    if (!email) { this.emailError = 'Vui lòng nhập email.'; ok = false; }
    else {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(email)) { this.emailError = 'Email không hợp lệ.'; ok = false; }
    }

    if (!username) { this.usernameError = 'Vui lòng nhập username.'; ok = false; }
    if (!password) { this.passwordError = 'Vui lòng nhập mật khẩu.'; ok = false; }

    if (!ok) this.errorMessage = 'Vui lòng điền đầy đủ thông tin.';
    return ok;
  }

register(): void {
  this.clearMessages();
  if (!this.validateForm()) return;

  // ✅ xoá token cũ để interceptor KHÔNG gắn Authorization cho /register
  localStorage.removeItem('jwt');
  localStorage.removeItem('access_token');

  // (tuỳ chọn) lưu email để về sau fallback lấy userId theo email
  localStorage.setItem('login_email', (this.email || '').trim());

  this.isLoading = true;
  const rawRoleForBE: RawRole = (this.newUser?.role || 'STUDENT') as RawRole;

  this.auth.register(this.username, this.email, this.password, rawRoleForBE)
    .pipe(finalize(() => {
      this.isLoading = false;
      this.password = ''; // không giữ mật khẩu trong state
    }))
    .subscribe({
      next: () => {
        this.router.navigate(['/login'], {
          state: { justRegistered: true, email: this.email }
        });
      },
      error: (err) => {
        if (err?.status === 409)      this.errorMessage = 'Email đã tồn tại.';
        else if (err?.status === 400) this.errorMessage = 'Thông tin chưa hợp lệ.';
        else if (err?.status === 0)   this.errorMessage = 'Không thể kết nối máy chủ.';
        else                          this.errorMessage = 'Đăng ký thất bại.';
      }
    });
}

  goToLogin() {
    this.router.navigate(['/login']);
  }
}
