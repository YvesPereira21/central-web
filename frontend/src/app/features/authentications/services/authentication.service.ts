import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Observable, tap, throwError } from 'rxjs';
import { LoginRequest, LoginResponse } from '../../models/authentication';
import { Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private apiUrl = `${environment.apiUrl}/auth`

  isAuthenticated = signal<boolean>(this.hasToken());
  userRole = signal<string | null>(this.getUserRole());

  login(loginData: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, loginData).pipe(
      tap((response) => {
        this.saveTokens(response.token, response.refreshToken);
        this.isAuthenticated.set(true);
      })
    );
  }

  refreshToken(): Observable<any> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) {
      this.logout();
      return throwError(() => new Error("No refresh token available"));
    }

    return this.http.post<any>(`${this.apiUrl}/refresh`, { refreshToken }).pipe(
      tap((response) => {
        this.saveTokens(response.accessToken, response.refreshToken);
        this.isAuthenticated.set(true);
      })
    );
  }

  logout() {
    const refreshToken = this.getRefreshToken();
    if (refreshToken) {
      // Chama o backend para invalidar o refresh token
      this.http.post(`${this.apiUrl}/logout`, { refreshToken }).subscribe({
        next: () => this.clearStorageAndNavigate(),
        error: () => this.clearStorageAndNavigate()
      });
    } else {
      this.clearStorageAndNavigate();
    }
  }

  private clearStorageAndNavigate() {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    this.isAuthenticated.set(false);
    this.userRole.set(null);
    this.router.navigate(['/login']);
  }

  private saveTokens(token: string, refreshToken: string) {
    localStorage.setItem('token', token);
    localStorage.setItem('refreshToken', refreshToken);
    this.userRole.set(this.getUserRole());
  }

  getToken() {
    return localStorage.getItem('token');
  }

  getRefreshToken() {
    return localStorage.getItem('refreshToken');
  }

  private hasToken(): boolean {
    return !!this.getToken();
  }

  private getUserRole(): string | null {
    const token = localStorage.getItem('token');
    if (!token) return null;

    try {
      const decodedToken: any = jwtDecode(token)
      return decodedToken.role;
    } catch (error) {
      return null;
    }
  }

  private getAuthenticatedUserId(): string | null {
    const token = localStorage.getItem('token');
    if (!token) return null;

    try {
      const decodedToken: any = jwtDecode(token)
      return decodedToken.id;
    } catch (error) {
      return null;
    }
  }

  isAdmin(): boolean {
    return this.userRole() === 'ADMIN';
  }

  isPerson(): boolean {
    return this.userRole() === 'PERSON';
  }

  isOwner(userId: string): boolean {
    return this.getAuthenticatedUserId() === userId;
  }
}
