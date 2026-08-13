import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthenticationService } from '../../features/authentications/services/authentication.service';
import { catchError, switchMap, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authenticationService = inject(AuthenticationService);

  // Adiciona withCredentials: true em todas as requisições para enviar e receber cookies HttpOnly
  const request = req.clone({
    withCredentials: true
  });

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      // Ignora rotas de autenticação para evitar loops infinitos
      if (req.url.includes('/auth/login') || req.url.includes('/auth/refresh')) {
        return throwError(() => error);
      }

      // 401 ou 403 indicam token invalido/expirado
      if (error.status === 401 || error.status === 403) {
        return authenticationService.refreshToken().pipe(
          switchMap(() => {
            // Tenta fazer a requisição original novamente após a renovação dos cookies
            const newRequest = req.clone({
              withCredentials: true
            });
            return next(newRequest);
          }),
          catchError((refreshError) => {
            authenticationService.clearStateAndNavigate();
            return throwError(() => refreshError);
          })
        );
      }

      return throwError(() => error);
    })
  );
};
