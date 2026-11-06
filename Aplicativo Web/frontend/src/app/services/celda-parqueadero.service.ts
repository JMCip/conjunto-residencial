import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CeldaParqueaderoModel } from '../models/celda-parqueadero.model';

/**
 * Servicio que consulta y modifica las celdas del parqueadero a través de /api/celdas.
 * Todas las URL son relativas: el proxy de desarrollo las reenvía al backend.
 */
@Injectable({
  providedIn: 'root',
})
export class CeldaParqueaderoService {
  /** Cliente HTTP de Angular para llamar al backend. */
  private cliente: HttpClient = inject(HttpClient);

  /** URL base del recurso (/api/celdas). */
  private readonly urlbase = `${environment.apiUrl}/celdas`;

  /**
   * Obtiene todas las celdas del parqueadero.
   * @returns Observable con la lista completa.
   */
  getTodos(): Observable<CeldaParqueaderoModel[]> {
    return this.cliente.get<CeldaParqueaderoModel[]>(this.urlbase);
  }

  /**
   * Obtiene una celda por su identificador.
   * @param id Valor de idCelda.
   * @returns Observable con el registro.
   */
  getPorId(id: number): Observable<CeldaParqueaderoModel> {
    return this.cliente.get<CeldaParqueaderoModel>(`${this.urlbase}/${id}`);
  }

  /**
   * Crea una celda; el identificador lo genera la base de datos.
   * @param celda Datos del registro sin idCelda.
   * @returns Observable con el mensaje del backend y el idCelda creado.
   */
  crear(celda: Omit<CeldaParqueaderoModel, 'idCelda'>): Observable<{ mensaje: string; idCelda: number }> {
    return this.cliente.post<{ mensaje: string; idCelda: number }>(this.urlbase, celda);
  }

  /**
   * Actualiza una celda; el identificador va en la URL.
   * @param celda Registro con los datos nuevos.
   * @returns Observable con el mensaje del backend.
   */
  actualizar(celda: CeldaParqueaderoModel): Observable<{ mensaje: string }> {
    return this.cliente.put<{ mensaje: string }>(`${this.urlbase}/${celda.idCelda}`, celda);
  }

  /**
   * Elimina una celda.
   * @param id Valor de idCelda.
   * @returns Observable con el mensaje del backend.
   */
  eliminar(id: number): Observable<{ mensaje: string }> {
    return this.cliente.delete<{ mensaje: string }>(`${this.urlbase}/${id}`);
  }

}
