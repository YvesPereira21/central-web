import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Observable, tap } from 'rxjs';
import { LoginRequest } from '../../../core/models/authentication';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private apiUrl = `${environment.apiUrl}/auth`;

  isAuthenticated = signal<boolean>(false);
  userRole = signal<string | null>('PERSON');
  currentUserId = signal<string | null>(null);

  login(loginData: LoginRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/login`, loginData, { withCredentials: true }).pipe(
      tap(() => {
        this.isAuthenticated.set(true);
        this.userRole.set('PERSON');
      })
    );
  }

  refreshToken(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/refresh`, {}, { withCredentials: true }).pipe(
      tap(() => {
        this.isAuthenticated.set(true);
        this.userRole.set('PERSON');
      })
    );
  }

  logout(): void {
    this.http.post<void>(`${this.apiUrl}/logout`, {}, { withCredentials: true }).subscribe({
      next: () => this.clearStateAndNavigate(),
      error: () => this.clearStateAndNavigate()
    });
  }

  clearStateAndNavigate(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    this.isAuthenticated.set(false);
    this.userRole.set(null);
    this.currentUserId.set(null);
    this.router.navigate(['/login']);
  }

  setCurrentUserId(userId: string | null): void {
    this.currentUserId.set(userId);
  }

  isAdmin(): boolean {
    return this.userRole() === 'ADMIN';
  }

  isPerson(): boolean {
    return true; // Por padrão usuários logados são PERSON
  }

  isOwner(userId: string): boolean {
    return !!this.currentUserId() && this.currentUserId() === userId;
  }
}
