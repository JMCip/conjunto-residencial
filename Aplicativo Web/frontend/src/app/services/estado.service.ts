import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { InfoBdModel } from '../models/info-bd.model';

/** Rutas de los endpoints de diagnóstico (ya existen en el backend). */
const RUTA_ESTADO = '/estado';
const RUTA_BD = '/bd/prueba';

/**
 * Servicio que consulta si el backend responde y con qué datos está conectado a PostgreSQL.
 * No usa mocks: siempre pregunta al backend real.
 */
@Injectable({ providedIn: 'root' })
export class EstadoService {
  private cliente: HttpClient = inject(HttpClient);

  /**
   * Consulta si el backend está vivo.
   * @returns Observable con el texto plano que responde Javalin.
   */
  getEstado(): Observable<string> {
    return this.cliente.get(`${environment.apiUrl}${RUTA_ESTADO}`, { responseType: 'text' });
  }

  /**
   * Consulta usuario, base y versión de PostgreSQL.
   * @returns Observable con la información de la base.
   */
  getInfoBd(): Observable<InfoBdModel> {
    return this.cliente.get<InfoBdModel>(`${environment.apiUrl}${RUTA_BD}`);
  }
}
