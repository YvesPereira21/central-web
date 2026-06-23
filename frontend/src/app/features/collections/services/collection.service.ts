import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Collection, CollectionCreate } from '../../../core/models/collection';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Page } from '../../../core/models/page';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CollectionService {
  private apiUrl = `${environment.apiUrl}/collections`;
  private http = inject(HttpClient);

  createCollection(collectionCreate: CollectionCreate): Observable<Collection> {
    return this.http.post<Collection>(this.apiUrl, collectionCreate);
  }

  getMyCollections(page: number = 0, size: number = 15): Observable<Page<Collection>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<Collection>>(`${this.apiUrl}/my-collections`, { params });
  }

  getCollectionById(collectionId: string): Observable<Collection> {
    return this.http.get<Collection>(`${this.apiUrl}/${collectionId}`);
  }

  addArticleToCollection(collectionId: string, articleId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${collectionId}/articles/${articleId}`, {});
  }

  addQuestionToCollection(collectionId: string, questionId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${collectionId}/questions/${questionId}`, {});
  }

  removeArticleFromAllMyCollections(articleId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/articles/${articleId}`);
  }

  removeQuestionFromAllMyCollections(questionId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/questions/${questionId}`);
  }

  deleteCollection(collectionId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${collectionId}`);
  }
}
