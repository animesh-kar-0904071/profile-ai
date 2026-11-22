import { Component, ElementRef, inject, signal, viewChild } from '@angular/core';
import { ChatService } from './chat.service';

interface Message {
  role: 'user' | 'bot';
  text: string;
  error?: boolean;
}

@Component({
  selector: 'app-chat',
  templateUrl: './chat.html',
  styleUrl: './chat.scss',
})
export class Chat {
  private readonly chatService = inject(ChatService);
  private readonly log = viewChild<ElementRef<HTMLElement>>('log');

  readonly messages = signal<Message[]>([]);
  readonly draft = signal('');
  readonly busy = signal(false);

  readonly suggestions = [
    'What did he build at Vendasta?',
    "Tell me about his security research",
    "What's his tech stack?",
    'What does he do for fun?',
    'How can I contact him?',
  ];

  ask(question: string): void {
    this.draft.set(question);
    this.send();
  }

  send(): void {
    const text = this.draft().trim();
    if (!text || this.busy()) return;

    this.draft.set('');
    this.busy.set(true);
    this.messages.update((m) => [...m, { role: 'user', text }, { role: 'bot', text: '' }]);
    this.scrollDown();

    this.chatService.stream(text).subscribe({
      next: (chunk) => {
        this.patchLast((msg) => ({ ...msg, text: msg.text + chunk }));
        this.scrollDown();
      },
      error: (err: Error) => {
        this.patchLast((msg) => ({ ...msg, text: err.message, error: true }));
        this.busy.set(false);
      },
      complete: () => this.busy.set(false),
    });
  }

  private patchLast(change: (m: Message) => Message): void {
    this.messages.update((list) => {
      const copy = [...list];
      copy[copy.length - 1] = change(copy[copy.length - 1]);
      return copy;
    });
  }

  private scrollDown(): void {
    setTimeout(() => {
      const el = this.log()?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    });
  }
}