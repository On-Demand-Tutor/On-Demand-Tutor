// src/app/tutor_profile/tutor_profile.spec.ts
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TutorProfileComponent } from './tutor_profile';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { By } from '@angular/platform-browser';

describe('TutorProfileComponent', () => {
  let component: TutorProfileComponent;
  let fixture: ComponentFixture<TutorProfileComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      // ⬇️ Nếu TutorProfileComponent là standalone (rất hay dùng trong code của bạn)
      imports: [TutorProfileComponent, HttpClientTestingModule, RouterTestingModule],

      // ⬇️ Nếu KHÔNG phải standalone, dùng cấu hình này thay cho cái trên:
      // declarations: [TutorProfileComponent],
      // imports: [HttpClientTestingModule, RouterTestingModule],
    }).compileComponents();

    fixture = TestBed.createComponent(TutorProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should render username in sidebar', () => {
    (component as any).username = 'NgocMai';   // cast any để né TS2339
    fixture.detectChanges();

    const compiled = fixture.debugElement.nativeElement as HTMLElement;
    const nameText = compiled.querySelector('.profile-avatar h3')?.textContent || '';
    expect(nameText).toContain('NgocMai');
  });

  it('should show tutor info if userRole is tutor', () => {
    (component as any).userRole = 'tutor';
    (component as any).qualifications = 'Master in Math';
    (component as any).skills = 'Algebra';
    (component as any).teachingGrades = '6-12';
    (component as any).price = 200000;
    (component as any).description = 'Experienced tutor';
    fixture.detectChanges();

    const text = (fixture.debugElement.nativeElement as HTMLElement).textContent || '';
    expect(text).toContain('Master in Math');
    expect(text).toContain('Algebra');
    expect(text).toContain('6-12');
    // chấp nhận 200,000 hoặc 200.000 hoặc có ký hiệu tiền tệ
    expect(/200[\.,]000/.test(text) || text.includes('200 000')).toBeTrue();
    expect(text).toContain('Experienced tutor');
  });

  it('should show student info if userRole is student', () => {
    (component as any).userRole = 'student';
    (component as any).grade = 10;
    fixture.detectChanges();

    const text = (fixture.debugElement.nativeElement as HTMLElement).textContent || '';
    // tuỳ template: "Lớp: 10" hoặc "Lớp 10"
    expect(/Lớp[:\s]*10/.test(text)).toBeTrue();
  });

  it('should display loading template when isLoading is true', () => {
    (component as any).isLoading = true;
    fixture.detectChanges();

    const loadingEl = fixture.debugElement.query(By.css('.loading-card'));
    expect(loadingEl).toBeTruthy();
  });

  it('should display error message when errorMessage is set', () => {
    (component as any).errorMessage = 'Có lỗi xảy ra';
    fixture.detectChanges();

    const text = (fixture.debugElement.nativeElement as HTMLElement).textContent || '';
    expect(text).toContain('Có lỗi xảy ra');
  });
});
  