import { Component, inject, OnInit, signal } from '@angular/core';
import { ArticleService } from '../../../features/articles/services/article.service';
import { Article } from '../../../features/models/article';

@Component({
  selector: 'app-article-list',
  imports: [],
  templateUrl: './article-list.component.html',
  styleUrl: './article-list.component.css'
})
export class ArticleListComponent implements OnInit {
  private articleService = inject(ArticleService);

  articles = signal<Article[]>([]);

  ngOnInit(): void {
    this.loadArticles();
  }

  loadArticles() {
    this.articleService.getAllPublishedArticles().subscribe({
      next: (data) => {
        this.articles.set(data);
      },
      error: (error) => {
        alert("Não foi possível encontrar todos os pontos");
      }
    })
  }
}
