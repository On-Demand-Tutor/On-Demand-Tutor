import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { AuthService } from '../auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class LoginComponent implements OnInit {
  email = '';
  password = '';
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  constructor(private auth: AuthService, private router: Router) {}

  ngOnInit(): void {
    // nếu vừa chuyển từ trang đăng ký:
    const state = history.state as any;
    if (state?.justRegistered) {
      this.successMessage = 'Tạo tài khoản thành công! Vui lòng đăng nhập.';
      if (state.email) this.email = state.email;
      history.replaceState({}, '');
    }
  }

  clearMessages() {
    this.errorMessage = '';
    this.successMessage = '';
  }

  onSubmit(e: Event) {
    e.preventDefault();
    this.clearMessages();

    // ===== Basic validation =====
    const email = (this.email || '').trim();
    const password = (this.password || '').trim();

    if (!email || !password) {
      this.errorMessage = 'Vui lòng nhập đầy đủ email và mật khẩu';
      return;
    }

    // ===== Email format validation =====
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      this.errorMessage = 'Email không hợp lệ';
      return;
    }

    // ===== Call API thật =====
    this.isLoading = true;

   this.auth.login(email, password)
  .pipe(finalize(() => { 
    this.isLoading = false; 
    this.password = ''; // ✅ không giữ mật khẩu trong state sau request
  }))
  .subscribe({
    next: () => {
      this.auth.ensureRoleFromTokenIfMissing();
      this.successMessage = 'Đăng nhập thành công! Đang chuyển hướng...';
      setTimeout(() => this.router.navigateByUrl('/home', { replaceUrl: true }), 800); // ✅ tránh back về /login
    },
    error: (err) => {
      // ✅ đừng show err.error.message thô
      if (err?.status === 0)       this.errorMessage = 'Không kết nối được máy chủ. Vui lòng thử lại.';
      else if (err?.status === 401) this.errorMessage = 'Email hoặc mật khẩu không đúng.';
      else if (err?.status === 400) this.errorMessage = 'Thông tin đăng nhập chưa hợp lệ.';
      else                          this.errorMessage = 'Đăng nhập thất bại. Vui lòng thử lại.';
    }
  });
  }
}
