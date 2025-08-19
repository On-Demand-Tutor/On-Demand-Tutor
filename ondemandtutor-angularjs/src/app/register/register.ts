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
    // Validate input - kiểm tra thông tin đầu vào
    if (!this.email || !this.username || !this.password) {
      this.errorMessage = 'Please fill in all required fields';
      return;
    }

    // Validate email format
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.email)) {
      this.errorMessage = 'Please enter a valid email address';
      return;
    }

    // Clear error message và bắt đầu loading
    this.errorMessage = '';
    this.isLoading = true;

    const newUser = {
      email: this.email,
      username: this.username,
      password: this.password,
      role: this.role
    };

    this.authService.register(newUser).subscribe({
      next: () => {
        this.isLoading = false;
        this.registrationSuccess = true;
      },
      error: err => {
        this.isLoading = false;

        // Handle different types of errors
        if (err && err.error) {
          if (typeof err.error === 'string') {
            this.errorMessage = err.error;
          } else if (err.error.message) {
            this.errorMessage = err.error.message;
          } else if (err.status === 409) {
            this.errorMessage = 'Email or username already exists';
          } else if (err.status === 400) {
            this.errorMessage = 'Invalid registration data';
          } else {
            this.errorMessage = 'Registration failed. Please try again.';
          }
        } else {
          this.errorMessage = 'Network error. Please check your connection and try again.';
        }

        console.error('Registration failed:', err);
      }
    });
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }
}
