import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Article, ArticleCreate, ArticleUpdate } from '../../models/article';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page } from '../../models/page';

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

  getAllPublishedArticles(page: number, size: number): Observable<Page<Article>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<Article>>(this.apiUrl, { params });
  }

  searchArticles(query: string, page: number, size: number): Observable<Page<Article>> {
    const params = new HttpParams()
      .set('query', query)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<Article>>(`${this.apiUrl}/search`, { params });
  }

  getArticlesByTag(technologyName: string, page: number, size: number): Observable<Page<Article>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<Article>>(`${this.apiUrl}/${technologyName}/tag`, { params });
  }

  getArticlesByTags(tags: string[], page: number, size: number): Observable<Page<Article>> {
    const params = new HttpParams()
      .set('tags', tags.join(','))
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<Article>>(`${this.apiUrl}/filter`, { params });
  }

  getProfileArticles(profileId: string, page: number, size: number): Observable<Page<Article>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<Article>>(`${this.apiUrl}/${profileId}/profile`, { params });
  }

  updateArticle(articleId: string, articleUpdated: ArticleUpdate): Observable<Article> {
    return this.http.put<Article>(`${this.apiUrl}/${articleId}`, articleUpdated);
  }

  toggleArticleLike(articleId: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${articleId}/like`, null);
  }

  deleteArticle(articleId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${articleId}`)
  }
}
