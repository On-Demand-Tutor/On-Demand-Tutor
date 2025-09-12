import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SearchOnlyComponent } from './search_only';

describe('SearchTutorComponent', () => {
  let component: SearchOnlyComponent;
  let fixture: ComponentFixture<SearchOnlyComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SearchOnlyComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(SearchOnlyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
