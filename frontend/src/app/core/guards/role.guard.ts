import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthenticationService } from '../../features/authentications/services/authentication.service';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthenticationService);
  const router = inject(Router);
  const expectedRoles: string[] = route.data['roles'];
  const currentRole = authService.userRole();

  if (!authService.isAuthenticated() || !currentRole) return router.parseUrl('/login');

  if (!expectedRoles || expectedRoles.length === 0) return true;

  if (expectedRoles.includes(currentRole)) return true;

  return router.parseUrl('/')
};
