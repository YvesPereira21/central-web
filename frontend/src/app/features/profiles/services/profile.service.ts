import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Profile, ProfileCreate, ProfileUpdate } from '../../../core/models/profile';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/profiles`;

  public profileUpdated$ = new Subject<void>();

  createProfile(profileData: ProfileCreate): Observable<Profile> {
    return this.http.post<Profile>(this.apiUrl, profileData);
  }

  uploadAvatar(profileId: string, file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${environment.apiUrl}/photos/${profileId}/avatar`, formData, { responseType: 'text' })
      .pipe(tap(() => this.profileUpdated$.next()));
  }

  getProfile(profileId: string): Observable<Profile> {
    return this.http.get<Profile>(`${this.apiUrl}/${profileId}`);
  }

  getMyProfile(): Observable<Profile> {
    return this.http.get<Profile>(`${this.apiUrl}/me`);
  }

  updateProfile(profileId: string, profileUpdated: ProfileUpdate): Observable<Profile> {
    return this.http.put<Profile>(`${this.apiUrl}/${profileId}`, profileUpdated)
      .pipe(tap(() => this.profileUpdated$.next()));
  }

  deleteProfile(profileId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${profileId}`);
  }
}
