import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';


@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = environment.apiUrls.userService;

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

  register(user: any): Observable<any> {
      // POST http://localhost:8082/api/users/register
      return this.http.post(`${this.apiUrl}/register`, user);
   }

  login(credentials: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, credentials);
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
