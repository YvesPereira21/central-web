import { Component, inject, Input, OnInit, signal } from '@angular/core';
import { ArticleService } from '../../../features/articles/services/article.service';
import { Article } from '../../../features/models/article';
import { ActivatedRoute, RouterLink } from "@angular/router";
import { PaginationComponent } from '../pagination/pagination.component';

@Component({
  selector: 'app-article-list',
  imports: [RouterLink, PaginationComponent],
  templateUrl: './article-list.component.html',
  styleUrl: './article-list.component.css'
})
export class ArticleListComponent implements OnInit {
  private articleService = inject(ArticleService);
  private activatedRoute = inject(ActivatedRoute);

  @Input() profileId: string | null = null;
  @Input() showPagination: boolean = true;

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
}
