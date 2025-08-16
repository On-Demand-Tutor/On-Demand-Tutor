import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../auth';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './register.html',
  styleUrls: ['./register.css']
})
export class RegisterComponent {
  email = '';
  username = '';
  password = '';
  role = 'STUDENT';

  constructor(private authService: AuthService, private router: Router) {}

  register() {
    const newUser = {
      email: this.email,
      username: this.username,
      password: this.password,
      role: this.role
    };

    this.authService.register(newUser).subscribe({
      next: () => this.router.navigate(['/login']),
      error: err => alert('Đăng ký thất bại: ' + err.error)
    });
  }
}
