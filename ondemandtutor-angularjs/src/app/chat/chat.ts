// import { Component, Input, OnDestroy, OnInit } from '@angular/core';
// import { CommonModule } from '@angular/common';
// import { FormsModule } from '@angular/forms';
// import { HttpClient, HttpHeaders } from '@angular/common/http';
// import { ActivatedRoute } from '@angular/router';
// import { finalize } from 'rxjs/operators';
// import { environment } from '../../environments/environment';
// import { AuthService } from '../auth';
// import SockJS from 'sockjs-client';
// import { over } from 'stompjs';

// interface Message {
//   id: number;
//   sender: string;
//   text: string;
//   time: string;
//   type: 'sent' | 'received';
//   ts?: number;     // timestamp để sort/merge
//   temp?: boolean;  // đánh dấu tin local pending
// }
// interface TutorItem { id: number; name: string; avatar?: string; }

// @Component({
//   selector: 'app-chat',
//   standalone: true,
//   imports: [CommonModule, FormsModule],
//   templateUrl: './chat.html',
//   styleUrls: ['./chat.css']
// })
// export class ChatComponent implements OnInit, OnDestroy {
//   @Input() receiverId: number | null = null;
//   @Input() enableWs = true;

//   tutors: TutorItem[] = [];
//   filteredTutors: TutorItem[] = [];
//   searchTerm = '';

//   chatTitle = 'Chọn gia sư để chat';
//   messages: Message[] = [];
//   newMessage = '';
//   isSending = false;
//   isLoadingHistory = false;

//   private userId: number | null = null;
//   currentChatId: number | null = null;

//   private stompClient: any = null;
//   private wsRoomId: string | null = null;
//   private pollTimer: any = null;

//   // ==== BASE URLS ====
//   private studentBase      = (environment as any).apiUrls?.studentService ?? '/api/students';
//   private tutorBase        = (environment as any).apiUrls?.tutorService   ?? '/api/tutors';
//   private chatsMsgBase     = (environment as any).apiUrls?.chatsService   ?? '/api/chats'; // messages/room-id
//   private chatFallbackBase = (environment as any).apiUrls?.chatService    ?? '/api/chat';  // fallback
//   private studentChatBase  = `${this.studentBase}/chat`;                                    // send
//   private userBase         = (environment as any).apiUrls?.userService    ?? '/api/users';

//   // optimistic store + cache
//   private pendingTemps = new Map<number, Message>();
//   private cacheKey = 'chat_cache_v1';
//   private chatCache: Record<string, Message[]> = {}; // key = tutorId

//   constructor(
//     private http: HttpClient,
//     private route: ActivatedRoute,
//     private auth: AuthService,
//   ) {}

//   ngOnInit(): void {
//     this.loadCache();

//     this.auth?.ensureUserIdFromTokenIfMissing?.();
//     this.userId = this.auth?.getUserId?.() ?? null;

//     const paramId = Number(this.route.snapshot.paramMap.get('id'));
//     if (!this.receiverId && !Number.isNaN(paramId)) this.receiverId = paramId;

//     this.loadTutors(() => {
//       if (this.receiverId) {
//         const found = this.tutors.find(t => t.id === this.receiverId) ?? { id: this.receiverId!, name: 'Gia sư' };
//         this.selectTutor(found);
//       }
//     });
//   }

//   ngOnDestroy(): void {
//     this.disconnectWs();
//     if (this.pollTimer) clearInterval(this.pollTimer);
//   }
// // ====== Cache helpers ======
//   private loadCache(): void {
//     try {
//       const raw = localStorage.getItem(this.cacheKey);
//       if (raw) this.chatCache = JSON.parse(raw) || {};
//     } catch { this.chatCache = {}; }
//   }
//   private saveCache(): void {
//     try { localStorage.setItem(this.cacheKey, JSON.stringify(this.chatCache)); } catch {}
//   }
//   private getCachedMessages(tutorId: number): Message[] {
//     return (this.chatCache[String(tutorId)] ?? []).map(m => ({ ...m }));
//   }
//   private putCachedMessages(tutorId: number, list: Message[]): void {
//     this.chatCache[String(tutorId)] = list.map(m => ({ ...m }));
//     this.saveCache();
//   }

//   /** Bóc mảng từ nhiều kiểu payload để tránh arr.filter is not a function */
//   private pickArray(payload: any): any[] {
//     if (!payload) return [];
//     const direct = [
//       payload, payload.content, payload.data, payload.result,
//       payload.items, payload.records, payload.users, payload.tutors, payload.list
//     ];
//     for (const v of direct) if (Array.isArray(v)) return v;
//     const deep = [payload.data?.content, payload.result?.content, payload.content?.content];
//     for (const v of deep) if (Array.isArray(v)) return v;
//     if (typeof payload === 'object') {
//       const vals = Object.values(payload);
//       if (Array.isArray(vals)) return vals;
//     }
//     return [];
//   }

//   private buildHeaders(): HttpHeaders {
//     const token = this.auth?.getToken?.() ?? null;
//     return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
//   }

