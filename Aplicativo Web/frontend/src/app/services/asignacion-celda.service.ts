import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AsignacionCeldaModel } from '../models/asignacion-celda.model';

/**
 * Servicio que consulta las asignaciones de celdas a casas a través de /api/asignaciones-celda.
 * Todas las URL son relativas: el proxy de desarrollo las reenvía al backend.
 */
@Injectable({
  providedIn: 'root',
})
export class AsignacionCeldaService {
  /** Cliente HTTP de Angular para llamar al backend. */
  private cliente: HttpClient = inject(HttpClient);

  /** URL base del recurso (/api/asignaciones-celda). */
  private readonly urlbase = `${environment.apiUrl}/asignaciones-celda`;

  /**
   * Obtiene todas las asignaciones de celdas.
   * @returns Observable con la lista completa.
   */
  getTodos(): Observable<AsignacionCeldaModel[]> {
    return this.cliente.get<AsignacionCeldaModel[]>(this.urlbase);
  }
}
