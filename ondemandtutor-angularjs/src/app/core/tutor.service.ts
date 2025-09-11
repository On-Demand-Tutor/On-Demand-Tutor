import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, map, tap } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Tutor {
  id: number;
  name: string;
  description: string;
  rating: number;
  subjects: string[];
  verified?: boolean;
  avatar?: string;
}

@Injectable({ providedIn: 'root' })
export class TutorService {
  private http = inject(HttpClient);
  private api = environment.apiUrls?.tutorService || '/api/tutors';

  /** Nguồn dữ liệu trung tâm */
  private readonly _tutors$ = new BehaviorSubject<Tutor[]>([]);
  readonly tutors$ = this._tutors$.asObservable();

  /** Tải toàn bộ (hoặc theo keyword) từ BE */
  loadAll(keyword = ''): Observable<Tutor[]> {
    const url = keyword
      ? `${this.api}?keyword=${encodeURIComponent(keyword)}`
      : this.api;
    return this.http.get<Tutor[]>(url).pipe(
      tap(list => this._tutors$.next(list))
    );
  }

  /** Đồng bộ sau khi thêm/sửa/xoá: gọi lại loadAll hoặc cập nhật cục bộ */
  refresh(keyword = '') { this.loadAll(keyword).subscribe(); }

  /** Cập nhật cục bộ không cần gọi API (khi đã biết đối tượng thay đổi) */
  upsertLocal(t: Tutor) {
    const cur = this._tutors$.value.slice();
    const idx = cur.findIndex(x => x.id === t.id);
    if (idx >= 0) cur[idx] = { ...cur[idx], ...t };
    else cur.unshift(t);
    this._tutors$.next(cur);
  }

  removeLocal(id: number) {
    const cur = this._tutors$.value.filter(x => x.id !== id);
    this._tutors$.next(cur);
  }

  /** Ví dụ các API CRUD */
  create(payload: Partial<Tutor>) {
    return this.http.post<Tutor>(this.api, payload).pipe(
      tap(t => this.upsertLocal(t))
    );
  }
  update(id: number, payload: Partial<Tutor>) {
    return this.http.put<Tutor>(`${this.api}/${id}`, payload).pipe(
      tap(t => this.upsertLocal(t))
    );
  }
  delete(id: number) {
    return this.http.delete<void>(`${this.api}/${id}`).pipe(
      tap(() => this.removeLocal(id))
    );
  }
}
