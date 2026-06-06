import { Component, inject, Input, signal } from '@angular/core';
import { AnswerService } from '../../../features/answers/services/answer.service';
import { Answer } from '../../../features/models/answer';
import { PaginationComponent } from '../pagination/pagination.component';
import { RouterLink } from "@angular/router";
import { CommentsAnswerComponent } from '../comments-answer/comments-answer.component';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';

@Component({
  selector: 'app-answers-question',
  imports: [RouterLink, PaginationComponent, CommentsAnswerComponent, ReactiveFormsModule],
  templateUrl: './answers-question.component.html',
  styleUrl: './answers-question.component.css'
})
export class AnswersQuestionComponent {
  private answerService = inject(AnswerService);
  private formBuilder = inject(FormBuilder);

  @Input({ required: true }) questionId!: string;

  @Input() isQuestionOwner: boolean = false;

  answers = signal<Answer[]>([]);
  isSubmitting = signal<boolean>(false);

  answerForm = this.formBuilder.group({
    content: ['', [Validators.required, Validators.minLength(10)]]
  });

  isEmpty = signal<boolean>(false);
  isFirst = signal<boolean>(true);
  isLast = signal<boolean>(false);
  pageSize = signal<number>(0);
  currentPage = signal<number>(0);
  numberOfElements = signal<number>(0);
  totalElements = signal<number>(0);
  totalPages = signal<number>(0);

  ngOnInit(): void {
    this.loadAnswersFromQuestion(0, 15);
  }

  loadAnswersFromQuestion(page: number, size: number) {
    this.answerService.getAllAnswersFromQuestion(this.questionId, page, size).subscribe({
      next: (response) => {
        this.answers.set(response.content);
        this.isEmpty.set(response.empty);
        this.isFirst.set(response.first);
        this.isLast.set(response.last);
        this.pageSize.set(response.size);
        this.currentPage.set(response.number);
        this.numberOfElements.set(response.numberOfElements)
        this.totalElements.set(response.totalElements);
        this.totalPages.set(response.totalPages);
      },
      error: (error) => {
        alert("Não foi possível encontrar todos os pontos");
      }
    })
  }

  submitAnswer() {
    if (this.answerForm.invalid) return;

    this.isSubmitting.set(true);
    const content = this.answerForm.value.content!;
    
    this.answerService.createAnswer(this.questionId, { content, questionId: this.questionId }).subscribe({
      next: (newAnswer) => {
        this.answers.update(answers => [newAnswer, ...answers]);
        this.answerForm.reset();
        this.isSubmitting.set(false);
      },
      error: () => {
        alert('Não foi possível enviar a resposta. Tente novamente.');
        this.isSubmitting.set(false);
      }
    });
  }

  toggleAnswerLike(answerId: string) {
    this.answerService.toggleAnswerLike(answerId).subscribe({
      next: () => {
        this.answers.update(answers =>
          answers.map(
            currentAnswer => {
              if (currentAnswer.answerId === answerId) {
                const willLike = !currentAnswer.liked

                return {
                  ...currentAnswer,
                  liked: willLike,
                  answerTotalLikes: currentAnswer.answerTotalLikes + (willLike ? 1 : -1)
                };
              }
              return currentAnswer
            },
          ),
        )
      },
      error: (erro) => {
        alert('Não foi possível processar essa curtida. Tente novamente')
      }
    })
  }

  acceptAnswer(answer: Answer) {
    if (!this.isQuestionOwner || answer.accepted) return;

    const confirmAccept = window.confirm("Tem certeza que deseja marcar esta resposta como a correta? Esta ação não poderá ser desfeita.");
    if (!confirmAccept) return;

    this.answerService.acceptAnswer(answer.answerId).subscribe({
      next: () => {
        this.answers.update(answers =>
          answers.map(
            currentAnswer => {
              if (currentAnswer.answerId === answer.answerId) {
                return { ...currentAnswer, accepted: true };
              }
              return { ...currentAnswer, accepted: false }; // Assuming only one answer can be accepted
            }
          )
        );
      },
      error: () => {
        alert('Não foi possível aceitar a resposta. Tente novamente.');
      }
    });
  }
}
