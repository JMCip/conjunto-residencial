import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { environment } from '../../environments/environment';
import { CasaModel } from '../models/casa.model';

/** Ruta provisional del recurso (se confirma cuando el backend la publique). */
const RUTA = '/casas';

/**
 * Servicio que consulta las casas del conjunto.
 * Todavía no tiene mock: con usarMocks en true devuelve una lista vacía (módulo pendiente).
 */
@Injectable({ providedIn: 'root' })
export class CasaService {
  private cliente: HttpClient = inject(HttpClient);

  /** URL base del recurso. Relativa: el proxy la reenvía al backend. */
  private readonly urlbase = `${environment.apiUrl}${RUTA}`;

  /**
   * Obtiene todas las casas.
   * @returns Observable con la lista de casas.
   */
  getTodas(): Observable<CasaModel[]> {
    if (environment.usarMocks) return of([]);
    return this.cliente.get<CasaModel[]>(this.urlbase);
  }
}
