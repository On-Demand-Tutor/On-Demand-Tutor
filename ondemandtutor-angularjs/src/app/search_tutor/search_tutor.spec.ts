import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SearchTutorComponent } from './search_tutor';

describe('SearchTutorComponent', () => {
  let component: SearchTutorComponent;
  let fixture: ComponentFixture<SearchTutorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SearchTutorComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(SearchTutorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
