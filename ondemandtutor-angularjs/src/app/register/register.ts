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
  // form inputs
  email = '';
  username = '';
  password = '';

  // select role trong template
  newUser: { role: RawRole } = { role: 'STUDENT' };

  // UI state
  isLoading = false;
  errorMessage = '';
  successMessage = '';          // nếu cần hiển thị trong chính trang này
  registrationSuccess = false;  // không bắt buộc

  // lỗi từng trường
  emailError = '';
  usernameError = '';
  passwordError = '';

  constructor(private auth: AuthService, private router: Router) {}

  // reset thông báo
  clearMessages() {
    this.errorMessage = '';
    this.successMessage = '';
    this.emailError = this.usernameError = this.passwordError = '';
  }

  // kiểm tra form trước khi gọi BE
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

    if (!ok) this.errorMessage = 'Vui lòng điền đầy đủ thông tin bên trên.';
    return ok;
  }

  // template gọi (ngSubmit)="register()"
  register(): void {
    this.clearMessages();

    if (!this.validateForm()) return;

    this.isLoading = true;

    const rawRoleForBE: RawRole =
      (this.newUser?.role || 'STUDENT') as RawRole;

   this.auth.register(this.username, this.email, this.password, rawRoleForBE)
  .pipe(finalize(() => { 
    this.isLoading = false; 
    this.password = ''; // ✅ clear mật khẩu sau khi gọi API
  }))
  .subscribe({
    next: () => {
      this.router.navigate(['/login'], {
        state: { justRegistered: true, email: this.email }
      });
    },
    error: (err) => {
      if (err?.status === 409)      this.errorMessage = 'Email đã tồn tại trong hệ thống.';
      else if (err?.status === 400) this.errorMessage = 'Thông tin chưa hợp lệ. Vui lòng kiểm tra lại.';
      else if (err?.status === 0)   this.errorMessage = 'Không thể kết nối máy chủ. Vui lòng thử lại.';
      else                          this.errorMessage = 'Đăng ký thất bại. Vui lòng thử lại.';
    }
  });
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }
}
