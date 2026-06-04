import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Observable, tap } from 'rxjs';
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
        this.saveToken(response.token);
        this.isAuthenticated.set(true);
      })
    );
  }

  logout() {
    localStorage.removeItem('token');
    this.isAuthenticated.set(false);
    this.userRole.set(this.getUserRole());
    this.router.navigate(['/login']);
  }

  private saveToken(token: string) {
    localStorage.setItem('token', token);
    this.userRole.set(this.getUserRole());
  }

  getToken() {
    return localStorage.getItem('token');
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
