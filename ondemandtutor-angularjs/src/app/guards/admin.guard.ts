import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../auth';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(): boolean {
    const isLoggedIn = this.authService.isLoggedIn();
    const userRole = this.authService.getUserRole();
    
    if (!isLoggedIn) {
      this.router.navigate(['/login']);
      return false;
    }
    
    if (userRole !== 'admin') {
      this.router.navigate(['/home']);
      return false;
    }
    
    return true;
  }
}