//   private scrollToBottom(): void {
//     setTimeout(() => {
//       const el = document.querySelector('.chat-messages') as HTMLElement | null;
//       if (el) el.scrollTop = el.scrollHeight;
//     }, 0);
//   }

//   // ====== Tutors list ======
//   private loadTutors(after?: () => void): void {
//     this.http.get<any>(`${this.userBase}/getAllUser?page=0&size=200`, { headers: this.buildHeaders() })
//       .subscribe({
//         next: (page) => {
//           const arr = this.pickArray(page);
//           this.tutors = arr
//             .filter((u: any) => String(u.role ?? u.userRole ?? '').toUpperCase() === 'TUTOR')
//             .map((u: any, i: number) => ({
//               id: Number(u.id ?? u.userId ?? i + 1),
//               name: String(u.username ?? u.name ?? `Gia sư #${u.id}`),
//               avatar: u.avatar
//             }));
//           this.applyFilter(); after?.();
//         },
//         error: () => {
//           this.http.get<any>(`${this.tutorBase}`, { headers: this.buildHeaders() }).subscribe({
//             next: (raw) => this.fromTutorRowsHydrateNames(raw, after),
//             error: () => { this.tutors = []; this.applyFilter(); after?.(); }
//           });
//         }
//       });
//   }

//   private fromTutorRowsHydrateNames(raw: any, after?: () => void): void {
//     const rows = this.pickArray(raw);
//     let items = rows.map((r: any, i: number) => ({
//       id: Number(r.user_id ?? r.userId ?? i + 1),
//       name: `Gia sư #${Number(r.user_id ?? r.userId ?? i + 1)}`,
// avatar: undefined
//     })).filter(t => Number.isFinite(t.id) && t.id > 0);

//     this.tutors = items; this.applyFilter(); after?.();
//     items.forEach(({ id }) => {
//       this.http.get<any>(`${this.userBase}/${id}`, { headers: this.buildHeaders() })
//         .subscribe({ next: (u) => this.patchTutorNameAvatar(id, u) });
//     });
//   }

//   private patchTutorNameAvatar(id: number, u: any): void {
//     const idx = this.tutors.findIndex(t => t.id === id);
//     if (idx > -1) {
//       this.tutors[idx] = {
//         ...this.tutors[idx],
//         name: String(u?.username ?? u?.name ?? `Gia sư #${id}`),
//         avatar: u?.avatar
//       };
//       this.applyFilter();
//     }
//   }

//   applyFilter(): void {
//     const q = (this.searchTerm || '').toLowerCase().trim();
//     this.filteredTutors = !q
//       ? [...this.tutors]
//       : this.tutors.filter(t => (t.name || '').toLowerCase().includes(q));
//   }

//   // ====== Chat flow ======
//   selectTutor(t: TutorItem): void {
//     this.currentChatId = t.id;
//     this.chatTitle = t.name || 'Chat';
//     // Prefill từ cache để không “mất tin” khi đổi phòng/F5
//     this.messages = this.getCachedMessages(t.id);
//     this.scrollToBottom();

//     this.loadHistory(t.id).then(() => {
//       if (this.enableWs) this.trySetupWebSocket(t.id).catch(() => this.startPolling(t.id));
//       else this.startPolling(t.id);
//     });
//   }

//   /** Hợp nhất serverList với local (giữ temp đến khi server xác nhận) */
//   private mergeWithLocal(serverList: Message[], tutorId: number): void {
//     const existing = this.getCachedMessages(tutorId).concat(
//       this.messages.filter(m => m.temp) // giữ temp nếu cache chưa có
//     );

//     const byId = new Map<number, Message>();
//     for (const m of existing) if (m.id > 0) byId.set(m.id, m);
//     for (const s of serverList) if (s.id > 0) byId.set(s.id, s); // server overwrite

//     const temps = existing.filter(m => m.temp);
//     const merged: Message[] = Array.from(byId.values());

//     for (const temp of temps) {
//       const matched = merged.some(s =>
//         s.type === 'sent' &&
//         s.text === temp.text &&
//         Math.abs((s.ts ?? 0) - (temp.ts ?? 0)) < 60_000
//       );
//       if (!matched) merged.push(temp);
//       else this.pendingTemps.delete(temp.id);
//     }

//     // thêm record không có id (nếu BE không set id)
//     for (const s of serverList) if (!s.id || s.id <= 0) merged.push(s);

//     merged.sort((a, b) => (a.ts ?? Date.parse(a.time)) - (b.ts ?? Date.parse(b.time)));

//     this.messages = merged;
//     this.putCachedMessages(tutorId, merged);
//     this.scrollToBottom();
//   }

//   /** Lịch sử: /api/chats/messages/{id}?page=0&size=200&sort=createdAt,asc (fallback /api/chat/...) */
//   private async loadHistory(tutorId: number): Promise<void> {
//     this.isLoadingHistory = true;

//     const toMsgs = (raw: any[]): Message[] => raw.map((r: any) => {
//       const ts = new Date(r.createdAt ?? r.timestamp ?? Date.now()).getTime();
//       return {
//         id: Number(r.id ?? 0),
//         sender: String(r.senderUserId ?? r.senderId ?? ''),
//         text: String(r.content ?? ''),
//         time: new Date(ts).toLocaleTimeString(),
//         ts,
//         type: (this.userId != null && Number(r.senderUserId ?? r.senderId) === Number(this.userId)) ? 'sent' : 'received'
//       };
//     });

