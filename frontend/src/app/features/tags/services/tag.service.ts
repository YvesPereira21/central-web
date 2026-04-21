import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Tag, TagUpdate } from '../../models/tag';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TagService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/tags`;

  createTag(tagData: Tag): Observable<Tag> {
    return this.http.post<Tag>(this.apiUrl, tagData);
  }

  updateTag(tagId: string, tagUpdated: TagUpdate): Observable<Tag> {
    return this.http.put<Tag>(`${this.apiUrl}/${tagId}`, tagUpdated);
  }

  deleteTag(tagId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${tagId}`);
  }
}
