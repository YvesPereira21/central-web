import { Component, inject, Input, OnInit, signal } from '@angular/core';
import { ArticleService } from '../../../features/articles/services/article.service';
import { Article } from '../../../features/models/article';
import { ActivatedRoute, RouterLink } from "@angular/router";
import { PaginationComponent } from '../pagination/pagination.component';
import { CollectionModalComponent } from '../collection-modal/collection-modal.component';
import { CollectionService } from '../../../features/collections/services/collection.service';
import { AuthenticationService } from '../../../features/authentications/services/authentication.service';

@Component({
  selector: 'app-article-list',
  imports: [RouterLink, PaginationComponent, CollectionModalComponent],
  templateUrl: './article-list.component.html',
  styleUrl: './article-list.component.css'
})
export class ArticleListComponent implements OnInit {
  private articleService = inject(ArticleService);
  private collectionService = inject(CollectionService);
  private activatedRoute = inject(ActivatedRoute);
  authService = inject(AuthenticationService);

  @Input() profileId: string | null = null;
  @Input() showPagination: boolean = true;
  @Input() showCreateButton: boolean = true;

  isOpen = signal<boolean>(false);
  selectedArticleId = signal<string | undefined>(undefined);

  articles = signal<Article[]>([]);

  isEmpty = signal<boolean>(false);
  isFirst = signal<boolean>(true);
  isLast = signal<boolean>(false);
  pageSize = signal<number>(0);
  currentPage = signal<number>(0);
  numberOfElements = signal<number>(0);
  totalElements = signal<number>(0);
  totalPages = signal<number>(0);

  ngOnInit(): void {
    this.activatedRoute.paramMap.subscribe(params => {
      const routeId = params.get('id');
      if (routeId) {
        this.profileId = routeId;
      }

      this.loadData(0, 10);
    });
  }

  loadData(page: number, size: number) {
    if (this.profileId) {
      this.loadProfileArticles(page, size);
    } else {
      this.loadArticles(page, size);
    }
  }

  loadArticles(page: number, size: number) {
    this.articleService.getAllPublishedArticles(page, size).subscribe({
      next: (response) => {
        this.articles.set(response.content);
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

  loadProfileArticles(page: number, size: number) {
    if (!this.profileId) return

    this.articleService.getProfileArticles(this.profileId, page, size).subscribe({
      next: (response) => {
        this.articles.set(response.content);
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

  toggleArticleLike(articleId: string) {
    if (!articleId) return;

    this.articleService.toggleArticleLike(articleId).subscribe({
      next: () => {
        this.articles.update(articles =>
          articles.map(
            currentArticle => {
              if (currentArticle.articleId == articleId) {
                const willLike = !currentArticle.liked;

                return {
                  ...currentArticle,
                  liked: willLike,
                  articleTotalLikes: currentArticle.articleTotalLikes + (willLike ? 1 : -1)
                };
              }
              return currentArticle
            }
          )
        );
      },
      error: () => {
        alert("Não foi possível processar a curtida. Tente novamente.");
      }
    });
  }

  toggleSave(article: Article) {
    if (article.saved) {
      this.collectionService.removeArticleFromAllMyCollections(article.articleId).subscribe({
        next: () => {
          this.articles.update(articles =>
            articles.map(a => a.articleId === article.articleId ? { ...a, saved: false } : a)
          );
        },
        error: () => {
          alert('Erro ao remover o artigo das coleções.');
        }
      });
    } else {
      this.selectedArticleId.set(article.articleId);
      this.isOpen.set(true);
    }
  }

  onSaveSuccess(articleId: string) {
    this.articles.update(articles =>
      articles.map(a => a.articleId === articleId ? { ...a, saved: true } : a)
    );
  }

  deleteArticle(articleId: string) {
    if (confirm('Tem certeza de que deseja excluir este artigo?')) {
      this.articleService.deleteArticle(articleId).subscribe({
        next: () => {
          this.articles.update(articles => articles.filter(a => a.articleId !== articleId));
          alert('Artigo removido com sucesso!');
        },
        error: () => {
          alert('Erro ao remover o artigo. Tente novamente.');
        }
      });
    }
  }
}
