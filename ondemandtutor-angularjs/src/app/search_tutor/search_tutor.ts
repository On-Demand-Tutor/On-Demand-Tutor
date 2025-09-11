import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TutorService, Tutor } from '../core/tutor.service';
import {
  Subscription, combineLatest, map,
  interval, switchMap, Subject, takeUntil
} from 'rxjs';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-search-tutor',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './search_tutor.html',
  styleUrls: ['./search_tutor.css']
})
export class SearchTutorComponent implements OnInit, OnDestroy {
  constructor(private tutorService: TutorService, private router: Router) {}

  keyword = '';
  pageSize = 4;

  currentPage = 1;
  totalPages = 1;
  pages: number[] = [];
  totalTutors = 0;

  filteredResults: Tutor[] = [];
  pagedResults: Tutor[] = [];

  private sub?: Subscription;
  private destroy$ = new Subject<void>();

  ngOnInit() {
    // tải lần đầu
    this.tutorService.loadAll().subscribe();

    // subscribe store -> filter + phân trang
    this.sub = combineLatest([this.tutorService.tutors$]).pipe(
      map(([list]) => {
        const kw = (this.keyword || '').trim().toLowerCase();
        const filtered = kw
          ? list.filter(t =>
              t.name.toLowerCase().includes(kw) ||
              (t.subjects || []).some(s => s.toLowerCase().includes(kw))
            )
          : list;

        this.totalTutors = list.length;
        this.filteredResults = filtered;

        // giữ trang hợp lệ
        if ((this.currentPage - 1) * this.pageSize >= filtered.length) {
          this.currentPage = 1;
        }
        this.updatePagination();
        return filtered;
      })
    ).subscribe();

    // Reload khi quay lại route /search_tutor (kể cả component tái sử dụng)
    this.router.events.pipe(
      filter(e => e instanceof NavigationEnd),
      takeUntil(this.destroy$)
    ).subscribe((e: any) => {
      if (e.urlAfterRedirects?.includes('/search_tutor')) {
        this.tutorService.loadAll(this.keyword).subscribe();
      }
    });

    // Fallback: polling 30s để các tài khoản/thiết bị khác tự thấy (khi chưa có SSE)
    interval(30000).pipe(
      takeUntil(this.destroy$),
      switchMap(() => this.tutorService.loadAll(this.keyword))
    ).subscribe();
  }

  ngOnDestroy() {
    this.sub?.unsubscribe();
    this.destroy$.next(); this.destroy$.complete();
  }

  search() {
    // lọc cục bộ; nếu muốn tìm server-side: this.tutorService.loadAll(this.keyword).subscribe();
    this.currentPage = 1;
    this.updatePagination();
  }

  updatePagination() {
    const len = this.filteredResults.length;
    this.totalPages = Math.max(1, Math.ceil(len / this.pageSize));
    this.pages = Array.from({ length: this.totalPages }, (_, i) => i + 1);
    this.goToPage(this.currentPage);
  }

  goToPage(p: number) {
    this.currentPage = p;
    const start = (p - 1) * this.pageSize;
    this.pagedResults = this.filteredResults.slice(start, start + this.pageSize);
  }

  prevPage() { if (this.currentPage > 1) this.goToPage(this.currentPage - 1); }
  nextPage() { if (this.currentPage < this.totalPages) this.goToPage(this.currentPage + 1); }
}
