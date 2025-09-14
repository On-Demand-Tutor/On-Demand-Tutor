import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';

import { ChatComponent } from './chat';
import { AuthService } from '../auth'; // <-- import trực tiếp, KHÔNG dùng require

// Mock AuthService đủ các hàm mà ChatComponent gọi
class MockAuthService {
  getUserId() { return 999; }
  ensureUserIdFromTokenIfMissing() { return 999; }
  getUserRole() { return 'STUDENT'; }
  ensureRoleFromTokenIfMissing() { return 'STUDENT'; }
  getToken() { return 'fake.jwt.token'; }
}

describe('ChatComponent (FE-only)', () => {
  let fixture: ComponentFixture<ChatComponent>;
  let component: ChatComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChatComponent, HttpClientTestingModule],
      providers: [
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => null } } } },
        { provide: AuthService, useClass: MockAuthService }, // <-- dùng provider chuẩn
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ChatComponent);
    component = fixture.componentInstance;

    // Không dùng websocket trong unit test
    component.enableWs = false;

    // Chặn các hàm async để test thuần FE
    spyOn<any>(component, 'startPolling').and.callFake(() => {});
    spyOn<any>(component, 'loadHistory').and.callFake(async (_id: number) => {
      component.messages = [
        { id: 1, sender: '123', text: 'hello from server', time: '10:00', type: 'received' }
      ];
    });
    spyOn<any>(component, 'loadPartners').and.callFake((after?: () => void) => after && after());

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
    expect(component.chatTitle).toBe('Chọn đối tác để chat');
  });

  it('should have a tutor list after manual seed + filter', () => {
    component['tutors'] = [
      { id: 1, name: 'Tutor A' },
      { id: 2, name: 'Tutor B' },
    ];
    component.applyFilter();

    expect(component.filteredTutors.length).toBe(2);
    expect(component.filteredTutors[0].name).toBe('Tutor A');
  });

  it('should load messages when selecting a partner', fakeAsync(() => {
    component['tutors'] = [{ id: 1, name: 'Tutor A' }];
    component.applyFilter();

    const partner = component['tutors'][0];
    component.selectTutor(partner);
    tick();

    expect(component.currentChatId).toBe(partner.id);
    expect(component.chatTitle).toBe(partner.name);
    expect(component.messages.length).toBeGreaterThan(0);
  }));

  it('should add message on send and clear input', fakeAsync(() => {
    component['tutors'] = [{ id: 10, name: 'Tutor X' }];
    component.applyFilter();

    const partner = component['tutors'][0];
    component.selectTutor(partner);
    tick();

    const before = component.messages.length;
    component.newMessage = 'Hello tutor!';
    component.send();

    tick();

    expect(component.messages.length).toBe(before + 1);
    const last = component.messages.at(-1)!;
    expect(last.text).toBe('Hello tutor!');
    expect(last.type).toBe('sent');
    expect(component.newMessage).toBe('');
  }));

  it('should ignore empty/whitespace-only message', fakeAsync(() => {
    component['tutors'] = [{ id: 3, name: 'Tutor Y' }];
    component.applyFilter();

    const partner = component['tutors'][0];
    component.selectTutor(partner);
    tick();

    const before = component.messages.length;
    component.newMessage = '   ';
    component.send();

    expect(component.messages.length).toBe(before);
  }));

  it('should not send if no conversation is selected', () => {
    component.currentChatId = null;
    component.messages = [];

    component.newMessage = 'Will not send';
    component.send();

    expect(component.messages.length).toBe(0);
  });
});
