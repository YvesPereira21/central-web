import { inject, Injectable } from '@angular/core';
import { Question, QuestionCreate, QuestionUpdate } from '../../../core/models/question';
import { Observable } from 'rxjs';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Page } from '../../../core/models/page';

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

  getAllPublishedQuestions(page: number, size: number): Observable<Page<Question>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<Question>>(this.apiUrl, { params });
  }

  getProfileQuestions(profileId: string, page: number, size: number): Observable<Page<Question>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<Question>>(`${this.apiUrl}/${profileId}/profile`, { params });
  }

  searchQuestions(query: string, page: number, size: number): Observable<Page<Question>> {
    const params = new HttpParams()
      .set('query', query)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<Question>>(`${this.apiUrl}/search`, { params });
  }

  getQuestionsByTag(technologyName: string, page: number, size: number): Observable<Page<Question>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<Question>>(`${this.apiUrl}/${technologyName}/tag`, { params });
  }

  getQuestionsByTags(tags: string[], page: number, size: number): Observable<Page<Question>> {
    const params = new HttpParams()
      .set('tags', tags.join(','))
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<Question>>(`${this.apiUrl}/filter`, { params });
  }

  getQuestionsWithAcceptedAnswers(page: number, size: number): Observable<Page<Question>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<Question>>(`${this.apiUrl}/accepteds-answers`, { params });
  }

  updateQuestion(questionId: string, questionUpdated: QuestionUpdate): Observable<Question> {
    return this.http.put<Question>(`${this.apiUrl}/${questionId}`, questionUpdated);
  }

  toggleQuestionLike(questionId: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${questionId}/like`, null);
  }

  deleteQuestion(questionId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${questionId}`);
  }
}
