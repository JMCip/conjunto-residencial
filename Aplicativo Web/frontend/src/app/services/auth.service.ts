import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoginRequestModel, LoginResponseModel } from '../models/auth.model';

/** Clave con la que se guarda el token en sessionStorage. */
const CLAVE_TOKEN = 'token_conjunto';

/**
 * Servicio de autenticación: inicia y cierra la sesión, guarda el token JWT y
 * revisa si sigue vigente.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  /** Cliente HTTP de Angular para llamar al backend. */
  private cliente: HttpClient = inject(HttpClient);

  /** URL base de autenticación (/api/auth). */
  private readonly urlbase = `${environment.apiUrl}/auth`;

  /**
   * Inicia sesión y guarda el token recibido.
   * @param usuario Nombre de usuario.
   * @param clave Contraseña.
   * @returns Observable con el token y el rol.
   */
  login(usuario: string, clave: string): Observable<LoginResponseModel> {
    const cuerpo: LoginRequestModel = { usuario, clave };
    return this.cliente
      .post<LoginResponseModel>(`${this.urlbase}/login`, cuerpo)
      .pipe(tap((respuesta) => sessionStorage.setItem(CLAVE_TOKEN, respuesta.token)));
  }

  /** Cierra la sesión borrando el token guardado. */
  logout(): void {
    sessionStorage.removeItem(CLAVE_TOKEN);
  }

  /**
   * Devuelve el token guardado.
   * @returns El token, o null si no hay sesión.
   */
  getToken(): string | null {
    return sessionStorage.getItem(CLAVE_TOKEN);
  }

  /**
   * Indica si hay una sesión vigente.
   * @returns true si existe un token y su fecha de expiración todavía no ha pasado.
   */
  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) return false;
    const exp = this.leerExpiracion(token);
    return exp !== null && exp * 1000 > Date.now();
  }

  /**
   * Lee la fecha de expiración (claim exp) del token sin validarlo.
   * @param token Token JWT.
   * @returns Segundos desde 1970 en que vence el token, o null si no se puede leer.
   */
  private leerExpiracion(token: string): number | null {
    try {
      const parteCentral = token.split('.')[1];
      const json = atob(parteCentral.replace(/-/g, '+').replace(/_/g, '/'));
      return JSON.parse(json).exp ?? null;
    } catch {
      return null;
    }
  }
}
