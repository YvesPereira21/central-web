import { inject, Injectable } from '@angular/core';
import { Question, QuestionCreate, QuestionUpdate } from '../../models/question';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class QuestionService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/questions`;

  createQuestion(questionData: QuestionCreate): Observable<Question> {
    return this.http.post<Question>(this.apiUrl, questionData);
  }

  getQuestion(questionId: string): Observable<Question> {
    return this.http.get<Question>(`${this.apiUrl}/${questionId}`);
  }

  getAllPublishedQuestions(): Observable<Question[]> {
    return this.http.get<Question[]>(this.apiUrl);
  }

  getQuestionsByTitle(title: string): Observable<Question[]> {
    return this.http.get<Question[]>(`${this.apiUrl}/${title}/title`);
  }

  getQuestionsByTag(technologyName: string): Observable<Question[]> {
    return this.http.get<Question[]>(`${this.apiUrl}/${technologyName}/tag`);
  }

  getQuestionsWithAcceptedAnswers(): Observable<Question[]> {
    return this.http.get<Question[]>(`${this.apiUrl}/accepteds-answers`);
  }

  updateQuestion(questionId: string, questionUpdated: QuestionUpdate): Observable<Question> {
    return this.http.put<Question>(`${this.apiUrl}/${questionId}`, questionUpdated);
  }

  deleteQuestion(questionId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${questionId}`);
  }
}
