import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { environment } from '../../environments/environment';
import { SancionModel } from '../models/sancion.model';

/** Ruta provisional del recurso (se confirma cuando el backend la publique). */
const RUTA = '/sanciones';

/**
 * Servicio que consulta las sanciones a casas y residentes.
 * Todavía no tiene mock: con usarMocks en true devuelve una lista vacía (módulo pendiente).
 */
@Injectable({ providedIn: 'root' })
export class SancionService {
  private cliente: HttpClient = inject(HttpClient);

  /** URL base del recurso. Relativa: el proxy la reenvía al backend. */
  private readonly urlbase = `${environment.apiUrl}${RUTA}`;

  /**
   * Obtiene todas las sanciones.
   * @returns Observable con la lista de sanciones.
   */
  getTodas(): Observable<SancionModel[]> {
    if (environment.usarMocks) return of([]);
    return this.cliente.get<SancionModel[]>(this.urlbase);
  }
}
