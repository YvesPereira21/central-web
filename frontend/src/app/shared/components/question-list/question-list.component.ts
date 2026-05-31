import { Component, inject, OnInit, signal } from '@angular/core';
import { QuestionService } from '../../../features/questions/services/question.service';
import { QuestionList } from '../../../features/models/question';
import { RouterLink } from "@angular/router";
import { PaginationComponent } from '../pagination/pagination.component';
import { CollectionModalComponent } from '../collection-modal/collection-modal.component';
import { CollectionService } from '../../../features/collections/services/collection.service';

@Component({
  selector: 'app-question-list',
  imports: [RouterLink, PaginationComponent, CollectionModalComponent],
  templateUrl: './question-list.component.html',
  styleUrl: './question-list.component.css'
})
export class QuestionListComponent implements OnInit {
  private questionService = inject(QuestionService);
  private collectionService = inject(CollectionService);

  questions = signal<QuestionList[]>([]);
  isOpen = signal<boolean>(false);
  selectedQuestionId = signal<string | undefined>(undefined);

  isEmpty = signal<boolean>(false);
  isFirst = signal<boolean>(true);
  isLast = signal<boolean>(false);
  pageSize = signal<number>(0);
  currentPage = signal<number>(0);
  numberOfElements = signal<number>(0);
  totalElements = signal<number>(0);
  totalPages = signal<number>(0);

  ngOnInit(): void {
    this.loadAllQuestions(0, 1);
  }

  loadAllQuestions(page: number, size: number) {
    this.questionService.getAllPublishedQuestions(page, size).subscribe({
      next: (response) => {
        this.questions.set(response.content);
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

  toggleQuestionLike(questionId: string) {
    if (!questionId) return;

    this.questionService.toggleQuestionLike(questionId).subscribe({
      next: () => {
        this.questions.update(questions =>
          questions.map(
            currentQuestion => {
              if (currentQuestion.questionId == questionId) {
                const willLike = !currentQuestion.liked;

                return {
                  ...currentQuestion,
                  liked: willLike,
                  questionTotalLikes: currentQuestion.questionTotalLikes + (willLike ? 1 : -1)
                };
              }
              return currentQuestion
            }
          )
        );
      },
      error: () => {
        alert("Não foi possível processar a curtida. Tente novamente.");
      }
    });
  }

  toggleSave(question: QuestionList) {
    if (question.saved) {
      this.collectionService.removeQuestionFromAllMyCollections(question.questionId).subscribe({
        next: () => {
          this.questions.update(questions =>
            questions.map(q => q.questionId === question.questionId ? { ...q, saved: false } : q)
          );
        },
        error: () => {
          alert('Erro ao remover a pergunta das coleções.');
        }
      });
    } else {
      this.selectedQuestionId.set(question.questionId);
      this.isOpen.set(true);
    }
  }

  onSaveSuccess(questionId: string) {
    this.questions.update(questions =>
      questions.map(q => q.questionId === questionId ? { ...q, saved: true } : q)
    );
  }
}
