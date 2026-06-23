import { Component, inject, Input, OnInit, signal } from '@angular/core';
import { QuestionService } from '../../../features/questions/services/question.service';
import { QuestionList } from '../../../features/models/question';
import { ActivatedRoute, RouterLink } from "@angular/router";
import { PaginationComponent } from '../pagination/pagination.component';
import { CollectionModalComponent } from '../collection-modal/collection-modal.component';
import { LevelStageComponent } from '../level-stage/level-stage.component';
import { CollectionService } from '../../../features/collections/services/collection.service';
import { AuthenticationService } from '../../../features/authentications/services/authentication.service';
import { combineLatest } from 'rxjs';
import { MarkdownComponent } from 'ngx-markdown';
import { DatePipe } from '@angular/common';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-question-list',
  imports: [RouterLink, PaginationComponent, CollectionModalComponent, LevelStageComponent, MarkdownComponent, DatePipe],
  templateUrl: './question-list.component.html',
  styleUrl: './question-list.component.css'
})
export class QuestionListComponent implements OnInit {
  environment = environment;
  private questionService = inject(QuestionService);
  private collectionService = inject(CollectionService);
  private activatedRoute = inject(ActivatedRoute);
  authService = inject(AuthenticationService);

  @Input() profileId: string | null = null;
  @Input() tags: string[] = [];
  @Input() searchQuery: string | null = null;
  @Input() onlyAccepted: boolean = false;
  @Input() showPagination: boolean = true;
  @Input() showCreateButton: boolean = true;

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
    combineLatest([
      this.activatedRoute.paramMap,
      this.activatedRoute.queryParamMap
    ]).subscribe(([params, queryParams]) => {
      const routeId = params.get('id');
      if (routeId) {
        this.profileId = routeId;
      }

      const tagsParam = queryParams.get('tags');
      this.tags = tagsParam ? tagsParam.split(',') : [];

      this.searchQuery = queryParams.get('search');

      const acceptedParam = queryParams.get('accepted');
      this.onlyAccepted = acceptedParam === 'true';

      this.loadData(0, 10);
    });
  }

  loadData(page: number, size: number) {
    if (this.profileId) {
      this.loadProfileQuestions(page, size);
    } else if (this.onlyAccepted) {
      this.loadQuestionsWithAcceptedAnswers(page, size);
    } else if (this.searchQuery) {
      this.loadQuestionsBySearch(this.searchQuery, page, size);
    } else if (this.tags && this.tags.length > 0) {
      this.loadQuestionsByTags(this.tags, page, size);
    } else {
      this.loadAllQuestions(page, size);
    }
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
        alert("Não foi possível encontrar todas as perguntas");
      }
    });
  }

  loadQuestionsWithAcceptedAnswers(page: number, size: number) {
    this.questionService.getQuestionsWithAcceptedAnswers(page, size).subscribe({
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
        alert("Não foi possível encontrar as perguntas com respostas aceitas");
      }
    });
  }

  loadQuestionsBySearch(query: string, page: number, size: number) {
    this.questionService.searchQuestions(query, page, size).subscribe({
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
        alert("Não foi possível encontrar perguntas para sua pesquisa");
      }
    });
  }

  loadQuestionsByTags(tags: string[], page: number, size: number) {
    this.questionService.getQuestionsByTags(tags, page, size).subscribe({
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
        alert("Não foi possível encontrar as perguntas filtradas pelas tags");
      }
    });
  }

  loadProfileQuestions(page: number, size: number) {
    if (!this.profileId) return;

    this.questionService.getProfileQuestions(this.profileId, page, size).subscribe({
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
        alert("Não foi possível encontrar todas as perguntas do perfil");
      }
    });
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

  deleteQuestion(questionId: string) {
    if (confirm('Tem certeza de que deseja excluir esta pergunta?')) {
      this.questionService.deleteQuestion(questionId).subscribe({
        next: () => {
          this.questions.update(questions => questions.filter(q => q.questionId !== questionId));
          alert('Pergunta removida com sucesso!');
        },
        error: () => {
          alert('Erro ao remover a pergunta. Tente novamente.');
        }
      });
    }
  }
}
