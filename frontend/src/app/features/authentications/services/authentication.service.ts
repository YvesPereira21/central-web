import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Observable, tap, switchMap } from 'rxjs';
import { LoginRequest } from '../../../core/models/authentication';
import { Router } from '@angular/router';

export interface AuthUser {
  userId: string;
  email: string;
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private apiUrl = `${environment.apiUrl}/auth`;

  isAuthenticated = signal<boolean>(false);
  userRole = signal<string | null>(null);
  currentUserId = signal<string | null>(null);

  getAuthUser(): Observable<AuthUser> {
    return this.http.get<AuthUser>(`${this.apiUrl}/me`, { withCredentials: true });
  }

  login(loginData: LoginRequest): Observable<AuthUser> {
    return this.http.post<void>(`${this.apiUrl}/login`, loginData, { withCredentials: true }).pipe(
      switchMap(() => this.getAuthUser()),
      tap((user) => {
        this.isAuthenticated.set(true);
        this.userRole.set(user.role);
        this.currentUserId.set(user.userId);
        if (user.role === 'ADMIN') {
          this.router.navigate(['/admin/qualifications']);
        } else {
          this.router.navigate(['/']);
        }
      })
    );
  }

  refreshToken(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/refresh`, {}, { withCredentials: true }).pipe(
      tap(() => {
        this.isAuthenticated.set(true);
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
    return this.userRole() === 'PERSON';
  }

  isOwner(userId: string): boolean {
    return !!this.currentUserId() && this.currentUserId() === userId;
  }
}
