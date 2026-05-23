import { Component, inject, signal } from '@angular/core';
import { QuestionService } from '../../../features/questions/services/question.service';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Question } from '../../../features/models/question';
import { AnswersQuestionComponent } from '../answers-question/answers-question.component';

@Component({
  selector: 'app-question-detail',
  imports: [RouterLink, AnswersQuestionComponent],
  templateUrl: './question-detail.component.html',
  styleUrl: './question-detail.component.css'
})
export class QuestionDetailComponent {
  private questionService = inject(QuestionService);
  private activatedRoute = inject(ActivatedRoute);

  question = signal<Question | null>(null);
  questionId = signal<string>('');
  errorMessage: string = '';

  ngOnInit(): void {
    const questionId = this.activatedRoute.snapshot.paramMap.get('id')
    if (questionId) {
      this.questionId.set(questionId);
      this.loadQuestion(questionId);
    }
  }

  loadQuestion(questionId: string) {
    this.questionService.getQuestion(questionId).subscribe({
      next: (response) => {
        this.question.set(response);
      },
      error: (error) => {
        this.errorMessage = 'Erro ao carregar a pergunta.';
        alert(this.errorMessage);
      }
    });
  }

  toggleQuestionLike(questionId: string) {
    if (!questionId) return;

    this.questionService.toggleQuestionLike(questionId).subscribe({
      next: () => {
        this.question.update(currentQuestion => {
          if (!currentQuestion) return null;

          const willLike = !currentQuestion.liked;

          return {
            ...currentQuestion,
            liked: willLike,
            questionTotalLikes: currentQuestion.questionTotalLikes + (willLike ? 1 : -1)
          };
        });
      },
      error: () => {
        alert("Não foi possível processar a curtida. Tente novamente.");
      }
    });
  }
}
