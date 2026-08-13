import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthenticationService } from '../../features/authentications/services/authentication.service';
import { ProfileService } from '../../features/profiles/services/profile.service';
import { catchError, map, of } from 'rxjs';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthenticationService);
  const profileService = inject(ProfileService);
  const router = inject(Router);
  const expectedRoles: string[] = route.data ? route.data['roles'] : undefined;

  // 1. Se o serviço já possui o estado autenticado e o role definido em memória
  if (authService.isAuthenticated() && authService.userRole()) {
    const currentRole = authService.userRole()!;
    if (!expectedRoles || expectedRoles.length === 0) return true;
    if (expectedRoles.includes(currentRole)) return true;
    return router.parseUrl('/');
  }

  // 2. Se falta o perfil/role ou se a página recarregou (F5)
  return profileService.getMyProfile().pipe(
    map((profile) => {
      authService.isAuthenticated.set(true);
      authService.userRole.set('PERSON');
      if (profile.userId) {
        authService.setCurrentUserId(profile.userId);
      }
      const currentRole = authService.userRole() || 'PERSON';
      if (!expectedRoles || expectedRoles.length === 0) return true;
      if (expectedRoles.includes(currentRole)) return true;
      return router.parseUrl('/');
    }),
    catchError(() => {
      authService.isAuthenticated.set(false);
      authService.userRole.set(null);
      return of(router.parseUrl('/login'));
    })
  );
};
