import { Component, input, model, signal } from '@angular/core';
import { MarkdownComponent } from 'ngx-markdown';

@Component({
  selector: 'app-preview-mode',
  imports: [MarkdownComponent],
  templateUrl: './preview-mode.component.html',
  styleUrl: './preview-mode.component.css'
})
export class PreviewModeComponent {
  content = input<string | null | undefined>('');
}
