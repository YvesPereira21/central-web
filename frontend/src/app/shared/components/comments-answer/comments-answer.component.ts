import { Component, inject, Input, OnInit, signal, ViewChild, ElementRef } from '@angular/core';
import { CommentService } from '../../../features/comments/services/comment.service';
import { Comment, CommentCreate } from '../../../features/models/comment';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-comments-answer',
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './comments-answer.component.html',
  styleUrl: './comments-answer.component.css'
})
export class CommentsAnswerComponent implements OnInit {
  @Input() answerId!: string;
  @ViewChild('commentInput') commentInput!: ElementRef<HTMLTextAreaElement>;

  private commentService = inject(CommentService);
  private formBuilder = inject(FormBuilder);

  comments = signal<Comment[]>([]);
  isSubmiting = signal<boolean>(false);

  commentForm = this.formBuilder.group({
    content: ['', Validators.required]
  });

  ngOnInit(): void {
    this.loadComments();
  }

  loadComments() {
    if (!this.answerId) return;
    this.commentService.getCommentsByAnswer(this.answerId).subscribe({
      next: (data) => {
        this.comments.set(data.content);
      },
      error: (error) => {
        console.error('Erro ao buscar comentários', error);
      }
    });
  }

  onSubmit() {
    if (this.commentForm.invalid || !this.answerId) return;

    this.isSubmiting.set(true);

    const commentCreate: CommentCreate = {
      content: this.commentForm.value.content!,
      answerId: this.answerId
    };

    this.commentService.createComment(this.answerId, commentCreate).subscribe({
      next: (newComment) => {
        this.comments.update(current => [...current, newComment]);
        this.commentForm.reset();
        this.isSubmiting.set(false);
      },
      error: (error) => {
        console.error('Erro ao enviar comentário', error);
        alert('Erro ao enviar comentário. Tente novamente.');
        this.isSubmiting.set(false);
      }
    });
  }

  replyTo(username: string) {
    const currentContent = this.commentForm.value.content || '';
    this.commentForm.patchValue({
      content: `@${username} ${currentContent}`
    });

    if (this.commentInput) {
      this.commentInput.nativeElement.focus();
    }
  }
}
