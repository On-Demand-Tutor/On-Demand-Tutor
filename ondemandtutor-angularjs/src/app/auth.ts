import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080 '; // user-service chạy trên port 8080

  constructor(private http: HttpClient) {}

  // Lưu token vào localStorage
  setToken(token: string): void {
    localStorage.setItem('jwt', token);
  }

  getToken(): string | null {
    return localStorage.getItem('jwt');
  }

  private parseToken(): any {
    const token = this.getToken();
    if (!token) return null;
    try {
      return JSON.parse(atob(token.split('.')[1]));
    } catch {
      return null;
    }
  }

  getUserRole(): string {
    const payload = this.parseToken();
    return payload ? payload.role?.toLowerCase() : '';
  }

  getUserId(): number {
    const payload = this.parseToken();
    return payload ? payload.id : 0;
  }

  // ===== API gọi BE =====
  register(user: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/register`, user);
  }


  login(credentials: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/login`, credentials);
  }

  getUserById(userId: number, role: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/users/${userId}`);
  }

  updateProfile(userId: number, role: string, data: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/users/${userId}`, data);
  }


  logout(): void {
    localStorage.removeItem('jwt');
  }
}
