import { Component, Input, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { finalize, catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { RatingService } from '../core/rating.service';

@Component({
  selector: 'app-rating',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './rating.html',
  styleUrls: ['./rating.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RatingComponent implements OnInit {
  @Input() tutorUserId!: number;

  stars = [1, 2, 3, 4, 5];
  selected = 0;
  comment = '';
  msg = '';
  color = '';
  busy = false;
  canRate: boolean | null = null; // null = đang kiểm tra

  constructor(
    private api: RatingService,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    if (!this.tutorUserId) {
      const id = Number(this.route.snapshot.paramMap.get('tutorUserId'));
      if (!Number.isNaN(id)) this.tutorUserId = id;
    }

    if (this.tutorUserId) {
      this.api.canRate(this.tutorUserId)
        .pipe(
          catchError(() => { this.msg = '❌ Không kiểm tra được quyền đánh giá'; this.color='red'; return of(false); }),
          finalize(() => this.cdr.markForCheck())
        )
        .subscribe(ok => { this.canRate = ok; });
    } else {
      this.canRate = false;
    }
  }

  pick(n: number) {
    this.selected = (this.selected === n) ? 0 : n;
    this.msg = '';
    this.cdr.markForCheck();
  }

  get disabled() {
    return this.busy || !this.tutorUserId || !this.canRate || !this.selected || !this.comment.trim();
  }

  submit() {
    if (this.disabled || !this.tutorUserId) return;
    this.busy = true;
    this.msg = '';

    this.api.rate(this.tutorUserId, this.selected, this.comment.trim())
      .pipe(
        catchError(err => {
          const beMsg = err?.error?.error || err?.error?.message || '';
          this.msg = beMsg || '❌ Gửi đánh giá thất bại';
          this.color = 'red';
          return of(null);
        }),
        finalize(() => { this.busy = false; this.cdr.markForCheck(); })
      )
      .subscribe(ok => {
        if (!ok) return;
        this.msg = '✅ Đánh giá thành công';
        this.color = 'green';
        this.comment = '';
        this.canRate = false; // đánh giá xong thì khóa
      });
  }
}
