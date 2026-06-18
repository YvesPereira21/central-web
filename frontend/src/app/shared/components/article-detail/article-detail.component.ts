import { Component, inject, OnInit, signal } from '@angular/core';
import { ArticleService } from '../../../features/articles/services/article.service';
import { CollectionService } from '../../../features/collections/services/collection.service';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Article } from '../../../features/models/article';
import { CollectionModalComponent } from '../collection-modal/collection-modal.component';
import { LevelStageComponent } from '../level-stage/level-stage.component';
import { MarkdownComponent } from 'ngx-markdown';

@Component({
  selector: 'app-article-detail',
  imports: [RouterLink, CollectionModalComponent, LevelStageComponent, MarkdownComponent],
  templateUrl: './article-detail.component.html',
  styleUrl: './article-detail.component.css'
})
export class ArticleDetailComponent implements OnInit {
  private articleService = inject(ArticleService);
  private collectionService = inject(CollectionService);
  private activatedRoute = inject(ActivatedRoute);

  article = signal<Article | null>(null);
  articleId = signal<string>('');
  isOpen = signal<boolean>(false);
  errorMessage: string = '';

  ngOnInit(): void {
    const articleId = this.activatedRoute.snapshot.paramMap.get('id')
    if (articleId) {
      this.articleId.set(articleId);
      this.loadArticle(articleId);
    }
  }

  loadArticle(articleId: string) {
    this.articleService.getArticle(articleId).subscribe({
      next: (response) => {
        this.article.set(response);
      },
      error: (error) => {
        this.errorMessage = 'Erro ao carregar o artigo.';
        alert(this.errorMessage);
      }
    });
  }

  toggleArticleLike(articleId: string) {
    if (!articleId) return;

    this.articleService.toggleArticleLike(articleId).subscribe({
      next: () => {
        this.article.update(currentArticle => {
          if (!currentArticle) return null;

          const willLike = !currentArticle.liked;

          return {
            ...currentArticle,
            liked: willLike,
            articleTotalLikes: currentArticle.articleTotalLikes + (willLike ? 1 : -1)
          };
        });
      },
      error: () => {
        alert("Não foi possível processar a curtida. Tente novamente.");
      }
    });
  }

  toggleSave() {
    const currentArticle = this.article();
    if (!currentArticle) return;

    if (currentArticle.saved) {
      this.collectionService.removeArticleFromAllMyCollections(this.articleId()).subscribe({
        next: () => {
          this.article.update(a => a ? { ...a, saved: false } : null);
        },
        error: () => {
          alert('Erro ao remover o artigo das coleções.');
        }
      });
    } else {
      this.isOpen.set(true);
    }
  }

  onSaveSuccess() {
    this.article.update(a => a ? { ...a, saved: true } : null);
  }
}
