import { Component, inject, OnInit } from '@angular/core';
import { ArticleService } from '../../../features/articles/services/article.service';
import { ActivatedRoute } from '@angular/router';
import { Article } from '../../../features/models/article';

@Component({
  selector: 'app-article-detail',
  imports: [],
  templateUrl: './article-detail.component.html',
  styleUrl: './article-detail.component.css'
})
export class ArticleDetailComponent implements OnInit {
  private articleService = inject(ArticleService);
  private activatedRoute = inject(ActivatedRoute);

  article: Article | null = null;
  errorMessage: string = '';

  ngOnInit(): void {
    const articleId = this.activatedRoute.snapshot.paramMap.get('id')
    if (articleId) {
      this.loadArticle(articleId);
    }
  }

  loadArticle(articleId: string) {
    this.articleService.getArticle(articleId).subscribe({
      next: (response) => {
        this.article = response;
      },
      error: (error) => {
        this.errorMessage = 'Erro ao carregar o artigo.';
        alert(this.errorMessage);
      }
    });
  }
}
