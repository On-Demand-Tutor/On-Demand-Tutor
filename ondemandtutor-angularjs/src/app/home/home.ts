import { Component, HostListener } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class Home {
  isAvatarMenuOpen = false;

  constructor(private router: Router) {}

  toggleAvatarMenu() {
    this.isAvatarMenuOpen = !this.isAvatarMenuOpen;
  }

  logout() {
    this.router.navigate(['/login']);
  }

  // Listener global click để đóng menu khi click ngoài
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