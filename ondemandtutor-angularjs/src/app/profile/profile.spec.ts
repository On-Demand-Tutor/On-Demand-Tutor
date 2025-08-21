import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProfileComponent } from './profile';
import { AuthService } from '../auth';
import { Router } from '@angular/router';
import { of } from 'rxjs';

describe('ProfileComponent', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;
  let mockAuthService: any;
  let mockRouter: any;

  beforeEach(async () => {
    mockAuthService = {
      getUserById: () => of({
        fullName: 'Burk Macklin',
        email: 'abc@gmail.com',
        phone: '00923469874656',
        address: 'street no. 4, xyz'
      }),
      updateProfile: jasmine.createSpy('updateProfile').and.returnValue(of(null)),
      logout: jasmine.createSpy('logout')
    };

    mockRouter = {
      navigate: jasmine.createSpy('navigate')
    };

    // Giả lập userId trong localStorage
    localStorage.setItem('userId', '1');

    await TestBed.configureTestingModule({
      imports: [ProfileComponent],
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load user data', () => {
    expect(component.fullName).toBe('Burk Macklin');
  });

  it('should call updateProfile with correct id', () => {
    component.updateProfile();
    expect(mockAuthService.updateProfile).toHaveBeenCalledWith(
      1,   // id
      jasmine.objectContaining({
        fullName: 'Burk Macklin',
        email: 'abc@gmail.com',
        phone: '00923469874656',
        address: 'street no. 4, xyz'
      })
    );
  });

  it('should logout', () => {
    component.logout();
    expect(mockAuthService.logout).toHaveBeenCalled();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
  });
});