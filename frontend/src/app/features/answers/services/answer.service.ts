import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Answer, AnswerAccepted, AnswerCreate } from '../../models/answer';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page } from '../../models/page';

@Injectable({
  providedIn: 'root'
})
export class AnswerService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/answers`;

  createAnswer(answerData: AnswerCreate): Observable<Answer> {
    return this.http.post<Answer>(this.apiUrl, answerData);
  }

  getAllAnswersFromQuestion(questionId: string, page: number, size: number): Observable<Page<Answer>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<Answer>>(`${this.apiUrl}/${questionId}`, { params });
  }

  acceptAnswer(answerAccepted: AnswerAccepted): Observable<Answer> {
    return this.http.put<Answer>(this.apiUrl, answerAccepted);
  }

  toggleAnswerLike(answerId: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${answerId}/like`, null);
  }

  deleteAnswer(answerId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${answerId}`);
  }
}
