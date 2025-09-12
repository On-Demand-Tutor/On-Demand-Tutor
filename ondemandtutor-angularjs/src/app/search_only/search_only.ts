import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-search-only',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './search_only.html',
  styleUrls: ['./search_only.css']
})
export class SearchOnlyComponent {
  keyword = '';

  constructor(private router: Router) {}

  search() {
    const q = (this.keyword || '').trim();
    // Điều hướng sang trang đầy đủ + gắn query q & page=1
    this.router.navigate(['/search_tutor'], { queryParams: { q, page: 1 } });
  }
}
