// src/app/profile/profile.spec.ts
import { TestBed } from '@angular/core/testing';
import { ProfileComponent } from './profile';
import { of, throwError } from 'rxjs';
import { Router } from '@angular/router';

// Mock đúng các hàm ProfileComponent đang gọi
class MockAuthService {
  _role: 'student' | 'tutor' | 'moderator' | 'admin' | '' = 'student';
  _userId: number | null = 123;

  getUserRole() { return this._role; }
  ensureRoleFromTokenIfMissing() { return this._role; }
  ensureUserIdFromTokenIfMissing() { return this._userId; }

  getUserById(id: number) {
    if (id !== this._userId) return throwError(() => new Error('not found'));
    return of({
      id,
      username: 'huytpd1501',
      role: this._role.toUpperCase(),
      grade: 11,
      qualifications: 'Bằng Sư Phạm',
      skills: 'Toán, Lý',
      teachingGrades: '6-12',
      price: 150000,
      description: 'Dễ gần, nhiệt tình',
      avatar: 'https://example.com/avt.png'
    });
  }

  logout() {}
}

class MockRouter {
  navigate = jasmine.createSpy('navigate');
}

describe('ProfileComponent', () => {
  let component: ProfileComponent;
  let auth: MockAuthService;
  let router: MockRouter;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProfileComponent], // standalone
      providers: [
        { provide: Router, useClass: MockRouter },
        // ánh xạ token AuthService thực tế sang mock
        { provide: (await import('../auth')).AuthService, useClass: MockAuthService },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    auth = TestBed.inject((await import('../auth')).AuthService) as any;
    router = TestBed.inject(Router) as any;

    // chạy ngOnInit -> component tự gọi getUserRole/ensure*/getUserById
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads profile on init and fills public fields', () => {
    expect(component.userId).toBe(123);
    expect(component.userRole).toBe('student');        // đúng field là userRole
    expect(component.username).toBe('huytpd1501');
    expect(component.grade).toBe(11);
    expect(component.qualifications).toBe('Bằng Sư Phạm');
    expect(component.skills).toBe('Toán, Lý');
    expect(component.teachingGrades).toBe('6-12');
    expect(component.price).toBe(150000);
    expect(component.description).toBe('Dễ gần, nhiệt tình');
    expect(component.avatar).toContain('http');
    expect(component.isLoading).toBeFalse();
    expect(component.errorMessage).toBe('');
  });

  it('handles load error by setting errorMessage', async () => {
    // ép lỗi: đổi userId để mock getUserById ném lỗi
    auth._userId = 999;

    const fixture2 = TestBed.createComponent(ProfileComponent);
    const comp2 = fixture2.componentInstance;
    fixture2.detectChanges();

    expect(comp2.userId).toBe(999);
    expect(comp2.errorMessage).toBeTruthy();
    expect(comp2.isLoading).toBeFalse();
  });

  it('goUpdateProfile() navigates to /update', () => {
    component.goUpdateProfile();
    expect(router.navigate).toHaveBeenCalledWith(['/update']);
  });

  it('logout() calls auth.logout and navigates to /login', () => {
    const spyLogout = spyOn(auth, 'logout');
    component.logout();
    expect(spyLogout).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
