import { Component, HostListener, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../auth';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class Home implements OnInit {
  isAvatarMenuOpen = false;
  userRole = '';
  isLoggedIn = false;

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.userRole = this.authService.getUserRole();
    this.isLoggedIn = !!this.authService.getToken(); // true nếu đã login
  }

  toggleAvatarMenu() {
    this.isAvatarMenuOpen = !this.isAvatarMenuOpen;
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }


  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    const avatar = document.querySelector('.auth-avatar');
    const dropdown = document.querySelector('.dropdown');

    if (
      this.isAvatarMenuOpen &&
      avatar && !avatar.contains(target) &&
      dropdown && !dropdown.contains(target)
    ) {
      this.isAvatarMenuOpen = false;
    }
  }
}
