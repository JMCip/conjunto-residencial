import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from '../services/auth.service';
import { NotificacionService } from '../services/notificacion.service';

/** Ruta de inicio de sesión: es la única petición a la API que no lleva token. */
const RUTA_LOGIN = `${environment.apiUrl}/auth/login`;

/**
 * Interceptor de autenticación.
 * - Agrega el encabezado Authorization: Bearer <token> a las peticiones a la API (menos al login).
 * - Si la API responde 401, cierra la sesión, muestra un aviso y redirige a /login.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const notificacionService = inject(NotificacionService);

  const token = authService.getToken();
  const esApi = req.url.startsWith(environment.apiUrl);
  const esLogin = req.url.startsWith(RUTA_LOGIN);

  const peticion =
    token && esApi && !esLogin
      ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : req;

  return next(peticion).pipe(
    catchError((error: unknown) => {
      const es401 = error instanceof HttpErrorResponse && error.status === 401;

      if (es401 && esApi && !esLogin) {
        authService.logout();
        if (!router.url.startsWith('/login')) {
          notificacionService.info('Inicia sesión de nuevo para continuar.', 'Sesión finalizada');
          router.navigate(['/login']);
        }
      }

      return throwError(() => error);
    }),
  );
};
