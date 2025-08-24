import { Component, OnInit, HostListener } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../auth';

@Component({
  selector: 'app-update',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterModule],
  templateUrl: './update.html',
  styleUrls: ['./update.css']
})
export class UpdateComponent implements OnInit {
  username = '';
  password = '';
  grade = 0;
  qualifications = '';
  skills = '';
  teachingGrades = '';
  userRole = '';

  successMessage = '';
  errorMessage = '';
  isLoading = false;
  isAvatarMenuOpen = false;

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit() {
    const userId = this.authService.getUserId();
    const role = this.authService.getUserRole();

    if (!userId || !role) {
      this.userRole = '';
      return;
    }

    this.userRole = role;

    this.authService.getUserById(userId, role).subscribe({
      next: (user) => {
        this.username = user.username || '';

        if (this.userRole === 'tutor') {
          Object.assign(this, {
            qualifications: user.qualifications || '',
            skills: user.skills || '',
            teachingGrades: user.teachingGrades || ''
          });
        }

        if (this.userRole === 'student') {
          this.grade = user.grade || 0;
        }
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
    if (!this.username) {
      this.errorMessage = 'Username là bắt buộc';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    let updatedUser: any = { username: this.username };

    if (this.password) {
      updatedUser.password = this.password;
    }

    if (this.userRole === 'tutor') {
      updatedUser = {
        ...updatedUser,
        qualifications: this.qualifications,
        skills: this.skills,
        teachingGrades: this.teachingGrades
      };
    }

    if (this.userRole === 'student') {
      updatedUser = {
        ...updatedUser,
        grade: this.grade
      };
    }

    const userId = this.authService.getUserId();
    const role = this.authService.getUserRole();

    this.authService.updateProfile(userId, role, updatedUser).subscribe({
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
            this.username = '';
            this.password = '';
            this.grade = 0;
            this.qualifications = '';
            this.skills = '';
            this.teachingGrades = '';
            this.userRole = '';
            this.router.navigate(['/login']);
          }
        }
