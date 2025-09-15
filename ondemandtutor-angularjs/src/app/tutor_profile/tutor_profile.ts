import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { UserService, UserProfile } from '../core/user.service';

@Component({
  selector: 'app-tutor-profile',
  standalone: true,
  imports: [CommonModule, RouterModule],  
  templateUrl: './tutor_profile.html',
  styleUrls: ['./tutor_profile.css']
})
export class TutorProfileComponent implements OnInit {
  tutor: UserProfile | null = null;
  isLoading = false;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const userId = Number(params.get('id'));
      if (!userId) {
        this.errorMessage = 'ID không hợp lệ';
        return;
      }

      this.isLoading = true;
      this.userService.getUserById(userId).subscribe({
        next: (res: any) => {
          this.tutor = res?.result ?? res;
          this.isLoading = false;
        },
        error: (err) => {
          console.error('User API error:', err);
          this.errorMessage = 'Không tải được thông tin người dùng';
          this.isLoading = false;
        }
      });
    });
  }
}
