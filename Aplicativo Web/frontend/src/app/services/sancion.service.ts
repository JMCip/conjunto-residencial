import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CambiosSancion, NuevaSancion, SancionModel } from '../models/sancion.model';

/**
 * Servicio que consulta y modifica las sanciones a través de /api/sanciones.
 * Todas las URL son relativas: el proxy de desarrollo las reenvía al backend.
 */
@Injectable({
  providedIn: 'root',
})
export class SancionService {
  /** Cliente HTTP de Angular para llamar al backend. */
  private cliente: HttpClient = inject(HttpClient);

  /** URL base del recurso (/api/sanciones). */
  private readonly urlbase = `${environment.apiUrl}/sanciones`;

  /**
   * Obtiene todas las sanciones.
   * @returns Observable con la lista completa.
   */
  getTodos(): Observable<SancionModel[]> {
    return this.cliente.get<SancionModel[]>(this.urlbase);
  }

  /**
   * Crea una sanción; queda EN_DESCARGOS y sin multa.
   * @param sancion Datos de la sanción.
   * @returns Observable con el mensaje y el id de la sanción creada.
   */
  crear(sancion: NuevaSancion): Observable<{ mensaje: string; idSancion: number }> {
    return this.cliente.post<{ mensaje: string; idSancion: number }>(this.urlbase, sancion);
  }

  /**
   * Avanza el proceso de una sanción EN_DESCARGOS: registra descargos, la confirma (con multa opcional) o la anula.
   * @param id Identificador de la sanción.
   * @param cambios Descargos, decisión o ambos.
   * @returns Observable con el mensaje del backend.
   */
  actualizar(id: number, cambios: CambiosSancion): Observable<{ mensaje: string }> {
    return this.cliente.put<{ mensaje: string }>(`${this.urlbase}/${id}`, cambios);
  }

  /**
   * Elimina una sanción ANULADA.
   * @param id Identificador de la sanción.
   * @returns Observable con el mensaje del backend.
   */
  eliminar(id: number): Observable<{ mensaje: string }> {
    return this.cliente.delete<{ mensaje: string }>(`${this.urlbase}/${id}`);
  }
}
