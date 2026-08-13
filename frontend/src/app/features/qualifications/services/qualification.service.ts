import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Qualification, QualificationCreate } from '../../../core/models/qualification';
import { Page } from '../../../core/models/page';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class QualificationService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/qualifications`;

  createQualification(qualificationData: QualificationCreate): Observable<Qualification> {
    return this.http.post<Qualification>(this.apiUrl, qualificationData);
  }

  getVerifiedQualificationsPaged(page: number = 0, size: number = 10): Observable<Page<Qualification>> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<Page<Qualification>>(`${this.apiUrl}/verified`, { params });
  }

  getNotVerifiedQualificationsPaged(page: number = 0, size: number = 10): Observable<Page<Qualification>> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<Page<Qualification>>(`${this.apiUrl}/not-verified`, { params });
  }

  getProfileVerifiedQualifications(profileId: string): Observable<Page<Qualification>> {
    return this.http.get<Page<Qualification>>(`${this.apiUrl}/${profileId}/verified`);
  }

  getProfileNotVerifiedQualifications(profileId: string): Observable<Page<Qualification>> {
    return this.http.get<Page<Qualification>>(`${this.apiUrl}/${profileId}/not-verified`);
  }

  verifyQualification(qualificationId: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${qualificationId}`, {});
  }

  deleteQualification(qualificationId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${qualificationId}`);
  }
}
