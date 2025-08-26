import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class LoginComponent {
  email = '';
  password = '';
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  constructor(private router: Router, private authService: AuthService) {}

  login() {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.email || !this.password) {
      this.errorMessage = 'Vui lòng nhập đầy đủ email và mật khẩu';
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.email)) {
      this.errorMessage = 'Email không hợp lệ';
      return;
    }

    this.isLoading = true;

    // ✅ Gọi API thật thay vì hardcode
    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: (res) => {
        this.isLoading = false;

        // BE trả về ApiResponse<AuthenticationResponse>
        const token = res.result?.token;
        if (token) {
          this.authService.setToken(token);
        }

        this.successMessage = 'Đăng nhập thành công! Đang chuyển hướng...';
        setTimeout(() => this.router.navigate(['/home']), 1500);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = 'Email hoặc mật khẩu không đúng';
        console.error(err);
      }
    });
  }

  onSubmit(event: Event) {
    event.preventDefault();
    this.login();
  }

  clearMessages() {
    this.errorMessage = '';
    this.successMessage = '';
  }
}
