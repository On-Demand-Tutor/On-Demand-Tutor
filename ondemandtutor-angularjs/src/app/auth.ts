import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../environments/environment';

export type Role = 'student' | 'tutor' | 'admin' | 'moderator' | '';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = environment.apiUrls.userService; // ví dụ: '/api/users'

  constructor(private http: HttpClient) {}

  // ===== AUTH =====
  login(email: string, password: string): Observable<any> {
    return this.http
      .post<any>(`${this.apiUrl}/login`, { email, password })
      .pipe(tap((res) => this.setSession(res)));
  }

  // rawRole gửi UPPERCASE: 'STUDENT' | 'TUTOR' | 'MODERATOR'
  register(
    username: string,
    email: string,
    password: string,
    rawRole: string
  ): Observable<any> {
    // Lưu pendingRole để FE có thể tạm hiển thị role nếu BE chưa trả role trong token
    const pending = this.normalizeRole(rawRole);
    if (pending) localStorage.setItem('pendingRole', pending);
    if (email) localStorage.setItem('pendingEmail', email); // tiện autofill màn login

    return this.http.post<any>(`${this.apiUrl}/register`, {
      username,
      email,
      password,
      role: rawRole,
    });
  }

  // ===== SESSION =====
  private normalizeRole(raw: any): Role {
    if (!raw) return '';
    const v = String(raw).toLowerCase().replace(/^role_/, '');
    if (v.includes('student')) return 'student';
    if (v.includes('tutor')) return 'tutor';
    if (v.includes('admin')) return 'admin';
    if (v.includes('moderator')) return 'moderator';
    return '';
  }

  private decodeJwt(token: string): any | null {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) return null;
      const payload = JSON.parse(
        atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'))
      );
      return payload;
    } catch {
      return null;
    }
  }

  private extractRoleFromToken(token: string): Role {
    const p = this.decodeJwt(token);
    if (!p) return '';
    const candidates = [
      p.role,
      p.roles,
      p.authorities,
      p.scope,
      p.scopes,
      p['cognito:groups'],
    ].filter(Boolean);

    for (const c of candidates) {
      const arr = Array.isArray(c)
        ? c
        : String(c)
            .split(/[ ,]/)
            .filter(Boolean);
      for (const it of arr) {
        const r = this.normalizeRole(it);
        if (r) return r;
      }
    }
    return '';
  }

  private setSession(res: any) {
    // BE có thể trả: { result: { token, authenticated }, user, userId, ... }
    const token =
      res?.result?.token ??
      res?.token ??
      res?.accessToken ??
      res?.jwt ??
      null;

    const user = res?.user ?? res?.result?.user ?? res?.data?.user ?? {};
    const userId =
      user?.id ?? res?.userId ?? res?.result?.userId ?? res?.data?.userId ?? null;
    const username =
      user?.username ??
      res?.username ??
      res?.result?.username ??
      res?.data?.username ??
      null;

    let role = this.normalizeRole(
      user?.role ?? res?.role ?? res?.result?.role ?? res?.data?.role ?? null
    );

    if (token) localStorage.setItem('jwt', String(token));
    if (userId != null) localStorage.setItem('userId', String(userId));
    if (username) localStorage.setItem('username', String(username));

    if (!role && token) role = this.extractRoleFromToken(token);
    if (!role) {
      const pending = localStorage.getItem('pendingRole') as Role | null;
      if (pending) role = pending;
    }
    if (role) localStorage.setItem('role', role);

    if (localStorage.getItem('pendingRole')) localStorage.removeItem('pendingRole');
  }

  // ✅ Lưu token thủ công (dùng khi BE trả token ngay sau register)
  setToken(
    token: string,
    extras?: { role?: string; userId?: number | string; username?: string }
  ) {
    if (!token) return;
    localStorage.setItem('jwt', String(token));

    // role: ưu tiên extras → fallback decode token
    let role = this.normalizeRole(extras?.role);
    if (!role) role = this.extractRoleFromToken(token);
    if (role) localStorage.setItem('role', role);

    // userId: ưu tiên extras → fallback decode token
    const payload = this.decodeJwt(token);
    const uid = extras?.userId ?? payload?.userId ?? payload?.sub;
    if (uid != null) localStorage.setItem('userId', String(uid));

    if (extras?.username) localStorage.setItem('username', String(extras.username));
  }

  // ===== GETTERS =====
  isLoggedIn(): boolean {
    return !!localStorage.getItem('jwt');
  }
  getToken(): string | null {
    return localStorage.getItem('jwt');
  }
  getUserId(): number | null {
    const v = localStorage.getItem('userId');
    return v ? Number(v) : null;
  }
  getUserRole(): Role {
    return (localStorage.getItem('role') as Role) ?? '';
  }
  setUserRole(r: Role) {
    if (r) localStorage.setItem('role', r);
  }
  ensureRoleFromTokenIfMissing(): Role {
    const cur = this.getUserRole();
    if (cur) return cur;
    const t = this.getToken();
    if (!t) return '';
    const r = this.extractRoleFromToken(t);
    if (r) this.setUserRole(r);
    return r;
    }

  // ✅ Lấy userId từ localStorage; nếu thiếu thì decode từ token và lưu lại
  ensureUserIdFromTokenIfMissing(): number | null {
    const cur = this.getUserId();
    if (cur != null) return cur;

    const t = this.getToken();
    if (!t) return null;

    const p = this.decodeJwt(t);
    const uid = p?.userId ?? p?.sub ?? null;
    if (uid != null) {
      localStorage.setItem('userId', String(uid));
      return Number(uid);
    }
    return null;
  }

  logout(): void {
    ['jwt', 'role', 'userId', 'username', 'pendingRole', 'pendingEmail'].forEach((k) =>
      localStorage.removeItem(k)
    );
  }

  // ===== USERS API =====
  getUserById(userId: number) {
    return this.http.get(`${this.apiUrl}/${userId}`);
  }
  updateProfile(userId: number, data: any) {
    return this.http.put(`${this.apiUrl}/update/${userId}`, data);
  }
}
