import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = '/api/users';

  constructor(private http: HttpClient) {}

  register(user: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, user);
  }

  // ✅ Đã xóa `responseType: 'text'`
  login(credentials: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, credentials);
  }

  // ✅ Đã thêm hàm getCurrentUser()
  getCurrentUser(): Observable<any> {
    // Gọi API để lấy thông tin người dùng hiện tại
    return this.http.get(`${this.apiUrl}/me`);
  }

  // ✅ Cập nhật hồ sơ người dùng
  updateProfile(userData: any): Observable<any> {
    // Gọi API để cập nhật thông tin người dùng hiện tại
    return this.http.put(`${this.apiUrl}/me`, userData);
  }

  // ✅ Đăng xuất
  logout(): void {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userId'); // Có thể bạn đã bỏ lỡ dòng này
    console.log('User logged out');
  }
}
