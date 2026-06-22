import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthenticationService } from '../../features/authentications/services/authentication.service';
import { catchError, switchMap, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authenticationService = inject(AuthenticationService);
  let token = authenticationService.getToken();

  let request = req;
  if (token) {
    request = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      // Ignora rotas de login e refresh para evitar loop infinito
      if (req.url.includes('/auth/login') || req.url.includes('/auth/refresh')) {
        return throwError(() => error);
      }

      // 401 ou 403 geralmente indicam token expirado
      if (error.status === 401 || error.status === 403) {
        return authenticationService.refreshToken().pipe(
          switchMap(() => {
            // Se o refresh funcionou, pega o novo token
            token = authenticationService.getToken();
            const newRequest = req.clone({
              setHeaders: {
                Authorization: `Bearer ${token}`
              }
            });
            // Tenta fazer a requisição original novamente
            return next(newRequest);
          }),
          catchError((refreshError) => {
            // Se o refresh falhar, usuário desloga
            authenticationService.logout();
            return throwError(() => refreshError);
          })
        );
      }

      return throwError(() => error);
    })
  );
};
