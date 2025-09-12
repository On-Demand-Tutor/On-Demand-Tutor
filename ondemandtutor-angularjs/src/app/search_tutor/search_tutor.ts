// src/app/search_tutor/search_tutor.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';

type TutorCard = {
  id: number;
  name: string;
  rating: number;
  description: string;
  subjects: string[];
  avatar?: string;
  verified?: boolean;
};

@Component({
  selector: 'app-search-tutor',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './search_tutor.html',
  styleUrls: ['./search_tutor.css']
})
export class SearchTutorComponent implements OnInit {
  // form
  keyword = '';

  // trạng thái UI
  hasSearched = false;
  isLoading = false;
  errorMessage = '';

  // dữ liệu hiển thị
  pagedResults: TutorCard[] = [];     
  filteredResults: TutorCard[] = [];  
  totalTutors = 0;                    

  // phân trang (server-side)
  pageSize = 8;            
  currentPage = 1;             
  totalPages = 1;
  pages: number[] = [];

  // Nếu BE dùng page 0-based (Spring Data mặc định) đặt true
  backendPageZeroBased = true

  constructor(
    private http: HttpClient,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Đọc query params ?q=...&page=...
    this.route.queryParamMap.subscribe(q => {
      const qkw = (q.get('q') || '').trim();       
      const p = Number(q.get('page') || '1') || 1;

      this.keyword = qkw;
      this.currentPage = p;

      if (qkw !== '' || q.has('q')) {
        this.hasSearched = true;
        this.fetchServerPage();
      } else {
        this.resetView();
      }
    });
  }

  search(): void {
    const q = (this.keyword || '').trim();
    this.hasSearched = true; 

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { q, page: 1 },
      queryParamsHandling: 'merge'
    });
  }

  private fetchServerPage(): void {
    this.isLoading = true;
    this.errorMessage = '';

    const apiPage = this.backendPageZeroBased ? (this.currentPage - 1) : this.currentPage;

    const body = {
      keyword: this.keyword || '',  
      page: apiPage,
      size: this.pageSize
    };

    this.http.post<any>('/api/students/search-tutor', body).subscribe({
      next: (res) => {
        const list = Array.isArray(res?.tutors) ? res.tutors : [];
        const total = Number(res?.totalElements ?? 0);

        const cards: TutorCard[] = list.map((u: any) => {
          const subjects = [
            ...(u?.skills ? String(u.skills).split(/[;,/]/) : []),
            ...(u?.teachingGrades ? String(u.teachingGrades).split(/[;,/]/) : [])
          ]
            .map((s: string) => s.trim())
            .filter(Boolean);

          return {
            id: Number(u?.userId ?? u?.id ?? 0),
            name: String(u?.username ?? u?.name ?? ''),
            rating: Number(u?.rating ?? 5),
            description: this.buildDescription(u),
            subjects,
            avatar: u?.avatar ?? 'https://via.placeholder.com/80',
            verified: true
          };
        });

        this.pagedResults = cards;
        this.filteredResults = cards;

        this.totalTutors = total;
        this.totalPages = Math.max(1, Math.ceil(total / this.pageSize));
        this.pages = Array.from({ length: this.totalPages }, (_, i) => i + 1);

        this.isLoading = false;
      },
      error: (err) => {
        console.error('search-tutor error:', err);
        this.isLoading = false;
        this.errorMessage =
          err?.status === 0 ? 'Không kết nối được máy chủ.' :
          err?.status === 401 ? 'Bạn không có quyền truy cập tính năng này.' :
          'Không tải được danh sách gia sư.';

        // vẫn giữ khu vực kết quả nhưng rỗng
        this.resetView(true);
      }
    });
  }

  // Mô tả ngắn cho card
  private buildDescription(u: any): string {
    const skill = u?.skills ? `Môn: ${u.skills}` : '';
    const grades = u?.teachingGrades ? `; Khối: ${u.teachingGrades}` : '';
    const price = (typeof u?.price === 'number')
      ? `; Học phí: ${this.formatVND(u.price)}`
      : '';
    return `${skill}${grades}${price}`.replace(/^; /, '');
  }

  private formatVND(n: number): string {
    try {
      return n.toLocaleString('vi-VN', { style: 'currency', currency: 'VND' });
    } catch {
      return `${n}₫`;
    }
  }

  private resetView(keepHasSearched = false): void {
    this.pagedResults = [];
    this.filteredResults = [];
    this.totalTutors = 0;
    this.totalPages = 1;
    this.pages = [1];
    if (!keepHasSearched) this.hasSearched = false;
  }

  // Điều hướng phân trang
  goToPage(p: number): void {
    if (p < 1 || p > this.totalPages) return;
    this.currentPage = p;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { page: p },
      queryParamsHandling: 'merge'
    }).then(() => this.fetchServerPage());
  }
  prevPage(): void { this.goToPage(this.currentPage - 1); }
  nextPage(): void { this.goToPage(this.currentPage + 1); }
}