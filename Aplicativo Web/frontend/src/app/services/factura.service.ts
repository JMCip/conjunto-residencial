import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of, delay } from 'rxjs';
import { environment } from '../../environments/environment';
import { FacturaModel } from '../models/factura.model';
import { FACTURAS_MOCK } from '../mocks/facturas.mock';

/** Ruta provisional del recurso (se confirma cuando el backend la publique). */
const RUTA = '/facturas';

/**
 * Servicio que consulta las facturas de administración del conjunto.
 * Solo lectura: todas las peticiones son GET.
 */
@Injectable({ providedIn: 'root' })
export class FacturaService {
  private cliente: HttpClient = inject(HttpClient);

  /** URL base del recurso. Relativa: el proxy la reenvía al backend. */
  private readonly urlbase = `${environment.apiUrl}${RUTA}`;

  /**
   * Obtiene todas las facturas.
   * @returns Observable con la lista de facturas.
   */
  getTodas(): Observable<FacturaModel[]> {
    if (environment.usarMocks) return of(FACTURAS_MOCK).pipe(delay(300));
    return this.cliente.get<FacturaModel[]>(this.urlbase);
  }

  /**
   * Obtiene las facturas de una casa.
   * @param idCasa Identificador de la casa.
   * @returns Observable con las facturas de esa casa.
   */
  getPorCasa(idCasa: number): Observable<FacturaModel[]> {
    if (environment.usarMocks) {
      return of(FACTURAS_MOCK.filter((f) => f.idCasa === idCasa)).pipe(delay(300));
    }
    const parametros = new HttpParams().set('casa', idCasa);
    return this.cliente.get<FacturaModel[]>(this.urlbase, { params: parametros });
  }
}
