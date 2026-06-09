import { Component, inject, signal } from '@angular/core';
import { QuestionService } from '../../../features/questions/services/question.service';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Question } from '../../../features/models/question';
import { AnswersQuestionComponent } from '../answers-question/answers-question.component';
import { CollectionModalComponent } from '../collection-modal/collection-modal.component';
import { CollectionService } from '../../../features/collections/services/collection.service';
import { LevelStageComponent } from '../level-stage/level-stage.component';
import { AuthenticationService } from '../../../features/authentications/services/authentication.service';

@Component({
  selector: 'app-question-detail',
  imports: [RouterLink, AnswersQuestionComponent, CollectionModalComponent, LevelStageComponent],
  templateUrl: './question-detail.component.html',
  styleUrl: './question-detail.component.css'
})
export class QuestionDetailComponent {
  private questionService = inject(QuestionService);
  private collectionService = inject(CollectionService);
  private activatedRoute = inject(ActivatedRoute);

  private authenticationService = inject(AuthenticationService);

  question = signal<Question | null>(null);
  questionId = signal<string>('');
  isOpen = signal<boolean>(false);
  isQuestionOwner = signal<boolean>(false);
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
        if (this.authenticationService.isOwner(response.profile.userId)) {
          this.isQuestionOwner.set(true);
        }
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

  toggleSave() {
    const currentQuestion = this.question();
    if (!currentQuestion) return;

    if (currentQuestion.saved) {
      this.collectionService.removeQuestionFromAllMyCollections(this.questionId()).subscribe({
        next: () => {
          this.question.update(q => q ? { ...q, saved: false } : null);
        },
        error: () => {
          alert('Erro ao remover a pergunta das coleções.');
        }
      });
    } else {
      this.isOpen.set(true);
    }
  }

  onSaveSuccess() {
    this.question.update(q => q ? { ...q, saved: true } : null);
  }
}
