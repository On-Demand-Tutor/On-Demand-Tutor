import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { provideRouter } from '@angular/router';
import { SearchTutorComponent } from './search_tutor';

describe('SearchTutorComponent', () => {
  let component: SearchTutorComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        RouterTestingModule.withRoutes([])
      ],
      providers: [
        provideRouter([])
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(SearchTutorComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('default values should be correct', () => {
    expect(component.keyword).toBe('');
    expect(component.currentPage).toBe(1);
    expect(component.filteredResults).toEqual([]);
    expect(component.totalTutors).toBe(0);
  });

  it('search() should not throw error when called', () => {
    expect(() => component.search()).not.toThrow();
  });

  it('pagination helpers should change currentPage', () => {
    component.totalPages = 3;
    component.goToPage(2);
    expect(component.currentPage).toBe(2);

    component.nextPage();
    expect(component.currentPage).toBe(3);

    component.prevPage();
    expect(component.currentPage).toBe(2);
  });
});
