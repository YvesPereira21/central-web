import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Answer, AnswerAccepted, AnswerCreate } from '../../models/answer';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AnswerService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/answers`;

  createAnswer(answerData: AnswerCreate): Observable<Answer> {
    return this.http.post<Answer>(this.apiUrl, answerData);
  }

  getAllAnswersFromQuestion(questionId: string): Observable<Answer[]> {
    return this.http.get<Answer[]>(`${this.apiUrl}/${questionId}`);
  }

  acceptAnswer(answerAccepted: AnswerAccepted): Observable<Answer> {
    return this.http.put<Answer>(this.apiUrl, answerAccepted);
  }

  deleteAnswer(answerId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${answerId}`);
  }
}
