import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Article, ArticleCreate, ArticleUpdate } from '../../models/article';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ArticleService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/articles`;

  createArticle(articleData: ArticleCreate): Observable<Article> {
    return this.http.post<Article>(this.apiUrl, articleData);
  }

  getArticle(articleId: string): Observable<Article> {
    return this.http.get<Article>(`${this.apiUrl}/${articleId}`);
  }

  getAllPublishedArticles(): Observable<Article[]> {
    return this.http.get<Article[]>(this.apiUrl);
  }

  getArticlesByTitle(title: string): Observable<Article[]> {
    return this.http.get<Article[]>(`${this.apiUrl}/${title}/title`);
  }

  getArticlesByTag(technologyName: string): Observable<Article[]> {
    return this.http.get<Article[]>(`${this.apiUrl}/${technologyName}/tag`);
  }

  getProfileArticles(profileId: string): Observable<Article[]> {
    return this.http.get<Article[]>(`${this.apiUrl}/${profileId}/profile`);
  }

  updateArticle(articleId: string, articleUpdated: ArticleUpdate): Observable<Article> {
    return this.http.put<Article>(`${this.apiUrl}/${articleId}`, articleUpdated);
  }

  deleteArticle(articleId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${articleId}`)
  }
}
