import { Component, OnInit, HostListener } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../auth';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterModule],
  templateUrl: './profile.html',
  styleUrls: ['./profile.css']
})
export class ProfileComponent implements OnInit {
  fullName = '';
  email = '';
  phone = '';
  address = '';

  successMessage = '';
  errorMessage = '';
  isLoading = false;
  isAvatarMenuOpen = false;

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit() {
    // Load user data
    const userId = Number(localStorage.getItem('userId'));
    this.authService.getUserById(userId).subscribe({
      next: (user) => {
        this.fullName = user.fullName || '';
        this.email = user.email || '';
        this.phone = user.phone || '';
        this.address = user.address || '';
      },
      error: () => {
        this.errorMessage = 'Không thể tải thông tin người dùng';
      }
    });
  }

  toggleAvatarMenu() {
    this.isAvatarMenuOpen = !this.isAvatarMenuOpen;
  }

  @HostListener('document:click', ['$event'])
  onOutsideClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (!target.closest('.avatar-menu')) {
      this.isAvatarMenuOpen = false;
    }
  }

  updateProfile() {
    if (!this.fullName || !this.email) {
      this.errorMessage = 'Full Name và Email là bắt buộc';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const updatedUser = {
      fullName: this.fullName,
      email: this.email,
      phone: this.phone,
      address: this.address
    };

    // 👉 Lấy id từ localStorage để truyền vào updateProfile
    const userId = Number(localStorage.getItem('userId'));

    this.authService.updateProfile(userId, updatedUser).subscribe({
      next: () => {
        this.isLoading = false;
        this.successMessage = 'Cập nhật thành công!';
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage = 'Cập nhật thất bại. Vui lòng thử lại.';
      }
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}