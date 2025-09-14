import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface Message {
  id: number;
  sender: string;
  text: string;
  time: string;
  type: 'sent' | 'received';
}

interface Conversation {
  id: number;
  name: string;
  lastMessage: string;
}

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat.html',
  styleUrls: ['./chat.css']
})
export class ChatComponent {
  conversations: Conversation[] = [
    { id: 1, name: 'Gia sư Toán', lastMessage: 'Hẹn lịch học nhé' },
    { id: 2, name: 'Gia sư Lý', lastMessage: 'Bài tập hôm qua em làm chưa?' }
  ];

  messages: Message[] = [];
  chatTitle = 'Chọn cuộc trò chuyện';
  currentChatId: number | null = null;

  newMessage = '';

  selectConversation(conv: Conversation) {
    this.currentChatId = conv.id;
    this.chatTitle = conv.name;

    // dữ liệu mẫu cho từng conv
    if (conv.id === 1) {
      this.messages = [
        { id: 1, sender: 'Gia sư Toán', text: 'Chào em', time: '10:00', type: 'received' },
        { id: 2, sender: 'me', text: 'Dạ em chào thầy', time: '10:01', type: 'sent' }
      ];
    } else if (conv.id === 2) {
      this.messages = [
        { id: 3, sender: 'Gia sư Lý', text: 'Em đã xem lại bài chưa?', time: '09:00', type: 'received' }
      ];
    } else {
      this.messages = [];
    }
  }

  send() {
    if (!this.newMessage.trim() || !this.currentChatId) return;

    const msg: Message = {
      id: Date.now(),
      sender: 'me',
      text: this.newMessage.trim(),
      time: new Date().toLocaleTimeString(),
      type: 'sent'
    };

    this.messages.push(msg);
    this.newMessage = '';
    setTimeout(() => this.scrollToBottom(), 0);
  }

  private scrollToBottom() {
    const el = document.getElementById('chat-messages');
    if (el) el.scrollTop = el.scrollHeight;
  }
}
