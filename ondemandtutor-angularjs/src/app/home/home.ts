import { Component, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService, Role } from '../auth';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class HomeComponent implements OnInit {
  // hiển thị ở header/avatar
  isAvatarMenuOpen = false;
  isLoggedIn = false;

  // dùng cho *ngIf trong template (student|tutor|admin|'')
  userRole: Role = '';

  // nếu template có hiển thị tên
  username = '';

  constructor(private auth: AuthService, private router: Router) {}

  ngOnInit(): void {
    // đăng nhập chưa?
     this.isLoggedIn = this.auth.isLoggedIn();

    // lấy role (ưu tiên localStorage, thiếu thì suy từ JWT)
    this.userRole = this.auth.getUserRole() || this.auth.ensureRoleFromTokenIfMissing();

    // lấy username nếu có
    this.username = localStorage.getItem('username') || '';
  }

  // mở/đóng menu avatar
  toggleAvatarMenu(): void {
    this.isAvatarMenuOpen = !this.isAvatarMenuOpen;
  }

  // đóng menu khi click ra ngoài (nếu HTML của bạn có dropdown)
  @HostListener('document:click', ['$event'])
  onDocClick(e: MouseEvent): void {
    const target = e.target as HTMLElement;
    // nếu click không nằm trong khu vực avatar/dropdown thì đóng
    if (!target.closest('.auth-avatar') && !target.closest('.dropdown')) {
      this.isAvatarMenuOpen = false;
    }
  }

  // điều hướng tới trang cập nhật hồ sơ
  goUpdateProfile(): void {
    this.isAvatarMenuOpen = false;
    this.router.navigate(['/update']);
  }

  // nếu bạn có nút "Vào trang của tôi" theo role
  goRoleHome(): void {
    this.router.navigate(['/home']);
  }

  // logout
  logout(): void {
    this.auth.logout();
    this.isAvatarMenuOpen = false;
    this.router.navigate(['/login']);
  }
}
