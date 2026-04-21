import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Qualification, QualificationCreate } from '../../models/qualification';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class QualificationService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/qualifications`;

  createQualification(qualificationData: QualificationCreate): Observable<Qualification> {
    return this.http.post<Qualification>(`${this.apiUrl}/qualifications`, qualificationData);
  }

  getVerifiedQualifications(): Observable<Qualification[]> {
    return this.http.get<Qualification[]>(`${this.apiUrl}/verified`);
  }

  getNotVerifiedQualifications(): Observable<Qualification[]> {
    return this.http.get<Qualification[]>(`${this.apiUrl}/not-verified`);
  }

  getProfileVerifiedQualifications(profileId: string): Observable<Qualification[]> {
    return this.http.get<Qualification[]>(`${this.apiUrl}/${profileId}/verified`);
  }

  getProfileNotVerifiedQualifications(profileId: string): Observable<Qualification[]> {
    return this.http.get<Qualification[]>(`${this.apiUrl}/${profileId}/not-verified`);
  }

  deleteQualification(qualificationId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${qualificationId}`);
  }
}
