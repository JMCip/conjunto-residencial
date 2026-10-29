import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { environment } from '../../environments/environment';
import { ReservaModel } from '../models/reserva.model';

/** Ruta provisional del recurso (se confirma cuando el backend la publique). */
const RUTA = '/reservas';

/**
 * Servicio que consulta las reservas del salón comunal.
 * Todavía no tiene mock: con usarMocks en true devuelve una lista vacía (módulo pendiente).
 */
@Injectable({ providedIn: 'root' })
export class ReservaService {
  private cliente: HttpClient = inject(HttpClient);

  /** URL base del recurso. Relativa: el proxy la reenvía al backend. */
  private readonly urlbase = `${environment.apiUrl}${RUTA}`;

  /**
   * Obtiene todas las reservas.
   * @returns Observable con la lista de reservas.
   */
  getTodas(): Observable<ReservaModel[]> {
    if (environment.usarMocks) return of([]);
    return this.cliente.get<ReservaModel[]>(this.urlbase);
  }
}
