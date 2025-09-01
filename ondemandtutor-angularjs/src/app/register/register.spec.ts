import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RegisterComponent } from './register';
import { HttpClientTestingModule } from '@angular/common/http/testing'; // Cần import HttpClientTestingModule
import { FormsModule, ReactiveFormsModule } from '@angular/forms'; // Thêm nếu RegisterComponent sử dụng form
import { RouterTestingModule } from '@angular/router/testing'; // Thêm nếu RegisterComponent sử dụng Router
import { AuthService } from '../auth'; // Đảm bảo đường dẫn này đúng với AuthService của bạn
import { of } from 'rxjs'; // Cần để mock AuthService methods trả về Observable
import { Router } from '@angular/router'; // <--- Đã thêm dòng này để import Router

describe('RegisterComponent', () => { // Đổi tên describe để khớp với component hơn
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let mockAuthService: any; // Khai báo mock AuthService
  let mockRouter: any; // Khai báo mock Router

  beforeEach(async () => {
    // Khởi tạo mock AuthService
    mockAuthService = {
      register: jasmine.createSpy('register').and.returnValue(of(null)) // Giả lập hàm register
      // Thêm các phương thức khác của AuthService mà RegisterComponent sử dụng
    };

    // Khởi tạo mock Router
    mockRouter = {
      navigate: jasmine.createSpy('navigate') // Giả lập hàm navigate
    };

    await TestBed.configureTestingModule({
      imports: [
        RegisterComponent, // Import RegisterComponent vì nó là standalone
        HttpClientTestingModule, // Cung cấp HttpClient cho các service sử dụng nó
        FormsModule, // Cần cho các form dựa trên template-driven
        ReactiveFormsModule, // Cần cho các form dựa trên reactive forms
        RouterTestingModule // Cần để mock Router và các chức năng liên quan đến routing
      ],
      providers: [
        // Cung cấp mock AuthService thay vì AuthService thực
        { provide: AuthService, useValue: mockAuthService },
        // Cung cấp mock Router thay vì Router thực
        { provide: Router, useValue: mockRouter }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

});
