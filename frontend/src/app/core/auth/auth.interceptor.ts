import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthenticationService } from '../../features/authentications/services/authentication.service';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authenticationService = inject(AuthenticationService);
  const token = authenticationService.getToken();

  if (token) {
    const clonedRequest = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(clonedRequest);
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Se o backend disser que o token expirou ou está inválido
      if (error.status === 401 || error.status === 403) {
        authenticationService.logout(); // Limpa a sujeira e envia para a tela de login
      }
      return throwError(() => error);
    })
  );
};
