import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthenticationService } from '../../features/authentications/services/authentication.service';
import { catchError, map, of } from 'rxjs';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthenticationService);
  const router = inject(Router);
  const expectedRoles: string[] = route.data ? route.data['roles'] : undefined;

  // 1. Se o serviço já possui o estado autenticado e o role definido em memória
  if (authService.isAuthenticated() && authService.userRole()) {
    const currentRole = authService.userRole()!;

    // Se o usuário ADMIN tentar acessar a Home dos usuários comum (/), retrata para /admin/qualifications
    if (currentRole === 'ADMIN' && state.url === '/') {
      return router.parseUrl('/admin/qualifications');
    }

    if (!expectedRoles || expectedRoles.length === 0) return true;
    if (expectedRoles.includes(currentRole)) return true;
    return router.parseUrl(currentRole === 'ADMIN' ? '/admin/qualifications' : '/');
  }

  // 2. Se a página foi recarregada (F5) ou falta autenticação
  return authService.getAuthUser().pipe(
    map((user) => {
      authService.isAuthenticated.set(true);
      authService.userRole.set(user.role);
      authService.setCurrentUserId(user.userId);

      if (user.role === 'ADMIN' && state.url === '/') {
        return router.parseUrl('/admin/qualifications');
      }

      if (!expectedRoles || expectedRoles.length === 0) return true;
      if (expectedRoles.includes(user.role)) return true;
      return router.parseUrl(user.role === 'ADMIN' ? '/admin/qualifications' : '/');
    }),
    catchError(() => {
      authService.isAuthenticated.set(false);
      authService.userRole.set(null);
      return of(router.parseUrl('/login'));
    })
  );
};
