import { Component, inject, signal } from '@angular/core';
import { QuestionService } from '../../../features/questions/services/question.service';
import { ActivatedRoute } from '@angular/router';
import { Question } from '../../../features/models/question';
import { AnswersQuestionComponent } from '../answers-question/answers-question.component';

@Component({
  selector: 'app-question-detail',
  imports: [AnswersQuestionComponent],
  templateUrl: './question-detail.component.html',
  styleUrl: './question-detail.component.css'
})
export class QuestionDetailComponent {
  private questionService = inject(QuestionService);
  private activatedRoute = inject(ActivatedRoute);

  idRoute = signal<string>('');
  question: Question | null = null;
  errorMessage: string = '';

  ngOnInit(): void {
    const questionId = this.activatedRoute.snapshot.paramMap.get('id')
    if (questionId) {
      this.idRoute.set(questionId);
      this.loadQuestion(questionId);
    }
  }

  loadQuestion(questionId: string) {
    this.questionService.getQuestion(questionId).subscribe({
      next: (response) => {
        this.question = response;
      },
      error: (error) => {
        this.errorMessage = 'Erro ao carregar a pergunta.';
        alert(this.errorMessage);
      }
    });
  }
}
