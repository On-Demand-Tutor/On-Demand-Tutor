import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-admin-embed',
  standalone: true,
  imports: [CommonModule],
  template: `
    <iframe
      title="Admin Preview"
      src="/assets/admin/index.html"
      style="display:block;width:100%;height:100vh;border:0;"
    ></iframe>
  `,
})
export class AdminEmbedComponent {}