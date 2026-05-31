import { Component, inject, Input, signal } from '@angular/core';
import { AnswerService } from '../../../features/answers/services/answer.service';
import { Answer } from '../../../features/models/answer';
import { PaginationComponent } from '../pagination/pagination.component';
import { RouterLink } from "@angular/router";
import { CommentsAnswerComponent } from '../comments-answer/comments-answer.component';

@Component({
  selector: 'app-answers-question',
  imports: [RouterLink, PaginationComponent, CommentsAnswerComponent],
  templateUrl: './answers-question.component.html',
  styleUrl: './answers-question.component.css'
})
export class AnswersQuestionComponent {
  private answerService = inject(AnswerService);

  @Input({ required: true }) questionId!: string;

  answers = signal<Answer[]>([]);

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
}
