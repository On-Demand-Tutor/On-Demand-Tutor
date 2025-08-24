import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UpdateComponent } from './update';
import { AuthService } from '../auth';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';

describe('UpdateComponent', () => {
  let component: UpdateComponent;
  let fixture: ComponentFixture<UpdateComponent>;
  let mockAuthService: any;

  beforeEach(async () => {
    mockAuthService = {
      getUserId: jasmine.createSpy('getUserId').and.returnValue(1),
      getUserRole: jasmine.createSpy('getUserRole').and.returnValue('student'),
      getUserById: jasmine.createSpy('getUserById').and.returnValue(of({ username: 'testuser', grade: 10 })),
      updateProfile: jasmine.createSpy('updateProfile').and.returnValue(of({}))
    };

    await TestBed.configureTestingModule({
      imports: [UpdateComponent, RouterTestingModule],
      providers: [{ provide: AuthService, useValue: mockAuthService }]
    }).compileComponents();

    fixture = TestBed.createComponent(UpdateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load user on init', () => {
    expect(component.username).toBe('testuser');
    expect(component.grade).toBe(10);
  });

  it('should update profile successfully', () => {
    component.username = 'updated';
    component.updateProfile();
    expect(component.successMessage).toBe('Cập nhật thành công!');
  });

  it('should handle update error', () => {
    mockAuthService.updateProfile.and.returnValue(throwError(() => new Error('fail')));
    component.username = 'updated';
    component.updateProfile();
    expect(component.errorMessage).toBe('Cập nhật thất bại. Vui lòng thử lại.');
  });
});
