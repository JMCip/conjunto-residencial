import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Protege las pantallas de la aplicación: si no hay una sesión vigente, borra el token
 * y redirige a /login.
 */
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) return true;

  authService.logout();
  return router.createUrlTree(['/login']);
};

/** Evita mostrar el login a quien ya tiene una sesión vigente: lo redirige a /inicio. */
export const loginGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.isLoggedIn() ? router.createUrlTree(['/inicio']) : true;
};
