import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private readonly url = '/api/chat/stream';

  stream(message: string): Observable<string> {
    return new Observable<string>((subscriber) => {
      const controller = new AbortController();

      (async () => {
        try {
          const res = await fetch(this.url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ message }),
            signal: controller.signal,
          });

          if (res.status === 429) {
            throw new Error('Too many messages. Please wait a moment and try again.');
          }
          if (!res.ok || !res.body) {
            throw new Error('Something went wrong. Please try again.');
          }

          const reader = res.body.getReader();
          const decoder = new TextDecoder();
          let buffer = '';

          while (true) {
            const { done, value } = await reader.read();
            if (done) break;

            buffer += decoder.decode(value, { stream: true }).replace(/\r\n/g, '\n');

            // each event ends with a blank line
            let end: number;
            while ((end = buffer.indexOf('\n\n')) !== -1) {
              const event = buffer.slice(0, end);
              buffer = buffer.slice(end + 2);

              const text = event
                .split('\n')
                .filter((line) => line.startsWith('data:'))
                .map((line) => line.slice(5))   // keep spaces: they belong to the text
                .join('\n');

              if (text) subscriber.next(text);
            }
          }
          subscriber.complete();
        } catch (e) {
          if ((e as Error).name === 'AbortError') return;
          const msg = e instanceof TypeError
            ? "Can't reach the assistant right now. Please try again later."
            : (e as Error).message;
          subscriber.error(new Error(msg));
        }
      })();

      return () => controller.abort();   // runs if the component unsubscribes
    });
  }
}