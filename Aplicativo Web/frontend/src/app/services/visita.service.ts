import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { VisitaModel } from '../models/visita.model';

/**
 * Servicio que consulta y modifica las visitas a las casas a través de /api/visitas.
 * Todas las URL son relativas: el proxy de desarrollo las reenvía al backend.
 */
@Injectable({
  providedIn: 'root',
})
export class VisitaService {
  /** Cliente HTTP de Angular para llamar al backend. */
  private cliente: HttpClient = inject(HttpClient);

  /** URL base del recurso (/api/visitas). */
  private readonly urlbase = `${environment.apiUrl}/visitas`;

  /**
   * Obtiene todas las visitas.
   * @returns Observable con la lista completa.
   */
  getTodos(): Observable<VisitaModel[]> {
    return this.cliente.get<VisitaModel[]>(this.urlbase);
  }

  /**
   * Obtiene una visita por su identificador.
   * @param id Valor de idVisita.
   * @returns Observable con el registro.
   */
  getPorId(id: number): Observable<VisitaModel> {
    return this.cliente.get<VisitaModel>(`${this.urlbase}/${id}`);
  }

  /**
   * Crea una visita; el identificador lo genera la base de datos.
   * @param visita Datos del registro sin idVisita.
   * @returns Observable con el mensaje del backend y el idVisita creado.
   */
  crear(visita: Omit<VisitaModel, 'idVisita'>): Observable<{ mensaje: string; idVisita: number }> {
    return this.cliente.post<{ mensaje: string; idVisita: number }>(this.urlbase, visita);
  }

  /**
   * Actualiza una visita; el identificador va en la URL.
   * @param visita Registro con los datos nuevos.
   * @returns Observable con el mensaje del backend.
   */
  actualizar(visita: VisitaModel): Observable<{ mensaje: string }> {
    return this.cliente.put<{ mensaje: string }>(`${this.urlbase}/${visita.idVisita}`, visita);
  }

  /**
   * Elimina una visita.
   * @param id Valor de idVisita.
   * @returns Observable con el mensaje del backend.
   */
  eliminar(id: number): Observable<{ mensaje: string }> {
    return this.cliente.delete<{ mensaje: string }>(`${this.urlbase}/${id}`);
  }
}