//     const url = `${this.chatsMsgBase}/messages/${tutorId}?page=0&size=200&sort=createdAt,asc`;

//     await new Promise<void>((resolve) => {
//       this.http.get<any>(url, { headers: this.buildHeaders() })
//         .pipe(finalize(() => { this.isLoadingHistory = false; resolve(); }))
//         .subscribe({
//           next: (payload) => {
//             const listRaw = this.pickArray(payload);
//             this.mergeWithLocal(toMsgs(listRaw), tutorId);
//           },
//           error: () => {
//             const fb = `${this.chatFallbackBase}/messages/${tutorId}?page=0&size=200&sort=createdAt,asc`;
//             this.http.get<any>(fb, { headers: this.buildHeaders() })
//               .subscribe({
//                 next: (payload2) => {
//                   const listRaw2 = this.pickArray(payload2);
//                   this.mergeWithLocal(toMsgs(listRaw2), tutorId);
//                 }
//               });
//           }
//         });
//     });
//   }

//   /** Gửi tin (student → tutor) */
//   send(): void {
//     const text = (this.newMessage || '').trim();
//     if (!text || !this.currentChatId) return;

//     const now = Date.now();
//     const tempId = -now; // id âm cho tin local
//     const temp: Message = {
//       id: tempId,
//       sender: String(this.userId ?? 'me'),
//       text,
//       time: new Date(now).toLocaleTimeString(),
//       ts: now,
//       type: 'sent',
//       temp: true
//     };
//     this.pendingTemps.set(tempId, temp);
//     this.mergeWithLocal([temp], this.currentChatId); // hiển thị ngay + cache

//     this.newMessage = '';
//     this.isSending = true;

//     this.http.post(
//       `${this.studentChatBase}/send-message/${this.currentChatId}`,
//       { content: text },
//       { headers: this.buildHeaders() }
//     ).pipe(finalize(() => (this.isSending = false)))
//      .subscribe({
//        next: () => { /* chờ polling/WS add bản chính */ },
//        error: () => {
//          const idx = this.messages.findIndex(m => m.id === tempId);
//          if (idx > -1) {
//            this.messages[idx] = { ...this.messages[idx], text: this.messages[idx].text + ' (GỬI THẤT BẠI)' };
//            this.putCachedMessages(this.currentChatId!, this.messages);
//          }
//        }
//      });
//   }

//   /** WS room-id: /api/chats/room-id/{id} (fallback /api/chat/...) */
//   private async trySetupWebSocket(tutorId: number): Promise<void> {
//   let roomId: string | null = null;
//   try {
//     const r1 = await this.http.get<any>(
//       `${this.chatsMsgBase}/room-id/${tutorId}`,
//       { headers: this.buildHeaders() }
//     ).toPromise();
//     roomId = r1?.roomId ?? null;
//   } catch {}
//   if (!roomId) {
//     try {
//       const r2 = await this.http.get<any>(
//         `${this.chatFallbackBase}/room-id/${tutorId}`,
//         { headers: this.buildHeaders() }
//       ).toPromise();
//       roomId = r2?.roomId ?? null;
//     } catch {}
//   }
//   if (!roomId) throw new Error('no-roomid');
//   this.wsRoomId = roomId;

//   const wsBase = this.chatsMsgBase.replace('/api/chats', '') || this.chatFallbackBase.replace('/api/chat', '');
//   const endpoint = `${wsBase}/ws-chat`;

//  const socket = new SockJS(endpoint);
// const client = over(socket);   // KHÔNG còn (Stomp as any).over(socket)
// this.stompClient = client;

//   await new Promise<void>((resolve, reject) => {
//     client.connect({}, () => {
//       client.subscribe(`/topic/chat/${roomId}`, (frame: any) => {
//         try {
//           const msg = JSON.parse(frame.body);
//           const ts = new Date(msg.createdAt ?? Date.now()).getTime();
//           const incoming: Message = {
//             id: Number(msg.id ?? 0),
//             sender: String(msg.senderUserId ?? ''),
//             text: String(msg.content ?? ''),
//             time: new Date(ts).toLocaleTimeString(),
//             ts,
//             type: (Number(msg.senderUserId) === Number(this.userId)) ? 'sent' : 'received'
//           };
//           if (this.currentChatId) this.mergeWithLocal([incoming], this.currentChatId);
//         } catch {}
//       });
//       resolve();
//     }, (err: any) => reject(err));
//   });
// }

//   private startPolling(tutorId: number): void {
//     if (this.pollTimer) clearInterval(this.pollTimer);
//     this.pollTimer = setInterval(() => this.loadHistory(tutorId), 3000);
//   }

//   private disconnectWs(): void {
//     try { this.stompClient?.disconnect?.(() => {}); } catch {}
//     this.stompClient = null;
//     this.wsRoomId = null;
//   }
// }

