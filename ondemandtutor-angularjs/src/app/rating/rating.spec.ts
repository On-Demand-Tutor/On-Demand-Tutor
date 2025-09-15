import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { of, throwError } from 'rxjs';
import { RatingComponent } from './rating';
import { RatingService } from '../core/rating.service';
import { ActivatedRoute } from '@angular/router';

class MockRatingService {
  rateTutor = jasmine.createSpy('rateTutor').and.returnValue(of({}));
}

describe('RatingComponent (rating.ts)', () => {
  let fixture: ComponentFixture<RatingComponent>;
  let component: RatingComponent;
  let service: MockRatingService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RatingComponent],
      providers: [
        { provide: RatingService, useClass: MockRatingService },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: new Map([['tutorUserId','33']]) } } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RatingComponent);
    component = fixture.componentInstance;
    service = TestBed.inject(RatingService) as unknown as MockRatingService;
    fixture.detectChanges();
  });

  it('renders 5 stars', () => {
    const stars = fixture.debugElement.queryAll(By.css('.stars span'));
    expect(stars.length).toBe(5);
  });

  it('highlights stars on click', () => {
    const third = fixture.debugElement.queryAll(By.css('.stars span'))[2];
    third.nativeElement.click();
    fixture.detectChanges();

    expect(component.selected).toBe(3);
    const selectedStars = fixture.debugElement.queryAll(By.css('.stars span.selected'));
    expect(selectedStars.length).toBe(3);
  });

  it('calls service on submit', () => {
    component.selected = 5;
    component.comment = 'Good tutor';
    component.submit();

    expect(service.rateTutor).toHaveBeenCalledWith(33, { rating: 5, comment: 'Good tutor' });
  });

  it('shows error when no star selected', () => {
    component.selected = 0;
    component.submit();
    expect(component.msg).toContain('Vui lòng chọn số sao');
  });

  it('shows fail message when service errors', () => {
    (service.rateTutor as any).and.returnValue(throwError(() => new Error('boom')));
    component.selected = 4;
    component.comment = 'ok';
    component.submit();
    expect(component.msg).toContain('Gửi đánh giá thất bại');
  });
});
