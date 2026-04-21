import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Profile, ProfileCreate, ProfileUpdate } from '../../models/profile';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/profiles`;

  createProfile(profileData: ProfileCreate): Observable<Profile> {
    return this.http.post<Profile>(this.apiUrl, profileData);
  }

  getProfile(profileId: string): Observable<Profile> {
    return this.http.get<Profile>(`${this.apiUrl}/${profileId}`);
  }

  updateProfile(profileId: string, profileUpdated: ProfileUpdate): Observable<Profile> {
    return this.http.put<Profile>(`${this.apiUrl}/${profileId}`, profileUpdated);
  }

  deleteProfile(profileId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${profileId}`);
  }
}
