import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

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

  constructor(private router: Router) {}

  login() {
    // Reset messages
    this.errorMessage = '';
    this.successMessage = '';

    // Basic validation
    if (!this.email || !this.password) {
      this.errorMessage = 'Vui lòng nhập đầy đủ email và mật khẩu';
      return;
    }

    // Email format validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.email)) {
      this.errorMessage = 'Email không hợp lệ';
      return;
    }

    // Start loading
    this.isLoading = true;

    // Giả lập API call với setTimeout
    setTimeout(() => {
      if (this.isValidLogin()) {
        // Đăng nhập thành công
        this.successMessage = 'Đăng nhập thành công! Đang chuyển hướng...';
        this.isLoading = false;

        // Lưu thông tin đăng nhập
        this.saveLoginInfo();

        // Chuyển hướng sau 1.5 giây
        setTimeout(() => {
          this.router.navigate(['/home']);
        }, 1500);
      } else {
        // Đăng nhập thất bại
        this.isLoading = false;
        this.errorMessage = 'Email hoặc mật khẩu không đúng';
      }
    }, 1000);
  }

  private isValidLogin(): boolean {
    // Logic chỉ kiểm tra tài khoản demo đã được định nghĩa sẵn
    const demoAccounts = [
      { email: 'student@demo.com', password: 'student123' },
      { email: 'tutor@demo.com', password: 'tutor123' },
      { email: 'moderator@demo.com', password: 'moderator123' }
    ];

    const demoAccount = demoAccounts.find(
      account => account.email.toLowerCase() === this.email.toLowerCase() &&
                 account.password === this.password
    );

    if (demoAccount) {
      return true;
    }

    return false;
  }

  private saveLoginInfo(): void {
    // Tạo token giả
    const token = 'demo-token-' + Date.now();

    // Tạo user info từ email
    const username = this.email.split('@')[0];
    const user = {
      email: this.email,
      name: username.charAt(0).toUpperCase() + username.slice(1),
      loginTime: new Date().toISOString()
    };

    // Lưu vào localStorage
    localStorage.setItem('token', token);
    localStorage.setItem('userData', JSON.stringify(user));
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
