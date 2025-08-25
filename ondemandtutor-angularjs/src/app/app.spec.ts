import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router'; // Nếu bạn sử dụng router-outlet trong app.component.html
import { CommonModule } from '@angular/common'; // Nếu bạn sử dụng các directive như *ngIf, *ngFor

@Component({
  selector: 'app-root',
  standalone: true, // Đảm bảo component là standalone nếu dự án Angular của bạn là thế hệ mới
  imports: [
    RouterOutlet, // Thêm nếu bạn có <router-outlet> trong app.component.html
    CommonModule  // Thêm nếu bạn sử dụng các directive của CommonModule
  ],
  templateUrl: './app.html', // Đường dẫn đến template HTML
  styleUrls: ['./app.css']   // Đường dẫn đến file CSS
})
export class AppComponent {
  title = 'ondemandtutor-angularjs'; // Đây là biến title mà test case đang mong đợi
}
