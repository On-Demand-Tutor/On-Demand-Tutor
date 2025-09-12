import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ChatComponent } from './chat';

describe('ChatComponent (FE-only)', () => {
  let fixture: ComponentFixture<ChatComponent>;
  let component: ChatComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChatComponent], // standalone component
    }).compileComponents();

    fixture = TestBed.createComponent(ChatComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have mock conversations initially', () => {
    expect(component.conversations.length).toBeGreaterThan(0);
    expect(component.chatTitle).toBe('Chọn cuộc trò chuyện');
    expect(component.messages.length).toBe(0);
    expect(component.currentChatId).toBeNull();
  });

  it('should load messages when selecting a conversation', () => {
    const conv = component.conversations[0];
    component.selectConversation(conv);
    expect(component.currentChatId).toBe(conv.id);
    expect(component.chatTitle).toBe(conv.name);
    expect(component.messages.length).toBeGreaterThan(0);
  });

  it('should add message on send and clear input', fakeAsync(() => {
    // Chọn phòng trước
    const conv = component.conversations[0];
    component.selectConversation(conv);

    const before = component.messages.length;
    component.newMessage = 'Hello tutor!';
    component.send();

    // có setTimeout scrollToBottom -> tick để flush
    tick();

    expect(component.messages.length).toBe(before + 1);
    expect(component.messages.at(-1)?.text).toBe('Hello tutor!');
    expect(component.messages.at(-1)?.type).toBe('sent');
    expect(component.newMessage).toBe('');
  }));

  it('should ignore empty/whitespace-only message', () => {
    const conv = component.conversations[0];
    component.selectConversation(conv);

    const before = component.messages.length;
    component.newMessage = '   ';
    component.send();

    expect(component.messages.length).toBe(before);
  });

  it('should not send if no conversation is selected', () => {
    // reset state
    component.currentChatId = null;
    component.messages = [];

    component.newMessage = 'Will not send';
    component.send();

    expect(component.messages.length).toBe(0);
  });
});
