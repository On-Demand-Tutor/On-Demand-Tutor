import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../environments/environment';

export type Role = 'student' | 'tutor' | 'admin' | 'moderator' | '';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = environment.apiUrls.userService; // '/api/users'

  constructor(private http: HttpClient) {}

  // ===== AUTH =====
  login(email: string, password: string): Observable<any> {
    return this.http
      .post<any>(`${this.apiUrl}/login`, { email, password })
      .pipe(tap((res) => this.setSession(res)));
  }

  register(username: string, email: string, password: string, rawRole: string): Observable<any> {
    const pending = this.normalizeRole(rawRole);
    if (pending) localStorage.setItem('pendingRole', pending);
    if (email) localStorage.setItem('login_email', email); // để fallback
    return this.http.post<any>(`${this.apiUrl}/register`, { username, email, password, role: rawRole });
  }

  // ===== SESSION / HELPERS =====
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
      return JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')));
    } catch {
      return null;
    }
  }

  private extractRoleFromToken(token: string): Role {
    const p = this.decodeJwt(token);
    if (!p) return '';
    const cands = [p.role, p.roles, p.authorities, p.scope, p.scopes, p['cognito:groups']].filter(Boolean);
    for (const c of cands) {
      const arr = Array.isArray(c) ? c : String(c).split(/[ ,]/).filter(Boolean);
      for (const it of arr) {
        const r = this.normalizeRole(it);
        if (r) return r;
      }
    }
    return '';
  }

  private setSession(res: any) {
    const token = res?.result?.token ?? res?.token ?? res?.accessToken ?? res?.jwt ?? null;
    const user  = res?.user ?? res?.result?.user ?? res?.data?.user ?? {};
    const uid   = user?.id ?? res?.userId ?? res?.result?.userId ?? res?.data?.userId ?? null;
    const uname = user?.username ?? res?.username ?? res?.result?.username ?? res?.data?.username ?? null;
    const email = user?.email ?? res?.email ?? res?.result?.email ?? res?.data?.email ?? null;

    let role = this.normalizeRole(user?.role ?? res?.role ?? res?.result?.role ?? res?.data?.role ?? null);

    if (token) {
      localStorage.setItem('jwt', String(token));
      localStorage.setItem('access_token', String(token)); // để tương thích
    }
    if (uid != null) localStorage.setItem('userId', String(uid));
    if (uname) localStorage.setItem('username', String(uname));
    if (email) localStorage.setItem('login_email', String(email));

    if (!role && token) role = this.extractRoleFromToken(token);
    const pending = localStorage.getItem('pendingRole') as Role | null;
    if (!role && pending) role = pending;
    if (role) localStorage.setItem('role', role);
    if (pending) localStorage.removeItem('pendingRole');
  }

  // ===== GETTERS =====
  isLoggedIn(): boolean { return !!this.getToken(); }
  getToken(): string | null { return localStorage.getItem('jwt') ?? localStorage.getItem('access_token'); }
  getUserId(): number | null { const v = localStorage.getItem('userId'); return v ? Number(v) : null; }
  getUserRole(): Role { return (localStorage.getItem('role') as Role) ?? ''; }
  setUserRole(r: Role) { if (r) localStorage.setItem('role', r); }

  // LẤY ROLE nếu LS chưa có
  ensureRoleFromTokenIfMissing(): Role {
    const cur = this.getUserRole();
    if (cur) return cur;
    const t = this.getToken(); if (!t) return '';
    const r = this.extractRoleFromToken(t);
    if (r) this.setUserRole(r);
    return r;
  }

  // LẤY USERID nếu LS chưa có: ưu tiên token → fallback qua email
  ensureUserIdFromTokenIfMissing(): number | null {
    const cur = this.getUserId();
    if (cur != null) return cur;

    const t = this.getToken();
    if (t) {
      const p = this.decodeJwt(t);
      const uid = p?.userId ?? p?.uid ?? p?.sub ?? null;
      if (uid != null) {
        localStorage.setItem('userId', String(uid));
        return Number(uid);
      }
    }
    return null;
  }

  logout(): void {
    ['jwt','access_token','role','userId','username','pendingRole','pendingEmail','login_email']
      .forEach(k => localStorage.removeItem(k));
  }

  // ===== USERS API =====
  getUserById(userId: number) { return this.http.get(`${this.apiUrl}/getUser/${userId}`); }
  getUserIdByEmail(email: string) { return this.http.get(`${this.apiUrl}/email/${encodeURIComponent(email)}`); } // điều chỉnh theo BE
  updateProfile(userId: number, data: any) { return this.http.put(`${this.apiUrl}/update/${userId}`, data); }
}
