import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Comment, CommentCreate, CommentUpdate } from '../../models/comment';
import { Page } from '../../models/page';
import { HttpClient, HttpParams } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class CommentService {
  private apiUrl = `${environment.apiUrl}/comments`;
  private http = inject(HttpClient);

  createComment(answerId: string, comment: CommentCreate): Observable<Comment> {
    return this.http.post<Comment>(`${this.apiUrl}/answer/${answerId}`, comment);
  }

  getCommentsByAnswer(answerId: string, page: number = 0, size: number = 10): Observable<Page<Comment>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<Comment>>(`${this.apiUrl}/answer/${answerId}`, { params });
  }

  updateComment(commentId: string, comment: CommentUpdate): Observable<Comment> {
    return this.http.put<Comment>(`${this.apiUrl}/${commentId}`, comment);
  }

  deleteComment(commentId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${commentId}`);
  }
}
