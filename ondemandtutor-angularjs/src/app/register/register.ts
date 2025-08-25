import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../auth';
import { RouterModule } from '@angular/router';

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
  role = 'STUDENT';
  registrationSuccess = false;
  errorMessage = '';

  isLoading = false;

  constructor(private authService: AuthService, private router: Router) {}

 register() {
    if (!this.email || !this.username || !this.password) {
      this.errorMessage = 'Please fill in all required fields';
      return;

    }

    const newUser = {
      email: this.email,
      username: this.username,
      password: this.password,
      role: this.role
    };

    this.errorMessage = '';
    this.isLoading = true;

    this.authService.register(newUser).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.registrationSuccess = true;

        // Nếu BE trả về token → lưu token và login luôn
        if (res.token) {
          this.authService.setToken(res.token);
          this.router.navigate(['/home']);
        } else {
          // Nếu BE chỉ tạo user mà không trả token → quay về login
          setTimeout(() => this.router.navigate(['/login']), 2000);
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage =
          err.error?.message ||
          'Registration failed. Please try again.';
      }
    });
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }
}
