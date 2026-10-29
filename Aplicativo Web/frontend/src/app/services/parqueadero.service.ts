import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { environment } from '../../environments/environment';
import { ParqueaderoModel } from '../models/parqueadero.model';

/** Ruta provisional del recurso (se confirma cuando el backend la publique). */
const RUTA = '/parqueaderos';

/**
 * Servicio que consulta los parqueaderos y su asignación.
 * Todavía no tiene mock: con usarMocks en true devuelve una lista vacía (módulo pendiente).
 */
@Injectable({ providedIn: 'root' })
export class ParqueaderoService {
  private cliente: HttpClient = inject(HttpClient);

  /** URL base del recurso. Relativa: el proxy la reenvía al backend. */
  private readonly urlbase = `${environment.apiUrl}${RUTA}`;

  /**
   * Obtiene todos los parqueaderos.
   * @returns Observable con la lista de parqueaderos.
   */
  getTodos(): Observable<ParqueaderoModel[]> {
    if (environment.usarMocks) return of([]);
    return this.cliente.get<ParqueaderoModel[]>(this.urlbase);
  }
}
